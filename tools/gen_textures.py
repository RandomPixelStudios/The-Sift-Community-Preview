"""Generates The Sift block/item textures by recolouring vanilla 26.3 textures,
plus hand-painted entity textures. Output goes into the mod's assets folder."""
import colorsys, math, os, random, shutil
from PIL import Image

SCRATCH = '/tmp/claude-1000/-home-cat-Dokumente-Minecraft-The-Sift/cfaeee90-d691-4710-b54e-e87b2d62cad0/scratchpad'
VAN = SCRATCH + '/mc/data/assets/minecraft/textures/'
OUT = '/home/cat/Dokumente/Minecraft The Sift/src/main/resources/assets/thesift/textures/'

def hexc(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))

def load(name):
    return Image.open(VAN + name + '.png').convert('RGBA')

def save(img, name, mcmeta=None):
    path = OUT + name + '.png'
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    if mcmeta:
        with open(path + '.mcmeta', 'w') as f:
            f.write(mcmeta)

def lum(p):
    return (0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]) / 255.0

def gradient(img, dark, light, gamma=1.0, alpha_mul=1.0):
    """Maps each pixel's luminance onto a dark->light colour ramp (keeps alpha)."""
    dark, light = hexc(dark), hexc(light)
    src = img.load()
    # normalise luminance range so every texture uses the full ramp
    ls = [lum(src[x, y]) for x in range(img.width) for y in range(img.height) if src[x, y][3] > 0]
    lo, hi = (min(ls), max(ls)) if ls else (0, 1)
    out = Image.new('RGBA', img.size)
    o = out.load()
    for x in range(img.width):
        for y in range(img.height):
            p = src[x, y]
            if p[3] == 0:
                o[x, y] = (0, 0, 0, 0)
                continue
            t = (lum(p) - lo) / (hi - lo) if hi > lo else 0.5
            t = max(0.0, min(1.0, t)) ** gamma
            c = tuple(int(dark[i] + (light[i] - dark[i]) * t) for i in range(3))
            o[x, y] = c + (int(p[3] * alpha_mul),)
    return out

def recolor(src, dst, dark, light, gamma=1.0, mcmeta=None, copy_meta=False, alpha_mul=1.0):
    img = gradient(load(src), dark, light, gamma, alpha_mul)
    meta = mcmeta
    if copy_meta and os.path.exists(VAN + src + '.png.mcmeta'):
        meta = open(VAN + src + '.png.mcmeta').read()
    save(img, dst, meta)
    return img

def side_overlay(base, top_img, seed):
    """Grass-block style side: base texture with a jagged band of the top colour."""
    rnd = random.Random(seed)
    out = base.copy()
    o, t = out.load(), top_img.load()
    for x in range(16):
        depth = rnd.choice([2, 3, 3, 4, 4, 5])
        for y in range(depth):
            o[x, y] = t[x, (y + x) % 16]
    return out

def rainbow_animation(src, dst, frame_h, alpha=205, frametime=None):
    img = load(src)
    w, h = img.size
    frames = h // frame_h
    out = Image.new('RGBA', img.size)
    s, o = img.load(), out.load()
    for x in range(w):
        for y in range(h):
            p = s[x, y]
            f, fy = divmod(y, frame_h)
            hue = ((x + fy) / (w * 1.6) + f / frames) % 1.0
            v = 0.55 + 0.45 * lum(p)
            r, g, b = colorsys.hsv_to_rgb(hue, 0.55, v)
            o[x, y] = (int(r * 255), int(g * 255), int(b * 255), alpha)
    meta = '{\n  "animation": {%s}\n}\n' % ('\n    "frametime": %d\n  ' % frametime if frametime else '')
    save(out, dst, meta)

def calm_animation(src, dst, frame_h, alpha=200, frametime=None):
    """Soft pastel shimmer: every frame shares one slowly drifting hue (pink -> lilac -> sky), so large
    bodies of ichor look like one calm, glowing surface instead of rainbow stripes."""
    img = load(src)
    w, h = img.size
    frames = h // frame_h
    out = Image.new('RGBA', img.size)
    s, o = img.load(), out.load()
    for x in range(w):
        for y in range(h):
            f, fy = divmod(y, frame_h)
            phase = math.sin(f / frames * math.tau)
            hue = (0.80 + 0.07 * phase + 0.02 * lum(s[x, y])) % 1.0
            v = 0.72 + 0.28 * lum(s[x, y])
            r, g, b = colorsys.hsv_to_rgb(hue, 0.38, v)
            o[x, y] = (int(r * 255), int(g * 255), int(b * 255), alpha)
    meta = '{\n  "animation": {%s}\n}\n' % ('\n    "frametime": %d\n  ' % frametime if frametime else '')
    save(out, dst, meta)

