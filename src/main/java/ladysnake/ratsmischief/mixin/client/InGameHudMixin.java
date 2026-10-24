package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The vanilla HUD otherwise hides its status bars when the camera is a mob. */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$showPlayerHud(CallbackInfoReturnable<PlayerEntity> cir) {
		if (RatPossessionClient.isControllingRat()) {
			cir.setReturnValue(MinecraftClient.getInstance().player);
		}
	}
}
