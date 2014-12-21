package ro_bot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import packet.Packet;
import packet.Serf;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;
import common.Mysql;
import common.Time;

public class Vending {
	Serf serf;
	PacketManager manager;
	Timer timer;
	Time tick;
	
	public Vending(Serf serf, PacketManager manager, Timer timer, Time tick) {
		this.serf = serf;
		this.manager = manager;
		this.timer = timer;
		this.tick = tick;
	}
	
	PacketListener lister1;
	PacketListener lister2;
	PacketListener lister3;
	PacketListener lister4;
	
	final int x = 156;
	final int start_y = 60;
	final int end_y = 196;
	
	LinkedList<Long> gold = new LinkedList<Long>();
	double goldValue = 30000;
	
	public void run() {
		Mysql.getInstancia().hacerConsulta("TRUNCATE TABLE `vending_board`");
		Mysql.getInstancia().hacerConsulta("TRUNCATE TABLE `char_position`");

		// 金幣取平均值
		try {
			ResultSet result = Mysql.getInstancia().hacerConsulta("SELECT AVG(`value`) FROM `vendinglog` WHERE `nameid` = 671 AND `time` > (SELECT MAX(`time`) - INTERVAL 7 DAY FROM `vendinglog`)");
			if (result.next()) {
				goldValue = result.getDouble(1);
			}
		} catch (SQLException e1) {
			System.err.println("無法取得金幣價格");
			e1.printStackTrace();
		}
		
		// clif_showvendingboard
		/// Displays a vending board to target/area (ZC_STORE_ENTRY).
		/// 0131 <owner id>.L <message>.80B
		manager.addListener(lister1 = new PacketListenerByCmd(2, 0x0131) {
			
			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long owner_id = event.buf.getL(2);
				String message;
				try {
					message = event.buf.getString(6, 80);
				} catch (UnsupportedEncodingException e) {
					message = "";
				}
				System.out.println("vending board: " + owner_id + "[" + message + "]");
				
				if (message.startsWith("[金幣] ")) {
					gold.add(new Long(owner_id));
				}
				
				try {
					serf.VendingListReq(owner_id);
				} catch (IOException e) {
					System.err.println("Can not send packet to server [getVendingList]");
					System.err.println("error: " + e.toString());
				}
				
				try {
					PreparedStatement psAgregar;
					psAgregar = Mysql.getInstancia().getConnection().prepareStatement("DELETE FROM `vending_board` WHERE `owner_id`=?");
					psAgregar.setLong(1, owner_id);
					psAgregar.executeUpdate();
					
					psAgregar = Mysql.getInstancia().getConnection().prepareStatement("INSERT INTO `vending_board` (`owner_id`,`message`) VALUES (?,?)");
					psAgregar.setLong(1, owner_id);
					//psAgregar.setString(2, message);
					psAgregar.setBytes(2, message.getBytes("BIG5"));
					psAgregar.executeUpdate();
				} catch (SQLException | UnsupportedEncodingException e) {
					System.err.println("error insert row: " + e.toString());
					System.err.printf("data: %d, %s\n", owner_id, message);
				}
			}
			
		});