def gen_blocks():
    # --- portal ---------------------------------------------------------------
    recolor('block/nether_portal', 'block/sift_portal', '#0a2f7a', '#8ff4ff', gamma=0.9, copy_meta=True)

    # --- sift stone family (mauve rock of the cliffs) -------------------------
    recolor('block/stone', 'block/sift_stone', '#5a3d5f', '#9c7699')
    recolor('block/cobblestone', 'block/cobbled_sift_stone', '#4b3150', '#94708f')
    recolor('block/polished_andesite', 'block/polished_sift_stone', '#5e4262', '#a8829f')
    recolor('block/stone_bricks', 'block/sift_stone_bricks', '#4f3554', '#a07b9b')
    recolor('block/chiseled_stone_bricks', 'block/chiseled_sift_stone_bricks', '#4f3554', '#a8839f')

    # --- Singer's Meadow ground (coral red, end-stone-like) -------------------
    meadow = recolor('block/end_stone', 'block/meadow_rock', '#b4404c', '#f2938c')
    blush_top = recolor('block/moss_block', 'block/blushing_moss_top', '#d9536a', '#ffb0bb')
    save(side_overlay(meadow, blush_top, 11), 'block/blushing_moss_side')

    # --- Lullaby Hills ground (cyan glowing moss on sift stone) ---------------
    sift = gradient(load('block/stone'), '#5a3d5f', '#9c7699')
    glow_top = recolor('block/moss_block', 'block/glowmoss_top', '#159aa8', '#8ffff4')
    save(side_overlay(sift, glow_top, 23), 'block/glowmoss_side')

    # --- The Carapace (layered orange / red / blue shell stone) ---------------
    recolor('block/dripstone_block', 'block/orange_carapace', '#a3471b', '#f2a65a')
    recolor('block/dripstone_block', 'block/red_carapace', '#7f2228', '#d9604f')
    recolor('block/dripstone_block', 'block/blue_carapace', '#24407f', '#62a8e8')
    recolor('block/smooth_basalt', 'block/polished_carapace', '#a3471b', '#eea060')
    recolor('block/end_stone', 'block/carapace_sand', '#c9773b', '#f6c486')

    # --- Sift Willow wood set (dark trunk, pale icy canopy) -------------------
    recolor('block/cherry_log', 'block/sift_willow_log', '#1f2a36', '#5b7286')
    recolor('block/cherry_log_top', 'block/sift_willow_log_top', '#2b3846', '#c9e2ee')
    recolor('block/stripped_cherry_log', 'block/stripped_sift_willow_log', '#7fa4ba', '#d6ecf6')
    recolor('block/stripped_cherry_log_top', 'block/stripped_sift_willow_log_top', '#7fa4ba', '#e2f3fa')
    recolor('block/cherry_planks', 'block/sift_willow_planks', '#86a9bf', '#dbeef8')
    recolor('block/cherry_leaves', 'block/sift_willow_leaves', '#a9cadb', '#f4fcff', copy_meta=True)
    recolor('block/cherry_door_top', 'block/sift_willow_door_top', '#7697ad', '#dbeef8')
    recolor('block/cherry_door_bottom', 'block/sift_willow_door_bottom', '#7697ad', '#dbeef8')
    recolor('block/cherry_trapdoor', 'block/sift_willow_trapdoor', '#7697ad', '#dbeef8')
    recolor('block/cherry_sapling', 'block/sift_willow_sapling', '#24414f', '#d8f2ff')
    recolor('item/cherry_door', 'item/sift_willow_door', '#7697ad', '#dbeef8')

    # --- plants -----------------------------------------------------------------
    recolor('block/short_grass', 'block/blush_grass', '#c63c56', '#ffa6b4')
    recolor('block/tall_grass_top', 'block/tall_blush_grass_top', '#c63c56', '#ffa6b4')
    recolor('block/tall_grass_bottom', 'block/tall_blush_grass_bottom', '#b8344d', '#ff98a8')
    recolor('block/nether_sprouts', 'block/glow_sprouts', '#0f8c9c', '#a0fff6')
    recolor('block/dandelion', 'block/lullaby_bloom', '#5f8f2a', '#fff59a', gamma=0.8)
    recolor('block/blue_orchid', 'block/echo_bloom', '#1b5ea8', '#a6f6ff')
    recolor('block/dead_bush', 'block/sift_thorns', '#5a1440', '#f0638f')

    # --- soul crystals -----------------------------------------------------------
    recolor('block/amethyst_cluster', 'block/soul_crystal_cluster', '#1a5fa8', '#b4fdff')
    recolor('block/amethyst_block', 'block/soul_crystal_block', '#1d63a6', '#9ff4ff')
    recolor('item/amethyst_shard', 'item/soul_shard', '#1a5fa8', '#c4feff')

    # --- ichor (rainbow fluid) -----------------------------------------------------
    calm_animation('block/water_still', 'block/ichor_still', 16, frametime=3)
    calm_animation('block/water_flow', 'block/ichor_flow', 32, frametime=2)

def gen_items():
    bucket, water = load('item/bucket'), load('item/water_bucket')
    out = water.copy()
    b, w, o = bucket.load(), water.load(), out.load()
    for x in range(16):
        for y in range(16):
            if w[x, y] != b[x, y] and w[x, y][3] > 0:
                r, g, bb = colorsys.hsv_to_rgb(((x + y) / 18.0) % 1.0, 0.6, 0.6 + 0.4 * lum(w[x, y]))
                o[x, y] = (int(r * 255), int(g * 255), int(bb * 255), 255)
    save(out, 'item/ichor_bucket')

    # bottle of ichor
    bottle, water_bottle = load('item/glass_bottle'), load('item/potion_overlay')
    out = bottle.copy()
    o, wb = out.load(), water_bottle.load()
    for x in range(16):
        for y in range(16):
            if wb[x, y][3] > 0:
                r, g, bb = colorsys.hsv_to_rgb(((x * 1.3 + y) / 16.0) % 1.0, 0.55, 0.65 + 0.35 * lum(wb[x, y]))
                o[x, y] = (int(r * 255), int(g * 255), int(bb * 255), 255)
    save(out, 'item/ichor_bottle')

    recolor('item/allay_spawn_egg', 'item/blub_spawn_egg', '#2f6fc0', '#a8f0ff')
    recolor('item/allay_spawn_egg', 'item/sifter_spawn_egg', '#4a1f4f', '#ff8fa8')

