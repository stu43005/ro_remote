package test;

import java.util.HashMap;

import packet.Packet;

public class Test {

	static void printByte(byte b) {
		System.out.printf("%1$02x, %1$3d, %2$3d\n", b, Packet.btoi(b));
	}
	
	public static void main(String[] args) throws Exception {
		packet.packet_db.packetdb_readdb();
		
		/*Packet buf = Packet.clone(new int[] {0x5F, 0x03, 0x1c, 0xc7, 0xE0});
		int[] pos = buf.getPos(2);
		System.out.printf("move to %d,%d\n", pos[0], pos[1]);
		buf.setPos(2, new int[] {110, 126});
		pos = buf.getPos(2);
		System.out.printf("move to %d,%d\n", pos[0], pos[1]);*/

		/*Packet buf = Packet.clone(new int[] {0x87, 0x00, 0x19, 0xF2, 0x6C, 0x9B, 0x1C, 0x87, 0xE1, 0xCC, 0x7E, 0x88});
		int[] pos = buf.getPos2(6);
		System.out.printf("move from %d,%d to %d,%d\n", pos[0], pos[1], pos[2], pos[3]);*/

		HashMap<Integer, String> a = new HashMap<Integer, String>();
		
		a.put(1, "1");
		a.put(1, "2");
		a.put(3, "3");
		
		System.out.println(a.get(1));
		System.out.println(a.get(4));
		
		System.out.println("Test End");
	}
	
}
