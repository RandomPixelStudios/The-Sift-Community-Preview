"""Generates The Sift's data pack: dimension, worldgen, loot tables, recipes, tags and the
always-active Ancient City portal (patched city_center structure templates)."""
import json, os, shutil, sys
sys.path.insert(0, os.path.dirname(__file__))
import nbt
import structures

SCRATCH = '/tmp/claude-1000/-home-cat-Dokumente-Minecraft-The-Sift/cfaeee90-d691-4710-b54e-e87b2d62cad0/scratchpad'
VAN = SCRATCH + '/mc/data/data/minecraft/'
ROOT = '/home/cat/Dokumente/Minecraft The Sift/src/main/resources/data/'
NS = 'thesift'
WOODS = ['sift_willow', 'duskwood', 'pearlbark']
ARMOR_SETS = ['siftite', 'carapace', 'lumenite', 'jade', 'warden']

def write(rel, obj):
    path = ROOT + rel
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')

def vload(rel):
    with open(VAN + rel) as f:
        return json.load(f)

def t(name):
    return '%s:%s' % (NS, name)

def transform(obj, fn):
    if isinstance(obj, dict):
        return {k: transform(v, fn) for k, v in obj.items()}
    if isinstance(obj, list):
        return [transform(v, fn) for v in obj]
    if isinstance(obj, str):
        return fn(obj)
    return obj

def renamer(old, new):
    def rename(s):
        if old not in s:
            return s
        if s.startswith('minecraft:'):
            s = NS + ':' + s[len('minecraft:'):]
        elif s.startswith('#minecraft:'):
            s = '#' + NS + ':' + s[len('#minecraft:'):]
        return s.replace(old, new)
    return rename

# ==========================================================================================
# Ancient City: fill the reinforced deepslate frame with the always-active Sift portal
# ==========================================================================================
def patch_ancient_city():
    src = VAN + 'structure/ancient_city/city_center/'
    dst = ROOT + 'minecraft/structure/ancient_city/city_center/'
    os.makedirs(dst, exist_ok=True)
    for i in (1, 2, 3):
        name, root = nbt.load(src + 'city_center_%d.nbt' % i)
        palette = root['palette'][1][1]
        blocks = root['blocks'][1][1]
        frame = [j for j, p in enumerate(palette) if p['id'][1] == 'minecraft:reinforced_deepslate']
        pos = [tuple(b['pos'][1][1]) for b in blocks if b['state'][1] in frame]
        # the frame stands in a single x plane; its inside is everything enclosed by it
        x = pos[0][0]
        assert all(p[0] == x for p in pos)
        ys = [p[1] for p in pos]
        zs = [p[2] for p in pos]
        y0, y1, z0, z1 = min(ys), max(ys), min(zs), max(zs)
        portal_state = len(palette)
        palette.append({'id': (8, t('sift_portal')), 'properties': (10, {'axis': (8, 'z')})})
        occupied = {tuple(b['pos'][1][1]): b for b in blocks}
        added = 0
        for y in range(y0 + 1, y1):
            for z in range(z0 + 1, z1):
                p = (x, y, z)
                entry = {'pos': (9, (3, [x, y, z])), 'state': (3, portal_state)}
                if p in occupied:
                    occupied[p].clear()
                    occupied[p].update(entry)
                else:
                    blocks.append(entry)
                added += 1
        nbt.save(dst + 'city_center_%d.nbt' % i, name, root)
        print('city_center_%d: portal %dx%d (%d blocks)' % (i, z1 - z0 - 1, y1 - y0 - 1, added))

# ==========================================================================================
# Dimension
# ==========================================================================================
def gen_dimension():
    write('%s/dimension_type/the_sift.json' % NS, {
        'ambient_light': 0.08,
        'attributes': {
            'minecraft:audio/ambient_sounds': {
                'mood': {'block_search_extent': 8, 'offset': 2.0, 'sound': 'minecraft:ambient.cave', 'tick_delay': 6000}
            },
            'minecraft:gameplay/can_start_raid': False,
            'minecraft:gameplay/respawn_anchor_works': False,
            'minecraft:visual/ambient_light_color': '#1a2a33',
            'minecraft:visual/cloud_color': '#ccffd6f2',
            'minecraft:visual/cloud_height': 230.0,
            'minecraft:visual/fog_color': '#9fe3d9',
            'minecraft:visual/sky_color': '#5cd6c6',
            'minecraft:visual/sky_light_color': '#b8f0ff',
        },
        'coordinate_scale': 1.0,
        'has_ceiling': False,
        'has_ender_dragon_fight': False,
        'has_fixed_time': True,
        'has_skylight': True,
        'height': 384,
        'infiniburn': '#minecraft:infiniburn_overworld',
        'logical_height': 384,
        'min_y': -64,
        'monster_spawn_block_light_limit': 11,
        'monster_spawn_light_level': {'type': 'minecraft:uniform', 'max_inclusive': 7, 'min_inclusive': 0},
        'skybox': 'overworld',
    })

    def point(temperature, humidity=(-1.0, 1.0), depth=0.0, continentalness=(-0.35, 1.0), offset=0.0):
        return {
            'temperature': list(temperature), 'humidity': list(humidity), 'continentalness': list(continentalness),
            'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'depth': depth, 'offset': offset,
        }

    biomes = [
        {'biome': t('lullaby_hills'), 'parameters': point((-1.0, -0.3), humidity=(-1.0, 0.0))},
        {'biome': t('pearl_grove'), 'parameters': point((-1.0, -0.3), humidity=(0.0, 1.0))},
        {'biome': t('singers_meadow'), 'parameters': point((-0.3, 0.3), humidity=(-1.0, 0.0))},
        {'biome': t('verdant_isles'), 'parameters': point((-0.3, 0.3), humidity=(0.0, 1.0))},
        {'biome': t('the_carapace'), 'parameters': point((0.3, 1.0), humidity=(-1.0, -0.1))},
        {'biome': t('sift_jungle'), 'parameters': point((0.3, 1.0), humidity=(-0.1, 1.0))},
        {'biome': t('ichor_sea'), 'parameters': point((-1.0, 1.0), continentalness=(-1.0, -0.35))},
        {'biome': t('soulsong_hollows'), 'parameters': point((-1.0, 1.0), depth=[0.6, 1.5], continentalness=(-1.0, 1.0))},
    ]
    write('%s/dimension/the_sift.json' % NS, {
        'type': t('the_sift'),
        'generator': {
            'type': 'minecraft:noise',
            'biome_source': {'type': 'minecraft:multi_noise', 'biomes': biomes},
            'settings': t('the_sift'),
        },
    })

    gen_terrain()


    gen_material_rule()

# ------------------------------------------------------------------------------------------
# Terrain: gentle overworld-like hills and valleys from a pure 2D height field (so no overhangs or
# torn-off floating chunks), with ichor seas in the low basins. Caves come from the carvers.
# ------------------------------------------------------------------------------------------
def df(name, obj):
    write('%s/worldgen/density_function/%s.json' % (NS, name), obj)

def op(kind, a, b):
    return {'type': 'minecraft:' + kind, 'left': a, 'right': b}

def noise(name, xz=1.0, y=0.0):
    return {'type': 'minecraft:noise', 'noise': t(name), 'xz_scale': xz, 'y_scale': y}

def y_grad(y0, v0, y1, v1):
    return {'type': 'minecraft:gradient', 'axis': 'y', 'from_coordinate': y0, 'to_coordinate': y1, 'from_value': v0, 'to_value': v1}

SEA_LEVEL = 62

def gen_terrain():
    write('%s/worldgen/noise/continents.json' % NS, {'base_octave': -9, 'octave_count': 3, 'amplitude_modifiers': [1.0, 0.5, 0.25]})
    write('%s/worldgen/noise/hills.json' % NS, {'base_octave': -7, 'octave_count': 3, 'amplitude_modifiers': [1.0, 0.5, 0.25]})
    write('%s/worldgen/noise/detail.json' % NS, {'base_octave': -5, 'octave_count': 2, 'amplitude_modifiers': [1.0, 0.5]})
    # biome noises: a few octaves finer than vanilla, so biomes stay small
    write('%s/worldgen/noise/temperature.json' % NS, {'base_octave': -8, 'octave_count': 2, 'amplitude_modifiers': [1.0, 0.5]})
    write('%s/worldgen/noise/humidity.json' % NS, {'base_octave': -7, 'octave_count': 2, 'amplitude_modifiers': [1.0, 0.5]})

    cont = {'type': 'minecraft:cache', 'input': noise('continents')}
    df('continents', cont)
    # hills only grow where the land is; the sea floor stays calm
    land = {'type': 'minecraft:clamp', 'input': op('add', t('continents'), 0.55), 'min': 0.0, 'max': 1.0}
    hills = op('mul', noise('hills'), op('mul', land, 16.0))
    height = op('add', op('add', 66.0, op('mul', t('continents'), 18.0)), op('add', hills, op('mul', noise('detail'), 3.0)))
    # seas get properly deep basins instead of shallow puddles
    height = op('add', height, op('mul', op('min', op('add', t('continents'), 0.25), 0.0), 48.0))
    # terraces: the land steps up in 4-block shelves with sheer mauve cliffs, like the trailer
    terraced = {'type': 'minecraft:floor', 'input': height, 'multiple': 4.0}
    df('height', {'type': 'minecraft:cache', 'input': terraced})
    y = y_grad(-64, -64.0, 320, 320.0)
    density = op('sub', t('height'), y)
    # a few huge caverns instead of many small tunnels; they stay well below the surface
    write('%s/worldgen/noise/cavern.json' % NS, {'base_octave': -7, 'octave_count': 2, 'amplitude_modifiers': [1.0, 0.4]})
    cavern = op('sub', 0.42, noise('cavern', 1.0, 1.6))
    near_surface = {'type': 'minecraft:clamp', 'input': op('mul', op('sub', y, op('sub', t('height'), 16.0)), 0.25), 'min': 0.0, 'max': 10.0}
    near_bottom = {'type': 'minecraft:clamp', 'input': op('mul', op('sub', -54.0, y), 0.25), 'min': 0.0, 'max': 10.0}
    caves = {'type': 'minecraft:interpolated', 'cell_size_xz': 4, 'cell_size_y': 4, 'input': op('add', cavern, op('add', near_surface, near_bottom))}
    df('final_density', op('add', op('min', op('mul', density, 0.1), caves), {'type': 'minecraft:beardifier'}))

    write('%s/worldgen/noise_settings/the_sift.json' % NS, {
        'default_block': t('sift_stone'),
        'default_fluid': t('ichor'),
        'disable_mob_generation': False,
        'legacy_random_source': False,
        'material_rule': t('the_sift'),
        'noise': {'min_y': -64, 'height': 384},
        'noise_router': {
            'chunk_surface_level': t('height'),
            'continents': t('continents'),
            'depth': y_grad(-64, 1.5, 96, -0.5),
            'erosion': 'minecraft:overworld/erosion',
            'final_density': t('final_density'),
            'ridges': 'minecraft:overworld/ridges',
            'temperature': noise('temperature'),
            'vegetation': noise('humidity'),
        },
        'sea_level': SEA_LEVEL,
        'spawn_target': [],
        # real aquifers: caves stay dry instead of being completely flooded with ichor
        'aquifers': dict(vload('worldgen/noise_settings/overworld.json')['aquifers'], surface_level=op('sub', t('height'), 10.0), exclusion=-1.0),
    })

