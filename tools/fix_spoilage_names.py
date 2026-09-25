import json
from pathlib import Path

STAGES = [
    ("", "unaffected"),
    ("exposed_", "exposed"),
    ("weathered_", "weathered"),
    ("oxidized_", "oxidized"),
    ("waxed_", "waxed"),
    ("waxed_exposed_", "waxed_exposed"),
    ("waxed_weathered_", "waxed_weathered"),
    ("waxed_oxidized_", "waxed_oxidized"),
]


def rewrite_zh(path: Path) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    data.pop("item.mooncake.weather.unaffected", None)
    data["item.mooncake.weather.exposed"] = "轻微变质的%s"
    data["item.mooncake.weather.weathered"] = "变质的%s"
    data["item.mooncake.weather.oxidized"] = "严重变质的%s"
    data["item.mooncake.weather.waxed"] = "包膜的%s"
    data["item.mooncake.weather.waxed_exposed"] = "包膜的轻微变质的%s"
    data["item.mooncake.weather.waxed_weathered"] = "包膜的变质的%s"
    data["item.mooncake.weather.waxed_oxidized"] = "包膜的严重变质的%s"
    data["item.mooncake.waxed"] = "包膜的%s"

    stage_prefix = {
        "unaffected": "",
        "exposed": "轻微变质的",
        "weathered": "变质的",
        "oxidized": "严重变质的",
        "waxed": "包膜的",
        "waxed_exposed": "包膜的轻微变质的",
        "waxed_weathered": "包膜的变质的",
        "waxed_oxidized": "包膜的严重变质的",
    }
    bases = {
        "mooncake": "月饼",
        "mooncake_piece": "月饼块",
        "hardened_mooncake": "硬化月饼",
        "hardened_mooncake_piece": "硬化月饼切块",
        "hardened_mooncake_block": "硬化月饼块",
        "cut_hardened_mooncake": "切制硬化月饼块",
        "cut_hardened_mooncake_slab": "切制硬化月饼台阶",
        "cut_hardened_mooncake_stairs": "切制硬化月饼楼梯",
        "hardened_mooncake_door": "硬化月饼门",
    }
    for key in list(data.keys()):
        if not key.startswith("block.mooncake."):
            continue
        name = key[len("block.mooncake.") :]
        if name in ("mixing_bowl", "mooncake_workbench"):
            continue
        for pref, stage in STAGES:
            for base_id, base_zh in bases.items():
                if name == pref + base_id:
                    data[key] = stage_prefix[stage] + base_zh
                    break
            else:
                continue
            break
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("updated", path)


def rewrite_en(path: Path) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    data.pop("item.mooncake.weather.unaffected", None)
    data["item.mooncake.weather.exposed"] = "Slightly Spoiled %s"
    data["item.mooncake.weather.weathered"] = "Spoiled %s"
    data["item.mooncake.weather.oxidized"] = "Severely Spoiled %s"
    data["item.mooncake.weather.waxed"] = "Wrapped %s"
    data["item.mooncake.weather.waxed_exposed"] = "Wrapped Slightly Spoiled %s"
    data["item.mooncake.weather.waxed_weathered"] = "Wrapped Spoiled %s"
    data["item.mooncake.weather.waxed_oxidized"] = "Wrapped Severely Spoiled %s"
    data["item.mooncake.waxed"] = "Wrapped %s"

    stage_prefix = {
        "unaffected": "",
        "exposed": "Slightly Spoiled ",
        "weathered": "Spoiled ",
        "oxidized": "Severely Spoiled ",
        "waxed": "Wrapped ",
        "waxed_exposed": "Wrapped Slightly Spoiled ",
        "waxed_weathered": "Wrapped Spoiled ",
        "waxed_oxidized": "Wrapped Severely Spoiled ",
    }
    bases = {
        "mooncake": "Mooncake",
        "mooncake_piece": "Mooncake Piece",
        "hardened_mooncake": "Hardened Mooncake",
        "hardened_mooncake_piece": "Hardened Mooncake Piece",
        "hardened_mooncake_block": "Hardened Mooncake Block",
        "cut_hardened_mooncake": "Cut Hardened Mooncake",
        "cut_hardened_mooncake_slab": "Cut Hardened Mooncake Slab",
        "cut_hardened_mooncake_stairs": "Cut Hardened Mooncake Stairs",
        "hardened_mooncake_door": "Hardened Mooncake Door",
    }
    for key in list(data.keys()):
        if not key.startswith("block.mooncake."):
            continue
        name = key[len("block.mooncake.") :]
        if name in ("mixing_bowl", "mooncake_workbench"):
            continue
        for pref, stage in STAGES:
            for base_id, base_en in bases.items():
                if name == pref + base_id:
                    data[key] = stage_prefix[stage] + base_en
                    break
            else:
                continue
            break
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("updated", path)


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    rewrite_zh(root / "src/main/resources/assets/mooncake/lang/zh_cn.json")
    rewrite_en(root / "src/main/resources/assets/mooncake/lang/en_us.json")
