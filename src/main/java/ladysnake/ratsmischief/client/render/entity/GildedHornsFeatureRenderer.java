package ladysnake.ratsmischief.client.render.entity;

import ladysnake.ratsmischief.common.item.RatHornsItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class GildedHornsFeatureRenderer<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead>
	extends FeatureRenderer<T, M> {
	public GildedHornsFeatureRenderer(FeatureRendererContext<T, M> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, T entity,
		float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		ItemStack horns = RatHornsItem.getWornHorns(entity);
		if (horns.isEmpty()) {
			return;
		}
		matrices.push();
		this.getContextModel().getHead().rotate(matrices);
		matrices.translate(0.0f, -0.25f, 0.0f);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
		matrices.scale(0.625f, -0.625f, -0.625f);
		MinecraftClient client = MinecraftClient.getInstance();
		Identifier id = Registries.ITEM.getId(horns.getItem());
		BakedModel model = client.getBakedModelManager().getModel(
			new ModelIdentifier(new Identifier(id.getNamespace(), id.getPath() + "_worn"), "inventory"));
		client.getItemRenderer().renderItem(horns, ModelTransformationMode.HEAD, false,
			matrices, consumers, light, OverlayTexture.DEFAULT_UV, model);
		matrices.pop();
	}
}
