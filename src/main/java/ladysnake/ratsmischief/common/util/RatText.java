package ladysnake.ratsmischief.common.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class RatText {
	private RatText() {
	}

	public static MutableText withColor(Text text, int color) {
		return text.copy().styled(style -> style.withColor(color));
	}
}
