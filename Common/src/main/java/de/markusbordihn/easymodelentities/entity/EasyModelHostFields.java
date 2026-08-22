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

package de.markusbordihn.easymodelentities.entity;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import java.util.Objects;
import net.minecraft.network.syncher.EntityDataAccessor;

public record EasyModelHostFields(
    EntityDataAccessor<String> profileId,
    EntityDataAccessor<String> renderProfileId,
    EntityDataAccessor<String> version,
    EntityDataAccessor<Float> width,
    EntityDataAccessor<Float> height,
    EntityDataAccessor<Float> eyeHeight,
    EntityDataAccessor<ModelBodyType> bodyType,
    EntityDataAccessor<EasyModelAnimationSetting> animation,
    EntityDataAccessor<Boolean> lookAtPlayers,
    EntityDataAccessor<Boolean> randomStroll,
    EntityDataAccessor<EasyModelTextureSetting> texture,
    EntityDataAccessor<Float> opacity,
    EntityDataAccessor<Integer> lightLevel) {

  public EasyModelHostFields {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(renderProfileId, "renderProfileId");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(width, "width");
    Objects.requireNonNull(height, "height");
    Objects.requireNonNull(eyeHeight, "eyeHeight");
    Objects.requireNonNull(bodyType, "bodyType");
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(lookAtPlayers, "lookAtPlayers");
    Objects.requireNonNull(randomStroll, "randomStroll");
    Objects.requireNonNull(texture, "texture");
    Objects.requireNonNull(opacity, "opacity");
    Objects.requireNonNull(lightLevel, "lightLevel");
  }

  public boolean isRuntimeContractField(EntityDataAccessor<?> entityDataAccessor) {
    return entityDataAccessor == this.profileId
        || entityDataAccessor == this.renderProfileId
        || entityDataAccessor == this.version
        || entityDataAccessor == this.width
        || entityDataAccessor == this.height
        || entityDataAccessor == this.eyeHeight
        || entityDataAccessor == this.bodyType
        || entityDataAccessor == this.animation;
  }

  public boolean isDimensionsField(EntityDataAccessor<?> entityDataAccessor) {
    return entityDataAccessor == this.width
        || entityDataAccessor == this.height
        || entityDataAccessor == this.eyeHeight;
  }
}
