package packet;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * 封包物件
 * <p>
 * 提供類似eAthena的封包方法
 */
public class Packet {
	private ByteBuffer bf;
	private byte[] buf;
	private int length;
	private int skip;
	
	/**
	 * 用現存的{@link java.nio.ByteBuffer}建立封包物件
	 * @param buff - ByteBuffer物件
	 */
	public Packet(ByteBuffer buff) {
		this.bf = buff;
		this.buf = buff.array();
		this.length = buff.limit();
		this.skip = 0;
	}
	
	/**
	 * 建立一個新的封包
	 * 
	 * @param len - 新的封包長度
	 * @return 新的封包
	 */
	public static Packet create(int len) {
		ByteBuffer bf = ByteBuffer.allocate(len);
		Packet p = new Packet(bf);
		return p;
	}
	
	public static Packet clone(ByteBuffer original) {
		return clone(original, original.capacity());
	}
	public static Packet clone(ByteBuffer original, int len) {
		Packet p = Packet.create(len);
		p.setRange(0, len, original.array());
		p.length(len);
		return p;
	}
	public static Packet clone(Packet original) {
		return clone(original, original.length());
	}
	public static Packet clone(Packet original, int len) {
		byte[] b = original.getRange(0, len);
		return clone(b);
	}
	public static Packet clone(byte[] original) {
		ByteBuffer clone = ByteBuffer.wrap(original);
		Packet p = new Packet(clone);
		return p;
	}
	public static Packet clone(int[] original) {
		byte[] arr = new byte[original.length];
		for(int i = 0; i < original.length; i++) {
			arr[i] = (byte)original[i];
		}
		return clone(arr);
	}

	/**
	 * 定義封包切割完後執行的函數
	 * <p>
	 * 用於{@link Packet#cut(Packet, int, Command)}方法
	 */
	public abstract static class Command {
		public abstract void execute(Packet buf, int cmd, int packet_len);
	}
	
	/**
	 * 以packet_db數據切割封包
	 * 
	 * @param p - 被切割的封包
	 * @param packet_ver - 封包版本
	 * @param command - 切割後執行的方法
	 * @return
	 * 		0 - 正常<br>
	 * 		1 - 有錯誤<br>
	 * 		2 - 讀取packet_db錯誤<br>
	 * @see Command
	 */
	public static int cut(Packet p, int packet_ver, Command command) {
		if (!packet_db.readed) {
			try {
				packet_db.packetdb_readdb();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				return 2;
			}
		}
		while(true) {
			if (p.length() < 2)
				return 0;
			
			int cmd = p.getW(0);
			
			if (cmd > packet_db.MAX_PACKET_DB || packet_db.packet_db[packet_ver][cmd].len == 0) {
				System.err.printf("Received unsupported packet (packet 0x%04x, %d bytes received, packet_ver %d).\n", cmd, p.length(), packet_ver);
				return 1;
			}
			
			int packet_len = packet_db.packet_db[packet_ver][cmd].len;
			if (packet_len == -1) {
				if (p.length() < 4)
					return 0;
				
				packet_len = p.getW(2);
				if (packet_len < 4 || packet_len > 32768) {
					System.err.printf("Received packet 0x%04x specifies invalid packet_len (%d), packet_ver %d.\n", cmd, packet_len, packet_ver);
					return 1;
				}
			}
			if (p.length() < packet_len)
				return 0;
			
			command.execute(p, cmd, packet_len);
			p.skip(packet_len);
		}
	}
	
	public int length() {
		return length;
	}
	public void length(int len) {
		bf.limit(skip + len);
		length = len;
	}

	public int skip() {
		return skip;
	}
	public void skip(int i) {
		skip += i;
		length -= i;
	}
	
	public void clear() {
		skip = 0;
		length(0);
	}
	
	public void clearSkip() {
		if (skip > 0) {
			int oldSkip = skip;
			skip = 0;
			setRange(0, length, getRange(oldSkip, length));
			length(length);
		}
	}

	public void append(Packet p) {
		int len = p.length();
		setRange(length, len, p.getRange(0, len));
		length(length + len);
	}
	
	public ByteBuffer getBuffer() {
		return bf;
	}
	
	public void send(SocketChannel sc, int len) throws IOException {
		length(len);
		bf.position(skip);
		sc.write(bf);
	}
	
	public long get(int pos, int size) {
		long t = 0;
		for (int i = 0; i < size; i++)
			t |= btoi(buf[skip + pos + i]) << (i * 8);
		return t;
	}
	public int getB(int pos) {
		return btoi(buf[skip + pos]);
	}
	public int getW(int pos) {
		return (int)get(pos, 2);
	}
	public long getL(int pos) {
		return get(pos, 4);
	}
	public long getQ(int pos) {
		return get(pos, 8);
	}
	public int[] getPos(int pos) {
		int[] p = btoi(getRange(pos, 3));
		int[] i = new int[3];
		
		//x
		i[0] = ( ( p[0] & 0xff ) << 2 ) | ( p[1] >> 6 );
		//y
		i[1] = ( ( p[1] & 0x3f ) << 4 ) | ( p[2] >> 4 );
		//dir
		i[2] = ( p[2] & 0x0f );
		return i;
	}
	public int[] getPos2(int pos) {
		int[] p = btoi(getRange(pos, 6));
		int[] i = new int[6];
		
		//x0
		i[0] = ( ( p[0] & 0xff ) << 2 ) | ( p[1] >> 6 );
		//y0
		i[1] = ( ( p[1] & 0x3f ) << 4 ) | ( p[2] >> 4 );
		//x1
		i[2] = ( ( p[2] & 0x0f ) << 6 ) | ( p[3] >> 2 );
		//y1
		i[3] = ( ( p[3] & 0x03 ) << 8 ) | ( p[4] >> 0 );
		//sx0
		i[4] = ( p[5] & 0xf0 ) >> 4;
		//sy0
		i[5] = ( p[5] & 0x0f ) >> 0;
		return i;
	}
	public byte[] getRange(int pos, int len) {
		return Arrays.copyOfRange(buf, skip + pos, skip + pos + len);
	}
	public String getString(int pos, int len) throws UnsupportedEncodingException {
		byte[] b = getRange(pos, len);
		return new String(b, "BIG5").replace("\0", "");
	}
	
