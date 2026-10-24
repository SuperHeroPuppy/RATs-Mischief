package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatInventoryScreen;
import ladysnake.ratsmischief.client.RatPossessionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$openRatInventory(CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!((Object) this instanceof RatInventoryScreen) && client.player != null && RatPossessionClient.isControllingRat()) {
			client.setScreen(new RatInventoryScreen(client.player));
			ci.cancel();
		}
	}
}
