package test;

import java.util.Random;

public class TestElunium {

	static int[] refineDb = new int[] {100,100,100,100, 60, 40, 40, 20, 20, 10};
	static final int testNum = 1000000;
	
	static int enrichedEluniumValue = 69;
	static int hdEluniumValue = 2499;
	
	static int elunium(boolean hd, int startRefine, int maxRefine) {
		int num = 0, refine = startRefine;
		Random rnd = new Random();
		while (refine < maxRefine) {
			if (refineDb[refine] != 100)
				num++;
			if (refineDb[refine] == 100 || rnd.nextInt(100) < refineDb[refine]) {
				refine++;
			} else {
				if (!hd)
					refine--;
			}
		}
		return num;
	}
	
	static double eluniumTest(boolean hd, int startRefine, int maxRefine, int numMult, boolean print) {
		int sum = 0, num = testNum, max = -1, min = -1;
		for(int i=0;i<num;i++) {
			int t = elunium(hd, startRefine, maxRefine);
			t *= numMult;
			sum += t;
			if (max == -1 || max < t) max = t;
			if (min == -1 || min > t) min = t;
		}
		double avg = (double)sum/num;
		if (print)
			System.out.printf("%5d, %3d, %7.2f\n", max, min, avg);
		return avg / 10 * (hd ? hdEluniumValue : enrichedEluniumValue);
	}
	
	static void test(int start, int end, int numMult) {
		for(int i=(end>7?7:end);i<=end;i++) {
			double num = 0;
			System.out.println("  max, min, average");
			num += eluniumTest(false, start, i, numMult, true);
			if (i != end)
				num += eluniumTest(true, i, end, numMult, true);
			System.out.printf("Cash sum: %10.4f\n", num);
			System.out.println("==========");
		}
	}
	
	public static void main(String[] args) {
		test(4, 9, 1);
	}

}

/*
numMult = 1
  max, min, average
  222,   3,   16.67
   73,   2,    9.99
Cash sum:  2610.8765
==========
  max, min, average
  782,   4,   61.59
   77,   1,    5.00
Cash sum:  1674.3718
==========
  max, min, average
 3374,   5,  246.62
Cash sum:  1701.6871
==========


numMult = 2
  max, min, average
  430,   6,   33.31
  154,   4,   20.00
Cash sum:  5228.9820
==========
  max, min, average
 1884,   8,  123.16
  128,   2,   10.00
Cash sum:  3349.8277
==========
  max, min, average
 8826,  10,  493.99
Cash sum:  3408.5274
==========


numMult = 3
  max, min, average
  552,   9,   50.00
  237,   6,   30.02
Cash sum:  7847.2832
==========
  max, min, average
 2514,  12,  184.92
  168,   3,   15.02
Cash sum:  5029.7993
==========
  max, min, average
10596,  15,  739.39
Cash sum:  5101.7645
==========
*/
