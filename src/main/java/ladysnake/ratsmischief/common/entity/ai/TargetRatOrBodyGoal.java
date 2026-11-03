package ladysnake.ratsmischief.common.entity.ai;

import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;

/** Makes vanilla and modded hostile mobs notice rats and the abandoned possession body. */
public final class TargetRatOrBodyGoal extends Goal {
	private final MobEntity mob;
	private LivingEntity candidate;

	public TargetRatOrBodyGoal(MobEntity mob) {
		this.mob = mob;
		this.setControls(EnumSet.of(Control.TARGET));
	}

	@Override
	public boolean canStart() {
		if (this.mob.getWorld().isClient) return false;
		LivingEntity current = this.mob.getTarget();
		if (current instanceof PlayerEntity player && RatPossession.getControlledRat(player) != null) {
			this.mob.setTarget(null);
			current = null;
		}
		if (current != null && current.isAlive()) return false;
		double range = this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
		Box search = this.mob.getBoundingBox().expand(range);
		double nearest = range * range;
		this.candidate = null;
		for (RatEntity rat : this.mob.getWorld().getEntitiesByClass(RatEntity.class, search,
			entity -> entity.isAlive() && RatPossession.canHostilesTargetRat(entity))) {
			double distance = this.mob.squaredDistanceTo(rat);
			if (distance < nearest && this.mob.canSee(rat)) {
				nearest = distance;
				this.candidate = rat;
			}
		}
		for (ArmorStandEntity body : this.mob.getWorld().getEntitiesByClass(ArmorStandEntity.class, search,
			RatPossession::canHostilesTargetBody)) {
			double distance = this.mob.squaredDistanceTo(body);
			if (distance < nearest && this.mob.canSee(body)) {
				nearest = distance;
				this.candidate = body;
			}
		}
		return this.candidate != null;
	}

	@Override
	public boolean shouldContinue() {
		if (this.candidate instanceof RatEntity rat && !RatPossession.canHostilesTargetRat(rat)) return false;
		if (this.candidate instanceof ArmorStandEntity body && !RatPossession.canHostilesTargetBody(body)) return false;
		return this.candidate != null && this.candidate.isAlive()
			&& this.mob.getTarget() == this.candidate
			&& this.mob.squaredDistanceTo(this.candidate) <
				this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)
					* this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
	}

	@Override
	public void start() {
		this.mob.setTarget(this.candidate);
	}

	@Override
	public void stop() {
		if (this.mob.getTarget() == this.candidate) this.mob.setTarget(null);
		this.candidate = null;
	}
}
