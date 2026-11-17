package pl.ziomsigiemka.krolik.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.Shop;
import pl.ziomsigiemka.krolik.entity.PetRabbit;
import pl.ziomsigiemka.krolik.network.BuyPacket;
import pl.ziomsigiemka.krolik.network.ModNetwork;

import java.util.ArrayList;
import java.util.List;

/** Tablet "Gucio Shop" - ekran włączania, Gucio 3D, kategorie i karty z rzeczami. */
public class TabletScreen extends Screen {
    private static final int W = 360, H = 224;
    private static final int PANEL_X = 112, PANEL_W = 242;
    private static final int CARD_W = 118, CARD_H = 50, PER_PAGE = 6;
    private static final int BOOT_TICKS = 14;
    private static final String[] TABS = {"Wszystko", "Dom", "Zabawki", "Ubranka", "Gadżety"};
    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    private static int tab = 0;
    private static int page = 0;

    private int left, top;
    private int boot = BOOT_TICKS;
    private String flash = null;
    private int flashColor = 0xFF55FF55, flashTicks = 0;
    private PetRabbit preview;
    private final List<Integer> visible = new ArrayList<>();

    public TabletScreen() { super(Component.literal("Gucio Shop")); }

    // ------------------------------------------------------------ układ
    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        if (preview == null && minecraft != null && minecraft.level != null) {
            preview = ModRegistry.PET_RABBIT.get().create(minecraft.level);
        }
        if (boot > 0) return;

        List<Shop.Entry> all = Shop.entries();
        visible.clear();
        for (int i = 0; i < all.size(); i++) {
            if (tab == 0 || all.get(i).category() == tab) visible.add(i);
        }
        int pages = pages();
        page = Mth.clamp(page, 0, pages - 1);

        // zakładki
        int x = left + PANEL_X;
        for (int t = 0; t < TABS.length; t++) {
            final int tt = t;
            int w = font.width(TABS[t]) + 8;
            addRenderableWidget(Button.builder(Component.literal((t == tab ? "§e§n" : "") + TABS[t]), b -> {
                tab = tt;
                page = 0;
                click();
                rebuildWidgets();
            }).bounds(x, top + 20, w, 16).build());
            x += w + 2;
        }

