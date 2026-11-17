package pl.ziomsigiemka.krolik;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pl.ziomsigiemka.krolik.block.PaczkaBlockEntity;

import java.util.List;
import java.util.function.Supplier;

/** Sklep w tablecie + dostawa paczką. Ceny są w szmaragdach (w trybie kreatywnym za darmo). */
public class Shop {

    public record Entry(String name, Supplier<? extends Item> item, int count, int price, String desc) {}

    private static List<Entry> entries;

    public static List<Entry> entries() {
        if (entries == null) {
            entries = List.of(
                    new Entry("Królik", ModRegistry.KROLIK, 1, 5, "Twój własny królik! PPM na ziemię, żeby go wypuścić."),
                    new Entry("Klatka", ModRegistry.KLATKA_ITEM, 1, 3, "Z poidełkiem i jedzeniem. Królik sam do niej chodzi."),
                    new Entry("Drewno do gryzienia", ModRegistry.DREWNO_ITEM, 1, 1, "Królik gryzie 5 sekund, potem 10 minut przerwy."),
                    new Entry("Przysmak", ModRegistry.PRZYSMAK, 4, 1, "Trzymasz w ręce - królik przybiega."),
                    new Entry("Piłka", ModRegistry.PILKA, 1, 2, "Królik będzie ją odbijał."),
                    new Entry("Okulary do wody", ModRegistry.OKULARY, 1, 3, "Królik nie topi się i pływa na górze."),
                    new Entry("Buty", ModRegistry.BUTY, 1, 3, "Powolne opadanie, zero obrażeń od upadku."),
                    new Entry("Sterownik", ModRegistry.STEROWNIK, 1, 8, "Wchodzisz w ciało królika i nim sterujesz!"),
                    new Entry("Ochrona", ModRegistry.OCHRONA, 1, 6, "Przeciwnicy obok królika są ogłuszeni i tracą życie."),
                    new Entry("Dźwiękoinator", ModRegistry.DZWIEKOINATOR, 1, 2, "Królik gada na czacie."),
                    new Entry("Deskorolka", ModRegistry.DESKOROLKA, 1, 4, "Królik na niej jeździ i jest szybszy."),
                    new Entry("Tunel", ModRegistry.TUNEL, 1, 3, "Szklany tunel od bloku do bloku - kanał dla królika."),
                    new Entry("Zły zapach", ModRegistry.ZLY_ZAPACH, 3, 2, "Uderz kogoś - królik go gryzie!"),
                    new Entry("Miłośnik", ModRegistry.MILOSNIK, 1, 10, "Komendy: /obrot /skok /idz /gryzon /miksturowiec")
            );
        }
        return entries;
    }

    public static void buy(ServerPlayer p, int index) {
        List<Entry> list = entries();
        if (index < 0 || index >= list.size()) return;
        Entry e = list.get(index);

        if (!p.getAbilities().instabuild) {
            Inventory inv = p.getInventory();
            int have = inv.countItem(Items.EMERALD);
            if (have < e.price()) {
                p.displayClientMessage(Component.literal("§cBrakuje ci szmaragdów! " + e.name() + " kosztuje " + e.price() + ", a masz " + have + "."), false);
                return;
            }
            int left = e.price();
            for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Items.EMERALD)) {
                    int take = Math.min(left, s.getCount());
                    s.shrink(take);
                    left -= take;
                }
            }
        }
        deliver(p, new ItemStack(e.item().get(), e.count()));
    }

    /** Dostawa: paczka ląduje ok. 5 bloków przed graczem. */
    public static void deliver(ServerPlayer p, ItemStack stack) {
        ServerLevel level = p.serverLevel();
        Vec3 look = p.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        if (flat.lengthSqr() < 1.0E-4D) flat = new Vec3(0.0D, 0.0D, 1.0D);
        flat = flat.normalize();

        int[][] offsets = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        BlockPos spot = null;
        for (int[] o : offsets) {
            BlockPos base = BlockPos.containing(p.getX() + flat.x * 5.0D + o[0], p.getY(), p.getZ() + flat.z * 5.0D + o[1]);
            spot = findGround(level, base);
            if (spot != null) break;
        }

        if (spot == null) {
            if (!p.getInventory().add(stack)) p.drop(stack, false);
            p.displayClientMessage(Component.literal("§a[Dostawa] Nie było miejsca na paczkę - wrzuciłem ją do twojego ekwipunku."), false);
            return;
        }

        level.setBlock(spot, ModRegistry.PACZKA.get().defaultBlockState(), 3);
        if (level.getBlockEntity(spot) instanceof PaczkaBlockEntity be) be.setContent(stack);
        level.sendParticles(ParticleTypes.CLOUD, spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D, 20, 0.4D, 0.4D, 0.4D, 0.02D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spot.getX() + 0.5D, spot.getY() + 0.8D, spot.getZ() + 0.5D, 10, 0.4D, 0.3D, 0.4D, 0.0D);
        level.playSound(null, spot, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6F, 1.6F);
        p.displayClientMessage(Component.literal("§a[Dostawa] Paczka już leży przed tobą! Kliknij ją PPM, żeby otworzyć."), false);
    }

    private static BlockPos findGround(ServerLevel level, BlockPos base) {
        for (int dy = 3; dy >= -4; dy--) {
            BlockPos c = base.above(dy);
            BlockState s = level.getBlockState(c);
            BlockPos below = c.below();
            if (s.canBeReplaced() && s.getFluidState().isEmpty()
                    && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                return c;
            }
        }
        return null;
    }
}
