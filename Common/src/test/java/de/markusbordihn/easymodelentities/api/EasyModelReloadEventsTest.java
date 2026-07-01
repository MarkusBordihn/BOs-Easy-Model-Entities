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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents.Listener;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class EasyModelReloadEventsTest {

  @Test
  void profileReloadInvokesRegisteredListeners() {
    AtomicInteger calls = new AtomicInteger();
    Listener listener = calls::incrementAndGet;
    EasyModelReloadEvents.onProfileReload(listener);
    try {
      EasyModelReloadDispatcher.fireProfileReload();
      EasyModelReloadDispatcher.fireProfileReload();
      assertEquals(2, calls.get());
    } finally {
      EasyModelReloadEvents.removeProfileReloadListener(listener);
    }
  }

  @Test
  void removedListenerNoLongerInvoked() {
    AtomicInteger calls = new AtomicInteger();
    Listener listener = calls::incrementAndGet;
    EasyModelReloadEvents.onProfileReload(listener);
    assertTrue(EasyModelReloadEvents.removeProfileReloadListener(listener));
    EasyModelReloadDispatcher.fireProfileReload();
    assertEquals(0, calls.get());
  }

  @Test
  void profileAndRenderChannelsAreIndependent() {
    AtomicInteger profileCalls = new AtomicInteger();
    AtomicInteger renderCalls = new AtomicInteger();
    Listener profileListener = profileCalls::incrementAndGet;
    Listener renderListener = renderCalls::incrementAndGet;
    EasyModelReloadEvents.onProfileReload(profileListener);
    EasyModelReloadEvents.onRenderProfileReload(renderListener);
    try {
      EasyModelReloadDispatcher.fireProfileReload();
      assertEquals(1, profileCalls.get());
      assertEquals(0, renderCalls.get());
      EasyModelReloadDispatcher.fireRenderProfileReload();
      assertEquals(1, profileCalls.get());
      assertEquals(1, renderCalls.get());
    } finally {
      EasyModelReloadEvents.removeProfileReloadListener(profileListener);
      EasyModelReloadEvents.removeRenderProfileReloadListener(renderListener);
    }
  }

  @Test
  void failingListenerDoesNotStopOtherListeners() {
    AtomicInteger calls = new AtomicInteger();
    Listener failing =
        () -> {
          throw new IllegalStateException("boom");
        };
    Listener healthy = calls::incrementAndGet;
    EasyModelReloadEvents.onProfileReload(failing);
    EasyModelReloadEvents.onProfileReload(healthy);
    try {
      EasyModelReloadDispatcher.fireProfileReload();
      assertEquals(1, calls.get());
    } finally {
      EasyModelReloadEvents.removeProfileReloadListener(failing);
      EasyModelReloadEvents.removeProfileReloadListener(healthy);
    }
  }

  @Test
  void registeringNullListenerThrows() {
    assertThrows(NullPointerException.class, () -> EasyModelReloadEvents.onProfileReload(null));
    assertThrows(
        NullPointerException.class, () -> EasyModelReloadEvents.onRenderProfileReload(null));
  }
}
