package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The vanilla HUD otherwise hides its status bars when the camera is a mob. */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow private int scaledWidth;
	@Shadow private int scaledHeight;
	@Shadow private void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines,
		int regeneratingHeartIndex, float maxHealth, int health, int lastHealth, int absorption, boolean blinking) {}

	@Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$renderRatHearts(DrawContext context, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || !(client.getCameraEntity() instanceof RatEntity rat)) return;
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
