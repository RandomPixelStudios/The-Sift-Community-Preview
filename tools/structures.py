"""Builds The Sift's structure templates (.nbt) procedurally."""
import math, os, random
import nbt

DATA_VERSION = 5023
NS = 'thesift'

def t(name):
    return name if ':' in name else '%s:%s' % (NS, name)

class Template:
    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz)
        self.blocks = {}
        self.entities = []

    def set(self, x, y, z, block, nbt_data=None, **props):
        if 0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]:
            self.blocks[(x, y, z)] = (t(block), tuple(sorted((k, str(v).lower()) for k, v in props.items())), nbt_data)

    def get(self, x, y, z):
        b = self.blocks.get((x, y, z))
        return b[0] if b else None

    def fill(self, x0, y0, z0, x1, y1, z1, block, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, **props)

    def hollow(self, x0, y0, z0, x1, y1, z1, wall, inside='minecraft:air'):
        self.fill(x0, y0, z0, x1, y1, z1, inside)
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    if x in (x0, x1) or y in (y0, y1) or z in (z0, z1):
                        self.set(x, y, z, wall)

    def chest(self, x, y, z, loot, facing='north'):
        self.set(x, y, z, 'minecraft:chest', {'id': (8, 'minecraft:chest'), 'LootTable': (8, loot)}, facing=facing)

    def entity(self, x, y, z, entity_id, extra=None):
        data = {'id': (8, t(entity_id)), 'PersistenceRequired': (1, 1),
                'Pos': (9, (6, [x + 0.5, float(y), z + 0.5])), 'Motion': (9, (6, [0.0, 0.0, 0.0])),
                'Rotation': (9, (5, [0.0, 0.0]))}
        if extra:
            data.update(extra)
        self.entities.append({'blockPos': (9, (3, [x, y, z])), 'pos': (9, (6, [x + 0.5, float(y), z + 0.5])), 'nbt': (10, data)})

    def save(self, path):
        palette, index, blocks = [], {}, []
        for pos, (bid, props, nbt_data) in sorted(self.blocks.items()):
            key = (bid, props)
            if key not in index:
                index[key] = len(palette)
                entry = {'id': (8, bid)}
                if props:
                    entry['properties'] = (10, {k: (8, v) for k, v in props})
                palette.append(entry)
            b = {'pos': (9, (3, list(pos))), 'state': (3, index[key])}
            if nbt_data:
                b['nbt'] = (10, nbt_data)
            blocks.append(b)
        root = {
            'size': (9, (3, list(self.size))),
            'entities': (9, (10 if self.entities else 0, self.entities)),
            'blocks': (9, (10, blocks)),
            'palette': (9, (10, palette)),
            'DataVersion': (3, DATA_VERSION),
        }
        os.makedirs(os.path.dirname(path), exist_ok=True)
        nbt.save(path, '', root)

# ------------------------------------------------------------------------------------------
def sift_shrine():
    """A stepped stone dais with a staircase leading up to a glowing, always-open Sift portal
    (the scene from the reveal trailer)."""
    T = Template(15, 12, 16)
    rnd = random.Random(1)
    T.fill(0, 1, 0, 14, 11, 15, 'minecraft:air')
    # foundation steps - each tier smaller
    for tier in range(3):
        for x in range(tier, 15 - tier):
            for z in range(tier, 15 - tier):
                mat = 'sift_stone_bricks' if rnd.random() > 0.25 else rnd.choice(['cobbled_sift_stone', 'chiseled_sift_stone_bricks', 'meadow_rock_bricks'])
                T.set(x, tier, z, mat)
    # stairs walking up to the dais from the south
    for i in range(3):
        T.fill(5, i, 15 - i, 9, i, 15 - i, 'sift_stone_brick_stairs', facing='north', half='bottom', shape='straight')
    # portal frame of reinforced deepslate, portal axis along x
    T.fill(3, 3, 5, 11, 3, 5, 'minecraft:reinforced_deepslate')
    T.fill(3, 9, 5, 11, 9, 5, 'minecraft:reinforced_deepslate')
    T.fill(3, 3, 5, 3, 9, 5, 'minecraft:reinforced_deepslate')
    T.fill(11, 3, 5, 11, 9, 5, 'minecraft:reinforced_deepslate')
    T.fill(4, 4, 5, 10, 8, 5, 'sift_portal', axis='x')
    # crumbling top ornaments
    for x in (2, 12):
        T.fill(x, 3, 5, x, 7 + (x % 3), 5, 'sift_stone_bricks')
        T.set(x, 8 + (x % 3), 5, 'sift_lantern')
    T.set(7, 10, 5, 'chiseled_sift_stone_bricks')
    # glowing crystals and plants around the dais
    for x, z in [(3, 9), (11, 10), (4, 12), (10, 8), (2, 3), (12, 2)]:
        T.set(x, 3, z, 'soul_crystal_cluster', facing='up')
    for x, z in [(6, 8), (8, 10), (5, 11), (9, 12)]:
        T.set(x, 3, z, 'glow_sprouts')
    T.set(7, 3, 10, 'sift_lantern')
    T.chest(12, 3, 11, 'thesift:chests/sift_shrine', facing='west')
    return T

