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

package de.markusbordihn.easymodelentities.data.renderprofile;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record EasyModelRenderProfile(
    Identifier id,
    String schemaVersion,
    String version,
    ModelBodyType bodyType,
    Identifier model,
    Identifier texture,
    Map<Integer, Identifier> textures,
    ModelRenderSettings rendering,
    ModelAnimationSettings animation,
    ModelRenderProfileStatus status,
    List<ModelRenderProfileValidationIssue> validationIssues,
    String assetFingerprint) {

  public EasyModelRenderProfile {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(schemaVersion, "schemaVersion");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(bodyType, "bodyType");
    Objects.requireNonNull(model, "model");
    Objects.requireNonNull(texture, "texture");
    Objects.requireNonNull(rendering, "rendering");
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(status, "status");
    textures = Map.copyOf(Objects.requireNonNull(textures, "textures"));
    validationIssues = List.copyOf(Objects.requireNonNull(validationIssues, "validationIssues"));
    assetFingerprint = assetFingerprint == null ? "" : assetFingerprint;
  }

  public EasyModelRenderProfile(
      Identifier id,
      String schemaVersion,
      String version,
      ModelBodyType bodyType,
      Identifier model,
      Identifier texture,
      Map<Integer, Identifier> textures,
      ModelRenderSettings rendering,
      ModelAnimationSettings animation,
      ModelRenderProfileStatus status,
      List<ModelRenderProfileValidationIssue> validationIssues) {
    this(
        id,
        schemaVersion,
        version,
        bodyType,
        model,
        texture,
        textures,
        rendering,
        animation,
        status,
        validationIssues,
        "");
  }

  public EasyModelRenderProfile(
      Identifier id,
      String schemaVersion,
      String version,
      ModelBodyType bodyType,
      Identifier model,
      Identifier texture,
      ModelRenderSettings rendering,
      ModelAnimationSettings animation,
      ModelRenderProfileStatus status,
      List<ModelRenderProfileValidationIssue> validationIssues) {
    this(
        id,
        schemaVersion,
        version,
        bodyType,
        model,
        texture,
        Map.of(),
        rendering,
        animation,
        status,
        validationIssues,
        "");
  }

  public boolean isActive() {
    return this.status == ModelRenderProfileStatus.ACTIVE;
  }

  public boolean usesFallbackModel() {
    return this.status != ModelRenderProfileStatus.ACTIVE
        && this.status != ModelRenderProfileStatus.MISSING_TEXTURE;
  }

  public boolean usesFallbackTexture() {
    return this.status == ModelRenderProfileStatus.MISSING_TEXTURE;
  }

  public float scale() {
    return this.rendering.scale();
  }

  public float shadowRadius() {
    return this.rendering.shadowRadius();
  }

  public float visibleBoundsWidth() {
    return this.rendering.visibleBoundsWidth();
  }

  public float visibleBoundsHeight() {
    return this.rendering.visibleBoundsHeight();
  }

  public Vec3f visibleBoundsOffset() {
    return this.rendering.visibleBoundsOffset();
  }

  public boolean hasVisibleBounds() {
    return this.rendering.hasVisibleBounds();
  }

  public EasyModelRenderProfile withValidationIssues(
      List<ModelRenderProfileValidationIssue> validationIssues) {
    return new EasyModelRenderProfile(
        this.id,
        this.schemaVersion,
        this.version,
        this.bodyType,
        this.model,
        this.texture,
        this.textures,
        this.rendering,
        this.animation,
        ModelRenderProfileStatus.statusForIssues(validationIssues),
        validationIssues,
        this.assetFingerprint);
  }
}
