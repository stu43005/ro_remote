package common.data;

public class StatusData {
	public long
		hp, sp,  // see status_cpy before adding members before hp and sp
		max_hp, max_sp;
	public int
		str, agi, vit, int_, dex, luk,
		batk,
		matk_min, matk_max,
		speed,
		amotion, adelay, dmotion;
	public int mode;	//enum e_mode mode;
	public int
		hit, flee, cri, flee2,
		def2, mdef2,
		aspd_rate;
	/**
	 * defType is RENEWAL dependent and defined in src/map/config/data/const.h
	 **/
	public int def,mdef;

	public int
		def_ele, ele_lv,
		size, race;

	//struct weapon_atk rhw, lhw; //Right Hand/Left Hand Weapon.
}
