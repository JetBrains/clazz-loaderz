/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.classes.resources.entry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created 26.07.13 12:12
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public abstract class BaseGZipResourceEntry implements ResourceEntry {
  protected final byte[] myData;

  public BaseGZipResourceEntry(@NotNull byte[] data) {
    myData = data;
  }

  @Override
  @NotNull
  public InputStream getStream() throws IOException {
    return new GZIPInputStream(new ByteArrayInputStream(myData));
  }

  @Nullable
  @Override
  public ResourceEntry getNextEntry() {
    return null;
  }
}
