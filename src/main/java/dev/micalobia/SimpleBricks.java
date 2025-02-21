package dev.micalobia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleBricks implements ModInitializer {
    public static final String MODID = "simple_bricks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static Map<Block, BlockFamily> FAMILIES = new LinkedHashMap<>();
    public static Map<Block, Block> SUBFAMILIES = new HashMap<>();
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), id("simple_bricks"));
    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(Blocks.QUARTZ_BRICKS)).displayName(Text.translatable("simple_bricks.simple_bricks")).build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, ITEM_GROUP);
        registerFamily(Blocks.STONE, "stone", RegisteredFamily::wall);
        registerFamily(Blocks.QUARTZ_BLOCK, "quartz_block", RegisteredFamily::wall);
        registerFamily(Blocks.SMOOTH_QUARTZ, "smooth_quartz", RegisteredFamily::wall);
        registerFamily(Blocks.PRISMARINE_BRICKS, "prismarine_brick", RegisteredFamily::wall);
        registerFamily(Blocks.DARK_PRISMARINE, "dark_prismarine", RegisteredFamily::wall);
        registerFamily(Blocks.SMOOTH_SANDSTONE, "smooth_sandstone", RegisteredFamily::wall);
        registerFamily(Blocks.SMOOTH_RED_SANDSTONE, "smooth_red_sandstone", RegisteredFamily::wall);
        registerFamily(Blocks.POLISHED_ANDESITE, "polished_andesite", RegisteredFamily::wall);
        registerFamily(Blocks.POLISHED_DIORITE, "polished_diorite", RegisteredFamily::wall);
        registerFamily(Blocks.POLISHED_GRANITE, "polished_granite", RegisteredFamily::wall);
        registerFamily(Blocks.PURPUR_BLOCK, "purpur", RegisteredFamily::wall);
        registerFamily(Blocks.CALCITE, "calcite", builder -> builder.slab().stairs().wall());
        registerFamily(Blocks.SMOOTH_BASALT, "smooth_basalt", builder -> builder.slab().stairs().wall());
        registerFamily(Blocks.QUARTZ_BRICKS, "quartz_brick", builder -> builder.slab().stairs().wall());
        registerFamily(Blocks.END_STONE, "end_stone", builder -> builder.slab().stairs().wall());
        registerFamily(Blocks.RED_NETHER_BRICKS, "red_nether_brick", RegisteredFamily::fence);

        SUBFAMILIES.put(Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_BLOCK);
        SUBFAMILIES.put(Blocks.SMOOTH_QUARTZ, Blocks.QUARTZ_BLOCK);
        SUBFAMILIES.put(Blocks.POLISHED_ANDESITE, Blocks.ANDESITE);
        SUBFAMILIES.put(Blocks.POLISHED_DIORITE, Blocks.DIORITE);
        SUBFAMILIES.put(Blocks.POLISHED_GRANITE, Blocks.GRANITE);
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    private void registerFamily(Block base, String root, Consumer<RegisteredFamily> consumer) {
        var wrapper = new RegisteredFamily(base, root);
        consumer.accept(wrapper);
        var family = wrapper.build();
        FAMILIES.put(base, family);
    }
}