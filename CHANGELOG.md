# Changelog for Easy Model Entities 1.21.11

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

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
- Added `set_animation` command to force an animation state on host entities for testing and
  command block setups.
- Added `swim` and `fly` animation states to the API.
- Added standard `idle`/`walk`/`swim`/`fly` animations to the little explorer, stone turtle,
  dawn sparrow, and coral drifter examples.
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
