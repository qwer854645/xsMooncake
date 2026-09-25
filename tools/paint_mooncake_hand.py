"""One-shot hand-painted 64x64 mooncake textures (not a procedural generator).
Each stroke is an explicit artist decision: palette, scallops, stamps, blank panel.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

SIZE = 64
ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/mooncake/textures"
BLOCK = ROOT / "block"
ITEM = ROOT / "item"

# Hand palette — warm Cantonese egg-wash crust
C = {
    "out": (0, 0, 0, 0),
    "edge": (92, 42, 14, 255),
    "deep": (132, 64, 22, 255),
    "mid": (186, 108, 40, 255),
    "gold": (222, 154, 62, 255),
    "bright": (244, 196, 108, 255),
    "shine": (252, 228, 168, 255),
    "groove": (108, 50, 16, 255),
    "panel": (236, 178, 88, 255),  # under blank frame only; blank itself transparent
    "fill": (110, 56, 22, 255),
    "fill2": (88, 42, 16, 255),
    "yolk": (232, 150, 42, 255),
    "yolk2": (252, 210, 120, 255),
}

# Center blank for later overlay (transparent)
BLANK = (22, 24, 42, 40)


def new_img() -> Image.Image:
    return Image.new("RGBA", (SIZE, SIZE), C["out"])


def put(img: Image.Image, x: int, y: int, rgba: tuple[int, int, int, int]) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((x, y), rgba)


def disk(img: Image.Image, cx: float, cy: float, r: float, rgba: tuple[int, int, int, int]) -> None:
    r2 = r * r
    for y in range(SIZE):
        for x in range(SIZE):
            if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r2:
                put(img, x, y, rgba)


def ring(img: Image.Image, cx: float, cy: float, r0: float, r1: float, rgba: tuple[int, int, int, int]) -> None:
    for y in range(SIZE):
        for x in range(SIZE):
            d2 = (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2
            if r0 * r0 <= d2 <= r1 * r1:
                put(img, x, y, rgba)


def paint_top() -> Image.Image:
    img = new_img()
    cx = cy = 31.5

    # Base cake body
    disk(img, cx, cy, 29.2, C["mid"])
    disk(img, cx, cy, 27.5, C["gold"])
    # Soft bake gradient (hand bands)
    disk(img, 26, 26, 14, C["bright"])
    disk(img, 24, 24, 7, C["shine"])
    disk(img, 38, 40, 12, C["mid"])
    disk(img, 42, 44, 7, C["deep"])

    # Outer scallops — 16 hand-placed lobes
    lobes = [
        (31.5, 3.5), (41.5, 6.5), (49.5, 13.5), (55.0, 22.5),
        (56.5, 31.5), (55.0, 40.5), (49.5, 49.5), (41.5, 55.5),
        (31.5, 57.5), (21.5, 55.5), (13.5, 49.5), (7.5, 40.5),
        (5.5, 31.5), (7.5, 22.5), (13.5, 13.5), (21.5, 6.5),
    ]
    for lx, ly in lobes:
        disk(img, lx, ly, 4.2, C["gold"])
        disk(img, lx - 0.8, ly - 0.8, 2.0, C["bright"])
        disk(img, lx + 1.0, ly + 1.2, 1.6, C["deep"])

    # Crisp outline
    ring(img, cx, cy, 28.4, 29.6, C["edge"])
    # Outer mold groove
    ring(img, cx, cy, 25.0, 26.6, C["groove"])
    # Inner decorative ring
    ring(img, cx, cy, 19.0, 20.4, C["groove"])

    # Hand-placed diamond stamps between rings (8 directions)
    diamonds = [
        (31, 14), (43, 19), (48, 31), (43, 43),
        (31, 48), (19, 43), (14, 31), (19, 19),
    ]
    for dx, dy in diamonds:
        for ox, oy in ((0, -1), (0, 0), (0, 1), (-1, 0), (1, 0)):
            put(img, dx + ox, dy + oy, C["groove"])
        put(img, dx, dy, C["deep"])

    # Small petal ticks
    ticks = [
        (31, 17), (40, 21), (45, 31), (40, 41),
        (31, 45), (22, 41), (17, 31), (22, 21),
    ]
    for tx, ty in ticks:
        put(img, tx, ty, C["edge"])
        put(img, tx, ty - 1, C["deep"])

    # Frame around blank panel
    x0, y0, x1, y1 = BLANK
    draw = ImageDraw.Draw(img)
    draw.rectangle([x0 - 2, y0 - 2, x1 + 1, y1 + 1], outline=C["edge"], width=2)
    draw.rectangle([x0 - 1, y0 - 1, x1, y1], outline=C["groove"], width=1)
    # Clear blank interior to transparent
    for y in range(y0, y1):
        for x in range(x0, x1):
            put(img, x, y, C["out"])

    # Corner shine accents on frame
    put(img, x0 - 1, y0 - 1, C["shine"])
    put(img, x1, y0 - 1, C["bright"])
    return img


def paint_side() -> Image.Image:
    img = new_img()
    # Full usable side atlas: vertical ridges across entire tile
    for x in range(SIZE):
        col = x % 6
        for y in range(SIZE):
            if col == 0:
                c = C["edge"]
            elif col in (1, 5):
                c = C["deep"]
            elif col in (2, 4):
                c = C["mid"]
            else:
                c = C["gold"]
            # Top egg-wash lip
            if y <= 4:
                c = C["bright"] if col not in (0, 1) else C["deep"]
            elif y <= 8:
                c = C["gold"] if col != 0 else C["groove"]
            # Bottom bake
            if y >= SIZE - 6:
                c = C["edge"] if col <= 1 else C["deep"]
            elif y >= SIZE - 12:
                c = C["deep"] if col in (0, 1, 5) else C["mid"]
            # Speck highlight mid band
            if 14 <= y <= 16 and col == 3:
                c = C["shine"]
            put(img, x, y, c)
    return img


def paint_bottom() -> Image.Image:
    img = new_img()
    cx = cy = 31.5
    disk(img, cx, cy, 29.0, C["deep"])
    disk(img, cx, cy, 26.5, C["mid"])
    ring(img, cx, cy, 27.5, 29.0, C["edge"])
    # Faint press marks
    for x, y in ((20, 22), (40, 24), (24, 40), (42, 42), (31, 31), (18, 34), (44, 34)):
        put(img, x, y, C["deep"])
        put(img, x + 1, y, C["groove"])
    return img


def paint_inner() -> Image.Image:
    img = new_img()
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, 63, 63], fill=C["mid"])
    draw.rectangle([0, 0, 63, 63], outline=C["edge"], width=3)
    draw.rectangle([3, 3, 60, 60], outline=C["deep"], width=2)
    draw.rectangle([6, 6, 57, 57], fill=C["fill"])
    # Lotus paste noise — hand speckles
    for x, y in (
        (12, 14), (18, 20), (28, 16), (40, 18), (48, 22),
        (14, 30), (22, 34), (36, 32), (46, 36), (50, 28),
        (16, 44), (26, 48), (38, 46), (48, 50), (30, 40),
        (20, 26), (42, 44), (34, 22),
    ):
        put(img, x, y, C["fill2"])
    # Salted egg yolk
    disk(img, 32, 34, 11, C["yolk"])
    disk(img, 30, 32, 5, C["yolk2"])
    disk(img, 29, 31, 2, C["shine"])
    return img


def gray(img: Image.Image, amount: float) -> Image.Image:
    out = img.copy()
    pix = out.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = pix[x, y]
            if a == 0:
                continue
            gy = int(0.299 * r + 0.587 * g + 0.114 * b)
            pix[x, y] = (
                int(r * (1 - amount) + gy * amount),
                int(g * (1 - amount) + gy * amount),
                int(b * (1 - amount) + gy * amount),
                a,
            )
    return out


def pale_raw(img: Image.Image) -> Image.Image:
    out = img.copy()
    pix = out.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = pix[x, y]
            if a == 0:
                continue
            pix[x, y] = (
                min(255, int(r * 0.55 + 115)),
                min(255, int(g * 0.55 + 125)),
                min(255, int(b * 0.55 + 135)),
                a,
            )
    return out


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    print("wrote", path)


def main() -> None:
    top = paint_top()
    side = paint_side()
    bottom = paint_bottom()
    inner = paint_inner()

    stages = {
        "": 0.0,
        "exposed_": 0.28,
        "weathered_": 0.52,
        "oxidized_": 0.78,
    }
    for pref, amt in stages.items():
        t = top if amt == 0 else gray(top, amt)
        s = side if amt == 0 else gray(side, amt)
        b = bottom if amt == 0 else gray(bottom, amt)
        i = inner if amt == 0 else gray(inner, amt)
        name = f"{pref}mooncake"
        save(t, BLOCK / f"{name}_top.png")
        save(s, BLOCK / f"{name}_side.png")
        save(b, BLOCK / f"{name}_bottom.png")
        save(i, BLOCK / f"{name}_inner.png")
        save(t, ITEM / ("mooncake.png" if pref == "" else f"{pref}mooncake.png"))

    save(pale_raw(top), ITEM / "raw_mooncake.png")


if __name__ == "__main__":
    main()
