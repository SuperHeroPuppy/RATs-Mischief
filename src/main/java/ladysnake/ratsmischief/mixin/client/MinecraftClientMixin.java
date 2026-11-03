package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.common.item.RatMasterOcarinaItem;
import ladysnake.ratsmischief.common.item.RatPickup;
import ladysnake.ratsmischief.common.item.RatPouchItem;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.client.RatPossessionClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 2000)
public abstract class MinecraftClientMixin {
	@Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$priorityRatPickup(CallbackInfo ci) {
		MinecraftClient client = (MinecraftClient) (Object) this;
		if (RatPossessionClient.isControllingRat() || client.player == null
			|| !(client.crosshairTarget instanceof EntityHitResult hit)
			|| !(hit.getEntity() instanceof RatEntity rat)) return;
		Hand hand;
		if (client.player.getMainHandStack().getItem() instanceof RatPouchItem) hand = Hand.MAIN_HAND;
		else if (client.player.getOffHandStack().getItem() instanceof RatPouchItem) hand = Hand.OFF_HAND;
		else if (client.player.isSneaking() && client.player.getMainHandStack().isEmpty()
			&& rat.isOwner(client.player)) hand = Hand.MAIN_HAND;
		else return;
		var buffer = PacketByteBufs.create();
		buffer.writeVarInt(rat.getId());
		buffer.writeBoolean(hand == Hand.OFF_HAND);
		ClientPlayNetworking.send(RatPickup.PICKUP_PACKET, buffer);
		ci.cancel();
	}
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
		MinecraftClient client = (MinecraftClient) (Object) this;
		if (!RatPossessionClient.isControllingRat() && client.player != null
			&& client.player.getMainHandStack().getItem() instanceof RatMasterOcarinaItem) {
			ClientPlayNetworking.send(RatMasterOcarinaItem.CYCLE_PACKET, PacketByteBufs.empty());
			cir.setReturnValue(true);
		}
	}

}
