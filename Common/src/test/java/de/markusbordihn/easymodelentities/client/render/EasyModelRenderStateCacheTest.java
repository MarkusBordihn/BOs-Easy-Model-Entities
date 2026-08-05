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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EasyModelRenderStateCacheTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "entity/lizard");

  private static EasyModelRuntimeContract contract(float width, float height) {
    return new EasyModelRuntimeContract(
        PROFILE_ID,
        PROFILE_ID,
        "render-v1",
        width,
        height,
        Math.min(1.62f, height),
        ModelBodyType.QUADRUPED,
        EasyModelAnimationSetting.of(EasyModelAnimation.AUTO));
  }

  private static EasyModelRenderProfile renderProfile() {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        Constants.SCHEMA_VERSION,
        "render-v1",
        ModelBodyType.QUADRUPED,
        ResourceLocation.fromNamespaceAndPath("example", "model"),
        ResourceLocation.fromNamespaceAndPath("example", "texture"),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void activeRenderProfileUsesDimensionIndependentCacheKey() {
    EasyModelRenderProfile renderProfile = renderProfile();
    EasyModelServices.setRenderProfileService(
        new EasyModelRenderProfileService() {
          @Override
          public Optional<EasyModelRenderProfile> getRenderProfile(
              ResourceLocation renderProfileId) {
            return PROFILE_ID.equals(renderProfileId)
                ? Optional.of(renderProfile)
                : Optional.empty();
          }
        });

    EasyModelRuntimeContract firstContract = contract(0.6f, 1.8f);
    EasyModelRuntimeContract secondContract = contract(0.9f, 2.1f);

    EasyModelRenderStateCache.RenderStateKey firstKey =
        assertDoesNotThrow(() -> EasyModelRenderStateCache.keyOf(firstContract));
    EasyModelRenderStateCache.RenderStateKey secondKey =
        assertDoesNotThrow(() -> EasyModelRenderStateCache.keyOf(secondContract));

    assertEquals(firstKey, secondKey);
  }

  @Test
  void fallbackCacheKeyPreservesContractDimensions() {
    assertNotEquals(
        EasyModelRenderStateCache.keyOf(contract(0.6f, 1.8f)),
        EasyModelRenderStateCache.keyOf(contract(0.9f, 2.1f)));
  }
}
