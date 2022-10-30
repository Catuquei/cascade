package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CollisionEvent extends EventStage {

    private final Entity entity;
    private final BlockPos pos;
    private final Block block;

    private AxisAlignedBB bb;

    public CollisionEvent(BlockPos pos, AxisAlignedBB bb, Entity entity, Block block) {
        this.pos = pos;
        this.bb = bb;
        this.entity = entity;
        this.block = block;
    }

    public AxisAlignedBB getBB() {
        return bb;
    }

    public void setBB(AxisAlignedBB bb) {
        this.bb = bb;
    }

    public Entity getEntity() {
        return entity;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getBlock() {
        return block;
    }

    public interface Listener {
        void onCollision(CollisionEvent event);
    }
}