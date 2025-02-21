package dev.micalobia;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class SimpleBricksDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();

        pack.addProvider(ModelProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(LanguageProvider::new);
        pack.addProvider(RecipeProvider::new);
    }

    private static class ModelProvider extends FabricModelProvider {

        public ModelProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator generator) {
            for (var family : SimpleBricks.FAMILIES.values()) {
                // Had to do this manually, for some reason quartz blocks are a column
                if (family.getBaseBlock() == Blocks.QUARTZ_BLOCK) {
                    continue;
                }
                generator.registerCubeAllModelTexturePool(family.getBaseBlock()).family(family);
            }
        }

        @Override
        public void generateItemModels(ItemModelGenerator generator) {
        }
    }

    private static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
        TagKey<Block> WALLS = TagKey.of(RegistryKeys.BLOCK, Identifier.ofVanilla("walls"));
        TagKey<Block> FENCES = TagKey.of(RegistryKeys.BLOCK, Identifier.ofVanilla("fences"));

        public BlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            var walls = getOrCreateTagBuilder(WALLS);
            var fences = getOrCreateTagBuilder(FENCES);
            for (var family : SimpleBricks.FAMILIES.values()) {
                var variants = family.getVariants();
                if (variants.containsKey(BlockFamily.Variant.WALL))
                    walls.add(family.getVariant(BlockFamily.Variant.WALL));
                if (variants.containsKey(BlockFamily.Variant.FENCE))
                    fences.add(family.getVariant(BlockFamily.Variant.FENCE));
            }
        }
    }

    private static class LanguageProvider extends FabricLanguageProvider {

        protected LanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapper, TranslationBuilder builder) {
            builder.add("simple_bricks.simple_bricks", "Simple Bricks");
            for (var family : SimpleBricks.FAMILIES.values()) {
                var variants = family.getVariants();
                for (var entry : variants.entrySet()) {
                    var block = entry.getValue();
                    var id = Registries.BLOCK.getId(block);
                    var snake = snakeToTitle(id.getPath());
                    SimpleBricks.LOGGER.warn("{}; {}", snake, block.getTranslationKey());
                    builder.add(block, snake);
                }
            }
        }

        public static String snakeToTitle(String s) {
            String[] words = s.split("_");
            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
            return String.join(" ", words);
        }
    }

    private static class RecipeProvider extends FabricRecipeProvider {
        public RecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
            return new Generator(wrapperLookup, recipeExporter);
        }

        @Override
        public String getName() {
            return "simple_bricks";
        }

        private static final class Generator extends RecipeGenerator {
            private Generator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
                super(registries, exporter);
            }

            @Override
            public void generate() {
                for (var entry : SimpleBricks.FAMILIES.entrySet()) {
                    var family = entry.getValue();
                    var current = entry.getKey();
                    boolean hasParent;
                    do {
                        generate(current, family);
                        hasParent = SimpleBricks.SUBFAMILIES.containsKey(current);
                        if (hasParent) current = SimpleBricks.SUBFAMILIES.get(current);
                    } while (hasParent);
                }
            }

            private void generate(Block input, BlockFamily output) {
                SimpleBricks.LOGGER.info("{}, {}", input, output.getBaseBlock());
                boolean isRoot = input == output.getBaseBlock();
                for (var entry : output.getVariants().entrySet()) {
                    var variant = entry.getKey();
                    var block = entry.getValue();
                    switch (variant) {
                        case SLAB -> {
                            if (isRoot) offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, block, input);
                            offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, block, input, 2);
                        }
                        case STAIRS -> {
                            if (isRoot)
                                createStairsRecipe(block, Ingredient.ofItem(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
                            offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, block, input);
                        }
                        case WALL -> {
                            if (isRoot) offerWallRecipe(RecipeCategory.BUILDING_BLOCKS, block, input);
                            offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, block, input);
                        }
                        default -> {
                        }
                    }
                }
            }
        }
    }
}
