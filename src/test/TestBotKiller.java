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
			l.add("�A�n�A���F�O���L�~�����C�����ҡA");
			l.add("�^���U������ܪ��Ʀr�C");
			l.add("���p�h�@�p�����@�p�p�p�@�p�h���@");
			l.add(" ` :���@ . `�h�@: ; ���@��. : �@");
			l.add("���p�p�@�p���p�@�p���h�@�p�h�p�@");
			l.add("�p `, �@�p : .�@` . �p�@: �@�h�@");
			l.add("���p���@�h���p�@�h�h�h�@�������@");
			break;
		case 1:
			System.out.println("8361");
			l.add("�A�n�A���F�O���L�~�����C�����ҡA");
			l.add("���X�U������ܪ��ϮסC");
			l.add("�����h�@���h���@�p���h�@; �� :�@");
			l.add("��``�p�@�@; �p�@��, ``�@ :��. �@");
			l.add("�����p�@�p�p�h�@�p�p�h�@: ��, �@");
			l.add("��. �h�@, , �h�@�h `�h�@; �h :�@");
			l.add("�p�p�h�@���h�p�@�p�h�h�@ :�p  �@");
			break;
		case 2:
			System.out.println("8672");
			l.add("�A�n�A���F�O���L�~�����C�����ҡA");
			l.add("�^�ФU������ܪ��Ʀr�C");
			l.add("�p�p�p�@�p�p�p�@�h�h�p�@�h���h�@");
			l.add("�h. �p�@�p. ; �@�p` �p�@�@``�h�@");
			l.add("���h�h�@�p�p�h�@�� .�h�@�h�p���@");
			l.add("�h: �h�@�p `�h�@``, ���@�h ,, �@");
			l.add("�p���p�@�h�����@`  :�p�@���p���@");
			break;
		case 3:
			System.out.println("7378");
			l.add("�A�n�A���F�O���L�~�����C�����ҡA");
			l.add("�^���U���ҦC���Ʀr�C");
			l.add("�h�h�p�@���p�p�@�p�����@�h�h���@");
			l.add("��. ���@` . �p�@�h :�h�@�� `�h�@");
			l.add("�p``�p�@�h�p�p�@�h, �h�@���h�p�@");
			l.add("``. �h�@`  `�p�@; , ���@��  ���@");
			l.add(" `, ���@�h���p�@``, �h�@���p�p�@");
			break;
		case 4:
			System.out.println("4942");
			l.add("�� :�h�@�p���p�@�p�@�p�@�p�p�p�@");
			l.add("��. ���@�p .�h�@�h; �h�@`` .���@");
			l.add("�����h�@���h�h�@�p�p�h�@�p�p�h�@");
			l.add(" ,  ���@ .` �p�@. ; �h�@�p,  .�@");
			l.add(" : ,���@�@, �h�@:  ,�h�@�p�p�p�@");
			break;
		}
		
		System.out.println(BotKiller.botkiller(l));
	}

}
