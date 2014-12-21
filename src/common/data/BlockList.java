package common.data;

public class BlockList {
	public long id;
	public int m, x, y;
	public int type;	//enum bl_type type;
	
	//enum bl_type
	public static final int BL_NUL   = 0x000;
	public static final int BL_PC    = 0x001;
	public static final int BL_MOB   = 0x002;
	public static final int BL_PET   = 0x004;
	public static final int BL_HOM   = 0x008;
	public static final int BL_MER   = 0x010;
	public static final int BL_ITEM  = 0x020;
	public static final int BL_SKILL = 0x040;
	public static final int BL_NPC   = 0x080;
	public static final int BL_CHAT  = 0x100;
	public static final int BL_ELEM  = 0x200;
	public static final int BL_ALL   = 0xFFF;
	public static final int BL_CHAR  = (BL_PC|BL_MOB|BL_HOM|BL_MER|BL_ELEM);
}
