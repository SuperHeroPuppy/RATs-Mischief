package ladysnake.ratsmischief.client.render.entity;

import net.minecraft.client.render.VertexConsumer;
import ladysnake.ratsmischief.client.model.RatEntityModel;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RatEntityRenderer extends GeoEntityRenderer<RatEntity> {
	public RatEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new RatEntityModel());
		this.shadowRadius = 0.35f;
		this.addRenderLayer(new PartyHatFeatureRenderer(this));
		this.addRenderLayer(new EnderEyeFeatureRenderer(this));
	}

	@Override
	public Vec3d getPositionOffset(RatEntity rat, float tickDelta) {
		if (rat.isSneaking()) {
			return new Vec3d(0, 0.15, 0);
		}

		return super.getPositionOffset(rat, tickDelta);
	}

	@Override
	public void render(RatEntity ratEntity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
		if (ratEntity.isFlying() && ratEntity.age < 5) {
			return;
		}

		super.render(ratEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}

	@Override
	public void renderRecursively(MatrixStack stack, RatEntity ratEntity, GeoBone bone, RenderLayer renderType,
		VertexConsumerProvider vertexConsumerProvider, VertexConsumer bufferIn, boolean isReRender, float partialTicks,
		int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if (!isReRender && bone.getName().equals("bodybone")) {
			ItemStack itemStack = ratEntity.isSitting() || ratEntity.isSneaking()
				? ItemStack.EMPTY : ratEntity.getEquippedStack(EquipmentSlot.MAINHAND);
			stack.push();
			stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
			stack.translate(bone.getPosX(), bone.getPosZ(), bone.getPosY() - 0.05);
			stack.scale(0.7f, 0.7f, 0.7f);
			stack.multiply(new Quaternionf().rotationXYZ(bone.getRotX(), bone.getRotZ(), bone.getRotY()));

			if (!itemStack.isEmpty()) {
				MinecraftClient.getInstance().getItemRenderer().renderItem(
					ratEntity,
					itemStack,
					ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
					false,
					stack,
					vertexConsumerProvider,
					ratEntity.getWorld(),
					packedLightIn,
					packedOverlayIn,
					0
				);
			}

			stack.pop();

			// restore the render buffer - GeckoLib expects this state otherwise you'll have weird texture issues
			bufferIn = vertexConsumerProvider.getBuffer(renderType);
		}

		super.renderRecursively(stack, ratEntity, bone, renderType, vertexConsumerProvider, bufferIn,
			isReRender, partialTicks, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	protected int getBlockLight(RatEntity rat, BlockPos blockPos) {
		if (rat.getRatType() == RatEntity.Type.RAT_KID && rat.getRatColor() == DyeColor.PURPLE) {
			return 15;
		} else {
			return super.getBlockLight(rat, blockPos);
		}
	}

}
