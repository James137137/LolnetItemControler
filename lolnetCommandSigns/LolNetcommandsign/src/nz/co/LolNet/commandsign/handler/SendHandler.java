package nz.co.LolNet.commandsign.handler;

import nz.co.LolNet.commandsign.SignExecutor;

import org.bukkit.ChatColor;

public class SendHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		if (e.getPlayer() != null && command.startsWith("\\")) {
			command = command.substring(1);
			e.getPlayer().sendMessage(
					ChatColor.translateAlternateColorCodes('&', command));
		}
	}

}
