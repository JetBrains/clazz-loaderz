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
import org.jetbrains.classes.resources.ResourceHolder;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
* Created 26.07.13 13:22
*
* @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
*/
public abstract class ZipScan<T, P> {
  @NotNull
  public T processEntries(@NotNull final ResourceHolder entry, @NotNull final P p) throws IOException {
    final ZipInputStream myZip = new ZipInputStream(entry.getContent());
    ZipEntry ze;
    while ((ze = myZip.getNextEntry()) != null) {
      if (ze == null) break;
      if (ze.isDirectory()) continue;

      final T t = processItem(ze, p, myZip);
      if (t != null) return t;
    }
    return notFound(p, myZip);
  }

  @Nullable
  protected abstract T processItem(@NotNull ZipEntry entry,
                                   @NotNull P p,
                                   @NotNull ZipInputStream stream) throws IOException;

  @NotNull
  protected abstract T notFound(@NotNull P p,
                                @NotNull ZipInputStream stream) throws IOException;
}
