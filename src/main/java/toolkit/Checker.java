package toolkit;

public class Checker {

	public static final boolean assertive = false;

	public static void check(boolean condition, String s) {
		if (!condition)
			throw new IllegalArgumentException(Thread.currentThread().getStackTrace()[2].getClassName() + ": " + s);
	}

}
