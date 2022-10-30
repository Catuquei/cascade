package cascade.mixin.mixins;

import cascade.Cascade;
import cascade.event.events.GameLoopEvent;
import cascade.event.events.KeyEvent;
import cascade.event.events.WorldClientEvent;
import cascade.features.modules.core.ClientManagement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {Minecraft.class})
public abstract class MixinMinecraft {

    private int gameLoop = 0;

    public WorldClient world;

    @Inject(method = {"shutdownMinecraftApplet"}, at = {@At(value = "HEAD")})
    private void stopClient(CallbackInfo callbackInfo) {
        unload();
    }

    @Redirect(method = {"run"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void displayCrashReport(Minecraft minecraft, CrashReport crashReport) {
        unload();
    }

    @Inject(method = {"runTickKeyboard"}, at = {@At(value = "INVOKE", remap = false, target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", ordinal = 0, shift = At.Shift.BEFORE)})
    private void onKeyboard(CallbackInfo callbackInfo) {
        int i;
        int n = i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            KeyEvent event = new KeyEvent(i);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Inject(method = {"getLimitFramerate"}, at = {@At(value = "HEAD")}, cancellable = true)
    public
    void getLimitFramerateHook(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            if (ClientManagement.getInstance().isEnabled() && ClientManagement.getInstance().unfocusedCPU.getValue() && !Display.isActive()) {
                callbackInfoReturnable.setReturnValue(ClientManagement.getInstance().cpuFPS.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public int getGameLoop() {
        return gameLoop;
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoopHead(CallbackInfo callbackInfo) {
        gameLoop++;
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.AFTER))
    private void post_ScheduledTasks(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new GameLoopEvent());
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;" + "Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorldHook(WorldClient worldClient, String loadingMessage, CallbackInfo info) {
        if (world != null) {
            MinecraftForge.EVENT_BUS.post(new WorldClientEvent.Unload(world));
        }
    }

    void unload() {
        Cascade.LOGGER.info("Initiated client shutdown.");
        Cascade.onUnload();
        Cascade.LOGGER.info("Finished client shutdown.");
    }
}