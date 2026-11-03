package ladysnake.ratsmischief.mixin;

import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {
	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$exitWhenBodyAttacked(DamageSource source, float amount,
		CallbackInfoReturnable<Boolean> cir) {
		ArmorStandEntity stand = (ArmorStandEntity) (Object) this;
		if (!RatPossession.isPossessionBody(stand)) return;
		if (amount > 0) RatPossession.onBodyAttacked(stand, source);
		cir.setReturnValue(false);
	}
}
