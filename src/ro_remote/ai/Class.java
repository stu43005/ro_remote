package ro_remote.ai;

import java.util.Timer;

import common.Skill;
import common.Time;
import packet.Serf;
import packet.event.PacketManager;

public abstract class Class {
	Serf serf;
	PacketManager manager;
	Skill skill;
	Timer timer;
	Time tick;
	
	public void init(Serf serf, PacketManager manager, Skill skill, Timer timer, Time tick) {
		this.serf = serf;
		this.manager = manager;
		this.skill = skill;
		this.timer = timer;
		this.tick = tick;
		init2();
	}

	public abstract void init2();
	public abstract void start();
	public abstract void stop();
}
