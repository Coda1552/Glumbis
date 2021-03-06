package coda.glumbis.client.renderer;

import coda.glumbis.client.model.GlumpModel;
import coda.glumbis.common.entities.GlumpEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class GlumpRenderer extends GeoEntityRenderer<GlumpEntity> {

    public GlumpRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GlumpModel());
        this.shadowRadius = 0.4F;
    }

}