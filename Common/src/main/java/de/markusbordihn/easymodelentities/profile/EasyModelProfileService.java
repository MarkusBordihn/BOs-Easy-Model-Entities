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

package de.markusbordihn.easymodelentities.profile;

import de.markusbordihn.easymodelentities.data.diagnostics.ModelResourceRejection;
import de.markusbordihn.easymodelentities.data.profile.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface EasyModelProfileService {

  EasyModelProfileService EMPTY = new EasyModelProfileService() {};

  default Collection<ModelResourceRejection> getRejectedResources() {
    return List.of();
  }

  default boolean hasProfile(Identifier profileId) {
    return getProfile(profileId).isPresent();
  }

  default Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
    return Optional.empty();
  }

  default Collection<Identifier> getProfileIds() {
    return getProfiles().stream().map(EasyModelEntityProfile::id).toList();
  }

  default Collection<EasyModelEntityProfile> getProfiles() {
    return List.of();
  }

  default Collection<EasyModelEntityProfile> getActiveProfiles() {
    return getProfiles().stream().filter(EasyModelEntityProfile::isActive).toList();
  }

  default Collection<ModelProfileValidationIssue> getValidationIssues(Identifier profileId) {
    return getProfile(profileId).map(EasyModelEntityProfile::validationIssues).orElseGet(List::of);
  }

  default boolean isActive(Identifier profileId) {
    return getProfile(profileId).map(EasyModelEntityProfile::isActive).orElse(false);
  }
}
