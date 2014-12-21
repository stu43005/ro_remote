package packet.event;

public abstract class PacketListenerByCmd implements PacketListener {
	public int type;
	public int[] cmd;
	
	public PacketListenerByCmd(int type, int... cmd) {
		this.type = type;
		this.cmd = cmd;
	}

	public abstract void run(PacketListener lister, PacketEvent event);
	
	@Override
	public void packetEvent(PacketEvent event) {
		for(int i = 0; i < cmd.length; i++) {
			if (type == event.type && cmd[i] == event.cmd) {
				run(this, event);
			}
		}
	}
	
}
