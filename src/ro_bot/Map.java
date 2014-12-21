package ro_bot;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import packet.Packet;
import packet.Serf;
import packet.event.PacketManager;
import common.BotKiller;
import common.data.BlockList;
import common.data.SessionData;

public class Map extends Thread {
	RoBot bot;
	SocketChannel remote;
	SessionData sd;
	Timer timer;
	PacketManager manager;
	Serf serf;
	MapReceive clif;
	
	public Map(RoBot bot) {
		this.bot = bot;
		this.remote = bot.remote;
		this.sd = bot.sd;
		this.timer = new Timer();
		this.manager = new PacketManager();
		this.serf = new Serf(remote, RoBot.packet_ver);
		this.clif = new MapReceive(this);
		start();
	}
	
	public void run() {
		try {
			serf.WantToConnection(sd.account_id, sd.char_id, sd.login_id1, sd.sex);
			
			sd.bl.id = sd.account_id;
			sd.status.account_id = sd.account_id;
			sd.status.char_id = sd.char_id;
			sd.status.sex = sd.sex;
			sd.bl.type = BlockList.BL_PC;
			
			new BotKiller(serf, manager);
			
			timer.schedule(new TimerTask() {
				
				@Override
				public synchronized void run() {
					try {
						serf.TickSend();
					} catch (IOException e) {
						bot.isRun = false;
						e.printStackTrace();
					}
				}
				
			}, 1000, 12000);

			
			sleep(1000);
			
			while(bot.isRun && bot.remoteType == 3) {
				String s = bot.command.poll();
				if (s != null) {
					if (s.equals("vending")) {
						new Vending(serf, manager, timer, sd.tick).run();
					}
				}
				yield();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bot.isRun = false;
			timer.cancel();
		}
	}

	public void MapServerCommand(Packet buf, int cmd, int packet_len) {
		if (RoBot.debug > 1) buf.showDump(packet_len);
		if (RoBot.debug > 0) System.out.printf("MapServerCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
		switch(cmd) {
		case 0x6a:
			int type = buf.getB(2);
			switch (type) {
			case 3:
				// Rejected by server
				System.out.println("Rejected by server");
				break;
			case 5:
				// Your Game's EXE file is not the latest version
				System.out.println("Your Game's EXE file is not the latest version");
				break;
			}
			bot.isRun = false;
			break;
		case 0x81:
			// Notifies the client of a ban or forced disconnect (SC_NOTIFY_BAN).
			int errorCode = buf.getB(2);
			String errorMsg;
			switch (errorCode) {
			case 1:
				errorMsg = "server closed";
				break;
			case 2:
				errorMsg = "ID already logged in";
				break;
			case 3:
				errorMsg = "timeout/too much lag";
				break;
			case 4:
				errorMsg = "server full";
				break;
			case 5:
				errorMsg = "underaged";
				break;
			case 6:
				errorMsg = "Server sill recognizes last connection";
				break;
			case 8:
				errorMsg = "Server sill recognizes last connection";
				break;
			case 9:
				errorMsg = "too many connections from this ip";
				break;
			case 10:
				errorMsg = "out of available time paid for";
				break;
			default:
				errorMsg = "";
				break;
			}
			System.out.println("server forced disconnect (errorCode=" + errorCode + ")[" + errorMsg + "]");
			bot.isRun = false;
			break;
		case 0x018b:	
			// Notification about the result of a disconnect request (ZC_ACK_REQ_DISCONNECT).
			int result = buf.getW(2);
			switch (result) {
			case 0:
				// disconnect (quit)
				System.out.println("server disconnect");
				bot.isRun = false;
				break;
			case 1:
				// cannot disconnect (wait 10 seconds)
				System.out.println("server cannot disconnect (wait 10 seconds)");
				break;
			}
			break;
		
		case 0x283:
			// clif_parse_wantToConnection
			// 0283 <sd->bl.id>.L
			break;
		case 0x73:
		case 0x2eb:
			clif.authok(buf, cmd, packet_len);
			try {
				serf.LoadEndAck();
				serf.TickSend();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 0x7f:
			clif.notify_time(buf, cmd, packet_len);
			break;
		
		case 0x91:
			clif.changemap(buf, cmd, packet_len);
			break;

		case 0xb0:
		case 0xb1:
		case 0xbe:
		case 0x121:
		case 0x13a:
		case 0x141:
			clif.updatestatus(buf, cmd, packet_len);
			break;
		case 0x43f:
			// clif_status_change
			/// Notifies clients of a status change.
			/// 0196 <index>.W <id>.L <state>.B (ZC_MSG_STATE_CHANGE) [used for ending status changes and starting them on non-pc units (when needed)]
			/// 043f <index>.W <id>.L <state>.B <remain msec>.L { <val>.L }*3 (ZC_MSG_STATE_CHANGE2) [used exclusively for starting statuses on pcs]
			/// 08ff <id>.L <index>.W <remain msec>.L { <val>.L }*3  (PACKETVER >= 20111108)
			/// 0983 <index>.W <id>.L <state>.B <total msec>.L <remain msec>.L { <val>.L }*3 (PACKETVER >= 20120618)
			/// 0984 <id>.L <index>.W <total msec>.L <remain msec>.L { <val>.L }*3 (PACKETVER >= 20120618)
			break;
		case 0x1d7:
			// clif_changelook
			/// Updates sprite/style properties of an object.
			/// 00c3 <id>.L <type>.B <value>.B (ZC_SPRITE_CHANGE)
			/// 01d7 <id>.L <type>.B <value>.L (ZC_SPRITE_CHANGE2)
			break;

		case 0x2e8:
		case 0x2d0:
			// clif_inventorylist
			/// Unified inventory function which sends all of the inventory (requires two packets, one for equipable items and one for stackable ones. [Skotlex]
			break;
		case 0x013c:
			// clif_arrowequip
			/// Marks an ammunition item in inventory as equipped (ZC_EQUIP_ARROW).
			/// 013c <index>.W
			break;
			
		/// Party
		case 0x7d8:
			// clif_party_option
			/// Updates party settings.
			/// 0101 <exp option>.L (ZC_GROUPINFO_CHANGE)
			/// 07d8 <exp option>.L <item pick rule>.B <item share rule>.B (ZC_REQ_GROUPINFO_CHANGE_V2)
			/// exp option:
			///     0 = exp sharing disabled
			///     1 = exp sharing enabled
			///     2 = cannot change exp sharing
			///
			/// flag:
			///     0 = send to party
			///     1 = send to sd
			break;
		case 0xfb:
			// clif_party_info
			/// Sends party information (ZC_GROUP_LIST).
			/// 00fb <packet len>.W <party name>.24B { <account id>.L <nick>.24B <map name>.16B <role>.B <state>.B }*
			/// role:
			///     0 = leader
			///     1 = normal
			/// state:
			///     0 = connected
			///     1 = disconnected
			break;
		
		/// Guild
		case 0x16c:
			// clif_guild_belonginfo
			/// Notifies the client that it is belonging to a guild (ZC_UPDATE_GDID).
			/// 016c <guild id>.L <emblem id>.L <mode>.L <ismaster>.B <inter sid>.L <guild name>.24B
			/// mode:
			///     &0x01 = allow invite
			///     &0x10 = allow expel
			break;
		case 0x16f:
			// clif_guild_notice
			/// Sends guild notice to client (ZC_GUILD_NOTICE).
			/// 016f <subject>.60B <notice>.120B
			break;
			
		/// Friends List
		case 0x201:
			// clif_friendslist_send
			/// Sends the whole friends list (ZC_FRIENDS_LIST).
			/// 0201 <packet len>.W { <account id>.L <char id>.L <name>.24B }*
			break;
		case 0x206:
			// clif_friendslist_toggle
			/// Toggles a single friend online/offline [Skotlex] (ZC_FRIENDS_STATE).
			/// 0206 <account id>.L <char id>.L <state>.B
			/// state:
			///     0 = online
			///     1 = offline
			break;

		/// Questlog System [Kevin] [Inkfish]
		case 0x2b1:
			// clif_quest_send_list
			/// Sends list of all quest states (ZC_ALL_QUEST_LIST).
			/// 02b1 <packet len>.W <num>.L { <quest id>.L <active>.B }*num
			break;
		case 0x2b2:
			// clif_quest_send_mission
			/// Sends list of all quest missions (ZC_ALL_QUEST_MISSION).
			/// 02b2 <packet len>.W <num>.L { <quest id>.L <start time>.L <expire time>.L <mobs>.W { <mob id>.L <mob count>.W <mob name>.24B }*3 }*num
			break;
		}
		manager.triggerEvent(2, buf, cmd, packet_len);
	}

}
