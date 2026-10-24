package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.common.item.RatMasterOcarinaItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$cycleOcarina(CallbackInfoReturnable<Boolean> cir) {
		MinecraftClient client = (MinecraftClient) (Object) this;
		if (client.player != null && client.player.getMainHandStack().getItem() instanceof RatMasterOcarinaItem) {
			ClientPlayNetworking.send(RatMasterOcarinaItem.CYCLE_PACKET, PacketByteBufs.empty());
			cir.setReturnValue(true);
		}
	}
}