def block(name):
    return {'type': 'minecraft:block', 'result_state': name}

def cond(if_true, then_run):
    return {'type': 'minecraft:condition', 'if_true': if_true, 'then_run': then_run}

def biome_is(*names):
    return {'type': 'minecraft:biome', 'biome_is': [t(n) for n in names]}

def noise_above(noise, threshold):
    return {'type': 'minecraft:noise_threshold', 'noise': noise, 'min_threshold': threshold, 'max_threshold': 1.7976931348623157e308}

def y_above(y, mult=0, add_stone=False):
    return {'type': 'minecraft:y_above', 'anchor': {'absolute': y}, 'surface_depth_multiplier': mult, 'add_stone_depth': add_stone}

def gen_material_rule():
    carapace_strata = {'type': 'minecraft:sequence', 'sequence': [
        cond(noise_above('minecraft:patch', 0.15), block(t('blue_carapace'))),
        cond(y_above(92, add_stone=True), block(t('orange_carapace'))),
        cond(y_above(80, add_stone=True), block(t('red_carapace'))),
        cond(y_above(70, add_stone=True), block(t('orange_carapace'))),
        block(t('red_carapace')),
    ]}
    def soil(top, under, steep=None):
        floor = [block(t(top))]
        if steep:
            floor.insert(0, cond({'type': 'minecraft:steep'}, block(t(steep))))
        return {'type': 'minecraft:sequence', 'sequence': [
            cond('minecraft:on_floor', {'type': 'minecraft:sequence', 'sequence': floor}),
            cond('minecraft:under_floor', block(t(under))),
        ]}
    above_sea = {'type': 'minecraft:sequence', 'sequence': [
        cond(biome_is('the_carapace'), {'type': 'minecraft:sequence', 'sequence': [
            cond('minecraft:on_floor', {'type': 'minecraft:sequence', 'sequence': [
                cond({'type': 'minecraft:steep'}, carapace_strata),
                cond(noise_above('minecraft:surface', -0.1), block(t('carapace_sand'))),
                carapace_strata,
            ]}),
            cond('minecraft:deep_under_floor', carapace_strata),
        ]}),
        cond(biome_is('singers_meadow'), soil('blushing_moss', 'meadow_rock', 'sift_stone')),
        cond(biome_is('verdant_isles'), soil('verdant_turf', 'rust_soil', 'sift_stone')),
        cond(biome_is('lullaby_hills'), soil('glowmoss', 'sift_stone', 'sift_stone')),
        cond(biome_is('sift_jungle'), soil('dusk_turf', 'rust_soil', 'sift_stone')),
        cond(biome_is('pearl_grove'), soil('pearl_moss', 'rust_soil', 'sift_stone')),
        cond(biome_is('ichor_sea'), soil('sift_sand', 'sift_sand')),
    ]}
    underwater = {'type': 'minecraft:not', 'invert': {'type': 'minecraft:water', 'offset': -1, 'surface_depth_multiplier': 0, 'add_stone_depth': False}}
    rule = {'type': 'minecraft:sequence', 'sequence': [
        'minecraft:bedrock_floor',
        # glowing moss carpets the floors of the great caverns
        cond(biome_is('soulsong_hollows'), cond('minecraft:on_floor', block(t('glowmoss')))),
        cond(y_above(SEA_LEVEL - 1), above_sea),
        cond(underwater, {'type': 'minecraft:sequence', 'sequence': [
            cond('minecraft:on_floor', block(t('sift_sand'))), cond('minecraft:under_floor', block(t('sift_sand')))]}),
        cond({'type': 'minecraft:vertical_gradient', 'random_name': 'thesift:deep_sift_stone',
              'true_at_and_below': {'absolute': 0}, 'false_at_and_above': {'absolute': 10}}, block(t('deep_sift_stone'))),
    ]}
    write('%s/worldgen/material_rule/the_sift.json' % NS, rule)

# ==========================================================================================
# Features
# ==========================================================================================
def state(name, **props):
    s = {'id': name}
    if props:
        s['properties'] = {k: str(v).lower() for k, v in props.items()}
    return s

def feature(name, obj):
    write('%s/worldgen/feature/%s.json' % (NS, name), obj)

def placed(name, feat, placement):
    write('%s/worldgen/placed_feature/%s.json' % (NS, name), {'feature': feat, 'placement': placement})

def patch_placement(count, tries=32, spread=7, on_surface=True, survive=None, noise=None):
    p = []
    if noise:
        p.append({'type': 'minecraft:noise_threshold_count', 'noise_level': -0.8, 'below_noise': noise[0], 'above_noise': noise[1]})
    else:
        p.append({'type': 'minecraft:count', 'count': count})
    p += [
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG' if on_surface else 'MOTION_BLOCKING'},
        {'type': 'minecraft:biome'},
        {'type': 'minecraft:count', 'count': tries},
        {'type': 'minecraft:offset',
         'x': {'type': 'minecraft:trapezoid', 'min': -spread, 'max': spread, 'plateau': 0},
         'y': {'type': 'minecraft:trapezoid', 'min': -3, 'max': 3, 'plateau': 0},
         'z': {'type': 'minecraft:trapezoid', 'min': -spread, 'max': spread, 'plateau': 0}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'}},
    ]
    if survive:
        p.append({'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': survive}})
    return p

