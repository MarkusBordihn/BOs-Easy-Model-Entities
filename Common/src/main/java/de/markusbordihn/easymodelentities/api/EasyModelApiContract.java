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

package de.markusbordihn.easymodelentities.api;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnosticSeverity;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelPresetType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelGaitType;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import java.util.Arrays;
import java.util.List;

public final class EasyModelApiContract {

  private EasyModelApiContract() {}

  public static String schemaVersion() {
    return Constants.SCHEMA_VERSION;
  }

  public static int maxModelFileSizeBytes() {
    return ModelAssetBudgets.MAX_MODEL_FILE_SIZE_BYTES;
  }

  public static int softModelFileSizeBytes() {
    return ModelAssetBudgets.SOFT_MODEL_FILE_SIZE_BYTES;
  }

  public static int maxTextureSize() {
    return ModelAssetBudgets.MAX_TEXTURE_SIZE;
  }

  public static int softTextureSize() {
    return ModelAssetBudgets.SOFT_TEXTURE_SIZE;
  }

  public static int maxBoneCount() {
    return ModelAssetBudgets.MAX_BONE_COUNT;
  }

  public static int softBoneCount() {
    return ModelAssetBudgets.SOFT_BONE_COUNT;
  }

  public static int maxCubeCount() {
    return ModelAssetBudgets.MAX_CUBE_COUNT;
  }

  public static int softCubeCount() {
    return ModelAssetBudgets.SOFT_CUBE_COUNT;
  }

  public static int maxHierarchyDepth() {
    return ModelAssetBudgets.MAX_HIERARCHY_DEPTH;
  }

  public static int softHierarchyDepth() {
    return ModelAssetBudgets.SOFT_HIERARCHY_DEPTH;
  }

  public static int maxAnimationCount() {
    return ModelAssetBudgets.MAX_ANIMATION_COUNT;
  }

  public static List<String> modelTypes() {
    return Arrays.stream(ModelType.values()).map(ModelType::getSerializedName).toList();
  }

  public static List<String> bodyTypes() {
    return Arrays.stream(ModelBodyType.values()).map(ModelBodyType::getSerializedName).toList();
  }

  public static List<String> movementTypes() {
    return Arrays.stream(ModelMovementType.values())
        .map(ModelMovementType::getSerializedName)
        .toList();
  }

  public static List<String> behaviorModes() {
    return Arrays.stream(ModelBehaviorMode.values())
        .map(ModelBehaviorMode::getSerializedName)
        .toList();
  }

  public static List<String> presetTypes() {
    return Arrays.stream(ModelPresetType.values()).map(ModelPresetType::getSerializedName).toList();
  }

  public static List<String> blockEntityPresetTypes() {
    return Arrays.stream(ModelBlockEntityPresetType.values())
        .map(ModelBlockEntityPresetType::getSerializedName)
        .toList();
  }

  public static List<String> animationModes() {
    return Arrays.stream(ModelAnimationMode.values())
        .map(ModelAnimationMode::getSerializedName)
        .toList();
  }

  public static List<String> animationClips() {
    return ModelAnimationClips.STANDARD;
  }

  public static List<String> gaits() {
    return Arrays.stream(ModelGaitType.values()).map(ModelGaitType::getSerializedName).toList();
  }

  public static List<String> serverStatuses() {
    return Arrays.stream(ModelProfileStatus.values()).map(Enum::name).toList();
  }

  public static List<String> renderStatuses() {
    return Arrays.stream(ModelRenderProfileStatus.values()).map(Enum::name).toList();
  }

  public static List<String> diagnosticSeverities() {
    return Arrays.stream(ModelDiagnosticSeverity.values()).map(Enum::name).toList();
  }
}
