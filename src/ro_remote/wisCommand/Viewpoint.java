package ro_remote.wisCommand;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TimerTask;

import packet.Packet;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;
import ro_remote.ClientBot;

public class Viewpoint {
	private ClientBot bot;
	private PacketManager manager;
	
	public Viewpoint(ClientBot bot) {
		this.bot = bot;
		this.manager = bot.manager;
	}
	
	PacketListener lister1;
	
	int x, y;
	
	public void run(int x1, int y1) {
		this.x = x1;
		this.y = y1;
		
		// clif_changemap
		/// Notifies the client of a position change to coordinates on given map (ZC_NPCACK_MAPMOVE).
		/// 0091 <map name>.16B <x>.W <y>.W
		manager.addListener(lister1 = new PacketListenerByCmd(2, 0x0091) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				bot.timer.schedule(new TimerTask() {

					@Override
					public void run() {
						try {
							bot.clif.viewpoint(0, 1, x, y, 1, 0x0000FF);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				}, 500);
			}
			
		});
		
		bot.timer.schedule(new TimerTask() {

			@Override
			public void run() {
				manager.removeListener(lister1);
			}
			
		}, 60000);

		// trigger clif_changemap
		Packet buf1 = Packet.create(22);
		buf1.setW(0, 0x0091);
		try {
			buf1.setString(2, 16, "");
		} catch (UnsupportedEncodingException e) {
		}
		buf1.setW(18, 0);
		buf1.setW(20, 0);
		manager.triggerEvent(2, buf1, 0x0091, 22);
	}
}
