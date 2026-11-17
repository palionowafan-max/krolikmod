package pl.ziomsigiemka.krolik.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openTablet() {
        Minecraft.getInstance().setScreen(new TabletScreen());
    }
}
