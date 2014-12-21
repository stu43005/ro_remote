package common;

public abstract class Callback {
	Object[] args;
	
	public Callback() {
		args = new Object[0];
	}
	public Callback(Object... objects) {
		args = objects;
	}
	
	public abstract Object call();
}
