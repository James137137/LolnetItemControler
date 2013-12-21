package nz.co.LolNet.commandsign.config;

import nz.co.LolNet.commandsign.CommandSigns;
import nz.co.LolNet.commandsign.util.YamlLoader;

import org.bukkit.configuration.Configuration;

public class Config extends ConfigStore {

	public Config(CommandSigns plugin) {
		super(plugin);
	}

	public void load() {
		Configuration config = YamlLoader.loadResource(plugin, "config.yml");

		for (String k : config.getKeys(true)) {
			if (!config.isConfigurationSection(k)) {
				this.put(k, config.getString(k));
			}
		}
	}

}
