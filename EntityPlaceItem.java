package pl.ziomsigiemka.krolik.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Item, który stawia byt (piłkę, deskorolkę) na ziemi. */
public class EntityPlaceItem extends OpisItem {
    private final Supplier<? extends EntityType<?>> type;

    public EntityPlaceItem(Properties props, Supplier<? extends EntityType<?>> type, String desc) {
        super(props, desc);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!level.isClientSide) {
            Entity e = type.get().create(level);
            if (e != null) {
                BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
                e.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, ctx.getRotation(), 0.0F);
                level.addFreshEntity(e);
                if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
