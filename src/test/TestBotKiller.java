package test;

import java.util.LinkedList;
import java.util.Random;

import common.BotKiller;

public class TestBotKiller {

	public static void main(String[] args) throws Exception {
		LinkedList<String> l = new LinkedList<String>();
		
		switch(new Random().nextInt(5)) {
		case 0:
			System.out.println("2235");
			l.add("你好，為了保持無外掛的遊戲環境，");
			l.add("回答下面所顯示的數字。");
			l.add("■▉▇　▉■■　▉▉▉　▉▇■　");
			l.add(" ` :■　 . `▇　: ; ■　■. : 　");
			l.add("■▉▉　▉■▉　▉■▇　▉▇▉　");
			l.add("▉ `, 　▉ : .　` . ▉　: 　▇　");
			l.add("■▉■　▇■▉　▇▇▇　■■■　");
			break;
		case 1:
			System.out.println("8361");
			l.add("你好，為了保持無外掛的遊戲環境，");
			l.add("答出下面所顯示的圖案。");
			l.add("■■▇　■▇■　▉■▇　; ■ :　");
			l.add("■``▉　　; ▉　■, ``　 :■. 　");
			l.add("■■▉　▉▉▇　▉▉▇　: ■, 　");
			l.add("■. ▇　, , ▇　▇ `▇　; ▇ :　");
			l.add("▉▉▇　■▇▉　▉▇▇　 :▉  　");
			break;
		case 2:
			System.out.println("8672");
			l.add("你好，為了保持無外掛的遊戲環境，");
			l.add("回覆下面所顯示的數字。");
			l.add("▉▉▉　▉▉▉　▇▇▉　▇■▇　");
			l.add("▇. ▉　▉. ; 　▉` ▉　　``▇　");
			l.add("■▇▇　▉▉▇　■ .▇　▇▉■　");
			l.add("▇: ▇　▉ `▇　``, ■　▇ ,, 　");
			l.add("▉■▉　▇■■　`  :▉　■▉■　");
			break;
		case 3:
			System.out.println("7378");
			l.add("你好，為了保持無外掛的遊戲環境，");
			l.add("回答下面所列表的數字。");
			l.add("▇▇▉　■▉▉　▉■■　▇▇■　");
			l.add("■. ■　` . ▉　▇ :▇　■ `▇　");
			l.add("▉``▉　▇▉▉　▇, ▇　■▇▉　");
			l.add("``. ▇　`  `▉　; , ■　■  ■　");
			l.add(" `, ■　▇■▉　``, ▇　■▉▉　");
			break;
		case 4:
			System.out.println("4942");
			l.add("■ :▇　▉■▉　▉　▉　▉▉▉　");
			l.add("■. ■　▉ .▇　▇; ▇　`` .■　");
			l.add("■■▇　■▇▇　▉▉▇　▉▉▇　");
			l.add(" ,  ■　 .` ▉　. ; ▇　▉,  .　");
			l.add(" : ,■　　, ▇　:  ,▇　▉▉▉　");
			break;
		}
		
		System.out.println(BotKiller.botkiller(l));
	}

}