def willow_camp():
    """A small explorer's camp: a willow-plank lean-to, a lantern, seats and a supply chest."""
    T = Template(9, 6, 9)
    T.fill(0, 0, 0, 8, 0, 8, 'blushing_moss')
    T.fill(0, 1, 0, 8, 5, 8, 'minecraft:air')
    # lean-to shelter
    for x in (1, 5):
        T.fill(x, 1, 1, x, 3, 1, 'sift_willow_log', axis='y')
    T.fill(1, 4, 1, 5, 4, 3, 'sift_willow_slab', type='bottom')
    T.fill(1, 1, 0, 5, 3, 0, 'sift_willow_planks')
    T.chest(2, 1, 1, 'thesift:chests/willow_camp', facing='south')
    T.set(4, 1, 1, 'minecraft:crafting_table')
    # campfire with soul fire and log seats
    T.set(4, 1, 5, 'minecraft:soul_campfire', facing='north')
    T.set(2, 1, 5, 'stripped_sift_willow_log', axis='z')
    T.set(6, 1, 5, 'stripped_sift_willow_log', axis='z')
    T.set(4, 1, 7, 'stripped_sift_willow_log', axis='x')
    T.set(7, 1, 2, 'sift_lantern')
    for x, z in [(0, 7), (8, 6), (7, 8), (1, 4)]:
        T.set(x, 1, z, 'blush_grass')
    return T

def floating_island(radius, seed, tree):
    """A lush floating island: verdant turf on rust soil over a tapering rock underside."""
    rnd = random.Random(seed)
    size = radius * 2 + 3
    depth = radius + 3
    T = Template(size, depth + 9, size)
    c = size // 2
    top = depth
    offsets = [rnd.uniform(0, math.tau) for _ in range(3)]
    surface = []
    for x in range(size):
        for z in range(size):
            dx, dz = x - c, z - c
            ang = math.atan2(dz, dx)
            edge = radius * (0.82 + 0.12 * math.sin(ang * 3 + offsets[0]) + 0.08 * math.sin(ang * 5 + offsets[1]))
            d = math.hypot(dx, dz)
            if d > edge:
                continue
            k = 1.0 - d / edge
            thick = int(1 + k * depth * (0.8 + 0.3 * rnd.random()))
            bump = 1 if k > 0.55 and rnd.random() > 0.5 else 0
            ytop = top + bump
            for y in range(ytop - thick, ytop + 1):
                if y == ytop:
                    T.set(x, y, z, 'verdant_turf')
                elif y >= ytop - 2:
                    T.set(x, y, z, 'rust_soil')
                else:
                    T.set(x, y, z, 'sift_stone' if rnd.random() > 0.15 else 'meadow_rock')
            surface.append((x, ytop, z, k))
    for x, y, z, k in surface:
        r = rnd.random()
        if r < 0.22:
            T.set(x, y + 1, z, 'glimmer_petals', facing=rnd.choice(['north', 'east', 'south', 'west']), flower_amount=rnd.randint(1, 4))
        elif r < 0.27:
            T.set(x, y + 1, z, 'bluebell')
        elif r < 0.30:
            T.set(x, y + 1, z, 'pale_lily')
        elif r < 0.36:
            T.set(x, y + 1, z, 'blush_grass')
    if tree:
        x, y, z, _ = max(surface, key=lambda s: s[3])
        T.fill(x, y + 1, z, x, y + 5, z, 'sift_willow_log', axis='y')
        for lx in range(-3, 4):
            for lz in range(-3, 4):
                for ly in range(3, 8):
                    dd = math.hypot(lx, lz) + abs(ly - 5) * 0.9
                    if dd < 3.4 and (lx, lz) != (0, 0) or (lx, lz) == (0, 0) and ly > 5:
                        T.set(x + lx, y + ly, z + lz, 'sift_willow_leaves', persistent=True, distance=1, waterlogged=False)
                if math.hypot(lx, lz) > 2.2 and math.hypot(lx, lz) < 3.4 and rnd.random() > 0.5:
                    T.set(x + lx, y + 2, z + lz, 'sift_willow_leaves', persistent=True, distance=1, waterlogged=False)
        # a little pool of ichor
        px, py, pz = c + 2, None, c - 2
        for sx, sy, sz, k in surface:
            if (sx, sz) in ((px, pz), (px + 1, pz), (px, pz + 1)) and k > 0.3:
                T.set(sx, sy, sz, 'ichor')
    return T

