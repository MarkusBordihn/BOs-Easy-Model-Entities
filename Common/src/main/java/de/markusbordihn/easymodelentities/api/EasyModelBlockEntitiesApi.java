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

package de.markusbordihn.easymodelentities.api;

import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntitiesApi {

  private EasyModelBlockEntitiesApi() {}

  public static Optional<ResourceLocation> getProfileId(BlockEntity blockEntity) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      return Optional.of(hostBlockEntity.getEasyModelProfileId());
    }
    if (blockEntity instanceof EasyModelRenderable renderable) {
      return Optional.ofNullable(renderable.getEasyModelProfileId());
    }

    return Optional.empty();
  }

  public static boolean setProfileId(BlockEntity blockEntity, ResourceLocation profileId) {
    Objects.requireNonNull(blockEntity, "blockEntity");
    Objects.requireNonNull(profileId, "profileId");
    if (blockEntity instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelProfileId(profileId);
      return true;
    }

    return false;
  }
}