		// clif_set_unit_idle
		// Prepares 'unit standing/spawning' packet
		manager.addListener(lister2 = new PacketListenerByCmd(2, 0x857) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long id = event.buf.getL(5);
				int[] pos = event.buf.getPos(55);
				
				try {
					PreparedStatement psAgregar;
					psAgregar = Mysql.getInstancia().getConnection().prepareStatement("DELETE FROM `char_position` WHERE `id`=?");
					psAgregar.setLong(1, id);
					psAgregar.executeUpdate();
					
					psAgregar = Mysql.getInstancia().getConnection().prepareStatement("INSERT INTO `char_position` (`id`,`x`,`y`) VALUES (?,?,?)");
					psAgregar.setLong(1, id);
					psAgregar.setInt(2, pos[0]);
					psAgregar.setInt(3, pos[1]);
					psAgregar.executeUpdate();
				} catch (SQLException e) {
					System.err.println("error insert row: " + e.toString());
					System.err.printf("data: %d, %d,%d, %s\n", id, pos[0], pos[1]);
				}
			}
			
		});

		// clif_vendinglist
		/// Sends a list of items in a shop.
		/// R 0133 <packet len>.W <owner id>.L { <price>.L <amount>.W <index>.W <type>.B <name id>.W <identified>.B <damaged>.B <refine>.B <card1>.W <card2>.W <card3>.W <card4>.W }* (ZC_PC_PURCHASE_ITEMLIST_FROMMC)
		/// R 0800 <packet len>.W <owner id>.L <unique id>.L { <price>.L <amount>.W <index>.W <type>.B <name id>.W <identified>.B <damaged>.B <refine>.B <card1>.W <card2>.W <card3>.W <card4>.W }* (ZC_PC_PURCHASE_ITEMLIST_FROMMC2)
		manager.addListener(lister3 = new PacketListenerByCmd(2, 0x133, 0x0800) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				Packet buf = event.buf;
				int offset, count;
				long owner_id, unique_id = 0;
				boolean isCashShop = false;
				
				if (event.cmd == 0x133)	// PACKETVER < 20100105
					offset = 8;
				else
					offset = 12;
				count = (event.packet_len - offset) / 22;
				owner_id = buf.getL(4);
				if (event.cmd == 0x800)
					unique_id = buf.getL(8);
				
				for(Long id : gold) {
					if (owner_id == id) {
						isCashShop = true;
						break;
					}
				}
				
				System.out.println("vending list: " + owner_id + (event.cmd == 0x800 ? ", " + unique_id : "") + (isCashShop ? " (金幣商店)" : ""));
				
				for( int i = 0; i < count; i++ ) {
					long value;
					int amount, nameid, identify, attribute, refine;
					int[] cards = new int[4];
					
					value		= buf.getL(offset+ 0+i*22);
					amount		= buf.getW(offset+ 4+i*22);
					//index		= buf.getW(offset+ 6+i*22);
					//type		= buf.getB(offset+ 8+i*22);
					nameid		= buf.getW(offset+ 9+i*22);
					identify	= buf.getB(offset+11+i*22);
					attribute	= buf.getB(offset+12+i*22);
					refine		= buf.getB(offset+13+i*22);
					cards[0]	= buf.getW(offset+14+i*22);
					cards[1]	= buf.getW(offset+16+i*22);
					cards[2]	= buf.getW(offset+18+i*22);
					cards[3]	= buf.getW(offset+20+i*22);
					
					try {
						String sql;
						if (!isCashShop)
							sql = "INSERT INTO `vendinglog` (`time`,`char_id`,`nameid`,`amount`,`identify`,`refine`,`attribute`,`card0`,`card1`,`card2`,`card3`,`value`) VALUES (NOW(),?,?,?,?,?,?,?,?,?,?,?)";
						else
							sql = "INSERT INTO `vendinglog` (`time`,`char_id`,`nameid`,`amount`,`identify`,`refine`,`attribute`,`card0`,`card1`,`card2`,`card3`,`value`,`cash`) VALUES (NOW(),?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement psAgregar = Mysql.getInstancia().getConnection().prepareStatement(sql);
						psAgregar.setLong(1, owner_id);
						psAgregar.setInt(2, nameid);
						psAgregar.setInt(3, amount);
						psAgregar.setInt(4, identify);
						psAgregar.setInt(5, refine);
						psAgregar.setInt(6, attribute);
						psAgregar.setInt(7, (short)cards[0]);
						psAgregar.setInt(8, (short)cards[1]);
						psAgregar.setInt(9, (short)cards[2]);
						psAgregar.setInt(10, (short)cards[3]);
						if (!isCashShop) {
							psAgregar.setLong(11, value);
						} else {
							psAgregar.setLong(11, (long) (value*goldValue));
							psAgregar.setLong(12, value);
						}
						psAgregar.executeUpdate();
					} catch (SQLException e) {
						System.err.println("error insert row: " + e.toString());
						System.err.printf("data: %d, %d, %d, %d, %d, %d, %d, %d, %d, %d\n", value, amount, nameid, identify, attribute, refine, cards[0], cards[1], cards[2], cards[3]);
					}
				}
			}
			
		});

		// clif_walkok
		/// Notifies the client, that it is walking (ZC_NOTIFY_PLAYERMOVE).
		/// 0087 <walk start time>.L <walk data>.6B
		manager.addListener(lister4 = new PacketListenerByCmd(2, 0x0087) {

			boolean back = false;
			int[] data;
			
			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long difftime = 700;
				data = event.buf.getPos2(6);
				
				System.out.printf("%d,%d to %d,%d; back=%d\n", data[0], data[1], data[2], data[3], back?1:0);
				
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						int y;
						if (back)
							y = start_y;
						else
							y = end_y;
						
						if (data[3] != y) {
							try {
								int move_y;
								if (data[3] < y)
									move_y = Math.min(data[3] + 5, y);
								else
									move_y = Math.max(data[3] - 5, y);
								
								System.out.printf("move to %d,%d\n", x, move_y);

								serf.WalkToXY(x, move_y);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							if (!back) {
								System.out.printf("back to %d,%d\n", x, start_y);
								back = true;
								manager.removeListener(lister1);
								manager.removeListener(lister2);
								manager.removeListener(lister3);
								
								// clif_walkok
								Packet buf1 = Packet.create(12);
								buf1.setW(0, 0x0087);
								buf1.setL(2, tick.getServerTick());
								buf1.setPos2(6, new int[] {x, end_y, x, end_y, 8, 8});
								manager.triggerEvent(2, buf1, 0x0087, 12);
							} else {
								manager.removeListener(lister4);
								System.out.println("end vending");

								try {
									serf.QuitGame();
								} catch (IOException e) {
									//handler.disconnect();
								}
							}
						}
					}
					
				}, difftime);
			}
		
		});
		
		try {
			serf.WalkToXY(x, start_y);
		} catch (IOException e) {
			// clif_walkok
			Packet buf1 = Packet.create(12);
			buf1.setW(0, 0x0087);
			buf1.setL(2, tick.getServerTick());
			buf1.setPos2(6, new int[] {x, start_y, x, start_y, 8, 8});
			manager.triggerEvent(2, buf1, 0x0087, 12);
		}
	}
	
}
