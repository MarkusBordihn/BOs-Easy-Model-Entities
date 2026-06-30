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

package de.markusbordihn.easymodelentities.event;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents.Listener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EasyModelReloadDispatcher {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final List<Listener> PROFILE_RELOAD_LISTENERS = new CopyOnWriteArrayList<>();
  private static final List<Listener> RENDER_PROFILE_RELOAD_LISTENERS =
      new CopyOnWriteArrayList<>();

  private EasyModelReloadDispatcher() {}

  public static void addProfileReloadListener(Listener listener) {
    PROFILE_RELOAD_LISTENERS.add(Objects.requireNonNull(listener, "listener"));
  }

  public static boolean removeProfileReloadListener(Listener listener) {
    return PROFILE_RELOAD_LISTENERS.remove(listener);
  }

  public static void addRenderProfileReloadListener(Listener listener) {
    RENDER_PROFILE_RELOAD_LISTENERS.add(Objects.requireNonNull(listener, "listener"));
  }

  public static boolean removeRenderProfileReloadListener(Listener listener) {
    return RENDER_PROFILE_RELOAD_LISTENERS.remove(listener);
  }

  public static void fireProfileReload() {
    dispatch(PROFILE_RELOAD_LISTENERS);
  }

  public static void fireRenderProfileReload() {
    dispatch(RENDER_PROFILE_RELOAD_LISTENERS);
  }

  private static void dispatch(List<Listener> listeners) {
    for (Listener listener : listeners) {
      try {
        listener.onReload();
      } catch (RuntimeException exception) {
        log.error("Easy Model Entities reload listener {} failed:", listener, exception);
      }
    }
  }
}
