package pl.ziomsigiemka.krolik.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Kawałek drewna do gryzienia. */
public class DrewnoBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 4, 14, 6, 12);

    public DrewnoBlock(Properties props) { super(props); }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
}
