"""Generates blockstates, models, item definitions and the language file for The Sift."""
import json, os

SCRATCH = '/tmp/claude-1000/-home-cat-Dokumente-Minecraft-The-Sift/cfaeee90-d691-4710-b54e-e87b2d62cad0/scratchpad'
VAN = SCRATCH + '/mc/data/assets/minecraft/'
OUT = '/home/cat/Dokumente/Minecraft The Sift/src/main/resources/assets/thesift/'
NS = 'thesift'

def write(rel, obj):
    path = OUT + rel
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')

def vload(rel):
    with open(VAN + rel) as f:
        return json.load(f)

# ------------------------------------------------------------------------------------------
# Copying vanilla families with a name substitution (keeps 26.3's exact model formats)
# ------------------------------------------------------------------------------------------
def renamer(old, new):
    def rename(s):
        if old not in s:
            return s
        if s.startswith('minecraft:'):
            s = NS + ':' + s[len('minecraft:'):]
        elif s.startswith('block/') or s.startswith('item/'):
            s = NS + ':' + s
        return s.replace(old, new)
    return rename

def transform(obj, fn):
    if isinstance(obj, dict):
        return {k: transform(v, fn) for k, v in obj.items()}
    if isinstance(obj, list):
        return [transform(v, fn) for v in obj]
    if isinstance(obj, str):
        return fn(obj)
    return obj

def collect_models(obj, out):
    if isinstance(obj, dict):
        for k, v in obj.items():
            if k in ('model', 'parent') and isinstance(v, str) and v.startswith(NS + ':'):
                out.add(v)
            else:
                collect_models(v, out)
    elif isinstance(obj, list):
        for v in obj:
            collect_models(v, out)

def copy_family(vname, old, new):
    """Copies blockstate + all referenced models + item definition of a vanilla block."""
    fn = renamer(old, new)
    nname = vname.replace(old, new)
    bs = transform(vload('blockstates/%s.json' % vname), fn)
    write('blockstates/%s.json' % nname, bs)
    item = {}
    if os.path.exists(VAN + 'items/%s.json' % vname):
        item = transform(vload('items/%s.json' % vname), fn)
        write('items/%s.json' % nname, item)
    todo = set()
    collect_models(bs, todo)
    collect_models(item, todo)
    done = set()
    while todo:
        m = todo.pop()
        if m in done:
            continue
        done.add(m)
        path = m[len(NS) + 1:]                    # block/sift_willow_log_x
        vpath = path.replace(new, old)
        model = transform(vload('models/%s.json' % vpath), fn)
        write('models/%s.json' % path, model)
        collect_models(model, todo)

# ------------------------------------------------------------------------------------------
# Simple generated models
# ------------------------------------------------------------------------------------------
def simple_block(name, parent='minecraft:block/cube_all', textures=None):
    textures = textures or {'all': '%s:block/%s' % (NS, name)}
    write('models/block/%s.json' % name, {'parent': parent, 'textures': textures})
    write('blockstates/%s.json' % name, {'variants': {'': {'model': '%s:block/%s' % (NS, name)}}})
    write('items/%s.json' % name, {'model': {'type': 'minecraft:model', 'model': '%s:block/%s' % (NS, name)}})

def cross_plant(name, texture=None, item_texture=None):
    texture = texture or name
    write('models/block/%s.json' % name, {'parent': 'minecraft:block/cross', 'textures': {'cross': '%s:block/%s' % (NS, texture)}})
    write('blockstates/%s.json' % name, {'variants': {'': {'model': '%s:block/%s' % (NS, name)}}})
    generated_item(name, 'block/' + (item_texture or texture))

def generated_item(name, texture=None):
    texture = texture or 'item/' + name
    write('models/item/%s.json' % name, {'parent': 'minecraft:item/generated', 'textures': {'layer0': '%s:%s' % (NS, texture)}})
    write('items/%s.json' % name, {'model': {'type': 'minecraft:model', 'model': '%s:item/%s' % (NS, name)}})