# ---------------------------------------------------------------------------------
# Entity textures: painted per box face using the vanilla box UV layout.
# ---------------------------------------------------------------------------------
class Painter:
    def __init__(self, w, h):
        self.img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        self.px = self.img.load()

    def rect(self, x0, y0, w, h, fn):
        for x in range(w):
            for y in range(h):
                c = fn(x, y, w, h)
                if c is not None:
                    self.px[x0 + x, y0 + y] = c if len(c) == 4 else c + (255,)

    def box(self, u, v, w, h, d, side, top=None, bottom=None, front=None, back=None):
        """Vanilla cube UV layout. side/top/... are fn(x, y, w, h) -> rgb."""
        top, bottom = top or side, bottom or side
        front, back = front or side, back or side
        self.rect(u + d, v, w, d, top)
        self.rect(u + d + w, v, w, d, bottom)
        self.rect(u, v + d, d, h, side)            # east/right
        self.rect(u + d, v + d, w, h, front)       # north/front
        self.rect(u + d + w, v + d, d, h, side)    # west/left
        self.rect(u + 2 * d + w, v + d, w, h, back)

def noise_col(base, var, seed):
    rnd = random.Random(seed)
    base = hexc(base)
    def f(x, y, w, h):
        k = rnd.randint(-var, var)
        return tuple(max(0, min(255, c + k)) for c in base)
    return f

def vgrad(top, bottom, var, seed, split=None, band=None):
    """Vertical gradient with pixel noise; optional lower colour band like the Blub's cyan belly."""
    rnd = random.Random(seed)
    top, bottom = hexc(top), hexc(bottom)
    bandc = hexc(band) if band else None
    def f(x, y, w, h):
        t = y / max(1, h - 1)
        if bandc and split is not None and y >= int(h * split):
            c = bandc
        else:
            c = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        k = rnd.randint(-var, var)
        return tuple(max(0, min(255, ch + k)) for ch in c)
    return f

def gen_blub():
    p = Painter(64, 32)
    body = vgrad('#6aa7e6', '#4d8fd6', 10, 1, split=0.62, band='#5fc8dc')
    def face(x, y, w, h):
        c = body(x, y, w, h)
        # eyes: wide dark purple rectangles, mouth: small purple block (matches the leak)
        if y == 3 and (1 <= x <= 2 or w - 3 <= x <= w - 2):
            return hexc('#3b2a7a')
        if y == 5 and w // 2 - 1 <= x <= w // 2:
            return hexc('#4c3a8e')
        return c
    # body: 10 x 8 x 9 at (0,0)
    p.box(0, 0, 10, 8, 9, body, top=noise_col('#74b2ee', 10, 2), bottom=noise_col('#5fc8dc', 8, 3), front=face)
    ear = vgrad('#7fb6ee', '#5c96d8', 8, 4)
    ear_front = lambda x, y, w, h: hexc('#3f7fd0') if 0 < x < w - 1 and 0 < y < h - 1 else ear(x, y, w, h)
    p.box(40, 0, 3, 6, 2, ear, front=ear_front)   # left ear
    p.box(50, 0, 3, 6, 2, ear, front=ear_front)   # right ear
    foot = noise_col('#9fe6f2', 8, 5)
    p.box(0, 20, 3, 2, 3, foot)
    p.box(12, 20, 3, 2, 3, foot)
    p.box(24, 20, 3, 2, 3, foot)
    p.box(36, 20, 3, 2, 3, foot)
    save(p.img, 'entity/blub/blub')

def gen_sifter():
    p = Painter(64, 64)
    bark = vgrad('#d0566e', '#7a2a4e', 14, 10)
    bark2 = vgrad('#b8486a', '#5e2046', 14, 11)
    def head_front(x, y, w, h):
        # a sieve-like face: grid of dark holes with two glowing cyan eyes
        if y in (3, 4) and (x in (1, 2) or x in (w - 3, w - 2)):
            return hexc('#7ffcff')
        if y >= 6 and (x + y) % 2 == 0 and 1 <= x <= w - 2 and y <= h - 2:
            return hexc('#2a0f2a')
        return bark(x, y, w, h)
    p.box(0, 0, 8, 10, 8, bark, top=noise_col('#e47a8e', 10, 12), front=head_front)   # head
    p.box(32, 0, 8, 12, 4, bark2)          # torso
    p.box(0, 20, 3, 16, 3, bark2)          # right arm
    p.box(16, 20, 3, 16, 3, bark2)         # left arm
    p.box(0, 40, 3, 14, 3, bark)           # right leg
    p.box(16, 40, 3, 14, 3, bark)          # left leg
    # glowing tendrils on the head
    tend = vgrad('#9ffcff', '#2a9fbf', 10, 13)
    p.box(32, 20, 1, 6, 1, tend)
    p.box(40, 20, 1, 6, 1, tend)
    p.box(48, 20, 1, 6, 1, tend)
    save(p.img, 'entity/sifter/sifter')
    # emissive eyes layer
    eyes = Painter(64, 64)
    eyes.rect(8 + 1, 8 + 3, 2, 2, lambda *a: hexc('#a8ffff'))
    eyes.rect(8 + 5, 8 + 3, 2, 2, lambda *a: hexc('#a8ffff'))
    save(eyes.img, 'entity/sifter/sifter_eyes')

def gen_icon():
    # mod icon: a big Blub face
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    f = vgrad('#6aa7e6', '#4d8fd6', 10, 7, split=0.62, band='#5fc8dc')
    for x in range(2, 14):
        for y in range(4, 15):
            px[x, y] = f(x - 2, y - 4, 12, 11) + (255,)
    for x in (3, 4, 11, 12):
        for y in range(0, 4):
            px[x, y] = (110, 168, 232, 255)
    for x in (4, 5, 6, 9, 10, 11):
        px[x, 8] = (59, 42, 122, 255)
    px[7, 10] = px[8, 10] = (76, 58, 142, 255)
    path = '/home/cat/Dokumente/Minecraft The Sift/src/main/resources/assets/thesift/icon.png'
    img.resize((128, 128), Image.NEAREST).save(path)


