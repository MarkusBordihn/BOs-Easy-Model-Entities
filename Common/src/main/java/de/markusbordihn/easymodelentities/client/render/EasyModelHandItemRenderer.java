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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartPoseListener;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHandItems;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelItemAnchor;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartPose;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EasyModelHandItemRenderer {

  private EasyModelHandItemRenderer() {}

  public static Pass createPass(BakedModel bakedModel, EasyModelHandItems handItems) {
    if (bakedModel == null || handItems == null || handItems.isEmpty()) {
      return Pass.EMPTY;
    }

    List<AnchoredItem> anchoredItems = new ArrayList<>(2);
    addAnchoredItem(anchoredItems, bakedModel, handItems.mainHandItem(), handItems.mainArm());
    addAnchoredItem(anchoredItems, bakedModel, handItems.offHandItem(), handItems.offArm());
    if (anchoredItems.isEmpty()) {
      return Pass.EMPTY;
    }

    return new Pass(handItems.holder(), List.copyOf(anchoredItems));
  }

  private static void addAnchoredItem(
      List<AnchoredItem> anchoredItems,
      BakedModel bakedModel,
      ItemStack itemStack,
      HumanoidArm arm) {
    if (itemStack.isEmpty()) {
      return;
    }

    Optional<EasyModelItemAnchor> anchor = EasyModelItemAnchorResolver.getItemAnchor(bakedModel, arm);
    anchor.ifPresent(
        resolvedAnchor -> anchoredItems.add(new AnchoredItem(itemStack, arm, resolvedAnchor)));
  }

  private record AnchoredItem(ItemStack itemStack, HumanoidArm arm, EasyModelItemAnchor anchor) {}

  public static final class Pass implements EasyModelPartPoseListener {

    private static final Pass EMPTY = new Pass(null, List.of());

    private final LivingEntity holder;
    private final List<AnchoredItem> anchoredItems;
    private final Map<String, EasyModelPartPose> anchorPoses;

    private Pass(LivingEntity holder, List<AnchoredItem> anchoredItems) {
      this.holder = holder;
      this.anchoredItems = anchoredItems;
      this.anchorPoses = anchoredItems.isEmpty() ? Map.of() : new HashMap<>(anchoredItems.size());
    }

    public boolean isEmpty() {
      return this.anchoredItems.isEmpty();
    }

    @Override
    public boolean wantsPart(String partName) {
      for (AnchoredItem anchoredItem : this.anchoredItems) {
        if (anchoredItem.anchor().partName().equals(partName)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public void onPartPose(EasyModelPartPose partPose) {
      this.anchorPoses.put(partPose.partName(), partPose);
    }

    public void render(
        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      Minecraft minecraft = Minecraft.getInstance();
      Level level = this.holder == null ? minecraft.level : this.holder.level();
      for (AnchoredItem anchoredItem : this.anchoredItems) {
        EasyModelPartPose partPose = this.anchorPoses.get(anchoredItem.anchor().partName());
        if (partPose == null) {
          continue;
        }

        boolean leftHand = anchoredItem.arm() == HumanoidArm.LEFT;
        ItemDisplayContext displayContext =
            leftHand
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        poseStack.pushPose();
        partPose.applyTo(poseStack);
        EasyModelVec3f localOffset = anchoredItem.anchor().localOffset();
        poseStack.translate(
            localOffset.x() / 16.0f, localOffset.y() / 16.0f, localOffset.z() / 16.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        minecraft
            .getItemRenderer()
            .renderStatic(
                this.holder,
                anchoredItem.itemStack(),
                displayContext,
                leftHand,
                poseStack,
                bufferSource,
                level,
                packedLight,
                packedOverlay,
                this.holder == null ? 0 : this.holder.getId() + displayContext.ordinal());
        poseStack.popPose();
      }
    }
  }
}