def handheld_item(name):
    write('models/item/%s.json' % name, {'parent': 'minecraft:item/handheld', 'textures': {'layer0': '%s:item/%s' % (NS, name)}})
    write('items/%s.json' % name, {'model': {'type': 'minecraft:model', 'model': '%s:item/%s' % (NS, name)}})

def gen_expansion(lang):
    for name in ['siftite_ore', 'raw_siftite_block', 'siftite_block', 'meadow_rock_bricks', 'carapace_bricks', 'rust_soil']:
        simple_block(name)
    simple_block('verdant_turf', 'minecraft:block/cube_bottom_top', {
        'top': NS + ':block/verdant_turf_top', 'side': NS + ':block/verdant_turf_side', 'bottom': NS + ':block/rust_soil'})
    copy_family('soul_lantern', 'soul_lantern', 'sift_lantern')
    copy_family('pink_petals', 'pink_petals', 'glimmer_petals')
    cross_plant('bluebell')
    cross_plant('pale_lily')
    for name in ['raw_siftite', 'siftite_ingot', 'carapace_scale', 'harmonic_core',
                 'nuzzle_spawn_egg', 'singer_spawn_egg']:
        generated_item(name)
    for name in ['siftite_sword', 'siftite_pickaxe', 'siftite_axe', 'siftite_shovel', 'siftite_hoe', 'resonant_blade']:
        handheld_item(name)
    for mat in ['siftite', 'carapace']:
        for piece in ['helmet', 'chestplate', 'leggings', 'boots']:
            generated_item('%s_%s' % (mat, piece))
        write('equipment/%s.json' % mat, {'layers': {
            'humanoid': [{'texture': '%s:%s' % (NS, mat)}],
            'humanoid_leggings': [{'texture': '%s:%s' % (NS, mat)}],
            'humanoid_baby': [{'texture': '%s:%s' % (NS, mat)}]}})

    lang.update({
        'block.thesift.siftite_ore': 'Siftite Ore',
        'block.thesift.raw_siftite_block': 'Block of Raw Siftite',
        'block.thesift.siftite_block': 'Block of Siftite',
        'block.thesift.meadow_rock_bricks': 'Meadow Rock Bricks',
        'block.thesift.carapace_bricks': 'Carapace Bricks',
        'block.thesift.sift_lantern': 'Sift Lantern',
        'block.thesift.rust_soil': 'Rust Soil',
        'block.thesift.verdant_turf': 'Verdant Turf',
        'block.thesift.glimmer_petals': 'Glimmer Petals',
        'block.thesift.bluebell': 'Bluebell',
        'block.thesift.pale_lily': 'Pale Lily',
        'item.thesift.raw_siftite': 'Raw Siftite',
        'item.thesift.siftite_ingot': 'Siftite Ingot',
        'item.thesift.carapace_scale': 'Carapace Scale',
        'item.thesift.harmonic_core': 'Harmonic Core',
        'item.thesift.siftite_sword': 'Siftite Sword',
        'item.thesift.siftite_pickaxe': 'Siftite Pickaxe',
        'item.thesift.siftite_axe': 'Siftite Axe',
        'item.thesift.siftite_shovel': 'Siftite Shovel',
        'item.thesift.siftite_hoe': 'Siftite Hoe',
        'item.thesift.resonant_blade': 'Resonant Blade',
        'item.thesift.siftite_helmet': 'Siftite Helmet',
        'item.thesift.siftite_chestplate': 'Siftite Chestplate',
        'item.thesift.siftite_leggings': 'Siftite Leggings',
        'item.thesift.siftite_boots': 'Siftite Boots',
        'item.thesift.carapace_helmet': 'Carapace Helmet',
        'item.thesift.carapace_chestplate': 'Carapace Chestplate',
        'item.thesift.carapace_leggings': 'Carapace Leggings',
        'item.thesift.carapace_boots': 'Carapace Boots',
        'item.thesift.nuzzle_spawn_egg': 'Nuzzle Spawn Egg',
        'item.thesift.singer_spawn_egg': 'Singer Spawn Egg',
        'entity.thesift.nuzzle': 'Nuzzle',
        'entity.thesift.singer': 'Singer',
        'tag.item.thesift.siftite_tool_materials': 'Siftite Tool Materials',
        'tag.item.thesift.repairs_siftite_armor': 'Repairs Siftite Armor',
        'tag.item.thesift.repairs_carapace_armor': 'Repairs Carapace Armor',
    })

