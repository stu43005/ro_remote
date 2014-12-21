package ro_bot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import packet.Packet;
import packet.PacketControl;
import packet.PacketVer;
import packet.packet_db;
import common.ServerList;
import common.data.SessionData;

public class RoBot {
	public static final ServerList loginSL = new ServerList(new int[] {138,91,40,24}, 6902, 1);
	//public static final ServerList loginSL = new ServerList(new int[] {127, 0, 0, 1}, 6900, 1);
	public static final String loginAccount = "vending1";
	public static final String loginPassword = "abc123";
	public static final int logincharSlot = 1;
	
	public static final int serverId = 0;
	public static PacketVer packet_ver = new PacketVer(5);
	
	/**
	 * 偵錯類型
	 * <ol start=0>
	 * <li>不偵錯</li>
	 * <li>顯示封包類型</li>
	 * <li>顯示封包內容</li>
	 * </ol>
	 */
	public static int debug = 0;

	public static void main(String[] args) throws Exception {
		packet_db.packetdb_readdb();
		
		RoBot bot = new RoBot(loginSL, loginAccount, loginPassword, logincharSlot);
		bot.run(new String[] {"vending"});
	}

	private String account;
	private String password;
	int charSlot;
	
	public SocketChannel remote;
	private Selector sel;
	private PacketControl pc = null;
	private ByteBuffer buf = ByteBuffer.allocate(30720);
	
	/**
	 * 遠端類型:
	 * <ol>
	 * <li>Login</li>
	 * <li>Char</li>
	 * <li>Map</li>
	 * </ol>
	 */
	public int remoteType = 1;
	
	public boolean isRun = false;
	
	public SessionData sd;
	Login login; // LoginBot
	Char ch; // CharBot
	Map mp; // MapBot
	
	public RoBot(ServerList loginSL, String account, String password, int charSlot) throws IOException {
		this.account = account;
		this.password = password;
		this.charSlot = charSlot;
		
		this.sel = Selector.open();
		this.sd = new SessionData();
		this.connect(loginSL);
		this.login = new Login(this);
		isRun = true;
	}
	
	void connect(ServerList sl) throws IOException {
		this.remote = SocketChannel.open(new InetSocketAddress(InetAddress.getByAddress(sl.ip), sl.port));
		System.out.println("connect to " + Packet.ip2str(sl.ip) + ":" + sl.port);
		this.remote.configureBlocking(false);
		SelectionKey remoteKey = this.remote.register(this.sel, SelectionKey.OP_READ);
		this.remoteType = sl.serverType;
		remoteKey.attach(this.remoteType);
	}
	
	LinkedList<String> command = new LinkedList<String>();
	
	public void run(String[] command) {
		for(String s : command) {
			this.command.offer(s);
		}
		
		try {
			Login.requestLogin(remote, account, password);
			
			while (isRun) {
				sel.select();
				Set<?> keys = sel.selectedKeys();
				Iterator<?> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					try {
						if (key.isReadable()) {
							int type = (int) key.attachment();
							SocketChannel channel = (SocketChannel) key.channel();
							if (channel.isOpen()) {
								switch(type) {
								case 1:	// From Login Server
								case 2:	// From Char Server
								case 3:	// From Map Server
									if (remoteType != type)
										break;
									buf.clear();
									remote.read(buf);
									if (buf.position() > 0) {
										buf.flip();
										ServerCommand(new Packet(buf));
									}
									break;
								}
							} else {
								//disconnect();
							}
						}
					} catch (CancelledKeyException e) {
						key.cancel();
					}
				}
			}
			remote.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			isRun = false;
			//if (mp != null) mp.notify();
		}
	}

	public void disconnect() {
		isRun = false;
		System.out.println("disconnect");
	}

	public void ServerCommand(Packet buf) {
		try {
			int cmd = buf.getW(0), packet_len = buf.getW(2);
			if (remoteType < 3) {
				if (RoBot.debug > 1) buf.showDump();
				if (RoBot.debug > 0) System.out.printf("ServerCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
			}
			switch (remoteType) {
			case 1:
				// Login
				if (login == null) {
					login = new Login(this);
				}
				login.LoginServerCommand(buf, cmd, packet_len);
				break;
			case 2:
				// Char
				if (ch == null) {
					ch = new Char(this);
				}
				ch.CharServerCommand(buf, cmd, packet_len);
				break;
			case 3:
				// Map
				//MapServerCommand(buf, cmd, packet_len);
				/*Packet.cut(buf, 0, new Packet.Command() {
					
					@Override
					public void execute(Packet buf, int cmd, int packet_len) {
						MapServerCommand(buf, cmd, packet_len);
					}
					
				});*/
				if (mp == null) {
					mp = new Map(this);
				}
				if (pc == null) {
					pc = new PacketControl(0, new Packet.Command() {

						@Override
						public void execute(Packet buf, int cmd, int packet_len) {
							mp.MapServerCommand(buf, cmd, packet_len);
						}
						
					});
				}
				pc.addPacket(buf);
				break;
			}
		} catch (BufferUnderflowException e) {
			System.err.println("packet error");
		}
	}
	
}
