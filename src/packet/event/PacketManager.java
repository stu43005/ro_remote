package packet.event;

import java.util.*;

import packet.Packet;

public class PacketManager {
	private Collection<PacketListener> listeners;
	private Collection<PacketListener> removes;
	
    /**
     * 添加事件
     * 
     * @param listener PacketListener
     */
    public void addListener(PacketListener listener) {
        if (listeners == null) {
            listeners = new HashSet<PacketListener>();
        }
        listeners.add(listener);
    }
    
    /**
     * 移除事件 (加入移除隊列)
     * 
     * @param listener PacketListener
     */
    public void removeListener(PacketListener listener) {
        if (listeners == null)
            return;
        if (removes == null) {
        	removes = new HashSet<PacketListener>();
        }
        removes.add(listener);
    }
    
    /**
     * 移除事件
     */
    private void removeListener() {
        if (removes == null)
            return;
        Iterator<PacketListener> iter = removes.iterator();
        while (iter.hasNext()) {
        	PacketListener listener = (PacketListener) iter.next();
        	listeners.remove(listener);
        }
        removes.clear();
    }
    
    /**
     * 通知所有的PacketListener
     */
    private void notifyListeners(PacketEvent event) {
    	removeListener();
        Iterator<PacketListener> iter = listeners.iterator();
        while (iter.hasNext()) {
        	PacketListener listener = (PacketListener) iter.next();
            listener.packetEvent(event);
        }
    }
    
    /**
     * 觸發事件
     */
    public void triggerEvent(int type, Packet buf, int cmd, int packet_len) {
        if (listeners == null)
            return;
        PacketEvent event = new PacketEvent(this, type, buf, cmd, packet_len);
        notifyListeners(event);
    }
}