def title(name):
    return ' '.join(w.capitalize() for w in name.split('_'))

def gen_v4(lang):
    for wood in ('duskwood', 'pearlbark'):
        for v in ['cherry_log', 'cherry_wood', 'stripped_cherry_log', 'stripped_cherry_wood', 'cherry_planks', 'cherry_leaves',
                  'cherry_sapling', 'cherry_stairs', 'cherry_slab', 'cherry_fence', 'cherry_fence_gate', 'cherry_door',
                  'cherry_trapdoor', 'cherry_button', 'cherry_pressure_plate']:
            copy_family(v, 'cherry', wood)
            n = v.replace('cherry', wood)
            lang['block.thesift.' + n] = title(n)
    for wood in ('sift_willow', 'duskwood', 'pearlbark'):
        for kind in ('boat', 'chest_boat'):
            generated_item('%s_%s' % (wood, kind))
            lang['item.thesift.%s_%s' % (wood, kind)] = title('%s_%s' % (wood, kind)).replace('Chest Boat', 'Boat with Chest')
    for name in ['sift_sand', 'sift_coal_ore', 'sift_copper_ore', 'sift_iron_ore', 'sift_gold_ore', 'sift_diamond_ore',
                 'lumenite_ore', 'jade_ore', 'lumenite_block', 'jade_block', 'temple_bricks']:
        simple_block(name)
    for name in ['dusk_turf', 'pearl_moss']:
        simple_block(name, 'minecraft:block/cube_bottom_top', {
            'top': NS + ':block/%s_top' % name, 'side': NS + ':block/%s_side' % name, 'bottom': NS + ':block/rust_soil'})
    simple_block('sift_altar', 'minecraft:block/cube_bottom_top', {
        'top': NS + ':block/sift_altar_top', 'side': NS + ':block/sift_altar_side', 'bottom': NS + ':block/sift_altar_bottom'})

    items = ['echo_eye', 'bumblehog_chop', 'cooked_bumblehog_chop', 'shade_spawn_egg', 'gloomstalker_spawn_egg', 'bumblehog_spawn_egg', 'lumenite_shard', 'jade', 'warden_hide', 'warden_heart', 'duskfruit', 'roasted_duskfruit', 'venison', 'cooked_venison',
             'ray_fillet', 'cooked_ray_fillet', 'blub_jelly', 'shellback_spawn_egg', 'lumoth_spawn_egg', 'pearl_stag_spawn_egg',
             'ichor_ray_spawn_egg']
    for name in items:
        generated_item(name)
    handheld_item('warden_cleaver')
    cross_plant('mint_grass')
    cross_plant('glowcap')
    for n in ['deep_sift_stone', 'cobbled_deep_sift_stone', 'veilstone', 'polished_veilstone', 'rose_marble', 'polished_rose_marble',
              'rose_marble_bricks', 'sift_gravel']:
        simple_block(n)
        lang['block.thesift.' + n] = title(n)
    lang['block.thesift.glowcap'] = 'Glowcap'
    for half in ('top', 'bottom'):
        write('models/block/tall_mint_grass_%s.json' % half, {'parent': 'minecraft:block/cross', 'textures': {'cross': NS + ':block/tall_mint_grass_' + half}})
    write('blockstates/tall_mint_grass.json', {'variants': {
        'half=lower': {'model': NS + ':block/tall_mint_grass_bottom'}, 'half=upper': {'model': NS + ':block/tall_mint_grass_top'}}})
    generated_item('tall_mint_grass', 'block/tall_mint_grass_top')
    lang['block.thesift.mint_grass'] = 'Mint Grass'
    lang['block.thesift.tall_mint_grass'] = 'Tall Mint Grass'
    for mat in ['lumenite', 'jade', 'warden']:
        for piece in ['helmet', 'chestplate', 'leggings', 'boots']:
            generated_item('%s_%s' % (mat, piece))
            lang['item.thesift.%s_%s' % (mat, piece)] = title('%s_%s' % (mat, piece))
        write('equipment/%s.json' % mat, {'layers': {
            'humanoid': [{'texture': '%s:%s' % (NS, mat)}],
            'humanoid_leggings': [{'texture': '%s:%s' % (NS, mat)}],
            'humanoid_baby': [{'texture': '%s:%s' % (NS, mat)}]}})
    for name in items + ['warden_cleaver']:
        lang['item.thesift.' + name] = title(name)
    for name in ['sift_sand', 'sift_coal_ore', 'sift_copper_ore', 'sift_iron_ore', 'sift_gold_ore', 'sift_diamond_ore',
                 'lumenite_ore', 'jade_ore', 'temple_bricks', 'dusk_turf', 'pearl_moss', 'sift_altar']:
        lang['block.thesift.' + name] = title(name)
    lang.update({
        'block.thesift.lumenite_block': 'Block of Lumenite',
        'block.thesift.jade_block': 'Block of Jade',
        'block.thesift.temple_bricks': 'Ancient Temple Bricks',
        'item.thesift.echo_eye': 'Echo Eye',
        'item.thesift.bumblehog_chop': 'Raw Bumblehog Chop',
        'item.thesift.cooked_bumblehog_chop': 'Cooked Bumblehog Chop',
        'item.thesift.shade_spawn_egg': 'Shade Spawn Egg',
        'item.thesift.gloomstalker_spawn_egg': 'Gloomstalker Spawn Egg',
        'item.thesift.bumblehog_spawn_egg': 'Bumblehog Spawn Egg',
        'entity.thesift.shade': 'Shade',
        'entity.thesift.gloomstalker': 'Gloomstalker',
        'entity.thesift.bumblehog': 'Bumblehog',        'block.thesift.sift_altar': 'Sift Altar',
        'block.thesift.sift_altar.only_siftite': 'Try again with Siftite gear!',
        'block.thesift.sift_altar.hint': 'Hold siftite gear and right-click: %s Soul Shards + %s levels re-roll its enchantments.',
        'block.thesift.sift_altar.cost': 'The souls ask for %s Soul Shards and %s levels.',
        'item.thesift.warden_hide': 'Ancient Warden Hide',
        'item.thesift.warden_heart': 'Heart of the Ancient Warden',
        'item.thesift.warden_cleaver': 'Ancient Warden Cleaver',
        'item.thesift.warden_helmet': 'Warden Hide Helmet',
        'item.thesift.warden_chestplate': 'Warden Hide Chestplate',
        'item.thesift.warden_leggings': 'Warden Hide Leggings',
        'item.thesift.warden_boots': 'Warden Hide Boots',
        'entity.thesift.shellback': 'Shellback',
        'entity.thesift.lumoth': 'Lumoth',
        'entity.thesift.pearl_stag': 'Pearl Stag',
        'entity.thesift.ichor_ray': 'Ichor Ray',
        'biome.thesift.verdant_isles': 'Mint Arches',
        'biome.thesift.sift_jungle': 'Sift Jungle',
        'biome.thesift.pearl_grove': 'Pearl Grove',
        'biome.thesift.ichor_sea': 'Ichor Sea',
        'effect.thesift.ichor_drowning': 'Ichor Drowning',
        'enchantment.thesift.soul_siphon': 'Soul Siphon',
        'enchantment.thesift.resonance': 'Resonance',
        'enchantment.thesift.ichor_stride': 'Ichor Stride',
        'enchantment.thesift.featherstep': 'Featherstep',
        'enchantment.thesift.lumen_sight': 'Lumen Sight',
        'enchantment.thesift.reach': 'Reach',
        'enchantment.thesift.vigor': 'Vigor',
        'tag.item.thesift.siftite_equipment': 'Siftite Equipment',
        'tag.item.thesift.repairs_lumenite_armor': 'Repairs Lumenite Armor',
        'tag.item.thesift.repairs_jade_armor': 'Repairs Jade Armor',
        'tag.item.thesift.repairs_warden_armor': 'Repairs Warden Armor',
        'tag.item.thesift.duskwood_logs': 'Duskwood Logs',
        'tag.item.thesift.pearlbark_logs': 'Pearlbark Logs',
    })

