package common.data;

public class CharStatus {
	public long char_id;
	public long account_id;
	public long partner_id;
	public long father;
	public long mother;
	public long child;

	public long base_exp,job_exp;
	public long zeny;

	public int class_;
	public long status_point,skill_point;
	public long hp,max_hp,sp,max_sp;
	public long option;
	public long manner;
	public long karma;
	public int hair,hair_color,clothes_color;
	public long party_id,guild_id,pet_id,hom_id,mer_id,ele_id;
	public long fame;

	// Mercenary Guilds Rank
	public long arch_faith, arch_calls;
	public long spear_faith, spear_calls;
	public long sword_faith, sword_calls;

	public int weapon; // enum weapon_type
	public int shield; // view-id
	public int head_top,head_mid,head_bottom;
	public long robe;

	public String name;
	public long base_level,job_level;
	public int str,agi,vit,int_,dex,luk;
	public int slot,sex;

	public byte[] mapip;
	public long mapport;

	public String last_map;
	//struct point last_point,save_point,memo_point[MAX_MEMOPOINTS];
	//struct item inventory[MAX_INVENTORY],cart[MAX_CART];
	//struct storage_data storage;
	//struct s_skill skill[MAX_SKILL];

	//struct s_friend friends[MAX_FRIENDS]; //New friend system [Skotlex]
	//struct hotkey hotkeys[MAX_HOTKEYS];
	
	public boolean show_equip;
	public int rename;

	public long delete_date;

	// Char server addon system
	public long character_moves;
}