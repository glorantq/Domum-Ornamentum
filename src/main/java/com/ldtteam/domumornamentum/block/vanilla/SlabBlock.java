package com.ldtteam.domumornamentum.block.vanilla;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.ldtteam.domumornamentum.block.AbstractBlockSlab;
import com.ldtteam.domumornamentum.block.ICachedItemGroupBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.block.components.SimpleRetexturableComponent;
import com.ldtteam.domumornamentum.entity.block.MateriallyTexturedBlockEntity;
import com.ldtteam.domumornamentum.recipe.ModRecipeSerializers;
import com.ldtteam.domumornamentum.tag.ModTags;
import com.ldtteam.domumornamentum.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static net.minecraft.world.level.block.Blocks.OAK_PLANKS;

public class SlabBlock extends AbstractBlockSlab<SlabBlock> implements IMateriallyTexturedBlock, EntityBlock, ICachedItemGroupBlock
{
    public static final List<IMateriallyTexturedBlockComponent> COMPONENTS = ImmutableList.<IMateriallyTexturedBlockComponent>builder()
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("minecraft:block/oak_planks"), ModTags.SLAB_MATERIALS, OAK_PLANKS))
                                                                               .build();

    private final List<ItemStack> fillItemGroupCache = Lists.newArrayList();

    public SlabBlock()
    {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().strength(2.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.WOOD));
    }


    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MateriallyTexturedBlockEntity mtbe) {
            Block block = mtbe.getTextureData().getTexturedComponents().get(COMPONENTS.get(0).getId());
            if (block != null)
            {
                return block.getExplosionResistance(state, level, pos, explosion);
            }
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MateriallyTexturedBlockEntity mtbe) {
            Block block = mtbe.getTextureData().getTexturedComponents().get(COMPONENTS.get(0).getId());
            if (block != null)
            {
                return block.getDestroyProgress(block.defaultBlockState(), player, level, pos);
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public @NotNull List<IMateriallyTexturedBlockComponent> getComponents()
    {
        return COMPONENTS;
    }

    @Override
    public void fillItemCategory(final @NotNull NonNullList<ItemStack> items)
    {
        if (!fillItemGroupCache.isEmpty()) {
            items.addAll(fillItemGroupCache);
            return;
        }

        try {
            final ItemStack result = new ItemStack(this);

            fillItemGroupCache.add(result);
        } catch (IllegalStateException exception)
        {
            //Ignored. Thrown during start up.
        }

        items.addAll(fillItemGroupCache);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final @NotNull BlockPos blockPos, final @NotNull BlockState blockState)
    {
        return new MateriallyTexturedBlockEntity(blockPos, blockState);
    }

    @Override
    public void resetCache()
    {
        fillItemGroupCache.clear();
    }



    @Override
    public @NotNull List<ItemStack> getDrops(final @NotNull BlockState state, final @NotNull LootParams.Builder builder)
    {
        return BlockUtils.getMaterializedDrops(builder);
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final BlockGetter world, final BlockPos pos, final Player player)
    {
        return BlockUtils.getMaterializedItemStack(world.getBlockEntity(pos));
    }

    @Override
    public @NotNull Block getBlock()
    {
        return this;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MateriallyTexturedBlockEntity mtbe) {
            Block block = mtbe.getTextureData().getTexturedComponents().get(COMPONENTS.get(0).getId());
            if (block != null)
            {
                return block.getSoundType(state, level, pos, entity);
            }
        }
        return super.getSoundType(state, level, pos, entity);
    }

    @NotNull
    public Collection<FinishedRecipe> getValidCutterRecipes() {
        return Lists.newArrayList(
          new FinishedRecipe() {
              @Override
              public void serializeRecipeData(final @NotNull JsonObject json)
              {
                  json.addProperty("count", COMPONENTS.size() * 2);
              }

              @Override
              public @NotNull ResourceLocation getId()
              {
                  return Objects.requireNonNull(getRegistryName(getBlock()));
              }

              @Override
              public @NotNull RecipeSerializer<?> getType()
              {
                  return ModRecipeSerializers.ARCHITECTS_CUTTER.get();
              }

              @Nullable
              @Override
              public JsonObject serializeAdvancement()
              {
                  return null;
              }

              @Nullable
              @Override
              public ResourceLocation getAdvancementId()
              {
                  return null;
              }
          }
        );
    }
}
