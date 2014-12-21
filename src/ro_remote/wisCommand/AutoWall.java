package ro_remote.wisCommand;

import java.util.LinkedList;
import java.util.TimerTask;

import packet.packet_db;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;
import ro_remote.ClientBot;
import ro_remote.ClientHandler;

public class AutoWall {
	private ClientBot bot;
	private ClientHandler handler;
	private PacketManager manager;
	
	public AutoWall(ClientBot bot) {
		this.bot = bot;
		this.handler = bot.handler;
		this.manager = bot.manager;
	}
	
	PacketListener lister1 = null;
	PacketListener lister2;
	PacketListener lister3;
	
	class SkillPos {
		int skill_id;
		int skill_lv;
		int x;
		int y;
		
		long id = 0;
	}
	
	LinkedList<SkillPos> skillList = new LinkedList<SkillPos>();
	
	boolean isRunFirst = false;
	
	public void first() {
		// clif_getareachar_skillunit
		/// Notifies the client of a skill unit.
		/// 011f <id>.L <creator id>.L <x>.W <y>.W <unit id>.B <visible>.B (ZC_SKILL_ENTRY)
		manager.addListener(lister2 = new PacketListenerByCmd(2, 0x11f) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long id = event.buf.getL(2);
				int x = event.buf.getW(10);
				int y = event.buf.getW(12);
				int unit_id = event.buf.getB(14);
				
				if (unit2Skill(unit_id) != 0) {
					SkillPos s = findSkill(x, y);
					if (s != null) {
						s.id = id;
						
						//bot.PushMessage("skillunit: " + id);
					}
				}
			}
			
		});

		// clif_skill_delunit
		/// Removes a skill unit (ZC_SKILL_DISAPPEAR).
		/// 0120 <id>.L
		manager.addListener(lister3 = new PacketListenerByCmd(2, 0x120) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long id = event.buf.getL(2);
				SkillPos s = findSkill(id);
				
				if (s != null) {
					s.id = 0;
					//bot.PushMessage("delunit: " + id);
					
					bot.skill.addSkill(s.skill_id, s.skill_lv, s.x, s.y, 3);
				}
			}
			
		});
		
		isRunFirst = true;
	}
	
	public void run() {
		if (!isRunFirst)
			first();
		
		if (lister1 == null) {
			// clif_parse_UseSkillToPos
			/// Request to use a ground skill.
			/// 0366 <skill lv>.W <skill id>.W <x>.W <y>.W (CZ_USE_SKILL_TOGROUND2)
			/// There are various variants of this packet, some of them have padding between fields.
			lister1 = new PacketListenerByCmd(1, 0x366) {

				@Override
				public void run(PacketListener lister, PacketEvent event) {
					packet_db.s_packet_db info = packet_db.packet_db[handler.packet_ver.packet_ver][event.cmd];
					int skill_lv = event.buf.getW(info.pos[0]);
					int skill_id = event.buf.getW(info.pos[1]);
					int x = event.buf.getW(info.pos[2]);
					int y = event.buf.getW(info.pos[3]);
					
					if (skill2Unit(skill_id) != 0) {
						SkillPos s = new SkillPos();
						s.skill_id = skill_id;
						s.skill_lv = skill_lv;
						s.x = x;
						s.y = y;
						skillList.add(s);
						// FIXME: 防止重複加入
						bot.PushMessage("紀錄技能: " + skill_id + " lv" + skill_lv + "; " + x + "," + y);
					}
				}
				
			};
		}
		
		manager.addListener(lister1);
		bot.PushMessage("開始紀錄技能");
		
		bot.timer.schedule(new TimerTask() {

			@Override
			public void run() {
				manager.removeListener(lister1);
				bot.PushMessage("停止紀錄技能");
			}
			
		}, 10000);
	}

	public void clear() {
		skillList.clear();
		bot.PushMessage("清除技能紀錄");
	}

	// 12,MG_SAFETYWALL,暗之障壁
	// 25,AL_PNEUMA,光之障壁

	int skill2Unit(int skill_id) {
		switch(skill_id) {
		case 12:
			return 0x7e;
		case 25:
			return 0x85;
		}
		return 0;
	}

	int unit2Skill(int unit_id) {
		switch(unit_id) {
		case 0x7e:
			return 12;
		case 0x85:
			return 25;
		}
		return 0;
	}
	
	SkillPos findSkill(int x, int y) {
		for(SkillPos s : skillList) {
			if (s.x == x && s.y == y) {
				return s;
			}
		}
		return null;
	}
	
	SkillPos findSkill(long id) {
		for(SkillPos s : skillList) {
			if (s.id == id) {
				return s;
			}
		}
		return null;
	}

}
