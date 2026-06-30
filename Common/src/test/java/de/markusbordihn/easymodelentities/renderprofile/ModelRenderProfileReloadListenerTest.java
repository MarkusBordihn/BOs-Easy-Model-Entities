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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents;
import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents.Listener;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ModelRenderProfileReloadListenerTest {

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void applyPublishesServicesAndFiresRenderReloadEvent() {
    ModelRenderProfileReloadListener listener = new ModelRenderProfileReloadListener();
    ModelDecoderRegistry decoderRegistry = ModelDecoderRegistry.createDefault();
    ModelBakeService bakeService = new ModelBakeService(decoderRegistry);
    ModelRenderProfileManager renderProfileManager = new ModelRenderProfileManager(Map.of());
    ModelRenderProfileReloadListener.ReloadState reloadState =
        new ModelRenderProfileReloadListener.ReloadState(
            renderProfileManager, decoderRegistry, bakeService);
    AtomicReference<Object> renderServiceInCallback = new AtomicReference<>();
    Listener reloadListener =
        () -> renderServiceInCallback.set(EasyModelServices.renderProfileService());
    EasyModelReloadEvents.onRenderProfileReload(reloadListener);

    try {
      listener.apply(reloadState, mock(ResourceManager.class), mock(ProfilerFiller.class));

      assertSame(renderProfileManager, EasyModelServices.renderProfileService());
      assertSame(bakeService, EasyModelServices.bakeService());
      assertSame(decoderRegistry, EasyModelServices.decoderRegistry());
      assertSame(renderProfileManager, renderServiceInCallback.get());
    } finally {
      assertTrue(EasyModelReloadEvents.removeRenderProfileReloadListener(reloadListener));
    }
  }
}