def gen_features():
    # --- Sift Willow: dark trunk, drooping pale canopy (cherry-style placer) ------------------
    tree = vload('worldgen/feature/cherry.json')
    tree['trunk_provider'] = state(t('sift_willow_log'), axis='y')
    tree['foliage_provider'] = state(t('sift_willow_leaves'), distance=7, persistent=False, waterlogged=False)
    tree['foliage_placer']['hanging_leaves_chance'] = 0.45
    tree['foliage_placer']['hanging_leaves_extension_chance'] = 0.6
    tree['trunk_placer']['base_height'] = 6
    feature('sift_willow', tree)
    placed('sift_willow_checked', t('sift_willow'), [
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': t('sift_willow_sapling')}}])

    def trees(name, count, extra_chance):
        placed(name, t('sift_willow'), [
            {'type': 'minecraft:count', 'count': {'type': 'minecraft:weighted_list', 'distribution': [
                {'data': count, 'weight': 9}, {'data': count + extra_chance, 'weight': 1}]}},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:surface_water_depth_filter', 'max_water_depth': 0},
            {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR'},
            {'type': 'minecraft:biome'},
            {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': t('sift_willow_sapling')}},
        ])
    trees('trees_singers_meadow', 1, 2)
    trees('trees_lullaby_hills', 4, 3)

    # --- ground cover --------------------------------------------------------------------------
    feature('blush_grass', {'type': 'minecraft:simple_block', 'to_place': state(t('blush_grass'))})
    feature('tall_blush_grass', {'type': 'minecraft:simple_block', 'to_place': state(t('tall_blush_grass'))})
    feature('glow_sprouts', {'type': 'minecraft:simple_block', 'to_place': state(t('glow_sprouts'))})
    feature('lullaby_bloom', {'type': 'minecraft:simple_block', 'to_place': state(t('lullaby_bloom'))})
    feature('echo_bloom', {'type': 'minecraft:simple_block', 'to_place': state(t('echo_bloom'))})
    feature('sift_thorns', {'type': 'minecraft:simple_block', 'to_place': state(t('sift_thorns'))})
    placed('patch_blush_grass', t('blush_grass'), patch_placement(0, tries=64, noise=(24, 48), survive=t('blush_grass')))
    placed('patch_tall_blush_grass', t('tall_blush_grass'), patch_placement(14, tries=48, survive=t('tall_blush_grass')))
    placed('patch_lullaby_bloom', t('lullaby_bloom'), patch_placement(3, tries=24, survive=t('lullaby_bloom')))
    placed('patch_glow_sprouts', t('glow_sprouts'), patch_placement(0, tries=64, noise=(20, 40), survive=t('glow_sprouts')))
    placed('patch_echo_bloom', t('echo_bloom'), patch_placement(3, tries=16, survive=t('echo_bloom')))
    placed('patch_sift_thorns', t('sift_thorns'), patch_placement(1, tries=12, spread=5, survive=t('sift_thorns')))
    placed('patch_sift_thorns_dense', t('sift_thorns'), patch_placement(4, tries=20, spread=6, survive=t('sift_thorns')))

    # --- ichor pools ---------------------------------------------------------------------------
    lake = vload('worldgen/feature/lake_lava.json')
    lake['fluid'] = state(t('ichor'), level=0)
    lake['barrier'] = state(t('sift_stone'))
    feature('lake_ichor', lake)
    placed('lake_ichor_surface', t('lake_ichor'), [
        {'type': 'minecraft:rarity_filter', 'chance': 12},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'},
        {'type': 'minecraft:biome'}])
    placed('lake_ichor_carapace', t('lake_ichor'), [
        {'type': 'minecraft:rarity_filter', 'chance': 4},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'},
        {'type': 'minecraft:biome'}])

    # --- soul crystals -------------------------------------------------------------------------
    geode = vload('worldgen/feature/amethyst_geode.json')
    geode['blocks']['alternate_inner_layer_provider'] = state(t('soul_crystal_block'))
    geode['blocks']['inner_layer_provider'] = state(t('soul_crystal_block'))
    geode['blocks']['inner_placements'] = [t('soul_crystal_cluster')]
    geode['blocks']['middle_layer_provider'] = state(t('polished_sift_stone'))
    geode['blocks']['outer_layer_provider'] = state(t('cobbled_sift_stone'))
    geode['use_alternate_layer0_chance'] = 0.0
    feature('soul_crystal_geode', geode)
    placed('soul_crystal_geode', t('soul_crystal_geode'), [
        {'type': 'minecraft:rarity_filter', 'chance': 10},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'above_bottom': 6}, 'max_inclusive': {'absolute': 60}}},
        {'type': 'minecraft:biome'}])

    feature('soul_crystal_cluster', {'type': 'minecraft:simple_block', 'to_place': state(t('soul_crystal_cluster'), facing='up', waterlogged=False)})
    placed('surface_soul_crystals', t('soul_crystal_cluster'), [
        {'type': 'minecraft:count', 'count': 2},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'},
        {'type': 'minecraft:biome'},
        {'type': 'minecraft:count', 'count': 6},
        {'type': 'minecraft:offset',
         'x': {'type': 'minecraft:uniform', 'min_inclusive': -4, 'max_inclusive': 4},
         'y': {'type': 'minecraft:uniform', 'min_inclusive': -2, 'max_inclusive': 2},
         'z': {'type': 'minecraft:uniform', 'min_inclusive': -4, 'max_inclusive': 4}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
            {'type': 'minecraft:has_sturdy_face', 'direction': 'up', 'offset': [0, -1, 0]}]}},
    ])
    placed('cave_soul_crystals', t('soul_crystal_cluster'), [
        {'type': 'minecraft:count', 'count': 90},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 80}}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
            {'type': 'minecraft:has_sturdy_face', 'direction': 'up', 'offset': [0, -1, 0]}]}},
        {'type': 'minecraft:biome'},
    ])
    placed('cave_glow_sprouts', t('glow_sprouts'), [
        {'type': 'minecraft:count', 'count': 40},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 80}}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
            {'type': 'minecraft:would_survive', 'state': t('glow_sprouts')}]}},
        {'type': 'minecraft:biome'},
    ])

    # soul crystal ore veins inside sift stone (the dimension's own gem)
    feature('ore_soul_crystal', {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': 0.3, 'size': 7, 'targets': [
        {'state': state(t('soul_crystal_block')), 'target': {'predicate_type': 'minecraft:block_match', 'block': t('sift_stone')}}]})
    placed('ore_soul_crystal', t('ore_soul_crystal'), [
        {'type': 'minecraft:count', 'count': 8}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:trapezoid', 'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 100}}},
        {'type': 'minecraft:biome'}])
    # the Sift's own ores, all set in sift stone
    for ore, size, count, lo, hi, discard in (('sift_coal_ore', 14, 24, -10, 160, 0.0), ('sift_copper_ore', 10, 14, -32, 112, 0.0),
                                             ('sift_iron_ore', 9, 18, -50, 96, 0.0), ('sift_gold_ore', 8, 8, -64, 48, 0.0),
                                             ('sift_diamond_ore', 7, 7, -64, 16, 0.0), ('lumenite_ore', 7, 10, -40, 120, 0.0),
                                             ('jade_ore', 6, 7, -48, 64, 0.0), ('siftite_ore', 5, 9, -64, 40, 0.0)):
        name = 'ore_' + ore.replace('_ore', '')
        feature(name, {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': discard, 'size': size, 'targets': [
            {'state': state(t(ore)), 'target': {'predicate_type': 'minecraft:tag_match', 'tag': t('stone_ore_replaceables')}}]})
        placed(name, t(name), [
            {'type': 'minecraft:count', 'count': count}, {'type': 'minecraft:in_square'},
            {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:trapezoid', 'min_inclusive': {'absolute': lo}, 'max_inclusive': {'absolute': hi}}},
            {'type': 'minecraft:biome'}])

    feature('bluebell', {'type': 'minecraft:simple_block', 'to_place': state(t('bluebell'))})
    feature('pale_lily', {'type': 'minecraft:simple_block', 'to_place': state(t('pale_lily'))})
    feature('glimmer_petals', {'type': 'minecraft:simple_block', 'to_place': state(t('glimmer_petals'), facing='north', flower_amount=3)})
    placed('patch_bluebell', t('bluebell'), patch_placement(2, tries=16, survive=t('bluebell')))
    placed('patch_pale_lily', t('pale_lily'), patch_placement(1, tries=12, survive=t('pale_lily')))
    placed('patch_glimmer_petals', t('glimmer_petals'), patch_placement(3, tries=32, survive=t('glimmer_petals')))

    feature('ore_meadow_rock', {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': 0.0, 'size': 40, 'targets': [
        {'state': state(t('meadow_rock')), 'target': {'predicate_type': 'minecraft:block_match', 'block': t('sift_stone')}}]})
    placed('ore_meadow_rock', t('ore_meadow_rock'), [
        {'type': 'minecraft:count', 'count': 4}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': 0}, 'max_inclusive': {'absolute': 160}}},
        {'type': 'minecraft:biome'}])

