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

package de.markusbordihn.easymodelentities.api.data.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.data.model.ModelPartType;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.List;
import org.junit.jupiter.api.Test;

class EasyModelPartDefinitionTest {

  private static EasyModelPartDefinition partDefinition(String name) {
    return new EasyModelPartDefinition(name, Vec3f.ZERO, Vec3f.ZERO, List.of());
  }

  @Test
  void semanticTypeResolvesKnownName() {
    assertEquals(ModelPartType.HEAD, partDefinition("head").semanticType());
    assertEquals(ModelPartType.RIGHT_WING, partDefinition("right_wing").semanticType());
  }

  @Test
  void semanticTypeFallsBackToUnknown() {
    assertEquals(ModelPartType.UNKNOWN, partDefinition("mystery_bone").semanticType());
    assertEquals(ModelPartType.UNKNOWN, partDefinition(null).semanticType());
  }
}
