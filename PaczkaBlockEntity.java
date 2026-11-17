package pl.ziomsigiemka.krolik.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pl.ziomsigiemka.krolik.ModRegistry;

public class PaczkaBlockEntity extends BlockEntity {
    private ItemStack content = ItemStack.EMPTY;

    public PaczkaBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.PACZKA_BE.get(), pos, state);
    }

    public void setContent(ItemStack stack) {
        this.content = stack.copy();
        setChanged();
    }

    public ItemStack takeContent() {
        ItemStack s = content;
        content = ItemStack.EMPTY;
        setChanged();
        return s;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Item", content.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        content = ItemStack.of(tag.getCompound("Item"));
    }
}
