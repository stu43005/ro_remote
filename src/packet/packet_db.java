package packet;

import java.io.*;

public class packet_db {
	public static final int MAX_PACKET_DB = 0xf00;
	public static final int MAX_PACKET_VER = 39;
	public static final int MAX_PACKET_POS = 20;
	
	public static class s_packet_db {
		public int len = 0;
		public String func = "";
		public int[] pos = new int[MAX_PACKET_POS];
	}
	public static s_packet_db[][] packet_db = new s_packet_db[MAX_PACKET_VER+1][MAX_PACKET_DB+1];
	
	public static class Clif_Config {
		public int packet_db_ver;
		public int[] connect_cmd = new int[MAX_PACKET_VER+1];
	}
	public static Clif_Config clif_config = new Clif_Config();
	
	public static boolean readed = false;
	
	public static synchronized void packetdb_readdb() throws Exception {
		int[] packet_len_table = new int[] {
				   10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0040
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0, 55, 17,  3, 37, 46, -1, 23, -1,  3,114,  3,  2,
				    3, 28, 19, 11,  3, -1,  9,  5, 55, 53, 58, 60, 44,  2,  6,  6,
				//#0x0080
				    7,  3,  2,  2,  2,  5, 16, 12, 10,  7, 29,  2, -1, -1, -1,  0, // 0x8b changed to 2 (was 23)
				    7, 22, 28,  2,  6, 30, -1, -1,  3, -1, -1,  5,  9, 17, 17,  6,
				   23,  6,  6, -1, -1, -1, -1,  8,  7,  6,  9,  4,  7,  0, -1,  6, // 0xaa changed to 9 (was 7)
				    8,  8,  3,  3, -1,  6,  6, -1,  7,  6,  2,  5,  6, 44,  5,  3,
				//#0x00C0
				    7,  2,  6,  8,  6,  7, -1, -1, -1, -1,  3,  3,  6,  3,  2, 27, // 0xcd change to 3 (was 6)
				    3,  4,  4,  2, -1, -1,  3, -1,  6, 14,  3, -1, 28, 29, -1, -1,
				   30, 30, 26,  2,  6, 26,  3,  3,  8, 19,  5,  2,  3,  2,  2,  2,
				    3,  2,  6,  8, 21,  8,  8,  2,  2, 26,  3, -1,  6, 27, 30, 10,
				//#0x0100
				    2,  6,  6, 30, 79, 31, 10, 10, -1, -1,  4,  6,  6,  2, 11, -1,
				   10, 39,  4, 10, 31, 35, 10, 18,  2, 13, 15, 20, 68,  2,  3, 16,
				    6, 14, -1, -1, 21,  8,  8,  8,  8,  8,  2,  2,  3,  4,  2, -1,
				    6, 86,  6, -1, -1,  7, -1,  6,  3, 16,  4,  4,  4,  6, 24, 26,
				//#0x0140
				   22, 14,  6, 10, 23, 19,  6, 39,  8,  9,  6, 27, -1,  2,  6,  6,
				  110,  6, -1, -1, -1, -1, -1,  6, -1, 54, 66, 54, 90, 42,  6, 42,
				   -1, -1, -1, -1, -1, 30, -1,  3, 14,  3, 30, 10, 43, 14,186,182,
				   14, 30, 10,  3, -1,  6,106, -1,  4,  5,  4, -1,  6,  7, -1, -1,
				//#0x0180
				    6,  3,106, 10, 10, 34,  0,  6,  8,  4,  4,  4, 29, -1, 10,  6,
				   90, 86, 24,  6, 30,102,  9,  4,  8,  4, 14, 10, -1,  6,  2,  6,
				    3,  3, 37,  5, 11, 26, -1,  4,  4,  6, 10, 12,  6, -1,  4,  4,
				   11,  7, -1, 67, 12, 18,114,  6,  3,  6, 26, 26, 26, 26,  2,  3,
				//#0x01C0,   Set 0x1d5=-1
				    2, 14, 10, -1, 22, 22,  4,  2, 13, 97,  3,  9,  9, 30,  6, 28,
				    8, 14, 10, 35,  6, -1,  4, 11, 54, 53, 60,  2, -1, 47, 33,  6,
				   30,  8, 34, 14,  2,  6, 26,  2, 28, 81,  6, 10, 26,  2, -1, -1,
				   -1, -1, 20, 10, 32,  9, 34, 14,  2,  6, 48, 56, -1,  4,  5, 10,
				//#0x0200
				   26, -1, 26, 10, 18, 26, 11, 34, 14, 36, 10,  0,  0, -1, 32, 10, // 0x20c change to 0 (was 19)
				   22,  0, 26, 26, 42,  6,  6,  2,  2,282,282, 10, 10, -1, -1, 66,
				   10, -1, -1,  8, 10,  2,282, 18, 18, 15, 58, 57, 65,  5, 71,  5,
				   12, 26,  9, 11, -1, -1, 10,  2,282, 11,  4, 36,  6, -1,  4,  2,
				//#0x0240
				   -1, -1, -1, -1, -1,  3,  4,  8, -1,  3, 70,  4,  8, 12,  4, 10,
				    3, 32, -1,  3,  3,  5,  5,  8,  2,  3, -1,  6,  4,  6,  4,  6,
				    6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0280
				    0,  0,  0,  6, 14,  0,  0, -1, 10, 12, 18,  0,  0,  0,  0,  0, // 0x288, 0x289 increase by 4 (kafra points)
				    0,  4,  0, 70, 10,  0,  0,  0,  8,  6, 27, 80,  0, -1,  0,  0,
				    0,  0,  8,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				   85, -1, -1,107,  6, -1,  7,  7, 22,191,  0,  8,  0,  0,  0,  0,
				//#0x02C0
				    0, -1,  0,  0,  0, 30, 30,  0,  0,  3,  0, 65,  4, 71, 10,  0,
				   -1, -1, -1,  0, 29,  0,  6, -1, 10, 10,  3,  0, -1, 32,  6, 36,
				   34, 33,  0,  0,  0,  0,  0,  0, -1, -1, -1, 13, 67, 59, 60,  8,
				   10,  2,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0300
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0340
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0380
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x03C0
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0400
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  8,  0, 25,
				//#0x0440
				   10,  4, -1,  0,  0,  0, 14,  0,  0,  0,  6,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0480
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x04C0
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0500
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25,
				//#0x0540
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0580
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x05C0
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0600
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25,
				//#0x0640
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0680
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x06C0
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0700
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 25,
				//#0x0740
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0780
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x07C0
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    6,  2, -1,  4,  4,  4,  4,  8,  8,268,  6,  8,  6, 54, 30, 54,
				    0, 15,  8,  6, -1,  8,  8, 32, -1,  5,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0, 14, -1, -1, -1,  8, 25,  0,  0, 26,  0,
				//#0x0800
				   -1, -1, 18,  4,  8,  6,  2,  4, 14, 50, 18,  6,  2,  3, 14, 20,
				    3, -1,  8, -1,  86, 2,  6,  6, -1, -1,  4, 10, 10,  0,  0,  0,
				    0,  0,  0,  0,  6,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0, -1, -1,  3,  2, 66,  5,  2, 12,  6,  0,  0,
				//#0x0840
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  19,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0, -1, -1, -1, -1,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0880
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x08C0
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 10,
					9,  7, 10,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0900
					0,  0,  0,  0,  0,  0,  0,  0,  5,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
				//#0x0940
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0, 14,  6, 50,  0,  0,  0,  0,  0,  0,
				//#0x0980
					0,  0,  0, 29,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					31,  0,  0,  0,  0,  0,  0,  -1,  8,  11,  9,  8,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
					0,  0,  0,  0,  0,  0,  0, 14,  0,  0,  0,  0,  0,  0,  0,  0,
				};
		
		int packet_ver, skip_ver = 0, warned = 0, max_cmd = -1, entries = 0;
		
		for( int i = 0; i < packet_len_table.length; ++i ) {
			packet_db[0][i] = new s_packet_db();
			packet_db[0][i].len = packet_len_table[i];
		}
		
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File("packet_db.txt"))));
			
			clif_config.packet_db_ver = MAX_PACKET_VER;
			packet_ver = MAX_PACKET_VER;
			for( int i = 0; i < MAX_PACKET_DB; i++ ) {
				packet_db[packet_ver][i] = new s_packet_db();
			}
			
			String line = null;
			while ((line = bf.readLine()) != null) {
				if (line.startsWith("//"))
					continue;
				String[] line2 = line.split("[\t ]*//");
				line = line2[0];
				String[] w = line.split(": ?");
				if (w.length == 2) {
					if (w[0].equals("packet_ver")) {
						int prev_ver = packet_ver;
						skip_ver = 0;
						packet_ver = Integer.valueOf(w[1]);
						if ( packet_ver > MAX_PACKET_VER ) {
							if( (warned&1) == 0 )
								System.err.printf("The packet_db table only has support up to version %d.\n", MAX_PACKET_VER);
							warned &= 1;
							skip_ver = 1;
						}
						else if( packet_ver < 0 )
						{
							if( (warned&2) == 0 )
								System.err.printf("Negative packet versions are not supported.\n");
							warned &= 2;
							skip_ver = 1;
						}
						else if( packet_ver == 0 )
						{
							if( (warned&4) == 0 )
								System.err.printf("Packet version %d is reserved for server use only.\n", 0);
							warned &= 4;
							skip_ver = 1;
						}

						if( skip_ver > 0 )
						{
							System.err.printf("Skipping packet version %d.\n", packet_ver);
							packet_ver = prev_ver;
							continue;
						}
						
						//packet_db[packet_ver] = packet_db[prev_ver];
						for( int i = 0; i < MAX_PACKET_DB; i++ ) {
							packet_db[packet_ver][i] = new s_packet_db();
							packet_db[packet_ver][i].len = packet_db[prev_ver][i].len;
							packet_db[packet_ver][i].func = new String(packet_db[prev_ver][i].func);
							for( int j = 0; j < MAX_PACKET_POS; j++ ) {
								packet_db[packet_ver][i].pos[j] = packet_db[prev_ver][i].pos[j];
							}
						}
						continue;
					} else if (w[0].equals("packet_db_ver")) {
						if (w[1].equals("default"))
							clif_config.packet_db_ver = MAX_PACKET_VER;
						else {
							int w2 = Integer.valueOf(w[1]);
							clif_config.packet_db_ver = Math.min(Math.max(w2, 0), MAX_PACKET_VER);
						}
						
						continue;
					}
				}
				
				if( skip_ver != 0 )
					continue; // Skipping current packet version
				
				String[] str = line.split(",");
				if (str.length < 2) {
					continue;
				}
				int cmd = Integer.parseInt(str[0].substring(2), 16);
				if (max_cmd < cmd)
					max_cmd = cmd;
				if (cmd <= 0 || cmd > MAX_PACKET_DB)
					continue;
				if (str.length < 2) {
					System.err.println("packet_db: packet len error");
					continue;
				}
				
				if (packet_db[packet_ver][cmd] == null) packet_db[packet_ver][cmd] = new s_packet_db();
				packet_db[packet_ver][cmd].len = Integer.valueOf(str[1]);

				if (str.length < 3) {
					continue;
				}
				
				packet_db[packet_ver][cmd].func = str[2];
				
				if (str[2].equals("wanttoconnection"))
					clif_config.connect_cmd[packet_ver] = cmd;

				if (str.length < 4) {
					throw new Exception("packet_db: packet error");
				}
				
				String[] str2 = str[3].split(":");
				for(int j = 0; j < str2.length; j++) {
					if( j >= MAX_PACKET_POS ) {
						System.err.printf("Too many positions found for packet 0x%04x (max=%d).\n", cmd, MAX_PACKET_POS);
						break;
					}
					packet_db[packet_ver][cmd].pos[j] = Integer.valueOf(str2[j]);
				}
				entries++;
			}
			bf.close();
			
			if(max_cmd > MAX_PACKET_DB) {
				System.err.printf("Found packets up to 0x%X, ignored 0x%X and above.\n", max_cmd, MAX_PACKET_DB);
				System.err.printf("Please increase MAX_PACKET_DB and recompile.\n");
			}
			
			if (clif_config.connect_cmd[clif_config.packet_db_ver] == 0) {
				int j;
				for(j = clif_config.packet_db_ver; j >= 0 && clif_config.connect_cmd[j] == 0; j--);

				clif_config.packet_db_ver = j > 0 ? j : MAX_PACKET_VER;
			}
			
			System.out.printf("Done reading '%d' entries in '%s'.\n", entries, "packet_db.txt");
			System.out.printf("Using default packet version: %d.\n", clif_config.packet_db_ver);
			readed = true;
		} catch (FileNotFoundException e) {
			throw new Exception("can't read packet_db.txt");
		} finally {
			if (bf != null) bf.close();
		}
	}
	
	public static int guessPacketVer(Packet buf) {
		int err = 1;
		int packet_ver = -1;
		int cmd, packet_len;
		long value;
		packet_ver = clif_config.packet_db_ver;
		cmd = buf.getW(0);
		packet_len = buf.length();
		
		if( cmd != clif_config.connect_cmd[packet_ver] || packet_len != packet_db[packet_ver][cmd].len )
			;
		else if( (value=buf.getL(packet_db[packet_ver][cmd].pos[0])) < 2000000 || value > 100000000 ) {
			if( err == 1 )
				err = 2;
		}
		else if( (value=buf.getL(packet_db[packet_ver][cmd].pos[1])) <= 0 ) {
			if( err == 1 )
				err = 3;
		}
		else if( (value=(long)buf.getB(packet_db[packet_ver][cmd].pos[4])) != 0 && value != 1 ) {
			if( err == 1 )
				err = 6;
		}
		else {
			err = 0;
			return packet_ver;
		}
		
		for (packet_ver = MAX_PACKET_VER; packet_ver > 0; packet_ver--) {
			if( cmd != clif_config.connect_cmd[packet_ver] || packet_len != packet_db[packet_ver][cmd].len )
				;
			else if( (value=buf.getL(packet_db[packet_ver][cmd].pos[0])) < 2000000 || value > 100000000 ) {
				if( err == 1 )
					err = 2;
			}
			else if( (value=buf.getL(packet_db[packet_ver][cmd].pos[1])) <= 0 ) {
				if( err == 1 )
					err = 3;
			}
			else if( (value=(long)buf.getB(packet_db[packet_ver][cmd].pos[4])) != 0 && value != 1 ) {
				if( err == 1 )
					err = 6;
			}
			else {
				err = 0;
				return packet_ver;
			}
		}
		packet_ver = -1;
		return -1;
	}
}
