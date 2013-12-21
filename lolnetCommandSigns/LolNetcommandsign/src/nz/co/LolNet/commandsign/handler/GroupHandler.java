package nz.co.LolNet.commandsign.handler;

import java.util.Arrays;
import java.util.List;

import nz.co.LolNet.commandsign.CommandSigns;
import nz.co.LolNet.commandsign.SignExecutor;

public class GroupHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		plugin = e.getPlugin();
		if (e.getPlayer() != null && CommandSigns.permission != null
				&& CommandSigns.permission.isEnabled()
				&& command.startsWith("@")) {
			boolean allowed = false;
			List<String> groups = Arrays.asList(CommandSigns.permission
					.getPlayerGroups(e.getPlayer()));
			for (String s : command.substring(1).split(",")) {
				allowed = allowed || groups.contains(s);
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
