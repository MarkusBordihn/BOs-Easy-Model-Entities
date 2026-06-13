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

package de.markusbordihn.easymodelentities.profile;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record EasyModelEntityProfile(
    ResourceLocation id,
    String schemaVersion,
    String version,
    ModelHostSettings host,
    ModelClientSettings client,
    ModelDimensions dimensions,
    ModelMovementSettings movement,
    ModelBehaviorSettings behavior,
    ModelAttributes attributes,
    ModelProfileStatus status,
    List<ModelProfileValidationIssue> validationIssues) {

  public EasyModelEntityProfile {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(schemaVersion, "schemaVersion");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(host, "host");
    Objects.requireNonNull(client, "client");
    Objects.requireNonNull(dimensions, "dimensions");
    Objects.requireNonNull(movement, "movement");
    Objects.requireNonNull(behavior, "behavior");
    Objects.requireNonNull(attributes, "attributes");
    Objects.requireNonNull(status, "status");
    validationIssues = List.copyOf(Objects.requireNonNull(validationIssues, "validationIssues"));
  }

  public boolean isActive() {
    return this.status == ModelProfileStatus.ACTIVE;
  }

  public ResourceLocation hostEntityType() {
    return this.host.entityType();
  }

  public ModelMovementType movementType() {
    return this.host.movementType();
  }

  public ModelBodyType bodyType() {
    return this.host.bodyType();
  }

  public ResourceLocation renderProfileId() {
    return this.client.renderProfile();
  }

  public float width() {
    return this.dimensions.width();
  }

  public float height() {
    return this.dimensions.height();
  }

  public float eyeHeight() {
    return this.dimensions.eyeHeight();
  }
}
