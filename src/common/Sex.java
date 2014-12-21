package common;

public class Sex {
	public static final int SEX_FEMALE = 0;
	public static final int SEX_MALE = 1;
	
	public static char num2str(int num) {
		return (num ==  SEX_FEMALE  ) ? 'F' : (num ==  SEX_MALE  ) ? 'M' : 'S';
	}
}
