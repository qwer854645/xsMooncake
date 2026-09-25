"""Hardened derivatives = vanilla textures, mooncake-palette recolor only.

Wood / stick pixels stay vanilla. Metal (iron gray, copper, mace head, …)
is remapped to the matching hardened-mooncake spoil-stage colors.

Waxed variants share textures with their unwaxed spoil twin.
"""
from __future__ import annotations

import colorsys
import json
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/mooncake"
TEX_BLOCK = ASSETS / "textures/block"
TEX_ITEM = ASSETS / "textures/item"
TEX_ARMOR = ASSETS / "textures/models/armor"
TEX_ENTITY = ASSETS / "textures/entity"
BLOCK_MODELS = ASSETS / "models/block"
ITEM_MODELS = ASSETS / "models/item"
BLOCKSTATES = ASSETS / "blockstates"
VAN = ROOT / "tools/_van"
MC_JAR = Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar"

SPOIL = ["", "exposed_", "weathered_", "oxidized_"]
WAX_PREFIXES = {
    "": ["", "waxed_"],
    "exposed_": ["exposed_", "waxed_exposed_"],
    "weathered_": ["weathered_", "waxed_weathered_"],
    "oxidized_": ["oxidized_", "waxed_oxidized_"],
}

# jar path → local _van relative path
EXTRACT = [
    "item/iron_sword.png",
    "item/iron_pickaxe.png",
    "item/iron_axe.png",
    "item/iron_shovel.png",
    "item/iron_hoe.png",
    "item/iron_helmet.png",
    "item/iron_chestplate.png",
    "item/iron_leggings.png",
    "item/iron_boots.png",
    "item/bow.png",
    "item/crossbow_standby.png",
    "item/arrow.png",
    "item/trident.png",
    "item/mace.png",
    "item/copper_door.png",
    "item/exposed_copper_door.png",
    "item/weathered_copper_door.png",
    "item/oxidized_copper_door.png",
    "block/copper_block.png",
    "block/exposed_copper.png",
    "block/weathered_copper.png",
    "block/oxidized_copper.png",
    "block/cut_copper.png",
    "block/exposed_cut_copper.png",
    "block/weathered_cut_copper.png",
    "block/oxidized_cut_copper.png",
    "block/copper_door_top.png",
    "block/copper_door_bottom.png",
    "block/exposed_copper_door_top.png",
    "block/exposed_copper_door_bottom.png",
    "block/weathered_copper_door_top.png",
    "block/weathered_copper_door_bottom.png",
    "block/oxidized_copper_door_top.png",
    "block/oxidized_copper_door_bottom.png",
    "models/armor/iron_layer_1.png",
    "models/armor/iron_layer_2.png",
    "entity/projectiles/arrow.png",
    "entity/shield_base_nopattern.png",
]

# Building / doors: always the fresh copper texture as base (same idea as iron
# tools for every spoil stage). Only the mooncake palette changes per stage.
VAN_BLOCK = "block/copper_block.png"
VAN_CUT = "block/cut_copper.png"
VAN_DOOR_BOTTOM = "block/copper_door_bottom.png"
VAN_DOOR_TOP = "block/copper_door_top.png"
VAN_DOOR_ITEM = "item/copper_door.png"

GEAR_ITEMS = {
    "sword": ("minecraft:item/handheld", "iron_sword"),
    "pickaxe": ("minecraft:item/handheld", "iron_pickaxe"),
    "axe": ("minecraft:item/handheld", "iron_axe"),
    "shovel": ("minecraft:item/handheld", "iron_shovel"),
    "hoe": ("minecraft:item/handheld", "iron_hoe"),
    "helmet": ("minecraft:item/generated", "iron_helmet"),
    "chestplate": ("minecraft:item/generated", "iron_chestplate"),
    "leggings": ("minecraft:item/generated", "iron_leggings"),
    "boots": ("minecraft:item/generated", "iron_boots"),
    "trident": ("minecraft:item/generated", "trident"),
    "mace": ("minecraft:item/handheld", "mace"),
    "bow": ("minecraft:item/generated", "bow"),
    "crossbow": ("minecraft:item/generated", "crossbow_standby"),
    "arrow": ("minecraft:item/generated", "arrow"),
}

