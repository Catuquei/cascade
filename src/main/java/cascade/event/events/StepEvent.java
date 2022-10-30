package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.util.math.AxisAlignedBB;

public class StepEvent extends EventStage {

    private final AxisAlignedBB bb;
    private float height;

    public StepEvent(int stage, AxisAlignedBB bb, float height) {
        super(stage);
        this.height = height;
        this.bb = bb;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        if (this.getStage() == 0) {
            this.height = height;
        }
    }

    public AxisAlignedBB getBB() {
        return bb;
    }
}