def tree_placement(name, feat, count, extra, sapling):
    placed(name, feat, [
        {'type': 'minecraft:count', 'count': {'type': 'minecraft:weighted_list', 'distribution': [
            {'data': count, 'weight': 9}, {'data': count + extra, 'weight': 1}]}},
        {'type': 'minecraft:in_square'},
        {'type': 'minecraft:surface_water_depth_filter', 'max_water_depth': 0},
        {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR'},
        {'type': 'minecraft:biome'},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': t(sapling)}},
    ])

def gen_features_extra():
    # stone variety underground
    for name, size, count, lo, hi in (('veilstone', 48, 4, -64, 80), ('rose_marble', 40, 3, -32, 110), ('sift_gravel', 33, 6, -64, 100),
                                      ('meadow_rock_deep', 40, 3, -64, 60)):
        blk = 'meadow_rock' if name == 'meadow_rock_deep' else name
        feature('blob_' + name, {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': 0.0, 'size': size, 'targets': [
            {'state': state(t(blk)), 'target': {'predicate_type': 'minecraft:tag_match', 'tag': t('stone_ore_replaceables')}}]})
        placed('blob_' + name, t('blob_' + name), [
            {'type': 'minecraft:count', 'count': count}, {'type': 'minecraft:in_square'},
            {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': lo}, 'max_inclusive': {'absolute': hi}}},
            {'type': 'minecraft:biome'}])
    # glowcaps
    feature('glowcap', {'type': 'minecraft:simple_block', 'to_place': state(t('glowcap'))})
    placed('patch_glowcap', t('glowcap'), patch_placement(2, tries=16, spread=5, survive=t('glowcap')))
    placed('cave_glowcap', t('glowcap'), [
        {'type': 'minecraft:count', 'count': 30}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 50}}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'}, {'type': 'minecraft:would_survive', 'state': t('glowcap')}]}},
        {'type': 'minecraft:biome'}])
    # boulders
    for name, blk in (('boulder_veilstone', 'veilstone'), ('boulder_meadow', 'meadow_rock'), ('boulder_carapace', 'orange_carapace')):
        feature(name, {'type': 'minecraft:block_blob', 'can_place_on': {'type': 'minecraft:matching_block_tag', 'tag': t('arrival_ground')}, 'state': t(blk)})
        placed(name, t(name), [{'type': 'minecraft:rarity_filter', 'chance': 4}, {'type': 'minecraft:in_square'},
                               {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}, {'type': 'minecraft:biome'}])
    # fallen logs
    for wood in WOODS:
        f = vload('worldgen/feature/fallen_oak_tree.json')
        f['trunk_provider'] = state(t(wood + '_log'), axis='y')
        f['log_decorators'][0]['block_provider'] = {'type': 'minecraft:weighted', 'entries': [
            {'data': t('glowcap'), 'weight': 2}, {'data': t('mint_grass'), 'weight': 3}]}
        feature('fallen_' + wood, f)
        placed('fallen_' + wood, t('fallen_' + wood), [{'type': 'minecraft:rarity_filter', 'chance': 6}, {'type': 'minecraft:in_square'},
                                                       {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'}, {'type': 'minecraft:biome'}])
    # glowing soul crystal columns growing from cave floors
    feature('crystal_column', {'type': 'minecraft:block_column', 'allowed_placement': {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
                               'direction': 'up', 'prioritize_tip': True, 'layers': [
        {'height': {'type': 'minecraft:uniform', 'min_inclusive': 2, 'max_inclusive': 6}, 'provider': state(t('soul_crystal_block'))},
        {'height': 1, 'provider': state(t('soul_crystal_cluster'), facing='up', waterlogged=False)}]})
    placed('cave_crystal_columns', t('crystal_column'), [
        {'type': 'minecraft:count', 'count': 12}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 40}}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'}, {'type': 'minecraft:has_sturdy_face', 'direction': 'up', 'offset': [0, -1, 0]}]}},
        {'type': 'minecraft:biome'}])

    def tree(name, vanilla, log, leaves, **changes):
        f = vload('worldgen/feature/%s.json' % vanilla)
        f['trunk_provider'] = state(t(log), axis='y')
        f['foliage_provider'] = state(t(leaves), distance=7, persistent=False, waterlogged=False)
        f.update(changes)
        feature(name, f)
    # Duskwood: the tall, vine-hung jungle trees of the Sift Jungle
    tree('duskwood', 'jungle_tree', 'duskwood_log', 'duskwood_leaves', decorators=[
        {'type': 'minecraft:trunk_vine'}, {'type': 'minecraft:leave_vine', 'probability': 0.25}])
    tree('mega_duskwood', 'mega_jungle_tree', 'duskwood_log', 'duskwood_leaves')
    # Pearlbark: slender pale trees with golden crowns
    tree('pearlbark', 'birch', 'pearlbark_log', 'pearlbark_leaves')
    f = json.load(open(ROOT + '%s/worldgen/feature/pearlbark.json' % NS))
    f['trunk_placer']['base_height'] = 7
    f['trunk_placer']['height_rand_a'] = 4
    feature('pearlbark', f)
    placed('duskwood_checked', t('duskwood'), [
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': t('duskwood_sapling')}}])
    placed('pearlbark_checked', t('pearlbark'), [
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:would_survive', 'state': t('pearlbark_sapling')}}])
    tree_placement('trees_sift_jungle', t('duskwood'), 8, 4, 'duskwood_sapling')
    tree_placement('mega_duskwood_trees', t('mega_duskwood'), 1, 2, 'duskwood_sapling')
    tree_placement('trees_pearl_grove', t('pearlbark'), 6, 3, 'pearlbark_sapling')
    feature('verdant_trees', {'type': 'minecraft:random_selector', 'default': t('pearlbark_checked'), 'features': [
        {'chance': 0.5, 'feature': t('sift_willow_checked')}]})
    tree_placement('trees_verdant', t('verdant_trees'), 0, 2, 'pearlbark_sapling')

    # big blocky blue trees of the Lullaby forest: fancy-oak shapes with icy willow leaves
    tree('blue_giant', 'fancy_oak', 'sift_willow_log', 'sift_willow_leaves')
    tree_placement('trees_blue_forest', t('blue_giant'), 5, 3, 'sift_willow_sapling')

    # mint grass carpets the terraces
    feature('mint_grass', {'type': 'minecraft:simple_block', 'to_place': state(t('mint_grass'))})
    feature('tall_mint_grass', {'type': 'minecraft:simple_block', 'to_place': state(t('tall_mint_grass'))})
    placed('patch_mint_grass', t('mint_grass'), patch_placement(0, tries=64, noise=(28, 48), survive=t('mint_grass')))
    placed('patch_tall_mint_grass', t('tall_mint_grass'), patch_placement(10, tries=48, survive=t('tall_mint_grass')))

    # crimson rock arches and spires rising out of the grass
    feature('rock_formation', {'type': 'minecraft:template', 'templates':
        [{'data': {'id': t('rock/arch_%d' % i)}, 'weight': 2} for i in range(4)] +
        [{'data': {'id': t('rock/spire_%d' % i)}, 'weight': 3} for i in range(4)]})
    for name, chance in (('rock_formations', 3), ('rock_formations_rare', 9)):
        placed(name, t('rock_formation'), [
            {'type': 'minecraft:rarity_filter', 'chance': chance},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR_WG'},
            {'type': 'minecraft:offset', 'x': 0, 'y': -6, 'z': 0},
            {'type': 'minecraft:biome'}])

    # waterfalls: water and ichor springs breaking out of cliff faces
    valid = [t(b) for b in ('sift_stone', 'meadow_rock', 'orange_carapace', 'red_carapace', 'blue_carapace', 'rust_soil')]
    for fluid, block_id, count in (('water', 'minecraft:water', 30), ('ichor', t('ichor'), 14)):
        feature('spring_' + fluid, {'type': 'minecraft:spring_feature', 'state': {'id': block_id, 'properties': {'falling': 'true'}},
                                    'valid_blocks': valid})
        placed('spring_' + fluid, t('spring_' + fluid), [
            {'type': 'minecraft:count', 'count': count}, {'type': 'minecraft:in_square'},
            {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': 60}, 'max_inclusive': {'absolute': 120}}},
            {'type': 'minecraft:biome'}])

# ==========================================================================================
# Biomes
# ==========================================================================================
def spawns(creature=(), monster=(), water=(), ambient=()):
    creature = list(creature)
    def entries(lst):
        return [{'type': e, 'count': {'type': 'minecraft:uniform', 'min_inclusive': lo, 'max_inclusive': hi}, 'weight': w} for e, w, lo, hi in lst]
    by_cat = {}
    if creature:
        by_cat['creature'] = entries(creature)
    if monster:
        by_cat['monster'] = entries(monster)
    if water:
        by_cat['water_creature'] = entries(water)
    if ambient:
        by_cat['ambient'] = entries(ambient)
    return {'argument': {'spawn_costs': {}, 'spawns_by_category': by_cat}, 'modifier': 'override'}

def biome(name, fog, water, particles, music, features, creature=(), monster=(), temperature=0.7, carvers=None, water_mobs=(), ambient=()):
    steps = [[] for _ in range(11)]
    for step, fs in features.items():
        # one global order for every biome, otherwise Minecraft finds a feature order cycle
        steps[step] = sorted(set(fs))
    attributes = {
        'minecraft:audio/background_music': {'default': {'sound': music, 'min_delay': 12000, 'max_delay': 24000}},
        'minecraft:gameplay/natural_mob_spawns': spawns(creature, monster, water_mobs, ambient),
        'minecraft:visual/ambient_particles': {'argument': [{'particle': {'type': p}, 'probability': prob} for p, prob in particles], 'modifier': 'override'},
        'minecraft:visual/fog_color': fog,
        'minecraft:gameplay/creature_world_gen_spawn_probability': 0.3,
    }
    write('%s/worldgen/biome/%s.json' % (NS, name), {
        'attributes': attributes,
        'carvers': carvers if carvers is not None else ['minecraft:cave', 'minecraft:canyon'],
        'downfall': 0.0,
        'effects': {'water_color': water},
        'features': steps,
        'has_precipitation': False,
        'temperature': temperature,
    })

BLUB = t('blub')
SIFTER = t('sifter')
NUZZLE = t('nuzzle')
SINGER = t('singer')
SHELLBACK = t('shellback')
SHADE = t('shade')
GLOOM = t('gloomstalker')
HOG = t('bumblehog')
DARK = [(SHADE, 40, 1, 3), (GLOOM, 30, 1, 2)]
LUMOTH = t('lumoth')
STAG = t('pearl_stag')
RAY = t('ichor_ray')

def gen_biomes():
    ores = [t('ore_soul_crystal'), t('ore_meadow_rock')] + [t('ore_' + o) for o in
            ('sift_coal', 'sift_copper', 'sift_iron', 'sift_gold', 'sift_diamond', 'lumenite', 'jade', 'siftite')]
    common_underground = {
        1: [t('lake_ichor_surface')],
        3: [t('soul_crystal_geode')],
        6: ores + [t('blob_veilstone'), t('blob_rose_marble'), t('blob_sift_gravel'), t('blob_meadow_rock_deep')],
        7: [t('spring_water'), t('spring_ichor'), t('cave_glowcap'), t('cave_crystal_columns')],
    }
    def merge(extra):
        out = {k: list(v) for k, v in common_underground.items()}
        for k, v in extra.items():
            out.setdefault(k, [])
            out[k] = out[k] + v
        return out
    calm = 'minecraft:music.overworld.cherry_grove'
    # Sifters only lurk rarely in the darkest corners; the Sift is a peaceful place
    biome('singers_meadow', '#f2c2d0', '#d77bd6',
          [('minecraft:spore_blossom_air', 0.004)], calm,
          merge({4: [t('rock_formations_rare'), t('boulder_meadow')], 9: [t('fallen_sift_willow'), t('trees_singers_meadow'), t('patch_blush_grass'), t('patch_tall_blush_grass'), t('patch_lullaby_bloom'),
                     t('patch_sift_thorns'), t('patch_bluebell'), t('surface_soul_crystals')]}),
          creature=[(BLUB, 14, 2, 5), (NUZZLE, 10, 2, 4), (SINGER, 4, 1, 2), (STAG, 6, 2, 4), (HOG, 10, 2, 4)], monster=DARK, ambient=[(LUMOTH, 4, 1, 3)], temperature=0.6)
    biome('lullaby_hills', '#bfe6f2', '#6fb6ff',
          [('minecraft:glow', 0.006)], 'minecraft:music.overworld.lush_caves',
          merge({4: [t('boulder_veilstone')], 9: [t('fallen_sift_willow'), t('patch_glowcap'), t('trees_blue_forest'), t('trees_lullaby_hills'), t('patch_glow_sprouts'), t('patch_mint_grass'), t('patch_echo_bloom'),
                     t('patch_pale_lily'), t('surface_soul_crystals')]}),
          creature=[(BLUB, 12, 2, 5), (SINGER, 6, 1, 2), (HOG, 8, 2, 4), (STAG, 6, 2, 3)], monster=DARK, ambient=[(LUMOTH, 8, 2, 4)], temperature=0.3)
    biome('pearl_grove', '#e8eef6', '#9fc8ff',
          [('minecraft:cherry_leaves', 0.002)], 'minecraft:music.overworld.forest',
          merge({4: [t('boulder_veilstone')], 9: [t('fallen_pearlbark'), t('patch_glowcap'), t('trees_pearl_grove'), t('patch_pale_lily'), t('patch_bluebell'), t('patch_blush_grass')]}),
          creature=[(STAG, 12, 2, 5), (NUZZLE, 6, 1, 3), (HOG, 8, 2, 4)], monster=DARK, ambient=[(LUMOTH, 6, 2, 4)], temperature=0.4)
    biome('the_carapace', '#f6d0a8', '#e0905a',
          [('minecraft:white_ash', 0.008)], 'minecraft:music.overworld.desert',
          merge({1: [t('lake_ichor_carapace')], 4: ['minecraft:fossil_upper', t('rock_formations'), t('boulder_carapace')], 9: [t('patch_sift_thorns_dense')]}),
          creature=[(SHELLBACK, 14, 2, 4), (HOG, 4, 1, 3)], monster=DARK, temperature=1.6)
    biome('sift_jungle', '#b8f0c8', '#5fd8b0',
          [('minecraft:spore_blossom_air', 0.006)], 'minecraft:music.overworld.jungle',
          merge({9: [t('fallen_duskwood'), t('patch_glowcap'), t('trees_sift_jungle'), t('mega_duskwood_trees'), 'minecraft:vines', t('patch_glow_sprouts'), t('patch_lullaby_bloom'), t('patch_blush_grass')]}),
          creature=[(NUZZLE, 12, 2, 4), (SINGER, 5, 1, 2), (BLUB, 8, 2, 4), (HOG, 10, 2, 4)], monster=DARK + [(SIFTER, 4, 1, 1)], ambient=[(LUMOTH, 10, 2, 5)], temperature=0.95)
    biome('verdant_isles', '#d0f6e4', '#8fd8ff',
          [('minecraft:spore_blossom_air', 0.003)], 'minecraft:music.overworld.flower_forest',
          merge({4: [t('rock_formations'), t('boulder_meadow')], 9: [t('trees_verdant'), t('patch_mint_grass'), t('patch_tall_mint_grass'), t('patch_glimmer_petals'),
                     t('patch_pale_lily'), t('patch_bluebell')]}),
          creature=[(NUZZLE, 10, 2, 4), (SINGER, 6, 1, 2), (STAG, 8, 2, 4), (HOG, 10, 2, 4), (BLUB, 8, 2, 4)], monster=DARK, ambient=[(LUMOTH, 4, 1, 3)], temperature=0.7)
    biome('ichor_sea', '#c8d8f6', '#b48fff', [], calm, merge({}), water_mobs=[(RAY, 10, 1, 3)], temperature=0.6)
    biome('soulsong_hollows', '#7fbcc8', '#5fd0ff',
          [('minecraft:soul', 0.002)], 'minecraft:music.overworld.lush_caves',
          merge({7: [t('cave_soul_crystals'), t('cave_glow_sprouts')],
                 9: ['minecraft:glow_lichen', 'minecraft:lush_caves_ceiling_vegetation', 'minecraft:cave_vines',
                     'minecraft:spore_blossom', 'minecraft:classic_vines_cave_feature', t('patch_echo_bloom'), t('patch_mint_grass')]}),
          monster=DARK + [(SIFTER, 6, 1, 1)], ambient=[(LUMOTH, 6, 1, 3)], temperature=0.5)

# ==========================================================================================
# Loot tables
# ==========================================================================================
def loot(name, pools):
    write('%s/loot_table/blocks/%s.json' % (NS, name), {'type': 'minecraft:block', 'pools': pools, 'random_sequence': '%s:blocks/%s' % (NS, name)})

def drop_self(name):
    loot(name, [{'condition': {'type': 'minecraft:survives_explosion'}, 'entries': [{'type': 'minecraft:item', 'name': t(name)}], 'rolls': 1}])

def silk_or(name, other):
    loot(name, [{'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'condition': 'minecraft:tool/can_silk_touch', 'name': t(name)},
        {'type': 'minecraft:item', 'condition': {'type': 'minecraft:survives_explosion'}, 'name': other}]}], 'rolls': 1}])

def shears_only(name):
    loot(name, [{'entries': [{'type': 'minecraft:item', 'condition': 'minecraft:tool/can_shear', 'name': t(name)}], 'rolls': 1}])

def copy_loot(vname, old, new):
    data = transform(vload('loot_table/blocks/%s.json' % vname), renamer(old, new))
    write('%s/loot_table/blocks/%s.json' % (NS, vname.replace(old, new)), data)

def gen_loot():
    for n in ['cobbled_sift_stone', 'polished_sift_stone', 'sift_stone_bricks', 'sift_stone_brick_stairs', 'sift_stone_brick_wall',
              'chiseled_sift_stone_bricks', 'meadow_rock', 'orange_carapace', 'red_carapace', 'blue_carapace', 'polished_carapace',
              'carapace_sand', 'soul_crystal_block', 'lullaby_bloom', 'echo_bloom', 'sift_thorns',
              'raw_siftite_block', 'siftite_block', 'meadow_rock_bricks', 'carapace_bricks', 'sift_lantern', 'rust_soil',
              'bluebell', 'pale_lily']:
        drop_self(n)
    silk_or('verdant_turf', t('rust_soil'))
    copy_loot('pink_petals', 'pink_petals', 'glimmer_petals')
    ore_loot = transform(vload('loot_table/blocks/diamond_ore.json'), renamer('diamond_ore', 'siftite_ore'))
    ore_loot = transform(ore_loot, lambda s: t('raw_siftite') if s == 'minecraft:diamond' else s)
    write('%s/loot_table/blocks/siftite_ore.json' % NS, ore_loot)
    silk_or('sift_stone', t('cobbled_sift_stone'))
    silk_or('blushing_moss', t('meadow_rock'))
    silk_or('glowmoss', t('sift_stone'))
    shears_only('blush_grass')
    shears_only('glow_sprouts')
    shears_only('mint_grass')
    copy_loot('tall_grass', 'tall_grass', 'tall_mint_grass')
    data = transform(vload('loot_table/blocks/cherry_slab.json'), renamer('cherry', 'sift_willow'))
    write('%s/loot_table/blocks/sift_stone_brick_slab.json' % NS, transform(data, lambda s: s.replace('sift_willow_slab', 'sift_stone_brick_slab')))
    for wood in WOODS:
        for v in ['cherry_log', 'cherry_wood', 'stripped_cherry_log', 'stripped_cherry_wood', 'cherry_planks', 'cherry_leaves',
                  'cherry_sapling', 'cherry_stairs', 'cherry_slab', 'cherry_fence', 'cherry_fence_gate', 'cherry_door', 'cherry_trapdoor',
                  'cherry_button', 'cherry_pressure_plate']:
            copy_loot(v, 'cherry', wood)
    # duskwood leaves also drop duskfruit, like apples from oak
    data = json.load(open(ROOT + '%s/loot_table/blocks/duskwood_leaves.json' % NS))
    data['pools'].append({'rolls': 1, 'conditions': [{'condition': 'minecraft:table_bonus', 'enchantment': 'minecraft:fortune',
                          'chances': [0.02, 0.03, 0.04, 0.06, 0.1]}], 'entries': [{'type': 'minecraft:item', 'name': t('duskfruit')}]})
    write('%s/loot_table/blocks/duskwood_leaves.json' % NS, data)
    for n in ['dusk_turf', 'pearl_moss']:
        silk_or(n, t('rust_soil'))
    silk_or('deep_sift_stone', t('cobbled_deep_sift_stone'))
    for n in ['sift_sand', 'lumenite_block', 'jade_block', 'sift_altar', 'cobbled_deep_sift_stone', 'veilstone', 'polished_veilstone',
              'rose_marble', 'polished_rose_marble', 'rose_marble_bricks', 'sift_gravel', 'glowcap']:
        drop_self(n)
    for ore, drop in (('sift_coal_ore', 'minecraft:coal'), ('sift_copper_ore', 'minecraft:raw_copper'), ('sift_iron_ore', 'minecraft:raw_iron'),
                      ('sift_gold_ore', 'minecraft:raw_gold'), ('sift_diamond_ore', 'minecraft:diamond'), ('lumenite_ore', t('lumenite_shard')),
                      ('jade_ore', t('jade'))):
        data = transform(vload('loot_table/blocks/diamond_ore.json'), renamer('diamond_ore', ore))
        data = transform(data, lambda x, d=drop: d if x == 'minecraft:diamond' else x)
        write('%s/loot_table/blocks/%s.json' % (NS, ore), data)
    copy_loot('tall_grass', 'tall_grass', 'tall_blush_grass')
    copy_loot('amethyst_cluster', 'amethyst_cluster', 'soul_crystal_cluster')
    data = json.load(open(ROOT + '%s/loot_table/blocks/soul_crystal_cluster.json' % NS))
    data = transform(data, lambda s: t('soul_shard') if s == 'minecraft:amethyst_shard' else s)
    write('%s/loot_table/blocks/soul_crystal_cluster.json' % NS, data)
    # soul crystal block mined without silk touch shatters into shards
    loot('soul_crystal_block', [{'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'condition': 'minecraft:tool/can_silk_touch', 'name': t('soul_crystal_block')},
        {'type': 'minecraft:item', 'name': t('soul_shard'), 'modifier': [
            {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 2, 'max': 4}},
            {'type': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'},
            {'type': 'minecraft:explosion_decay'}]}]}], 'rolls': 1}])

    gen_chest_loot()
    def entity_loot(name, pools):
        write('%s/loot_table/entities/%s.json' % (NS, name), {'type': 'minecraft:entity', 'pools': pools, 'random_sequence': '%s:entities/%s' % (NS, name)})
    def drop(item, lo, hi, killed_by_player=False):
        pool = {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item, 'modifier': [
            {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}]}]}
        if killed_by_player:
            pool['condition'] = {'type': 'minecraft:killed_by_player'}
        return pool
    entity_loot('nuzzle', [drop('minecraft:string', 0, 2)])
    entity_loot('singer', [drop(t('soul_shard'), 0, 2), drop('minecraft:amethyst_shard', 0, 1)])
    entity_loot('shellback', [drop(t('carapace_scale'), 1, 3)])
    entity_loot('pearl_stag', [drop(t('venison'), 1, 3), drop('minecraft:leather', 0, 2)])
    entity_loot('shade', [drop(t('soul_shard'), 0, 2), drop('minecraft:rotten_flesh', 0, 2)])
    entity_loot('gloomstalker', [drop('minecraft:string', 0, 2), drop('minecraft:spider_eye', 0, 1), drop(t('lumenite_shard'), 0, 1)])
    entity_loot('bumblehog', [drop(t('bumblehog_chop'), 1, 3)])
    entity_loot('lumoth', [drop('minecraft:glowstone_dust', 0, 2)])
    entity_loot('ichor_ray', [drop(t('ray_fillet'), 1, 2)])


    # mob loot
    write('%s/loot_table/entities/blub.json' % NS, {'type': 'minecraft:entity', 'pools': [{'rolls': 1, 'entries': [
        {'type': 'minecraft:item', 'name': t('blub_jelly'), 'modifier': [
            {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}}]}]}],
        'random_sequence': '%s:entities/blub' % NS})
    write('%s/loot_table/entities/sifter.json' % NS, {'type': 'minecraft:entity', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': t('soul_shard'), 'modifier': [
            {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 1, 'max': 3}}]}]},
        {'rolls': 1, 'condition': {'type': 'minecraft:killed_by_player'},
         'entries': [{'type': 'minecraft:item', 'name': t('sift_thorns'), 'weight': 1}, {'type': 'minecraft:empty', 'weight': 3}]}],
        'random_sequence': '%s:entities/sifter' % NS})

