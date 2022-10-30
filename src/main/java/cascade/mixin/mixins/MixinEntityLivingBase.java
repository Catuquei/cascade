package cascade.mixin.mixins;

import cascade.event.events.LiquidJumpEvent;
import cascade.features.modules.player.PacketUse;
import cascade.util.Util;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityLivingBase.class})
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow
    protected int activeItemStackUseCount;
    @Shadow
    protected ItemStack activeItemStack;

    @Redirect(method={"onItemUseFinish"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/EntityLivingBase;resetActiveHand()V"))
    private void resetActiveHandHook(EntityLivingBase base) {
        if (PacketUse.getInstance().isEnabled() && base instanceof EntityPlayerSP && !Util.mc.isSingleplayer() && PacketUse.getInstance().mode.getValue() == PacketUse.Mode.NoDelay && (activeItemStack.getItem() instanceof ItemFood) || activeItemStack.getItem() instanceof ItemBucketMilk || (activeItemStack.getItem() instanceof ItemPotion) && PacketUse.getInstance().potions.getValue()) {
            activeItemStackUseCount = 0; //field_70170_p.isRemote removed this
            ((EntityPlayerSP)base).connection.sendPacket(new CPacketPlayerTryUseItem(base.getActiveHand()));
        } else {
            base.resetActiveHand();
        }
    }

    @Inject(method = "handleJumpWater", at = @At("HEAD"), cancellable = true)
    private void handleJumpWaterHook(CallbackInfo info) {
        LiquidJumpEvent event = new LiquidJumpEvent(EntityLivingBase.class.cast(this));
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "handleJumpLava", at = @At("HEAD"), cancellable = true)
    private void handleJumpLavaHook(CallbackInfo info) {
        LiquidJumpEvent event = new LiquidJumpEvent(EntityLivingBase.class.cast(this));
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }
    }
}