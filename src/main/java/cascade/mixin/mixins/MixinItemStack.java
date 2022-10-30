package cascade.mixin.mixins;

import cascade.features.modules.misc.TrueDurability;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ ItemStack.class })
public abstract class MixinItemStack {

    private int itemDamage;

    @Inject(method = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    @Dynamic
    private void initHook(Item item, int ilove0x22, int dura, NBTTagCompound compound, CallbackInfo info) {
        itemDamage = checkDurability(ItemStack.class.cast(this), this.itemDamage, dura);
    }

    @Inject(method = { "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V" }, at = { @At("RETURN") })
    private void initHook2(NBTTagCompound compound, CallbackInfo info) {
        itemDamage = checkDurability(ItemStack.class.cast(this), itemDamage, compound.getShort("Damage"));
    }

    private int checkDurability(ItemStack item, int damage, int meta) {
        int trueDura = damage;
        if (TrueDurability.getInstance().isEnabled() && meta < 0) {
            trueDura = meta;
        }
        return trueDura;
    }
}