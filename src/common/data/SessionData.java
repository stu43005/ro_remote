package common.data;

import java.util.*;

import common.*;

public class SessionData {
	// Login
	public long login_id1, login_id2;
	
	public long account_id;
	public int sex;
	
	// Char
	public int char_slots;
	public int MAX_CHARS;
	public int MAX_CHAR_BUF = 144;
	public long[] found_char;
	public long[] char_moves;
	public LinkedList<CharStatus> chars = new LinkedList<CharStatus>();
	public long pincode_seed;
	public int pincode_state;
	
	public long char_id;
	
	// Map
	//long client_tick;
	public Time tick = new Time();
	public int user_font;
	
	public BlockList bl = new BlockList();
	public UnitData ud = new UnitData();
	//struct view_data vd;
	public StatusData base_status = new StatusData(), battle_status = new StatusData();
	//struct status_change sc;

	public CharStatus status;
	
	public long weight, max_weight;
}
