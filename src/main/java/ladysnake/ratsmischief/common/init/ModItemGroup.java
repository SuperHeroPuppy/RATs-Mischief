package ladysnake.ratsmischief.common.init;

import ladysnake.ratsmischief.common.RatsMischief;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
	private ModItemGroup() {
	}

	public static void initialize() {
		ItemGroup group = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.ratsmischief"))
			.icon(() -> new ItemStack(ModItems.RAT))
			.entries((context, entries) -> ModItems.ITEMS.keySet().forEach(entries::add))
			.build();
		Registry.register(Registries.ITEM_GROUP, RatsMischief.id("items"), group);
	}
}
