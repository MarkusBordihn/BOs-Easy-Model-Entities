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

package de.markusbordihn.easymodelentities.runtime;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record EasyModelRuntimeContract(
    Identifier profileId,
    Identifier renderProfileId,
    String version,
    float width,
    float height,
    float eyeHeight,
    ModelBodyType bodyType,
    EasyModelAnimationState animationState) {

  public EasyModelRuntimeContract {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(renderProfileId, "renderProfileId");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(bodyType, "bodyType");
    Objects.requireNonNull(animationState, "animationState");
  }

  public static EasyModelRuntimeContract fromProfile(
      EasyModelEntityProfile profile, EasyModelAnimationState animationState) {
    Objects.requireNonNull(profile, "profile");
    return new EasyModelRuntimeContract(
        profile.id(),
        profile.renderProfileId(),
        profile.version(),
        profile.width(),
        profile.height(),
        profile.eyeHeight(),
        profile.bodyType(),
        animationState);
  }

  public static EasyModelRuntimeContract fromRenderProfile(
      Identifier profileId,
      EasyModelRenderProfile renderProfile,
      EasyModelAnimationState animationState) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(renderProfile, "renderProfile");
    EasyModelRuntimeContract fallback = fallback(profileId, animationState);
    return new EasyModelRuntimeContract(
        profileId,
        renderProfile.id(),
        renderProfile.version(),
        fallback.width(),
        fallback.height(),
        fallback.eyeHeight(),
        renderProfile.bodyType(),
        animationState);
  }

  public static EasyModelRuntimeContract fallback(Identifier profileId) {
    return fallback(profileId, EasyModelAnimationState.AUTO);
  }

  public static EasyModelRuntimeContract fallback(String profileId) {
    return fallback(Identifier.tryParse(Objects.requireNonNullElse(profileId, "")));
  }

  public static EasyModelRuntimeContract fallback(
      Identifier profileId, EasyModelAnimationState animationState) {
    Identifier fallbackProfileId =
        profileId == null
            ? Identifier.fromNamespaceAndPath(Constants.MOD_ID, "missing")
            : profileId;
    return new EasyModelRuntimeContract(
        fallbackProfileId,
        fallbackProfileId,
        "",
        0.6f,
        1.8f,
        1.62f,
        ModelBodyType.STATIC,
        animationState);
  }
}
