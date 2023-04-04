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
    "dungeon_type": "ruin_dungeon",
    "difficulty": {
        "easy": {
            "mob_modificator": 1.0,
            "loot_table_ids": [
                "dungeonz:chests/test_chest_loot"
            ],
            "boss_modificator": 3.0,
            "boss_loot_table_id": "dungeonz:chests/test_chest_loot"
        },
        "normal": {
            "mob_modificator": 1.5,
            "loot_table_ids": [
                "dungeonz:chests/test_chest_loot"
            ],
            "boss_modificator": 5.0,
            "boss_loot_table_id": "dungeonz:chests/test_chest_loot"
        }
    },
    "blocks": {
        "minecraft:grass_block": {
            "spawns": [
                "minecraft:zombie",
                "minecraft:skeleton"
            ],
            "chance": 0.6,
            "replace": null
        },
        "acacia_leaves": {
            "spawns": [
                "minecraft:zombie",
                "minecraft:skeleton"
            ],
            "chance": 0.6,
            "replace": "minecraft:stone"
        },
        "minecraft:iron_ore": {
            "boss_entity": "minecraft:sheep",
            "replace": null
        }
    },
    "max_group_size": 5,
    "cooldown": 10000,
    "dungeon_structure_pool_id": "dungeonz:ruin_dungeon_start"
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
