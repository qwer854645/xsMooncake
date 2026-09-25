"""Generate 64x64 Cantonese mooncake textures.

Draw the fresh (unaffected) stage first; later stages are progressive grayscale mixes.
Top face keeps a centered rectangular blank panel for future overlays.
"""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image

SIZE = 64
# Reserved stamp panel on top (inclusive start, exclusive end).
BLANK = (22, 24, 42, 40)  # 20x16

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/mooncake/textures"
BLOCK = ROOT / "block"
ITEM = ROOT / "item"
BLOCK.mkdir(parents=True, exist_ok=True)
ITEM.mkdir(parents=True, exist_ok=True)

# Fresh Cantonese crust palette
SHADOW = (118, 58, 20)
MID = (178, 100, 34)
LIGHT = (218, 150, 56)
HI = (242, 198, 110)
MOLD_D = (88, 40, 12)
MOLD_L = (250, 220, 150)
FILLING = (98, 48, 16)
YOLK = (224, 144, 38)

# Grayscale blend amounts per stage after unaffected
GRAY_AMOUNTS = {
    "": 0.0,
    "exposed_": 0.28,
    "weathered_": 0.52,
    "oxidized_": 0.78,
}


def px(img: Image.Image, x: int, y: int, color: tuple[int, int, int], alpha: int = 255) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((x, y), (*color, alpha))


def in_blank(x: int, y: int) -> bool:
    x0, y0, x1, y1 = BLANK
    return x0 <= x < x1 and y0 <= y < y1


def on_blank_border(x: int, y: int) -> bool:
    x0, y0, x1, y1 = BLANK
    near = (x0 - 1 <= x < x1 + 1) and (y0 - 1 <= y < y1 + 1)
    return near and not in_blank(x, y)


def dist(x: int, y: int, cx: float, cy: float) -> float:
    return math.hypot(x + 0.5 - cx, y + 0.5 - cy)


def to_gray(color: tuple[int, int, int]) -> tuple[int, int, int]:
    g = int(0.299 * color[0] + 0.587 * color[1] + 0.114 * color[2])
    return (g, g, g)


def mix_gray(color: tuple[int, int, int], amount: float) -> tuple[int, int, int]:
    if amount <= 0:
        return color
    g = to_gray(color)
    return tuple(int(c * (1 - amount) + g[i] * amount) for i, c in enumerate(color))  # type: ignore


def apply_gray(img: Image.Image, amount: float) -> Image.Image:
    if amount <= 0:
        return img
    out = img.copy()
    pix = out.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = pix[x, y]
            if a == 0:
                continue
            nr, ng, nb = mix_gray((r, g, b), amount)
            pix[x, y] = (nr, ng, nb, a)
    return out


def make_top_fresh() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx = cy = (SIZE - 1) / 2.0
    radius = SIZE * 0.46

    for y in range(SIZE):
        for x in range(SIZE):
            d = dist(x, y, cx, cy)
            if d > radius:
                continue
            if in_blank(x, y):
                continue
            if on_blank_border(x, y):
                px(img, x, y, MOLD_D)
                continue

            t = d / radius
            if t > 0.93:
                c = SHADOW
            elif t > 0.84:
                c = MID
            elif (x * 2 + y * 3) % 11 == 0:
                c = MID
            else:
                c = LIGHT

            ang = math.atan2(y - cy, x - cx)
            flute = abs(math.sin(ang * 10))
            if d > radius * 0.86 and flute > 0.68:
                c = MOLD_D

            if radius * 0.80 <= d <= radius * 0.89:
                c = MOLD_D if (x + y) % 2 == 0 else SHADOW

            in_cross = abs(x - cx) <= 2.2 or abs(y - cy) <= 2.2
            in_diag = abs(abs(x - cx) - abs(y - cy)) <= 2.0
            if radius * 0.40 <= d <= radius * 0.76:
                if in_cross or in_diag:
                    c = MOLD_D
                elif x < cx and y < cy:
                    c = HI
                else:
                    c = LIGHT

            if radius * 0.32 <= d <= radius * 0.38:
                c = MOLD_D

            if radius * 0.45 < d < radius * 0.58 and x <= cx - 2 and y <= cy - 2 and not in_cross:
                c = HI

            px(img, x, y, c)

    x0, y0, x1, y1 = BLANK
    for x, y in ((x0 - 1, y0 - 1), (x1, y0 - 1), (x0 - 1, y1), (x1, y1)):
        if dist(x, y, cx, cy) <= radius:
            px(img, x, y, MOLD_L)
    return img


