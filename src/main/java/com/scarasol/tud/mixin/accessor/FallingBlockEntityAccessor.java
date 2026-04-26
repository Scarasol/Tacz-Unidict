package com.scarasol.tud.mixin.accessor;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


/**
 * @author Scarasol
 */
@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {

    @Accessor("blockState")
    void tud$setBlockState(BlockState state);

    @Accessor("cancelDrop")
    boolean tud$getCancelDrop();
}
