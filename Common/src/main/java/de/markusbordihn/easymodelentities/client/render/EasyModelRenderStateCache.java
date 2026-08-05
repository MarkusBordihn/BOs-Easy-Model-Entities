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

import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.render.EasyModelRenderStateResolver;
import de.markusbordihn.easymodelentities.runtime.AssetPairing;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class EasyModelRenderStateCache {

  private static final int MAX_CACHE_ENTRIES = 4096;
  private static final Map<RenderStateKey, EasyModelRenderState> RENDER_STATES =
      leastRecentlyUsedCache();
  private static final Map<EasyModelRuntimeContract, EasyModelRenderState> BY_CONTRACT =
      leastRecentlyUsedCache();

  static {
    EasyModelReloadDispatcher.addProfileReloadListener(EasyModelRenderStateCache::clear);
    EasyModelReloadDispatcher.addRenderProfileReloadListener(EasyModelRenderStateCache::clear);
  }

  private EasyModelRenderStateCache() {}

  private static <K> Map<K, EasyModelRenderState> leastRecentlyUsedCache() {
    return Collections.synchronizedMap(
        new LinkedHashMap<>(256, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<K, EasyModelRenderState> eldest) {
            return size() > MAX_CACHE_ENTRIES;
          }
        });
  }

  public static EasyModelRenderState resolve(EasyModelRuntimeContract contract) {
    Objects.requireNonNull(contract, "contract");
    EasyModelRenderState contractState = BY_CONTRACT.get(contract);
    if (contractState != null) {
      return contractState;
    }

    RenderStateKey key = keyOf(contract);
    EasyModelRenderState renderState = RENDER_STATES.get(key);
    if (renderState == null) {
      renderState =
          EasyModelRenderStateResolver.resolve(
              contract,
              EasyModelServices.renderProfileService(),
              EasyModelServices.bakeService(),
              Minecraft.getInstance().getResourceManager());
      RENDER_STATES.put(key, renderState);
    }
    BY_CONTRACT.put(contract, renderState);
    return renderState;
  }

  private static void clear() {
    BY_CONTRACT.clear();
    RENDER_STATES.clear();
  }

  static RenderStateKey keyOf(EasyModelRuntimeContract contract) {
    boolean activeRenderProfile =
        EasyModelServices.renderProfileService()
            .getRenderProfile(contract.renderProfileId())
            .filter(EasyModelRenderProfile::isRenderable)
            .filter(renderProfile -> renderProfile.bodyType() == contract.bodyType())
            .filter(
                renderProfile -> AssetPairing.matches(contract.version(), renderProfile.version()))
            .isPresent();
    return new RenderStateKey(
        contract.renderProfileId(),
        contract.version(),
        contract.bodyType(),
        activeRenderProfile ? 0.0f : contract.width(),
        activeRenderProfile ? 0.0f : contract.height());
  }

  record RenderStateKey(
      Identifier renderProfileId,
      String version,
      ModelBodyType bodyType,
      float fallbackWidth,
      float fallbackHeight) {}
}
