package ladysnake.ratsmischief.common.init;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.item.RatPouchItem;
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
		ItemStack icon = ((RatPouchItem) ModItems.RAT_MASTER_POUCH).createFilledCreativeStack();
		ItemGroup group = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.ratsmischief"))
			.icon(() -> icon)
			.entries((context, entries) -> {
				entries.add(ModItems.RAT_SPAWN_EGG);
				addPouch(entries, (RatPouchItem) ModItems.LEATHER_RAT_POUCH);
				addPouch(entries, (RatPouchItem) ModItems.TWISTED_RAT_POUCH);
				addPouch(entries, (RatPouchItem) ModItems.PURPUR_RAT_POUCH);
				addPouch(entries, (RatPouchItem) ModItems.RAT_MASTER_POUCH);
				entries.add(ModItems.CLOTHED_INGOT);
				entries.add(ModItems.MISCHIEF_INGOT);
				entries.add(ModItems.LOYALTY_OF_THE_MISCHIEF);
				entries.add(ModItems.RAT_MASTER_SMITHING_TEMPLATE);
				entries.add(ModItems.RAT_MASTER_HOOD);
				entries.add(ModItems.RAT_MASTER_CLOAK);
				entries.add(ModItems.RAT_MASTER_BREECHES);
				entries.add(ModItems.RAT_MASTER_GREAVES);
				entries.add(ModItems.RAT_MASTER_MIRROR);
				entries.add(ModItems.RAT_MASTER_OCARINA);
				entries.add(ModItems.RAT_MASTER_MASK);
				entries.add(ModItems.RAT_BELLICIST_HOOD);
				entries.add(ModItems.RAT_BELLICIST_CLOAK);
				entries.add(ModItems.RAT_BELLICIST_BREECHES);
				entries.add(ModItems.RAT_BELLICIST_GREAVES);
				entries.add(ModItems.RAT_BELLICIST_HORNS);
				entries.add(ModItems.RAT_BELLICIST_CARNYX);
			})
			.build();
		Registry.register(Registries.ITEM_GROUP, RatsMischief.id("items"), group);
	}

	private static void addPouch(ItemGroup.Entries entries, RatPouchItem pouch) {
		entries.add(pouch);
		entries.add(pouch.createFilledCreativeStack());
	}
}
