package ro_remote.wisCommand;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.TimerTask;

import packet.packet_db;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;
import ro_remote.ClientBot;
import ro_remote.ClientHandler;

import common.MobList;

public class FindMob {
	private ClientBot bot;
	private ClientHandler handler;
	private PacketManager manager;
	
	public FindMob(ClientBot bot) {
		this.bot = bot;
		this.handler = bot.handler;
		this.manager = bot.manager;
	}

	LinkedList<PacketListener> lister = new LinkedList<PacketListener>(); 
	
	int mob_id;
	String map = null;
	int find = 0;
	boolean fly = false;
	boolean isBigFly = true;
	
	public void run(int w2) {
		this.mob_id = w2;
		
		// clif_set_unit_walking
		/// Prepares 'unit walking' packet
		// clif_set_unit_idle
		/// Prepares 'unit standing/spawning' packet
		lister.add(new PacketListenerByCmd(2, 0x856, 0x857) {

			int x, y;
			
			@Override
			public void run(PacketListener lister, PacketEvent event) {
				boolean match = false;
				int class_ = event.buf.getW(0x13);
				
				if (mob_id == 1) {
					match = MobList.isMvp(class_);
				} else if (mob_id == 2) {
					match = MobList.isBoss(class_);
				} else if (class_ == mob_id)
					match = true;
				
				if (match) {
					if (event.cmd == 0x856) {
						int[] pos = event.buf.getPos2(0x3b);
						x = pos[2];
						y = pos[3];
					} else {
						int[] pos = event.buf.getPos(55);
						x = pos[0];
						y = pos[1];
					}
					find = class_;
					bot.PushMessage("���Ǫ� " + find + "; " + x + "," + y);
					
					if (find == 1147/*�ƦZ*/) {
						fly = true;
					}
					
					bot.timer.schedule(new TimerTask() {

						@Override
						public void run() {
							try {
								bot.clif.viewpoint(0, 1, x, y, 1, 0x0000FF);
							} catch (IOException e) {
							}
							
							stop();
						}
						
					}, 3000);
				}
			}
			
		});
		
		// clif_parse_LoadEndAck
		/// Notification from the client, that it has finished map loading and is about to display player's character (CZ_NOTIFY_ACTORINIT).
		/// 007d
		lister.add(new PacketListenerByCmd(1, 0x7d) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				bot.timer.schedule(new TimerTask() {

					@Override
					public void run() {
						if (find == 0 || fly) {
							if (bot.skill.getSkillLv(26) > 0)
								bot.skill.addSkill(26/*��������*/, -1, handler.account_id, 0);
							else {
								bot.PushMessage("�S���������ʧޯ�?");
								// TODO: �S�������ʮɧ�λa�ǯͻH
							}

							fly = false;
						}
					}
					
				}, 500);
			}
			
		});
		
		// clif_skill_warppoint
		/// Presents a list of available warp destinations (ZC_WARPLIST).
		/// 011c <skill id>.W { <map name>.16B }*4
		lister.add(new PacketListenerByCmd(2, 0x011c) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int skill_id = event.buf.getW(2);
				String map1;
				try {
					map1 = event.buf.getString(4, 16);
				} catch (UnsupportedEncodingException e) {
					map1 = "Random";
				}
				try {
					bot.serf.UseSkillMap(skill_id, map1);
					
					isBigFly = false;
				} catch (IOException e) {
				}
			}
			
		});
		
		// clif_changemap
		/// Notifies the client of a position change to coordinates on given map (ZC_NPCACK_MAPMOVE).
		/// 0091 <map name>.16B <x>.W <y>.W
		lister.add(new PacketListenerByCmd(2, 0x0091) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				String map1;
				try {
					map1 = event.buf.getString(2, 16);
				} catch (UnsupportedEncodingException e) {
					map1 = null;
				}
				if (map == null) {
					map = map1;
				} else if (!map.equals(map1)) {
					bot.PushMessage("�a���ܧ�A����M��C");
					
					stop();
				} else if (isBigFly) {
					bot.PushMessage("�����j���A����M��C");
					
					stop();
				} else {
					isBigFly = true;
				}
			}
			
		});
		
		// clif_parse_UseSkillToId
		lister.add(new PacketListenerByCmd(1, 0x113) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int skill_id = event.buf.getW(packet_db.packet_db[handler.packet_ver.packet_ver][event.cmd].pos[1]);
				if (skill_id == 24/*���y*/ || skill_id == 75/*���B���|�q*/) {
					bot.PushMessage("����M��C");

					stop();
				}
			}
			
		});

		// clif_party_message
		/// Party chat message (ZC_NOTIFY_CHAT_PARTY).
		/// 0109 <packet len>.W <account id>.L <message>.?B
		lister.add(new PacketListenerByCmd(2, 0x109) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int textlen = event.buf.getW(2) - 8;
				String text;
				try {
					text = event.buf.getString(8, textlen);
				} catch (UnsupportedEncodingException e) {
					return;
				}
				String message;
				if (text.indexOf(" : ") != -1)
					message = text.substring(text.indexOf(" : ") + 3);
				else
					message = text;
				
				if (message.matches("\\d+")) {	// ���Ʀr
					bot.PushMessage("���ͳۤF�Ʀr�A����M��C");
					
					stop();
				}
			}
			
		});
		
		for(PacketListener l : lister) {
			manager.addListener(l);
		}
		
		bot.PushMessage("�}�l�M��Ǫ�: " + mob_id);
		manager.triggerEvent(1, null, 0x7d, 2);
	}
	
	void stop() {
		for(PacketListener l : lister) {
			manager.removeListener(l);
		}
	}
	
}