# ==========================================================================================
# Recipes
# ==========================================================================================
def recipe(name, obj):
    write('%s/recipe/%s.json' % (NS, name), obj)

def shaped(name, pattern, key, result, count=1, category='building', group=None):
    r = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern, 'result': {'count': count, 'id': result}}
    if group:
        r['group'] = group
    recipe(name, r)

def gen_recipes():
    for wood in WOODS:
        for v in ['cherry_planks', 'cherry_wood', 'stripped_cherry_wood', 'cherry_stairs', 'cherry_slab', 'cherry_fence', 'cherry_fence_gate',
                  'cherry_door', 'cherry_trapdoor', 'cherry_button', 'cherry_pressure_plate', 'cherry_boat', 'cherry_chest_boat']:
            data = transform(vload('recipe/%s.json' % v), renamer('cherry', wood))
            recipe(v.replace('cherry', wood), data)
    gen_recipes_extra()
    shaped('sift_stone_bricks', ['##', '##'], {'#': t('sift_stone')}, t('sift_stone_bricks'), 4)
    shaped('polished_sift_stone', ['##', '##'], {'#': t('sift_stone_bricks')}, t('polished_sift_stone'), 4)
    shaped('chiseled_sift_stone_bricks', ['#', '#'], {'#': t('sift_stone_brick_slab')}, t('chiseled_sift_stone_bricks'))
    shaped('sift_stone_brick_stairs', ['#  ', '## ', '###'], {'#': t('sift_stone_bricks')}, t('sift_stone_brick_stairs'), 4)
    shaped('sift_stone_brick_slab', ['###'], {'#': t('sift_stone_bricks')}, t('sift_stone_brick_slab'), 6)
    shaped('sift_stone_brick_wall', ['###', '###'], {'#': t('sift_stone_bricks')}, t('sift_stone_brick_wall'), 6)
    shaped('polished_carapace', ['##', '##'], {'#': t('orange_carapace')}, t('polished_carapace'), 4)
    shaped('soul_crystal_block', ['##', '##'], {'#': t('soul_shard')}, t('soul_crystal_block'))
    recipe('sift_stone_from_smelting', {'type': 'minecraft:smelting', 'category': 'blocks', 'cookingtime': 200, 'experience': 0.1,
                                        'ingredient': t('cobbled_sift_stone'), 'result': {'id': t('sift_stone')}})
    recipe('ichor_bottle', {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
                            'ingredients': [t('ichor_bucket'), 'minecraft:glass_bottle', 'minecraft:glass_bottle', 'minecraft:glass_bottle'],
                            'result': {'count': 3, 'id': t('ichor_bottle')}})
    shaped('sift_lantern', ['nnn', 'nSn', 'nnn'], {'n': 'minecraft:iron_nugget', 'S': t('soul_shard')}, t('sift_lantern'), 1, 'misc')
    shaped('meadow_rock_bricks', ['##', '##'], {'#': t('meadow_rock')}, t('meadow_rock_bricks'), 4)
    shaped('carapace_bricks', ['##', '##'], {'#': t('polished_carapace')}, t('carapace_bricks'), 4)
    shaped('siftite_block', ['###', '###', '###'], {'#': t('siftite_ingot')}, t('siftite_block'))
    shaped('raw_siftite_block', ['###', '###', '###'], {'#': t('raw_siftite')}, t('raw_siftite_block'))
    recipe('siftite_ingot_from_block', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [t('siftite_block')],
                                        'result': {'count': 9, 'id': t('siftite_ingot')}})
    recipe('raw_siftite_from_block', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [t('raw_siftite_block')],
                                      'result': {'count': 9, 'id': t('raw_siftite')}})
    for kind, time in (('smelting', 200), ('blasting', 100)):
        for src in ('raw_siftite', 'siftite_ore'):
            recipe('siftite_ingot_from_%s_%s' % (kind, src), {'type': 'minecraft:' + kind, 'category': 'misc', 'cookingtime': time,
                    'experience': 1.0, 'group': 'siftite_ingot', 'ingredient': t(src), 'result': {'id': t('siftite_ingot')}})
    for tool in ('sword', 'pickaxe', 'axe', 'shovel', 'hoe'):
        data = vload('recipe/diamond_%s.json' % tool)
        data = transform(data, lambda s: '#thesift:siftite_tool_materials' if s == '#minecraft:diamond_tool_materials' else s.replace('minecraft:diamond_', 'thesift:siftite_'))
        recipe('siftite_' + tool, data)
    for piece in ('helmet', 'chestplate', 'leggings', 'boots'):
        data = vload('recipe/diamond_%s.json' % piece)
        recipe('siftite_' + piece, transform(data, lambda s: t('siftite_ingot') if s == 'minecraft:diamond' else s.replace('minecraft:diamond_', 'thesift:siftite_')))
        recipe('carapace_' + piece, transform(data, lambda s: t('carapace_scale') if s == 'minecraft:diamond' else s.replace('minecraft:diamond_', 'thesift:carapace_')))
    recipe('resonant_blade', {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'pattern': [' C ', 'SBS', ' S '],
                              'key': {'C': t('harmonic_core'), 'B': t('siftite_sword'), 'S': t('soul_crystal_block')},
                              'result': {'id': t('resonant_blade')}})