	public void set(int pos, int size, long value) {
		for (int i = 0; i < size; i++) {
			buf[skip + pos + i] = (byte)(value & 0xff);
			value >>= 8;
		}
	}
	public void setB(int pos, int value) {
		buf[skip + pos] = (byte)value;
	}
	public void setW(int pos, int value) {
		set(pos, 2, value);
	}
	public void setL(int pos, long value) {
		set(pos, 4, value);
	}
	public void setQ(int pos, long value) {
		set(pos, 8, value);
	}
	public void setPos(int pos, int x, int y, int dir) {
		byte[] p = new byte[3];
		p[0] = (byte) ( x >> 2 );
		p[1] = (byte) ( ( x << 6 ) | ( ( y >> 4 ) & 0x3f ) );
		p[2] = (byte) ( ( y << 4 ) | ( dir & 0xf ) );
		setRange(pos, 3, p);
	}
	public void setPos(int pos, int x, int y) {
		setPos(pos, x, y, 0);
	}
	public void setPos(int pos, int[] i) {
		if (i.length == 3)
			setPos(pos, i[0], i[1], i[2]);
		else
			setPos(pos, i[0], i[1], 0);
	}
	public void setPos2(int pos, int x0, int y0, int x1, int y1, int sx0, int sy0) {
		byte[] p = new byte[6];
		p[0] = (byte) ( x0 >> 2 );
		p[1] = (byte) ( ( x0 << 6 ) | ( ( y0 >> 4 ) & 0x3f ) );
		p[2] = (byte) ( ( y0 << 4 ) | ( ( x1 >> 6 ) & 0x0f ) );
		p[3] = (byte) ( ( x1 << 2 ) | ( ( y1 >> 8 ) & 0x03 ) );
		p[4] = (byte) y1;
		p[5] = (byte) ( ( sx0 << 4 ) | ( sy0 & 0x0f ) );
		setRange(pos, 6, p);
	}
	public void setPos2(int pos, int[] i) {
		setPos2(pos, i[0], i[1], i[2], i[3], i[4], i[5]);
	}
	public void setRange(int pos, int len, byte[] value) {
		for (int i = 0; i < len && i < value.length; i++) {
			buf[skip + pos + i] = value[i];
		}
	}
	public int setString(int pos, int maxLen, String value) throws UnsupportedEncodingException {
		byte[] b = value.getBytes("BIG5");
		int len = b.length;
		if (len > maxLen) len = maxLen - 1;
		setRange(pos, len, b);
		setB(pos + len, 0);
		return len + 1;
	}
	
	public void showDump() {
		showDump(length);
	}
	public void showDump(int length) {
		int i;
		char[] hex = new char[48];
		char[] ascii = new char[16];
		char[] hexlist = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		
		System.out.printf("--- 00-01-02-03-04-05-06-07-08-09-0A-0B-0C-0D-0E-0F   0123456789ABCDEF\n");
		for(int j = 0; j < 48; j++)
			hex[j] = ' ';
		for(int j = 0; j < 16; j++)
			ascii[j] = ' ';
		
		try {
			for(i = 0; i < length; i++) {
				char c = (char)getB(i);
				
				ascii[i%16] = (iscntrl(c) != 0) ? '.' : c;
				hex[(i%16)*3] = hexlist[(c & 0xF0) >> 4];
				hex[(i%16)*3+1] = hexlist[c & 0xF];
				hex[(i%16)*3+2] = ' ';
				
				if ((i%16) == 15) {
					System.out.printf("%03X %s  %s\n", i/16, String.valueOf(hex), String.valueOf(ascii));
					for(int j = 0; j < 48; j++)
						hex[j] = ' ';
					for(int j = 0; j < 16; j++)
						ascii[j] = ' ';
				}
			}
			
			if( (i%16) != 0 ) {
				System.out.printf("%03X %-48s  %-16s\n", i/16, String.valueOf(hex), String.valueOf(ascii));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int iscntrl(int c) {
		if (c >= 0x00 && c <= 0x1f) return 1;
		if (c == 0x7f) return 1;
		return 0;
	}
	
	public static String ip2str(byte[] ip) {
		String str = btoi(ip[0]) + "." + btoi(ip[1]) + "." + btoi(ip[2]) + "." + btoi(ip[3]);
		return str;
	}

	public static int btoi(byte b) {
		int i = 0;
		i |= b & 0xff;
		return i;
	}
	public static int[] btoi(byte[] b) {
		int[] arr = new int[b.length];
		for(int i = 0; i < b.length; i++)
			arr[i] = btoi(b[i]);
		return arr;
	}
}
