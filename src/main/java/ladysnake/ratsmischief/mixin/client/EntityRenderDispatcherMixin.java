package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$hideFirstPersonRat(Entity entity, Frustum frustum,
		double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (entity == RatPossessionClient.getControlledRat()
			&& MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
			cir.setReturnValue(false);
		}
	}
}
