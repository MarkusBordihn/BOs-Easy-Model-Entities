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

import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.render.EasyModelRenderStateResolver;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

public final class EasyModelRenderStateCache {

  private static final Map<EasyModelRuntimeContract, EasyModelRenderState> CACHE =
      new ConcurrentHashMap<>();

  static {
    EasyModelReloadDispatcher.addRenderProfileReloadListener(CACHE::clear);
  }

  private EasyModelRenderStateCache() {}

  public static EasyModelRenderState resolve(EasyModelRuntimeContract contract) {
    Objects.requireNonNull(contract, "contract");
    EasyModelRuntimeContract key = keyOf(contract);
    EasyModelRenderState renderState = CACHE.get(key);
    if (renderState != null) {
      return renderState;
    }
    return CACHE.computeIfAbsent(
        key,
        cacheKey ->
            EasyModelRenderStateResolver.resolve(
                cacheKey,
                EasyModelServices.renderProfileService(),
                EasyModelServices.bakeService(),
                Minecraft.getInstance().getResourceManager()));
  }

  private static EasyModelRuntimeContract keyOf(EasyModelRuntimeContract contract) {
    return contract.animationState() == EasyModelAnimationState.AUTO
        ? contract
        : new EasyModelRuntimeContract(
            contract.profileId(),
            contract.renderProfileId(),
            contract.version(),
            contract.width(),
            contract.height(),
            contract.eyeHeight(),
            contract.bodyType(),
            EasyModelAnimationState.AUTO);
  }
}
