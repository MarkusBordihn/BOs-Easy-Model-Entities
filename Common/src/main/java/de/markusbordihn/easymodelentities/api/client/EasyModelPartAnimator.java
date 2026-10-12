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

import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import java.util.Objects;
import net.minecraft.util.Mth;

@FunctionalInterface
public interface EasyModelPartAnimator {

  EasyModelPartAnimator NONE = context -> EasyModelPartTransform.NONE;

  static EasyModelPartAnimator partLook(String partName, float yaw, float pitch) {
    String lookPartName = Objects.requireNonNull(partName, "partName").trim();
    if (lookPartName.isEmpty()) {
      throw new IllegalArgumentException("partName must not be blank.");
    }
    if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
      throw new IllegalArgumentException("Part look angles must be finite.");
    }

    EasyModelPartTransform lookTransform =
        new EasyModelPartTransform(
            Mth.wrapDegrees(pitch) * Mth.DEG_TO_RAD, Mth.wrapDegrees(yaw) * Mth.DEG_TO_RAD, 0.0f);
    return context ->
        context.partName().equalsIgnoreCase(lookPartName)
            ? lookTransform
            : EasyModelPartTransform.NONE;
  }

  EasyModelPartTransform animate(EasyModelPartAnimationContext context);

  default EasyModelPartAnimator andThen(EasyModelPartAnimator next) {
    Objects.requireNonNull(next, "next");
    return context ->
        Objects.requireNonNull(this.animate(context), "partAnimator result")
            .add(Objects.requireNonNull(next.animate(context), "partAnimator result"));
  }
}
