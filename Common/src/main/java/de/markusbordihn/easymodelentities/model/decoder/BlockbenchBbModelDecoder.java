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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.decoder;

import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class BlockbenchBbModelDecoder implements EasyModelDecoder {

  public static final String FORMAT = "bbmodel";
  public static final int MAX_MODEL_FILE_SIZE_BYTES = ModelAssetBudgets.MAX_MODEL_FILE_SIZE_BYTES;
  public static final int MAX_TEXTURE_SIZE = ModelAssetBudgets.MAX_TEXTURE_SIZE;
  public static final int MAX_BONE_COUNT = ModelAssetBudgets.MAX_BONE_COUNT;
  public static final int MAX_CUBE_COUNT = ModelAssetBudgets.MAX_CUBE_COUNT;
  public static final int MAX_HIERARCHY_DEPTH = ModelAssetBudgets.MAX_HIERARCHY_DEPTH;

  private final AbstractBbModelParser moddedEntityParser = new ModdedEntityBbModelParser();
  private final AbstractBbModelParser emeEntityParser = new EmeEntityBbModelParser();

  @Override
  public boolean supports(Identifier modelId, Resource resource) {
    return modelId != null && resource != null;
  }

  @Override
  public DecodedModel decode(Identifier modelId, Resource resource)
      throws EasyModelDecodeException {
    Objects.requireNonNull(modelId, "modelId");
    Objects.requireNonNull(resource, "resource");

    byte[] modelBytes = AbstractBbModelParser.readModelBytes(resource);
    JsonObject root = AbstractBbModelParser.parseRoot(modelId, modelBytes);
    String modelFormat = AbstractBbModelParser.modelFormat(root);
    AbstractBbModelParser parser = parserForFormat(modelFormat);
    return parser.parse(modelId, root, modelBytes.length);
  }

  private AbstractBbModelParser parserForFormat(String modelFormat)
      throws EasyModelDecodeException {
    if (ModdedEntityBbModelParser.MODEL_FORMAT.equals(modelFormat)) {
      return this.moddedEntityParser;
    }
    if (EmeEntityBbModelParser.MODEL_FORMAT.equals(modelFormat)) {
      return this.emeEntityParser;
    }

    throw new EasyModelDecodeException("Unsupported Blockbench model_format " + modelFormat + ".");
  }
}
