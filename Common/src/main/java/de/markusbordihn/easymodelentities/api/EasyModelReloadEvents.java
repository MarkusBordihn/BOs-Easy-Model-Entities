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

import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;

public final class EasyModelReloadEvents {

  private EasyModelReloadEvents() {}

  public static void onProfileReload(Listener listener) {
    EasyModelReloadDispatcher.addProfileReloadListener(listener);
  }

  public static boolean removeProfileReloadListener(Listener listener) {
    return EasyModelReloadDispatcher.removeProfileReloadListener(listener);
  }

  public static void onRenderProfileReload(Listener listener) {
    EasyModelReloadDispatcher.addRenderProfileReloadListener(listener);
  }

  public static boolean removeRenderProfileReloadListener(Listener listener) {
    return EasyModelReloadDispatcher.removeRenderProfileReloadListener(listener);
  }

  @FunctionalInterface
  public interface Listener {
    void onReload();
  }
}
