package com.minecraftabnormals.abnormals_delight.core.other;

import com.google.common.collect.Lists;
import com.minecraftabnormals.abnormals_delight.core.AbnormalsDelight;
import com.minecraftabnormals.abnormals_delight.core.registry.ADItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.utils.tags.ModTags;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = AbnormalsDelight.MOD_ID)
public class ADEvents {
	public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 9);
	public static final List<Supplier<Item>> CAKES = Util.make(Lists.newArrayList(), (list) -> {
		list.add(ADItems.VANILLA_CAKE_SLICE);
		list.add(ADItems.CHOCOLATE_CAKE_SLICE);
		list.add(ADItems.STRAWBERRY_CAKE_SLICE);
		list.add(ADItems.BANANA_CAKE_SLICE);
		list.add(ADItems.STRAWBERRY_CAKE_SLICE);
		list.add(ADItems.BANANA_CAKE_SLICE);
	});

	@SubscribeEvent
	public static void onCakeInteraction(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		ItemStack tool = event.getPlayer().getHeldItem(event.getHand());
		ResourceLocation name = state.getBlock().getRegistryName();

		if (ModTags.KNIVES.contains(tool.getItem()) && name != null) {
			for (Supplier<Item> item : CAKES) {
				ResourceLocation cakeName = item.get().getRegistryName();
				if (cakeName != null && cakeName.equals(name)) {
					int bites = state.get(CakeBlock.BITES);
					if (bites < 6) {
						world.setBlockState(pos, state.with(CakeBlock.BITES, bites + 1), 3);
					} else {
						world.removeBlock(pos, false);
					}
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(item.get()));
					world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F);

					event.setCancellationResult(ActionResultType.SUCCESS);
					event.setCanceled(true);
				}
			}

			if (name.equals(ADItems.YUCCA_GATEAU_SLICE.get().getRegistryName())) {
				int bites = state.get(BITES);
				if (bites < 9) {
					world.setBlockState(pos, state.with(BITES, bites + 1), 3);
				} else {
					world.removeBlock(pos, false);
				}
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ADItems.YUCCA_GATEAU_SLICE.get()));
				world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F);

				event.setCancellationResult(ActionResultType.SUCCESS);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		BlockState state = event.getState();
		PlayerEntity player = event.getPlayer();
		List<ItemStack> loot = Lists.newArrayList();
		if (state != null && player.getHeldItemMainhand().getItem().isIn(ModTags.KNIVES)) {
			ResourceLocation name = state.getBlock().getRegistryName();

			if (name != null) {
				for (Supplier<Item> item : CAKES) {
					ResourceLocation cakeName = item.get().getRegistryName();
					if (cakeName != null && cakeName.equals(name)) {
						loot.add(new ItemStack(item.get(), 7 - state.get(CakeBlock.BITES)));
					}
				}

				if (name.equals(ADItems.YUCCA_GATEAU_SLICE.get().getRegistryName())) {
					loot.add(new ItemStack(ADItems.YUCCA_GATEAU_SLICE.get(), 10 - state.get(BITES)));
				}

				if (!loot.isEmpty() && event.getWorld() instanceof World) {
					for (ItemStack stack : loot) {
						Block.spawnAsEntity((World) event.getWorld(), event.getPos(), stack);
					}
				}
			}
		}
	}
}