def make_side_fresh() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for y in range(SIZE):
        for x in range(SIZE):
            ridge = (x % 5) == 0
            if y <= 3:
                c = MOLD_D
            elif y >= SIZE - 4:
                c = SHADOW
            elif y <= 10:
                c = MID if ridge else LIGHT
            elif y >= SIZE - 14:
                c = SHADOW if ridge else MID
            else:
                c = MID if ridge else LIGHT
            if y == 12 and not ridge:
                c = HI
            px(img, x, y, c)
    return img


def make_bottom_fresh() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx = cy = (SIZE - 1) / 2.0
    radius = SIZE * 0.46
    for y in range(SIZE):
        for x in range(SIZE):
            d = dist(x, y, cx, cy)
            if d > radius:
                continue
            if d > radius * 0.90:
                c = SHADOW
            elif (x * 3 + y * 5) % 13 == 0:
                c = SHADOW
            else:
                c = MID
            if radius * 0.80 <= d <= radius * 0.86:
                c = MOLD_D
            px(img, x, y, c)
    return img


def make_inner_fresh() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    fill_dark = tuple(max(0, v - 24) for v in FILLING)
    for y in range(SIZE):
        for x in range(SIZE):
            edge = x <= 3 or y <= 3 or x >= SIZE - 4 or y >= SIZE - 4
            crust = x <= 7 or y <= 7 or x >= SIZE - 8 or y >= SIZE - 8
            if edge:
                c = SHADOW if (x + y) % 2 == 0 else MOLD_D
            elif crust:
                c = MID if (x % 2) == 0 else LIGHT
            else:
                c = FILLING if ((x + y) % 3) else fill_dark
                dx, dy = x - 31.5, y - 33.0
                r2 = dx * dx + dy * dy
                if r2 < 120.0:
                    c = YOLK
                if r2 < 48.0:
                    c = HI
                if r2 < 16.0:
                    c = (255, 232, 165)
            px(img, x, y, c)  # type: ignore
    return img


def make_dough(incomplete: bool = False) -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    base = (220, 190, 140) if not incomplete else (200, 175, 130)
    dark = (180, 140, 90) if not incomplete else (160, 125, 80)
    light = (240, 220, 175)
    cx, cy = 31.5, 34.0
    for y in range(SIZE):
        for x in range(SIZE):
            dx = (x + 0.5 - cx) / 24.8
            dy = (y + 0.5 - cy) / 18.4
            if dx * dx + dy * dy > 1.0:
                continue
            c = base
            if dx * dx + dy * dy > 0.72:
                c = dark
            if x <= 22 and y <= 28 and dx * dx + dy * dy < 0.45:
                c = light
            if incomplete and (x + y) % 5 == 0 and dx * dx + dy * dy < 0.6:
                c = (170, 130, 85)
            px(img, x, y, c)
    return img


def make_raw_top() -> Image.Image:
    """Pale unbaked version of the fresh top."""
    img = make_top_fresh()
    pix = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = pix[x, y]
            if a == 0:
                continue
            pix[x, y] = (
                min(255, int(r * 0.55 + 110)),
                min(255, int(g * 0.55 + 120)),
                min(255, int(b * 0.55 + 130)),
                a,
            )
    return img


def save(img: Image.Image, path: Path) -> None:
    img.save(path)
    print("wrote", path.relative_to(ROOT.parent.parent.parent.parent), f"({img.size[0]}x{img.size[1]})")


def main() -> None:
    tops = {"": make_top_fresh()}
    sides = {"": make_side_fresh()}
    bottoms = {"": make_bottom_fresh()}
    inners = {"": make_inner_fresh()}

    for prefix, amount in GRAY_AMOUNTS.items():
        if prefix == "":
            continue
        tops[prefix] = apply_gray(tops[""], amount)
        sides[prefix] = apply_gray(sides[""], amount)
        bottoms[prefix] = apply_gray(bottoms[""], amount)
        inners[prefix] = apply_gray(inners[""], amount)

    for prefix in GRAY_AMOUNTS:
        name = f"{prefix}mooncake"
        save(tops[prefix], BLOCK / f"{name}_top.png")
        save(sides[prefix], BLOCK / f"{name}_side.png")
        save(bottoms[prefix], BLOCK / f"{name}_bottom.png")
        save(inners[prefix], BLOCK / f"{name}_inner.png")
        save(tops[prefix], ITEM / f"{prefix}mooncake.png" if prefix else ITEM / "mooncake.png")

    save(make_raw_top(), ITEM / "raw_mooncake.png")
    save(make_dough(False), ITEM / "mooncake_dough.png")
    save(make_dough(True), ITEM / "incomplete_mooncake_dough.png")


if __name__ == "__main__":
    main()
