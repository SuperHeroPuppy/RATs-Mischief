package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.common.item.RatMasterOcarinaItem;
import ladysnake.ratsmischief.client.RatPossessionClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	private void ratsmischief$lockBodyHotbar(CallbackInfo ci) {
		if (!RatPossessionClient.isControllingRat()) return;
		MinecraftClient client = (MinecraftClient) (Object) this;
		if (client.player.isSpectator()) return;
		for (KeyBinding binding : client.options.hotbarKeys) {
			while (binding.wasPressed()) { /* the rat has no body hotbar */ }
		}
		while (client.options.swapHandsKey.wasPressed()) { /* keep body equipment in place */ }
	}
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$attackActions(CallbackInfoReturnable<Boolean> cir) {
		RatPossessionClient.requestSlap();
		MinecraftClient client = (MinecraftClient) (Object) this;
		if (!RatPossessionClient.isControllingRat() && client.player != null
			&& client.player.getMainHandStack().getItem() instanceof RatMasterOcarinaItem) {
			ClientPlayNetworking.send(RatMasterOcarinaItem.CYCLE_PACKET, PacketByteBufs.empty());
			cir.setReturnValue(true);
		}
	}

}
