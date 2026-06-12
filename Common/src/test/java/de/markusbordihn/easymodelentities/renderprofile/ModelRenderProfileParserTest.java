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

package de.markusbordihn.easymodelentities.renderprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import java.io.StringReader;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ModelRenderProfileParserTest {

  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "lizard");

  @Test
  void parsesValidRenderProfile() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse(RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE);

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertTrue(renderProfile.isActive());
    assertEquals(RENDER_PROFILE_ID, renderProfile.id());
    assertEquals("0.1.0", renderProfile.schemaVersion());
    assertEquals("2cbb2c6e-4f28-4f1d-b9f7-0d8c1f63d24a", renderProfile.pairId());
    assertEquals("sha256:abc123", renderProfile.assetFingerprint());
    assertEquals(ModelBodyType.QUADRUPED, renderProfile.bodyType());
    assertEquals(
        new ResourceLocation("example", "easy_model_entities/models/lizard"),
        renderProfile.model());
    assertEquals(
        new ResourceLocation("example", "textures/entity/lizard.png"), renderProfile.texture());
    assertEquals(1.25f, renderProfile.scale());
    assertEquals(0.4f, renderProfile.shadowRadius());
    assertEquals(ModelAnimationMode.AUTOMATIC, renderProfile.animation().mode());
    assertEquals(1.1f, renderProfile.animation().swingSpeed());
    assertEquals(0.9f, renderProfile.animation().walkSpeedMultiplier());
  }

  @Test
  void parsesVisibleBounds() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse(RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE);

    assertEquals(1.2f, renderProfile.rendering().visibleBoundsWidth());
    assertEquals(1.0f, renderProfile.rendering().visibleBoundsHeight());
    assertEquals(0.0f, renderProfile.rendering().visibleBoundsOffsetX());
    assertEquals(0.5f, renderProfile.rendering().visibleBoundsOffsetY());
    assertEquals(0.0f, renderProfile.rendering().visibleBoundsOffsetZ());
  }

  @Test
  void defaultsOptionalSettings() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/minimal_lizard.json");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals("", renderProfile.pairId());
    assertEquals("", renderProfile.assetFingerprint());
    assertEquals(1.0f, renderProfile.scale());
    assertEquals(0.3f, renderProfile.shadowRadius());
    assertEquals(1.0f, renderProfile.rendering().visibleBoundsWidth());
    assertEquals(1.0f, renderProfile.rendering().visibleBoundsHeight());
    assertEquals(0.5f, renderProfile.rendering().visibleBoundsOffsetY());
    assertEquals(ModelAnimationMode.AUTOMATIC, renderProfile.animation().mode());
    assertEquals(1.0f, renderProfile.animation().swingSpeed());
    assertEquals(1.0f, renderProfile.animation().walkSpeedMultiplier());
  }

  @Test
  void rejectsInvalidSchemaVersion() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_schema_version.json");

    assertEquals(ModelRenderProfileStatus.INVALID_SCHEMA_VERSION, renderProfile.status());
    assertFalse(renderProfile.isActive());
  }

  @Test
  void rejectsInvalidRenderProfileId() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_id.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsPathIdMismatch() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/path_id_mismatch.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsInvalidBodyType() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_body_type.json");

    assertEquals(ModelRenderProfileStatus.INVALID_BODY_TYPE, renderProfile.status());
  }

  @Test
  void rejectsInvalidModelId() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_model_id.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsInvalidTextureId() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_texture_id.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsInvalidAnimationMode() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_animation_mode.json");

    assertEquals(ModelRenderProfileStatus.INVALID_ANIMATION_MODE, renderProfile.status());
  }

  @Test
  void malformedJsonProducesInvalidJsonStatus() {
    EasyModelRenderProfile renderProfile =
        ModelRenderProfileParser.parse(RENDER_PROFILE_ID, new StringReader("{"));

    assertEquals(ModelRenderProfileStatus.INVALID_JSON, renderProfile.status());
    assertFalse(renderProfile.validationIssues().isEmpty());
  }

  @Test
  void unknownFieldsAreIgnoredSafely() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/unknown_fields.json");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
  }
}