def gen_recipes_extra():
    def block9(block, item):
        shaped(block, ['###', '###', '###'], {'#': t(item)}, t(block))
        recipe(item + '_from_' + block, {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [t(block)], 'result': {'count': 9, 'id': t(item)}})
    shaped('polished_veilstone', ['##', '##'], {'#': t('veilstone')}, t('polished_veilstone'), 4)
    shaped('polished_rose_marble', ['##', '##'], {'#': t('rose_marble')}, t('polished_rose_marble'), 4)
    shaped('rose_marble_bricks', ['##', '##'], {'#': t('polished_rose_marble')}, t('rose_marble_bricks'), 4)
    recipe('deep_sift_stone_from_smelting', {'type': 'minecraft:smelting', 'category': 'blocks', 'cookingtime': 200, 'experience': 0.1,
                                             'ingredient': t('cobbled_deep_sift_stone'), 'result': {'id': t('deep_sift_stone')}})
    block9('lumenite_block', 'lumenite_shard')
    block9('jade_block', 'jade')
    for kind, time in (('smelting', 200), ('blasting', 100)):
        for ore, out, xp in (('sift_coal_ore', 'minecraft:coal', 0.1), ('sift_copper_ore', 'minecraft:copper_ingot', 0.7),
                             ('sift_iron_ore', 'minecraft:iron_ingot', 0.7), ('sift_gold_ore', 'minecraft:gold_ingot', 1.0),
                             ('sift_diamond_ore', 'minecraft:diamond', 1.0), ('lumenite_ore', t('lumenite_shard'), 0.7), ('jade_ore', t('jade'), 1.0)):
            recipe('%s_from_%s' % (out.split(':')[1], kind + '_' + ore), {'type': 'minecraft:' + kind, 'category': 'misc', 'cookingtime': time,
                   'experience': xp, 'ingredient': t(ore), 'result': {'id': out}})
    recipe('echo_eye', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'result': {'id': t('echo_eye'), 'count': 2},
                        'ingredients': ['minecraft:ender_eye', 'minecraft:echo_shard', t('soul_shard')]})
    for raw, cooked in (('bumblehog_chop', 'cooked_bumblehog_chop'), ('venison', 'cooked_venison'), ('ray_fillet', 'cooked_ray_fillet'), ('duskfruit', 'roasted_duskfruit')):
        for kind, time in (('smelting', 200), ('smoking', 100), ('campfire_cooking', 600)):
            recipe('%s_from_%s' % (cooked, kind), {'type': 'minecraft:' + kind, 'category': 'food', 'cookingtime': time, 'experience': 0.35,
                   'ingredient': t(raw), 'result': {'id': t(cooked)}})
    for piece in ('helmet', 'chestplate', 'leggings', 'boots'):
        data = vload('recipe/diamond_%s.json' % piece)
        for mat, item in (('lumenite', 'lumenite_shard'), ('jade', 'jade'), ('warden', 'warden_hide')):
            recipe(mat + '_' + piece, transform(data, lambda x, i=item, m=mat: t(i) if x == 'minecraft:diamond' else x.replace('minecraft:diamond_', 'thesift:%s_' % m)))
    recipe('glass_from_sift_sand', {'type': 'minecraft:smelting', 'category': 'blocks', 'cookingtime': 200, 'experience': 0.1,
                                    'ingredient': t('sift_sand'), 'result': {'id': 'minecraft:glass'}})
    recipe('blub_jelly_block', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [t('blub_jelly')] * 9,
                                'result': {'id': 'minecraft:slime_block'}})

