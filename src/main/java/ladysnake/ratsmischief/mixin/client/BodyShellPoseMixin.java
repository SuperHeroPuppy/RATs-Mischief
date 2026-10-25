package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BodyShellPoseMixin {
	@Inject(method = "getLeaningPitch", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$standAtBodyOrigin(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (RatPossessionClient.isRenderingBodyShell()
			&& (Object) this == MinecraftClient.getInstance().player) {
			cir.setReturnValue(0.0F);
		}
	}
}
