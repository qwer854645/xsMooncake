"""High-precision voxel mooncake: non-overlapping cells (no z-fight flicker)."""
from __future__ import annotations

import json
import math
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
BLOCK_MODELS = ROOT / "src/main/resources/assets/mooncake/models/block"
TEX = ROOT / "src/main/resources/assets/mooncake/textures/block"
SHAPES_JAVA = ROOT / "src/main/java/com/mooncake/block/MooncakeShapes.java"

# --- layout ---
RES = 40
DIAMETER = 10.0
HEIGHT = 4.0
RIM_EXTRA = 0.4  # outer crust + rectangle frame
CX_U = 8.0
CZ_U = 8.0
CELL = DIAMETER / RES
ORIGIN = CX_U - DIAMETER / 2
UV_SCALE = 16.0 / RES

HILL_N = 16
BASE_R_PX = RES * 0.42
HILL_AMP_PX = RES * 0.045
RIM_WIDTH_PX = 2.0

LIGHT = (176, 128, 72, 255)
LIGHT2 = (158, 110, 58, 255)
DEEP = (78, 42, 20, 255)
DEEP2 = (92, 52, 26, 255)
EDGE2 = (140, 88, 48, 255)

# Wider-than-tall rectangle panel
FRAME_MX = int(RES * 0.24)  # left/right inset → wider
FRAME_MY = int(RES * 0.36)  # top/bottom inset → shorter
FRAME_THICK = 2


def rim_radius(ang: float) -> float:
    flute = 0.5 + 0.5 * math.cos(ang * HILL_N)
    return BASE_R_PX + HILL_AMP_PX * (0.25 + 0.75 * flute)


def solid_mask() -> list[list[bool]]:
    cx = cy = (RES - 1) / 2.0
    mask = [[False] * RES for _ in range(RES)]
    for j in range(RES):
        for i in range(RES):
            dx = i - cx
            dy = j - cy
            r = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            if r <= rim_radius(ang):
                mask[j][i] = True
    return mask


def is_crust_rim(i: int, j: int, mask: list[list[bool]]) -> bool:
    if not mask[j][i]:
        return False
    cx = cy = (RES - 1) / 2.0
    r = math.hypot(i - cx, j - cy)
    ang = math.atan2(j - cy, i - cx)
    if rim_radius(ang) - r <= RIM_WIDTH_PX:
        return True
    for dj, di in ((-1, 0), (1, 0), (0, -1), (0, 1)):
        jj, ii = j + dj, i + di
        if jj < 0 or jj >= RES or ii < 0 or ii >= RES or not mask[jj][ii]:
            return True
    return False


def frame_rect() -> tuple[int, int, int, int]:
    return FRAME_MX, FRAME_MY, RES - 1 - FRAME_MX, RES - 1 - FRAME_MY


def _on_h_band(j: int) -> bool:
    x0, y0, x1, y1 = frame_rect()
    for dy in range(FRAME_THICK):
        if j == y0 + dy or j == y1 - dy:
            return True
    return False


def _on_v_band(i: int) -> bool:
    x0, y0, x1, y1 = frame_rect()
    for dx in range(FRAME_THICK):
        if i == x0 + dx or i == x1 - dx:
            return True
    return False


def is_frame_rect(i: int, j: int, mask: list[list[bool]]) -> bool:
    """Only the rectangle border itself (not arms past the corners)."""
    if not mask[j][i] or is_crust_rim(i, j, mask):
        return False
    x0, y0, x1, y1 = frame_rect()
    if not (x0 <= i <= x1 and y0 <= j <= y1):
        return False
    return _on_h_band(j) or _on_v_band(i)


def cell_height(i: int, j: int, mask: list[list[bool]]) -> float:
    if is_crust_rim(i, j, mask) or is_frame_rect(i, j, mask):
        return HEIGHT + RIM_EXTRA
    return HEIGHT


def cell_bounds(i: int, j: int) -> tuple[float, float, float, float]:
    x0 = ORIGIN + i * CELL
    z0 = ORIGIN + j * CELL
    return x0, z0, x0 + CELL, z0 + CELL