def hue_recolor(src, dst, dark, light, hue_lo, hue_hi, min_sat=0.12):
    """Recolours only pixels whose hue lies in [hue_lo, hue_hi] (degrees), e.g. the diamond part of a tool."""
    img = load(src)
    px = img.load()
    d, l = hexc(dark), hexc(light)
    sel = []
    for x in range(img.width):
        for y in range(img.height):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, sat, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if hue_lo <= h * 360 <= hue_hi and sat >= min_sat:
                sel.append((x, y))
    ls = [lum(px[x, y]) for x, y in sel] or [0, 1]
    lo, hi = min(ls), max(ls)
    for x, y in sel:
        tt = (lum(px[x, y]) - lo) / (hi - lo) if hi > lo else 0.5
        px[x, y] = tuple(int(d[i] + (l[i] - d[i]) * tt) for i in range(3)) + (px[x, y][3],)
    save(img, dst)
    return img

def ore(vanilla_ore, vanilla_base, base_img, dst, dark, light):
    """Puts the ore speckles of a vanilla ore onto a custom stone texture."""
    o, b = load(vanilla_ore).load(), load(vanilla_base).load()
    out = base_img.copy()
    px = out.load()
    d, l = hexc(dark), hexc(light)
    for x in range(16):
        for y in range(16):
            if o[x, y][:3] != b[x, y][:3]:
                tt = lum(o[x, y])
                px[x, y] = tuple(int(d[i] + (l[i] - d[i]) * tt) for i in range(3)) + (255,)
    save(out, dst)

def gen_expansion():
    sift = gradient(load('block/stone'), '#5a3d5f', '#9c7699')
    SD, SL = '#6a1838', '#ffcf9a'          # siftite: deep rose to warm gold
    ore('block/emerald_ore', 'block/stone', sift, 'block/siftite_ore', SD, SL)
    recolor('block/netherite_block', 'block/siftite_block', '#5a1634', '#f6b48a')
    recolor('block/raw_gold_block', 'block/raw_siftite_block', '#5a1634', '#e8a07c')
    recolor('item/raw_gold', 'item/raw_siftite', '#5a1634', '#e8a07c')
    recolor('item/netherite_ingot', 'item/siftite_ingot', SD, SL)
    for tool in ['sword', 'pickaxe', 'axe', 'shovel', 'hoe']:
        hue_recolor('item/diamond_' + tool, 'item/siftite_' + tool, SD, SL, 140, 220)
    hue_recolor('item/diamond_sword', 'item/resonant_blade', '#5a2a8a', '#9ffcff', 140, 220)
    for piece in ['helmet', 'chestplate', 'leggings', 'boots']:
        hue_recolor('item/diamond_' + piece, 'item/siftite_' + piece, SD, SL, 140, 220)
        recolor('item/iron_' + piece, 'item/carapace_' + piece, '#8a3a16', '#f7b067')
    hue_recolor('entity/equipment/humanoid/diamond', 'entity/equipment/humanoid/siftite', SD, SL, 140, 220)
    hue_recolor('entity/equipment/humanoid_leggings/diamond', 'entity/equipment/humanoid_leggings/siftite', SD, SL, 140, 220)
    recolor('entity/equipment/humanoid/iron', 'entity/equipment/humanoid/carapace', '#8a3a16', '#f7b067')
    recolor('entity/equipment/humanoid_leggings/iron', 'entity/equipment/humanoid_leggings/carapace', '#8a3a16', '#f7b067')
    recolor('item/armadillo_scute', 'item/carapace_scale', '#9a3c14', '#ffc07a')
    recolor('item/heart_of_the_sea', 'item/harmonic_core', '#6a2a9a', '#aefcff')
    recolor('item/allay_spawn_egg', 'item/nuzzle_spawn_egg', '#3f8a5a', '#f2e6b0')
    recolor('item/allay_spawn_egg', 'item/singer_spawn_egg', '#3a6fd0', '#ffd0f0')

    hue_recolor('block/soul_lantern', 'block/sift_lantern', '#2a9fbf', '#ffd6f8', 150, 220, 0.2)
    hue_recolor('item/soul_lantern', 'item/sift_lantern', '#2a9fbf', '#ffd6f8', 150, 220, 0.2)
    recolor('block/end_stone_bricks', 'block/meadow_rock_bricks', '#a63a46', '#f29a92')
    recolor('block/end_stone_bricks', 'block/carapace_bricks', '#9a4418', '#f2ac64')

    soil = recolor('block/coarse_dirt', 'block/rust_soil', '#6e3226', '#c26b4c')
    turf = recolor('block/grass_block_top', 'block/verdant_turf_top', '#4aa888', '#b8f6dc')
    save(side_overlay(soil, turf, 37), 'block/verdant_turf_side')
    recolor('block/pink_petals', 'block/glimmer_petals', '#b8a020', '#fff6a0')
    recolor('item/pink_petals', 'item/glimmer_petals', '#b8a020', '#fff6a0')
    recolor('block/pink_petals_stem', 'block/glimmer_petals_stem', '#2f7a50', '#7fd8a0')
    recolor('block/cornflower', 'block/bluebell', '#1f5fc0', '#a8eaff')
    recolor('block/lily_of_the_valley', 'block/pale_lily', '#3f8a70', '#f6fffb')

def gen_nuzzle():
    p = Painter(64, 32)
    fur = vgrad('#7fd0a0', '#3f8a5a', 16, 21)
    belly = noise_col('#f2e6b0', 10, 22)
    def face(x, y, w, h):
        if y in (1, 2) and (x in (1, 2) or x in (w - 3, w - 2)):
            return hexc('#1c1430') if y == 2 else hexc('#ffffff')
        if y == 4 and w // 2 - 2 <= x <= w // 2 + 1:
            return hexc('#c86a7a')
        return fur(x, y, w, h)
    p.box(0, 0, 10, 6, 10, fur, bottom=belly, front=face)       # body
    p.box(40, 0, 3, 3, 3, noise_col('#f2e6b0', 8, 23))          # eye bump l
    p.box(40, 8, 3, 3, 3, noise_col('#f2e6b0', 8, 24))          # eye bump r
    for i in range(4):
        p.box(i * 12, 20, 3, 3, 3, noise_col('#5aa878', 10, 25 + i))
    p.box(52, 0, 2, 2, 2, noise_col('#bff0d0', 8, 30))           # tuft
    save(p.img, 'entity/nuzzle/nuzzle')

