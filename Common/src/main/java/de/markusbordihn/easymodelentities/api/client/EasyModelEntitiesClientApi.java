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

import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelEntitiesClientApi {

  private EasyModelEntitiesClientApi() {}

  public static <T extends Entity & EasyModelRenderable>
      EasyModelEntityRenderDelegate<T> createRenderDelegate() {
    return new EasyModelEntityRenderDelegate<>();
  }

  public static <T extends BlockEntity & EasyModelRenderable>
      EasyModelBlockEntityRenderDelegate<T> createBlockEntityRenderDelegate() {
    return new EasyModelBlockEntityRenderDelegate<>();
  }

  public static Optional<ModelBounds> getModelBounds(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return EasyModelEntitiesApi.getProfile(profileId)
        .filter(EasyModelEntityProfile::isActive)
        .map(profile -> EasyModelRuntimeContract.fromProfile(profile, EasyModelAnimationState.AUTO))
        .map(EasyModelEntityRenderBackend::resolveRenderState)
        .map(EasyModelRenderState::bakedModel)
        .map(BakedModel::bounds);
  }
}
