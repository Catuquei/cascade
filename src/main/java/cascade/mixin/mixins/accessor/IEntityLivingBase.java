package cascade.mixin.mixins.accessor;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLivingBase.class)
public interface IEntityLivingBase {

    @Accessor(value = "ticksSinceLastSwing")
    int getTicksSinceLastSwing();

    @Accessor(value = "activeItemStackUseCount")
    int getActiveItemStackUseCount();

    @Accessor(value = "ticksSinceLastSwing")
    void setTicksSinceLastSwing(int ticks);

    @Accessor(value = "activeItemStackUseCount")
    void setActiveItemStackUseCount(int count);

}