def _cell_element(
    i: int,
    j: int,
    mask: list[list[bool]],
    *,
    in_piece: bool,
    half: int,
) -> dict | None:
    """One voxel cell; piece mode keeps the NW quarter (i,j < half) with cut faces."""
    if not mask[j][i]:
        return None
    if in_piece and (i >= half or j >= half):
        return None

    def occupied(ii: int, jj: int) -> bool:
        if ii < 0 or jj < 0 or ii >= RES or jj >= RES or not mask[jj][ii]:
            return False
        if in_piece and (ii >= half or jj >= half):
            return False
        return True

    x0, z0, x1, z1 = cell_bounds(i, j)
    y1 = cell_height(i, j, mask)
    u0, v0 = i * UV_SCALE, j * UV_SCALE
    u1, v1 = (i + 1) * UV_SCALE, (j + 1) * UV_SCALE
    faces: dict = {
        "up": {
            "uv": [round(u0, 4), round(v0, 4), round(u1, 4), round(v1, 4)],
            "texture": "#top",
        },
        "down": {
            "uv": [round(u0, 4), round(v0, 4), round(u1, 4), round(v1, 4)],
            "texture": "#bottom",
            "cullface": "down",
        },
    }
    su0, su1 = u0, u1
    sv0, sv1 = 5.0, 11.0

    def side_uv(tex: str = "#side") -> dict:
        return {
            "uv": [round(su0, 4), sv0, round(su1, 4), sv1],
            "texture": tex,
        }

    if not occupied(i, j - 1):
        faces["north"] = side_uv()
    if not occupied(i, j + 1):
        # Cut face toward cake center uses bottom (filling) look
        faces["south"] = side_uv("#bottom" if in_piece and j + 1 == half else "#side")
    if not occupied(i - 1, j):
        faces["west"] = side_uv()
    if not occupied(i + 1, j):
        faces["east"] = side_uv("#bottom" if in_piece and i + 1 == half else "#side")

    for dj, di, face in (
        (-1, 0, "north"),
        (1, 0, "south"),
        (0, -1, "west"),
        (0, 1, "east"),
    ):
        ii, jj = i + di, j + dj
        if not occupied(ii, jj):
            continue
        if y1 > cell_height(ii, jj, mask) + 0.01:
            faces[face] = side_uv()

    return {
        "from": [round(x0, 4), 0.0, round(z0, 4)],
        "to": [round(x1, 4), round(y1, 4), round(z1, 4)],
        "faces": faces,
    }


def _shape_model(elements: list[dict]) -> dict:
    return {
        "ambientocclusion": False,
        "textures": {
            "particle": "#side",
            "top": "mooncake:block/mooncake_top",
            "side": "mooncake:block/mooncake_side",
            "bottom": "mooncake:block/mooncake_bottom",
        },
        # Flat disc: tilt camera down in GUI so the top face reads, not only the rim.
        "display": {
            "gui": {
                "rotation": [60, 45, 0],
                "translation": [0, 1.5, 0],
                "scale": [0.9, 0.9, 0.9],
            },
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 2, 0],
                "scale": [0.4, 0.4, 0.4],
            },
            "fixed": {
                "rotation": [0, 0, 0],
                "translation": [0, 0, 0],
                "scale": [0.5, 0.5, 0.5],
            },
            "thirdperson_righthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0.5],
                "scale": [0.4, 0.4, 0.4],
            },
            "thirdperson_lefthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0.5],
                "scale": [0.4, 0.4, 0.4],
            },
            "firstperson_righthand": {
                "rotation": [0, 135, 0],
                "translation": [0, 2, 0],
                "scale": [0.45, 0.45, 0.45],
            },
            "firstperson_lefthand": {
                "rotation": [0, 135, 0],
                "translation": [0, 2, 0],
                "scale": [0.45, 0.45, 0.45],
            },
        },
        "elements": elements,
    }


