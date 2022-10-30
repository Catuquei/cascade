package cascade.mixin.mixins;

import cascade.Cascade;
import cascade.event.events.CollisionEvent;
import cascade.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Block.class)
public class MixinBlock {


    @Deprecated
    @Inject(method = "addCollisionBoxToList" + "(Lnet/minecraft/block/state/IBlockState;" + "Lnet/minecraft/world/World;" + "Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/util/math/AxisAlignedBB;" + "Ljava/util/List;" + "Lnet/minecraft/entity/Entity;" + "Z)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("DuplicatedCode")
    private void addCollisionBoxToListHook_Pre(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> cBoxes, Entity entity, boolean isActualState, CallbackInfo info) {
        if (!Cascade.moduleManager.isModuleEnabled("Jesus")) {
            return;
        }

        Block block = Block.class.cast(this);
        AxisAlignedBB bb = block.getCollisionBoundingBox(state, world, pos);
        CollisionEvent event = new CollisionEvent(pos, bb, entity, block);
        //Jesus.getInstance().onCollision(event);

        if (bb != event.getBB()) {
            bb = event.getBB();
        }

        if (bb != null && entityBox.intersects(bb)) {
            cBoxes.add(bb);
        }

        //addCollisionBoxToList(pos, entityBox, cBoxes, bb);
        info.cancel();
    }

    @Inject(method = "addCollisionBoxToList" + "(Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/util/math/AxisAlignedBB;" + "Ljava/util/List;" + "Lnet/minecraft/util/math/AxisAlignedBB;)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("DuplicatedCode")
    private static void addCollisionBoxToListHook(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> cBoxes, AxisAlignedBB blockBox, CallbackInfo info) {
        if (blockBox != Block.NULL_AABB && (Cascade.moduleManager.isModuleEnabled("Jesus"))) {
            AxisAlignedBB bb = blockBox.offset(pos);
            CollisionEvent event = new CollisionEvent(pos, bb, null, Util.mc.world != null ? Util.mc.world.getBlockState(pos).getBlock() : null);
            //Jesus.getInstance().onCollision(event);

            if (bb != event.getBB()) {
                bb = event.getBB();
            }

            if (bb != null && entityBox.intersects(bb)) {
                cBoxes.add(bb);
            }

            info.cancel();
        }
    }
}