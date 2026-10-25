package ladysnake.ratsmischief.mixin;

import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PossessionPlayerInventoryMixin {
	@Shadow @Final public PlayerEntity player;
	@Shadow public abstract ItemStack getStack(int slot);
	@Shadow public abstract void setStack(int slot, ItemStack stack);

	@Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$limitRatPickupToOneSlot(ItemStack incoming, CallbackInfoReturnable<Boolean> cir) {
		if (RatPossession.getControlledRat(player) == null) return;
		if (incoming.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		ItemStack hand = this.getStack(0);
		if (hand.isEmpty()) {
			int count = Math.min(incoming.getCount(), incoming.getMaxCount());
			ItemStack picked = incoming.copy();
			picked.setCount(count);
			this.setStack(0, picked);
			incoming.decrement(count);
			this.player.currentScreenHandler.sendContentUpdates();
			cir.setReturnValue(true);
		} else if (ItemStack.canCombine(hand, incoming) && hand.getCount() < hand.getMaxCount()) {
			int count = Math.min(incoming.getCount(), hand.getMaxCount() - hand.getCount());
			hand.increment(count);
			incoming.decrement(count);
			this.player.getInventory().markDirty();
			this.player.currentScreenHandler.sendContentUpdates();
			cir.setReturnValue(count > 0);
		} else {
			cir.setReturnValue(false);
		}
	}
}
