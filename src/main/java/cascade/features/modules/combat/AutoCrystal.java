package cascade.features.modules.combat;

import cascade.Cascade;
import cascade.event.events.PacketEvent;
import cascade.event.events.Render3DEvent;
import cascade.event.events.UpdateWalkingPlayerEvent;
import cascade.features.modules.Module;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.Setting;
import cascade.util.misc.MathUtil;
import cascade.util.misc.Timer;
import cascade.util.entity.EntityUtil;
import cascade.util.player.BlockUtil;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {

    private Timer placeTimer = new Timer();
    private Timer breakTimer = new Timer();
    private Timer predictTimer = new Timer();
    private Timer swapTimer = new Timer();
    public Setting<Page> page = register(new Setting("Page", Page.Place));
    public Setting<Boolean> place;
    public Setting<Float> placeRange;
    public Setting<Float> placeWallRange;
    public Setting<Float> placeDelay;
    public Setting<Float> minDamage;
    public Setting<Float> maxSelfDamage;
    public Setting<Float> facePlace;
    public Setting<Float> minArmor;
    public Setting<Float> targetRange;
    public Setting<Boolean> predictMotion;
    public Setting<Integer> motionTicks;

    public Setting<Boolean> explode;
    public Setting<Float> breakDelay;
    public Setting<Float> breakRange;
    public Setting<Float> breakWallRange;
    public Setting<Boolean> packetBreak;
    public Setting<Boolean> predicts;
    public Setting<Integer> attackFactor;
    public Setting<Boolean> remove;
    public Setting<Integer> ticksExisted;

    public Setting<Boolean> await;
    public Setting<Boolean> noSuicide;
    public Setting<Float> safetyFactor;
    public Setting<SwapMode> swapType;
    public Setting<Integer> autoSwitchCooldown;
    public Setting<Boolean> ignoreUseAmount;
    public Setting<Integer> wasteAmount;
    public Setting<SwingMode> swingMode;

    public Setting<Boolean> render;
    public Setting<Boolean> renderDmg;
    public Setting<HUD> hud;
    public enum HUD {Target, Damage}
    public Setting<Boolean> box;
    public Setting<Color> c;
    public Setting<Float> lineWidth;
    public Setting<Boolean> outline;
    public Setting<Boolean> fadeSlower;
    private ConcurrentHashMap<BlockPos, Integer> renderSpots;
    EntityEnderCrystal crystal;
    public EntityLivingBase target;
    public BlockPos pos;
    public BlockPos calcPos;
    private int hotBarSlot;
    private boolean armor;
    private boolean armorTarget;
    private int crystalCount;
    double damage;
    private EntityLivingBase realTarget;
    private boolean exploded;
    private boolean confirmed;
    private CalculationThread calculationThread;
    public EnumHand hand;
    public static AutoCrystal INSTANCE;

    public static AutoCrystal getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoCrystal();
        }
        return INSTANCE;
    }

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT, "babbaj moment");
        place = (Setting<Boolean>)this.register(new Setting("Place", true, v -> this.page.getValue() == Page.Place));
        placeRange = (Setting<Float>)this.register(new Setting("PlaceRange", 4.0f, 0.1f, 6.0f, p -> this.place.getValue() && this.page.getValue() == Page.Place));
        placeWallRange = (Setting<Float>)this.register(new Setting("PlaceWallRange", 3.0f, 0.1f, 6.0f, p -> this.place.getValue() && this.page.getValue() == Page.Place));
        placeDelay = (Setting<Float>)this.register(new Setting("PlaceDelay", 0.0f, 0.0f, 300.0f, p -> this.place.getValue() && this.page.getValue() == Page.Place));
        minDamage = (Setting<Float>)this.register(new Setting("MinDamage", 4.0f, 0.1f, 36.0f, v -> this.page.getValue() == Page.Place));
        maxSelfDamage = (Setting<Float>)this.register(new Setting("MaxSelfDamage", 10.0f, 0.0f, 36.0f, v -> this.page.getValue() == Page.Place));
        facePlace = (Setting<Float>)this.register(new Setting("FacePlaceHP", 4.0f, 0.0f, 36.0f, v -> this.page.getValue() == Page.Place));
        minArmor = (Setting<Float>)this.register(new Setting("MinArmor", 4.0f, 0.1f, 100.0f, v -> this.page.getValue() == Page.Place));
        targetRange = (Setting<Float>)this.register(new Setting("TargetRange", 4.0f, 1.0f, 16.0f, v -> this.page.getValue() == Page.Place));
        predictMotion = (Setting<Boolean>)this.register(new Setting("PredictMotion", true, v -> this.page.getValue() == Page.Place));
        motionTicks = (Setting<Integer>)this.register(new Setting("MotionTicks", 2, 1, 15, v -> this.predictMotion.getValue() && this.page.getValue() == Page.Place));

        explode = (Setting<Boolean>)this.register(new Setting("Break", true, v -> this.page.getValue() == Page.Break));
        breakDelay = (Setting<Float>)this.register(new Setting("BreakDelay", 10.0f, 0.0f, 300.0f, v -> page.getValue() == Page.Break));
        breakRange = (Setting<Float>)this.register(new Setting("BreakRange", 4.0f, 0.1f, 6.0f, v -> page.getValue() == Page.Break));
        breakWallRange = (Setting<Float>)this.register(new Setting("BreakWallRange", 4.0f, 0.1f, 6.0f, v -> page.getValue() == Page.Break));
        packetBreak = (Setting<Boolean>)this.register(new Setting("PacketBreak", true, v -> page.getValue() == Page.Break));
        predicts = (Setting<Boolean>)this.register(new Setting("Predict", true, v -> page.getValue() == Page.Break));
        attackFactor = (Setting<Integer>)this.register(new Setting("PredictDelay", 0, 0, 200, p -> predicts.getValue() && this.page.getValue() == Page.Break));
        remove = (Setting<Boolean>)this.register(new Setting("Remove", true, v -> page.getValue() == Page.Break));
        ticksExisted = (Setting<Integer>)this.register(new Setting("TicksExisted", 0, 0, 5, p -> predicts.getValue() && this.page.getValue() == Page.Break));

        noSuicide = register(new Setting("NoSuicide", true, v -> page.getValue() == Page.Misc));
        //safetyFactor = register(new Setting("SafetyFactor", 1.6f, 0.0f, 3.0f, v -> page.getValue() == Page.Misc));
        swapType = register(new Setting("Switch", SwapMode.Off, v -> page.getValue() == Page.Misc));
        autoSwitchCooldown = register(new Setting("Cooldown", 50, 0, 200, p -> swapType.getValue() == SwapMode.Normal && this.page.getValue() == Page.Misc));
        ignoreUseAmount = register(new Setting("IgnoreUseAmount", true, v -> page.getValue() == Page.Misc));
        wasteAmount = register(new Setting("UseAmount", 4, 1, 5, v -> page.getValue() == Page.Misc));
        swingMode = register(new Setting("Swing", SwingMode.MainHand, v -> page.getValue() == Page.Misc));

        render = register(new Setting("Render", true, v -> page.getValue() == Page.Render));
        renderDmg = register(new Setting("RenderDmg", true, v -> page.getValue() == Page.Render));
        hud = register(new Setting("HUD", HUD.Target, v -> page.getValue() == Page.Render));
        box = register(new Setting("Box", true, v -> page.getValue() == Page.Render));
        c = register(new Setting("Color", new Color(-1)));
        lineWidth = register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> page.getValue() == Page.Render));
        outline = register(new Setting("Outline", true, v -> page.getValue() == Page.Render));
        fadeSlower = register(new Setting("FadeSlower", false, v -> page.getValue() == Page.Render));
        damage = 0.5;
        exploded = false;
        confirmed = true;
        INSTANCE = this;
    }

    public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                int y = sphere ? (cy - (int)r) : cy;
                while (true) {
                    float f2;
                    float f = f2 = (sphere ? (cy + r) : ((float)(cy + h)));
                    if (y >= f) {
                        break;
                    }
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
            }
        }
        return circleblocks;
    }

    @Override
    public void onDisable() {
        if (!fullNullCheck() && shouldNotify()) {
            TextComponentString text = new TextComponentString(Cascade.chatManager.getClientMessage() + " " + ChatFormatting.RED + name + " toggled off.");
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        mc.addScheduledTask(() -> onCrystal());
    }

    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketUseEntity) {
            final CPacketUseEntity cPacketUseEntity = event.getPacket();
            if (cPacketUseEntity.getAction() == CPacketUseEntity.Action.ATTACK && cPacketUseEntity.getEntityFromWorld((World)AutoCrystal.mc.world) instanceof EntityEnderCrystal) {
                if (this.remove.getValue()) {
                    Objects.requireNonNull(cPacketUseEntity.getEntityFromWorld(mc.world)).setDead();
                    AutoCrystal.mc.world.removeEntityFromWorld(cPacketUseEntity.entityId);
                }
                if (AutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                    int crystalSlot = (AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) ? AutoCrystal.mc.player.inventory.currentItem : -1;
                    if (crystalSlot == -1) {
                        for (int l = 0; l < 9; ++l) {
                            if (AutoCrystal.mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                                crystalSlot = l;
                                hotBarSlot = l;
                                break;
                            }
                        }
                    }
                    if (crystalSlot == -1) {
                        pos = null;
                        calcPos = null;
                        target = null;
                        realTarget = null;
                        return;
                    }
                }
                if (swapType.getValue() == SwapMode.Silent) {
                    return;
                }
                if (pos != null && mc.player.onGround) {
                    RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5));
                    EnumFacing f = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, f, (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)pos.x, (float)pos.y, (float)pos.z));
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck() && shouldNotify()) {
            TextComponentString text = new TextComponentString(Cascade.chatManager.getClientMessage() + " " + ChatFormatting.GREEN + name +" toggled on.");
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }

        this.renderSpots = new ConcurrentHashMap<>();
        this.placeTimer.reset();
        this.breakTimer.reset();
        this.swapTimer.reset();
        this.hotBarSlot = -1;
        this.damage = 0.5;
        this.pos = null;
        this.calcPos = null;
        this.crystal = null;
        this.target = null;
        this.realTarget = null;
        this.armor = false;
        this.armorTarget = false;
        this.exploded = false;
        this.confirmed = true;
        (this.calculationThread = new CalculationThread()).start();
    }

    @Override
    public String getDisplayInfo() {
        if (realTarget != null) {
            if (hud.getValue() == HUD.Target) {
                return realTarget.getName();
            }
            if (hud.getValue() == HUD.Damage) {
                return String.format("%.1f", damage);
            }
        }

        return null;
    }

    public void onCrystal() {
        if (fullNullCheck()) {
            return;
        }
        if (Cascade.moduleManager.isModuleEnabled("Burrow")) {
            return;
        }
        crystalCount = 0;
        if (!ignoreUseAmount.getValue()) {
            for (Entity crystal : AutoCrystal.mc.world.loadedEntityList) {
                if (crystal instanceof EntityEnderCrystal) {
                    if (!IsValidCrystal(crystal)) {
                        continue;
                    }
                    boolean count = false;
                    double damage = calculateDamage(target.getPosition().getX() + 0.5, target.getPosition().getY() + 1.0, target.getPosition().getZ() + 0.5, target);
                    if (damage >= minDamage.getValue()) {
                        count = true;
                    }
                    if (!count) {
                        continue;
                    }
                    ++crystalCount;
                }
            }
        }
        hotBarSlot = -1;
        if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            int crystalSlot = (AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) ? mc.player.inventory.currentItem : -1;
            if (crystalSlot == -1) {
                for (int l = 0; l < 9; ++l) {
                    if (mc.player.inventory.getStackInSlot(l).getItem() == Items.END_CRYSTAL) {
                        crystalSlot = l;
                        hotBarSlot = l;
                        break;
                    }
                }
            }
            if (crystalSlot == -1) {
                pos = null;
                calcPos = null;
                target = null;
                realTarget = null;
                return;
            }
        }
        if (target == null) {
            target = getTarget();
        }
        if (target == null) {
            crystal = null;
            return;
        }
        if (target.getDistance(mc.player) > 12.0f) {
            crystal = null;
            target = null;
            realTarget = null;
        }
        this.crystal = (EntityEnderCrystal)AutoCrystal.mc.world.loadedEntityList.stream().filter(this::IsValidCrystal).map(p_Entity -> p_Entity).min(Comparator.comparing(p_Entity -> this.target.getDistance(p_Entity))).orElse(null);
        if (this.crystal != null && this.explode.getValue()) {
            if (this.crystal.ticksExisted < this.ticksExisted.getValue()) {
                return;
            }
            if (breakTimer.passedMs(breakDelay.getValue().longValue())) {
                breakTimer.reset();
                if (swingMode.getValue() == SwingMode.MainHand) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
                if (swingMode.getValue() == SwingMode.OffHand) {
                    mc.player.swingArm(EnumHand.OFF_HAND);
                }
                int oldSlot = AutoCrystal.mc.player.inventory.currentItem;
                int swordSlot = (AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) ? AutoCrystal.mc.player.inventory.currentItem : -1;
                if (swordSlot == -1) {
                    for (int i = 0; i < 9; ++i) {
                        if (AutoCrystal.mc.player.inventory.getStackInSlot(i).getItem() == Items.DIAMOND_SWORD) {
                            swordSlot = i;
                            break;
                        }
                    }
                }
                if (this.packetBreak.getValue()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                } else {
                    mc.playerController.attackEntity(mc.player, crystal);
                }
                this.exploded = true;
            }
        }
        if (this.placeTimer.passedMs(this.placeDelay.getValue().longValue()) && this.place.getValue()) {
            this.placeTimer.reset();
            this.pos = this.calcPos;
            final int oldSlot = AutoCrystal.mc.player.inventory.currentItem;
            if (this.pos == null) {
                return;
            }
            if (this.swapType.getValue() == SwapMode.Normal && AutoCrystal.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && AutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                AutoCrystal.mc.player.inventory.currentItem = this.hotBarSlot;
                return;
            }
            if (this.swapType.getValue() == SwapMode.Silent && AutoCrystal.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) {
                AutoCrystal.mc.player.inventory.currentItem = this.hotBarSlot;
                AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(this.hotBarSlot));
            }
            if (this.swapType.getValue() == SwapMode.Normal) {
                if (!this.swapTimer.passedMs(this.autoSwitchCooldown.getValue())) {
                    return;
                }
                this.swapTimer.reset();
            }
            if (this.swapType.getValue() != SwapMode.Silent && AutoCrystal.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && AutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                this.pos = null;
                this.calcPos = null;
                this.target = null;
                this.realTarget = null;
                return;
            }
            if (!this.ignoreUseAmount.getValue()) {
                int crystalLimit = this.wasteAmount.getValue();
                if (this.crystalCount >= crystalLimit) {
                    return;
                }
                if (this.damage < this.minDamage.getValue()) {
                    crystalLimit = 1;
                }
                if (this.crystalCount < crystalLimit && this.pos != null) {
                    if (!this.exploded) {
                        final RayTraceResult result = AutoCrystal.mc.world.rayTraceBlocks(new Vec3d(AutoCrystal.mc.player.posX, AutoCrystal.mc.player.posY + AutoCrystal.mc.player.getEyeHeight(), AutoCrystal.mc.player.posZ), new Vec3d(this.pos.x + 0.5, this.pos.y + 1.0, this.pos.z + 0.5));
                        final EnumFacing f = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
                        AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(this.pos, f, (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)this.pos.x, (float)this.pos.y, (float)this.pos.z));
                        AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
                        this.renderSpots.put(this.pos, c.getValue().getAlpha());
                    }
                    else {
                        this.exploded = false;
                    }
                }
            }
            else if (this.pos != null) {
                if (!this.exploded) {
                    final RayTraceResult result2 = AutoCrystal.mc.world.rayTraceBlocks(new Vec3d(AutoCrystal.mc.player.posX, AutoCrystal.mc.player.posY + AutoCrystal.mc.player.getEyeHeight(), AutoCrystal.mc.player.posZ), new Vec3d(this.pos.x + 0.5, this.pos.y + 1.0, this.pos.z + 0.5));
                    final EnumFacing f2 = (result2 == null || result2.sideHit == null) ? EnumFacing.UP : result2.sideHit;
                    AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(this.pos, f2, (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float)this.pos.x, (float)this.pos.y, (float)this.pos.z));
                    AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
                    this.renderSpots.put(this.pos, c.getValue().getAlpha());
                }
                else {
                    this.exploded = false;
                }
            }
            if (this.swapType.getValue() == SwapMode.Silent) {
                AutoCrystal.mc.player.inventory.currentItem = oldSlot;
                AutoCrystal.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(oldSlot));
            }
            this.confirmed = false;
        }
    }

    private void doCalculations() {
        if (target == null) {
            target = getTarget();
        }
        if (this.target == null) {
            return;
        }
        this.damage = 0.5;
        for (BlockPos blockPos : this.placePostions(this.placeRange.getValue())) {
            if (blockPos != null && this.target != null && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty() && this.target.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= this.targetRange.getValue() && !this.target.isDead) {
                if (this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0f) {
                    continue;
                }
                double targetDmg = this.calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, target);
                double localDmg = this.calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, mc.player);
                this.armor = false;
                if (localDmg > maxSelfDamage.getValue()) {
                    continue;
                }
                /*todo if (safetyFactor.getValue() != 0.0f) {
                    if (localDmg * safetyFactor.getValue() >= targetDmg) {
                        continue;
                    }
                }*/
                try {
                    for (final ItemStack is : this.target.getArmorInventoryList()) {
                        final float green = (is.getMaxDamage() - (float)is.getItemDamage()) / is.getMaxDamage();
                        final float red = 1.0f - green;
                        final int dmg = 100 - (int)(red * 100.0f);
                        if (dmg > this.minArmor.getValue()) {
                            continue;
                        }
                        this.armor = true;
                    }
                }
                catch (Exception ex) {}
                final double selfDmg;
                if ((targetDmg < this.minDamage.getValue() && this.target.getHealth() + this.target.getAbsorptionAmount() > this.facePlace.getValue() && !this.armor) || ((selfDmg = this.calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, (Entity)AutoCrystal.mc.player) + 4.0) >= AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() && selfDmg >= targetDmg)) {
                    continue;
                }
                if (this.damage > targetDmg) {
                    continue;
                }
                this.calcPos = blockPos;
                this.damage = targetDmg;
            }
        }
        if (this.damage == 0.5) {
            this.pos = null;
            this.calcPos = null;
            this.target = null;
            this.realTarget = null;
            return;
        }
        this.realTarget = this.target;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (Cascade.moduleManager.isModuleEnabled("Burrow")) {
            return;
        }
        SPacketSpawnObject packet;
        if (event.getPacket() instanceof SPacketSpawnObject && (packet = event.getPacket()).getType() == 51 && this.predicts.getValue() && this.predictTimer.passedMs(this.attackFactor.getValue()) && this.predicts.getValue() && this.explode.getValue() && this.packetBreak.getValue() && this.target != null) {
            if (!this.isPredicting(packet)) {
                return;
            }
            final CPacketUseEntity predict = new CPacketUseEntity();
            predict.entityId = packet.getEntityID();
            predict.action = CPacketUseEntity.Action.ATTACK;
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            mc.player.connection.sendPacket(predict);
        }
        if (event.getPacket() instanceof SPacketSoundEffect && isEnabled()) {
            SPacketSoundEffect sPacketSoundEffect = event.getPacket();
            try {
                if (sPacketSoundEffect.getCategory() == SoundCategory.BLOCKS && sPacketSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (final Entity e : AutoCrystal.mc.world.loadedEntityList) {
                        if (e instanceof EntityEnderCrystal && e.getDistance(sPacketSoundEffect.getX(), sPacketSoundEffect.getY(), sPacketSoundEffect.getZ()) <= breakRange.getValue()) {
                            Objects.requireNonNull(AutoCrystal.mc.world.getEntityByID(e.getEntityId())).setDead();
                            mc.world.removeEntityFromWorld(e.entityId);
                            confirmed = true;
                        }
                    }
                }
            }
            catch (Exception ex) {}
        }
        if (event.getPacket() instanceof SPacketExplosion) {
            try {
                final SPacketExplosion sPacketExplosion = event.getPacket();
                for (final Entity e : AutoCrystal.mc.world.loadedEntityList) {
                    if (e instanceof EntityEnderCrystal && e.getDistance(sPacketExplosion.getX(), sPacketExplosion.getY(), sPacketExplosion.getZ()) <= this.breakRange.getValue()) {
                        Objects.requireNonNull(AutoCrystal.mc.world.getEntityByID(e.getEntityId())).setDead();
                        AutoCrystal.mc.world.removeEntityFromWorld(e.entityId);
                        //continue; used to check if await setting is off
                        confirmed = true;
                    }
                }
            }
            catch (Exception ex2) {}
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.renderSpots != null && !this.renderSpots.isEmpty()) {
            for (final Map.Entry<BlockPos, Integer> entry : this.renderSpots.entrySet()) {
                final BlockPos blockPos = entry.getKey();
                Integer alpha = entry.getValue();
                if (this.fadeSlower.getValue()) {
                    --alpha;
                }
                else {
                    alpha -= 2;
                }
                if (alpha <= 0) {
                    this.renderSpots.remove(blockPos);
                }
                else {
                    this.renderSpots.replace(blockPos, alpha);
                    RenderUtil.drawBoxESP(blockPos, (ClickGui.getInstance().rainbow.getValue()) ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), alpha), this.outline.getValue(), ((boolean)ClickGui.getInstance().rainbow.getValue()) ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), alpha), this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), alpha, true);
                }
            }
        }
        if (pos != null && render.getValue() && target != null && renderDmg.getValue()) {
            try {
                double renderDamage = this.calculateDamage(this.pos.getX() + 0.5, this.pos.getY() + 1.0, this.pos.getZ() + 0.5, target);
                RenderUtil.drawText(this.pos, ((Math.floor(renderDamage) == renderDamage) ? Integer.valueOf((int)renderDamage) : String.format("%.1f", renderDamage)) + "");
            }
            catch (Exception ex) {}
        }
    }

    private boolean isPredicting(final SPacketSpawnObject packet) {
        try {
            final BlockPos packPos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
            return AutoCrystal.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= this.breakRange.getValue() && (BlockUtil.rayTracePlaceCheck(packPos) || AutoCrystal.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= this.breakWallRange.getValue()) && (this.ignoreUseAmount.getValue() || !this.target.isDead) && this.target.getHealth() + this.target.getAbsorptionAmount() > 0.0f;
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean IsValidCrystal(final Entity p_Entity) {
        try {
            if (p_Entity == null) {
                return false;
            }
            if (!(p_Entity instanceof EntityEnderCrystal)) {
                return false;
            }
            if (this.target == null) {
                return false;
            }
            if (p_Entity.getDistance((Entity)AutoCrystal.mc.player) > this.breakRange.getValue()) {
                return false;
            }
            if (!AutoCrystal.mc.player.canEntityBeSeen(p_Entity) && p_Entity.getDistance((Entity)AutoCrystal.mc.player) > this.breakWallRange.getValue()) {
                return false;
            }
            if (p_Entity.isDead) {
                return false;
            }
            if ((!this.ignoreUseAmount.getValue() && this.target.isDead) || this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0f) {
                return false;
            }
        }
        catch (Exception ex) {}
        return true;
    }

    private EntityPlayer getTarget() {
        EntityPlayer closestPlayer = null;
        try {
            for (EntityPlayer entity : mc.world.playerEntities) {
                if (mc.player != null && entity.entityId != -1337420 && !mc.player.isDead && !entity.isDead && entity != mc.player && !Cascade.friendManager.isFriend(entity.getName())) {
                    if (entity.getDistance(mc.player) > 12.0f) {
                        continue;
                    }
                    this.armorTarget = false;
                    for (ItemStack is : entity.getArmorInventoryList()) {
                        if (is.isEmpty) {
                            armorTarget = true;
                        }
                        final float green = (is.getMaxDamage() - (float)is.getItemDamage()) / is.getMaxDamage();
                        final float red = 1.0f - green;
                        final int dmg = 100 - (int)(red * 100.0f);
                        if (dmg > this.minArmor.getValue()) {
                            continue;
                        }
                        this.armorTarget = true;
                    }
                    if (EntityUtil.isInHole(entity) && entity.getAbsorptionAmount() + entity.getHealth() > this.facePlace.getValue() && !this.armorTarget && this.minDamage.getValue() > 2.2f) {
                        continue;
                    }
                    if (closestPlayer == null) {
                        closestPlayer = entity;
                    } else {
                        if (closestPlayer.getDistance(mc.player) <= entity.getDistance(mc.player)) {
                            continue;
                        }
                        closestPlayer = entity;
                    }
                }
            }
        } catch (Exception ex) {}
        if (closestPlayer != null && predictMotion.getValue()) {
            float f = closestPlayer.width / 2.0f;
            float f2 = closestPlayer.height;
            closestPlayer.setEntityBoundingBox(new AxisAlignedBB(closestPlayer.posX - f, closestPlayer.posY, closestPlayer.posZ - f, closestPlayer.posX + f, closestPlayer.posY + f2, closestPlayer.posZ + f));
            Entity y = getPredictedPosition(closestPlayer, motionTicks.getValue());
            closestPlayer.setEntityBoundingBox(y.getEntityBoundingBox());
        }
        return closestPlayer;
    }

    private NonNullList<BlockPos> placePostions(float placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), placeRange, (int)placeRange, false, true, 0).stream().filter(pos -> canPlaceCrystal(pos, true)).collect(Collectors.toList())); //deleted smth, check og src in case of issues
        return (NonNullList<BlockPos>)positions;
    }

    private boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        if (!BlockUtil.rayTracePlaceCheck(blockPos)) {
            if (mc.player.getDistanceSq(blockPos) > MathUtil.square(placeWallRange.getValue())) {
                return false;
            }
        } else if (mc.player.getDistanceSq(blockPos) > MathUtil.square(placeRange.getValue())) {
            return false;
        }
        try {
            /*if (ccMode.getValue()) {
                if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
                }
                for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) {
                        continue;
                    }
                    return false;
                }
            } else {*/
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && AutoCrystal.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
            if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                return false;
            }
            if (mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
                return false;
            }
            if (!specialEntityCheck) {
                return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
            }
            for (Entity entity : AutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                if (entity instanceof EntityEnderCrystal) {
                    continue;
                }
                return false;
            }
            for (Entity entity : AutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                if (entity instanceof EntityEnderCrystal) {
                    continue;
                }
                return false;
            }
            //}
        }
        catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = this.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        }
        catch (Exception ex) {}
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            try {
                finald = this.getBlastReduction((EntityLivingBase)entity, this.getDamageMultiplied(damage), new Explosion((World)AutoCrystal.mc.world, (Entity)null, posX, posY, posZ, 6.0f, false, true));
            }
            catch (Exception ex2) {}
        }
        return (float)finald;
    }

    public float getBlockDensity(final Vec3d vec, final AxisAlignedBB bb) {
        final double d0 = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        final double d2 = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        final double d3 = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        final double d4 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        final double d5 = (1.0 - Math.floor(1.0 / d3) * d3) / 2.0;
        if (d0 >= 0.0 && d2 >= 0.0 && d3 >= 0.0) {
            int j2 = 0;
            int k2 = 0;
            for (float f = 0.0f; f <= 1.0f; f += (float)d0) {
                for (float f2 = 0.0f; f2 <= 1.0f; f2 += (float)d2) {
                    for (float f3 = 0.0f; f3 <= 1.0f; f3 += (float)d3) {
                        final double d6 = bb.minX + (bb.maxX - bb.minX) * f;
                        final double d7 = bb.minY + (bb.maxY - bb.minY) * f2;
                        final double d8 = bb.minZ + (bb.maxZ - bb.minZ) * f3;
                        if (rayTraceBlocks(new Vec3d(d6 + d4, d7, d8 + d5), vec, false, false, false, true) == null) {
                            ++j2;
                        }
                        ++k2;
                    }
                }
            }
            return j2 / (float)k2;
        }
        return 0.0f;
    }

    public static RayTraceResult rayTraceBlocks(Vec3d vec31, final Vec3d vec32, final boolean stopOnLiquid, final boolean ignoreNoBox, final boolean returnLastUncollidableBlock, final boolean ignoreWebs) {
        if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
            return null;
        }
        if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
            int x1 = MathHelper.floor(vec31.x);
            int y1 = MathHelper.floor(vec31.y);
            int z1 = MathHelper.floor(vec31.z);
            final int x2 = MathHelper.floor(vec32.x);
            final int y2 = MathHelper.floor(vec32.y);
            final int z2 = MathHelper.floor(vec32.z);
            BlockPos pos = new BlockPos(x1, y1, z1);
            final IBlockState state = AutoCrystal.mc.world.getBlockState(pos);
            final Block block = state.getBlock();
            if ((!ignoreNoBox || state.getCollisionBoundingBox((IBlockAccess)AutoCrystal.mc.world, pos) != Block.NULL_AABB) && block.canCollideCheck(state, stopOnLiquid) && (!ignoreWebs || !(block instanceof BlockWeb))) {
                final RayTraceResult raytraceresult = state.collisionRayTrace((World)AutoCrystal.mc.world, pos, vec31, vec32);
                if (raytraceresult != null) {
                    return raytraceresult;
                }
            }
            RayTraceResult raytraceresult2 = null;
            int k1 = 200;
            while (k1-- >= 0) {
                if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                    return null;
                }
                if (x1 == x2 && y1 == y2 && z1 == z2) {
                    return returnLastUncollidableBlock ? raytraceresult2 : null;
                }
                boolean flag2 = true;
                boolean flag3 = true;
                boolean flag4 = true;
                double d0 = 999.0;
                double d2 = 999.0;
                double d3 = 999.0;
                if (x2 > x1) {
                    d0 = x1 + 1.0;
                }
                else if (x2 < x1) {
                    d0 = x1 + 0.0;
                }
                else {
                    flag2 = false;
                }
                if (y2 > y1) {
                    d2 = y1 + 1.0;
                }
                else if (y2 < y1) {
                    d2 = y1 + 0.0;
                }
                else {
                    flag3 = false;
                }
                if (z2 > z1) {
                    d3 = z1 + 1.0;
                }
                else if (z2 < z1) {
                    d3 = z1 + 0.0;
                }
                else {
                    flag4 = false;
                }
                double d4 = 999.0;
                double d5 = 999.0;
                double d6 = 999.0;
                final double d7 = vec32.x - vec31.x;
                final double d8 = vec32.y - vec31.y;
                final double d9 = vec32.z - vec31.z;
                if (flag2) {
                    d4 = (d0 - vec31.x) / d7;
                }
                if (flag3) {
                    d5 = (d2 - vec31.y) / d8;
                }
                if (flag4) {
                    d6 = (d3 - vec31.z) / d9;
                }
                if (d4 == -0.0) {
                    d4 = -1.0E-4;
                }
                if (d5 == -0.0) {
                    d5 = -1.0E-4;
                }
                if (d6 == -0.0) {
                    d6 = -1.0E-4;
                }
                EnumFacing enumfacing;
                if (d4 < d5 && d4 < d6) {
                    enumfacing = ((x2 > x1) ? EnumFacing.WEST : EnumFacing.EAST);
                    vec31 = new Vec3d(d0, vec31.y + d8 * d4, vec31.z + d9 * d4);
                }
                else if (d5 < d6) {
                    enumfacing = ((y2 > y1) ? EnumFacing.DOWN : EnumFacing.UP);
                    vec31 = new Vec3d(vec31.x + d7 * d5, d2, vec31.z + d9 * d5);
                }
                else {
                    enumfacing = ((z2 > z1) ? EnumFacing.NORTH : EnumFacing.SOUTH);
                    vec31 = new Vec3d(vec31.x + d7 * d6, vec31.y + d8 * d6, d3);
                }
                x1 = MathHelper.floor(vec31.x) - ((enumfacing == EnumFacing.EAST) ? 1 : 0);
                y1 = MathHelper.floor(vec31.y) - ((enumfacing == EnumFacing.UP) ? 1 : 0);
                z1 = MathHelper.floor(vec31.z) - ((enumfacing == EnumFacing.SOUTH) ? 1 : 0);
                pos = new BlockPos(x1, y1, z1);
                final IBlockState state2 = AutoCrystal.mc.world.getBlockState(pos);
                final Block block2 = state2.getBlock();
                if (ignoreNoBox && state2.getMaterial() != Material.PORTAL && state2.getCollisionBoundingBox((IBlockAccess)AutoCrystal.mc.world, pos) == Block.NULL_AABB) {
                    continue;
                }
                if (block2.canCollideCheck(state2, stopOnLiquid) && (!ignoreWebs || !(block2 instanceof BlockWeb))) {
                    final RayTraceResult raytraceresult3 = state2.collisionRayTrace((World)AutoCrystal.mc.world, pos, vec31, vec32);
                    if (raytraceresult3 != null) {
                        return raytraceresult3;
                    }
                    continue;
                }
                else {
                    raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, pos);
                }
            }
            return returnLastUncollidableBlock ? raytraceresult2 : null;
        }
        return null;
    }

    private float getBlastReduction(final EntityLivingBase entity, final float damageI, final Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            }
            catch (Exception ex) {}
            final float f = MathHelper.clamp((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static Entity getPredictedPosition(Entity entity, double x) {
        if (x == 0.0) {
            return entity;
        }
        EntityPlayer e = null;
        double mX = entity.posX - entity.lastTickPosX;
        double mY = entity.posY - entity.lastTickPosY;
        double mZ = entity.posZ - entity.lastTickPosZ;
        boolean shouldPredict = false;
        boolean shouldStrafe = false;
        double motion = Math.sqrt(Math.pow(mX, 2.0) + Math.pow(mZ, 2.0) + Math.pow(mY, 2.0));
        if (motion > 0.1) {
            shouldPredict = true;
        }
        if (!shouldPredict) {
            return entity;
        }
        if (motion > 0.31) {
            shouldStrafe = true;
        }
        for (int i = 0; i < x; ++i) {
            if (e == null) {
                if (isOnGround(0.0, 0.0, 0.0, entity)) {
                    mY = (shouldStrafe ? 0.4 : -0.07840015258789);
                } else {
                    mY -= 0.08;
                    mY *= 0.9800000190734863;
                }
                e = placeValue(mX, mY, mZ, (EntityPlayer)entity);
            } else {
                if (isOnGround(0.0, 0.0, 0.0, e)) {
                    mY = (shouldStrafe ? 0.4 : -0.07840015258789);
                } else {
                    mY -= 0.08;
                    mY *= 0.9800000190734863;
                }
                e = placeValue(mX, mY, mZ, e);
            }
        }
        return e;
    }

    public static boolean isOnGround(double x, double y, double z, Entity entity) {
        try {
            double d3 = y;
            List<AxisAlignedBB> list1 = mc.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().expand(x, y, z));
            if (y != 0.0) {
                for (int k = 0, l = list1.size(); k < l; ++k) {
                    y = list1.get(k).calculateYOffset(entity.getEntityBoundingBox(), y);
                }
            }
            return d3 != y && d3 < 0.0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static EntityPlayer placeValue(double x, double y, double z, final EntityPlayer entity) {
        final List<AxisAlignedBB> list1 = mc.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().expand(x, y, z));
        if (y != 0.0) {
            for (int k = 0, l = list1.size(); k < l; ++k) {
                y = list1.get(k).calculateYOffset(entity.getEntityBoundingBox(), y);
            }
            if (y != 0.0) {
                entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0, y, 0.0));
            }
        }
        if (x != 0.0) {
            for (int j5 = 0, l2 = list1.size(); j5 < l2; ++j5) {
                x = calculateXOffset(entity.getEntityBoundingBox(), x, list1.get(j5));
            }
            if (x != 0.0) {
                entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(x, 0.0, 0.0));
            }
        }
        if (z != 0.0) {
            for (int k2 = 0, i6 = list1.size(); k2 < i6; ++k2) {
                z = calculateZOffset(entity.getEntityBoundingBox(), z, list1.get(k2));
            }
            if (z != 0.0) {
                entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0, 0.0, z));
            }
        }
        return entity;
    }

    public static double calculateXOffset(final AxisAlignedBB other, double OffsetX, final AxisAlignedBB this1) {
        if (other.maxY > this1.minY && other.minY < this1.maxY && other.maxZ > this1.minZ && other.minZ < this1.maxZ) {
            if (OffsetX > 0.0 && other.maxX <= this1.minX) {
                final double d1 = this1.minX - 0.3 - other.maxX;
                if (d1 < OffsetX) {
                    OffsetX = d1;
                }
            }
            else if (OffsetX < 0.0 && other.minX >= this1.maxX) {
                final double d2 = this1.maxX + 0.3 - other.minX;
                if (d2 > OffsetX) {
                    OffsetX = d2;
                }
            }
        }
        return OffsetX;
    }

    public static double calculateZOffset(final AxisAlignedBB other, double OffsetZ, final AxisAlignedBB this1) {
        if (other.maxX > this1.minX && other.minX < this1.maxX && other.maxY > this1.minY && other.minY < this1.maxY) {
            if (OffsetZ > 0.0 && other.maxZ <= this1.minZ) {
                final double d1 = this1.minZ - 0.3 - other.maxZ;
                if (d1 < OffsetZ) {
                    OffsetZ = d1;
                }
            }
            else if (OffsetZ < 0.0 && other.minZ >= this1.maxZ) {
                final double d2 = this1.maxZ + 0.3 - other.minZ;
                if (d2 > OffsetZ) {
                    OffsetZ = d2;
                }
            }
        }
        return OffsetZ;
    }

    private float getDamageMultiplied(final float damage) {
        final int diff = AutoCrystal.mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    private enum Page {Place, Break, Misc, Render}

    public enum SwingMode {MainHand, OffHand, None}

    public enum SwapMode {Off, Normal, Silent}

    public static class CalculationThread extends Thread {
        @Override
        public void run() {
            while (AutoCrystal.getInstance().isEnabled()) {
                try {
                    AutoCrystal.getInstance().doCalculations();
                    TimeUnit.MILLISECONDS.sleep(50L);
                }
                catch (Exception ex) {}
            }
        }
    }
}