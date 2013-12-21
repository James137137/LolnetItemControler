package nz.co.LolNet.commandsign.handler;

import nz.co.LolNet.commandsign.CommandSigns;
import nz.co.LolNet.commandsign.SignExecutor;

public class PermissionHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		plugin = e.getPlugin();
		if (e.getPlayer() != null && CommandSigns.permission != null
				&& CommandSigns.permission.isEnabled()
				&& command.startsWith("&")) {
			boolean allowed = false;
			for (String s : command.substring(1).split(",")) {
				allowed = allowed
						|| e.getPlugin().hasPermission(e.getPlayer(), s, false);
			}
			if (allowed ^ negate) {
				e.getRestrictions().push(true);
			} else {
				e.getRestrictions().push(false);
				if (!silent)
					plugin.messenger.sendMessage(e.getPlayer(),
							"restriction.not_permitted");
			}
		}
	}

}
