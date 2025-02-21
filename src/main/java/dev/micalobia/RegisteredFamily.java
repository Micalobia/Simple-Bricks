package dev.micalobia;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

import static dev.micalobia.SimpleBricks.id;

public class RegisteredFamily {
    private final Block base;
    private final String root;
    private final BlockFamily.Builder builder;

    public RegisteredFamily(Block base, String root) {
        this.base = base;
        this.root = root;
        this.builder = new BlockFamily.Builder(base);
    }

    @Contract(" -> this")
    public RegisteredFamily fence() {
        builder.fence(register(FenceBlock::new, root + "_fence"));
        return this;
    }

    @Contract(" -> this")
    public RegisteredFamily slab() {
        builder.slab(register(SlabBlock::new, root + "_slab"));
        return this;
    }

    @Contract(" -> this")
    public RegisteredFamily stairs() {
        builder.stairs(register(s -> new StairsBlock(base.getDefaultState(), s), root + "_stairs"));
        return this;
    }

    @Contract(" -> this")
    public RegisteredFamily wall() {
        builder.wall(register(WallBlock::new, root + "_wall"));
        return this;
    }

    public BlockFamily build() {
        return this.builder.build();
    }

    private <T extends Block> T register(Function<AbstractBlock.Settings, T> constructor, String path) {
        var key = RegistryKey.of(RegistryKeys.BLOCK, id(path));
        var settings = AbstractBlock.Settings.copy(base).registryKey(key);
        var block = constructor.apply(settings);
        Registry.register(Registries.BLOCK, key, block);
        Items.register(block);
        ItemGroupEvents.modifyEntriesEvent(SimpleBricks.ITEM_GROUP_KEY).register(x -> x.add(block));
        return block;
    }
}
