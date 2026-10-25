package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossessionView;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements RatPossessionView {
	@Override
	public RatEntity ratsmischief$getControlledRat() {
		return RatPossessionClient.getControlledRat();
	}
	@Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$keepBodyItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
		if (RatPossessionClient.isControllingRat()) cir.setReturnValue(false);
	}
}
