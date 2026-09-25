# xsMooncake

面向 **Minecraft 1.21.1 / NeoForge** 的数据驱动月饼模组：自定义馅料与皮料、铜块式变质与包膜、硬化建材与装备，以及可扩展的食用效果。

当前版本：**0.2**  
仓库：[qwer854645/xsMooncake](https://github.com/qwer854645/xsMooncake)  
发布页：[Releases](https://github.com/qwer854645/xsMooncake/releases)

## 需要环境

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1+ |

### 可选兼容

- **JEI** — 配方查看
- **Jade** — 方块 / 物品信息
- **Create** — 搅拌等兼容配方
- **Farmer's Delight** — 砧板切割等兼容

## 安装

1. 安装对应版本的 NeoForge
2. 从 [Releases](https://github.com/qwer854645/xsMooncake/releases) 下载 `mooncake-x.y.jar`
3. 放入游戏目录的 `mods` 文件夹后启动

## 玩法概览

### 制作流程

1. **搅拌盆**：小麦 + 鸡蛋 + 糖 → 未完成的月饼皮；再放入未完成皮与可选**皮料**，搅拌得到带皮料的月饼皮  
2. **月饼工作台**：月饼皮 + 馅料 → 未完成的月饼  
3. **烤制**（营火等）：得到可食用月饼；可切成切片  
4. **保鲜膜**：包膜，阻止变质加深  
5. **硬化**：进一步做成硬化月饼、建材（块 / 切制 / 台阶 / 楼梯 / 门）与装备

### 馅料与皮料

- 多数物品可作馅料；皮料在搅拌盆第二阶段加入
- 食用时会结算馅料 / 皮料的食物属性，以及**药水等内容物效果**（如药水、牛奶、不祥之瓶等）
- 可用标签限制：`#mooncake:valid_fillings` / `banned_fillings`、`#mooncake:valid_crusts` / `banned_crusts`
- 默认禁止用本模组月饼系列物品再当馅 / 皮（防止存档问题）；可在配置关闭

### 变质与包膜

月饼与硬化系列类似铜：轻微变质 → 变质 → 严重变质；用水瓶可加深氧化阶段，用保鲜膜包膜后不再自然变质。

### 硬化装备

硬化月饼可做成工具、武器、护甲、盾、弓弩、箭等。装备可携带馅料效果：

- 武器命中有概率对目标施加馅料相关效果
- 工具破坏方块有概率对使用者触发
- 护甲可提供较弱的持续光环效果

### 数据驱动扩展

数据包可扩展：

- `data/*/mooncake/filling_rules/` — 馅料命名与营养加成
- `data/*/mooncake/crust_rules/` — 皮料规则
- `data/*/mooncake/eat_effects/` — 食用触发的状态效果 / 特殊行为

## 主要配置

服务端配置文件：`config/mooncake-server.toml`（字段名以实际文件为准）

| 选项 | 默认 | 说明 |
|------|------|------|
| `maxFillingsPerMooncake` | 64 | 单个月饼最大馅料数 |
| `maxCrustsPerMooncake` | 16 | 单个月饼最大皮料数 |
| `applyFillingFoodsOnEat` | true | 食用时完整结算馅料食物效果 |
| `banMooncakeSeriesAsIngredients` | true | 禁止本模组月饼系列再作馅 / 皮 |
| `mixingBowlTime` | 100 | 搅拌耗时（tick） |
| `weaponFillingsEffectChance` | 0.20 | 硬化武器触发馅料效果概率 |
| `toolFillingsEffectChance` | 0.15 | 硬化工具触发馅料效果概率 |
| `equipmentWetOxidationInterval` / `Chance` | 100 / 0.01 | 潮湿环境下装备氧化 |

## 从源码构建

```bash
./gradlew build
```

产物位于 `build/libs/mooncake-<version>.jar`。

## 许可证

[MIT](LICENSE)

---

## English

**xsMooncake** is a NeoForge 1.21.1 mod for data-driven mooncakes: custom fillings and crust extras, copper-like weathering and waxing, hardened building blocks and gear, plus datapack eat-effects.

- **Requires:** Minecraft 1.21.1, NeoForge 21.1+
- **Optional:** JEI, Jade, Create, Farmer's Delight
- **Download:** [Releases](https://github.com/qwer854645/xsMooncake/releases)

Craft dough in the mixing bowl (optional crust extras), assemble fillings on the workbench, cook soft cakes, wax to stop weathering, or harden into blocks and equipment. Eating applies filling/crust food effects and potion-like component effects. Datapacks under `mooncake/filling_rules`, `crust_rules`, and `eat_effects` can extend behavior.

License: MIT.
