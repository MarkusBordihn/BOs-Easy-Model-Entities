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

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBlockEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.client.render.EasyModelBlockEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelBlockEntityRenderDelegate<T extends BlockEntity & EasyModelRenderable> {

  private final Map<T, RandomIdleState> randomIdleStates = new WeakHashMap<>();

  EasyModelBlockEntityRenderDelegate() {}

  public void render(
      T blockEntity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        blockEntity,
        partialTick,
        poseStack,
        bufferSource,
        packedLight,
        EasyModelBlockEntityRenderOptions.DEFAULT);
  }

  public void render(
      T blockEntity,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      EasyModelBlockEntityRenderOptions options) {
    EasyModelRenderState renderState =
        EasyModelBlockEntityRenderBackend.resolveRenderState(contract(blockEntity));
    EasyModelBlockEntityRenderOptions resolvedOptions =
        resolveOptions(blockEntity, renderState, partialTick, options);
    EasyModelBlockEntityRenderBackend.render(
        blockEntity,
        renderState,
        partialTick,
        resolvedOptions,
        poseStack,
        bufferSource,
        packedLight);
  }

  public List<EasyModelPartDefinition> rootModelParts(T blockEntity) {
    EasyModelRenderState renderState =
        EasyModelBlockEntityRenderBackend.resolveRenderState(contract(blockEntity));
    return renderState.bakedModel().rootParts().stream()
        .map(EasyModelPartDefinitions::fromBakedPart)
        .toList();
  }

  public List<EasyModelPartDefinition> modelParts(T blockEntity) {
    return EasyModelPartDefinitions.flatten(rootModelParts(blockEntity));
  }

  private EasyModelRuntimeContract contract(T blockEntity) {
    return EasyModelBlockEntityRenderBackend.runtimeContract(
        blockEntity, EasyModelServices.profileService(), EasyModelServices.renderProfileService());
  }

  private EasyModelBlockEntityRenderOptions resolveOptions(
      T blockEntity,
      EasyModelRenderState renderState,
      float partialTick,
      EasyModelBlockEntityRenderOptions options) {
    EasyModelBlockEntityRenderOptions safeOptions =
        options == null ? EasyModelBlockEntityRenderOptions.DEFAULT : options;
    if (safeOptions.animationTicks() != null
        || renderState.animation().mode() != ModelAnimationMode.RANDOM_IDLE) {
      return safeOptions;
    }

    return safeOptions.withAnimationTicks(randomIdleAnimationTicks(blockEntity, partialTick));
  }

  private float randomIdleAnimationTicks(T blockEntity, float partialTick) {
    if (blockEntity.getLevel() == null) {
      return 0.0f;
    }

    return this.randomIdleStates
        .computeIfAbsent(blockEntity, key -> new RandomIdleState())
        .animationTicks(
            blockEntity.getLevel().getGameTime(), blockEntity.getLevel().getRandom(), partialTick);
  }

  private static class RandomIdleState {
    private long nextStartTick = Long.MIN_VALUE;
    private long startTick = Long.MIN_VALUE;
    private long endTick = Long.MIN_VALUE;

    private static int randomIdleDelay(RandomSource random) {
      return EasyModelHostBlockEntity.RANDOM_IDLE_MIN_GAP
          + random.nextInt(EasyModelHostBlockEntity.RANDOM_IDLE_GAP_RANGE);
    }

    float animationTicks(long gameTime, RandomSource random, float partialTick) {
      if (this.nextStartTick == Long.MIN_VALUE) {
        this.nextStartTick = gameTime + randomIdleDelay(random);
      }
      if (gameTime >= this.endTick && gameTime >= this.nextStartTick) {
        this.startTick = gameTime;
        this.endTick = gameTime + EasyModelHostBlockEntity.RANDOM_IDLE_BURST_LENGTH;
        this.nextStartTick = this.endTick + randomIdleDelay(random);
      }
      if (gameTime < this.endTick) {
        return gameTime - this.startTick + partialTick;
      }

      return 0.0f;
    }
  }
}
