package pl.ziomsigiemka.krolik.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import pl.ziomsigiemka.krolik.KrolikMod;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

/** Rysuje Gucia - większego niż zwykły królik. */
public class GucioRenderer extends MobRenderer<PetRabbit, GucioModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KrolikMod.MODID, "textures/item/gucio.png");

    public GucioRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GucioModel(ctx.bakeLayer(GucioModel.LAYER)), 0.5F);
    }

    @Override
    protected void scale(PetRabbit entity, PoseStack pose, float partialTick) {
        pose.scale(1.4F, 1.4F, 1.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(PetRabbit entity) {
        return TEXTURE;
    }
}
