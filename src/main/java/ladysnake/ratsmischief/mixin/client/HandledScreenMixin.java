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

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$hideBodyInventory(DrawContext context, Slot slot, CallbackInfo ci) {
		if ((Object) this instanceof RatInventoryScreen && slot.id >= 5) ci.cancel();
	}

	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
		at = @At("HEAD"), cancellable = true)
	private void ratsmischief$blockHiddenSlots(Slot slot, int slotId, int button, SlotActionType action,
		CallbackInfo ci) {
		if ((Object) this instanceof RatInventoryScreen && slot != null && slot.id >= 5) ci.cancel();
	}
}