DOOR_MODEL_SUFFIXES = [
    "bottom_left",
    "bottom_left_open",
    "bottom_right",
    "bottom_right_open",
    "top_left",
    "top_left_open",
    "top_right",
    "top_right_open",
]


def ensure_vanilla() -> None:
    VAN.mkdir(parents=True, exist_ok=True)
    missing = [p for p in EXTRACT if not (VAN / p).is_file()]
    if not missing:
        return
    if not MC_JAR.is_file():
        raise FileNotFoundError(f"Minecraft jar not found: {MC_JAR}")
    with zipfile.ZipFile(MC_JAR) as zf:
        for rel in missing:
            entry = f"assets/minecraft/textures/{rel}"
            data = zf.read(entry)
            dest = VAN / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def mix(c0: tuple[int, int, int], c1: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return (
        int(lerp(c0[0], c1[0], t)),
        int(lerp(c0[1], c1[1], t)),
        int(lerp(c0[2], c1[2], t)),
    )


def lum(c: tuple[int, int, int]) -> float:
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def extract_palette(prefix: str) -> list[tuple[int, int, int]]:
    """Sorted unique-ish key colors from hardened cake faces (dark → light)."""
    cols: list[tuple[int, int, int]] = []
    for face in ("top", "side", "bottom"):
        im = Image.open(TEX_BLOCK / f"{prefix}hardened_mooncake_{face}.png").convert("RGBA")
        for r, g, b, a in im.getdata():
            if a > 200:
                cols.append((r, g, b))
    uniq = sorted(set(cols), key=lum)
    # pick ~6 anchors across luminance
    n = len(uniq)
    idxs = [0, n // 5, 2 * n // 5, 3 * n // 5, 4 * n // 5, n - 1]
    anchors = [uniq[i] for i in idxs]
    # ensure endpoints span a bit
    anchors[0] = mix(anchors[0], (0, 0, 0), 0.15)
    anchors[-1] = mix(anchors[-1], (255, 255, 255), 0.08)
    return anchors


def is_wood(r: int, g: int, b: int) -> bool:
    """Vanilla stick / plank browns — leave untouched."""
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    if s < 0.32:
        return False
    # oak stick hue cluster ~0.08–0.12; allow a little room
    if 0.04 <= h <= 0.15 and v < 0.72:
        return True
    return False


def sample_ramp(anchors: list[tuple[int, int, int]], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t))
    if len(anchors) == 1:
        return anchors[0]
    scaled = t * (len(anchors) - 1)
    i = int(scaled)
    if i >= len(anchors) - 1:
        return anchors[-1]
    frac = scaled - i
    return mix(anchors[i], anchors[i + 1], frac)


def recolor(
    src: Image.Image,
    anchors: list[tuple[int, int, int]],
    *,
    preserve_wood: bool = True,
) -> Image.Image:
    """Remap opaque (non-wood) pixels by luminance onto mooncake anchors."""
    im = src.convert("RGBA")
    px = list(im.getdata())

    # luminance range of pixels we will recolor
    lums: list[float] = []
    for r, g, b, a in px:
        if a < 16:
            continue
        if preserve_wood and is_wood(r, g, b):
            continue
        lums.append(lum((r, g, b)))
    if not lums:
        return im
    lo, hi = min(lums), max(lums)
    span = max(1.0, hi - lo)

    out = []
    for r, g, b, a in px:
        if a < 16:
            out.append((r, g, b, a))
            continue
        if preserve_wood and is_wood(r, g, b):
            out.append((r, g, b, a))
            continue
        t = (lum((r, g, b)) - lo) / span
        nr, ng, nb = sample_ramp(anchors, t)
        out.append((nr, ng, nb, a))
    im.putdata(out)
    return im


def open_van(rel: str) -> Image.Image:
    return Image.open(VAN / rel).convert("RGBA")


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def write_door_models(spoil: str) -> None:
    top = f"mooncake:block/{spoil}hardened_mooncake_door_top"
    bottom = f"mooncake:block/{spoil}hardened_mooncake_door_bottom"
    for suf in DOOR_MODEL_SUFFIXES:
        write_json(
            BLOCK_MODELS / f"{spoil}hardened_mooncake_door_{suf}.json",
            {
                "parent": f"minecraft:block/door_{suf}",
                "textures": {"top": top, "bottom": bottom},
            },
        )


def write_door_blockstate(model_prefix: str, spoil: str) -> None:
    base = f"mooncake:block/{spoil}hardened_mooncake_door"
    table = [
        ("east", "lower", "left", False, f"{base}_bottom_left", 0),
        ("east", "lower", "left", True, f"{base}_bottom_left_open", 90),
        ("east", "lower", "right", False, f"{base}_bottom_right", 0),
        ("east", "lower", "right", True, f"{base}_bottom_right_open", 270),
        ("east", "upper", "left", False, f"{base}_top_left", 0),
        ("east", "upper", "left", True, f"{base}_top_left_open", 90),
        ("east", "upper", "right", False, f"{base}_top_right", 0),
        ("east", "upper", "right", True, f"{base}_top_right_open", 270),
        ("north", "lower", "left", False, f"{base}_bottom_left", 270),
        ("north", "lower", "left", True, f"{base}_bottom_left_open", 0),
        ("north", "lower", "right", False, f"{base}_bottom_right", 270),
        ("north", "lower", "right", True, f"{base}_bottom_right_open", 180),
        ("north", "upper", "left", False, f"{base}_top_left", 270),
        ("north", "upper", "left", True, f"{base}_top_left_open", 0),
        ("north", "upper", "right", False, f"{base}_top_right", 270),
        ("north", "upper", "right", True, f"{base}_top_right_open", 180),
        ("south", "lower", "left", False, f"{base}_bottom_left", 90),
        ("south", "lower", "left", True, f"{base}_bottom_left_open", 180),
        ("south", "lower", "right", False, f"{base}_bottom_right", 90),
        ("south", "lower", "right", True, f"{base}_bottom_right_open", 0),
        ("south", "upper", "left", False, f"{base}_top_left", 90),
        ("south", "upper", "left", True, f"{base}_top_left_open", 180),
        ("south", "upper", "right", False, f"{base}_top_right", 90),
        ("south", "upper", "right", True, f"{base}_top_right_open", 0),
        ("west", "lower", "left", False, f"{base}_bottom_left", 180),
        ("west", "lower", "left", True, f"{base}_bottom_left_open", 270),
        ("west", "lower", "right", False, f"{base}_bottom_right", 180),
        ("west", "lower", "right", True, f"{base}_bottom_right_open", 90),
        ("west", "upper", "left", False, f"{base}_top_left", 180),
        ("west", "upper", "left", True, f"{base}_top_left_open", 270),
        ("west", "upper", "right", False, f"{base}_top_right", 180),
        ("west", "upper", "right", True, f"{base}_top_right_open", 90),
    ]
    variants: dict[str, dict] = {}
    for facing, half, hinge, open_, model, y in table:
        key = f"facing={facing},half={half},hinge={hinge},open={str(open_).lower()}"
        entry: dict = {"model": model}
        if y:
            entry["y"] = y
        variants[key] = entry
    write_json(BLOCKSTATES / f"{model_prefix}hardened_mooncake_door.json", {"variants": variants})


def write_building_models(model_prefix: str, spoil: str) -> None:
    block = f"mooncake:block/{spoil}hardened_mooncake_block"
    cut = f"mooncake:block/{spoil}cut_hardened_mooncake"
    write_json(
        BLOCK_MODELS / f"{model_prefix}hardened_mooncake_block.json",
        {"parent": "minecraft:block/cube_all", "textures": {"all": block}},
    )
    write_json(
        ITEM_MODELS / f"{model_prefix}hardened_mooncake_block.json",
        {"parent": f"mooncake:block/{model_prefix}hardened_mooncake_block"},
    )
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake.json",
        {"parent": "minecraft:block/cube_all", "textures": {"all": cut}},
    )
    write_json(
        ITEM_MODELS / f"{model_prefix}cut_hardened_mooncake.json",
        {"parent": f"mooncake:block/{model_prefix}cut_hardened_mooncake"},
    )
    slab_tex = {"bottom": cut, "top": cut, "side": cut}
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake_slab.json",
        {"parent": "minecraft:block/slab", "textures": slab_tex},
    )
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake_slab_top.json",
        {"parent": "minecraft:block/slab_top", "textures": slab_tex},
    )
    write_json(
        ITEM_MODELS / f"{model_prefix}cut_hardened_mooncake_slab.json",
        {"parent": f"mooncake:block/{model_prefix}cut_hardened_mooncake_slab"},
    )
    stairs_tex = {"bottom": cut, "top": cut, "side": cut}
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake_stairs.json",
        {"parent": "minecraft:block/stairs", "textures": stairs_tex},
    )
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake_stairs_inner.json",
        {"parent": "minecraft:block/inner_stairs", "textures": stairs_tex},
    )
    write_json(
        BLOCK_MODELS / f"{model_prefix}cut_hardened_mooncake_stairs_outer.json",
        {"parent": "minecraft:block/outer_stairs", "textures": stairs_tex},
    )
    write_json(
        ITEM_MODELS / f"{model_prefix}cut_hardened_mooncake_stairs.json",
        {"parent": f"mooncake:block/{model_prefix}cut_hardened_mooncake_stairs"},
    )


