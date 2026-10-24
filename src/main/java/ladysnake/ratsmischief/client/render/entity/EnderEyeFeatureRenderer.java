package ladysnake.ratsmischief.client.render.entity;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EnderEyeFeatureRenderer extends GeoRenderLayer<RatEntity> {
	public static final Identifier TEXTURE = new Identifier(RatsMischief.MOD_ID, "textures/entity/ender_eye.png");

	public EnderEyeFeatureRenderer(GeoRenderer<RatEntity> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStackIn, RatEntity ratEntity, BakedGeoModel model, RenderLayer renderType, VertexConsumerProvider bufferIn, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
		if (ratEntity.isSpy() && !ratEntity.isInvisible()) {
			RenderLayer layer = RenderLayer.getEntityCutout(TEXTURE);
			this.getRenderer().reRender(model, matrixStackIn, bufferIn, ratEntity, layer,
				bufferIn.getBuffer(layer), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		}
	}
}
