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

package de.markusbordihn.easymodelentities.data.model.decoder;

import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record DecodedModel(
    Identifier modelId,
    int textureWidth,
    int textureHeight,
    List<DecodedModelPart> rootParts,
    List<DecodedTexture> textures,
    Map<String, ModelAnimationClip> animations,
    List<ModelRenderProfileValidationIssue> validationIssues) {

  public DecodedModel {
    Objects.requireNonNull(modelId, "modelId");
    rootParts = List.copyOf(Objects.requireNonNull(rootParts, "rootParts"));
    textures = List.copyOf(Objects.requireNonNull(textures, "textures"));
    animations = Map.copyOf(Objects.requireNonNull(animations, "animations"));
    validationIssues = List.copyOf(Objects.requireNonNull(validationIssues, "validationIssues"));
  }

  public DecodedModel(
      Identifier modelId,
      int textureWidth,
      int textureHeight,
      List<DecodedModelPart> rootParts,
      List<DecodedTexture> textures,
      List<ModelRenderProfileValidationIssue> validationIssues) {
    this(modelId, textureWidth, textureHeight, rootParts, textures, Map.of(), validationIssues);
  }

  public DecodedModel(
      Identifier modelId,
      int textureWidth,
      int textureHeight,
      List<DecodedModelPart> rootParts,
      List<ModelRenderProfileValidationIssue> validationIssues) {
    this(modelId, textureWidth, textureHeight, rootParts, List.of(), Map.of(), validationIssues);
  }

  public int boneCount() {
    return rootParts.stream().mapToInt(DecodedModelPart::partCount).sum();
  }

  public int cubeCount() {
    return rootParts.stream().mapToInt(DecodedModelPart::cubeCount).sum();
  }

  public int hierarchyDepth() {
    return rootParts.stream().mapToInt(DecodedModelPart::hierarchyDepth).max().orElse(0);
  }
}