        // przyciski "Kup"
        for (int k = 0; k < PER_PAGE; k++) {
            int idx = page * PER_PAGE + k;
            if (idx >= visible.size()) break;
            final int entryIndex = visible.get(idx);
            int cx = cardX(k), cy = cardY(k);
            addRenderableWidget(Button.builder(Component.literal("§lKup"), b -> buy(entryIndex))
                    .bounds(cx + CARD_W - 42, cy + CARD_H - 17, 38, 14).build());
        }

        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                if (page > 0) { page--; click(); rebuildWidgets(); }
            }).bounds(left + PANEL_X, top + H - 19, 16, 14).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if (page < pages() - 1) { page++; click(); rebuildWidgets(); }
            }).bounds(left + PANEL_X + PANEL_W - 16, top + H - 19, 16, 14).build());
        }
    }

    private int pages() { return Math.max(1, (visible.size() + PER_PAGE - 1) / PER_PAGE); }
    private int cardX(int k) { return left + PANEL_X + (k % 2) * (CARD_W + 6); }
    private int cardY(int k) { return top + 40 + (k / 2) * (CARD_H + 4); }

    private int emeralds() {
        return minecraft == null || minecraft.player == null ? 0 : minecraft.player.getInventory().countItem(Items.EMERALD);
    }

    private boolean creative() {
        return minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
    }

    private void click() {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
    }

    private void buy(int index) {
        Shop.Entry e = Shop.entries().get(index);
        if (!creative() && emeralds() < e.price()) {
            showFlash("Za mało szmaragdów! " + e.name() + " kosztuje " + e.price(), 0xFFFF5555);
            if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new BuyPacket(index));
        showFlash("Zamówiono: " + e.name() + "! Paczka już leci", 0xFF55FF55);
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
    }

    private void showFlash(String text, int color) {
        flash = text;
        flashColor = color;
        flashTicks = 50;
    }

    @Override
    public void tick() {
        if (boot > 0) {
            boot--;
            if (boot == 0) {
                rebuildWidgets();
                if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.6F));
            }
        }
        if (flashTicks > 0) flashTicks--;
        if (preview != null) preview.tickCount++;
    }

    // ------------------------------------------------------------ rysowanie
    private static void round(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        g.fill(x1 + 1, y1, x2 - 1, y1 + 1, c);
        g.fill(x1, y1 + 1, x2, y2 - 1, c);
        g.fill(x1 + 1, y2 - 1, x2 - 1, y2, c);
    }

    private void smallItem(GuiGraphics g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0F);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // obudowa tabletu
        round(g, left - 11, top - 11, left + W + 11, top + H + 11, 0xFF07070A);
        round(g, left - 9, top - 9, left + W + 9, top + H + 9, 0xFF26262F);
        g.fill(left + W / 2 - 3, top - 6, left + W / 2 + 3, top - 4, 0xFF3C3C4A); // kamerka
        g.fill(left - 7, top + H / 2 - 8, left - 5, top + H / 2 + 8, 0xFF3C3C4A);  // przycisk
        // ekran
        g.fillGradient(left, top, left + W, top + H, 0xFF2B1A4E, 0xFF0F2742);

        if (boot > 0) {
            renderBoot(g, partialTick);
            return;
        }

        renderStatusBar(g);
        renderGucioPanel(g, mouseX, mouseY);
        int hovered = renderCards(g, mouseX, mouseY);

        // dolny pasek
        String info = "Dostawa natychmiast: paczka 5 bloków od ciebie";
        if (pages() > 1) info = "Strona " + (page + 1) + "/" + pages() + "   " + info;
        g.drawCenteredString(font, info, left + PANEL_X + PANEL_W / 2, top + H - 16, 0xFF9FB3D9);

        super.render(g, mouseX, mouseY, partialTick);

        // komunikat po zakupie
        if (flashTicks > 0 && flash != null) {
            int fw = font.width(flash) + 16;
            int fx = left + W / 2 - fw / 2, fy = top + H - 44;
            round(g, fx, fy, fx + fw, fy + 18, 0xEE101018);
            g.fill(fx, fy + 17, fx + fw, fy + 18, flashColor);
            g.drawCenteredString(font, flash, left + W / 2, fy + 5, flashColor);
        }

        // opis rzeczy pod myszką
        if (hovered >= 0) {
            Shop.Entry e = Shop.entries().get(hovered);
            g.renderTooltip(font, font.split(Component.literal("§e" + e.name() + "\n§7" + e.desc()), 170), mouseX, mouseY);
        }
    }

    private void renderBoot(GuiGraphics g, float partialTick) {
        float progress = 1.0F - (boot - partialTick) / BOOT_TICKS;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        int cx = left + W / 2;

        if (preview != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, cx, top + 120, 50, 0.0F, -20.0F, preview);
        }
        g.pose().pushPose();
        g.pose().translate(cx, top + 136, 0);
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.drawCenteredString(font, "§6§lGucio §f§lShop", 0, 0, 0xFFFFFF);
        g.pose().popPose();

        int bw = 120, bx = cx - bw / 2, by = top + 162;
        round(g, bx - 1, by - 1, bx + bw + 1, by + 6, 0xFF000000);
        g.fillGradient(bx, by, bx + (int) (bw * progress), by + 5, 0xFFFFB347, 0xFFFF7A00);
        g.drawCenteredString(font, "Włączanie tabletu...", cx, by + 12, 0xFF9FB3D9);
    }

    private void renderStatusBar(GuiGraphics g) {
        g.fill(left, top, left + W, top + 14, 0x66000000);
        g.drawString(font, "§6§lGucio §f§lShop", left + 6, top + 3, 0xFFFFFF);

        if (minecraft != null && minecraft.level != null) {
            long t = (minecraft.level.getDayTime() + 6000L) % 24000L;
            String clock = String.format("%02d:%02d", t / 1000L, (t % 1000L) * 60L / 1000L);
            g.drawCenteredString(font, clock, left + W / 2, top + 3, 0xFFFFFF);
        }

        // bateria
        int bx = left + W - 22;
        g.fill(bx, top + 4, bx + 14, top + 10, 0xFFDDDDDD);
        g.fill(bx + 14, top + 5, bx + 15, top + 9, 0xFFDDDDDD);
        g.fill(bx + 1, top + 5, bx + 13, top + 9, 0xFF55DD55);

        // szmaragdy
        String money = creative() ? "§eGRATIS" : "§a" + emeralds();
        int mw = font.width(money);
        int mx = bx - 8 - mw;
        g.drawString(font, money, mx, top + 3, 0xFFFFFF);
        smallItem(g, EMERALD, mx - 11, top + 2, 0.6F);
    }

    private void renderGucioPanel(GuiGraphics g, int mouseX, int mouseY) {
        int px = left + 6, py = top + 20, pw = 100, ph = H - 26;
        round(g, px, py, px + pw, py + ph, 0x55000000);
        g.fillGradient(px + 2, py + 2, px + pw - 2, py + 110, 0x40FFB347, 0x00FFB347);

        int cx = px + pw / 2;
        if (preview != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, cx, py + 104, 42,
                    (float) cx - mouseX, (float) (py + 60) - mouseY, preview);
        }
        g.drawCenteredString(font, "§6§lGucio", cx, py + 112, 0xFFFFFF);
        g.drawCenteredString(font, "§7twój królik", cx, py + 123, 0xFFFFFF);
        g.drawCenteredString(font, "§c❤❤❤❤❤", cx, py + 136, 0xFFFFFF);
        g.drawString(font, "§7Kolor: §fłaciaty", px + 6, py + 152, 0xFFFFFF);
        g.drawString(font, "§7Lubi: §fmarchewki", px + 6, py + 164, 0xFFFFFF);
        g.drawString(font, "§7Ma: §fczuprynę!", px + 6, py + 176, 0xFFFFFF);
    }

    /** Rysuje karty i zwraca indeks rzeczy pod myszką (albo -1). */
    private int renderCards(GuiGraphics g, int mouseX, int mouseY) {
        int hovered = -1;
        List<Shop.Entry> all = Shop.entries();
        boolean free = creative();
        int money = emeralds();

        for (int k = 0; k < PER_PAGE; k++) {
            int idx = page * PER_PAGE + k;
            if (idx >= visible.size()) break;
            int entryIndex = visible.get(idx);
            Shop.Entry e = all.get(entryIndex);
            int cx = cardX(k), cy = cardY(k);
            boolean over = mouseX >= cx && mouseX < cx + CARD_W && mouseY >= cy && mouseY < cy + CARD_H;
            boolean overButton = mouseX >= cx + CARD_W - 42 && mouseY >= cy + CARD_H - 17;
            if (over && !overButton) hovered = entryIndex;

            round(g, cx, cy, cx + CARD_W, cy + CARD_H, over ? 0xFF453C78 : 0xFF2E2954);
            g.fill(cx + 1, cy + 1, cx + CARD_W - 1, cy + 2, 0x30FFFFFF);

            // ikonka
            round(g, cx + 4, cy + 7, cx + 40, cy + 43, 0x50000000);
            smallItem(g, new ItemStack(e.item().get()), cx + 6, cy + 9, 2.0F);

            // nazwa
            String name = font.plainSubstrByWidth(e.name(), CARD_W - 48);
            g.drawString(font, name, cx + 44, cy + 6, 0xFFFFFF);
            if (e.count() > 1) g.drawString(font, "§7x" + e.count(), cx + 44, cy + 16, 0xFFFFFF);

            // cena
            boolean canPay = free || money >= e.price();
            smallItem(g, EMERALD, cx + 44, cy + CARD_H - 16, 0.6F);
            String price = free ? "§eGRATIS" : (canPay ? "§a" : "§c") + e.price();
            g.drawString(font, price, cx + 56, cy + CARD_H - 14, 0xFFFFFF);
        }
        return hovered;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
