package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Let the same keys drive the rat while keeping the player's body still. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
	@Inject(method = "tick", at = @At("TAIL"))
	private void ratsmischief$controlRat(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
		if (!RatPossessionClient.isControllingRat()) return;
		this.movementForward = 0;
		this.movementSideways = 0;
		this.pressingForward = false;
		this.pressingBack = false;
		this.pressingLeft = false;
		this.pressingRight = false;
		this.jumping = false;
		this.sneaking = false;
	}
}
