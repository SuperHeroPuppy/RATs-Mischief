package ladysnake.ratsmischief.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ladysnake.ratsmischief.common.item.RatMasterMaskItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HeadFeatureRenderer.class)
public class HeadFeatureRendererMixin<T extends LivingEntity> {
	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
	private ItemStack amarite$hideHeadFeature(LivingEntity entity, EquipmentSlot slot, Operation<ItemStack> original) {
		if (RatMasterMaskItem.isWearingMask(entity)) {
			return RatMasterMaskItem.getWornMask(entity);
		}
		return original.call(entity, slot);
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void amarite$maskOffset(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci, ItemStack stack) {
		if (stack.getItem() instanceof RatMasterMaskItem) {
			matrixStack.translate(0, RatMasterMaskItem.getOffset(stack) * 0.1, 0);
		}
	}

	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	private void ratsmischief$renderWornMask(HeldItemRenderer renderer, LivingEntity entity, ItemStack stack,
		ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider consumers,
		int light, Operation<Void> original) {
		if (stack.getItem() instanceof RatMasterMaskItem) {
			MinecraftClient client = MinecraftClient.getInstance();
			Identifier id = Registries.ITEM.getId(stack.getItem());
			BakedModel model = client.getBakedModelManager().getModel(
				new ModelIdentifier(new Identifier(id.getNamespace(), id.getPath() + "_worn"), "inventory"));
			client.getItemRenderer().renderItem(stack, mode, leftHanded, matrices, consumers, light,
				net.minecraft.client.render.OverlayTexture.DEFAULT_UV, model);
		} else {
			original.call(renderer, entity, stack, mode, leftHanded, matrices, consumers, light);
		}
	}
}
