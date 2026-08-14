# Changelog for Easy Model Entities 1.21.1

## Note

This change log includes the summarized changes. For the full changelog, please go to
the [GitHub History][history] instead.

### 2.1.0

- Fixed a model whose only clips are numbered variants, such as `idle_2` and `idle_3`, silently
  falling back to the procedural animation.
- Changed the clip limit of a model from 16 to 64, with a warning above 32.
- Changed numbered clips to rotate by default; `animation.variant_mode: none` keeps one clip.
- Changed a numbered variant such as `walk_2` to count as a standard clip, not a custom one.
- Added `texture set`, `clear` and `query` to swap the texture of a single model slot at runtime.
- Added the texture override as saved, synchronized state, so it survives a reload and relog.
- Added a texture option to the render options, so a mod can swap a slot without a command.
- Added `texture blend … translucent` so a half-transparent texture is no longer drawn opaque.
- Added client API calls to list the texture slots of a model and the textures next to a slot.
- Added random clip variants: clips named `idle`, `idle_2`, `idle_3` form one group and EME picks a
  different one each cycle, controlled by `animation.variant_mode` in the render profile.
- Added `animation play … random "<clips>"` to pick one clip of a list on the server, so every
  player sees the same animation.

### 2.0.0

- Fixed the command auto-completion never offering the UUID of the entity in the crosshair.
- Changed the command auto-completion to drop `@a`, `@p`, `@r` and player names, which can never
  select a model entity.
- Changed every public signature to use stable API types and enums instead of internal classes.
- Changed the numeric animation states and named clips into the type-safe `EasyModelAnimation`.
- Changed the animation of an entity or block entity into saved, synchronized state, so it survives
  a reload and reaches players who arrive later.
- Changed render and model values to reject invalid input before it reaches rendering.
- Changed an invalid transform to show a reloadable placeholder instead of dropping the model.
- Changed the minimum Forge, Fabric Loader, and Fabric API versions; older loaders are rejected.
- Changed animations to keep their own clock, so a finished animation no longer jumps back.
- Removed `set_animation`; use `animation set entity <targets> <state>`, which does the same and
  also accepts named clips and `loop` or `once`.
- Added `behavior set` to freeze the movement of a host entity, so a `sit` or `death` animation no
  longer plays while the entity walks away.
- Added named Blockbench clips and host hooks for custom animations without a fixed state.
- Added `sit` as a standard animation state and Blockbench clip for all body types.
- Added animation discovery and metadata to list the clips of a model with their length.
- Added playback modes, transitions, repeats, and duration limits for controlled animations.
- Added `animation set` to give a lasting animation, with `clip`, `loop`, or `once` deciding whether
  it repeats.
- Added `animation play`, `stop`, and `restart` for one-off animations on everyone watching.
- Improved the migration from schema `0.1.0` to `0.2.0` to keep the existing render profile.
- Improved playback queues, missing-clip warnings, and loops to stay bounded in long sessions.

### 1.7.0

- Fixed the head of an entity never turning towards what it looks at.
- Fixed cubes outside of an outliner group being dropped without any message.
- Fixed profiles without an explicit `texture` staying inactive because of a wrong texture path.
- Fixed a single minor finding, such as an unknown behavior mode, deactivating a whole server
  profile.
- Fixed repaired models being reported only once, because the log suppression survived a resource
  reload.
- Fixed `modded_entity` models never deriving a texture path from the model file itself.
- Fixed the profiles of a single player world staying active after leaving it, which could break the
  models on a server joined afterwards.
- Changed a model with a missing texture to render its own shape with a pink placeholder texture
  instead of a pink box.
- Changed the creative tabs to hide render profiles from unexpected folders and models the server
  does not know.
- Removed eight example entities that only mirrored another example with different presets or
  numbers.
- Added a head look render option, so another mod can supply its own look direction.
- Added warnings for unknown fields, a missing `schema_version`, a one-sided `version` declaration
  and cubes that are fully covered by other cubes.
- Added detailed log and `validate_profiles` output for every skipped or failed profile, including
  the reason and the affected field.
- Added `scale`, a part pose listener and a forced animation state to the block entity render
  options, so block entities support the same integration as entities.
- Improved error messages to name the underlying parser message and the affected texture file.
- Improved render performance by resolving bone names once while baking and by reusing the entity
  render data per frame.
- Improved the render cache to drop only the least recently used entries instead of emptying itself
  completely.
- Improved the profile and render profile parsers to share their field handling, so both report the
  same problems in the same way.

