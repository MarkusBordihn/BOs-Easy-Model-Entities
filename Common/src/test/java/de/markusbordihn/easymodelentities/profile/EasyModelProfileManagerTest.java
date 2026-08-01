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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.diagnostics.ModelResourceRejection;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelProfileManagerTest {

  private static final String PROFILE_JSON =
      "{\"schema_version\":\"0.2.0\",\"model_type\":\"entity\",\"preset_type\":\"statue\"}";

  private static ResourceManager resourceManager(Identifier... profileResources) {
    ResourceManager resourceManager = mock(ResourceManager.class);
    Map<Identifier, Resource> resources =
        Arrays.stream(profileResources)
            .collect(
                Collectors.toMap(
                    resourceLocation -> resourceLocation,
                    resourceLocation -> resource(PROFILE_JSON)));
    when(resourceManager.listResources(eq(ModelResourcePaths.SERVER_PROFILE_DIRECTORY), any()))
        .thenReturn(resources);
    return resourceManager;
  }

  private static Resource resource(String json) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(
        packResources, () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void derivesEntityProfileIdFromTypedDataPath() {
    Optional<Identifier> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            Identifier.fromNamespaceAndPath(
                "example", "easy_model_entities/profiles/entity/lizard.json"));

    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "entity/lizard")), profileId);
  }

  @Test
  void derivesBlockEntityProfileIdFromTypedDataPath() {
    Optional<Identifier> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            Identifier.fromNamespaceAndPath(
                "example", "easy_model_entities/profiles/block_entity/lizard.json"));

    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "block_entity/lizard")), profileId);
  }

  @Test
  void ignoresProfilesOutsideTypedDataPaths() {
    Optional<Identifier> profileId =
        EasyModelProfileManager.profileIdFromResourceLocation(
            Identifier.fromNamespaceAndPath("example", "easy_model_entities/profiles/lizard.json"));

    assertTrue(profileId.isEmpty());
  }

  @Test
  @DisplayName("A profile outside of the typed folders is reported instead of vanishing")
  void ignoredProfilesAreReported() {
    Identifier misplacedResource =
        Identifier.fromNamespaceAndPath("example", "easy_model_entities/profiles/lizard.json");
    Identifier loadedResource =
        Identifier.fromNamespaceAndPath(
            "example", "easy_model_entities/profiles/entity/lizard.json");

    EasyModelProfileManager manager =
        EasyModelProfileManager.load(resourceManager(misplacedResource, loadedResource));

    assertEquals(1, manager.getProfileIds().size());
    assertEquals(1, manager.getRejectedResources().size());
    ModelResourceRejection rejection = manager.getRejectedResources().iterator().next();
    assertEquals(misplacedResource, rejection.resource());
    assertTrue(rejection.reason().contains("entity/"));
  }
}
