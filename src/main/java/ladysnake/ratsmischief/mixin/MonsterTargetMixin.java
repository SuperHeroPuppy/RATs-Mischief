package ladysnake.ratsmischief.mixin;

import ladysnake.ratsmischief.common.entity.ai.TargetRatOrBodyGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MonsterTargetMixin extends LivingEntity {
	@Shadow @Final protected GoalSelector targetSelector;

	protected MonsterTargetMixin(EntityType<? extends LivingEntity> type, World world) {
		super(type, world);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void ratsmischief$targetRatsAndBodies(EntityType<? extends MobEntity> type, World world, CallbackInfo ci) {
		if ((Object) this instanceof Monster) {
			this.targetSelector.add(2, new TargetRatOrBodyGoal((MobEntity) (Object) this));
		}
	}
}
