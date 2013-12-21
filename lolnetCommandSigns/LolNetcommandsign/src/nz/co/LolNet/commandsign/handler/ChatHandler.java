package nz.co.LolNet.commandsign.handler;
import nz.co.LolNet.commandsign.SignExecutor;

public class ChatHandler extends Handler {

	@Override
	public void handle(SignExecutor e, String command, boolean silent,
			boolean negate) {
		if (e.getPlayer() != null && command.startsWith(".")) {
			command = command.substring(1);
			e.getPlayer().chat(command);
		}
	}

}
