package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
	@Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$keepBodySelection(double scrollAmount, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && (Object) this == client.player.getInventory()
			&& RatPossessionClient.isControllingRat()) ci.cancel();
	}
}
