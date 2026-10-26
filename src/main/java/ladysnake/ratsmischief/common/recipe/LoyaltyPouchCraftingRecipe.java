package ladysnake.ratsmischief.common.recipe;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.init.ModItems;
import ladysnake.ratsmischief.common.item.RatPouchItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class LoyaltyPouchCraftingRecipe extends SpecialCraftingRecipe {
	public LoyaltyPouchCraftingRecipe(Identifier id, CraftingRecipeCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(RecipeInputInventory inventory, World world) {
		ItemStack pouch = ItemStack.EMPTY;
		boolean hasLoyalty = false;
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (stack.isEmpty()) continue;
			if (stack.isOf(ModItems.LOYALTY_OF_THE_MISCHIEF) && !hasLoyalty) {
				hasLoyalty = true;
			} else if (stack.getItem() instanceof RatPouchItem && pouch.isEmpty()) {
				pouch = stack;
			} else {
				return false;
			}
		}
		return hasLoyalty && RatPouchItem.isFullUnbound(pouch);
	}

	@Override
	public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (stack.getItem() instanceof RatPouchItem) return RatPouchItem.bindFullPouch(stack);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RatsMischief.LOYALTY_POUCH_RECIPE;
	}
}
