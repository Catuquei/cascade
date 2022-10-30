package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.event.events.TotemPopEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

public class PopChams extends Module {

    /**
     * @author zPrestige & kambing & gerald
     */

    private static PopChams INSTANCE = new PopChams();

    public EntityOtherPlayerMP fakeEntity;

    public Setting<Boolean> solidParent = register(new Setting("Solid", false));
    public Setting<Boolean> solidSetting = register(new Setting("RenderSolid", true, v-> solidParent.getValue()));
    public Setting<Float> red = register(new Setting<>( "SolidRed", 0.0f, 0.0f, 255.0f, v-> solidParent.getValue() && solidSetting.getValue()));
    public Setting<Float> green = register(new Setting<>("SolidGreen", 255.0f, 0.0f, 255.0f, v-> solidParent.getValue() && solidSetting.getValue()));
    public Setting<Float> blue = register(new Setting<>("SolidBlue", 0.0f, 0.0f, 255.0f, v-> solidParent.getValue() && solidSetting.getValue()));

    public Setting<Boolean> wireFrameParent = register(new Setting("WireFrame", false));
    public Setting<Boolean> wireFrameSetting = register(new Setting("RenderWire", true, v-> wireFrameParent.getValue()));
    public Setting<Float> wireRed = register(new Setting<>( "WireRed", 0.0f, 0.0f, 255.0f, v-> wireFrameParent.getValue() && wireFrameSetting.getValue()));
    public Setting<Float> wireGreen = register(new Setting<>("WireGreen", 255.0f, 0.0f, 255.0f, v-> wireFrameParent.getValue() && wireFrameSetting.getValue()));
    public Setting<Float> wireBlue = register(new Setting<>("WireBlue", 0.0f, 0.0f, 255.0f, v-> wireFrameParent.getValue() && wireFrameSetting.getValue()));

    public Setting<Boolean> fadeParent = register(new Setting<>("Fade",  false));
    public Setting<Integer> startAlpha = register(new Setting<>("StartAlpha", 255, 0, 255, v-> fadeParent.getValue()));
    public Setting<Integer> endAlpha = register(new Setting<>("EndAlpha", 0, 0, 255, v-> fadeParent.getValue()));
    public Setting<Integer> fadeStep = register(new Setting<>("FadeStep", 10, 10, 100, v-> fadeParent.getValue()));

    public Setting<Boolean> yTravelParent = register(new Setting<>("YMovement",  false));
    public Setting<Boolean> yTravel = register(new Setting("YTravel",false, v-> yTravelParent.getValue()));
    public Setting<YTravelMode> yTravelMode = register(new Setting("TravelMode",YTravelMode.UP, v-> yTravelParent.getValue() && yTravel.getValue()));
    public enum YTravelMode{UP, DOWN}
    public Setting<Double> yTravelSpeed = register(new Setting<>("TravelSpeed", 0.1, 0.0, 2.0, v-> yTravel.getValue()));

    public Setting<Boolean> miscParent = register(new Setting<>("Misc",  false));
    public Setting<Boolean> onDeath = register(new Setting("OnDeath", false, v-> miscParent.getValue()));
    public Setting<Boolean> clearListOnPop = register(new Setting("ClearListOnPop", false, v-> miscParent.getValue()));
    public Setting<Boolean> clearListOnDeath = register(new Setting("ClearListOnDeath", false, v-> miscParent.getValue()));
    public Setting<Boolean> antiSelf = register(new Setting("AntiSelf", false, v-> miscParent.getValue()));

    public HashMap<EntityPlayer, Integer> poppedPlayers = new HashMap<>();

    public PopChams(){
        super("PopChams", Category.VISUAL, "Renders chams when a player pops");
        this.setInstance();
    }

