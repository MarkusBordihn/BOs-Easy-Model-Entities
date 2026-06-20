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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ModelRenderProfileReloadListener
    extends SimplePreparableReloadListener<ModelRenderProfileReloadListener.ReloadState> {

  public static final Identifier ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "render_profiles");

  @Override
  protected ReloadState prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    ModelDecoderRegistry decoderRegistry = ModelDecoderRegistry.createDefault();
    ModelBakeService bakeService = new ModelBakeService(decoderRegistry);
    return new ReloadState(
        ModelRenderProfileManager.load(resourceManager, bakeService), decoderRegistry, bakeService);
  }

  @Override
  protected void apply(
      ReloadState reloadState, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    EasyModelServices.setDecoderRegistry(reloadState.decoderRegistry());
    EasyModelServices.setBakeService(reloadState.bakeService());
    EasyModelServices.setRenderProfileService(reloadState.renderProfileManager());
  }

  record ReloadState(
      ModelRenderProfileManager renderProfileManager,
      ModelDecoderRegistry decoderRegistry,
      ModelBakeService bakeService) {}
}
