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

import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EasyModelHostPersistence {

  public static final String PROFILE_ID_TAG = "ProfileId";
  public static final String RENDER_PROFILE_ID_TAG = "RenderProfileId";
  public static final String VERSION_TAG = "Version";
  public static final String BODY_TYPE_TAG = "BodyType";
  public static final String ANIMATION_STATE_TAG = "AnimationState";

  private EasyModelHostPersistence() {}

  public static Identifier parseResourceLocation(String idText) {
    return idText == null || idText.isBlank() ? null : Identifier.tryParse(idText);
  }

  public static Identifier parseResourceLocationOrMissing(String idText, Identifier missing) {
    Identifier parsedId = parseResourceLocation(idText);
    return parsedId == null ? missing : parsedId;
  }

  public static State read(ValueInput valueInput) {
    return new State(
        parseResourceLocation(valueInput.getStringOr(PROFILE_ID_TAG, null)),
        parseResourceLocation(valueInput.getStringOr(RENDER_PROFILE_ID_TAG, null)),
        valueInput.getStringOr(VERSION_TAG, ""),
        ModelBodyType.bySerializedName(valueInput.getStringOr(BODY_TYPE_TAG, "")),
        EasyModelAnimationState.bySerializedName(valueInput.getStringOr(ANIMATION_STATE_TAG, "")));
  }

  public static void write(
      ValueOutput valueOutput,
      Identifier profileId,
      Identifier renderProfileId,
      String version,
      ModelBodyType bodyType,
      EasyModelAnimationState animationState) {
    if (profileId != null) {
      valueOutput.putString(PROFILE_ID_TAG, profileId.toString());
    }
    if (renderProfileId != null) {
      valueOutput.putString(RENDER_PROFILE_ID_TAG, renderProfileId.toString());
    }
    valueOutput.putString(VERSION_TAG, version != null ? version : "");
    valueOutput.putString(BODY_TYPE_TAG, bodyType != null ? bodyType.getSerializedName() : "");
    valueOutput.putString(
        ANIMATION_STATE_TAG, animationState != null ? animationState.getSerializedName() : "");
  }

  public record State(
      Identifier profileId,
      Identifier renderProfileId,
      String version,
      ModelBodyType bodyType,
      EasyModelAnimationState animationState) {}
}