    public static PopChams getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PopChams();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (Map.Entry<EntityPlayer, Integer> pop : poppedPlayers.entrySet()) {
            poppedPlayers.put(pop.getKey(), pop.getValue() - ((fadeStep.getValue() + 10) / 20));
            if (pop.getValue() <= endAlpha.getValue()) {
                poppedPlayers.remove(pop.getKey());
                return;
            }
            if(PopChams.getInstance().yTravel.getValue()){
                if(PopChams.getInstance().yTravelMode.getValue() == PopChams.YTravelMode.UP) {
                    pop.getKey().posY = pop.getKey().posY + (PopChams.getInstance().yTravelSpeed.getValue() / 20);
                } else if(PopChams.getInstance().yTravelMode.getValue() == PopChams.YTravelMode.DOWN){
                    pop.getKey().posY = pop.getKey().posY - (PopChams.getInstance().yTravelSpeed.getValue() / 20);
                }
            }
            if(wireFrameSetting.getValue()) {
                GlStateManager.pushMatrix();
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(1032, 6913);
                glDisable(3553);
                glDisable(2896);
                glDisable(2929);
                glEnable(2848);
                glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glColor4f(wireRed.getValue() / 255f, wireGreen.getValue() / 255f, wireBlue.getValue() / 255f, pop.getValue() / 255f);
                renderEntityStatic(pop.getKey(), event.getPartialTicks(), false);
                GL11.glLineWidth(1f);
                glEnable(2896);
                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
            }
            if(solidSetting.getValue()) {
                GL11.glPushMatrix();
                GL11.glDepthRange(0.01, 1.0f);
                GL11.glPushAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GL11.glLineWidth(1f);
                GL11.glColor4f(red.getValue() / 255f, green.getValue() / 255f, blue.getValue() / 255f, pop.getValue() / 255f);
                renderEntityStatic(pop.getKey(), event.getPartialTicks(), false);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glColor4f(1f, 1f, 1f, 1f);
                GL11.glPopAttrib();
                GL11.glDepthRange(0.0, 1.0f);
                GL11.glPopMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onPop(TotemPopEvent event) {
        if (mc.world.getEntityByID(event.getEntity().entityId) != null && isEnabled()) {
            if(antiSelf.getValue() && event.getEntity().entityId == mc.player.getEntityId()){
                return;
            }
            final Entity entity = mc.world.getEntityByID(event.getEntity().entityId);
            if (entity instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer) entity;
                fakeEntity = new EntityOtherPlayerMP(mc.world, player.getGameProfile());
                fakeEntity.copyLocationAndAnglesFrom(player);
                fakeEntity.rotationYawHead = player.rotationYawHead;
                fakeEntity.prevRotationYawHead = player.rotationYawHead;
                fakeEntity.rotationYaw = player.rotationYaw;
                fakeEntity.prevRotationYaw = player.rotationYaw;
                fakeEntity.rotationPitch = player.rotationPitch;
                fakeEntity.prevRotationPitch = player.rotationPitch;
                fakeEntity.cameraYaw = fakeEntity.rotationYaw;
                fakeEntity.cameraPitch = fakeEntity.rotationPitch;
                if(clearListOnPop.getValue()) {
                    poppedPlayers.clear();
                }
                poppedPlayers.put(fakeEntity, startAlpha.getValue());
            }
        }
    }

    public void onDeath(int entityId){
        if( onDeath.getValue()) {
            if (mc.world.getEntityByID(entityId) != null) {
                final Entity entity = mc.world.getEntityByID(entityId);
                if (entity instanceof EntityPlayer) {
                    final EntityPlayer player = (EntityPlayer) entity;
                    fakeEntity = new EntityOtherPlayerMP(mc.world, player.getGameProfile());
                    fakeEntity.copyLocationAndAnglesFrom(player);
                    fakeEntity.rotationYawHead = player.rotationYawHead;
                    fakeEntity.prevRotationYawHead = player.rotationYawHead;
                    fakeEntity.rotationYaw = player.rotationYaw;
                    fakeEntity.prevRotationYaw = player.rotationYaw;
                    fakeEntity.rotationPitch = player.rotationPitch;
                    fakeEntity.prevRotationPitch = player.rotationPitch;
                    fakeEntity.cameraYaw = fakeEntity.rotationYaw;
                    fakeEntity.cameraPitch = fakeEntity.rotationPitch;
                    if(clearListOnDeath.getValue()) {
                        poppedPlayers.clear();
                    }
                    poppedPlayers.put(fakeEntity, startAlpha.getValue());
                }
            }
        }
    }

    public void handlePopESP(int entityId){
        if (mc.world.getEntityByID(entityId) != null) {
            final Entity entity = mc.world.getEntityByID(entityId);
            if (entity instanceof EntityPlayer) {
                final EntityPlayer player = (EntityPlayer) entity;
                fakeEntity = new EntityOtherPlayerMP(mc.world, player.getGameProfile());
                fakeEntity.copyLocationAndAnglesFrom(player);
                fakeEntity.rotationYawHead = player.rotationYawHead;
                fakeEntity.prevRotationYawHead = player.rotationYawHead;
                fakeEntity.rotationYaw = player.rotationYaw;
                fakeEntity.prevRotationYaw = player.rotationYaw;
                fakeEntity.rotationPitch = player.rotationPitch;
                fakeEntity.prevRotationPitch = player.rotationPitch;
                fakeEntity.cameraYaw = fakeEntity.rotationYaw;
                fakeEntity.cameraPitch = fakeEntity.rotationPitch;
                if(clearListOnDeath.getValue()) {
                    poppedPlayers.clear();
                }
                poppedPlayers.put(fakeEntity, startAlpha.getValue());
            }
        }
    }


    public void renderEntityStatic(Entity entityIn, float partialTicks, boolean p_188388_3_) {
        if (entityIn.ticksExisted == 0) {
            entityIn.lastTickPosX = entityIn.posX;
            entityIn.lastTickPosY = entityIn.posY;
            entityIn.lastTickPosZ = entityIn.posZ;
        }
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        float f = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks;
        int i = entityIn.getBrightnessForRender();
        /** this may fix the bright floor thing
         if (entityIn.isBurning()) {
         i = 15728880;
         }
         int j = i % 65536;
         int k = i / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
         */
        mc.getRenderManager().renderEntity(entityIn, d0 - mc.getRenderManager().viewerPosX, d1 - mc.getRenderManager().viewerPosY, d2 - mc.getRenderManager().viewerPosZ, f, partialTicks, p_188388_3_);
    }
}