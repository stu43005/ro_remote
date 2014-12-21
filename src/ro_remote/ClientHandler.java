package ro_remote;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import packet.*;
import common.*;

public class ClientHandler extends Thread {
	private ServerSocketChannel serverChannel;
	private SocketChannel client;
	private SocketChannel remote;
	private Selector sel;
	private ByteBuffer buff = ByteBuffer.allocate(30720);
	private ByteBuffer buf = ByteBuffer.allocate(30720);
	private ClientBot bot = null;
	
	/**
	 * 遠端類型:
	 * <ol>
	 * <li>Login</li>
	 * <li>Char</li>
	 * <li>Map</li>
	 * </ol>
	 */
	public int remoteType = 1;
	
	/**
	 * 封包版本:
	 * <p>
	 *   -1 = 自動偵測
	 */
	public PacketVer packet_ver = new PacketVer();
	
	public boolean isRun = false;
	
	public long account_id;
	
	public ClientHandler(SocketChannel client) throws IOException {
		this.sel = Selector.open();
		this.connect(client, RoRemote.loginSL);
		isRun = true;
		start();
	}
	
	private void connect(SocketChannel client, ServerList sl) throws IOException {
		this.client = client;
		this.client.configureBlocking(false);
		SelectionKey clientKey = this.client.register(this.sel, SelectionKey.OP_READ);
		clientKey.attach(0);
		
		this.remote = SocketChannel.open(new InetSocketAddress(InetAddress.getByAddress(sl.ip), sl.port));
		System.out.println("connect to " + Packet.ip2str(sl.ip) + ":" + sl.port);
		this.remote.configureBlocking(false);
		SelectionKey remoteKey = this.remote.register(this.sel, SelectionKey.OP_READ);
		this.remoteType = sl.serverType;
		remoteKey.attach(this.remoteType);
	}

	private PacketControl pcServer = null;
	
