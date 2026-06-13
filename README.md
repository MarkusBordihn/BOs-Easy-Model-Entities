# Easy Model Entities

Turn Blockbench models into simple Minecraft entities without writing Java code.

## Bundled Demo Entities

The mod includes three small demo profiles which can be spawned in a dev or test world after the
mod is installed:

```mcfunction
/summon easy_model_entities:static_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:training_dummy"}
/summon easy_model_entities:ground_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:little_explorer"}
/summon easy_model_entities:ground_entity ~ ~ ~ {ProfileId:"easy_model_entities_examples:stone_turtle"}
```

- `training_dummy` is a static wooden marker.
- `little_explorer` is a simple biped with automatic walk animation.
- `stone_turtle` is a slow quadruped with automatic leg animation.

These examples use bundled `.bbmodel` files and vanilla Minecraft block textures, so they should
render without an extra resource pack.