def build_model(mask: list[list[bool]]) -> dict:
    elements: list[dict] = []
    for j in range(RES):
        for i in range(RES):
            el = _cell_element(i, j, mask, in_piece=False, half=RES // 2)
            if el is not None:
                elements.append(el)
    return _shape_model(elements)


def build_piece_model(mask: list[list[bool]]) -> dict:
    """Cross-cut quarter of the whole cake (matches MooncakeShapes.PIECE_SHAPE)."""
    half = RES // 2
    elements: list[dict] = []
    for j in range(half):
        for i in range(half):
            el = _cell_element(i, j, mask, in_piece=True, half=half)
            if el is not None:
                elements.append(el)
    return _shape_model(elements)


def paint_top(mask: list[list[bool]]) -> Image.Image:
    img = Image.new("RGBA", (RES, RES), LIGHT)
    px = img.load()
    for j in range(RES):
        for i in range(RES):
            if not mask[j][i]:
                px[i, j] = LIGHT
                continue
            if is_crust_rim(i, j, mask) or is_frame_rect(i, j, mask):
                px[i, j] = DEEP  # pure deep brown
            else:
                px[i, j] = LIGHT
    return img


def paint_bottom(mask: list[list[bool]]) -> Image.Image:
    img = Image.new("RGBA", (RES, RES), DEEP)
    px = img.load()
    cx = cy = (RES - 1) / 2.0
    for j in range(RES):
        for i in range(RES):
            if not mask[j][i]:
                continue
            dx, dy = i - cx, j - cy
            r = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            rim = rim_radius(ang)
            dist_in = rim - r
            if dist_in < 1.2:
                t = dist_in / 1.2
                c = tuple(int(LIGHT[k] * (1 - t) + EDGE2[k] * t) for k in range(3)) + (255,)
            elif dist_in < 2.4:
                t = (dist_in - 1.2) / 1.2
                c = tuple(int(EDGE2[k] * (1 - t) + DEEP[k] * t) for k in range(3)) + (255,)
            else:
                c = DEEP2 if ((i + j) % 7 == 0) else DEEP
            px[i, j] = c
    return img


def paint_side() -> Image.Image:
    img = Image.new("RGBA", (RES, RES), LIGHT)
    px = img.load()
    for y in range(RES):
        for x in range(RES):
            wave = 0.5 + 0.5 * math.cos((x + 0.5) / RES * math.pi * HILL_N)
            if wave > 0.78:
                c = list(LIGHT)
            elif wave > 0.45:
                t = (wave - 0.45) / 0.33
                c = [int(LIGHT2[k] * (1 - t) + LIGHT[k] * t) for k in range(3)]
            else:
                c = list(LIGHT2)
                if wave < 0.18:
                    c = [max(0, c[k] - 18) for k in range(3)]
            band = abs(((y + 0.5) / RES) - 0.5)
            if band > 0.38:
                c = [max(0, c[k] - 22) for k in range(3)]
            elif band > 0.28:
                c = [max(0, c[k] - 10) for k in range(3)]
            if ((x * 17 + y * 13) % 11) == 0:
                c = [max(0, c[k] - 12) for k in range(3)]
            elif ((x * 9 + y * 5) % 15) == 0:
                c = [min(255, c[k] + 10) for k in range(3)]
            px[x, y] = (c[0], c[1], c[2], 255)
    return img


def write_shapes_java() -> None:
    r = DIAMETER / 2
    h = HEIGHT + RIM_EXTRA
    boxes = [
        (CX_U - r * 0.6, 0.0, CX_U - r, CX_U + r * 0.6, h, CX_U + r),
        (CX_U - r, 0.0, CX_U - r * 0.6, CX_U + r, h, CX_U + r * 0.6),
        (CX_U - r * 0.85, 0.0, CX_U - r * 0.85, CX_U + r * 0.85, h, CX_U + r * 0.85),
    ]
    lines = [
        "package com.mooncake.block;",
        "",
        "import net.minecraft.core.Direction;",
        "import net.minecraft.world.level.block.Block;",
        "import net.minecraft.world.phys.shapes.Shapes;",
        "import net.minecraft.world.phys.shapes.VoxelShape;",
        "",
        "/**",
        f" * Mooncake collision (~{DIAMETER:.0f} disc, {h:.1f} tall with crust rim).",
        " * Piece shapes match {@code mooncake_piece_shape} + blockstate Y rotation.",
        " */",
        "public final class MooncakeShapes {",
        "    public static final VoxelShape SHAPE = Shapes.or(",
    ]
    for i, (x0, y0, z0, x1, y1, z1) in enumerate(boxes):
        comma = "," if i < len(boxes) - 1 else ""
        lines.append(
            f"            Block.box({x0}D, {y0}D, {z0}D, {x1}D, {y1}D, {z1}D){comma}"
        )
    lines += [
        "    );",
        "",
        "    /** facing=east (model y=0): NW quarter. */",
        "    private static final VoxelShape PIECE_EAST = Shapes.or(",
        f"            Block.box({CX_U - r}D, 0.0D, {CX_U - r}D, {CX_U}D, {h}D, {CX_U}D),",
        f"            Block.box({CX_U - r + 0.5}D, 0.0D, {CX_U - r}D, {CX_U}D, {h}D, {CX_U + 0.5}D),",
        f"            Block.box({CX_U - r}D, 0.0D, {CX_U - r + 0.5}D, {CX_U + 0.5}D, {h}D, {CX_U}D)",
        "    );",
        "",
        "    private static final VoxelShape PIECE_SOUTH = rotateY90(PIECE_EAST);",
        "    private static final VoxelShape PIECE_WEST = rotateY90(PIECE_SOUTH);",
        "    private static final VoxelShape PIECE_NORTH = rotateY90(PIECE_WEST);",
        "",
        "    private MooncakeShapes() {",
        "    }",
        "",
        "    public static VoxelShape pieceShape(Direction facing) {",
        "        return switch (facing) {",
        "            case SOUTH -> PIECE_SOUTH;",
        "            case WEST -> PIECE_WEST;",
        "            case NORTH -> PIECE_NORTH;",
        "            default -> PIECE_EAST;",
        "        };",
        "    }",
        "",
        "    /**",
        "     * Match blockstate model {@code \"y\": 90}: clockwise when viewed from above (N up),",
        "     * so NW quarter → NE.",
        "     */",
        "    private static VoxelShape rotateY90(VoxelShape shape) {",
        "        VoxelShape[] result = {Shapes.empty()};",
        "        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {",
        "            result[0] = Shapes.or(",
        "                    result[0],",
        "                    Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)",
        "            );",
        "        });",
        "        return result[0];",
        "    }",
        "}",
        "",
    ]
    SHAPES_JAVA.write_text("\n".join(lines), encoding="utf-8")


def darken(img: Image.Image, brightness: float, cool: float = 0.0) -> Image.Image:
    """Scale RGB by brightness; optional cool bias (pull R down, B up) for spoil look."""
    out = img.copy()
    px = out.load()
    w, h = out.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            r = int(r * brightness * (1.0 - cool * 0.15))
            g = int(g * brightness * (1.0 - cool * 0.05))
            b = int(min(255, b * brightness * (1.0 + cool * 0.08)))
            px[x, y] = (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), a)
    return out


