# DungeonZ

DungeonZ is a mod which adds the basement for creating explorable dungeons.

### Installation

DungeonZ is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) to be installed separately; all other dependencies are installed with the mod.

### License

DungeonZ is licensed under MIT.

### Datapacks

If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack) website and try to create your first one for the vanilla game.
If you know how to create one, the folder path has to be ```data\dungeonz\dungeon\YOURFILE.json```

```json
{
    "dungeon_type": "dark_dungeon",
    "difficulty": {
        "easy": {
            "mob_modificator": 1.0,
            "loot_table_ids": [
                "dungeonz:chests/dark_dungeon_low_tier_chest_loot",
                "dungeonz:chests/dark_dungeon_mid_tier_chest_loot"
            ],
            "boss_modificator": 1.0,
            "boss_loot_table_id": "dungeonz:chests/dark_dungeon_easy_boss_loot"
        },
        "normal": {
            "mob_modificator": 1.5,
            "loot_table_ids": [
                "dungeonz:chests/dark_dungeon_low_tier_chest_loot",
                "dungeonz:chests/dark_dungeon_mid_tier_chest_loot",
                "dungeonz:chests/dark_dungeon_high_tier_chest_loot"
            ],
            "boss_modificator": 2.0,
            "boss_loot_table_id": "dungeonz:chests/dark_dungeon_normal_boss_loot"
        }
    },
    "blocks": {
        "minecraft:gold_block": {
            "spawns": [
                "minecraft:skeleton"
            ],
            "chance": {
                "easy": 0.4,
                "normal": 0.7
            },
            "replace": "minecraft:air"
        },
        "minecraft:iron_block": {
            "spawns": [
                "minecraft:zombie"
            ],
            "chance": {
                "easy": 0.4,
                "normal": 0.7
            },
            "replace": "minecraft:air"
        },
        "minecraft:netherite_block": {
            "boss_entity": "minecraft:warden",
            "replace": "minecraft:air"
        },
        "minecraft:emerald_block": {
            "boss_loot_block": true,
            "replace": "minecraft:air"
        },
        "minecraft:quartz_block": {
            "exit_block": true,
            "replace": "minecraft:stone_bricks"
        }
    },
    "spawner": {
        "minecraft:zombie": 10,
        "minecraft:skeleton": 5
    },
    "breakable": [],
    "placeable": [
        "minecraft:torch"
    ],
    "required": {
        "minecraft:stick": 3
    },
    "elytra": false,
    "max_group_size": 5,
    "cooldown": 108000,
    "background_texture": "",
    "dungeon_structure_pool_id": "dungeonz:dark_dungeon/dungeon_spawn"
}
```

Make sure your first structure piece (`"dungeon_structure_pool_id"`) has a jigsaw block named `dungeonz:spawn`. This is the block where the player will teleport to.

An example part for the overworld structure which leads to the dungeon:

```json
{
    "type": "dungeonz:dimension_structures",
    "start_pool": "dungeonz:overworld_test_start",
    "size": 1,
    "max_distance_from_center": 80,
    "biomes": "#minecraft:is_overworld",
    "step": "surface_structures",
    "start_height": {
        "absolute": 0
    },
    "project_start_to_heightmap": "WORLD_SURFACE_WG",
    "spawn_overrides": {},
    "dungeon_type": "ruin_dungeon"
}
```

DungeonZ has an extra field called `"dungeon_type"` which has to be one of the defined dungeon types.

### Advancement

DungeonZ provides a advancement criterion trigger called `dungeonz:dungeon_completion`.

```json
    "criteria": {
        "completion_example": {
            "trigger": "dungeonz:dungeon_completion",
            "conditions": {
                "dungeon_type": "ruin_dungeon",
                "difficulty": "easy"
            }
        }
    }
```
