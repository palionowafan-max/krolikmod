package pl.ziomsigiemka.krolik.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.ziomsigiemka.krolik.Shop;
import pl.ziomsigiemka.krolik.network.BuyPacket;
import pl.ziomsigiemka.krolik.network.ModNetwork;

import java.util.List;

/** Ekran tabletu - sklep dla królika. */
public class TabletScreen extends Screen {
    private static final int W = 270, H = 220, PER_PAGE = 7;
    private int page = 0;
    private int left, top;

    public TabletScreen() { super(Component.literal("Tablet")); }

    private int pages() {
        return (Shop.entries().size() + PER_PAGE - 1) / PER_PAGE;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        List<Shop.Entry> list = Shop.entries();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
            final int idx = start + i;
            Shop.Entry e = list.get(idx);
            String label = e.name() + (e.count() > 1 ? " x" + e.count() : "") + "  -  " + e.price() + " szm.";
            addRenderableWidget(Button.builder(Component.literal(label), b -> ModNetwork.CHANNEL.sendToServer(new BuyPacket(idx)))
                    .bounds(left + 34, top + 34 + i * 22, W - 50, 20)
                    .tooltip(Tooltip.create(Component.literal(e.desc())))
                    .build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (page > 0) { page--; rebuildWidgets(); }
        }).bounds(left + 12, top + H - 28, 30, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (page < pages() - 1) { page++; rebuildWidgets(); }
        }).bounds(left + W - 42, top + H - 28, 30, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        // obudowa tabletu
        g.fill(left - 8, top - 8, left + W + 8, top + H + 8, 0xFF111111);
        g.fill(left - 6, top - 6, left + W + 6, top + H + 6, 0xFF2B2B2B);
        // ekran
        g.fill(left, top, left + W, top + H, 0xFF1E2C4F);
        g.fill(left, top, left + W, top + 28, 0xFF2F4A8A);

        g.drawCenteredString(font, "§lSklep dla Królika", width / 2, top + 6, 0xFFFFFF);
        String money;
        if (minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild) {
            money = "Tryb kreatywny: wszystko za darmo!";
        } else {
            int em = minecraft == null || minecraft.player == null ? 0 : minecraft.player.getInventory().countItem(Items.EMERALD);
            money = "Twoje szmaragdy: " + em;
        }
        g.drawCenteredString(font, money, width / 2, top + 17, 0x55FF55);

        // ikonki itemów obok przycisków
        List<Shop.Entry> list = Shop.entries();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
            g.renderItem(new ItemStack(list.get(start + i).item().get()), left + 12, top + 36 + i * 22);
        }

        g.drawCenteredString(font, "Strona " + (page + 1) + "/" + pages() + "  |  dostawa od razu, 5 bloków od ciebie", width / 2, top + H - 22, 0xBBBBBB);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