def to_grayer(img: Image.Image, amount: float = 0.55) -> Image.Image:
    """Blend toward luminance so hardened cakes look more gray than soft ones."""
    out = img.copy()
    px = out.load()
    w, h = out.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            # Rec. 601 luminance
            yv = int(0.299 * r + 0.587 * g + 0.114 * b)
            r = int(r * (1.0 - amount) + yv * amount)
            g = int(g * (1.0 - amount) + yv * amount)
            b = int(b * (1.0 - amount) + yv * amount)
            px[x, y] = (r, g, b, a)
    return out


# Spoilage stages: brightness vs fresh mooncake (unique + progressively darker)
SPOIL_STAGES = [
    ("", 1.00, 0.00),
    ("exposed_", 0.82, 0.25),
    ("weathered_", 0.64, 0.50),
    ("oxidized_", 0.48, 0.75),
]

# Hardened = same hue as soft spoil twin, uniformly darker (no desaturation)
HARDENED_BRIGHT = 0.72


def write_stage_textures(mask: list[list[bool]]) -> None:
    top0 = paint_top(mask)
    bot0 = paint_bottom(mask)
    side0 = paint_side()
    for prefix, bright, cool in SPOIL_STAGES:
        top = darken(top0, bright, cool)
        bot = darken(bot0, bright, cool)
        side = darken(side0, bright, cool)
        top.save(TEX / f"{prefix}mooncake_top.png")
        bot.save(TEX / f"{prefix}mooncake_bottom.png")
        side.save(TEX / f"{prefix}mooncake_side.png")
        darken(top, HARDENED_BRIGHT).save(TEX / f"{prefix}hardened_mooncake_top.png")
        darken(bot, HARDENED_BRIGHT).save(TEX / f"{prefix}hardened_mooncake_bottom.png")
        darken(side, HARDENED_BRIGHT).save(TEX / f"{prefix}hardened_mooncake_side.png")


