package nz.co.LolNet.commandsign.handler;

import nz.co.LolNet.commandsign.CommandSigns;
import nz.co.LolNet.commandsign.SignExecutor;

public abstract class Handler {

	protected CommandSigns plugin;

	public abstract void handle(SignExecutor e, String command, boolean silent,
			boolean negate);

}