def gen():
    # portal
    copy_family('nether_portal', 'nether_portal', 'sift_portal')

    # ichor fluid block: no geometry, only a particle texture
    write('models/block/ichor.json', {'textures': {'particle': NS + ':block/ichor_still'}})
    write('blockstates/ichor.json', {'variants': {'': {'model': NS + ':block/ichor'}}})

    for name in ['sift_stone', 'cobbled_sift_stone', 'polished_sift_stone', 'chiseled_sift_stone_bricks', 'meadow_rock',
                 'orange_carapace', 'red_carapace', 'blue_carapace', 'polished_carapace', 'carapace_sand',
                 'soul_crystal_block']:
        simple_block(name)

    # stone brick family
    copy_family('stone_bricks', 'stone_brick', 'sift_stone_brick')
    copy_family('stone_brick_stairs', 'stone_brick', 'sift_stone_brick')
    copy_family('stone_brick_slab', 'stone_brick', 'sift_stone_brick')
    copy_family('stone_brick_wall', 'stone_brick', 'sift_stone_brick')

    # moss-topped soils
    simple_block('blushing_moss', 'minecraft:block/cube_bottom_top', {
        'top': NS + ':block/blushing_moss_top', 'side': NS + ':block/blushing_moss_side', 'bottom': NS + ':block/meadow_rock'})
    simple_block('glowmoss', 'minecraft:block/cube_bottom_top', {
        'top': NS + ':block/glowmoss_top', 'side': NS + ':block/glowmoss_side', 'bottom': NS + ':block/sift_stone'})

    # sift willow wood set (from cherry)
    for v in ['cherry_log', 'cherry_wood', 'stripped_cherry_log', 'stripped_cherry_wood', 'cherry_planks', 'cherry_leaves',
              'cherry_sapling', 'cherry_stairs', 'cherry_slab', 'cherry_fence', 'cherry_fence_gate', 'cherry_door',
              'cherry_trapdoor', 'cherry_button', 'cherry_pressure_plate']:
        copy_family(v, 'cherry', 'sift_willow')

    # plants
    cross_plant('blush_grass')
    cross_plant('glow_sprouts')
    cross_plant('lullaby_bloom')
    cross_plant('echo_bloom')
    cross_plant('sift_thorns')
    write('models/block/tall_blush_grass_top.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': NS + ':block/tall_blush_grass_top'}})
    write('models/block/tall_blush_grass_bottom.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': NS + ':block/tall_blush_grass_bottom'}})
    write('blockstates/tall_blush_grass.json', {'variants': {
        'half=lower': {'model': NS + ':block/tall_blush_grass_bottom'},
        'half=upper': {'model': NS + ':block/tall_blush_grass_top'}}})
    generated_item('tall_blush_grass', 'block/tall_blush_grass_top')

    copy_family('amethyst_cluster', 'amethyst_cluster', 'soul_crystal_cluster')

    # items
    for name in ['ichor_bucket', 'ichor_bottle', 'soul_shard', 'blub_spawn_egg', 'sifter_spawn_egg']:
        generated_item(name)

    lang = {
        'itemGroup.thesift.the_sift': 'The Sift',
        'block.thesift.sift_portal': 'Sift Portal',
        'block.thesift.ichor': 'Ichor',
        'block.thesift.sift_stone': 'Sift Stone',
        'block.thesift.cobbled_sift_stone': 'Cobbled Sift Stone',
        'block.thesift.polished_sift_stone': 'Polished Sift Stone',
        'block.thesift.sift_stone_bricks': 'Sift Stone Bricks',
        'block.thesift.sift_stone_brick_stairs': 'Sift Stone Brick Stairs',
        'block.thesift.sift_stone_brick_slab': 'Sift Stone Brick Slab',
        'block.thesift.sift_stone_brick_wall': 'Sift Stone Brick Wall',
        'block.thesift.chiseled_sift_stone_bricks': 'Chiseled Sift Stone Bricks',
        'block.thesift.meadow_rock': 'Meadow Rock',
        'block.thesift.blushing_moss': 'Blushing Moss',
        'block.thesift.glowmoss': 'Glowmoss',
        'block.thesift.orange_carapace': 'Orange Carapace',
        'block.thesift.red_carapace': 'Red Carapace',
        'block.thesift.blue_carapace': 'Blue Carapace',
        'block.thesift.polished_carapace': 'Polished Carapace',
        'block.thesift.carapace_sand': 'Carapace Sand',
        'block.thesift.sift_willow_log': 'Sift Willow Log',
        'block.thesift.sift_willow_wood': 'Sift Willow Wood',
        'block.thesift.stripped_sift_willow_log': 'Stripped Sift Willow Log',
        'block.thesift.stripped_sift_willow_wood': 'Stripped Sift Willow Wood',
        'block.thesift.sift_willow_planks': 'Sift Willow Planks',
        'block.thesift.sift_willow_leaves': 'Sift Willow Leaves',
        'block.thesift.sift_willow_sapling': 'Sift Willow Sapling',
        'block.thesift.sift_willow_stairs': 'Sift Willow Stairs',
        'block.thesift.sift_willow_slab': 'Sift Willow Slab',
        'block.thesift.sift_willow_fence': 'Sift Willow Fence',
        'block.thesift.sift_willow_fence_gate': 'Sift Willow Fence Gate',
        'block.thesift.sift_willow_door': 'Sift Willow Door',
        'block.thesift.sift_willow_trapdoor': 'Sift Willow Trapdoor',
        'block.thesift.sift_willow_button': 'Sift Willow Button',
        'block.thesift.sift_willow_pressure_plate': 'Sift Willow Pressure Plate',
        'block.thesift.blush_grass': 'Blush Grass',
        'block.thesift.tall_blush_grass': 'Tall Blush Grass',
        'block.thesift.glow_sprouts': 'Glow Sprouts',
        'block.thesift.lullaby_bloom': 'Lullaby Bloom',
        'block.thesift.echo_bloom': 'Echo Bloom',
        'block.thesift.sift_thorns': 'Sift Thorns',
        'block.thesift.soul_crystal_block': 'Block of Soul Crystal',
        'block.thesift.soul_crystal_cluster': 'Soul Crystal Cluster',
        'item.thesift.ichor_bucket': 'Ichor Bucket',
        'item.thesift.ichor_bottle': 'Bottle of Ichor',
        'item.thesift.soul_shard': 'Soul Shard',
        'item.thesift.blub_spawn_egg': 'Blub Spawn Egg',
        'item.thesift.sifter_spawn_egg': 'Sifter Spawn Egg',
        'entity.thesift.blub': 'Blub',
        'entity.thesift.sifter': 'Sifter',
        'biome.thesift.singers_meadow': "Singer's Meadow",
        'biome.thesift.lullaby_hills': 'Lullaby Hills',
        'biome.thesift.the_carapace': 'The Carapace',
        'biome.thesift.soulsong_hollows': 'Soulsong Hollows',
        'tide.thesift.flow': 'Flow Tide',
        'tide.thesift.thrive': 'Thrive Tide',
        'tide.thesift.endure': 'Endure Tide',
        'tide.thesift.changed': 'The tide shifts: %s',
        'dimension.thesift.the_sift': 'The Sift',
        'tag.item.thesift.sift_willow_logs': 'Sift Willow Logs',
    }
    gen_expansion(lang)
    gen_v4(lang)
    write('lang/en_us.json', lang)

if __name__ == '__main__':
    gen()
    print('assets done')
