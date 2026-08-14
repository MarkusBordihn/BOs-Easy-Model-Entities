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

package de.markusbordihn.easymodelentities.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.junit.jupiter.api.Test;

class EasyModelHostFieldsTest {

  @SuppressWarnings("unchecked")
  private static <T> EntityDataAccessor<T> accessor() {
    return mock(EntityDataAccessor.class);
  }

  private static EasyModelHostFields fields() {
    return new EasyModelHostFields(
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor(),
        accessor());
  }

  @Test
  void identifiesOnlyRuntimeContractAccessors() {
    EasyModelHostFields fields = fields();

    assertTrue(fields.isRuntimeContractField(fields.profileId()));
    assertTrue(fields.isRuntimeContractField(fields.renderProfileId()));
    assertTrue(fields.isRuntimeContractField(fields.version()));
    assertTrue(fields.isRuntimeContractField(fields.width()));
    assertTrue(fields.isRuntimeContractField(fields.height()));
    assertTrue(fields.isRuntimeContractField(fields.eyeHeight()));
    assertTrue(fields.isRuntimeContractField(fields.bodyType()));
    assertTrue(fields.isRuntimeContractField(fields.animation()));
    assertFalse(fields.isRuntimeContractField(fields.lookAtPlayers()));
    assertFalse(fields.isRuntimeContractField(fields.randomStroll()));
    assertFalse(fields.isRuntimeContractField(fields.texture()));
    assertFalse(fields.isRuntimeContractField(accessor()));
  }

  @Test
  void identifiesOnlyDimensionAccessors() {
    EasyModelHostFields fields = fields();

    assertTrue(fields.isDimensionsField(fields.width()));
    assertTrue(fields.isDimensionsField(fields.height()));
    assertTrue(fields.isDimensionsField(fields.eyeHeight()));
    assertFalse(fields.isDimensionsField(fields.profileId()));
    assertFalse(fields.isDimensionsField(fields.texture()));
    assertFalse(fields.isDimensionsField(accessor()));
  }

  @Test
  void behaviorChecksReadCachedSynchedValues() {
    EasyModelHostFields fields = fields();
    SynchedEntityData entityData = mock(SynchedEntityData.class);
    when(entityData.get(fields.lookAtPlayers())).thenReturn(true);
    when(entityData.get(fields.randomStroll())).thenReturn(false);

    assertTrue(EasyModelHostSupport.shouldLookAtPlayers(entityData, fields));
    assertFalse(EasyModelHostSupport.shouldRandomStroll(entityData, fields));
  }
}
