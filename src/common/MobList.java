package common;

public class MobList {
	public static int[] boss_id = new int[] {
			1088/*�n�u����*/,
			1089/*���*/,
			1090/*�i�Q����*/,
			1091/*�s��*/,
			1092/*�y�����T*/,
			1093/*�źƨ�*/,
			1096/*�ѨϪi�Q*/,
			1120/*���F�i�Q*/,
			1259/*���l�~*/,
			1268/*��{�M�h*/,
			1289/*�g���ƦZ*/,
			1582/*�c�]�i�Q*/,
			1618/*�ťj�O�w�S*/,
			1765/*�ڰ�O ���S�J�O��*/,
			1795/*��{�M�h*/
	};
	
	public static int[] mvp_id = new int[] {
			1038/*�X�먽��*/,
			1039/*�ڭ��S*/,
			1046/*���F*/,
			1059/*���Z*/,
			1086/*������*/,
			1087/*�~�H�^��*/,
			1112/*���s����*/,
			1115/*���*/,
			1147/*�ƦZ*/,
			1150/*��]��*/,
			1157/*�k�Ѥ�*/,
			1159/*�֨���*/,
			1190/*�~�H����*/,
			1251/*�B���M�h*/,
			1272/*�·t����*/,
			1312/*�Q�t�N�x*/,
			1373/*���F�M�h*/,
			1389/*�w�j�ԧB��*/,
			1418/*���D�g*/,
			1492/*���F�Z�h*/,
			1511/*�j�J�Τ�*/,
			1518/*�կ��s*/,
			1583/*����s�d*/,
			1623/*RSX-0806*/,
			1630/*�կ��s*/,
			1685/*����i*/,
			1688/*�֥��p�j*/,
			1708/*�]�C�h �F�Ǧ������O��*/,
			1719/*�}��Ǯ���Ǵ�*/,
			1734/*����-D-01*/,
			1751/*���S�J�O��*/,
			1768/*�շt���L*/,
			1779/*�����s*/,
			1785/*���Sù��*/,
			1832/*��ҧQ�S*/,
			1871/*�Z���j���x �x��*/,
			1873/*�A�G��*/,
			1874/*�A�G�Ǥ�*/,
			1885/*�C�a�s*/,
			1916/*�]����ù�J*/,
			1917/*�t�˹�ù�J*/,
			1990/*�w�۲r��*/,
			2068/*�i����*/
	};
	
	public static boolean isBoss(int class_) {
		int i;
		for(i = 0; i < boss_id.length && class_ > boss_id[i]; i++);
		if (i < boss_id.length && class_ == boss_id[i])
			return true;
		return false;
	}
	
	public static boolean isMvp(int class_) {
		int i;
		for(i = 0; i < mvp_id.length && class_ > mvp_id[i]; i++);
		if (i < mvp_id.length && class_ == mvp_id[i])
			return true;
		return false;
	}
}
