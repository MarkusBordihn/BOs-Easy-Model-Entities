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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import java.io.StringReader;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EasyModelRuntimeContractTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "lizard");

  @Test
  void fromProfileUsesServerProfileRuntimeFields() {
    EasyModelEntityProfile profile =
        EasyModelProfileParser.parse(
            PROFILE_ID,
            new StringReader(
                """
                {
                  "model_type": "entity",
                  "preset_type": "quadruped_wandering",
                  "version": "v1",
                  "client": {
                    "render_profile": "example:lizard_render"
                  },
                  "dimensions": {
                    "width": 0.6,
                    "height": 0.8,
                    "eye_height": 0.5
                  }
                }
                """));

    EasyModelRuntimeContract contract =
        EasyModelRuntimeContract.fromProfile(profile, EasyModelAnimationState.WALK);

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(new ResourceLocation("example", "lizard_render"), contract.renderProfileId());
    assertEquals("v1", contract.version());
    assertEquals(0.6f, contract.width());
    assertEquals(0.8f, contract.height());
    assertEquals(0.5f, contract.eyeHeight());
    assertEquals(ModelBodyType.QUADRUPED, contract.bodyType());
    assertEquals(EasyModelAnimationState.WALK, contract.animationState());
  }

  @Test
  void fallbackUsesSafeValuesForMissingProfile() {
    EasyModelRuntimeContract contract = EasyModelRuntimeContract.fallback("example:missing");

    assertEquals(new ResourceLocation("example", "missing"), contract.profileId());
    assertEquals(new ResourceLocation("example", "missing"), contract.renderProfileId());
    assertEquals("", contract.version());
    assertEquals(0.6f, contract.width());
    assertEquals(1.8f, contract.height());
    assertEquals(1.62f, contract.eyeHeight());
    assertEquals(ModelBodyType.STATIC, contract.bodyType());
    assertEquals(EasyModelAnimationState.AUTO, contract.animationState());
  }

  @Test
  void fallbackUsesMissingIdForInvalidProfileIdString() {
    EasyModelRuntimeContract contract = EasyModelRuntimeContract.fallback("bad id");

    assertEquals(new ResourceLocation("easy_model_entities", "missing"), contract.profileId());
    assertEquals(
        new ResourceLocation("easy_model_entities", "missing"), contract.renderProfileId());
  }
}