def forgotten_shrine():
    """An abandoned shrine: crumbling sift-brick walls around an old altar where siftite gear can be re-enchanted."""
    T = Template(11, 8, 11)
    rnd = random.Random(5)
    T.fill(0, 1, 0, 10, 7, 10, 'minecraft:air')
    for x in range(11):
        for z in range(11):
            T.set(x, 0, z, rnd.choice(['sift_stone_bricks', 'sift_stone_bricks', 'cobbled_sift_stone', 'polished_sift_stone']))
    for x in range(1, 10):
        for z in range(1, 10):
            if x in (1, 9) or z in (1, 9):
                h = rnd.choice([1, 2, 3, 4, 5]) if (x + z) % 4 else 6
                for y in range(1, h + 1):
                    T.set(x, y, z, rnd.choice(['sift_stone_bricks', 'cobbled_sift_stone', 'chiseled_sift_stone_bricks']))
    T.fill(4, 1, 1, 6, 4, 1, 'minecraft:air')            # doorway
    T.fill(3, 1, 3, 7, 1, 7, 'polished_sift_stone')
    T.set(5, 2, 6, 'chiseled_sift_stone_bricks')
    for x, z in ((3, 3), (7, 3), (3, 7), (7, 7)):
        T.set(x, 2, z, 'sift_lantern')
    for x, z in ((2, 5), (8, 4), (6, 8)):
        T.set(x, 1, z, 'soul_crystal_cluster', facing='up')
    T.set(4, 2, 7, 'minecraft:cobweb')
    T.chest(8, 1, 8, 'thesift:chests/sift_shrine', facing='west')
    return T

def bone_arch(seed):
    """A huge rib of some ancient creature half buried in the Carapace."""
    rnd = random.Random(seed)
    T = Template(21, 16, 5)
    for i in range(5):
        z = i if i < 5 else 4
        for a in range(0, 181, 4):
            x = int(round(10 + math.cos(math.radians(a)) * 9))
            y = int(round(math.sin(math.radians(a)) * (12 - abs(i - 2) * 2)))
            if i in (0, 2, 4):
                T.set(x, y, z, 'minecraft:bone_block', axis='y')
    T.fill(0, 0, 0, 20, 0, 4, 'minecraft:bone_block', axis='x') if rnd.random() < 0 else None
    for x in range(0, 21, 2):
        T.set(x, 0, 2, 'minecraft:bone_block', axis='x')
    return T

