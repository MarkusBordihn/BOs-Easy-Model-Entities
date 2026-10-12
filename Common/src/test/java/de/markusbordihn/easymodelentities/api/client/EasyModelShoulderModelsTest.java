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

package de.markusbordihn.easymodelentities.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelShoulderModel;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelShoulderModelsTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "entity/sparrow");
  private static final ResourceLocation FAMILIAR_TYPE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "familiar");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static CompoundTag entityTag(ResourceLocation entityTypeId) {
    CompoundTag entityTag = new CompoundTag();
    entityTag.putString("id", entityTypeId.toString());
    return entityTag;
  }

  @Test
  @DisplayName("A seated host entity resolves to its profile")
  void hostEntityResolvesToItsProfile() {
    CompoundTag entityTag = entityTag(ModelEntityTypeIds.GROUND_ENTITY);
    entityTag.putString(EasyModelHostPersistence.PROFILE_ID_TAG, PROFILE_ID.toString());

    Optional<EasyModelShoulderModel> shoulderModel = EasyModelShoulderModels.resolve(entityTag);

    assertTrue(shoulderModel.isPresent());
    assertEquals(PROFILE_ID, shoulderModel.get().profileId());
    assertEquals(EasyModelEntityRenderOptions.DEFAULT, shoulderModel.get().options());
  }

  @Test
  @DisplayName("A host entity without profile is not shown on the shoulder")
  void hostEntityWithoutProfileResolvesToNothing() {
    assertTrue(
        EasyModelShoulderModels.resolve(entityTag(ModelEntityTypeIds.GROUND_ENTITY)).isEmpty());
  }

  @Test
  @DisplayName("A parrot or any other unregistered type is left to its own renderer")
  void unregisteredEntityTypeResolvesToNothing() {
    assertTrue(
        EasyModelShoulderModels.resolve(
                entityTag(ResourceLocation.fromNamespaceAndPath("minecraft", "parrot")))
            .isEmpty());
    assertTrue(EasyModelShoulderModels.resolve(new CompoundTag()).isEmpty());
  }

  @Test
  @DisplayName("A registered resolver handles its own entity type")
  void registeredResolverHandlesItsEntityType() {
    EasyModelShoulderModels.register(
        FAMILIAR_TYPE_ID, entityTag -> Optional.of(EasyModelShoulderModel.of(PROFILE_ID)));

    assertEquals(
        Optional.of(EasyModelShoulderModel.of(PROFILE_ID)),
        EasyModelShoulderModels.resolve(entityTag(FAMILIAR_TYPE_ID)));
  }
}
