package nz.co.LolNet.commandsign.util;

import java.io.File;
import java.io.IOException;

import nz.co.LolNet.commandsign.CommandSigns;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlLoader {

	/**
	 * Loads or updates a YAML configuration file
	 * 
	 * @param plugin
	 * @param filename
	 * @return
	 */
	public static Configuration loadResource(CommandSigns plugin,
			String filename) {
		File f = new File(plugin.getDataFolder(), filename);

		// Load the included file
		FileConfiguration included = YamlConfiguration.loadConfiguration(plugin
				.getResource(filename));

		// Write the included file if an external one doens't exist
		if (!f.exists()) {
			plugin.getLogger().info("Creating default " + filename + ".");
			plugin.saveResource(filename, true);
		}

		// Load the external file
		Configuration external = YamlConfiguration.loadConfiguration(f);


		
			// Write the file to disk
			try {
				included.save(f);
			} catch (IOException e) {
				plugin.getLogger().info("Could not update " + filename + ".");
			}

			// Copy the new configuration back into external
			external = included;

		return external;
	}

}
