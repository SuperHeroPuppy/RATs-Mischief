package ladysnake.ratsmischief.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Unique
	private DamageSource lastSource;

	@Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$keepHostAligned(Entity other, CallbackInfo ci) {
		if (RatPossession.isControllerAndHost((Entity) (Object) this, other)) ci.cancel();
	}

	@Inject(method = "damage", at = @At(value = "HEAD"))
	private void mischief$logSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.lastSource = source;
	}

	@WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
	private void mischief$lessKnockback(LivingEntity entity, double x, double y, double z, Operation<Void> operation) {
		if (this.lastSource != null && this.lastSource.getAttacker() instanceof RatEntity) {
			return;
		}
		operation.call(entity, x, y, z);
	}
}
