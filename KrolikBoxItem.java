package pl.ziomsigiemka.krolik.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

/** Królik w pudełku - PPM na ziemię i masz swojego królika. */
public class KrolikBoxItem extends OpisItem {

    public KrolikBoxItem(Properties props) {
        super(props, "PPM na ziemię = wypuść Gucia!\nGucio będzie twój i będzie za tobą chodził.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        PetRabbit r = ModRegistry.PET_RABBIT.get().create(sl);
        if (r == null) return InteractionResult.FAIL;
        r.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, ctx.getRotation() + 180.0F, 0.0F);
        r.finalizeSpawn(sl, sl.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null, null);
        r.setCustomName(Component.literal("Gucio"));
        if (player != null) {
            r.setOwnerId(player.getUUID());
            player.displayClientMessage(Component.literal("§aOto twój królik " + r.getName().getString() + "!"), true);
        }
        sl.addFreshEntity(r);
        if (player == null || !player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
