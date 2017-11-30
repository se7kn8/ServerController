package sebe3012.servercontroller.addon.craftbukkit;

import sebe3012.servercontroller.addon.api.Addon;
import sebe3012.servercontroller.addon.vanilla.VanillaServer;

import javafx.beans.property.StringProperty;

import java.util.Map;

public class CraftbukkitServer extends VanillaServer {
	private StringProperty bukkitConfig;

	public CraftbukkitServer(Map<String, StringProperty> properties, Addon addon){
		super(properties, addon);

		bukkitConfig = properties.get("bukkit");
	}

	@Override
	public int getSaveVersion() {
		return 1;
	}

	@Override
	public String getServerInfo() {
		return super.getServerInfo();
	}

	public String getBukkitConfig() {
		return bukkitConfig.get();
	}

}
