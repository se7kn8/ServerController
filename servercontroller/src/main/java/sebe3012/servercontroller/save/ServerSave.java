package sebe3012.servercontroller.save;

import sebe3012.servercontroller.ServerController;
import sebe3012.servercontroller.addon.Addons;
import sebe3012.servercontroller.addon.api.Addon;
import sebe3012.servercontroller.addon.api.AddonUtil;
import sebe3012.servercontroller.gui.FrameHandler;
import sebe3012.servercontroller.gui.tab.Tabs;
import sebe3012.servercontroller.gui.tree.TreeEntry;
import sebe3012.servercontroller.preferences.PreferencesConstants;
import sebe3012.servercontroller.preferences.ServerControllerPreferences;
import sebe3012.servercontroller.server.BasicServer;
import sebe3012.servercontroller.server.Servers;
import sebe3012.servercontroller.util.DialogUtil;
import sebe3012.servercontroller.util.FileUtil;
import sebe3012.servercontroller.util.I18N;
import sebe3012.servercontroller.util.settings.Settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerSave {

	public static void saveServerController(){
		String file = FileUtil.openFileChooser("*.xml", ".xml", true);

		try {
			ServerSave.saveServerController(file, true);
		} catch (IOException e) {
			e.printStackTrace();
			showSaveErrorDialog();
		}
	}

	private static Logger log = LogManager.getLogger();

	private static void saveServerController(String path, boolean showDialog) throws IOException {

		Task<Void> saveTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				FrameHandler.showBar();
				log.info("Start saving");

				if (path != null) {
					ServerControllerPreferences.saveSetting(PreferencesConstants.LAST_SERVERS, path);
				}

				Servers.serversList.forEach(item -> {
					if (item.getItem().isRunning()) {
						log.warn("Can't save while server is running");
						showServerIsRunningDialog();
						return;
					}
				});

				FileOutputStream fos = new FileOutputStream(new File(path));

				final Element rootElement = new Element("servercontroller");
				rootElement.setAttribute("servercontroller", ServerController.VERSION);

				Document xml = new Document(rootElement);

				int max = Servers.serversList.size();

				for (int i = 0; i < max; i++) {
					updateProgress(i, max);

					TreeEntry<BasicServer> item = Servers.serversList.get(i);

					BasicServer server = item.getItem();

					log.info("Start saving server {}", server.getName());
					final Element serverElement = new Element("server");

					log.debug("Addon id from server {} is {}", server.getName(), server.getAddon().getAddonInfo().getId());
					serverElement.setAttribute("addon", server.getAddon().getAddonInfo().getId());
					serverElement.setAttribute("addonVersion", String.valueOf(server.getSaveVersion()));
					log.debug("Save version from Server {} is {}", server.getName(), server.getSaveVersion());

					Map<String, StringProperty> saveMap = Servers.getServerProperties(server);

					saveMap.forEach((key, value) -> {
						log.debug("Save entry from server {} is '{}' with value '{}'", server.getName(), key, value);
						Element keyElement = new Element(key);
						keyElement.setText(value.get());
						serverElement.addContent(keyElement);
					});

					rootElement.addContent(serverElement);
					log.info("Finished saving server {}", server.getName());

				}

				XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

				out.output(xml, fos);

				fos.close();

				log.info("Finished saving");

				if (showDialog) {
					Platform.runLater(() -> DialogUtil.showInformationAlert(I18N.translate("dialog_information"), "", I18N.translate("dialog_save_successful")));
				}

				FrameHandler.hideBar();
				return null;
			}
		};

		FrameHandler.currentProgress.progressProperty().bind(saveTask.progressProperty());

		saveTask.setOnFailed(event -> log.error("Can't save servers", saveTask.getException()));
		new Thread(saveTask).start();
	}

	public static void loadServerController(){
		String file = FileUtil.openFileChooser("*.xml", ".xml");

		try {
			ServerSave.loadServerController(file, true);

		} catch (IllegalStateException e) {
			e.printStackTrace();
			showSaveStateErrorDialog();
		} catch (JDOMException | IOException | IllegalArgumentException | ReflectiveOperationException e) {
			e.printStackTrace();
			showSaveErrorDialog();
		}
	}

	private static void loadServerController(String path, boolean showDialog) throws JDOMException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Task<Void> loadTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				FrameHandler.showBar();

				log.info("Start loading");

				if (path == null || !Files.exists(Paths.get(path))) {
					log.info("Can't load servers, because xml path is invalid");
					return null;
				}

				ServerControllerPreferences.saveSetting(PreferencesConstants.LAST_SERVERS, path);

				Servers.serversList.forEach(item -> {
					if (item.getItem().isRunning()) {
						log.warn("Can't load while server is running");
						showServerIsRunningDialog();
						return;
					}
				});

				Platform.runLater(Tabs::removeAllTabs);

				FileInputStream fis = new FileInputStream(new File(path));

				Document xml = new SAXBuilder().build(fis);

				Element serverController = xml.getRootElement();

				List<Element> elementList = serverController.getChildren("server");

				int counter = 0;
				int max = elementList.size();

				for (Element serverElement : elementList) {
					counter++;

					updateProgress(counter, max);
					log.info("Start loading {}", serverElement.getName());
					String addonId = serverElement.getAttributeValue("addon");
					log.debug("Plugin is {}", addonId);
					long saveVersion = Long.valueOf(serverElement.getAttributeValue("addonVersion"));

					Addon serverAddon = Addons.addonForID(addonId);

					Class<? extends BasicServer> serverClass = AddonUtil.getServerTypes().get(serverAddon);

					if (serverClass == null) {
						log.warn("No plugin found with name: {}", addonId);
						Platform.runLater(() -> DialogUtil.showErrorAlert(I18N.translate("dialog_error"), "", I18N.format("dialog_save_no_plugin", addonId)));
					}

					Map<String, StringProperty> map = new HashMap<>();

					for (Element e : serverElement.getChildren()) {
						log.debug("Load server information '{}' with value '{}'", e.getName(), e.getValue());
						map.put(e.getName(), new SimpleStringProperty(e.getValue()));
					}

					if (serverClass == null) {
						log.warn("Server-class is null. Can't load server");
						continue;
					}


					BasicServer server = Servers.createBasicServer(map, serverClass);

					log.info("Create server");
					if (server.getSaveVersion() != saveVersion) {
						throw new IllegalStateException("The save type of the server has been changed");
					}
					Servers.addServer(server, serverAddon);


				}

				fis.close();

				if (showDialog) {
					Platform.runLater(() -> DialogUtil.showInformationAlert(I18N.translate("dialog_information"), "", I18N.translate("dialog_load_successful")));
				}

				log.info("Finished loading");

				FrameHandler.hideBar();
				return null;
			}
		};

		FrameHandler.currentProgress.progressProperty().bind(loadTask.progressProperty());
		loadTask.setOnFailed(event -> log.error("Can't save servers", loadTask.getException()));
		new Thread(loadTask).start();


	}

	public static void loadServerControllerFromLastFile(){
		if ((boolean) Settings.readSetting(Settings.Constants.AUTO_LOAD_SERVERS)) {
			try {
				ServerSave.loadServerController(ServerControllerPreferences.loadSetting(PreferencesConstants.LAST_SERVERS, null), false);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				showSaveStateErrorDialog();
			} catch (JDOMException | IOException | IllegalArgumentException | ReflectiveOperationException e) {
				e.printStackTrace();
				showSaveErrorDialog();
			}
		}
	}

	private static void showServerIsRunningDialog() {
		Platform.runLater(() -> DialogUtil.showWaringAlert(I18N.translate("dialog_warning"), "", "dialog_save_servers_running"));
	}

	private static void showSaveErrorDialog() {
		DialogUtil.showErrorAlert(I18N.translate("dialog_error"), "", I18N.translate("dialog_save_error"));
	}

	private static void showSaveStateErrorDialog() {
		DialogUtil.showErrorAlert(I18N.translate("dialog_error"), "", I18N.translate("dialog_wrong_save_version"));
	}

}