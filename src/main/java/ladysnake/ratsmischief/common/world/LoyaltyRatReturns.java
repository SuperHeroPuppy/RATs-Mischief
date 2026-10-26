package ladysnake.ratsmischief.common.world;

import ladysnake.ratsmischief.common.item.RatPouchItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

/** Holds rats that died while their bound pouch was outside a player's inventory. */
public final class LoyaltyRatReturns extends PersistentState {
	private static final String STATE_ID = "ratsmischief_loyalty_returns";
	private final NbtList pending = new NbtList();

	public static LoyaltyRatReturns get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(
			LoyaltyRatReturns::readNbt, LoyaltyRatReturns::new, STATE_ID);
	}

	private static LoyaltyRatReturns readNbt(NbtCompound nbt) {
		LoyaltyRatReturns state = new LoyaltyRatReturns();
		state.pending.addAll(nbt.getList("Pending", NbtElement.COMPOUND_TYPE));
		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.put("Pending", this.pending.copy());
		return nbt;
	}

	public boolean returnRat(MinecraftServer server, NbtCompound ratNbt) {
		if (!RatPouchItem.isBoundRat(ratNbt)) return false;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			for (int slot = 0; slot < player.getInventory().size(); slot++) {
				ItemStack stack = player.getInventory().getStack(slot);
				if (RatPouchItem.tryStoreReturnedRat(stack, ratNbt)) {
					player.getInventory().markDirty();
					return true;
				}
			}
		}
		for (NbtElement element : this.pending) {
			NbtCompound queued = (NbtCompound) element;
			if (queued.getUuid("UUID").equals(ratNbt.getUuid("UUID"))
				&& queued.getUuid(RatPouchItem.LOYALTY_POUCH_ID).equals(ratNbt.getUuid(RatPouchItem.LOYALTY_POUCH_ID))) {
				return true;
			}
		}
		this.pending.add(ratNbt.copy());
		this.markDirty();
		return true;
	}

	public boolean restoreTo(ItemStack pouch) {
		boolean changed = false;
		for (int i = this.pending.size() - 1; i >= 0; i--) {
			if (RatPouchItem.tryStoreReturnedRat(pouch, (NbtCompound) this.pending.get(i))) {
				this.pending.remove(i);
				changed = true;
			}
		}
		if (changed) this.markDirty();
		return changed;
	}
}
