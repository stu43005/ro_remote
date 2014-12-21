package common;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;

import packet.Serf;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;

public class BotKiller {
	private Serf serf;
	private PacketManager manager;
	
	public BotKiller(Serf serf, PacketManager manager) {
		this.serf = serf;
		this.manager = manager;
		init();
	}

	private LinkedList<String> l = new LinkedList<String>();
	
	private void init() {
		// clif_wis_message
		/// Whisper is transmitted to the destination player (ZC_WHISPER).
		/// 0097 <packet len>.W <nick>.24B <message>.?B
		/// 0097 <packet len>.W <nick>.24B <isAdmin>.L <message>.?B (PACKETVER >= 20091104)
		manager.addListener(new PacketListenerByCmd(2, 0x97) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				try {
					String nick = event.buf.getString(4, 24);
					if (nick.equals("外掛檢測") || nick.equals("飄羽")) {
						int len = event.buf.getW(2);
						String message = event.buf.getString(32, len - 32);
						l.add(message);
						if (l.size() >= 5) {
							String num = botkiller(l);
							l.clear();
							
							serf.WisMessage(nick, num);
						}
					}
				} catch (UnsupportedEncodingException e) {
				} catch (Exception e) {
				}
			}
			
		});
	}

	private static final boolean[][] numArr = new boolean[][] {
			{true, true, true, true, false, true, true, false, true, true, false, true, true, true, true},	// 0
			{false, true, false, false, true, false, false, true, false, false, true, false, false, true, false},
			{true, true, true, false, false, true, true, true, true, true, false, false, true, true, true},	// 2
			{true, true, true, false, false, true, true, true, true, false, false, true, true, true, true},
			{true, false, true, true, false, true, true, true, true, false, false, true, false, false, true},	// 4
			{true, true, true, true, false, false, true, true, true, false, false, true, true, true, true},
			{true, true, true, true, false, false, true, true, true, true, false, true, true, true, true},	// 6
			{true, true, true, true, false, true, true, false, true, false, false, true, false, false, true},
			{true, true, true, true, false, true, true, true, true, true, false, true, true, true, true},	// 8
			{true, true, true, true, false, true, true, true, true, false, false, true, false, false, true}
	};
	
	public static String botkiller(LinkedList<String> l) throws Exception {
		String num = "";
		byte[][] arr = new byte[5][];
		int i = 0;
		boolean[][] brr = new boolean[4][15];
		
		for (String s : l) {
			if (i >= 5)
				break;
			try {
				byte[] b = s.getBytes("BIG5");
				if ((b[0] & 0xff) > 0xA2)
					continue;
				//System.out.println(s);
				arr[i++] = b;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		if (i < 5) {
			throw new Exception("所需字串不足");
		}
		
		for (i = 0; i < 5; i++) {
			for (int j = 0; j < arr[i].length; j += 2) {
				if (j % 8 == 6)
					continue;
				
				int x = arr[i][j] & 0xff;
				int y = arr[i][j + 1] & 0xff;
				
				int a = j / 8;
				int b = i * 3 + j % 8 / 2;
				boolean c = false;
				
				if (x == 0x20 || y == 0x20)
					c = false;
				else if (x == 0xA1 && y == 0x40)
					c = false;
				else if (x == 0xA1 && y == 0xBD)
					c = true;
				else if (x == 0xA2 && y == 0x68)
					c = true;
				else if (x == 0xA2 && y == 0x70)
					c = true;
				
				brr[a][b] = c;
			}
		}
		
		for (boolean[] a : brr) {
			int j;
			int len = numArr.length;
			for (j = 0; j < len; j++) {
				if (Arrays.equals(a, numArr[j])) {
					num = num + j;
					break;
				}
			}
			if (j == len) {
				/*for (boolean b : a)
					System.out.print(b + ", ");
				System.out.println();*/
				System.err.println("error number:");
				for (i = 0; i < a.length; i++) {
					System.err.print(a[i] ? "▉" : "　");
					if (i % 3 == 2)
						System.err.print("\n");
				}
				throw new Exception("無法解析數字");
			}
		}
		
		return num;
	}
}
