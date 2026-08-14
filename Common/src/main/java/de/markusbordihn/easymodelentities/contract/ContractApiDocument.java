/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.api.EasyModelApiContract;
import java.util.List;

public final class ContractApiDocument {

  public static final String RESOURCE_PATH =
      "data/easy_model_entities/contract/easy-model-api-"
          + EasyModelApiContract.schemaVersion()
          + ".json";

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private ContractApiDocument() {}

  public static JsonObject toJson() {
    JsonObject root = new JsonObject();
    root.addProperty("schema_version", EasyModelApiContract.schemaVersion());
    root.addProperty("api_version", EasyModelApiContract.apiVersion());
    root.add("supported_schema_versions", toArray(EasyModelApiContract.supportedSchemaVersions()));

    JsonObject budgets = new JsonObject();
    budgets.addProperty("max_model_file_size_bytes", EasyModelApiContract.maxModelFileSizeBytes());
    budgets.addProperty(
        "soft_model_file_size_bytes", EasyModelApiContract.softModelFileSizeBytes());
    budgets.addProperty("max_texture_size", EasyModelApiContract.maxTextureSize());
    budgets.addProperty("soft_texture_size", EasyModelApiContract.softTextureSize());
    budgets.addProperty("max_bone_count", EasyModelApiContract.maxBoneCount());
    budgets.addProperty("soft_bone_count", EasyModelApiContract.softBoneCount());
    budgets.addProperty("max_cube_count", EasyModelApiContract.maxCubeCount());
    budgets.addProperty("soft_cube_count", EasyModelApiContract.softCubeCount());
    budgets.addProperty("max_hierarchy_depth", EasyModelApiContract.maxHierarchyDepth());
    budgets.addProperty("soft_hierarchy_depth", EasyModelApiContract.softHierarchyDepth());
    budgets.addProperty("max_animation_count", EasyModelApiContract.maxAnimationCount());
    budgets.addProperty("soft_animation_count", EasyModelApiContract.softAnimationCount());
    root.add("budgets", budgets);

    JsonObject enums = new JsonObject();
    enums.add("model_types", toArray(EasyModelApiContract.modelTypes()));
    enums.add("body_types", toArray(EasyModelApiContract.bodyTypes()));
    enums.add("movement_types", toArray(EasyModelApiContract.movementTypes()));
    enums.add("behavior_modes", toArray(EasyModelApiContract.behaviorModes()));
    enums.add("preset_types", toArray(EasyModelApiContract.presetTypes()));
    enums.add("block_entity_preset_types", toArray(EasyModelApiContract.blockEntityPresetTypes()));
    enums.add("animation_modes", toArray(EasyModelApiContract.animationModes()));
    enums.add("animation_variant_modes", toArray(EasyModelApiContract.animationVariantModes()));
    enums.add("animation_states", toArray(EasyModelApiContract.animationStates()));
    enums.add("animation_clips", toArray(EasyModelApiContract.animationClips()));
    enums.add("animation_loops", toArray(EasyModelApiContract.animationLoops()));
    enums.add("animation_switch_timings", toArray(EasyModelApiContract.animationSwitchTimings()));
    enums.add("playback_modes", toArray(EasyModelApiContract.playbackModes()));
    enums.add("animation_types", toArray(EasyModelApiContract.animationTypes()));
    enums.add("texture_blends", toArray(EasyModelApiContract.textureBlends()));
    enums.add("gaits", toArray(EasyModelApiContract.gaits()));
    root.add("enums", enums);

    JsonObject statusCodes = new JsonObject();
    statusCodes.add("server", toArray(EasyModelApiContract.serverStatuses()));
    statusCodes.add("render", toArray(EasyModelApiContract.renderStatuses()));
    root.add("status_codes", statusCodes);

    root.add("diagnostic_severities", toArray(EasyModelApiContract.diagnosticSeverities()));
    return root;
  }

  public static String toJsonString() {
    return GSON.toJson(toJson());
  }

  private static JsonArray toArray(List<String> values) {
    JsonArray array = new JsonArray();
    values.forEach(array::add);
    return array;
  }
}
