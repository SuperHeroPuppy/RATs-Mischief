package ladysnake.ratsmischief.client;

import com.mojang.datafixers.util.Pair;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

/** Inventory layout used while looking through a rat. */
public final class RatInventoryScreen extends InventoryScreen {

	public RatInventoryScreen(PlayerEntity player) {
		super(player);
		this.backgroundWidth = 176;
		this.backgroundHeight = 166;
		this.titleX = 96;
		this.titleY = 6;
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		if (this.client != null && this.client.getCameraEntity() instanceof RatEntity rat) {
			context.drawText(this.textRenderer, rat.getName(), this.titleX, this.titleY, 0x404040, false);
		}
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = this.x;
		int y = this.y;
		// The alternate inventory frame and its cropped entity preview.
		context.fill(x + 1, y + 1, x + 175, y + 165, 0xFF555555);
		context.fill(x, y, x + 174, y + 164, 0xFFFFFFFF);
		context.fill(x + 2, y + 2, x + 173, y + 163, 0xFFC6C6C6);
		context.fill(x + 15, y + 82, x + 161, y + 155, 0xFF777777);
		context.fill(x + 16, y + 83, x + 160, y + 153, 0xFF000000);

		for (int row = 0; row < 4; row++) this.drawSlot(context, x + 7, y + 7 + row * 18);
		this.drawSlot(context, x + 46, y + 61);
		this.drawSlot(context, x + 76, y + 61);
		this.drawSlotIcon(context, this.handler.slots.get(45), x + 77, y + 62);
		for (int row = 0; row < 4; row++) {
			this.drawSlotIcon(context, this.handler.slots.get(5 + row), x + 8, y + 8 + row * 18);
		}
		for (int row = 0; row < 2; row++) {
			for (int column = 0; column < 2; column++) this.drawSlot(context, x + 97 + column * 18, y + 17 + row * 18);
		}
		context.fill(x + 135, y + 30, x + 149, y + 34, 0xFF8B8B8B);
		context.fill(x + 146, y + 27, x + 151, y + 37, 0xFF8B8B8B);
		this.drawSlot(context, x + 153, y + 25);
		if (this.client != null && this.client.getCameraEntity() instanceof RatEntity rat) {
			context.enableScissor(x + 16, y + 83, x + 160, y + 153);
			InventoryScreen.drawEntity(context, x + 91, y + 150, 30,
				(float) (x + 91 - mouseX), (float) (y + 100 - mouseY), rat);
			context.disableScissor();
		}
	}

	private void drawSlot(DrawContext context, int x, int y) {
		context.fill(x, y, x + 18, y + 18, 0xFF373737);
		context.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
		context.fill(x + 2, y + 2, x + 17, y + 17, 0xFF8B8B8B);
	}

	private void drawSlotIcon(DrawContext context, Slot slot, int x, int y) {
		if (this.client == null) return;
		Pair<Identifier, Identifier> spriteIds = slot.getBackgroundSprite();
		if (spriteIds == null) return;
		Sprite sprite = this.client.getSpriteAtlas(spriteIds.getFirst()).apply(spriteIds.getSecond());
		context.drawSprite(x, y, 0, 16, 16, sprite);
	}
}
