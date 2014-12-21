package ro_remote;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import packet.packet_db;
import common.ServerList;

public class RoRemote {
	public static ServerList loginSL = new ServerList(new int[] {138,91,40,24}, 6902, 1);
	public static int loginPort = 6900;
	public static int charPort = 6121;
	public static int mapPort = 5121;
	
	/**
	 * 偵錯類型
	 * <ol start=0>
	 * <li>不偵錯</li>
	 * <li>顯示封包類型</li>
	 * <li>顯示封包內容</li>
	 * </ol>
	 */
	public static int debug = 0;
	
	public static boolean isRun = true;
	public static int clientCount = 0;
	
	private static ServerSocketChannel serverChannel;
	private static Selector sel;
	
	public static void main(String[] args) throws Exception {
		packet_db.packetdb_readdb();
		
		serverChannel = ServerSocketChannel.open();
		try {
			serverChannel.socket().bind(new InetSocketAddress(loginPort));
			serverChannel.configureBlocking(false);
			sel = Selector.open();
			serverChannel.register(sel, SelectionKey.OP_ACCEPT);
			
			while (isRun) {
				sel.select();
				Set<?> keys = sel.selectedKeys();
				Iterator<?> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();
					it.remove();
					if (key.isAcceptable()) {
						clientConnect();
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						new ClientHandler(client);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverChannel != null) serverChannel.close();
			//Conexion.getInstancia().getConexion().close();
		}
	}

	public static synchronized void clientConnect() {
		clientCount++;
		System.out.println("client connected (clientCount=" + clientCount + ")");
	}
	
	public static synchronized void clientDisconnect() {
		clientCount--;
		System.out.println("client disconnect (clientCount=" + clientCount + ")");
		if (clientCount <= 0) isRun = false;
		sel.wakeup();
	}
	
}
