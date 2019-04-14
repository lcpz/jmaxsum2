package toolkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

	public static List<Integer> arr2List(int[] array) {
		List<Integer> l = new ArrayList<Integer>();
		for (int i = 0; i < array.length; i++)
			l.add(array[i]);
		return l;
	}

	public static HashMap<Integer, Float> getZeroMessages(int[] domain) {
		HashMap<Integer, Float> map = new HashMap<Integer, Float>();
		for (int d : domain)
			map.put(d, 0f);
		return map;
	}

	public static float checkedSum(float a, float b) {
		float sum = a + b;

		if (Float.isFinite(sum))
			return sum;
		else {
			if (Float.isFinite(a))
				return a;

			if (Float.isFinite(b))
				return b;

			return Float.NEGATIVE_INFINITY;
		}
	}

}