package sebe3012.servercontroller.addon;

import sebe3012.servercontroller.addon.api.Addon;
import sebe3012.servercontroller.addon.api.AddonInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Sebe3012 on 01.03.2017.
 * The loader class for all addons
 */
public final class AddonLoader {

	AddonLoader() {
	}

	public static final Path ADDON_PATH = Paths.get(System.getProperty("user.home"), ".servercontroller", "addons");
	public static final PathMatcher JAR_FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.jar");
	public static final List<Path> JAR_PATHS = new ArrayList<>();
	public static final BiMap<String, Addon> ADDONS = HashBiMap.create();

	private List<AddonInfo> addonsToLoad = new ArrayList<>();
	private List<AddonInfo> addonsToLoadSorted = new ArrayList<>();

	private Gson gson = new Gson();
	private Logger log = LogManager.getLogger();

	private static boolean loadingFlag = false;

	void searchAddons() {
		searchJars();
		searchAddonInfo();
	}

	void loadAddons() {

		if (!loadingFlag) {
			calculateDependencies();

			URL[] urls = new URL[addonsToLoadSorted.size()];

			for (int i = 0; i < urls.length; i++) {
				try {
					urls[i] = addonsToLoadSorted.get(i).getJarPath().toUri().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
			for (AddonInfo info : addonsToLoadSorted) {
				loadAddon(info, loader);
			}

			loadingFlag = true;
		} else {
			throw new RuntimeException("Addons already loaded");
		}
	}

	private void loadAddon(AddonInfo info, URLClassLoader loader) {
		try {
			String mainClass = info.getMainClass();

			Class<?> clazz = loader.loadClass(mainClass);
			Addon addon = (Addon) clazz.newInstance();
			addon.setAddonInfo(info);

			if (!addon.isLoaded()) {
				log.info("Start to load addon '{}'", info.getId());
				addon.loadAddon();
				log.info("Addon '{}' is loaded", info.getId());
			} else {
				log.warn("Addon '{}' is already loaded", info.getId());
			}

			ADDONS.put(info.getId(), addon);
		} catch (ClassNotFoundException e) {
			log.error("Can't continue loading addon '" + info.getId() + "', because wrong main-class in addon.json: " + e);
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("Can't continue loading addon '" + info.getId() + "', because somethings is wrong in the addon main-class: " + e);
		} catch (AbstractMethodError e) {
			log.error("Can't continue loading addon '" + info.getId() + "', because there are compatibility problems: " + e);
		}
	}

	void unloadAddons() {
		for (Addon addon : ADDONS.values()) {
			addon.unloadAddon();
		}
	}

	private void searchJars() {
		try {
			Files.createDirectories(ADDON_PATH);

			for (Path path : Files.newDirectoryStream(ADDON_PATH)) {
				if (JAR_FILE_MATCHER.matches(path)) {
					log.info("Found jar at: {}", path);
					JAR_PATHS.add(path);
				}
			}
		} catch (IOException e) {
			log.error("Can't continue searching jars, because: ", e);
		}

	}

	private void searchAddonInfo() {

		List<String> loadedIds = new ArrayList<>();

		for (Path jarPath : JAR_PATHS) {
			try {
				JarFile file = new JarFile(jarPath.toFile());

				JarEntry addonInfo = file.getJarEntry("addon.json");

				if (addonInfo == null) {
					log.warn("Can't load '{}', because addon.json was not found. Check your addon", jarPath);
					continue;
				}


				AddonInfo info = gson.fromJson(new InputStreamReader(file.getInputStream(addonInfo)), AddonInfo.class);

				info.setJarPath(jarPath);

				if(loadedIds.contains(info.getId())){
					log.warn("Addon '{}' will not load, because it's already registered", info.getJarPath());
					continue;
				}

				loadedIds.add(info.getId());

				log.info("Loading addon info: {}", info.getId());

				addonsToLoad.add(info);
			} catch (IOException e) {
				log.error("Can't continue searching addon.json in jarfile " + jarPath + ", because: ", e);
			}
		}
	}

	private void calculateDependencies() {

		List<String> resolvedDependencies = new ArrayList<>();

		//Test to find  addons with no dependencies
		for (AddonInfo info : addonsToLoad) {
			if (info.getDependencies().isEmpty()) {
				addonsToLoadSorted.add(info);
				resolvedDependencies.add(info.getId());
			}
		}


		//Removed resolved dependencies
		addonsToLoad.removeAll(addonsToLoadSorted);

		while (true) {

			boolean thingsChanged = false;

			for (AddonInfo info : addonsToLoad) {

				log.debug("Try to resolve dependencies for addon '{}'", info.getId());

				boolean allDependenciesFound = false;

				for (String s : info.getDependencies()) {
					if (resolvedDependencies.contains(s)) {
						allDependenciesFound = true;
					} else {
						allDependenciesFound = false;
						break;
					}
				}

				if (allDependenciesFound) {
					log.debug("Found all dependencies for addon '{}'", info.getId());
					addonsToLoadSorted.add(info);
					resolvedDependencies.add(info.getId());
					thingsChanged = true;
				}
			}

			addonsToLoad.removeAll(addonsToLoadSorted);

			if (!thingsChanged) {
				break;
			}

		}

		for (AddonInfo info : addonsToLoad) {
			log.warn("Couldn't find all dependencies for addon '{}'. The addon will not load", info.getId());
		}

	}

}
