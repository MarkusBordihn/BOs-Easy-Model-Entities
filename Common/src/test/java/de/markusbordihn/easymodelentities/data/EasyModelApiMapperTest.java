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

package de.markusbordihn.easymodelentities.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileType;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBounds;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelApiMapperTest {

  @Test
  @DisplayName("Every internal body type needs a public counterpart")
  void everyBodyTypeMapsToAnApiBodyType() {
    Arrays.stream(ModelBodyType.values())
        .forEach(
            bodyType ->
                assertEquals(
                    bodyType.getSerializedName(),
                    EasyModelApiMapper.bodyType(bodyType).getSerializedName()));
  }

  @Test
  void everyModelTypeMapsToAnApiProfileType() {
    Arrays.stream(ModelType.values())
        .forEach(
            modelType ->
                assertEquals(
                    modelType.getSerializedName(),
                    EasyModelApiMapper.profileType(modelType).getSerializedName()));
  }

  @Test
  void boundsKeepTheirCornersWhenMapped() {
    EasyModelBounds bounds =
        EasyModelApiMapper.bounds(
            new ModelBounds(new Vec3f(-1.0f, 0.0f, -2.0f), new Vec3f(1.0f, 3.0f, 2.0f)));

    assertEquals(-1.0f, bounds.min().x());
    assertEquals(3.0f, bounds.max().y());
  }

  @Test
  void rejectsNullInput() {
    assertThrows(NullPointerException.class, () -> EasyModelApiMapper.bodyType(null));
    assertThrows(NullPointerException.class, () -> EasyModelApiMapper.profileType(null));
    assertThrows(NullPointerException.class, () -> EasyModelApiMapper.bounds(null));
    assertThrows(NullPointerException.class, () -> EasyModelApiMapper.vector(null));
  }

  @Test
  void apiEnumsDoNotDeclareValuesTheMapperCannotProduce() {
    assertEquals(ModelBodyType.values().length, EasyModelBodyType.values().length);
    assertEquals(ModelType.values().length, EasyModelProfileType.values().length);
  }
}