### 1.6.0

- Added an `attack` animation state with standard Blockbench `attack` clip support for all body
  types, driven by the entity's attack swing progress.
- Added a procedural vanilla-style arm swing for biped and winged humanoid models that lack an
  `attack` clip.
- Added a part pose capture API (`EasyModelPartPoseListener` / `EasyModelPartPose` via
  `EasyModelEntityRenderOptions.withPartPoseListener`) so integrations can anchor held items or
  attachments to animated model parts.
- Added `EasyModelEntitiesClientApi.getItemAnchor(profileId, arm)` with cached anchor resolution
  (dedicated `right_item`/`left_item` anchor parts take precedence over the `*_hand`/`*_arm`
  fallback).
- Added `EasyModelEntityRenderOptions.withAnimationState(int)` to force an animation state without
  implementing `EasyModelRenderable`.
- Improved performance by 10%.

### 1.5.0

- Fixed the entity and block-entity creative tabs being empty on dedicated clients by building them
  from the client render profiles instead of the server-only profile registry.
- Fixed the spawn item icons rendering nothing on dedicated clients by falling back to the client
  render profile when no server profile is available.
- Added a stateless render-by-profile-id API with `scale`, displayed bounds, and body type lookups
  for mod integrations.
- Added `EasyModelEntitiesClientApi.listRenderableProfileIds()` so integrations such as Easy NPC can
  list and preview available models on a dedicated client without server profiles.
- Changed the example render profiles to align their IDs with the server profile IDs
  (`render_profiles/entity/<id>` and `render_profiles/block_entity/<id>`), so the model type is
  derived from the profile ID prefix on the client.
- Bumped `schema_version` to `0.2.0` and removed the server profile `client.render_profile`.
- Code refactoring and cleanup to improve maintainability and readability.

### 1.4.0 (beta)

- Fixed choppy body rotation of walking host entities by using the interpolated body rotation.
- Added playback of standard Blockbench keyframe animations (`idle`, `walk`, `swim`, `fly`) with
  linear interpolation and automatic clip selection.
- Added `set_animation` command to force an animation state on host entities for testing and command
  block setups.
- Added `swim` and `fly` animation states to the API.
- Added standard `idle`/`walk`/`swim`/`fly` animations to the little explorer, stone turtle, dawn
  sparrow, and coral drifter examples.
- Added entity and block entity spawn items for all example profiles.
- Added offset, rotation, and scale to the `ModelPart` definition to allow complex model
  definitions.
- Improved spawn item tooltips to show the behavior mode and movement type of entities.
- Improved render performance by caching resolved render states and fallback models per profile.
- Improved api support for mod integrations.
- Code refactoring and cleanup to improve maintainability and readability.

### 1.3.0 (beta)

- Added a stable API for mod integrations that lists all limits, types, and status codes.
- Added a generated JSON file that documents these limits, types, and status codes per version.
- Added a test that fails if the documented values and the actual code values no longer match.
- Added an optional `asset_fingerprint` so resource packs can ship alternate model styles.
- Added a migration hook to handle profiles from older `schema_version`s.
- Improved server and client `version` matching to only report a mismatch when both sides are set
  and differ.

### 1.2.0 (beta)

- Added read-only profile catalog API (listProfiles, listProfiles by body type, listProfileIds) for
  mod integrations.
- Added still and custom example presets covering all body types, plus a fast-moving example.
- Added case-insensitive bone name matching so `Body`, `body`, and `BODY` all resolve to `body`.
- Fixed profile attributes max_health and follow_range not being applied to host entities.
- Removed unused mixins.

### 1.1.0 (beta)

- Fixed wrong UV mapping for some model parts.
- Fixed wrong block placements.
- Fixed rendering edge case with multiple texture definitions.
- Added UV mapping record to define UV mapping for model parts in a more structured way.
- Added ModelPartTypes enum to replace strings for model part names to avoid typos and errors.
- Added Vec3f enum to replace unnamed float parameter for Vec3f types to avoid typos and errors.
- Added additional model examples for different entity types.
- Added automatic face culling and model optimization to improve performance.
- Added better aquatic and ambient entity support.
- Improved performance by pre-building and adding additional cache.
- Improved example models and textures for better demonstration of features and capabilities.

### 1.0.0 (beta) 🚀

- Initial release for Minecraft 1.20.1.

[history]: https://github.com/MarkusBordihn/BOs-Easy-Model-Entities/commits/1.20.1
