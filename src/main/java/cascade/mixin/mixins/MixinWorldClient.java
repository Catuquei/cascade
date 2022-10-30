package cascade.mixin.mixins;


import cascade.event.events.WorldClientEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void constructorHook(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new WorldClientEvent.Load(WorldClient.class.cast(this)));
    }
}