package common.data;


public class UnitData {
	public BlockList bl;
	//struct walkpath_data walkpath;
	//struct skill_timerskill *skilltimerskill[MAX_SKILLTIMERSKILL];
	//struct skill_unit_group *skillunit[MAX_SKILLUNITGROUP];
	//struct skill_unit_group_tickset skillunittick[MAX_SKILLUNITGROUPTICKSET];
	public int attacktarget_lv;
	public int to_x,to_y;
	public int skillx,skilly;
	public int skill_id,skill_lv;
	public long skilltarget;
	public long skilltimer;
	public long target;
	public long target_to;
	public long attacktimer;
	public long walktimer;
	public long chaserange;
	public long attackabletime;
	public long canact_tick;
	public long canmove_tick;
	public int dir;
	public int walk_count;
	public int target_count;
	/*struct {
		unsigned change_walk_target : 1 ;
		unsigned skillcastcancel : 1 ;
		unsigned attack_continue : 1 ;
		unsigned walk_easy : 1 ;
		unsigned running : 1;
		unsigned speed_changed : 1;
	} state;*/
}
