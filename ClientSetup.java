package pl.ziomsigiemka.krolik.client;

import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.ziomsigiemka.krolik.KrolikMod;
import pl.ziomsigiemka.krolik.ModRegistry;

@Mod.EventBusSubscriber(modid = KrolikMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModRegistry.PET_RABBIT.get(), RabbitRenderer::new);
        event.registerEntityRenderer(ModRegistry.PILKA_ENTITY.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.2F, false));
        event.registerEntityRenderer(ModRegistry.DESKOROLKA_ENTITY.get(), FlatItemRenderer::new);
    }
}
