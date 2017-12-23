package sebe3012.servercontroller.api.addon;

/**
 * Created by Sebe3012 on 28.02.2017.
 * Basic type for all addons
 */
public abstract class Addon {

	private AddonInfo addonInfo;
	private boolean loaded;

	protected Addon() {
	}

	public final void loadAddon(AddonRegistry registry) {
		if (!loaded) {
			load(registry);
			loaded = true;
		} else {
			throw new RuntimeException("Addon is already loaded!");
		}
	}

	public final void unloadAddon(AddonRegistry registry) {
		if (loaded) {
			unload(registry);
			loaded = false;
		} else {
			throw new RuntimeException("Addon is already unloaded");
		}
	}

	public final boolean isLoaded() {
		return loaded;
	}

	protected abstract void load(AddonRegistry registry);

	protected abstract void unload(AddonRegistry registry);

	public final void setAddonInfo(AddonInfo addonInfo) {
		this.addonInfo = addonInfo;
	}

	public final AddonInfo getAddonInfo() {
		return addonInfo;
	}
}