package ladysnake.ratsmischief.mixin;

import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.VibrationListener.class)
public abstract class VibrationListenerMixin {
	@Shadow @Final private Vibrations receiver;

	@Inject(method = "listen", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$ignoreRats(ServerWorld world, GameEvent event, GameEvent.Emitter emitter,
		Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
		if (emitter.sourceEntity() instanceof RatEntity &&
			(this.receiver instanceof SculkSensorBlockEntity || this.receiver instanceof SculkShriekerBlockEntity ||
				this.receiver instanceof WardenEntity)) {
			cir.setReturnValue(false);
		}
	}
}
