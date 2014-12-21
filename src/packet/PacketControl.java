package packet;

import packet.Packet.Command;

public class PacketControl {
	private Packet p1;
	private int packet_var;
	private Command cmd;

	public PacketControl(int packet_var, Command cmd) {
		this.packet_var = packet_var;
		this.cmd = cmd;
		this.p1 = Packet.create(30720);
		this.p1.length(0);
	}

	public void addPacket(Packet p2) {
		p1.clearSkip();
		p1.append(p2);
		if (Packet.cut(p1, packet_var, cmd) == 1) {
			p1.clear();
		}
	}
}
