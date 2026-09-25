"""Paint base Cantonese mooncake textures (16x16) for the current remake step."""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/mooncake/textures/block"
OUT.mkdir(parents=True, exist_ok=True)

# Light brown crust (side / rim), deep brown underside
LIGHT = (198, 148, 86, 255)
LIGHT2 = (186, 132, 72, 255)
DEEP = (78, 42, 20, 255)
DEEP2 = (92, 52, 26, 255)
EDGE2 = (140, 88, 48, 255)


def in_cake(x: int, y: int) -> tuple[bool, float, float]:
    cx, cy = 7.5, 7.5
    dx, dy = x + 0.5 - cx, y + 0.5 - cy
    r = math.hypot(dx, dy)
    ang = math.atan2(dy, dx)
    flute = abs(math.cos(ang * 4))  # 8 perimeter hills
    rim = 6.35 + 0.85 * flute
    return r <= rim, r, rim


def paint_bottom() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            ok, r, rim = in_cake(x, y)
            if not ok:
                continue
            dist_in = rim - r
            if dist_in < 0.9:
                t = dist_in / 0.9
                c = tuple(int(LIGHT[i] * (1 - t) + EDGE2[i] * t) for i in range(3)) + (255,)
            elif dist_in < 1.7:
                t = (dist_in - 0.9) / 0.8
                c = tuple(int(EDGE2[i] * (1 - t) + DEEP[i] * t) for i in range(3)) + (255,)
            else:
                c = DEEP2 if ((x + y) % 5 == 0) else DEEP
            px[x, y] = c
    return img


def paint_side() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            wave = abs(math.cos((x + 0.5) / 16.0 * math.pi * 8))
            if wave > 0.55:
                c = LIGHT
            elif wave > 0.25:
                c = tuple(int(LIGHT[i] * 0.7 + LIGHT2[i] * 0.3) for i in range(3)) + (255,)
            else:
                c = LIGHT2
            if y <= 1 or y >= 14:
                c = tuple(max(0, c[i] - 18) for i in range(3)) + (255,)
            px[x, y] = c
    return img


def main() -> None:
    bottom = paint_bottom()
    bottom.save(OUT / "mooncake_bottom.png")
    bottom.save(OUT / "mooncake_top.png")  # top temporarily same as bottom
    paint_side().save(OUT / "mooncake_side.png")
    print("wrote", OUT / "mooncake_{top,bottom,side}.png")


if __name__ == "__main__":
    main()
