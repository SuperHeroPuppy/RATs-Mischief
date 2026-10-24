package ladysnake.ratsmischief.common.item;

import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class RatMasterMirrorItem extends Item {
	public RatMasterMirrorItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (!world.isClient()) {
			RatEntity rat = world.getClosestEntity(
				RatEntity.class,
				TargetPredicate.createAttackable().setPredicate(r -> ((RatEntity) r).isOwner(user) && ((RatEntity) r).isSpy()),
				user,
				user.getX(),
				user.getY(),
				user.getZ(),
				new Box(user.getBlockPos()).expand(160) // 16 chunks
			);
			if (rat != null && user instanceof ServerPlayerEntity player && RatPossession.beginSpy(player, rat)) {
				player.getItemCooldownManager().set(this, 40);
			}
		}

		return TypedActionResult.success(user.getStackInHand(hand));
	}
}
