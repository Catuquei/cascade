package cascade.event.events;

import cascade.event.EventStage;
import net.minecraft.util.text.ITextComponent;

public class DisconnectEvent extends EventStage {

    private final ITextComponent component;

    public DisconnectEvent(ITextComponent component) {
        this.component = component;
    }

    public ITextComponent getComponent() {
        return component;
    }

}