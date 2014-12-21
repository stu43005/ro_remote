package ro_remote.wisCommand;

import ro_remote.ClientBot;
import ro_remote.ClientHandler;

public class Test {
	private ClientBot bot;
	private ClientHandler handler;
	
	public Test(ClientBot bot) {
		this.bot = bot;
		this.handler = bot.handler;
	}
	
	public void run() {
		bot.skill.addSkill(28, 10, handler.account_id, 0);
	}
}
