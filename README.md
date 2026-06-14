# Easy Model Entities (1.20.1)

[![Report an Issue](https://img.shields.io/badge/Report%20an%20Issue-Bug%20%2F%20Crash%20%2F%20Feature%20Request-black?style=flat&logo=github)][issues]
[![Wiki](https://img.shields.io/badge/Wiki-Documentation-black?style=flat&logo=github)][wiki]
[![CurseForge](https://img.shields.io/badge/CurseForge-Easy%20Model%20Entities-f16436?style=flat&logo=curseforge)][curseforge]
[![Support me on Ko-fi](https://img.shields.io/badge/Support_me_on_Ko--fi-!?labelColor=black&style=flat&logo=ko-fi)][ko-fi]

![Easy Model Entities Logo][logo]

**Easy Model Entities** turns Blockbench models into Minecraft entities and
block-entities without requiring custom Java code for every model.

It exists to make custom models easier to ship, test, and maintain. Instead of
writing a renderer, registering one-off entity classes, and hardcoding
animation behavior, you describe the model with data and resource pack profiles.
EME provides the host entities, host blocks, renderer integration, profile
validation, and a small automatic animation layer.

Install it if you want custom Blockbench models that can appear as ambient
creatures, static display entities, decorative block-entities, shrines, mimics,
or mod-integrated renderable objects with much less boilerplate.

## Why Use It?

- **No per-model Java required**: Define most models with data packs and
  resource packs.
- **Entity and block-entity support**: Use moving entities for mobs or fixed
  block-entities for decorations and machines.
- **Blockbench-friendly workflow**: Load bundled `.bbmodel` assets directly
  from resource packs.
- **Profile-driven behavior**: Configure host type, body type, dimensions,
  movement, behavior, rendering, texture, and animation from JSON.
- **Automatic animation**: Basic idle, walk, wing, tail, and random-idle
  behavior is available out of the box.
- **Developer API**: Mods can use render delegates, custom part animators, and
  runtime profile data without depending on internal renderer classes.
- **Diagnostics and demo profiles**: Commands help list, validate, debug, spawn,
  and place test models in a dev world.

## What Can You Build?

- Ambient critters, NPCs, training dummies, and simple custom entities.
- Static display models that still use Minecraft entity rendering.
- Decorative block-entities such as shrines, statues, altars, or mimics.
- Mod-controlled entities and block-entities that use EME for model loading,
  rendering, texture handling, and optional part animation.

## Quick Start

After installing the mod, try the bundled demo profiles in a dev or test world:

```mcfunction
/easy_model_entities summon easy_model_entities_examples:entity/little_explorer
/easy_model_entities summon easy_model_entities_examples:entity/stone_turtle
/easy_model_entities place_block easy_model_entities_examples:block_entity/shrine
```

Useful diagnostic commands:

```mcfunction
/easy_model_entities list_profiles
/easy_model_entities validate_profiles
/easy_model_entities debug_profile easy_model_entities_examples:entity/little_explorer
```

More demo commands and explanations are available in the
[Demo Commands wiki page](wiki/Demo.md).

## Pack Workflow

Easy Model Entities uses two profile layers:

- **Server profiles** in a data pack decide what the model is and how it
  behaves.
- **Render profiles** in a resource pack decide which model, texture, scale,
  bounds, and animation settings are used on the client.

Typical files:

```text
data/<namespace>/easy_model_entities/profiles/entity/<id>.json
data/<namespace>/easy_model_entities/profiles/block_entity/<id>.json

assets/<namespace>/easy_model_entities/render_profiles/<id>.json
assets/<namespace>/easy_model_entities/models/<model>.bbmodel
assets/<namespace>/textures/entity/<texture>.png
assets/<namespace>/textures/block/<texture>.png
```

See the [Pack Usage wiki page](wiki/User-Guide.md) for the full structure.

## Developer Integration

Mods can integrate EME into their own renderers by implementing
`EasyModelRenderable` and using the client render delegates:

- `EasyModelEntityRenderDelegate`
- `EasyModelBlockEntityRenderDelegate`
- `EasyModelEntityRenderOptions`
- `EasyModelBlockEntityRenderOptions`
- `EasyModelPartAnimator`

Custom part animation can either add to the automatic EME transform or replace
it entirely with `EasyModelPartAnimationMode.REPLACE`.

See the [Developer Integration wiki page](wiki/Developer-Guide.md) for examples.

## Installation and Compatibility

This branch targets **Minecraft 1.20.1** and provides loader-specific modules
for **Fabric** and **Forge**.

Install the matching loader build on both client and server when profiles,
entities, or block-entities are used in multiplayer.

## Documentation

- [Wiki Home][wiki]
- [Pack Usage](wiki/User-Guide.md)
- [Entities](wiki/Entity-Guide.md)
- [Block-Entities](wiki/Block-Entity-Guide.md)
- [Developer Integration](wiki/Developer-Guide.md)
- [Profile Reference](wiki/Profile-Reference.md)
- [Demo Commands](wiki/Demo.md)

## License

The [MIT LICENSE](LICENSE.md) applies to the code in this repository.
Images, models, textures, and other assets may have separate rights and are not
automatically covered by the code license.

[curseforge]: https://curseforge.com/minecraft/mc-mods/easy-model-entities

[issues]: https://github.com/MarkusBordihn/BOs-Easy-Model-Entities/issues

[ko-fi]: https://ko-fi.com/Kaworru

[wiki]: https://github.com/MarkusBordihn/BOs-Easy-Model-Entities/wiki

[logo]: Common/src/main/resources/logo.png
