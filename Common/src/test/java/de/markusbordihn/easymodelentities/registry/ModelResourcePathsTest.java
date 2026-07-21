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

package de.markusbordihn.easymodelentities.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ModelResourcePathsTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "lizard");

  @Test
  void resourcePathsMatchSplitArchitecture() {
    assertEquals(
        "data/example/easy_model_entities/profiles/entity/lizard.json",
        ModelResourcePaths.entityServerProfilePath(PROFILE_ID));
    assertEquals(
        "data/example/easy_model_entities/profiles/block_entity/lizard.json",
        ModelResourcePaths.blockEntityServerProfilePath(PROFILE_ID));
    assertEquals(
        "data/example/easy_model_entities/profiles/entity/lizard.json",
        ModelResourcePaths.serverProfilePath(new ResourceLocation("example", "entity/lizard")));
    assertEquals(
        "assets/example/easy_model_entities/render_profiles/lizard.json",
        ModelResourcePaths.renderProfilePath(PROFILE_ID));
    assertEquals(
        "assets/example/easy_model_entities/models/lizard.bbmodel",
        ModelResourcePaths.modelPath(ModelResourcePaths.defaultModelId(PROFILE_ID)));
    assertEquals(
        "assets/example/textures/entity/lizard.png",
        ModelResourcePaths.texturePath(ModelResourcePaths.defaultTextureId(PROFILE_ID)));
  }

  @Test
  void defaultAssetIdsUseProfileNamespace() {
    assertEquals(
        new ResourceLocation("example", "easy_model_entities/models/lizard"),
        ModelResourcePaths.defaultModelId(PROFILE_ID));
    assertEquals(
        new ResourceLocation("example", "textures/entity/lizard.png"),
        ModelResourcePaths.defaultTextureId(PROFILE_ID));
  }

  @Test
  void defaultAssetIdsStripTheModelTypePrefix() {
    assertEquals(
        new ResourceLocation("example", "textures/entity/lizard.png"),
        ModelResourcePaths.defaultTextureId(new ResourceLocation("example", "entity/lizard")));
    assertEquals(
        new ResourceLocation("example", "textures/entity/shrine.png"),
        ModelResourcePaths.defaultTextureId(
            new ResourceLocation("example", "block_entity/shrine")));
    assertEquals(
        new ResourceLocation("example", "easy_model_entities/models/lizard"),
        ModelResourcePaths.defaultModelId(new ResourceLocation("example", "entity/lizard")));
    assertEquals(
        new ResourceLocation("example", "easy_model_entities/models/shrine"),
        ModelResourcePaths.defaultModelId(new ResourceLocation("example", "block_entity/shrine")));
  }

  @Test
  void defaultAssetIdsKeepNestedPathsThatAreNotAModelType() {
    assertEquals(
        new ResourceLocation("example", "textures/entity/mobs/lizard.png"),
        ModelResourcePaths.defaultTextureId(new ResourceLocation("example", "mobs/lizard")));
  }

  @Test
  void fileExtensionsAreNotDuplicated() {
    ResourceLocation modelId =
        new ResourceLocation("example", "easy_model_entities/models/lizard.bbmodel");

    assertEquals(
        "assets/example/easy_model_entities/models/lizard.bbmodel",
        ModelResourcePaths.modelPath(modelId));
  }

  @Test
  void unsafePathSegmentsAreRejected() {
    ResourceLocation unsafeProfileId = new ResourceLocation("example", "models/../lizard");

    assertThrows(
        IllegalArgumentException.class,
        () -> ModelResourcePaths.serverProfilePath(unsafeProfileId));
  }
}