# ==========================================================================================
# Enchantments
# ==========================================================================================
def gen_enchantments():
    def ench(name, slots, supported, effects, max_level=3, weight=3, anvil=4, primary=None):
        e = {'anvil_cost': anvil, 'description': {'translate': 'enchantment.thesift.' + name}, 'effects': effects,
             'max_cost': {'base': 50, 'per_level_above_first': 10}, 'max_level': max_level,
             'min_cost': {'base': 10, 'per_level_above_first': 10}, 'slots': slots, 'supported_items': supported, 'weight': weight}
        if primary:
            e['primary_items'] = primary
        write('%s/enchantment/%s.json' % (NS, name), e)
    direct = {'type': 'minecraft:damage_source_properties', 'predicate': {'is_direct': True}}
    def mob_effect(effect, seconds, amp=0.0):
        return {'type': 'minecraft:apply_mob_effect', 'to_apply': effect, 'min_duration': seconds, 'max_duration': seconds,
                'min_amplifier': amp, 'max_amplifier': amp}
    def attr(attribute, base, per, op='add_value'):
        return {'minecraft:attributes': [{'id': 'thesift:enchantment.' + attribute.split(':')[1], 'attribute': attribute, 'operation': op,
                                          'amount': {'type': 'minecraft:linear', 'base': base, 'per_level_above_first': per}}]}
    ench('soul_siphon', ['mainhand'], '#minecraft:enchantable/melee_weapon', {'minecraft:post_attack': [
        {'affected': 'attacker', 'enchanted': 'attacker', 'effect': mob_effect('minecraft:regeneration', 2.0, 0.0),
         'requirements': {'type': 'minecraft:all_of', 'terms': [direct, {'type': 'minecraft:random_chance', 'chance': {'type': 'minecraft:enchantment_level', 'amount': {'type': 'minecraft:linear', 'base': 0.2, 'per_level_above_first': 0.15}}}]}}]},
        weight=2, primary='#minecraft:swords')
    ench('resonance', ['mainhand'], '#minecraft:enchantable/melee_weapon', {'minecraft:post_attack': [
        {'affected': 'victim', 'enchanted': 'attacker', 'effect': {'type': 'minecraft:all_of', 'effects': [
            mob_effect('minecraft:glowing', 4.0), mob_effect('minecraft:slowness', 2.0)]}, 'requirements': direct}]},
        max_level=2, primary='#minecraft:swords')
    ench('ichor_stride', ['feet'], '#minecraft:enchantable/foot_armor', attr('minecraft:water_movement_efficiency', 0.33, 0.33), weight=2)
    ench('featherstep', ['feet'], '#minecraft:enchantable/foot_armor', attr('minecraft:safe_fall_distance', 2.0, 2.0))
    ench('lumen_sight', ['head'], '#minecraft:enchantable/head_armor', {'minecraft:tick': [
        {'effect': mob_effect('minecraft:night_vision', 15.0), 'requirements': {'type': 'minecraft:entity_properties', 'entity': 'this',
         'predicate': {'minecraft:periodic_tick': 100}}}]}, max_level=1, weight=1, anvil=8)
    ench('reach', ['mainhand'], '#minecraft:enchantable/mining', attr('minecraft:block_interaction_range', 0.75, 0.75), max_level=3, weight=2)
    ench('vigor', ['chest'], '#minecraft:enchantable/chest_armor', attr('minecraft:max_health', 2.0, 2.0), max_level=3, weight=1, anvil=8)

ENCHANTS = ['soul_siphon', 'resonance', 'ichor_stride', 'featherstep', 'lumen_sight', 'reach', 'vigor']

def gen_enchantment_tags():
    tag('enchantment', 'non_treasure', [t(e) for e in ENCHANTS])
    tag('enchantment', 'tooltip_order', [t(e) for e in ENCHANTS])

# ==========================================================================================
# Tags
# ==========================================================================================
def tag(kind, name, values, ns='minecraft'):
    write('%s/tags/%s/%s.json' % (ns, kind, name), {'replace': False, 'values': values})

def gen_tags():
    soils = [t('blushing_moss'), t('glowmoss'), t('verdant_turf'), t('rust_soil'), t('meadow_rock'), t('dusk_turf'), t('pearl_moss'), t('carapace_sand'), t('sift_sand')]
    ores = ['siftite_ore', 'sift_coal_ore', 'sift_copper_ore', 'sift_iron_ore', 'sift_gold_ore', 'sift_diamond_ore', 'lumenite_ore', 'jade_ore']
    stones = [t(n) for n in ['sift_stone', 'cobbled_sift_stone', 'polished_sift_stone', 'sift_stone_bricks', 'sift_stone_brick_stairs',
                             'sift_stone_brick_slab', 'sift_stone_brick_wall', 'chiseled_sift_stone_bricks', 'meadow_rock',
                             'orange_carapace', 'red_carapace', 'blue_carapace', 'polished_carapace', 'soul_crystal_block',
                             'soul_crystal_cluster', 'raw_siftite_block', 'siftite_block', 'meadow_rock_bricks',
                             'carapace_bricks', 'sift_lantern', 'lumenite_block', 'jade_block', 'sift_altar', 'deep_sift_stone',
                             'cobbled_deep_sift_stone', 'veilstone', 'polished_veilstone', 'rose_marble', 'polished_rose_marble',
                             'rose_marble_bricks'] + ores]
    tag('block', 'mineable/pickaxe', stones)
    tag('block', 'needs_diamond_tool', [t('siftite_ore'), t('raw_siftite_block'), t('siftite_block'), t('sift_diamond_ore'), t('jade_ore')])
    tag('block', 'needs_iron_tool', [t('sift_gold_ore'), t('lumenite_ore')])
    tag('block', 'needs_stone_tool', [t('sift_iron_ore'), t('sift_copper_ore')])
    tag('block', 'mineable/shovel', [t('blushing_moss'), t('glowmoss'), t('carapace_sand'), t('rust_soil'), t('verdant_turf'),
                                     t('dusk_turf'), t('pearl_moss'), t('sift_sand')])
    tag('block', 'beacon_base_blocks', [t('siftite_block'), t('jade_block')])
    tag('item', 'beacon_payment_items', [t('siftite_ingot')])
    tag('item', 'siftite_tool_materials', [t('siftite_ingot')], NS)
    tag('item', 'repairs_siftite_armor', [t('siftite_ingot')], NS)
    tag('item', 'repairs_carapace_armor', [t('carapace_scale')], NS)
    tag('item', 'repairs_lumenite_armor', [t('lumenite_shard')], NS)
    tag('item', 'repairs_jade_armor', [t('jade')], NS)
    tag('item', 'repairs_warden_armor', [t('warden_hide')], NS)
    # gear the shrine altars accept
    tag('item', 'siftite_equipment', [t('siftite_' + n) for n in ('sword', 'pickaxe', 'axe', 'shovel', 'hoe', 'helmet', 'chestplate', 'leggings', 'boots')]
        + [t('resonant_blade')], NS)
    swords = ['siftite_sword', 'resonant_blade', 'warden_cleaver']
    for kind, items in (('swords', swords), ('pickaxes', ['siftite_pickaxe']), ('axes', ['siftite_axe']),
                        ('shovels', ['siftite_shovel']), ('hoes', ['siftite_hoe'])):
        tag('item', kind, [t(i) for i in items])
    for kind, piece in (('head_armor', 'helmet'), ('chest_armor', 'chestplate'), ('leg_armor', 'leggings'), ('foot_armor', 'boots')):
        pieces = [t(m + '_' + piece) for m in ARMOR_SETS]
        tag('item', kind, pieces)
        tag('item', 'trimmable_armor', pieces)
    tag('block', 'mineable/hoe', [t(w + '_leaves') for w in WOODS])
    tag('block', 'supports_vegetation', soils[:7])
    tag('block', 'cannot_replace_below_tree_trunk', soils)
    tag('block', 'base_stone_overworld', [t('sift_stone'), t('deep_sift_stone'), t('veilstone'), t('rose_marble')])
    tag('block', 'stone_ore_replaceables', [t('sift_stone'), t('deep_sift_stone')], NS)
    tag('block', 'arrival_ground', soils + [t('glowmoss'), t('sift_stone'), t('orange_carapace'), t('red_carapace')], NS)
    tag('block', 'mineable/shovel', [t('blushing_moss'), t('glowmoss'), t('carapace_sand'), t('rust_soil'), t('verdant_turf'),
                                     t('dusk_turf'), t('pearl_moss'), t('sift_sand'), t('sift_gravel')])
    axe = []
    for wood in WOODS:
        W = lambda n, w=wood: t(w + '_' + n)
        logs = [W('log'), W('wood'), t('stripped_%s_log' % wood), t('stripped_%s_wood' % wood)]
        tag('block', wood + '_logs', logs, NS)
        tag('item', wood + '_logs', logs, NS)
        axe += ['#%s:%s_logs' % (NS, wood)] + [W(n) for n in ['planks', 'stairs', 'slab', 'fence', 'fence_gate', 'door', 'trapdoor', 'button', 'pressure_plate']]
    tag('block', 'mineable/axe', axe)
    tag('item', 'boats', [t(w + '_boat') for w in WOODS])
    tag('item', 'chest_boats', [t(w + '_chest_boat') for w in WOODS])
    def each(n):
        return [t(w + '_' + n) for w in WOODS]
    for kind in ('block', 'item'):
        tag(kind, 'logs_that_burn', ['#%s:%s_logs' % (NS, w) for w in WOODS])
        tag(kind, 'planks', each('planks'))
        tag(kind, 'wooden_stairs', each('stairs'))
        tag(kind, 'wooden_slabs', each('slab'))
        tag(kind, 'wooden_fences', each('fence'))
        tag(kind, 'fence_gates', each('fence_gate'))
        tag(kind, 'wooden_doors', each('door'))
        tag(kind, 'wooden_trapdoors', each('trapdoor'))
        tag(kind, 'wooden_buttons', each('button'))
        tag(kind, 'wooden_pressure_plates', each('pressure_plate'))
        tag(kind, 'leaves', each('leaves'))
        tag(kind, 'saplings', each('sapling'))
        tag(kind, 'small_flowers', [t('lullaby_bloom'), t('echo_bloom'), t('bluebell'), t('pale_lily'), t('glowcap')])
        tag(kind, 'flowers', [t('glimmer_petals')])
        tag(kind, 'stairs', [t('sift_stone_brick_stairs')])
        tag(kind, 'slabs', [t('sift_stone_brick_slab')])
        tag(kind, 'walls', [t('sift_stone_brick_wall')])
    tag('block', 'sand', [t('sift_sand')])
    tag('item', 'coal_ores', [t('sift_coal_ore')]); tag('item', 'iron_ores', [t('sift_iron_ore')])
    tag('item', 'copper_ores', [t('sift_copper_ore')]); tag('item', 'gold_ores', [t('sift_gold_ore')]); tag('item', 'diamond_ores', [t('sift_diamond_ore')])
    for kind in ('block',):
        tag(kind, 'coal_ores', [t('sift_coal_ore')]); tag(kind, 'iron_ores', [t('sift_iron_ore')])
        tag(kind, 'copper_ores', [t('sift_copper_ore')]); tag(kind, 'gold_ores', [t('sift_gold_ore')]); tag(kind, 'diamond_ores', [t('sift_diamond_ore')])
    tag('block', 'wither_immune', [t('sift_portal'), t('temple_bricks')])
    tag('block', 'dragon_immune', [t('sift_portal'), t('temple_bricks')])
    solid = stones + soils + [t(n) for n in ('temple_bricks', 'soul_crystal_block', 'sift_gravel')]
    tag('block', 'blocks_motion_no_leaves', sorted(set(solid) - {t('soul_crystal_cluster')}))
    gen_enchantment_tags()
    tag('block', 'replaceable', [t('blush_grass'), t('tall_blush_grass'), t('glow_sprouts'), t('mint_grass'), t('tall_mint_grass')])
    tag('block', 'replaceable_by_trees', [t('blush_grass'), t('tall_blush_grass'), t('glow_sprouts'), t('mint_grass'), t('tall_mint_grass')])
    tag('block', 'portals', [t('sift_portal')])
    # ichor swims like water
    tag('fluid', 'water', [t('ichor'), t('flowing_ichor')])
    tag('entity_type', 'can_breathe_under_water', [BLUB, RAY])