def gen_singer():
    p = Painter(64, 64)
    body = vgrad('#ffd0f0', '#6a9fff', 10, 40)
    def face(x, y, w, h):
        if y == 4 and (x in (2, 3) or x in (w - 4, w - 3)):
            return hexc('#2a1f5a')
        if y in (7, 8) and w // 2 - 2 <= x <= w // 2 + 1:
            return hexc('#3a2a6a') if y == 8 else hexc('#ff9fd0')
        return body(x, y, w, h)
    p.box(0, 0, 12, 10, 12, body, top=noise_col('#ffe0f6', 8, 41), front=face)
    fin = vgrad('#bfe8ff', '#7fb0ff', 8, 42)
    p.box(0, 24, 8, 1, 6, fin)          # left fin
    p.box(30, 24, 8, 1, 6, fin)         # right fin
    p.box(0, 34, 6, 1, 8, fin)          # tail
    for i in range(4):
        p.box(i * 12, 46, 1, 8, 1, vgrad('#9ffcff', '#ffd0f0', 8, 43 + i))   # tendrils
    save(p.img, 'entity/singer/singer')

WOOD_COLORS = {
    # name: (bark dark, bark light, inner dark, inner light, leaves dark, leaves light)
    'duskwood': ('#24142e', '#5a3c78', '#2f6a72', '#8fd8cc', '#164a3e', '#5fd0a0'),
    'pearlbark': ('#a89cb8', '#fbf6ee', '#d0b47a', '#fff2cc', '#c08a22', '#ffe680'),
}

def gen_woods():
    for wood, (bd, bl, idk, il, ld, ll) in WOOD_COLORS.items():
        recolor('block/cherry_log', 'block/%s_log' % wood, bd, bl)
        recolor('block/cherry_log_top', 'block/%s_log_top' % wood, bd, il)
        recolor('block/stripped_cherry_log', 'block/stripped_%s_log' % wood, idk, il)
        recolor('block/stripped_cherry_log_top', 'block/stripped_%s_log_top' % wood, idk, il)
        recolor('block/cherry_planks', 'block/%s_planks' % wood, idk, il)
        recolor('block/cherry_leaves', 'block/%s_leaves' % wood, ld, ll, copy_meta=True)
        recolor('block/cherry_door_top', 'block/%s_door_top' % wood, idk, il)
        recolor('block/cherry_door_bottom', 'block/%s_door_bottom' % wood, idk, il)
        recolor('block/cherry_trapdoor', 'block/%s_trapdoor' % wood, idk, il)
        recolor('block/cherry_sapling', 'block/%s_sapling' % wood, ld, ll)
        recolor('item/cherry_door', 'item/%s_door' % wood, idk, il)
    boats = dict(WOOD_COLORS)
    boats['sift_willow'] = (None, None, '#86a9bf', '#dbeef8', None, None)
    for wood, cols in boats.items():
        idk, il = cols[2], cols[3]
        recolor('item/cherry_boat', 'item/%s_boat' % wood, idk, il)
        recolor('item/cherry_chest_boat', 'item/%s_chest_boat' % wood, idk, il)
        recolor('entity/boat/cherry', 'entity/boat/%s' % wood, idk, il)
        recolor('entity/chest_boat/cherry', 'entity/chest_boat/%s' % wood, idk, il)

