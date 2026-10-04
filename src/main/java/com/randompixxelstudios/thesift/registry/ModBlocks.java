package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.block.SiftAltarBlock;
import com.randompixxelstudios.thesift.block.SiftPortalBlock;
import com.randompixxelstudios.thesift.block.SiftSoilBlock;
import com.randompixxelstudios.thesift.block.SiftThornsBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
	/** Blocks that get a matching BlockItem, in creative-tab order. */
	public static final List<Block> BLOCKS_WITH_ITEMS = new ArrayList<>();

	public static final ResourceKey<Feature> SIFT_WILLOW_TREE = ResourceKey.create(Registries.FEATURE, TheSift.id("sift_willow"));
	public static final TreeGrower SIFT_WILLOW_GROWER = new TreeGrower(
		"thesift_sift_willow", WeightedList.of(SIFT_WILLOW_TREE), WeightedList.of(), WeightedList.of(), SIFT_WILLOW_TREE
	);
	public static final ResourceKey<Feature> DUSKWOOD_TREE = ResourceKey.create(Registries.FEATURE, TheSift.id("duskwood"));
	public static final ResourceKey<Feature> MEGA_DUSKWOOD_TREE = ResourceKey.create(Registries.FEATURE, TheSift.id("mega_duskwood"));
	public static final TreeGrower DUSKWOOD_GROWER = new TreeGrower(
		"thesift_duskwood", WeightedList.of(DUSKWOOD_TREE), WeightedList.of(MEGA_DUSKWOOD_TREE), WeightedList.of(), DUSKWOOD_TREE
	);
	public static final ResourceKey<Feature> PEARLBARK_TREE = ResourceKey.create(Registries.FEATURE, TheSift.id("pearlbark"));
	public static final TreeGrower PEARLBARK_GROWER = new TreeGrower(
		"thesift_pearlbark", WeightedList.of(PEARLBARK_TREE), WeightedList.of(), WeightedList.of(), PEARLBARK_TREE
	);

	// --- Portal & fluid -------------------------------------------------------------------
	public static final Block SIFT_PORTAL = register(
		"sift_portal",
		SiftPortalBlock::new,
		BlockBehaviour.Properties.of()
			.noCollision()
			.strength(-1.0F)
			.sound(SoundType.GLASS)
			.lightLevel(state -> 12)
			.pushReaction(PushReaction.IMMOVEABLE)
			.noLootTable(),
		false
	);
	public static final Block ICHOR = register(
		"ichor",
		p -> new LiquidBlock(ModFluids.ICHOR, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_MAGENTA)
			.replaceable()
			.noCollision()
			.strength(100.0F)
			.lightLevel(state -> 7)
			.pushReaction(PushReaction.POPPED)
			.noLootTable()
			.liquid()
			.sound(SoundType.EMPTY),
		false
	);

	// --- Sift stone (the mauve cliffs) ----------------------------------------------------------
	public static final Block SIFT_STONE = register("sift_stone", stone(MapColor.TERRACOTTA_PURPLE, 1.5F, 6.0F));
	public static final Block COBBLED_SIFT_STONE = register("cobbled_sift_stone", stone(MapColor.TERRACOTTA_PURPLE, 2.0F, 6.0F));
	public static final Block POLISHED_SIFT_STONE = register("polished_sift_stone", stone(MapColor.TERRACOTTA_PURPLE, 1.5F, 6.0F));
	public static final Block SIFT_STONE_BRICKS = register("sift_stone_bricks", stone(MapColor.TERRACOTTA_PURPLE, 1.5F, 6.0F));
	public static final Block SIFT_STONE_BRICK_STAIRS = register(
		"sift_stone_brick_stairs", p -> new StairBlock(SIFT_STONE_BRICKS.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(SIFT_STONE_BRICKS)
	);
	public static final Block SIFT_STONE_BRICK_SLAB = register("sift_stone_brick_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SIFT_STONE_BRICKS));
	public static final Block SIFT_STONE_BRICK_WALL = register(
		"sift_stone_brick_wall", WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SIFT_STONE_BRICKS).forceSolidOn()
	);
	public static final Block CHISELED_SIFT_STONE_BRICKS = register("chiseled_sift_stone_bricks", stone(MapColor.TERRACOTTA_PURPLE, 1.5F, 6.0F));

	// --- Singer's Meadow ---------------------------------------------------------------------
	public static final Block MEADOW_ROCK = register(
		"meadow_rock",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.2F, 4.0F)
	);
	public static final Block BLUSHING_MOSS = register(
		"blushing_moss",
		p -> new SiftSoilBlock(MEADOW_ROCK, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.8F).sound(SoundType.MOSS).randomTicks()
	);

	// --- Lullaby Hills -----------------------------------------------------------------------
	public static final Block GLOWMOSS = register(
		"glowmoss",
		p -> new SiftSoilBlock(SIFT_STONE, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.8F).sound(SoundType.MOSS).randomTicks().lightLevel(state -> 3)
	);

	// --- The Carapace ------------------------------------------------------------------------
	public static final Block ORANGE_CARAPACE = register("orange_carapace", stone(MapColor.COLOR_ORANGE, 1.25F, 4.2F));
	public static final Block RED_CARAPACE = register("red_carapace", stone(MapColor.COLOR_RED, 1.25F, 4.2F));
	public static final Block BLUE_CARAPACE = register("blue_carapace", stone(MapColor.COLOR_BLUE, 1.25F, 4.2F));
	public static final Block POLISHED_CARAPACE = register("polished_carapace", stone(MapColor.COLOR_ORANGE, 1.5F, 6.0F));
	public static final Block CARAPACE_SAND = register(
		"carapace_sand", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND)
	);

	// --- Sift Willow wood set ------------------------------------------------------------------
	public static final Block SIFT_WILLOW_LOG = register(
		"sift_willow_log", RotatedPillarBlock::new, Blocks.logProperties(MapColor.COLOR_LIGHT_BLUE, MapColor.TERRACOTTA_BLUE, SoundType.CHERRY_WOOD)
	);
	public static final Block SIFT_WILLOW_WOOD = register(
		"sift_willow_wood", RotatedPillarBlock::new, Blocks.logProperties(MapColor.TERRACOTTA_BLUE, MapColor.TERRACOTTA_BLUE, SoundType.CHERRY_WOOD)
	);
	public static final Block STRIPPED_SIFT_WILLOW_LOG = register(
		"stripped_sift_willow_log", RotatedPillarBlock::new, Blocks.logProperties(MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_LIGHT_BLUE, SoundType.CHERRY_WOOD)
	);
	public static final Block STRIPPED_SIFT_WILLOW_WOOD = register(
		"stripped_sift_willow_wood", RotatedPillarBlock::new, Blocks.logProperties(MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_LIGHT_BLUE, SoundType.CHERRY_WOOD)
	);
	public static final Block SIFT_WILLOW_PLANKS = register(
		"sift_willow_planks",
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.0F, 3.0F)
			.sound(SoundType.CHERRY_WOOD)
			.ignitedByLava()
	);
	public static final Block SIFT_WILLOW_LEAVES = register(
		"sift_willow_leaves",
		p -> new UntintedParticleLeavesBlock(0.02F, ParticleTypes.WHITE_ASH, AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
		Blocks.leavesProperties(SoundType.CHERRY_LEAVES).mapColor(MapColor.SNOW)
	);
	public static final Block SIFT_WILLOW_SAPLING = register(
		"sift_willow_sapling",
		p -> new SaplingBlock(SIFT_WILLOW_GROWER, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.noCollision()
			.randomTicks()
			.instabreak()
			.sound(SoundType.CHERRY_SAPLING)
			.pushReaction(PushReaction.POPPED)
	);
	public static final Block SIFT_WILLOW_STAIRS = register(
		"sift_willow_stairs", p -> new StairBlock(SIFT_WILLOW_PLANKS.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(SIFT_WILLOW_PLANKS)
	);
	public static final Block SIFT_WILLOW_SLAB = register("sift_willow_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SIFT_WILLOW_PLANKS));
	public static final Block SIFT_WILLOW_FENCE = register("sift_willow_fence", FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(SIFT_WILLOW_PLANKS));
	public static final Block SIFT_WILLOW_FENCE_GATE = register(
		"sift_willow_fence_gate", p -> new FenceGateBlock(WoodType.CHERRY, p), BlockBehaviour.Properties.ofFullCopy(SIFT_WILLOW_PLANKS).forceSolidOn()
	);
	public static final Block SIFT_WILLOW_DOOR = register(
		"sift_willow_door",
		p -> new DoorBlock(BlockSetType.CHERRY, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.ignitedByLava()
			.pushReaction(PushReaction.POPPED)
	);
	public static final Block SIFT_WILLOW_TRAPDOOR = register(
		"sift_willow_trapdoor",
		p -> new TrapDoorBlock(BlockSetType.CHERRY, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.instrument(NoteBlockInstrument.BASS)
			.strength(3.0F)
			.noOcclusion()
			.isValidSpawn(Blocks::never)
			.ignitedByLava()
	);
	public static final Block SIFT_WILLOW_BUTTON = register(
		"sift_willow_button", p -> new ButtonBlock(BlockSetType.CHERRY, 30, p), Blocks.buttonProperties()
	);
	public static final Block SIFT_WILLOW_PRESSURE_PLATE = register(
		"sift_willow_pressure_plate",
		p -> new PressurePlateBlock(BlockSetType.CHERRY, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.forceSolidOn()
			.instrument(NoteBlockInstrument.BASS)
			.noCollision()
			.strength(0.5F)
			.ignitedByLava()
			.pushReaction(PushReaction.POPPED)
	);

	// --- Flora --------------------------------------------------------------------------------
	public static final Block BLUSH_GRASS = register("blush_grass", TallGrassBlock::new, plant(MapColor.COLOR_PINK).replaceable().offsetType(BlockBehaviour.OffsetType.XYZ));
	public static final Block TALL_BLUSH_GRASS = register(
		"tall_blush_grass", DoublePlantBlock::new, plant(MapColor.COLOR_PINK).replaceable().offsetType(BlockBehaviour.OffsetType.XZ)
	);
	public static final Block GLOW_SPROUTS = register(
		"glow_sprouts", TallGrassBlock::new, plant(MapColor.COLOR_CYAN).replaceable().lightLevel(state -> 6).offsetType(BlockBehaviour.OffsetType.XZ)
	);
	public static final Block LULLABY_BLOOM = register(
		"lullaby_bloom", p -> new FlowerBlock(MobEffects.REGENERATION, 5.0F, p), plant(MapColor.COLOR_YELLOW).offsetType(BlockBehaviour.OffsetType.XZ)
	);
	public static final Block ECHO_BLOOM = register(
		"echo_bloom",
		p -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, p),
		plant(MapColor.COLOR_LIGHT_BLUE).lightLevel(state -> 5).offsetType(BlockBehaviour.OffsetType.XZ)
	);
	public static final Block SIFT_THORNS = register(
		"sift_thorns", SiftThornsBlock::new, plant(MapColor.COLOR_MAGENTA).sound(SoundType.SWEET_BERRY_BUSH).offsetType(BlockBehaviour.OffsetType.XZ)
	);

	// --- Soul crystals ------------------------------------------------------------------------
	public static final Block SOUL_CRYSTAL_BLOCK = register(
		"soul_crystal_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.5F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().lightLevel(state -> 6)
	);
	public static final Block SOUL_CRYSTAL_CLUSTER = register(
		"soul_crystal_cluster",
		p -> new AmethystClusterBlock(7.0F, 10.0F, p),
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_LIGHT_BLUE)
			.forceSolidOn()
			.noOcclusion()
			.sound(SoundType.AMETHYST_CLUSTER)
			.strength(1.5F)
			.lightLevel(state -> 10)
			.pushReaction(PushReaction.POPPED)
	);

	// --- Siftite (the Sift's endgame metal) -----------------------------------------------------
	public static final Block SIFTITE_ORE = register(
		"siftite_ore",
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 6.0F)
	);
	public static final Block RAW_SIFTITE_BLOCK = register(
		"raw_siftite_block", stone(MapColor.COLOR_CYAN, 5.0F, 6.0F)
	);
	public static final Block SIFTITE_BLOCK = register(
		"siftite_block",
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).requiresCorrectToolForDrops().strength(6.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK)
	);

	// --- Building blocks ---------------------------------------------------------------------
	public static final Block MEADOW_ROCK_BRICKS = register("meadow_rock_bricks", stone(MapColor.COLOR_RED, 1.5F, 6.0F));
	public static final Block CARAPACE_BRICKS = register("carapace_bricks", stone(MapColor.COLOR_ORANGE, 1.5F, 6.0F));
	public static final Block SIFT_LANTERN = register(
		"sift_lantern",
		LanternBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.forceSolidOn()
			.strength(3.5F)
			.sound(SoundType.LANTERN)
			.lightLevel(state -> 15)
			.noOcclusion()
			.pushReaction(PushReaction.POPPED)
	);

	// --- Verdant Isles (floating islands above the ichor sea) ------------------------------------
	public static final Block RUST_SOIL = register(
		"rust_soil", BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).strength(0.5F).sound(SoundType.ROOTED_DIRT)
	);
	public static final Block VERDANT_TURF = register(
		"verdant_turf",
		p -> new SiftSoilBlock(RUST_SOIL, p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.6F).sound(SoundType.GRASS).randomTicks()
	);
	public static final Block GLIMMER_PETALS = register(
		"glimmer_petals",
		p -> new FlowerBedBlock(p, 3),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).noCollision().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.POPPED)
	);
	public static final Block BLUEBELL = register(
		"bluebell", p -> new FlowerBlock(MobEffects.SPEED, 5.0F, p), plant(MapColor.COLOR_BLUE).lightLevel(state -> 4).offsetType(BlockBehaviour.OffsetType.XZ)
	);
	public static final Block PALE_LILY = register(
		"pale_lily", p -> new FlowerBlock(MobEffects.SLOW_FALLING, 5.0F, p), plant(MapColor.SNOW).offsetType(BlockBehaviour.OffsetType.XZ)
	);

	public static final Block MINT_GRASS = register(
		"mint_grass", TallGrassBlock::new, plant(MapColor.COLOR_LIGHT_GREEN).replaceable().offsetType(BlockBehaviour.OffsetType.XYZ)
	);
	public static final Block TALL_MINT_GRASS = register(
		"tall_mint_grass", DoublePlantBlock::new, plant(MapColor.COLOR_LIGHT_GREEN).replaceable().offsetType(BlockBehaviour.OffsetType.XZ)
	);

	// --- More stone underground --------------------------------------------------------------
	public static final Block DEEP_SIFT_STONE = register("deep_sift_stone", stone(MapColor.COLOR_PURPLE, 3.0F, 6.0F).sound(SoundType.DEEPSLATE));
	public static final Block COBBLED_DEEP_SIFT_STONE = register("cobbled_deep_sift_stone", stone(MapColor.COLOR_PURPLE, 3.5F, 6.0F).sound(SoundType.DEEPSLATE));
	public static final Block VEILSTONE = register("veilstone", stone(MapColor.COLOR_LIGHT_BLUE, 1.5F, 6.0F).sound(SoundType.TUFF));
	public static final Block POLISHED_VEILSTONE = register("polished_veilstone", stone(MapColor.COLOR_LIGHT_BLUE, 1.5F, 6.0F).sound(SoundType.POLISHED_TUFF));
	public static final Block ROSE_MARBLE = register("rose_marble", stone(MapColor.COLOR_PINK, 1.5F, 6.0F).sound(SoundType.CALCITE));
	public static final Block POLISHED_ROSE_MARBLE = register("polished_rose_marble", stone(MapColor.COLOR_PINK, 1.5F, 6.0F).sound(SoundType.CALCITE));
	public static final Block ROSE_MARBLE_BRICKS = register("rose_marble_bricks", stone(MapColor.COLOR_PINK, 1.5F, 6.0F).sound(SoundType.CALCITE));
	public static final Block SIFT_GRAVEL = register(
		"sift_gravel",
		p -> new ColoredFallingBlock(new ColorRGBA(0xFF8A6A90), p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6F).sound(SoundType.GRAVEL)
	);
	public static final Block GLOWCAP = register(
		"glowcap",
		p -> new net.minecraft.world.level.block.FlowerBlock(MobEffects.GLOWING, 5.0F, p),
		plant(MapColor.COLOR_CYAN).lightLevel(state -> 10).offsetType(BlockBehaviour.OffsetType.XZ)
	);

	// --- New ground cover -----------------------------------------------------------------------
	public static final Block DUSK_TURF = register(
		"dusk_turf", p -> new SiftSoilBlock(RUST_SOIL, p), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.6F).sound(SoundType.GRASS).randomTicks()
	);
	public static final Block PEARL_MOSS = register(
		"pearl_moss", p -> new SiftSoilBlock(RUST_SOIL, p), BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.6F).sound(SoundType.MOSS).randomTicks()
	);
	public static final Block SIFT_SAND = register(
		"sift_sand",
		p -> new ColoredFallingBlock(new ColorRGBA(0xFFC9B4E0), p),
		BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.5F).sound(SoundType.SAND)
	);

	// --- Ores of the Sift ------------------------------------------------------------------------
	public static final Block SIFT_COAL_ORE = register("sift_coal_ore", p -> new DropExperienceBlock(UniformInt.of(0, 2), p), ore(3.0F));
	public static final Block SIFT_COPPER_ORE = register("sift_copper_ore", ore(3.0F));
	public static final Block SIFT_IRON_ORE = register("sift_iron_ore", ore(3.0F));
	public static final Block SIFT_GOLD_ORE = register("sift_gold_ore", ore(3.0F));
	public static final Block SIFT_DIAMOND_ORE = register("sift_diamond_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), ore(3.0F));
	public static final Block LUMENITE_ORE = register("lumenite_ore", p -> new DropExperienceBlock(UniformInt.of(2, 5), p), ore(3.0F).lightLevel(state -> 5));
	public static final Block JADE_ORE = register("jade_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), ore(4.0F));
	public static final Block LUMENITE_BLOCK = register(
		"lumenite_block", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(3.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().lightLevel(state -> 15)
	);
	public static final Block JADE_BLOCK = register(
		"jade_block", BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
	);

	// --- More wood ------------------------------------------------------------------------------
	public static final WoodSet DUSKWOOD = new WoodSet("duskwood", MapColor.COLOR_PURPLE, MapColor.TERRACOTTA_CYAN, DUSKWOOD_GROWER);
	public static final WoodSet PEARLBARK = new WoodSet("pearlbark", MapColor.SNOW, MapColor.COLOR_YELLOW, PEARLBARK_GROWER);

	// --- Shrines & temples ------------------------------------------------------------------------
	public static final Block SIFT_ALTAR = register(
		"sift_altar",
		SiftAltarBlock::new,
		BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(5.0F, 1200.0F).requiresCorrectToolForDrops().lightLevel(state -> 7).noOcclusion()
	);
	public static final Block TEMPLE_BRICKS = register(
		"temple_bricks", BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(-1.0F, 3600000.0F).sound(SoundType.DEEPSLATE_BRICKS).noLootTable()
	);
	/** A complete wood family: logs, planks and all the carpentry, plus a sapling. */
	public static final class WoodSet {
		public final String name;
		public final Block log;
		public final Block wood;
		public final Block strippedLog;
		public final Block strippedWood;
		public final Block planks;
		public final Block leaves;
		public final Block sapling;
		public final Block stairs;
		public final Block slab;
		public final Block fence;
		public final Block fenceGate;
		public final Block door;
		public final Block trapdoor;
		public final Block button;
		public final Block pressurePlate;

		WoodSet(final String name, final MapColor bark, final MapColor wood, final TreeGrower grower) {
			this.name = name;
			this.log = register(name + "_log", RotatedPillarBlock::new, Blocks.logProperties(wood, bark, SoundType.CHERRY_WOOD));
			this.wood = register(name + "_wood", RotatedPillarBlock::new, Blocks.logProperties(bark, bark, SoundType.CHERRY_WOOD));
			this.strippedLog = register("stripped_" + name + "_log", RotatedPillarBlock::new, Blocks.logProperties(wood, wood, SoundType.CHERRY_WOOD));
			this.strippedWood = register("stripped_" + name + "_wood", RotatedPillarBlock::new, Blocks.logProperties(wood, wood, SoundType.CHERRY_WOOD));
			this.planks = register(
				name + "_planks",
				BlockBehaviour.Properties.of().mapColor(wood).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.CHERRY_WOOD).ignitedByLava()
			);
			this.leaves = register(
				name + "_leaves",
				p -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.SPORE_BLOSSOM_AIR, AmbientLeavesBlockSoundPlayer.noAmbientSound(), p),
				Blocks.leavesProperties(SoundType.CHERRY_LEAVES).mapColor(wood)
			);
			this.sapling = register(
				name + "_sapling",
				p -> new SaplingBlock(grower, p),
				BlockBehaviour.Properties.of().mapColor(wood).noCollision().randomTicks().instabreak().sound(SoundType.CHERRY_SAPLING).pushReaction(PushReaction.POPPED)
			);
			Block planks = this.planks;
			this.stairs = register(name + "_stairs", p -> new StairBlock(planks.defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(planks));
			this.slab = register(name + "_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(planks));
			this.fence = register(name + "_fence", FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(planks));
			this.fenceGate = register(name + "_fence_gate", p -> new FenceGateBlock(WoodType.CHERRY, p), BlockBehaviour.Properties.ofFullCopy(planks).forceSolidOn());
			this.door = register(
				name + "_door",
				p -> new DoorBlock(BlockSetType.CHERRY, p),
				BlockBehaviour.Properties.of().mapColor(wood).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED)
			);
			this.trapdoor = register(
				name + "_trapdoor",
				p -> new TrapDoorBlock(BlockSetType.CHERRY, p),
				BlockBehaviour.Properties.of().mapColor(wood).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()
			);
			this.button = register(name + "_button", p -> new ButtonBlock(BlockSetType.CHERRY, 30, p), Blocks.buttonProperties());
			this.pressurePlate = register(
				name + "_pressure_plate",
				p -> new PressurePlateBlock(BlockSetType.CHERRY, p),
				BlockBehaviour.Properties.of().mapColor(wood).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.POPPED)
			);
		}
	}

	private static BlockBehaviour.Properties ore(final float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(hardness, 3.0F);
	}

	private static Block register(final String name, final BlockBehaviour.Properties properties, final boolean withItem) {
		return register(name, Block::new, properties, withItem);
	}

	private ModBlocks() {
	}

	private static BlockBehaviour.Properties stone(final MapColor color, final float hardness, final float resistance) {
		return BlockBehaviour.Properties.of()
			.mapColor(color)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.requiresCorrectToolForDrops()
			.strength(hardness, resistance);
	}

	private static BlockBehaviour.Properties plant(final MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED);
	}

	private static Block register(final String name, final BlockBehaviour.Properties properties) {
		return register(name, Block::new, properties, true);
	}

	private static Block register(final String name, final Function<BlockBehaviour.Properties, Block> factory, final BlockBehaviour.Properties properties) {
		return register(name, factory, properties, true);
	}

	private static Block register(
		final String name, final Function<BlockBehaviour.Properties, Block> factory, final BlockBehaviour.Properties properties, final boolean withItem
	) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, TheSift.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
		if (withItem) {
			BLOCKS_WITH_ITEMS.add(block);
		}

		return block;
	}

	public static void init() {
	}
}