# ==========================================================================================
# Structures
# ==========================================================================================
def gen_chest_loot():
    def item(name, w, lo=1, hi=1, extra=None):
        e = {'type': 'minecraft:item', 'name': name, 'weight': w}
        mods = []
        if hi > 1:
            mods.append({'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}})
        if extra:
            mods.append(extra)
        if mods:
            e['modifier'] = mods
        return e
    def chest(name, pools):
        write('%s/loot_table/chests/%s.json' % (NS, name), {'type': 'minecraft:chest', 'pools': pools, 'random_sequence': '%s:chests/%s' % (NS, name)})
    enchant = {'type': 'minecraft:enchant_randomly'}
    chest('sift_shrine', [
        {'rolls': {'type': 'minecraft:uniform', 'min': 4, 'max': 7}, 'entries': [
            item(t('soul_shard'), 20, 2, 7), item(t('raw_siftite'), 6, 1, 3), item(t('ichor_bottle'), 10, 1, 2),
            item('minecraft:golden_apple', 4), item(t('sift_lantern'), 8, 1, 2), item('minecraft:gold_ingot', 12, 2, 6),
            item('minecraft:diamond', 3, 1, 2), item(t('lullaby_bloom'), 10, 1, 4), item('minecraft:book', 6, 1, 1, enchant)]}])
    chest('willow_camp', [
        {'rolls': {'type': 'minecraft:uniform', 'min': 3, 'max': 6}, 'entries': [
            item('minecraft:bread', 15, 1, 4), item(t('sift_willow_sapling'), 10, 1, 3), item(t('carapace_scale'), 8, 1, 3),
            item(t('ichor_bottle'), 6, 1, 1), item('minecraft:arrow', 10, 4, 12), item(t('soul_shard'), 10, 1, 4),
            item('minecraft:compass', 3), item(t('glimmer_petals'), 8, 1, 6)]}])
    chest('sift_village', [
        {'rolls': {'type': 'minecraft:uniform', 'min': 3, 'max': 7}, 'entries': [
            item('minecraft:bread', 15, 1, 4), item(t('duskfruit'), 12, 1, 4), item(t('cooked_venison'), 8, 1, 3), item(t('lumenite_shard'), 6, 1, 4),
            item(t('jade'), 3, 1, 2), item(t('pearlbark_sapling'), 6, 1, 2), item(t('duskwood_sapling'), 6, 1, 2), item('minecraft:emerald', 8, 1, 4),
            item(t('soul_shard'), 10, 1, 5)]}])
    chest('warden_temple', [
        {'rolls': {'type': 'minecraft:uniform', 'min': 6, 'max': 10}, 'entries': [
            item(t('siftite_ingot'), 10, 2, 6), item('minecraft:diamond', 10, 3, 8), item(t('jade'), 8, 2, 6), item('minecraft:enchanted_golden_apple', 3),
            item('minecraft:echo_shard', 8, 2, 6), item(t('warden_hide'), 8, 1, 3), item(t('siftite_chestplate'), 3, 1, 1, {'type': 'minecraft:enchant_with_levels', 'levels': 40}),
            item(t('siftite_sword'), 3, 1, 1, {'type': 'minecraft:enchant_with_levels', 'levels': 40}), item('minecraft:book', 8, 1, 1, {'type': 'minecraft:enchant_with_levels', 'levels': 40}),
            item('minecraft:netherite_upgrade_smithing_template', 2), item(t('siftite_block'), 2), item(t('warden_heart'), 2),
            item(t('warden_cleaver'), 1, 1, 1, {'type': 'minecraft:enchant_with_levels', 'levels': 30}), item(t('harmonic_core'), 2)]}])

def gen_structures():
    structures.build_all(ROOT)
    land = ['singers_meadow', 'lullaby_hills', 'the_carapace']
    def jigsaw(name, biomes, spacing, separation, salt, adaptation='beard_thin', step='surface_structures'):
        tag('worldgen/biome', 'has_structure/' + name, [t(b) for b in biomes], NS)
        write('%s/worldgen/template_pool/%s.json' % (NS, name), {'fallback': 'minecraft:empty', 'elements': [{'weight': 1, 'element': {
            'element_type': 'minecraft:single_pool_element', 'location': t(name), 'processors': {'processors': []}, 'projection': 'rigid'}}]})
        write('%s/worldgen/structure/%s.json' % (NS, name), {
            'type': 'minecraft:jigsaw', 'biomes': '#%s:has_structure/%s' % (NS, name), 'max_distance_from_center': 80,
            'project_start_to_heightmap': 'WORLD_SURFACE_WG', 'size': 1, 'spawn_overrides': {}, 'start_height': {'absolute': 0},
            'start_pool': t(name), 'step': step, 'terrain_adaptation': adaptation, 'use_expansion_hack': False})
        write('%s/worldgen/structure_set/%s.json' % (NS, name), {
            'placement': {'type': 'minecraft:random_spread', 'salt': salt, 'separation': separation, 'spacing': spacing},
            'structures': [{'structure': t(name), 'weight': 1}]})
    everywhere = ['singers_meadow', 'lullaby_hills', 'the_carapace', 'verdant_isles', 'sift_jungle', 'pearl_grove']
    jigsaw('sift_shrine', everywhere, 32, 12, 26043001)
    jigsaw('willow_camp', ['singers_meadow', 'lullaby_hills', 'pearl_grove'], 20, 6, 26043002)
    jigsaw('forgotten_shrine', everywhere, 14, 5, 26043004)
    jigsaw('warden_temple', everywhere, 48, 16, 26043006, adaptation='beard_box')
    tag('worldgen/structure', 'echo_eye_located', [t('warden_temple')], NS)
    tag('worldgen/structure', 'ancient_cities', ['minecraft:ancient_city'], NS)
    jigsaw('bone_arch', ['the_carapace'], 10, 4, 26043007, adaptation='bury')

# axe stripping is data driven in 26.3: append our logs to the vanilla "axe" block transformer
def gen_block_transformer():
    data = vload('block_transformer/axe.json')
    strip = data[0]['block_state_provider']['rules']
    for src, dst in [(w + k, 'stripped_' + w + k) for w in WOODS for k in ('_log', '_wood')]:
        strip.append({'if_true': {'type': 'minecraft:matching_blocks', 'blocks': t(src)},
                      'then': {'type': 'minecraft:copy_properties', 'source': {'id': t(dst), 'properties': {'axis': 'y'}}}})
    write('minecraft/block_transformer/axe.json', data)

if __name__ == '__main__':
    if os.path.isdir(ROOT):
        shutil.rmtree(ROOT)
    patch_ancient_city()
    gen_dimension()
    gen_features()
    gen_features_extra()
    gen_biomes()
    gen_loot()
    gen_recipes()
    gen_tags()
    gen_block_transformer()
    gen_enchantments()
    gen_structures()
    print('data done')