def write_block_models() -> None:
    """Point each cake/piece variant at soft or hardened stage textures."""
    ITEM_MODELS = ROOT / "src/main/resources/assets/mooncake/models/item"
    ITEM_MODELS.mkdir(parents=True, exist_ok=True)
    names: list[tuple[str, str, bool]] = []  # name, spoil prefix, hardened?
    for prefix, _, _ in SPOIL_STAGES:
        for hard in (False, True):
            hard_s = "hardened_" if hard else ""
            if prefix:
                unwaxed = f"{prefix}{hard_s}mooncake"
                waxed = f"waxed_{prefix}{hard_s}mooncake"
            else:
                unwaxed = f"{hard_s}mooncake" if hard else "mooncake"
                waxed = f"waxed_{hard_s}mooncake" if hard else "waxed_mooncake"
            for base in (unwaxed, waxed):
                names.append((base, prefix, hard))
                names.append((f"{base}_piece", prefix, hard))

    for name, prefix, hard in names:
        stem = f"{prefix}hardened_mooncake" if hard else f"{prefix}mooncake"
        is_piece = name.endswith("_piece")
        parent = "mooncake:block/mooncake_piece_shape" if is_piece else "mooncake:block/mooncake_shape"
        tex = {
            "parent": parent,
            "textures": {
                "top": f"mooncake:block/{stem}_top",
                "side": f"mooncake:block/{stem}_side",
                "bottom": f"mooncake:block/{stem}_bottom",
                "particle": f"mooncake:block/{stem}_side",
            },
        }
        (BLOCK_MODELS / f"{name}.json").write_text(
            json.dumps(tex, indent=2) + "\n", encoding="utf-8"
        )
        (ITEM_MODELS / f"{name}.json").write_text(
            json.dumps({"parent": f"mooncake:block/{name}"}, indent=2) + "\n",
            encoding="utf-8",
        )


def write_shape_models(mask: list[list[bool]]) -> tuple[int, int]:
    model = build_model(mask)
    piece = build_piece_model(mask)
    (BLOCK_MODELS / "mooncake_shape.json").write_text(
        json.dumps(model, separators=(",", ":")) + "\n", encoding="utf-8"
    )
    (BLOCK_MODELS / "mooncake_piece_shape.json").write_text(
        json.dumps(piece, separators=(",", ":")) + "\n", encoding="utf-8"
    )
    return len(model["elements"]), len(piece["elements"])


def main() -> None:
    BLOCK_MODELS.mkdir(parents=True, exist_ok=True)
    TEX.mkdir(parents=True, exist_ok=True)

    mask = solid_mask()
    n, npiece = write_shape_models(mask)

    write_stage_textures(mask)
    write_block_models()
    write_shapes_java()
    print(
        f"res={RES} cells={n} piece_cells={npiece} "
        f"stages={[p or 'fresh' for p, _, _ in SPOIL_STAGES]}"
    )


if __name__ == "__main__":
    main()
