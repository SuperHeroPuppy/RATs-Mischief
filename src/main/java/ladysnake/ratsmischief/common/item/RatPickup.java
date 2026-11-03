package ladysnake.ratsmischief.common.item;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/** Handles priority rat pickup requests before another mod can consume a normal entity interaction. */
public final class RatPickup {
	public static final Identifier PICKUP_PACKET = RatsMischief.id("pickup_rat");

	private RatPickup() {
	}

	public static void initialize() {
		ServerPlayNetworking.registerGlobalReceiver(PICKUP_PACKET, (server, player, handler, buffer, responseSender) -> {
			int entityId = buffer.readVarInt();
			Hand hand = buffer.readBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
			server.execute(() -> {
				if (!(player.getWorld().getEntityById(entityId) instanceof RatEntity rat)
					|| player.squaredDistanceTo(rat) > 36.0 || !player.canSee(rat)) return;
				ItemStack stack = player.getStackInHand(hand);
				if (stack.getItem() instanceof RatPouchItem) {
					RatPouchItem.captureRat(stack, player, rat);
				} else if (stack.isEmpty() && player.isSneaking() && rat.isOwner(player)) {
					rat.turnRatIntoItemAndGive(player);
				}
			});
		});
	}
}