def house(T, x0, z0, w, d, wood, roof, rnd, bed_color='light_blue'):
    """A small cottage: stone-brick footing, plank walls, log corners and a pitched slab roof."""
    T.fill(x0, 0, z0, x0 + w - 1, 0, z0 + d - 1, 'polished_sift_stone')
    for x in range(x0, x0 + w):
        for z in range(z0, z0 + d):
            edge = x in (x0, x0 + w - 1) or z in (z0, z0 + d - 1)
            corner = x in (x0, x0 + w - 1) and z in (z0, z0 + d - 1)
            for y in range(1, 4):
                if corner:
                    T.set(x, y, z, wood + '_log', axis='y')
                elif edge:
                    T.set(x, y, z, 'minecraft:glass_pane' if y == 2 and (x + z) % 3 == 0 else wood + '_planks')
                else:
                    T.set(x, y, z, 'minecraft:air')
    # roof
    for i in range((d + 1) // 2 + 1):
        for x in range(x0 - 1, x0 + w + 1):
            for z in (z0 - 1 + i, z0 + d - i):
                if z0 - 1 <= z <= z0 + d:
                    T.set(x, 4 + i, z, roof, type='bottom')
        if i < (d + 1) // 2:
            for x in (x0, x0 + w - 1):
                for z in range(z0 + i, z0 + d - i):
                    T.set(x, 4 + i, z, wood + '_planks')
    door_x = x0 + w // 2
    T.set(door_x, 1, z0, wood + '_door', half='lower', facing='south', hinge='left', open=False, powered=False)
    T.set(door_x, 2, z0, wood + '_door', half='upper', facing='south', hinge='left', open=False, powered=False)
    T.set(door_x, 3, z0 - 1, 'sift_lantern', hanging=False)
    T.set(x0 + 1, 1, z0 + d - 2, 'minecraft:%s_bed' % bed_color, part='foot', facing='north', occupied=False)
    T.set(x0 + 1, 1, z0 + d - 3, 'minecraft:%s_bed' % bed_color, part='head', facing='north', occupied=False) if d > 4 else None
    return door_x

def sift_village(seed):
    """A Sift village: a random cluster of willow, duskwood and pearlbark cottages around a glowing well,
    with paths, farms, lamp posts and a bell. Every variant is laid out differently."""
    S = 41
    T = Template(S, 14, S)
    rnd = random.Random(seed)
    T.fill(0, 1, 0, S - 1, 13, S - 1, 'minecraft:air')
    c = S // 2
    woods = ['sift_willow', 'duskwood', 'pearlbark']
    # well or a glowing lantern plaza in the middle
    T.fill(c - 3, 0, c - 3, c + 3, 0, c + 3, 'polished_sift_stone')
    if rnd.random() < 0.6:
        T.fill(c - 2, 0, c - 2, c + 2, 0, c + 2, 'sift_stone_bricks')
        T.fill(c - 2, 1, c - 2, c + 2, 1, c + 2, 'sift_stone_brick_wall')
        T.fill(c - 1, 0, c - 1, c + 1, 1, c + 1, 'minecraft:water', level=0)
        for x, z in ((c - 2, c - 2), (c + 2, c - 2), (c - 2, c + 2), (c + 2, c + 2)):
            T.fill(x, 2, z, x, 3, z, rnd.choice(woods) + '_fence')
        T.fill(c - 2, 4, c - 2, c + 2, 4, c + 2, rnd.choice(woods) + '_slab', type='bottom')
        T.set(c, 3, c, 'sift_lantern', hanging=True)
    else:
        T.fill(c, 1, c, c, 3, c, 'chiseled_sift_stone_bricks')
        T.set(c, 4, c, 'soul_crystal_cluster', facing='up')
        for x, z in ((c - 2, c), (c + 2, c), (c, c - 2), (c, c + 2)):
            T.set(x, 1, z, 'sift_lantern', hanging=False)
    T.set(c + 3, 1, c - 3, 'minecraft:bell', attachment='floor', facing='north', powered=False)
    # plots around the plaza; each gets a house, a farm, a garden or stays meadow
    plots = [(x, z) for x in (2, 13, 26) for z in (2, 13, 26) if (x, z) != (13, 13)]
    rnd.shuffle(plots)
    stations = ['minecraft:lectern', 'minecraft:composter', 'minecraft:smithing_table', 'minecraft:cartography_table',
                'minecraft:barrel', 'minecraft:fletching_table', 'minecraft:loom', 'minecraft:stonecutter']
    rnd.shuffle(stations)
    beds = ['light_blue', 'pink', 'cyan', 'magenta', 'purple', 'white', 'lime', 'yellow']
    houses = rnd.randint(4, 6)
    for i, (px, pz) in enumerate(plots):
        if i < houses:
            w, d = rnd.randint(5, 8), rnd.randint(5, 7)
            x, z = px + rnd.randint(0, 11 - w), pz + rnd.randint(0, 10 - d)
            wood = rnd.choice(woods)
            house(T, x, z, w, d, wood, wood + '_slab', rnd, rnd.choice(beds))
            station = stations[i % len(stations)]
            sx, sz = x + w - 2, z + d - 2
            if station == 'minecraft:lectern':
                T.set(sx, 1, sz, station, facing='north', has_book=False, powered=False)
            elif station == 'minecraft:barrel':
                T.set(sx, 1, sz, station, facing='up', open=False)
            elif station == 'minecraft:composter':
                T.set(sx, 1, sz, station, level=0)
            elif station in ('minecraft:loom', 'minecraft:stonecutter'):
                T.set(sx, 1, sz, station, facing='north')
            else:
                T.set(sx, 1, sz, station)
            T.entity(x + w // 2, 1, z + 2, 'minecraft:villager')
            if i == 0:
                T.chest(x + 1, 1, z + 1, 'thesift:chests/sift_village', facing='south')
            # path from the door to the plaza
            door_x = x + w // 2
            zz = z - 1
            while zz >= 0 and abs(zz - c) > 3 and T.get(door_x, 0, zz) is None:
                T.set(door_x, 0, zz, rnd.choice(['polished_sift_stone', 'polished_sift_stone', 'cobbled_sift_stone']))
                zz -= 1
        elif rnd.random() < 0.6:
            fx, fz = px + 1, pz + 2
            T.fill(fx - 1, 0, fz - 1, fx + 7, 0, fz + 3, rnd.choice(woods) + '_log', axis='x')
            T.fill(fx, 0, fz, fx + 6, 0, fz + 2, 'minecraft:farmland', moisture=7)
            T.fill(fx, 0, fz + 1, fx + 6, 0, fz + 1, 'minecraft:water', level=0)
            crop = rnd.choice(['minecraft:wheat', 'minecraft:carrots', 'minecraft:potatoes'])
            for xx in range(fx, fx + 7):
                for zz in (fz, fz + 2):
                    T.set(xx, 1, zz, crop, age=rnd.randint(2, 7))
        else:
            for _ in range(12):
                T.set(px + rnd.randint(0, 10), 1, pz + rnd.randint(0, 10), rnd.choice(['bluebell', 'pale_lily', 'lullaby_bloom', 'glimmer_petals']),
                      **({'facing': 'north', 'flower_amount': 3} if False else {}))
    # main roads
    for x in range(S):
        for z in (c - 1, c, c + 1):
            if T.get(x, 0, z) is None and T.get(x, 1, z) in (None, 'minecraft:air'):
                T.set(x, 0, z, 'polished_sift_stone' if rnd.random() < 0.8 else 'cobbled_sift_stone')
    for _ in range(rnd.randint(3, 6)):
        x, z = rnd.randrange(2, S - 2), rnd.randrange(2, S - 2)
        if T.get(x, 1, z) in (None, 'minecraft:air') and T.get(x, 2, z) in (None, 'minecraft:air'):
            T.fill(x, 1, z, x, 3, z, rnd.choice(woods) + '_fence')
            T.set(x, 4, z, 'sift_lantern', hanging=False)
    # meadow ground everywhere else
    for x in range(S):
        for z in range(S):
            if T.get(x, 0, z) is None:
                T.set(x, 0, z, 'verdant_turf')
                if rnd.random() < 0.08 and T.get(x, 1, z) in (None, 'minecraft:air'):
                    T.set(x, 1, z, rnd.choice(['blush_grass', 'bluebell', 'pale_lily']))
    T.entity(c, 1, c + 5, 'minecraft:iron_golem')
    return T

def warden_temple():
    """The Ancient Temple: a stepped deepslate ziggurat ringed by sculk, with a grand arched gate, buttressed
    walls, a pillared entrance hall and a domed arena where the Ancient Warden waits in the ceiling.
    Stepping on the warden seal just inside the gate starts the fight."""
    M = 6
    BW, BD = 47, 58                       # building footprint
    R = 19                                 # arena radius
    W, D, H = BW + 2 * M, BD + 2 * M + 4, 40
    T = Template(W, H, D)
    rnd = random.Random(21)
    T.fill(0, 1, 0, W - 1, H - 1, D - 1, 'minecraft:air')
    x0, z0 = M, M + 4                      # building corner (front wall starts at z0)
    x1, z1 = x0 + BW - 1, z0 + BD - 1
    cx = x0 + BW // 2
    acz = z1 - R - 3                       # arena centre
    def brick():
        r = rnd.random()
        return 'minecraft:deepslate_bricks' if r < 0.55 else 'minecraft:cracked_deepslate_bricks' if r < 0.7 else \
            'minecraft:deepslate_tiles' if r < 0.9 else 'minecraft:cracked_deepslate_tiles'
    # --- sculk carpet around the temple, thinning out with distance -------------------------
    for x in range(W):
        for z in range(D):
            if x0 - 3 <= x <= x1 + 3 and z0 - 7 <= z <= z1 + 3:
                continue
            edge = min(x, z, W - 1 - x, D - 1 - z)
            if rnd.random() < 0.85 - edge * 0.1:
                T.set(x, 0, z, 'minecraft:sculk')
                r = rnd.random()
                if r < 0.04:
                    T.set(x, 1, z, 'minecraft:sculk_sensor', sculk_sensor_phase='inactive', power=0, waterlogged=False)
                elif r < 0.06:
                    T.set(x, 1, z, 'minecraft:sculk_shrieker', can_summon=False, shrieking=False, waterlogged=False)
                elif r < 0.2:
                    T.set(x, 1, z, 'minecraft:sculk_vein', down=True, up=False, north=False, south=False, east=False, west=False, waterlogged=False)
    # --- three-step plinth -------------------------------------------------------------------
    for step in range(3):
        g = 3 - step
        T.fill(x0 - g, step - 2, z0 - g, x1 + g, step - 2, z1 + g, 'minecraft:polished_deepslate')
    T.fill(x0 - 3, 0, z0 - 3, x1 + 3, 0, z1 + 3, 'minecraft:polished_deepslate')
    for x in range(x0 - 3, x1 + 4):
        T.set(x, 0, z0 - 3, 'minecraft:polished_deepslate_slab', type='bottom', waterlogged=False)
    # grand stairs up to the gate
    for i in range(4):
        T.fill(cx - 4, 0, z0 - 4 - i, cx + 4, 0, z0 - 4 - i, 'minecraft:air')
    T.fill(cx - 4, 0, z0 - 3, cx + 4, 0, z0 - 1, 'minecraft:deepslate_tiles')
    T.fill(cx - 4, 0, z0 - 4, cx + 4, 0, z0 - 4, 'minecraft:deepslate_tile_stairs', facing='north', half='bottom', shape='straight', waterlogged=False)
    # --- outer walls: two thick, buttresses every 6 blocks -----------------------------------------
    WALL_H = 18
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x - x0 < 2 or x1 - x < 2 or z - z0 < 2 or z1 - z < 2:
                for y in range(1, WALL_H + 1):
                    T.set(x, y, z, 'temple_bricks' if (x - x0 in (1, BW - 2) or z - z0 in (1, BD - 2)) else brick())
            else:
                T.set(x, 0, z, 'minecraft:deepslate_tiles')
    for i, x in enumerate(range(x0, x1 + 1, 6)):
        for z in (z0 - 1, z1 + 1):
            T.fill(x, 1, z, x, WALL_H + 1, z, 'minecraft:polished_deepslate')
            T.set(x, WALL_H + 2, z, 'minecraft:chiseled_deepslate')
            T.set(x, WALL_H + 3, z, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    for z in range(z0, z1 + 1, 6):
        for x in (x0 - 1, x1 + 1):
            T.fill(x, 1, z, x, WALL_H + 1, z, 'minecraft:polished_deepslate')
            T.set(x, WALL_H + 2, z, 'minecraft:chiseled_deepslate')
            T.set(x, WALL_H + 3, z, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    # cornice band and glowing sculk "veins" running along the walls
    for x in range(x0, x1 + 1):
        for z in (z0, z1):
            T.set(x, WALL_H, z, 'minecraft:chiseled_deepslate')
            if rnd.random() < 0.25:
                T.set(x, rnd.randint(2, WALL_H - 2), z, 'minecraft:sculk')
    for z in range(z0, z1 + 1):
        for x in (x0, x1):
            T.set(x, WALL_H, z, 'minecraft:chiseled_deepslate')
            if rnd.random() < 0.25:
                T.set(x, rnd.randint(2, WALL_H - 2), z, 'minecraft:sculk')
    # --- stepped roof with a dome over the arena ----------------------------------------------------
    for tier in range(6):
        y = WALL_H + 1 + tier
        g = tier * 3
        for x in range(x0 + g, x1 - g + 1):
            for z in range(z0 + g, z1 - g + 1):
                ring = x in (x0 + g, x1 - g) or z in (z0 + g, z1 - g)
                T.set(x, y, z, 'minecraft:deepslate_tiles' if ring else 'temple_bricks')
        for x in range(x0 + g, x1 - g + 1):
            T.set(x, y + 1, z0 + g, 'minecraft:deepslate_tile_slab', type='bottom', waterlogged=False)
    T.fill(cx - 1, WALL_H + 7, acz - 1, cx + 1, WALL_H + 10, acz + 1, 'minecraft:polished_deepslate')
    T.set(cx, WALL_H + 11, acz, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    # --- the grand gate (decorative arch around the real 3x4 doorway) -------------------------------
    for x in range(cx - 4, cx + 5):
        for y in range(1, 10):
            dx = abs(x - cx)
            if dx >= 3 or y >= 8 - (1 if dx == 2 else 0):
                T.set(x, y, z0 - 1, 'minecraft:chiseled_deepslate' if y == 9 or dx == 4 else 'minecraft:polished_deepslate')
    T.fill(cx - 2, 1, z0, cx + 2, 7, z0 + 1, 'minecraft:air')
    T.fill(cx - 2, 5, z0, cx + 2, 7, z0 + 1, 'temple_bricks')
    T.fill(cx - 1, 1, z0, cx + 1, 5, z0 + 1, 'minecraft:air')
    T.fill(cx - 2, 1, z0, cx - 2, 5, z0 + 1, 'minecraft:polished_deepslate')
    T.fill(cx + 2, 1, z0, cx + 2, 5, z0 + 1, 'minecraft:polished_deepslate')
    T.set(cx, 8, z0 - 1, 'minecraft:reinforced_deepslate')
    for x in (cx - 6, cx + 6):            # guardian statues: tall pillars crowned with sculk catalysts
        T.fill(x, 1, z0 - 3, x, 5, z0 - 3, 'minecraft:polished_deepslate_wall', up=True, north='none', south='none', east='none', west='none', waterlogged=False)
        T.set(x, 6, z0 - 3, 'minecraft:sculk_catalyst', bloom=False)
        T.set(x, 7, z0 - 3, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    # --- entrance hall ---------------------------------------------------------------------------
    hall_end = acz - R + 2
    for z in range(z0 + 2, hall_end):
        for x in range(x0 + 2, x1 - 1):
            if abs(x - cx) > 3:
                T.fill(x, 1, z, x, 8, z, brick())
        T.fill(cx - 3, 9, z, cx + 3, 9, z, 'minecraft:deepslate_tiles')
        T.set(cx, 0, z, 'minecraft:polished_deepslate')
    for z in range(z0 + 5, hall_end, 3):
        for x in (cx - 3, cx + 3):
            T.fill(x, 1, z, x, 8, z, 'minecraft:polished_deepslate')
            T.set(x + (1 if x < cx else -1), 4, z, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    # --- arena: round hall under the dome --------------------------------------------------------
    for x in range(x0 + 2, x1 - 1):
        for z in range(hall_end, z1 - 1):
            dd = math.hypot(x - cx, z - acz)
            if dd > R + 0.5:
                T.fill(x, 1, z, x, WALL_H, z, brick())
            else:
                T.set(x, 0, z, 'minecraft:sculk' if rnd.random() < 0.12 else ('minecraft:deepslate_tiles' if int(dd) % 4 else 'minecraft:polished_deepslate'))
                top = WALL_H + 3 + int(max(0.0, 1.0 - (dd / (R + 0.5)) ** 2) * 8)
                for y in range(1, top):
                    T.set(x, y, z, 'minecraft:air')
                T.set(x, top, z, 'temple_bricks')
    for a in range(0, 360, 30):
        px = int(round(cx + math.cos(math.radians(a)) * (R - 4)))
        pz = int(round(acz + math.sin(math.radians(a)) * (R - 4)))
        if abs(px - cx) <= 3 and pz < acz:
            continue                       # keep the way in clear
        T.fill(px, 1, pz, px, 16, pz, 'minecraft:polished_deepslate')
        T.set(px, 17, pz, 'minecraft:chiseled_deepslate')
        T.set(px, 8, pz, 'minecraft:sculk_catalyst', bloom=False)
    # the Sift altar on a stepped dais in the middle
    for r, block in ((3, 'minecraft:polished_deepslate'), (2, 'minecraft:deepslate_tiles'), (1, 'minecraft:chiseled_deepslate')):
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                T.set(cx + dx, 3 - r, acz + dz, block)
    T.set(cx, 3, acz, 'sift_altar')
    for dx, dz in ((2, 2), (-2, 2), (2, -2), (-2, -2)):
        T.set(cx + dx, 2, acz + dz, 'minecraft:soul_lantern', hanging=False, waterlogged=False)
    # hanging soul lights from the dome
    for a in range(0, 360, 40):
        px = int(round(cx + math.cos(math.radians(a)) * 10))
        pz = int(round(acz + math.sin(math.radians(a)) * 10))
        T.set(px, WALL_H + 3, pz, 'minecraft:chain', axis='y', waterlogged=False)
        T.set(px, WALL_H + 2, pz, 'minecraft:soul_lantern', hanging=True, waterlogged=False)
    # treasure alcove behind the arena
    T.fill(cx - 3, 1, z1 - 3, cx + 3, 4, z1 - 2, 'minecraft:air')
    for dx in (-2, 2):
        T.chest(cx + dx, 1, z1 - 2, 'thesift:chests/warden_temple', facing='north')
    T.set(cx, 1, z1 - 2, 'minecraft:sculk_catalyst', bloom=False)
    return T

def rock_arch(seed):
    """A weathered arch of crimson carapace rock, pitted with sandy hollows - like the Sift's eroded arches."""
    rnd = random.Random(seed)
    span = rnd.randint(9, 15)
    height = rnd.randint(10, 16)
    thick = rnd.randint(2, 3)
    W = span + 2 * thick + 4
    T = Template(W, height + 10, thick + 4)
    base = 6
    for i in range(0, 181, 2):
        a = math.radians(i)
        cxx = W / 2 + math.cos(a) * (span / 2 + thick / 2)
        cy = base + math.sin(a) * height
        for dx in range(-thick, thick + 1):
            for dy in range(-thick, thick + 1):
                if dx * dx + dy * dy > thick * thick + 1:
                    continue
                for z in range(2, 2 + thick):
                    x, y = int(round(cxx + dx * 0.6)), int(round(cy + dy * 0.6))
                    r = rnd.random()
                    T.set(x, y, z, 'carapace_sand' if r < 0.12 else 'orange_carapace' if r < 0.25 else 'red_carapace')
    # legs reach down into the ground
    for (x, y, z), v in list(T.blocks.items()):
        if y == base:
            for yy in range(0, base):
                T.set(x, yy, z, 'red_carapace')
    # erosion: knock random holes through
    for _ in range(len(T.blocks) // 12):
        k = rnd.choice(list(T.blocks.keys()))
        if k[1] > base + 2:
            del T.blocks[k]
    return T

def rock_spire(seed):
    """A tall eroded spire of crimson rock with ledges and holes."""
    rnd = random.Random(seed)
    h = rnd.randint(14, 24)
    T = Template(11, h + 7, 11)
    base = 6
    for y in range(0, base + h):
        t = max(0, y - base) / h
        r = 3.6 * (1 - t * 0.6) + math.sin(y * 0.7 + seed) * 0.8
        for x in range(11):
            for z in range(11):
                if math.hypot(x - 5, z - 5) <= r:
                    q = rnd.random()
                    if y > base + 2 and q < 0.06:
                        continue
                    T.set(x, y, z, 'carapace_sand' if q < 0.14 else 'orange_carapace' if q < 0.3 else 'red_carapace')
    # a few mint grass tufts on ledges
    for (x, y, z), v in list(T.blocks.items()):
        if y > base and (x, y + 1, z) not in T.blocks and rnd.random() < 0.15:
            T.set(x, y + 1, z, 'mint_grass')
    return T

def build_all(data_root):
    base = os.path.join(data_root, NS, 'structure')
    sift_shrine().save(os.path.join(base, 'sift_shrine.nbt'))
    willow_camp().save(os.path.join(base, 'willow_camp.nbt'))
    forgotten_shrine().save(os.path.join(base, 'forgotten_shrine.nbt'))
    warden_temple().save(os.path.join(base, 'warden_temple.nbt'))
    bone_arch(3).save(os.path.join(base, 'bone_arch.nbt'))
    for i in range(4):
        rock_arch(11 + i * 7).save(os.path.join(base, 'rock/arch_%d.nbt' % i))
        rock_spire(5 + i * 13).save(os.path.join(base, 'rock/spire_%d.nbt' % i))
