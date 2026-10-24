package ladysnake.ratsmischief.common.util;

public final class RatMath {
	private RatMath() {
	}

	public static int clampLoop(int value, int min, int maxExclusive) {
		if (maxExclusive <= min) {
			return min;
		}
		return min + Math.floorMod(value - min, maxExclusive - min);
	}
}
