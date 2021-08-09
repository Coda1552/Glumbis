package coda.glumbis.init;

import coda.glumbis.Glumbis;
import coda.glumbis.items.GlumbisItem;
import coda.glumbis.items.SocksArmorItem;
import coda.glumbis.items.SoggySocksArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class GlumbisItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Glumbis.MOD_ID);

    public static final RegistryObject<Item> SOCK = ITEMS.register("sock", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).stacksTo(1)));
    public static final RegistryObject<Item> GLUMBIS = ITEMS.register("glumbis", () -> new GlumbisItem(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS).stacksTo(1)));
    public static final RegistryObject<Item> SOCKS = ITEMS.register("socks", () -> new SocksArmorItem(EquipmentSlot.FEET));
    public static final RegistryObject<Item> SOGGY_SOCKS = ITEMS.register("soggy_socks", () -> new SoggySocksArmorItem(EquipmentSlot.FEET));

}
