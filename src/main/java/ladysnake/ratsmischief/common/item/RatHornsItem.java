package ladysnake.ratsmischief.common.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.Optional;

public final class RatHornsItem extends TrinketItem {
	public RatHornsItem(Settings settings) {
		super(settings);
	}

	public static ItemStack getWornHorns(LivingEntity entity) {
		Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
		if (component.isPresent()) {
			for (Pair<SlotReference, ItemStack> pair : component.get().getAllEquipped()) {
				if (pair.getRight().getItem() instanceof RatHornsItem) {
					return pair.getRight();
				}
			}
		}
		return ItemStack.EMPTY;
	}
}
