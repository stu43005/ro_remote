package common.data;

import java.util.HashMap;

public class Party {
	public static class PartyMember {
		int account_id;
		//int char_id;
		String name;
		int class_;
		int lv;
		boolean leader;
		boolean online;
	}
	
	//int party_id;
	String name;
	/**
	 * Count of online characters.
	 */
	int count;
	int exp;
	/**
	 * &1: Party-Share (round-robin),<br> &2: pickup style: shared.
	 */
	int item;
	
	HashMap<Integer, PartyMember> member = new HashMap<Integer, PartyMember>();
}
