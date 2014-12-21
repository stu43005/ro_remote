package common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import packet.Serf;
import packet.packet_db;
import packet.event.PacketEvent;
import packet.event.PacketListener;
import packet.event.PacketListenerByCmd;
import packet.event.PacketManager;

public class Skill {
	private Serf serf;
	private PacketManager manager;
	private Timer timer;
	private Time tick;
	private long account_id;
	
	public Skill(Serf serf, PacketManager manager, Timer timer, Time tick, long account_id) {
		this.serf = serf;
		this.manager = manager;
		this.timer = timer;
		this.tick = tick;
		this.account_id = account_id;
		init();
	}

	private long delayEnd = 0;
	private int taskDelay = 100;
	
	private void toggleTask(long casttime) {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				delayEndTask();
			}
			
		}, taskDelay + casttime);
	}
	
	private synchronized void delayEndTask() {
		if (tick.getServerTick() >= delayEnd) {
			SkillQueue s = sq.poll();
			if (s != null) {
				System.out.println(s.toString());
				
				// TODO: 發送前判斷
				try {
					if (s.type == 1)
						serf.UseSkillToId(s.skill_id, s.skill_lv, s.target_id);
					else
						serf.UseSkillToPos(s.skill_id, s.skill_lv, s.pos_x, s.pos_y);
				} catch (IOException e) {
					e.printStackTrace();
				}
				delayEnd = tick.getServerTick() + taskDelay;
			}
		}
	}
	
	private void init() {
		if (!packet_db.readed) {
			try {
				packet_db.packetdb_readdb();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}
		}
		
		/// Notifies clients in area, that an object is about to use a skill.
		/// 013e <src id>.L <dst id>.L <x>.W <y>.W <skill id>.W <property>.L <delaytime>.L (ZC_USESKILL_ACK)
		/// 07fb <src id>.L <dst id>.L <x>.W <y>.W <skill id>.W <property>.L <delaytime>.L <is disposable>.B (ZC_USESKILL_ACK2)
		/// property:
		///     0 = Yellow cast aura
		///     1 = Water elemental cast aura
		///     2 = Earth elemental cast aura
		///     3 = Fire elemental cast aura
		///     4 = Wind elemental cast aura
		///     5 = Poison elemental cast aura
		///     6 = Holy elemental cast aura
		///     ? = like 0
		/// is disposable:
		///     0 = yellow chat text "[src name] will use skill [skill name]."
		///     1 = no text
		manager.addListener(new PacketListenerByCmd(2, 0x13e, 0x7fb) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				long src = event.buf.getL(2);
				if (src == account_id) {
					long casttime = event.buf.getL(20);
					delayEnd = tick.getServerTick() + casttime;
					
					//System.out.println(casttime);
					
					toggleTask(casttime);
				}
			}
			
		});

		// clif_status_change
		/// Notifies clients of a status change.
		/// 043f <index>.W <id>.L <state>.B <remain msec>.L { <val>.L }*3 (ZC_MSG_STATE_CHANGE2) [used exclusively for starting statuses on pcs]
		manager.addListener(new PacketListenerByCmd(2, 0x043f) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int index = event.buf.getW(2);
				if (index == 46/*SI_ACTIONDELAY*/) {
					long time = event.buf.getL(9);
					delayEnd = tick.getServerTick() + time;
					
					//System.out.println(time);

					toggleTask(time);
				}
			}
			
		});

		// clif_skillinfoblock
		/// Updates whole skill tree (ZC_SKILLINFO_LIST).
		/// 010f <packet len>.W { <skill id>.W <type>.L <level>.W <sp cost>.W <attack range>.W <skill name>.24B <upgradable>.B }*
		// clif_addskill
		/// Adds new skill to the skill tree (ZC_ADD_SKILL).
		/// 0111 <skill id>.W <type>.L <level>.W <sp cost>.W <attack range>.W <skill name>.24B <upgradable>.B
		manager.addListener(new PacketListenerByCmd(2, 0x10f, 0x111) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int max, offset;
				if (event.cmd == 0x10f) {
					max = (event.packet_len - 4) / 37;
					offset = 4;
				} else {
					max = 1;
					offset = 2;
				}
				
				for(int i = 0; i < max; i++) {
					SkillInfo info = new SkillInfo();
					info.id = event.buf.getW(offset + 37 * i);
					info.inf = event.buf.getL(offset + 37 * i + 2);
					info.lv = event.buf.getW(offset + 37 * i + 6);
					info.sp = event.buf.getW(offset + 37 * i + 8);
					info.range = event.buf.getW(offset + 37 * i + 10);
					try {
						info.name = event.buf.getString(offset + 37 * i + 12, 24);
					} catch (UnsupportedEncodingException e) {
						info.name = "";
					}
					info.upgradable = event.buf.getB(offset + 37 * i + 36) > 0;
					skilltree.put(info.id, info);
				}
			}
			
		});
		
		// clif_deleteskill
		/// Deletes a skill from the skill tree (ZC_SKILLINFO_DELETE).
		/// 0441 <skill id>.W
		manager.addListener(new PacketListenerByCmd(2, 0x441) {

			@Override
			public void run(PacketListener lister, PacketEvent event) {
				int id = event.buf.getW(2);
				skilltree.remove(id);
			}
			
		});
	}

	@SuppressWarnings("unused")
	private class SkillInfo {
		int id;
		long inf;
		int lv;
		int sp;
		int range;
		String name;
		boolean upgradable;
		
		public String toString() {
			return (name.equals("") ? id : name) + " Lv " + lv + " (Sp : " + sp + ")";
		}
	}
	
	private HashMap<Integer, SkillInfo> skilltree = new HashMap<Integer, SkillInfo>();
	
	public int getSkillLv(int skill_id) {
		SkillInfo info = skilltree.get(skill_id);
		if (info == null)
			return 0;
		return info.lv;
	}
	
	private class SkillQueue {
		int skill_id;
		int skill_lv;
		/**
		 * 技能類型
		 * <p>
		 * <ol>
		 * <li>對單位</li>
		 * <li>對地面</li>
		 * </ol>
		 */
		int type;
		long target_id;
		int pos_x;
		int pos_y;
		/**
		 * 優先權 (越大的優先)
		 */
		int priority = 0;
		
		public String toString() {
			if (type == 1)
				return skill_id + " lv" + skill_lv + "; target " + target_id;
			else
				return skill_id + " lv" + skill_lv + "; pos " + pos_x + "," + pos_y;
		}
	}
	private LinkedList<SkillQueue> sq = new LinkedList<SkillQueue>();
	
	private synchronized void sortSkillQueue() {
		Collections.sort(sq, new Comparator<SkillQueue>() {

			@Override
			public int compare(SkillQueue o1, SkillQueue o2) {
				// 大的在前
				return o2.priority - o1.priority;
			}
			
		});
	}
	
	public synchronized boolean addSkill(int skill_id, int skill_lv, long account_id, int priority) {
		SkillQueue s = new SkillQueue();
		s.skill_id = skill_id;
		if (skill_lv == -1) {
			s.skill_lv = getSkillLv(skill_id);
		} else {
			s.skill_lv = skill_lv;
		}
		if (s.skill_lv < 1)
			return false;
		s.type = 1;
		s.target_id = account_id;
		s.priority = priority;
		sq.offer(s);
		
		sortSkillQueue();
		toggleTask(0);
		return true;
	}
	public synchronized boolean addSkill(int skill_id, int skill_lv, int pos_x, int pos_y, int priority) {
		SkillQueue s = new SkillQueue();
		s.skill_id = skill_id;
		if (skill_lv == -1) {
			s.skill_lv = getSkillLv(skill_id);
		} else {
			s.skill_lv = skill_lv;
		}
		if (s.skill_lv < 1)
			return false;
		s.type = 2;
		s.pos_x = pos_x;
		s.pos_y = pos_y;
		s.priority = priority;
		sq.offer(s);
		
		sortSkillQueue();
		toggleTask(0);
		return true;
	}
}
