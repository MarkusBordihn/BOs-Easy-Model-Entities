# Easy Model Entities

Turn Blockbench models into simple Minecraft entities without writing Java code.

## Bundled Demo Entities

The mod includes three small demo profiles which can be spawned in a dev or test world after the
mod is installed:

```mcfunction
/summon easy_model_entities:static_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:entity/training_dummy"}
/summon easy_model_entities:ground_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:entity/little_explorer"}
/summon easy_model_entities:ground_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:entity/stone_turtle"}
/easy_model_entities place_block easy_model_entities_examples:block_entity/shrine ~ ~ ~
```

- `entity/training_dummy` is a static wooden marker.
- `entity/little_explorer` is a simple biped with automatic walk animation.
- `entity/stone_turtle` is a slow quadruped with automatic leg animation.
- `block_entity/shrine` places an animated shrine BlockEntity.

These examples use bundled `.bbmodel` files and vanilla Minecraft block textures, so they should
render without an extra resource pack.