	public void run() {
		try {
			while (isRun) {
				sel.select();
				Set<?> keys = sel.selectedKeys();
				Iterator<?> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					try {
						if (key.isAcceptable()) {
							ServerList sl = (ServerList) key.attachment();
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel channel = server.accept();
							this.connect(channel, sl);
							if (sl.serverType == 3) {
								bot = new ClientBot(this);
							}
						} else if (key.isReadable()) {
							int type = (int) key.attachment();
							SocketChannel channel = (SocketChannel) key.channel();
							if (channel.isOpen()) {
								switch(type) {
								case 0:	// From Client
									buf.clear();
									client.read(buf);
									if (buf.position() > 0) {
										buf.flip();
										ClientCommand(new Packet(buf));
										if (buf.limit() > 0) {
											buf.rewind();
											remote.write(buf);
										}
									}
									break;
								case 1:	// From Login Server
								case 2:	// From Char Server
									if (remoteType != type)
										break;
									buff.clear();
									remote.read(buff);
									if (buff.position() > 0) {
										buff.flip();
										ServerCommand(new Packet(buff));
										if (buff.limit() > 0) {
											buff.rewind();
											client.write(buff);
										}
									}
									break;
								case 3:	// From Map Server
									if (remoteType != type)
										break;
									buff.clear();
									remote.read(buff);
									if (buff.position() > 0) {
										buff.flip();
										if (pcServer == null) {
											pcServer = new PacketControl(0, new Packet.Command() {
												
												@Override
												public void execute(Packet buf, int cmd, int packet_len) {
													Packet p = Packet.clone(buf, packet_len);
													MapServerCommand(p, cmd, packet_len);
													try {
														if (p.length() > 0) {
															p.send(client, packet_len);
														}
													} catch (IOException e) {
														e.printStackTrace();
													}
												}
												
											});
										}
										Packet p = new Packet(buff);
										bot.AddQueue(2, p);
										pcServer.addPacket(p);
									}
									break;
								}
							} else {
								//disconnect();
							}
						}
					} catch (CancelledKeyException e) {
						key.cancel();
					} catch (AsynchronousCloseException e) {
						e.printStackTrace();
					}
				}
			}
			remote.close();
			client.close();
			if (serverChannel != null) serverChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			disconnect();
			RoRemote.clientDisconnect();
		}
	}
	
	public void disconnect() {
		isRun = false;
		sel.wakeup();
		if (bot != null) bot.wakeup();
		//System.out.println("disconnect");
	}
	
	public SocketChannel getClient() {
		return client;
	}
	public SocketChannel getServer() {
		return remote;
	}
	
	public void ClientCommand(Packet buf) {
		try {
			int cmd = buf.getW(0), packet_len = buf.getW(2);
			if (remoteType < 3) {
				if (RoRemote.debug > 1) buf.showDump();
				if (RoRemote.debug > 0) System.out.printf("ClientCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
			}
			switch (remoteType) {
			case 1:
				// Login
				LoginClientCommand(buf, cmd, packet_len);
				break;
			case 2:
				// Char
				CharClientCommand(buf, cmd, packet_len);
				break;
			case 3:
				// Map
				if (packet_ver.packet_ver == -1) {
					packet_ver.packet_ver = packet_db.guessPacketVer(buf);
					if (packet_ver.packet_ver != -1)
						System.out.println("packet_ver=" + packet_ver.packet_ver);
				}
				MapClientCommand(buf, cmd, packet_len);
				/*Packet.cut(buf, packet_ver, new Packet.Command() {
					
					@Override
					public void execute(Packet buf, int cmd, int packet_len) {
						MapClientCommand(buf, cmd, packet_len);
					}
					
				});*/
				if (buf.length() > 0) {
					bot.AddQueue(1, buf);
				}
				break;
			}
		} catch (BufferUnderflowException e) {
			System.err.println("packet error");
		}
	}
	
	private void LoginClientCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
		case 0x64:
			// request client login (raw password)
			// S 0064 <version>.L <username>.24B <password>.24B <clienttype>.B
			System.out.println("request login");
			break;
		}
	}

	private void CharClientCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
		case 0x65:
			// request to connect
			// 0065 <account id>.L <login id1>.L <login id2>.L <???>.W <sex>.B
			System.out.println("request to connect");
			break;
		case 0x66:
			// char select
			int slot = buf.getB(2);
			System.out.println("char select (slot=" + slot + ")");
			break;
		case 0x187:
			// client keep-alive packet (every 12 seconds)
			// R 0187 <account ID>.l
			break;
		}
	}
	
	private void MapClientCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
		case 0x0072:
			// Request to connect to map-server.
			// 0072 <account id>.L <char id>.L <auth code>.L <client time>.L <gender>.B (CZ_ENTER)
			System.out.println("Request to connect to map-server.");
			break;
		case 0x018a:
			// Request to disconnect from server (CZ_REQ_DISCONNECT).
			System.out.println("client logout");
			break;
		case 0x0096:
			// clif_parse_WisMessage
			// Validates and processes whispered messages (CZ_WHISPER).
			// 0096 <packet len>.W <nick>.24B <message>.?B
			try {
				String nick = buf.getString(4, 24);
				String message = buf.getString(28, packet_len-28);
				if (nick.equals("bot")) {
					buf.clear();
					bot.AddWisCommand(buf, message);
				}
			} catch (UnsupportedEncodingException e) {
			}
			break;
		}
	}
	
	public void ServerCommand(Packet buf) {
		try {
			int cmd = buf.getW(0), packet_len = buf.getW(2);
			if (remoteType < 3) {
				if (RoRemote.debug > 1) buf.showDump();
				if (RoRemote.debug > 0) System.out.printf("ServerCommand: 0x%04x, packet_len: %d, buffer_len: %d\n", cmd, packet_len, buf.length());
			}
			switch (remoteType) {
			case 1:
				// Login
				LoginServerCommand(buf, cmd, packet_len);
				break;
			case 2:
				// Char
				CharServerCommand(buf, cmd, packet_len);
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
				System.err.println("Error map packet run this.");
				break;
			}
		} catch (BufferUnderflowException e) {
			System.err.println("packet error");
		}
	}
	
	private void LoginServerCommand(Packet buf, int cmd, int packet_len) {
		try {
			switch (cmd) {
			case 0x69:
				// login ok
				System.out.println("login auth ok");
				account_id = buf.getL(8);
				int server_num = (packet_len - 47) / 32,
					sex = buf.getB(46);
				System.out.println("login information: account_id=" + account_id + ", sex=" + Sex.num2str(sex));
				System.out.println("server list: (num=" + server_num + ")");
				
				server_num = 1;	// only one server
				for (int i = 0; i < server_num; i++) {
					byte[] charIp = buf.getRange(47+i*32, 4);
					int charPort = buf.getW(47+i*32+4),
						bindPort = RoRemote.charPort++;
					System.out.println("  " + (i+1) + ". " + Packet.ip2str(charIp) + ":" + charPort + ", bind: " + bindPort);
					
					try {
						if (serverChannel != null) serverChannel.close();
						serverChannel = ServerSocketChannel.open();
						serverChannel.socket().bind(new InetSocketAddress(bindPort));
						serverChannel.configureBlocking(false);
						SelectionKey serverKey = serverChannel.register(sel, SelectionKey.OP_ACCEPT);
						serverKey.attach(new ServerList(charIp, charPort, 2));
						
						buf.setRange(47+i*32, 4, new byte[]{127, 0, 0, 1});
						buf.setW(47+i*32+4, bindPort);
					} catch (IOException e1) {
						e1.printStackTrace();
						if (serverChannel != null) serverChannel.close();
					}
					if (RoRemote.debug > 1) buf.showDump();
				}
				break;
			case 0x6a:
				// Log the result of a failed connection attempt by sd
				System.out.println("login auth failed");
				disconnect();
				break;
			case 0x81:
				int type = buf.getB(2);
				switch (type) {
				case 1:
					// Server closed
					System.out.println("Server closed");
					break;
				case 8:
					// Server still recognizes your last login
					System.out.println("Server still recognizes your last login");
					break;
				}
				disconnect();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void CharServerCommand(Packet buf, int cmd, int packet_len) {
		try {
			switch (cmd) {
			case 0x71:
				// Send player to map
				System.out.println("Send player to map");
				long char_id = buf.getL(2);
				byte[] mapIp = buf.getRange(22, 4);
				int mapPort = buf.getW(26),
					bindPort = RoRemote.mapPort++;
				System.out.println("char information: char_id=" + char_id);
				System.out.println("map server: " + Packet.ip2str(mapIp) + ":" + mapPort + ", bind: " + bindPort);
				
				try {
					if (serverChannel != null) serverChannel.close();
					serverChannel = ServerSocketChannel.open();
					serverChannel.socket().bind(new InetSocketAddress(bindPort));
					serverChannel.configureBlocking(false);
					SelectionKey serverKey = serverChannel.register(sel, SelectionKey.OP_ACCEPT);
					serverKey.attach(new ServerList(mapIp, mapPort, 3));
					
					buf.setRange(22, 4, new byte[]{127, 0, 0, 1});
					buf.setW(26, bindPort);
				} catch (IOException e1) {
					e1.printStackTrace();
					if (serverChannel != null) serverChannel.close();
				}
				if (RoRemote.debug > 1) buf.showDump();
				break;
			case 0x82d:
				// Notify client about charselect window data
				break;
			case 0x6c:
				// rejected from server
				System.out.println("rejected from server");
				disconnect();
				break;
			case 0x81:
				int type = buf.getB(2);
				switch (type) {
				case 1:
					// Server closed
					System.out.println("Server closed");
					break;
				case 2:
					// Someone has already logged in with this id
					System.out.println("Someone has already logged in with this id");
					break;
				case 8:
					// Character already online. KICK KICK KICK
					System.out.println("Character already online");
					break;
				}
				disconnect();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void MapServerCommand(Packet buf, int cmd, int packet_len) {
		switch (cmd) {
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
			disconnect();
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
			disconnect();
			break;
		case 0x018b:
			// Notification about the result of a disconnect request (ZC_ACK_REQ_DISCONNECT).
			int result = buf.getW(2);
			switch (result) {
			case 0:
				// disconnect (quit)
				System.out.println("server disconnect");
				disconnect();
				break;
			case 1:
				// cannot disconnect (wait 10 seconds)
				System.out.println("server cannot disconnect (wait 10 seconds)");
				break;
			}
			break;
		}
	}
}
