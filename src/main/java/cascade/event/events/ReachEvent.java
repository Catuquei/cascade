package cascade.event.events;

import cascade.event.EventStage;

public class ReachEvent extends EventStage {

    private float distance;

    public ReachEvent(float distance) {
        this.distance = distance;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}