def gen_v4():
    sift = gradient(load('block/stone'), '#5a3d5f', '#9c7699')
    soil = gradient(load('block/coarse_dirt'), '#6e3226', '#c26b4c')
    dusk = recolor('block/moss_block', 'block/dusk_turf_top', '#1a5a3a', '#6fd890')
    save(side_overlay(soil, dusk, 41), 'block/dusk_turf_side')
    pearl = recolor('block/moss_block', 'block/pearl_moss_top', '#b8b0d0', '#fffcf4')
    save(side_overlay(soil, pearl, 43), 'block/pearl_moss_side')
    recolor('block/sand', 'block/sift_sand', '#a88cc0', '#f4e4fa')
    for name, van, dark, light in (('sift_coal_ore', 'coal_ore', '#140c18', '#4a3a50'), ('sift_copper_ore', 'copper_ore', '#8a4a20', '#f0a070'),
                                   ('sift_iron_ore', 'iron_ore', '#9a7a60', '#f6dcc0'), ('sift_gold_ore', 'gold_ore', '#a07010', '#fff090'),
                                   ('sift_diamond_ore', 'diamond_ore', '#1a8a90', '#c0fff8'), ('lumenite_ore', 'emerald_ore', '#c08a10', '#fffcc0'),
                                   ('jade_ore', 'diamond_ore', '#0f6a40', '#90f0b0')):
        ore('block/' + van, 'block/stone', sift, 'block/' + name, dark, light)
    recolor('block/glowstone', 'block/lumenite_block', '#c8901a', '#fffbd8')
    recolor('block/emerald_block', 'block/jade_block', '#0d5a38', '#8ef0b8')
    recolor('block/enchanting_table_top', 'block/sift_altar_top', '#2a1838', '#d8a0ff')
    recolor('block/chiseled_stone_bricks', 'block/sift_altar_side', '#3a1f4a', '#d8a0ff')
    recolor('block/polished_andesite', 'block/sift_altar_bottom', '#5e4262', '#a8829f')
    recolor('block/deepslate_tiles', 'block/temple_bricks', '#0c141c', '#3e5a68')

    recolor('item/amethyst_shard', 'item/lumenite_shard', '#b07a10', '#fffcc8')
    recolor('item/emerald', 'item/jade', '#0d5a38', '#9ef6c4')
    recolor('item/leather', 'item/warden_hide', '#06161c', '#2fa0a8')
    recolor('item/heart_of_the_sea', 'item/warden_heart', '#041a20', '#3ff4ff')
    recolor('item/netherite_sword', 'item/warden_cleaver', '#06121a', '#5fe8f0')
    recolor('item/apple', 'item/duskfruit', '#3a1658', '#d890ff')
    recolor('item/apple', 'item/roasted_duskfruit', '#2a1020', '#b06a70')
    recolor('item/beef', 'item/venison', '#6a1a24', '#f0a0a0')
    recolor('item/cooked_beef', 'item/cooked_venison', '#3a1a10', '#c08050')
    recolor('item/cod', 'item/ray_fillet', '#7a3a7a', '#ffd0f0')
    recolor('item/cooked_cod', 'item/cooked_ray_fillet', '#5a2a30', '#f0b090')
    recolor('item/slime_ball', 'item/blub_jelly', '#2a5ab0', '#b0f0ff')
    for mat, (dark, light) in (('lumenite', ('#a07010', '#fffad0')), ('jade', ('#0d5a38', '#9ef6c4')), ('warden', ('#06161c', '#3fd8e0'))):
        for piece in ['helmet', 'chestplate', 'leggings', 'boots']:
            recolor('item/iron_' + piece, 'item/%s_%s' % (mat, piece), dark, light)
        recolor('entity/equipment/humanoid/iron', 'entity/equipment/humanoid/' + mat, dark, light)
        recolor('entity/equipment/humanoid_leggings/iron', 'entity/equipment/humanoid_leggings/' + mat, dark, light)
    for egg, dark, light in (('shellback', '#8a3a16', '#f7b067'), ('lumoth', '#b08a20', '#fff6b0'), ('pearl_stag', '#b8b0c8', '#ffe680'),
                             ('ichor_ray', '#6a3aa0', '#ffc8f0')):
        recolor('item/allay_spawn_egg', 'item/%s_spawn_egg' % egg, dark, light)
    gen_woods()
    gen_ores()
    recolor('block/deepslate', 'block/deep_sift_stone', '#2a1a34', '#6a4a78')
    recolor('block/cobbled_deepslate', 'block/cobbled_deep_sift_stone', '#2a1a34', '#6a4a78')
    recolor('block/tuff', 'block/veilstone', '#3a5a78', '#a8d0e8')
    recolor('block/polished_tuff', 'block/polished_veilstone', '#3a5a78', '#b8dcf0')
    recolor('block/calcite', 'block/rose_marble', '#c88aa0', '#fff0f4')
    recolor('block/smooth_stone', 'block/polished_rose_marble', '#c88aa0', '#fff0f4')
    recolor('block/stone_bricks', 'block/rose_marble_bricks', '#b87a92', '#fbe6ee')
    recolor('block/gravel', 'block/sift_gravel', '#3a2a40', '#a088a8')
    recolor('block/brown_mushroom', 'block/glowcap', '#0a6a7a', '#b0fff8')
    recolor('item/ender_eye', 'item/echo_eye', '#043a40', '#7ffcf4')
    recolor('item/porkchop', 'item/bumblehog_chop', '#8a3a50', '#ffb8c8')
    recolor('item/cooked_porkchop', 'item/cooked_bumblehog_chop', '#5a2a1a', '#e0a070')
    for egg, dark, light in (('shade', '#1a1030', '#8a70d0'), ('gloomstalker', '#0a1430', '#3ff0ff'), ('bumblehog', '#a05a70', '#ffe0a0')):
        recolor('item/allay_spawn_egg', 'item/%s_spawn_egg' % egg, dark, light)
    recolor('block/short_grass', 'block/mint_grass', '#3a9a7a', '#b4f6dc')
    recolor('block/tall_grass_top', 'block/tall_mint_grass_top', '#3a9a7a', '#b4f6dc')
    recolor('block/tall_grass_bottom', 'block/tall_mint_grass_bottom', '#2f8a6c', '#a0eccc')

def gen_ores():
    """Hand-designed ore overlays on sift stone - every ore has its own shape, not a recolour of vanilla."""
    base = gradient(load('block/stone'), '#5a3d5f', '#9c7699')
    def paint(name, pixels, outline, mid, light, seed):
        img = base.copy()
        px = img.load()
        pts = set(pixels)
        for x, y in pts:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                q = (x + dx, y + dy)
                if q not in pts and 0 <= q[0] < 16 and 0 <= q[1] < 16:
                    px[q] = hexc(outline) + (255,)
        rnd = random.Random(seed)
        for x, y in pts:
            hi = (x - 1, y) not in pts or (x, y - 1) not in pts
            px[x, y] = hexc(light if hi and rnd.random() < 0.8 else mid) + (255,)
        save(img, 'block/' + name)
    def blobs(centres, r):
        out = []
        for cx, cy in centres:
            for x in range(16):
                for y in range(16):
                    if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                        out.append((x, y))
        return out
    def gems(centres):
        out = []
        for cx, cy in centres:
            for dx, dy in ((0, -2), (-1, -1), (0, -1), (1, -1), (-1, 0), (0, 0), (1, 0), (-1, 1), (0, 1), (1, 1), (0, 2)):
                out.append((cx + dx, cy + dy))
        return out
    def stars(centres):
        out = []
        for cx, cy in centres:
            out += [(cx, cy), (cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1), (cx + 2, cy), (cx - 2, cy), (cx, cy + 2), (cx, cy - 2)]
        return out
    def veins(seed):
        rnd = random.Random(seed)
        out = []
        for _ in range(3):
            x, y = rnd.randrange(16), rnd.randrange(4)
            for _ in range(14):
                out.append((x % 16, y % 16))
                x += rnd.choice((0, 1, 1)); y += 1
        return out
    paint('siftite_ore', veins(3) + gems([(4, 4), (11, 10)]), '#3a0c22', '#c0506a', '#ffd8a8', 1)
    paint('jade_ore', gems([(4, 4), (11, 5), (6, 11), (12, 12)]), '#05301c', '#2fa060', '#a8ffc8', 2)
    paint('sift_diamond_ore', gems([(4, 5), (11, 4), (8, 11)]), '#0a3a40', '#4ad8e0', '#d8ffff', 3)
    paint('lumenite_ore', stars([(4, 4), (11, 6), (6, 11), (12, 12)]), '#6a4a08', '#f0c030', '#fffbd0', 4)
    paint('sift_gold_ore', blobs([(4, 4), (11, 6), (6, 11), (12, 12)], 1.6), '#5a3a06', '#e8b020', '#fff4a0', 5)
    paint('sift_iron_ore', blobs([(4, 5), (11, 4), (7, 11), (13, 11)], 1.7), '#4a3020', '#c89a78', '#f6e0cc', 6)
    paint('sift_copper_ore', blobs([(5, 4), (11, 7), (4, 11), (12, 13)], 1.7), '#4a1e0a', '#d07040', '#ffc090', 7)
    paint('sift_coal_ore', blobs([(4, 4), (10, 5), (6, 11), (12, 12), (8, 8)], 1.5), '#2a2230', '#141018', '#4a4250', 8)

