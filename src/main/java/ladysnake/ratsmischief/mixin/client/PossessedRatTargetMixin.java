package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Let the controller's crosshair reach blocks and entities beyond its own host. */
@Mixin(LivingEntity.class)
public abstract class PossessedRatTargetMixin {
	@Inject(method = "canHit", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$skipControlledRat(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this == RatPossessionClient.getControlledRat()) cir.setReturnValue(false);
	}
}
