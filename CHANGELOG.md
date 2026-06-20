# Changelog for Easy Model Entities 26.1.2

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

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

[history]: https://github.com/MarkusBordihn/BOs-Easy-Model-Entities/commits/26.1.2
