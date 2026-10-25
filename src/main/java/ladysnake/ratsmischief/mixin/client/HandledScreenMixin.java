package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatInventoryScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
	@Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$ignoreBodySlots(Slot slot, double pointX, double pointY,
		CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof RatInventoryScreen) cir.setReturnValue(false);
	}

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$hideBodyInventory(DrawContext context, Slot slot, CallbackInfo ci) {
		if ((Object) this instanceof RatInventoryScreen) ci.cancel();
	}

	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
		at = @At("HEAD"), cancellable = true)
	private void ratsmischief$blockHiddenSlots(Slot slot, int slotId, int button, SlotActionType action,
		CallbackInfo ci) {
		if ((Object) this instanceof RatInventoryScreen) ci.cancel();
	}
}
