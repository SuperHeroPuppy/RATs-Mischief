package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The vanilla HUD otherwise hides its status bars when the camera is a mob. */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	private static final Identifier RAT_HOTBAR_TEXTURE = new Identifier("minecraft", "textures/gui/widgets.png");
	@Shadow private int scaledWidth;
	@Shadow private int scaledHeight;
	@Shadow private void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines,
		int regeneratingHeartIndex, float maxHealth, int health, int lastHealth, int absorption, boolean blinking) {}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$renderEmptyRatHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!RatPossessionClient.isControllingRat()) return;
		int x = this.scaledWidth / 2;
		context.getMatrices().push();
		context.getMatrices().translate(0, 0, -90);
		// Requiem's fully locked inventory leaves just the host's single hand slot.
		context.drawTexture(RAT_HOTBAR_TEXTURE, x - 12, this.scaledHeight - 22, 0, 0, 3, 22);
		context.drawTexture(RAT_HOTBAR_TEXTURE, x - 9, this.scaledHeight - 22, 3, 0, 18, 22);
		context.drawTexture(RAT_HOTBAR_TEXTURE, x + 9, this.scaledHeight - 22, 179, 0, 3, 22);
		context.drawTexture(RAT_HOTBAR_TEXTURE, x - 12, this.scaledHeight - 23, 0, 22, 24, 22);
		if (!client.player.getMainHandStack().isEmpty()) {
			context.drawItem(client.player.getMainHandStack(), x - 8, this.scaledHeight - 19);
			context.drawItemInSlot(client.textRenderer, client.player.getMainHandStack(), x - 8, this.scaledHeight - 19);
		}
		context.getMatrices().pop();
		ci.cancel();
	}

	@Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$renderRatHearts(DrawContext context, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		RatEntity rat = RatPossessionClient.getControlledRat();
		if (client.player == null || rat == null) return;
		int health = (int) Math.ceil(rat.getHealth());
		this.renderHealthBar(context, client.player, this.scaledWidth / 2 - 91, this.scaledHeight - 39,
			10, -1, rat.getMaxHealth(), health, health, 0, false);
		ci.cancel();
	}
	@Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$showPlayerHud(CallbackInfoReturnable<PlayerEntity> cir) {
		if (RatPossessionClient.isControllingRat()) {
			cir.setReturnValue(MinecraftClient.getInstance().player);
		}
	}
}
