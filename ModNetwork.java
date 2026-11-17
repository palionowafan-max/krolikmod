package pl.ziomsigiemka.krolik.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import pl.ziomsigiemka.krolik.KrolikMod;

public class ModNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KrolikMod.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(BuyPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BuyPacket::encode)
                .decoder(BuyPacket::new)
                .consumerMainThread(BuyPacket::handle)
                .add();
    }
}
