package ladysnake.ratsmischief.common.init;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.item.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@SuppressWarnings("unused")
public interface ModItems {
	Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

	//	Item MOD_ITEM = createItem("mod_item", new ModItem(new FabricItemSettings()));
	Item RAT = createItem("rat", new RatItem(new FabricItemSettings().maxCount(1)));
	Item RAT_SPAWN_EGG = createItem("rat_spawn_egg", new SpawnEggItem(ModEntities.RAT, 0x2E1C1C, 0x241317, new FabricItemSettings()));

	Item LEATHER_RAT_POUCH = createItem("leather_rat_pouch", new RatPouchItem(new FabricItemSettings().maxCount(1), 5));
	Item TWISTED_RAT_POUCH = createItem("twisted_rat_pouch", new RatPouchItem(new FabricItemSettings().maxCount(1), 10));
	Item PURPUR_RAT_POUCH = createItem("purpur_rat_pouch", new RatPouchItem(new FabricItemSettings().maxCount(1), 15));
	Item RAT_MASTER_POUCH = createItem("rat_master_pouch", new RatPouchItem(new FabricItemSettings().maxCount(1), 20));

	Item CLOTHED_INGOT = createItem("clothed_ingot", new Item(new FabricItemSettings()));
	Item CONTRACTED_INGOT = createItem("contracted_ingot", new Item(new FabricItemSettings()));
	Item UNCLOTHED_INGOT = createItem("unclothed_ingot", new Item(new FabricItemSettings()));
	Item UNCLOTHED_CONTRACTED_INGOT = createItem("unclothed_contracted_ingot", new Item(new FabricItemSettings()));
	Item RAT_MASTER_CLOTH = createItem("rat_master_cloth", new Item(new FabricItemSettings()));
	Item RAT_BELLICIST_CLOTH = createItem("rat_bellicist_cloth", new Item(new FabricItemSettings()));
	Item LOYALTY_OF_THE_MISCHIEF = createItem("loyalty_of_the_mischief", new Item(new FabricItemSettings().maxCount(1)));
	Item RAT_MASTER_SMITHING_TEMPLATE = createItem("rat_master_smithing_template", new SmithingTemplateItem(
		Text.translatable("item.ratsmischief.rat_master_smithing_template.applies_to"),
		Text.translatable("item.ratsmischief.rat_master_smithing_template.ingredients"),
		Text.translatable("item.ratsmischief.rat_master_smithing_template.title"),
		Text.translatable("item.ratsmischief.rat_master_smithing_template.base_slot"),
		Text.translatable("item.ratsmischief.rat_master_smithing_template.additions_slot"),
		List.of(new Identifier("item/empty_armor_slot_helmet"), new Identifier("item/empty_armor_slot_chestplate"),
			new Identifier("item/empty_armor_slot_leggings"), new Identifier("item/empty_armor_slot_boots")),
		List.of(new Identifier("item/empty_slot_ingot"))));
	Item RAT_BELLICIST_SMITHING_TEMPLATE = createItem("rat_bellicist_smithing_template", new SmithingTemplateItem(
		Text.translatable("item.ratsmischief.rat_bellicist_smithing_template.applies_to"),
		Text.translatable("item.ratsmischief.rat_bellicist_smithing_template.ingredients"),
		Text.translatable("item.ratsmischief.rat_bellicist_smithing_template.title"),
		Text.translatable("item.ratsmischief.rat_bellicist_smithing_template.base_slot"),
		Text.translatable("item.ratsmischief.rat_bellicist_smithing_template.additions_slot"),
		List.of(new Identifier("item/empty_armor_slot_helmet"), new Identifier("item/empty_armor_slot_chestplate"),
			new Identifier("item/empty_armor_slot_leggings"), new Identifier("item/empty_armor_slot_boots")),
		List.of(new Identifier("item/empty_slot_ingot"))));
	Item RAT_MASTER_HOOD = createItem("rat_master_hood", new RatMasterHoodItem(RatMasterArmorItem.RatMasterArmorMaterial.INSTANCE, EquipmentSlot.HEAD, new FabricItemSettings()));
	Item RAT_MASTER_CLOAK = createItem("rat_master_cloak", new RatMasterCloakItem(RatMasterArmorItem.RatMasterArmorMaterial.INSTANCE, EquipmentSlot.CHEST, new FabricItemSettings()));
	Item RAT_MASTER_BREECHES = createItem("rat_master_breeches", new RatMasterArmorItem(RatMasterArmorItem.RatMasterArmorMaterial.INSTANCE, EquipmentSlot.LEGS, new FabricItemSettings()));
	Item RAT_MASTER_GREAVES = createItem("rat_master_greaves", new RatMasterArmorItem(RatMasterArmorItem.RatMasterArmorMaterial.INSTANCE, EquipmentSlot.FEET, new FabricItemSettings()));
	Item RAT_MASTER_MIRROR = createItem("rat_master_mirror", new RatMasterMirrorItem(new FabricItemSettings().maxCount(1)));
	Item RAT_MASTER_OCARINA = createItem("rat_master_ocarina", new RatMasterOcarinaItem(new FabricItemSettings().maxCount(1)));
	Item RAT_MASTER_MASK = createItem("rat_master_mask", new RatMasterMaskItem(new FabricItemSettings().maxCount(1)));
	Item RAT_BELLICIST_HOOD = createItem("rat_bellicist_hood", new RatMasterHoodItem(RatMasterArmorItem.RatBellicistArmorMaterial.INSTANCE, EquipmentSlot.HEAD, new FabricItemSettings()));
	Item RAT_BELLICIST_CLOAK = createItem("rat_bellicist_cloak", new RatMasterCloakItem(RatMasterArmorItem.RatBellicistArmorMaterial.INSTANCE, EquipmentSlot.CHEST, new FabricItemSettings()));
	Item RAT_BELLICIST_BREECHES = createItem("rat_bellicist_breeches", new RatMasterArmorItem(RatMasterArmorItem.RatBellicistArmorMaterial.INSTANCE, EquipmentSlot.LEGS, new FabricItemSettings()));
	Item RAT_BELLICIST_GREAVES = createItem("rat_bellicist_greaves", new RatMasterArmorItem(RatMasterArmorItem.RatBellicistArmorMaterial.INSTANCE, EquipmentSlot.FEET, new FabricItemSettings()));
	Item RAT_BELLICIST_HORNS = createItem("rat_bellicist_horns", new RatHornsItem(new FabricItemSettings().maxCount(1)));
	Item RAT_BELLICIST_CARNYX = createItem("rat_bellicist_carnyx", new Item(new FabricItemSettings().maxCount(1)));

	private static <T extends Item> T createItem(String name, T item) {
		ITEMS.put(item, new Identifier(RatsMischief.MOD_ID, name));
		return item;
	}

	static void initialize() {
		ITEMS.keySet().forEach(item -> {
			Registry.register(Registries.ITEM, ITEMS.get(item), item);
		});
		ModItemGroup.initialize();
	}
}
