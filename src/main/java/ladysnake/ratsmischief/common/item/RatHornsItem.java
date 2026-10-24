package ladysnake.ratsmischief.common.item;

import ladysnake.ratsmischief.common.cca.RatHornsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class RatHornsItem extends Item {
	public RatHornsItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient()) {
			RatHornsComponent horns = RatHornsComponent.KEY.get(user);
			horns.setHorns(!horns.hasHorns());
		}
		return TypedActionResult.success(stack, world.isClient());
	}
}