def gen_shade():
    p = Painter(64, 64)
    cloth = vgrad('#3a2a5a', '#160e2a', 10, 140)
    def hood_front(x, y, w, h):
        # dark open face with two burning eyes
        if 1 <= x <= w - 2 and 2 <= y <= h - 1:
            if y == 4 and x in (2, 5):
                return hexc('#7ffcff')
            return hexc('#05030a')
        return cloth(x, y, w, h)
    p.box(0, 0, 8, 8, 8, cloth, top=noise_col('#4a3870', 8, 141), front=hood_front)
    p.box(32, 0, 4, 3, 3, cloth)
    def torso_front(x, y, w, h):
        if x == w // 2 or x == w // 2 - 1:
            return hexc('#5a3a8a') if y % 3 else hexc('#7ffcff')
        return cloth(x, y, w, h)
    p.box(0, 16, 8, 10, 5, cloth, front=torso_front)
    robe = vgrad('#2a1e48', '#0c0818', 10, 142)
    def tattered(x, y, w, h):
        if y >= h - 2 and (x * 7 + y) % 3 == 0:
            return (0, 0, 0, 0)
        return robe(x, y, w, h)
    p.box(0, 32, 10, 11, 7, tattered, top=robe)
    for i in range(3):
        p.box(i * 8, 52, 2, 6, 1, tattered)
    arm = vgrad('#3a2a5a', '#b8a8f0', 6, 143)
    p.box(40, 16, 3, 12, 3, arm)
    p.box(40, 32, 3, 12, 3, arm)
    save(p.img, 'entity/shade/shade')
    glow = Painter(64, 64)
    glow.rect(8 + 2, 8 + 4, 1, 1, lambda *a: hexc('#bffcff'))
    glow.rect(8 + 5, 8 + 4, 1, 1, lambda *a: hexc('#bffcff'))
    glow.rect(5 + 3, 21, 2, 10, lambda x, y, w, h: hexc('#7ffcff') if y % 3 == 0 else None)
    for u in (40, 40):
        pass
    glow.box(40, 16, 3, 12, 3, lambda x, y, w, h: hexc('#d8ccff') if y >= h - 3 else None)
    glow.box(40, 32, 3, 12, 3, lambda x, y, w, h: hexc('#d8ccff') if y >= h - 3 else None)
    save(glow.img, 'entity/shade/shade_glow')

def gen_gloomstalker():
    p = Painter(128, 64)
    shell = vgrad('#1a2448', '#070a18', 10, 150)
    def spots(x, y, w, h):
        if (x * 5 + y * 3) % 13 == 0:
            return hexc('#2fd8e8')
        return shell(x, y, w, h)
    eyes = {(1, 1), (6, 1), (2, 3), (5, 3), (3, 1), (4, 1)}
    def head_front(x, y, w, h):
        if (x, y) in eyes:
            return hexc('#8ffcff')
        return shell(x, y, w, h)
    p.box(0, 0, 8, 6, 8, shell, front=head_front)
    p.box(32, 0, 6, 5, 6, shell)
    p.box(0, 14, 10, 8, 12, spots, top=spots, bottom=noise_col('#0a1028', 6, 151))
    crystal = vgrad('#e0ffff', '#2aa8c8', 6, 152)
    for i in range(3):
        p.box(100 + i * 9, 0, 2, 4, 2, crystal)
    p.box(90, 0, 2, 3, 2, noise_col('#c8c0a8', 6, 153))
    p.box(90, 6, 2, 3, 2, noise_col('#c8c0a8', 6, 154))
    leg = vgrad('#20284a', '#0a0e1e', 6, 155)
    def leg_bands(x, y, w, h):
        if x % 6 == 0:
            return hexc('#1f6a80')
        return leg(x, y, w, h)
    for pair in range(4):
        p.box(0, 36 + pair * 4, 16, 2, 2, leg_bands)
        p.box(64, 36 + pair * 4, 16, 2, 2, leg_bands)
    save(p.img, 'entity/gloomstalker/gloomstalker')
    glow = Painter(128, 64)
    glow.rect(8, 8, 8, 6, lambda x, y, w, h: hexc('#bffcff') if (x, y) in eyes else None)
    glow.box(0, 14, 10, 8, 12, lambda x, y, w, h: hexc('#5ff0ff') if (x * 5 + y * 3) % 13 == 0 else None)
    for i in range(3):
        glow.box(100 + i * 9, 0, 2, 4, 2, crystal)
    save(glow.img, 'entity/gloomstalker/gloomstalker_glow')

