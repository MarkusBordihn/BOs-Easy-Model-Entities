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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.profile.*;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EasyModelProfileManagerTest {

  @Test
  void derivesEntityProfileIdFromTypedDataPath() {
    Optional<ResourceLocation> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            ResourceLocation.fromNamespaceAndPath(
                "example", "easy_model_entities/profiles/entity/lizard.json"));

    assertEquals(
        Optional.of(ResourceLocation.fromNamespaceAndPath("example", "entity/lizard")), profileId);
  }

  @Test
  void derivesBlockEntityProfileIdFromTypedDataPath() {
    Optional<ResourceLocation> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            ResourceLocation.fromNamespaceAndPath(
                "example", "easy_model_entities/profiles/block_entity/lizard.json"));

    assertEquals(
        Optional.of(ResourceLocation.fromNamespaceAndPath("example", "block_entity/lizard")),
        profileId);
  }

  @Test
  void ignoresProfilesOutsideTypedDataPaths() {
    Optional<ResourceLocation> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            ResourceLocation.fromNamespaceAndPath(
                "example", "easy_model_entities/profiles/lizard.json"));

    assertTrue(profileId.isEmpty());
  }
}