def write_gear_models(model_prefix: str, spoil: str) -> None:
    for kind in GEAR_ITEMS:
        parent = GEAR_ITEMS[kind][0]
        write_json(
            ITEM_MODELS / f"{model_prefix}hardened_mooncake_{kind}.json",
            {
                "parent": parent,
                "textures": {"layer0": f"mooncake:item/{spoil}hardened_mooncake_{kind}"},
            },
        )
    write_json(
        ITEM_MODELS / f"{model_prefix}hardened_mooncake_shield.json",
        {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"mooncake:item/{spoil}hardened_mooncake_shield"},
        },
    )
    write_json(
        ITEM_MODELS / f"{model_prefix}hardened_mooncake_door.json",
        {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"mooncake:item/{spoil}hardened_mooncake_door"},
        },
    )


def shield_item_icon(src: Image.Image) -> Image.Image:
    """Downscale entity shield sheet to a 16×16 inventory glyph."""
    # nopattern sheet is typically 64×64; take front face region and shrink
    w, h = src.size
    face = src.crop((0, 0, w // 2, h // 2)) if w >= 32 else src
    return face.resize((16, 16), Image.Resampling.NEAREST)


def _bevel_panel(
    put,
    ox: int,
    oy: int,
    size: int,
    crust: tuple[int, int, int],
    hi: tuple[int, int, int],
    lo: tuple[int, int, int],
) -> None:
    """One matte panel with cut-style bevel (light TL, dark BR)."""
    for y in range(size):
        for x in range(size):
            put(ox + x, oy + y, crust)
    last = size - 1
    for i in range(size):
        put(ox + i, oy + 0, hi)
        put(ox + 0, oy + i, hi)
        put(ox + i, oy + last, lo)
        put(ox + last, oy + i, lo)
    put(ox, oy, hi)
    put(ox + last, oy + last, lo)
    if size > 2:
        for i in range(1, last):
            put(ox + i, oy + 1, mix(crust, hi, 0.25))
            put(ox + 1, oy + i, mix(crust, hi, 0.2))
            put(ox + i, oy + last - 1, mix(crust, lo, 0.2))
            put(ox + last - 1, oy + i, mix(crust, lo, 0.15))


def paint_block_face(anchors: list[tuple[int, int, int]]) -> Image.Image:
    """Full block: single solid panel with the same bevel shading as cut tiles."""
    dark = anchors[0]
    deep = anchors[1]
    crust = anchors[min(len(anchors) - 2, 3)]
    hi = mix(crust, anchors[-1], 0.2)
    lo = mix(deep, dark, 0.25)

    im = Image.new("RGBA", (16, 16))
    px = im.load()

    def put(x: int, y: int, c: tuple[int, int, int]) -> None:
        px[x, y] = (*c, 255)

    _bevel_panel(put, 0, 0, 16, crust, hi, lo)
    # dark outer edge (same weight as cut grooves)
    for i in range(16):
        put(i, 0, dark)
        put(0, i, dark)
        put(i, 15, mix(lo, dark, 0.4))
        put(15, i, mix(lo, dark, 0.4))
    put(0, 0, dark)
    put(15, 15, mix(lo, dark, 0.5))
    return im


def paint_cut_face(anchors: list[tuple[int, int, int]]) -> Image.Image:
    """Cut block: vanilla 2×2 cut layout, matte — no copper specular blotches."""
    dark = anchors[0]
    deep = anchors[1]
    mid = anchors[len(anchors) // 2]
    crust = anchors[min(len(anchors) - 2, 3)]
    hi = mix(crust, anchors[-1], 0.2)
    lo = mix(deep, dark, 0.25)

    im = Image.new("RGBA", (16, 16))
    px = im.load()

    def put(x: int, y: int, c: tuple[int, int, int]) -> None:
        px[x, y] = (*c, 255)

    for ox, oy in ((0, 0), (8, 0), (0, 8), (8, 8)):
        _bevel_panel(put, ox, oy, 7, crust, hi, lo)

    # cross grooves
    for i in range(16):
        put(7, i, dark)
        put(i, 7, dark)
    for i in range(16):
        if i != 7:
            put(6, i, deep)
            put(8, i, mix(deep, mid, 0.3))
            put(i, 6, deep)
            put(i, 8, mix(deep, mid, 0.3))

    for i in range(16):
        put(i, 15, mix(lo, dark, 0.4))
        put(15, i, mix(lo, dark, 0.4))

    return im


def generate_stage(spoil: str) -> None:
    anchors = extract_palette(spoil)
    paint_block_face(anchors).save(TEX_BLOCK / f"{spoil}hardened_mooncake_block.png")
    paint_cut_face(anchors).save(TEX_BLOCK / f"{spoil}cut_hardened_mooncake.png")
    recolor(open_van(VAN_DOOR_BOTTOM), anchors, preserve_wood=False).save(
        TEX_BLOCK / f"{spoil}hardened_mooncake_door_bottom.png"
    )
    recolor(open_van(VAN_DOOR_TOP), anchors, preserve_wood=False).save(
        TEX_BLOCK / f"{spoil}hardened_mooncake_door_top.png"
    )
    recolor(open_van(VAN_DOOR_ITEM), anchors, preserve_wood=False).save(
        TEX_ITEM / f"{spoil}hardened_mooncake_door.png"
    )

    recolor(open_van("models/armor/iron_layer_1.png"), anchors, preserve_wood=False).save(
        TEX_ARMOR / f"{spoil}hardened_mooncake_layer_1.png"
    )
    recolor(open_van("models/armor/iron_layer_2.png"), anchors, preserve_wood=False).save(
        TEX_ARMOR / f"{spoil}hardened_mooncake_layer_2.png"
    )

    no_wood = {"helmet", "chestplate", "leggings", "boots", "trident"}
    for kind, (_, van_stem) in GEAR_ITEMS.items():
        recolor(
            open_van(f"item/{van_stem}.png"),
            anchors,
            preserve_wood=kind not in no_wood,
        ).save(TEX_ITEM / f"{spoil}hardened_mooncake_{kind}.png")

    shield = recolor(open_van("entity/shield_base_nopattern.png"), anchors, preserve_wood=True)
    shield_item_icon(shield).save(TEX_ITEM / f"{spoil}hardened_mooncake_shield.png")

    TEX_ENTITY.mkdir(parents=True, exist_ok=True)
    recolor(open_van("entity/projectiles/arrow.png"), anchors, preserve_wood=True).save(
        TEX_ENTITY / f"{spoil}hardened_mooncake_arrow.png"
    )

    write_door_models(spoil)
    for model_prefix in WAX_PREFIXES[spoil]:
        write_building_models(model_prefix, spoil)
        write_gear_models(model_prefix, spoil)
        write_door_blockstate(model_prefix, spoil)


def main() -> None:
    ensure_vanilla()
    TEX_BLOCK.mkdir(parents=True, exist_ok=True)
    TEX_ITEM.mkdir(parents=True, exist_ok=True)
    TEX_ARMOR.mkdir(parents=True, exist_ok=True)
    for spoil in SPOIL:
        generate_stage(spoil)
    print("recolored from vanilla:", SPOIL)


if __name__ == "__main__":
    main()
