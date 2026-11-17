package pl.ziomsigiemka.krolik.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import pl.ziomsigiemka.krolik.Shop;

import java.util.function.Supplier;

/** Klient -> serwer: "kupuję rzecz nr X z tabletu". */
public class BuyPacket {
    private final int index;

    public BuyPacket(int index) { this.index = index; }

    public BuyPacket(FriendlyByteBuf buf) { this.index = buf.readVarInt(); }

    public void encode(FriendlyByteBuf buf) { buf.writeVarInt(index); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) Shop.buy(player, index);
    }
}