def gen_bumblehog():
    p = Painter(64, 64)
    fur = vgrad('#ffcaa0', '#d08a70', 12, 120)
    p.box(0, 0, 10, 9, 12, fur, top=noise_col('#ffd8b8', 10, 121))
    def face(x, y, w, h):
        if y == 2 and x in (1, w - 2):
            return hexc('#201010')
        return fur(x, y, w, h)
    p.box(0, 24, 8, 7, 6, fur, front=face)
    p.box(28, 24, 4, 3, 2, noise_col('#ff9aa8', 6, 122))
    for i in range(4):
        p.box(i * 12, 44, 3, 4, 3, noise_col('#8a5a4a', 8, 123 + i))
    save(p.img, 'entity/bumblehog/bumblehog')

def gen_shellback():
    p = Painter(64, 64)
    shell = vgrad('#f2a65a', '#9a3c14', 14, 70)
    def shell_top(x, y, w, h):
        if x % 5 == 0 or y % 5 == 0:
            return hexc('#7a2a10')
        return shell(x, y, w, h)
    p.box(0, 0, 14, 8, 16, shell, top=shell_top)
    skin = vgrad('#8a7a5a', '#5a4a34', 10, 71)
    p.box(0, 24, 12, 3, 14, skin, bottom=noise_col('#c8b080', 8, 72))
    def face(x, y, w, h):
        if y == 1 and x in (1, w - 2):
            return hexc('#101010')
        return skin(x, y, w, h)
    p.box(0, 44, 6, 5, 6, skin, front=face)
    for u, v in ((24, 44), (40, 44), (24, 52), (40, 52)):
        p.box(u, v, 4, 4, 4, noise_col('#6a5a40', 10, u + v))
    save(p.img, 'entity/shellback/shellback')

def gen_pearl_stag():
    p = Painter(64, 64)
    fur = vgrad('#f4f0f8', '#c8c0d8', 8, 80)
    p.box(0, 0, 8, 8, 14, fur, bottom=noise_col('#fffaf4', 6, 81))
    p.box(44, 0, 3, 7, 3, fur)
    def face(x, y, w, h):
        if y == 1 and x in (0, w - 1):
            return hexc('#2a1838')
        if y >= h - 2 and w // 2 - 1 <= x <= w // 2:
            return hexc('#3a2a3a')
        return fur(x, y, w, h)
    p.box(0, 22, 5, 5, 8, fur, front=face)
    gold = vgrad('#fff6b0', '#d0a020', 6, 82)
    p.box(28, 22, 1, 7, 1, gold)
    p.box(32, 22, 1, 7, 1, gold)
    p.box(36, 22, 3, 1, 1, gold)
    p.box(36, 26, 3, 1, 1, gold)
    p.box(40, 36, 2, 3, 1, noise_col('#ffffff', 4, 83))
    for i in range(4):
        p.box(i * 10, 36, 2, 11, 2, vgrad('#e0d8ea', '#8a7a90', 8, 84 + i))
    save(p.img, 'entity/pearl_stag/pearl_stag')
    glow = Painter(64, 64)
    glow.box(28, 22, 1, 7, 1, gold)
    glow.box(32, 22, 1, 7, 1, gold)
    glow.box(36, 22, 3, 1, 1, gold)
    glow.box(36, 26, 3, 1, 1, gold)
    save(glow.img, 'entity/pearl_stag/pearl_stag_glow')

def gen_lumoth():
    p = Painter(32, 32)
    body = vgrad('#fff6c0', '#b08a40', 10, 90)
    p.box(0, 0, 2, 2, 5, body)
    wing = lambda x, y, w, h: hexc('#ffe680') if (x + y) % 4 else hexc('#c08a20')
    p.box(0, 8, 7, 0, 6, wing)
    p.box(0, 16, 7, 0, 6, wing)
    p.box(16, 0, 3, 2, 0, noise_col('#5a4020', 6, 91))
    save(p.img, 'entity/lumoth/lumoth')
    glow = Painter(32, 32)
    glow.box(0, 8, 7, 0, 6, lambda x, y, w, h: hexc('#fff4a0') if (x + y) % 4 else None)
    glow.box(0, 16, 7, 0, 6, lambda x, y, w, h: hexc('#fff4a0') if (x + y) % 4 else None)
    glow.box(0, 0, 2, 2, 5, lambda x, y, w, h: hexc('#fffbe0'))
    save(glow.img, 'entity/lumoth/lumoth_glow')

def gen_ichor_ray():
    p = Painter(64, 32)
    top = vgrad('#c890f0', '#6a3aa0', 12, 95)
    belly = noise_col('#ffe8f8', 6, 96)
    def back(x, y, w, h):
        if (x * 7 + y * 3) % 11 == 0:
            return hexc('#9ffcff')
        return top(x, y, w, h)
    p.box(0, 0, 10, 2, 10, top, top=back, bottom=belly)
    p.box(0, 12, 8, 1, 8, top, top=back, bottom=belly)
    p.box(32, 12, 8, 1, 8, top, top=back, bottom=belly)
    p.box(40, 0, 1, 1, 10, noise_col('#5a2a8a', 8, 97))
    save(p.img, 'entity/ichor_ray/ichor_ray')

if __name__ == '__main__':
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    gen_blocks()
    gen_items()
    gen_blub()
    gen_sifter()
    gen_icon()
    gen_expansion()
    gen_nuzzle()
    gen_singer()
    gen_v4()
    gen_shellback()
    gen_pearl_stag()
    gen_lumoth()
    gen_ichor_ray()
    gen_bumblehog()
    gen_shade()
    gen_gloomstalker()
    print('textures done')
