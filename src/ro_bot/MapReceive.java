package ro_bot;

import java.io.UnsupportedEncodingException;

import common.data.SessionData;

import packet.Packet;

public class MapReceive {
	Map map;
	SessionData sd;
	
	public MapReceive(Map map) {
		this.map = map;
		this.sd = map.sd;
	}
	
	/**
	 * Notifies the client, that it's connection attempt was accepted.
	 * <p>
	 * 0073 &lt;start time>.L &lt;position>.3B &lt;x size>.B &lt;y size>.B (ZC_ACCEPT_ENTER) <br>
	 * 02eb &lt;start time>.L &lt;position>.3B &lt;x size>.B &lt;y size>.B &lt;font>.W (ZC_ACCEPT_ENTER2) <br>
	 */
	public void authok(Packet buf, int cmd, int packet_len) {
		int[] pos = buf.getPos(6);
		sd.bl.x = pos[0];
		sd.bl.y = pos[1];
		sd.ud.dir = pos[2];
		if (cmd == 0x2eb)
			sd.user_font = buf.getW(11);
	}

	/**
	 * Server's tick (ZC_NOTIFY_TIME).
	 * <p>
	 * 007f &lt;time>.L
	 */
	public void notify_time(Packet buf, int cmd, int packet_len) {
		sd.tick.updateServerTick(buf.getL(2));
	}
	
	/**
	 * Notifies the client of a position change to coordinates on given map (ZC_NPCACK_MAPMOVE).
	 * <p>
	 * 0091 &lt;map name>.16B &lt;x>.W &lt;y>.W
	 */
	public void changemap(Packet buf, int cmd, int packet_len) {
		try {
			sd.status.last_map = buf.getString(2, 16);
		} catch (UnsupportedEncodingException e) {
			sd.status.last_map = "";
		}
		sd.bl.x = buf.getW(18);
		sd.bl.y = buf.getW(20);
	}
	
	/**
	 * Notifies client of a character parameter change.
	 * <p>
	 * 00b0 &lt;var id>.W &lt;value>.L (ZC_PAR_CHANGE) <br>
	 * 00b1 &lt;var id>.W &lt;value>.L (ZC_LONGPAR_CHANGE) <br>
	 * 00be &lt;status id>.W &lt;value>.B (ZC_STATUS_CHANGE) <br>
	 * 0121 &lt;current count>.W &lt;max count>.W &lt;current weight>.L &lt;max weight>.L (ZC_NOTIFY_CARTITEM_COUNTINFO) <br>
	 * 013a &lt;atk range>.W (ZC_ATTACK_RANGE) <br>
	 * 0141 &lt;status id>.L &lt;base status>.L &lt;plus status>.L (ZC_COUPLESTATUS) <br>
	 */
	public void updatestatus(Packet buf, int cmd, int packet_len) {
		int type, intvalue, intvalue2;
		long longvalue;
		switch(cmd) {
		case 0xb0:
			/// 00b0 <var id>.W <value>.L (ZC_PAR_CHANGE)
			type = buf.getW(2);
			longvalue = buf.getL(4);
			switch(type) {
			case 24:	//SP_WEIGHT
				sd.weight = longvalue;
				break;
			case 25:	//SP_MAXWEIGHT
				sd.max_weight = longvalue;
				break;
			case 0:		//SP_SPEED
				sd.battle_status.speed = (int) longvalue;
				break;
			case 11:	//SP_BASELEVEL
			case 55:	//SP_JOBLEVEL
			case 3:		//SP_KARMA
			case 4:		//SP_MANNER
			case 9:		//SP_STATUSPOINT
			case 12:	//SP_SKILLPOINT
			case 49:	//SP_HIT
			case 50:	//SP_FLEE1
			case 51:	//SP_FLEE2
			case 6:		//SP_MAXHP
			case 8:		//SP_MAXSP
			case 5:		//SP_HP
			case 7:		//SP_SP
			case 53:	//SP_ASPD
			case 41:	//SP_ATK1
			case 45:	//SP_DEF1
			case 47:	//SP_MDEF1
			case 42:	//SP_ATK2
			case 46:	//SP_DEF2
			case 48:	//SP_MDEF2
			case 52:	//SP_CRITICAL
			case 43:	//SP_MATK1
			case 44:	//SP_MATK2
				break;
			}
			break;
		case 0xb1:
			/// 00b1 <var id>.W <value>.L (ZC_LONGPAR_CHANGE)
			type = buf.getW(2);
			longvalue = buf.getL(4);
			switch(type) {
			case 20:	//SP_ZENY
			case 1:		//SP_BASEEXP
			case 2:		//SP_JOBEXP
			case 22:	//SP_NEXTBASEEXP
			case 23:	//SP_NEXTJOBEXP
				break;
			}
			break;
		case 0xbe:
			// SP_U<STAT> are used to update the amount of points necessary to increase that stat
			/// 00be <status id>.W <value>.B (ZC_STATUS_CHANGE)
			type = buf.getW(2);
			intvalue = buf.getB(4);
			switch(type) {
			case 32:	//SP_USTR
			case 33:	//SP_UAGI
			case 34:	//SP_UVIT
			case 35:	//SP_UINT
			case 36:	//SP_UDEX
			case 37:	//SP_ULUK
				break;
			}
			break;
		case 0x121:
			/// 0121 <current count>.W <max count>.W <current weight>.L <max weight>.L (ZC_NOTIFY_CARTITEM_COUNTINFO)
			
			//sd.cart_num = buf.getW(2);
			//MAX_CART = buf.getW(4);
			//sd.cart_weight = buf.getL(6);
			//sd.cart_weight_max = buf.getL(10);
			break;
		case 0x13a:
			// Tells the client how far it is allowed to attack (weapon range)
			/// 013a <atk range>.W (ZC_ATTACK_RANGE)
			
			//sd.battle_status.rhw.range = buf.getW(2);
			break;
		case 0x141: 
			/// 0141 <status id>.L <base status>.L <plus status>.L (ZC_COUPLESTATUS)
			type = (int) buf.getL(2);
			intvalue = (int) buf.getL(6);
			intvalue2 = (int) buf.getL(10);
			switch(type) {
			case 13:	//SP_STR
				sd.status.str = intvalue;
				sd.battle_status.str = intvalue + intvalue2;
				break;
			case 14:	//SP_AGI
				sd.status.agi = intvalue;
				sd.battle_status.agi = intvalue + intvalue2;
				break;
			case 15:	//SP_VIT
				sd.status.vit = intvalue;
				sd.battle_status.vit = intvalue + intvalue2;
				break;
			case 16:	//SP_INT
				sd.status.int_ = intvalue;
				sd.battle_status.int_ = intvalue + intvalue2;
				break;
			case 17:	//SP_DEX
				sd.status.dex = intvalue;
				sd.battle_status.dex = intvalue + intvalue2;
				break;
			case 18:	//SP_LUK
				sd.status.luk = intvalue;
				sd.battle_status.luk = intvalue + intvalue2;
				break;
			}
			break;
		}
	}
	
}
