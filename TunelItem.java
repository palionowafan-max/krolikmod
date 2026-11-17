package pl.ziomsigiemka.krolik.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.block.TunelBlock;

/** Kliknij blok początkowy, potem końcowy - powstaje szklany tunel od bloku do bloku. */
public class TunelItem extends OpisItem {

    public TunelItem(Properties props) {
        super(props, "1. PPM na blok = początek tunelu\n2. PPM na drugi blok = koniec tunelu\nTunel musi być w prostej linii (max 64 bloki).\nShift+PPM = anuluj. Królik chodzi tunelem jak kanałem!");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("Start");
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockPos clicked = ctx.getClickedPos();
        BlockPos point = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(ctx.getClickedFace());

        if (player != null && player.isShiftKeyDown()) {
            stack.removeTagKey("Start");
            msg(player, "§eAnulowano tunel.");
            return InteractionResult.CONSUME;
        }

        if (!stack.hasTag() || !stack.getTag().contains("Start")) {
            stack.getOrCreateTag().putLong("Start", point.asLong());
            msg(player, "§bPoczątek tunelu ustawiony! Teraz kliknij blok, gdzie ma być koniec.");
            return InteractionResult.CONSUME;
        }

        BlockPos start = BlockPos.of(stack.getTag().getLong("Start"));
        stack.removeTagKey("Start");

        Direction.Axis axis;
        if (start.equals(point)) axis = ctx.getHorizontalDirection().getAxis();
        else if (start.getY() != point.getY()) { msg(player, "§cPoczątek i koniec muszą być na tej samej wysokości!"); return InteractionResult.CONSUME; }
        else if (start.getX() == point.getX()) axis = Direction.Axis.Z;
        else if (start.getZ() == point.getZ()) axis = Direction.Axis.X;
        else { msg(player, "§cTunel musi być w prostej linii!"); return InteractionResult.CONSUME; }

        if (start.distManhattan(point) > 64) { msg(player, "§cTunel może mieć maksymalnie 64 bloki."); return InteractionResult.CONSUME; }

        BlockState tunel = ModRegistry.TUNEL_BLOK.get().defaultBlockState().setValue(TunelBlock.AXIS, axis);
        int placed = 0;
        for (BlockPos p : BlockPos.betweenClosed(start, point)) {
            if (level.getBlockState(p).canBeReplaced()) {
                level.setBlock(p, tunel, 3);
                placed++;
            }
        }
        if (placed > 0 && (player == null || !player.getAbilities().instabuild)) stack.shrink(1);
        msg(player, "§aZbudowano tunel: " + placed + " bloków!");
        return InteractionResult.CONSUME;
    }

    private static void msg(Player p, String s) {
        if (p != null) p.displayClientMessage(Component.literal(s), true);
    }
}
