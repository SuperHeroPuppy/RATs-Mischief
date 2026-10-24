package ladysnake.ratsmischief.client.render.entity;

import com.google.common.collect.Sets;
import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.RatsMischiefUtils;
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

import java.util.Set;

public class PartyHatFeatureRenderer extends GeoRenderLayer<RatEntity> {
	public static final Set<RatEntity.Type> DISALLOWED_TYPES = Sets.immutableEnumSet(RatEntity.Type.RAT_KID, RatEntity.Type.BIGGIE_CHEESE, RatEntity.Type.REMY);
	public static final Identifier[] TEXTURES = new Identifier[RatEntity.PartyHat.values().length];

	static {
		for (RatEntity.PartyHat hat : RatEntity.PartyHat.values()) {
			TEXTURES[hat.ordinal()] = new Identifier(RatsMischief.MOD_ID, "textures/entity/birthday_hats/" + hat.toString().toLowerCase() + ".png");
		}
	}

	public PartyHatFeatureRenderer(GeoRenderer<RatEntity> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStackIn, RatEntity ratEntity, BakedGeoModel model, RenderLayer renderType, VertexConsumerProvider bufferIn, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
		if (RatsMischiefUtils.IS_BIRTHDAY && !DISALLOWED_TYPES.contains(ratEntity.getRatType())) {
			Identifier hatTexture = TEXTURES[ratEntity.getPartyHat().ordinal()];
			RenderLayer layer = RenderLayer.getEntityCutout(hatTexture);
			this.getRenderer().reRender(model, matrixStackIn, bufferIn, ratEntity, layer,
				bufferIn.getBuffer(layer), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		}
	}
}
