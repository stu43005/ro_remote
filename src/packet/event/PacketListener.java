package packet.event;

import java.util.*;

public interface PacketListener extends EventListener {
	public void packetEvent(PacketEvent event);
}
