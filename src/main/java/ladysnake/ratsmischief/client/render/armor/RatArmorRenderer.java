package ladysnake.ratsmischief.client.render.armor;

import ladysnake.ratsmischief.client.RatsMischiefClient;
import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.item.RatMasterArmorItem;
import ladysnake.ratsmischief.common.item.RatMasterCloakItem;
import ladysnake.ratsmischief.common.item.RatMasterHoodItem;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class RatArmorRenderer implements ArmorRenderer {
	private PlayerEntityModel<LivingEntity> inner;
	private PlayerEntityModel<LivingEntity> outer;
	private PlayerEntityModel<LivingEntity> slimOuter;

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack, LivingEntity entity,
		EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
		if (!(stack.getItem() instanceof RatMasterArmorItem armor) || armor.getSlotType() != slot) {
			return;
		}
		boolean slim = entity instanceof AbstractClientPlayerEntity player && "slim".equals(player.getModel());
		PlayerEntityModel<LivingEntity> model = this.getModel(slot, slim);
		contextModel.copyBipedStateTo(model);
		model.setVisible(false);
		switch (slot) {
			case HEAD -> {
				model.head.visible = contextModel.head.visible;
				model.hat.visible = contextModel.head.visible;
			}
			case CHEST -> {
				model.body.visible = contextModel.body.visible;
				model.jacket.visible = contextModel.body.visible;
				model.leftArm.visible = contextModel.leftArm.visible;
				model.rightArm.visible = contextModel.rightArm.visible;
				model.leftSleeve.visible = contextModel.leftArm.visible && !RatMasterCloakItem.isStripped(stack);
				model.rightSleeve.visible = contextModel.rightArm.visible && !RatMasterCloakItem.isStripped(stack);
			}
			case LEGS -> {
				model.body.visible = contextModel.body.visible;
				model.jacket.visible = contextModel.body.visible;
				model.leftLeg.visible = contextModel.leftLeg.visible;
				model.rightLeg.visible = contextModel.rightLeg.visible;
				model.leftPants.visible = contextModel.leftLeg.visible;
				model.rightPants.visible = contextModel.rightLeg.visible;
			}
			case FEET -> {
				model.leftLeg.visible = contextModel.leftLeg.visible;
				model.rightLeg.visible = contextModel.rightLeg.visible;
				model.leftPants.visible = contextModel.leftLeg.visible;
				model.rightPants.visible = contextModel.rightLeg.visible;
			}
		}
		model.jacket.copyTransform(model.body);
		model.leftSleeve.copyTransform(model.leftArm);
		model.rightSleeve.copyTransform(model.rightArm);
		model.leftPants.copyTransform(model.leftLeg);
		model.rightPants.copyTransform(model.rightLeg);

		String part = switch (slot) {
			case HEAD -> RatMasterHoodItem.isHidden(stack) ? "hood_down" : "hood";
			case LEGS -> "legs";
			default -> "chest";
		};
		String suffix = slot == EquipmentSlot.CHEST && slim ? "_slim" : "";
		Identifier texture = RatsMischief.id("textures/entity/armor/" + armor.getMaterial().getName() + "_" + part + suffix + ".png");
		ArmorRenderer.renderPart(matrices, consumers, light, stack, model, texture);
	}

	private PlayerEntityModel<LivingEntity> getModel(EquipmentSlot slot, boolean slim) {
		if (this.inner == null) {
			var loader = MinecraftClient.getInstance().getEntityModelLoader();
			this.inner = new PlayerEntityModel<>(loader.getModelPart(RatsMischiefClient.RAT_MASTER_ARMOR_INNER_LAYER), false);
			this.outer = new PlayerEntityModel<>(loader.getModelPart(RatsMischiefClient.RAT_MASTER_ARMOR_OUTER_LAYER), false);
			this.slimOuter = new PlayerEntityModel<>(loader.getModelPart(RatsMischiefClient.RAT_MASTER_ARMOR_OUTER_LAYER_SLIM), true);
		}
		return slot == EquipmentSlot.LEGS ? this.inner : slim ? this.slimOuter : this.outer;
	}
}
