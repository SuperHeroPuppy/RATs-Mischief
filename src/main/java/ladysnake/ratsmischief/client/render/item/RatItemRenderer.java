package ladysnake.ratsmischief.client.render.item;

import ladysnake.ratsmischief.client.model.RatItemModel;
import ladysnake.ratsmischief.client.render.entity.EnderEyeFeatureRenderer;
import ladysnake.ratsmischief.client.render.entity.PartyHatFeatureRenderer;
import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.RatsMischiefUtils;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.item.RatItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.nbt.NbtCompound;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RatItemRenderer extends GeoItemRenderer<RatItem> {
	public RatItemRenderer() {
		super(new RatItemModel());
		this.addRenderLayer(new GeoRenderLayer<>(this) {
			@Override
			public void render(MatrixStack matrices, RatItem animatable, BakedGeoModel model, RenderLayer renderType,
				VertexConsumerProvider buffers, VertexConsumer buffer, float tickDelta, int light, int overlay) {
				ItemStack stack = RatItemRenderer.this.getCurrentItemStack();
				if (stack == null || stack.isEmpty()) {
					return;
				}
				NbtCompound ratTag = RatItem.getRatTag(stack);
				if (ratTag == null) {
					return;
				}
				if (ratTag.getBoolean("Spy")) {
					renderOverlay(EnderEyeFeatureRenderer.TEXTURE, matrices, animatable, model, buffers, tickDelta, light);
				}
				if (RatsMischiefUtils.IS_BIRTHDAY && !PartyHatFeatureRenderer.DISALLOWED_TYPES.contains(RatItem.getRatType(stack))) {
					try {
						RatEntity.PartyHat hat = RatEntity.PartyHat.valueOf(ratTag.getString("PartyHat"));
						renderOverlay(PartyHatFeatureRenderer.TEXTURES[hat.ordinal()], matrices, animatable, model, buffers, tickDelta, light);
					} catch (IllegalArgumentException ignored) {
						// Old rat items may not have a party hat stored.
					}
				}
			}

			private void renderOverlay(Identifier texture, MatrixStack matrices, RatItem animatable, BakedGeoModel model,
				VertexConsumerProvider buffers, float tickDelta, int light) {
				RenderLayer layer = RenderLayer.getEntityCutout(texture);
				getRenderer().reRender(model, matrices, buffers, animatable, layer, buffers.getBuffer(layer),
					tickDelta, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
			}
		});
	}

	@Override
	public Identifier getTextureLocation(RatItem animatable) {
		ItemStack stack = this.getCurrentItemStack();
		if (stack == null || stack.isEmpty()) {
			return super.getTextureLocation(animatable);
		}
		RatEntity.Type type = RatItem.getRatType(stack);
		DyeColor color = RatItem.getRatColor(stack);
		return RatsMischiefUtils.getRatTexture(type, color);
	}

	@Override
	public void preRender(MatrixStack matrices, RatItem animatable, BakedGeoModel model,
		VertexConsumerProvider buffers, VertexConsumer buffer, boolean isReRender, float tickDelta,
		int light, int overlay, float red, float green, float blue, float alpha) {
		super.preRender(matrices, animatable, model, buffers, buffer, isReRender, tickDelta,
			light, overlay, red, green, blue, alpha);
		ItemStack stack = this.getCurrentItemStack();
		NbtCompound ratTag = stack == null || stack.isEmpty() ? null : RatItem.getRatTag(stack);
		if (ratTag != null && ratTag.getInt("Age") < 0) {
			matrices.translate(0, 0, -0.1f);
			matrices.scale(0.5f, 0.5f, 0.5f);
		}
	}
}
