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

package org.jetbrains.classes.resources.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created 26.07.13 14:28
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public abstract class SearchingZipScan<T> extends ZipScan<T, String> {
  @NotNull
  protected abstract T processMatchingItem(@NotNull ZipEntry entry, @NotNull String name, @NotNull ZipInputStream stream) throws IOException;

  @Nullable
  @Override
  protected final T processItem(@NotNull ZipEntry entry, @NotNull String name, @NotNull ZipInputStream stream) throws IOException {
    if (entry.getName().equals(name)) return processMatchingItem(entry, name, stream);
    return null;
  }

  @NotNull
  @Override
  protected final T notFound(@NotNull String name, @NotNull ZipInputStream stream) throws IOException {
    stream.close();
    throw new FileNotFoundException("Failed to find " + name);
  }
}
