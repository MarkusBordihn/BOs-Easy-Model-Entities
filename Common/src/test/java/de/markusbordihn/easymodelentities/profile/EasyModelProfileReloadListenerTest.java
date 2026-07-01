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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents;
import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents.Listener;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EasyModelProfileReloadListenerTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "fox");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static EasyModelEntityProfile activeProfile(ResourceLocation profileId) {
    return new EasyModelEntityProfile(
        profileId,
        "0.1.0",
        "server-v1",
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY, ModelMovementType.GROUND, ModelBodyType.QUADRUPED),
        null,
        new ModelClientSettings(profileId),
        new ModelDimensions(0.6f, 0.8f, 0.5f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void applyPublishesCatalogAndFiresProfileReloadEvent() {
    EasyModelProfileReloadListener listener = new EasyModelProfileReloadListener();
    EasyModelProfileManager manager =
        new EasyModelProfileManager(Map.of(PROFILE_ID, activeProfile(PROFILE_ID)));
    AtomicReference<List<ResourceLocation>> catalogInCallback = new AtomicReference<>();
    Listener reloadListener = () -> catalogInCallback.set(EasyModelEntitiesApi.listProfileIds());
    EasyModelReloadEvents.onProfileReload(reloadListener);

    try {
      listener.apply(manager, mock(ResourceManager.class), mock(ProfilerFiller.class));

      assertSame(manager, EasyModelServices.profileService());
      assertEquals(List.of(PROFILE_ID), catalogInCallback.get());
    } finally {
      EasyModelReloadEvents.removeProfileReloadListener(reloadListener);
    }
  }
}
