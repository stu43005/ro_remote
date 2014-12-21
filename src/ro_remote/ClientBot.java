package ro_remote;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import packet.Clif;
import packet.Packet;
import packet.PacketControl;
import packet.Serf;
import packet.event.PacketManager;
import ro_remote.wisCommand.AutoWall;
import ro_remote.wisCommand.FindMob;
import ro_remote.wisCommand.Test;
import ro_remote.wisCommand.Viewpoint;

import common.BotKiller;
import common.Skill;

public class ClientBot extends Thread {
	public ClientHandler handler;
	public PacketManager manager;
	public Timer timer;
	public common.Time tick;
	public Skill skill;
	public Serf serf;
	public Clif clif;
	
	public ClientBot(ClientHandler handler) {
		this.handler = handler;
		this.manager = new PacketManager();
		this.timer = new Timer();
		this.tick = new common.Time();
		this.serf = new Serf(handler.getServer(), handler.packet_ver);
		this.clif = new Clif(handler.getClient());
		this.skill = new Skill(serf, manager, timer, tick, handler.account_id);
		new BotKiller(serf, manager);
		start();
	}

	private class BotQueue {
		/**
		 * 類型:
		 * <ol start=0>
		 * <li>WisCommand</li>
		 * <li>Client</li>
		 * <li>Server</li>
		 * </ol>
		 */
		int type;
		Packet buf;
		Object[] obj;
		
		public BotQueue(int type, Packet buf, Object... obj) {
			this.type = type;
			this.buf = buf;
			this.obj = obj;
		}
	}
	private Queue<BotQueue> bq = new LinkedList<BotQueue>();

	private synchronized BotQueue getQueue() {
		BotQueue q = null;
		while(handler.isRun && (q = bq.poll()) == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return q;
	}
	
	private PacketControl pcClient = null;
	private PacketControl pcServer = null;
	
	public void run() {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					clif.wisMessage("bot", "已登入RoRemote");
				} catch (IOException e) {
				}
			}
			
		}, 1000);
		
		try {
			while(handler.isRun) {
				BotQueue q = getQueue();
				if (q == null)
					continue;
				try {
					switch (q.type) {
					case 0:	// WisCommand
						ClientWisCommand(q.buf, (String) q.obj[0]);
						break;
					case 1:	// Client
						//ClientCommand(q.buf, (int) q.obj[0], (int) q.obj[1]);
						if (pcClient == null) {
							pcClient = new PacketControl(handler.packet_ver.packet_ver, new Packet.Command() {
								
								@Override
								public void execute(Packet buf, int cmd, int packet_len) {
									ClientCommand(buf, cmd, packet_len);
								}
								
							});
						}
						pcClient.addPacket(q.buf);
						break;
					case 2:	// Server
						//ServerCommand(q.buf, (int) q.obj[0], (int) q.obj[1]);
						if (pcServer == null) {
							pcServer = new PacketControl(0, new Packet.Command() {
								
								@Override
								public void execute(Packet buf, int cmd, int packet_len) {
									ServerCommand(buf, cmd, packet_len);
								}
								
							});
						}
						pcServer.addPacket(q.buf);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			handler.disconnect();
			timer.cancel();
		}
	}
	
	public synchronized void AddQueue(int type, Packet buf) {
		//bq.offer(new BotQueue(type, Packet.clone(buf, packet_len), cmd, packet_len));
		bq.offer(new BotQueue(type, Packet.clone(buf)));
		notify();
	}
	public synchronized void AddWisCommand(Packet buf, String cmd) {
		bq.offer(new BotQueue(0, Packet.clone(buf), cmd));
		notify();
	}
	public synchronized void wakeup() {
		notifyAll();
	}

	public void PushMessage(String msg) {
		System.out.println("Message: " + msg);
		try {
			clif.displaymessage(msg);
		} catch (Exception e) {
			System.err.println("Can not send message to client");
			System.err.println("error: " + e.toString());
		}
	}
	
	private AutoWall autoWall = null;
	
	public void ClientWisCommand(Packet buf, String cmd) {
		String[] w = cmd.split(":");
		
		if (w[0].equals("findmob") || w[0].equals("fm") || w[0].equals("find") || w[0].equals("mob")) {	// findmob:<mob_id>
			try {
				if (w.length > 1) {
					int w1;
					
					if (w[1].equals("mvp") || w[1].equals("m"))	// findmob:mvp
						w1 = 1;
					else if (w[1].equals("boss") || w[1].equals("b"))	// findmob:boss
						w1 = 2;
					else
						w1 = Integer.valueOf(w[1]);
					
					new FindMob(this).run(w1);
				} else
					PushMessage("指令說明: findmob:<mob_id>");
			} catch (NumberFormatException e) {
				PushMessage("mod_id 輸入錯誤");
			}
		} else if (w[0].equals("viewpoint")) {	// viewpoint:<x>:<y>
			try {
				if (w.length > 2) {
					int x, y;
					x = Integer.valueOf(w[1]);
					y = Integer.valueOf(w[2]);
					
					new Viewpoint(this).run(x, y);
				} else
					PushMessage("指令說明: viewpoint:<x>:<y>");
			} catch (NumberFormatException e) {
				PushMessage("位置輸入錯誤");
			}
		} else if (w[0].equals("autowall")) {
			if (autoWall == null) {
				autoWall = new AutoWall(this);
				autoWall.first();
			}
			if (w.length == 1)	// autowall
				autoWall.run();
			else if (w[1].equals("clear"))	// autowall:clear
				autoWall.clear();
		} else if (w[0].equals("debug")) {	// debug:<mode>
			try {
				if (w.length > 1) {
					int mode;
					mode = Integer.valueOf(w[1]);
					
					RoRemote.debug = mode;
					PushMessage("偵錯模式改為: " + RoRemote.debug);
				} else
					PushMessage("指令說明: debug:<mode>");
			} catch (NumberFormatException e) {
				PushMessage("輸入錯誤");
			}
		} else if (w[0].equals("test")) {
			new Test(this).run();
		} else {
			PushMessage("Unknown command [" + cmd + "]");
		}
		// TODO: Follow
		// TODO: 自動詩舞
	}
	
	public void ClientCommand(Packet buf, int cmd, int packet_len) {
		if (RoRemote.debug > 1) buf.showDump(packet_len);
		if (RoRemote.debug > 0) System.out.printf("MapClientCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
		switch (cmd) {
		}
		manager.triggerEvent(1, buf, cmd, packet_len);
	}
	
	public void ServerCommand(Packet buf, int cmd, int packet_len) {
		if (RoRemote.debug > 1) buf.showDump(packet_len);
		if (RoRemote.debug > 0) System.out.printf("MapServerCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
		switch (cmd) {
		case 0x7f:
			/// Server's tick (ZC_NOTIFY_TIME).
			/// 007f <time>.L
			tick.updateServerTick(buf.getL(2));
			break;
		}
		manager.triggerEvent(2, buf, cmd, packet_len);
	}
}
