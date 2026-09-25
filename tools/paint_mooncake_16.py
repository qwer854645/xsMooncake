"""Hand-painted 16x16 mooncake textures."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

SIZE = 16
ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/mooncake/textures"
BLOCK = ROOT / "block"
ITEM = ROOT / "item"

# Center blank (transparent) for later overlay — 6x4
BLANK = (5, 6, 11, 10)

P = {
    "air": (0, 0, 0, 0),
    "edge": (86, 38, 12, 255),
    "deep": (128, 60, 20, 255),
    "mid": (184, 104, 38, 255),
    "gold": (220, 150, 58, 255),
    "lit": (242, 192, 100, 255),
    "shine": (252, 224, 160, 255),
    "groove": (104, 46, 14, 255),
    "fill": (108, 52, 20, 255),
    "fill2": (84, 40, 14, 255),
    "yolk": (228, 144, 40, 255),
    "yolk2": (248, 204, 110, 255),
}


def put(img: Image.Image, x: int, y: int, c: tuple[int, int, int, int]) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((x, y), c)


def paint_top() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), P["air"])

    # Round silhouette mask (hand-tuned)
    body = {
        (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1), (11, 1), (12, 1),
        (2, 2), (3, 2), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2), (13, 2),
        (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3), (11, 3), (12, 3), (13, 3), (14, 3),
        (1, 4), (2, 4), (3, 4), (4, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4), (12, 4), (13, 4), (14, 4),
        (1, 5), (2, 5), (3, 5), (4, 5), (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 5), (12, 5), (13, 5), (14, 5),
        (1, 6), (2, 6), (3, 6), (4, 6), (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (11, 6), (12, 6), (13, 6), (14, 6),
        (1, 7), (2, 7), (3, 7), (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (11, 7), (12, 7), (13, 7), (14, 7),
        (1, 8), (2, 8), (3, 8), (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8), (12, 8), (13, 8), (14, 8),
        (1, 9), (2, 9), (3, 9), (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9), (11, 9), (12, 9), (13, 9), (14, 9),
        (1, 10), (2, 10), (3, 10), (4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10), (11, 10), (12, 10), (13, 10), (14, 10),
        (1, 11), (2, 11), (3, 11), (4, 11), (5, 11), (6, 11), (7, 11), (8, 11), (9, 11), (10, 11), (11, 11), (12, 11), (13, 11), (14, 11),
        (1, 12), (2, 12), (3, 12), (4, 12), (5, 12), (6, 12), (7, 12), (8, 12), (9, 12), (10, 12), (11, 12), (12, 12), (13, 12), (14, 12),
        (2, 13), (3, 13), (4, 13), (5, 13), (6, 13), (7, 13), (8, 13), (9, 13), (10, 13), (11, 13), (12, 13), (13, 13),
        (3, 14), (4, 14), (5, 14), (6, 14), (7, 14), (8, 14), (9, 14), (10, 14), (11, 14), (12, 14),
    }
    for x, y in body:
        # Default gold crust
        c = P["gold"]
        # Top-left shine
        if x + y <= 8:
            c = P["lit"]
        if x + y <= 5:
            c = P["shine"]
        # Bottom-right bake
        if x + y >= 20:
            c = P["mid"]
        if x + y >= 23:
            c = P["deep"]
        put(img, x, y, c)

    # Scallop edge darkening
    edge_pts = [
        (3, 1), (7, 1), (11, 1),
        (1, 3), (14, 3), (1, 7), (14, 7), (1, 11), (14, 11),
        (3, 14), (7, 14), (11, 14),
        (2, 2), (13, 2), (2, 13), (13, 13),
    ]
    for x, y in edge_pts:
        put(img, x, y, P["edge"])

    # Outer mold ring
    for x, y in (
        (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 2),
        (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11),
        (13, 4), (13, 5), (13, 6), (13, 7), (13, 8), (13, 9), (13, 10), (13, 11),
        (4, 13), (5, 13), (6, 13), (7, 13), (8, 13), (9, 13), (10, 13), (11, 13),
    ):
        put(img, x, y, P["groove"])

    # Inner ring around blank
    for x, y in (
        (4, 5), (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 5),
        (4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10), (11, 10),
        (4, 6), (4, 7), (4, 8), (4, 9),
        (11, 6), (11, 7), (11, 8), (11, 9),
    ):
        put(img, x, y, P["groove"])

    # Diamond stamps (8)
    for x, y in ((7, 3), (12, 7), (7, 12), (3, 7), (10, 4), (10, 11), (5, 4), (5, 11)):
        put(img, x, y, P["edge"])
        put(img, x, y - 1 if y > 0 else y, P["deep"])

    # Blank panel + dark frame
    x0, y0, x1, y1 = BLANK
    for y in range(y0 - 1, y1 + 1):
        for x in range(x0 - 1, x1 + 1):
            if not (0 <= x < SIZE and 0 <= y < SIZE):
                continue
            if x0 <= x < x1 and y0 <= y < y1:
                put(img, x, y, P["air"])
            elif img.getpixel((x, y))[3] != 0:
                put(img, x, y, P["edge"])
    put(img, x0 - 1, y0 - 1, P["shine"])
    put(img, x1, y0 - 1, P["lit"])
    return img


def paint_side() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), P["air"])
    for x in range(SIZE):
        col = x % 4
        for y in range(SIZE):
            if col == 0:
                c = P["edge"]
            elif col == 1:
                c = P["deep"]
            elif col == 2:
                c = P["gold"]
            else:
                c = P["mid"]
            if y <= 1:
                c = P["lit"] if col >= 2 else P["deep"]
            elif y <= 3:
                c = P["gold"] if col != 0 else P["groove"]
            if y >= 13:
                c = P["edge"]
            elif y >= 11:
                c = P["deep"] if col <= 1 else P["mid"]
            if y == 5 and col == 2:
                c = P["shine"]
            put(img, x, y, c)
    return img


def paint_bottom() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), P["air"])
    body = paint_top()
    for y in range(SIZE):
        for x in range(SIZE):
            a = body.getpixel((x, y))[3]
            if a == 0:
                continue
            # Flatten to darker underside; keep blank
            x0, y0, x1, y1 = BLANK
            if x0 <= x < x1 and y0 <= y < y1:
                put(img, x, y, P["air"])
            else:
                put(img, x, y, P["mid"] if (x + y) % 3 else P["deep"])
    # rim
    for x, y in ((3, 1), (7, 1), (11, 1), (1, 7), (14, 7), (7, 14)):
        put(img, x, y, P["edge"])
    return img


def paint_inner() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), P["mid"])
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=P["edge"], width=1)
    d.rectangle([1, 1, 14, 14], outline=P["deep"], width=1)
    d.rectangle([2, 2, 13, 13], fill=P["fill"])
    for x, y in ((4, 4), (6, 5), (9, 4), (11, 6), (4, 9), (7, 10), (10, 11), (12, 9)):
        put(img, x, y, P["fill2"])
    # yolk
    for x, y in ((6, 6), (7, 6), (8, 6), (6, 7), (7, 7), (8, 7), (9, 7), (6, 8), (7, 8), (8, 8), (7, 9)):
        put(img, x, y, P["yolk"])
    put(img, 7, 7, P["yolk2"])
    put(img, 6, 6, P["shine"])
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


def pale(img: Image.Image) -> Image.Image:
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
    print("wrote", path.name, img.size)


def main() -> None:
    top = paint_top()
    side = paint_side()
    bottom = paint_bottom()
    inner = paint_inner()
    for pref, amt in (("", 0.0), ("exposed_", 0.28), ("weathered_", 0.52), ("oxidized_", 0.78)):
        t = top if amt == 0 else gray(top, amt)
        s = side if amt == 0 else gray(side, amt)
        b = bottom if amt == 0 else gray(bottom, amt)
        i = inner if amt == 0 else gray(inner, amt)
        n = f"{pref}mooncake"
        save(t, BLOCK / f"{n}_top.png")
        save(s, BLOCK / f"{n}_side.png")
        save(b, BLOCK / f"{n}_bottom.png")
        save(i, BLOCK / f"{n}_inner.png")
        save(t, ITEM / ("mooncake.png" if pref == "" else f"{pref}mooncake.png"))
    save(pale(top), ITEM / "raw_mooncake.png")
    # dough stays simple 16x16 dots
    dough = Image.new("RGBA", (SIZE, SIZE), P["air"])
    for y in range(4, 13):
        for x in range(3, 13):
            if (x - 7.5) ** 2 / 25 + (y - 8.5) ** 2 / 16 <= 1:
                put(dough, x, y, P["lit"] if x + y < 14 else P["gold"])
    save(dough, ITEM / "mooncake_dough.png")
    inc = dough.copy()
    for x, y in ((5, 7), (8, 9), (10, 6)):
        put(inc, x, y, P["mid"])
    save(inc, ITEM / "incomplete_mooncake_dough.png")


if __name__ == "__main__":
    main()
