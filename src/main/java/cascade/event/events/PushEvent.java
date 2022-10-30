package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PushEvent extends EventStage {

    public Entity entity;
    public double x;
    public double y;
    public double z;
    public boolean airbone;

    public PushEvent(final Entity entity, final double x, final double y, final double z, final boolean airbone) {
        super(0);
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.airbone = airbone;
    }

    public PushEvent(final int stage) {
        super(stage);
    }

    public PushEvent(final int stage, final Entity entity) {
        super(stage);
        this.entity = entity;
    }
}