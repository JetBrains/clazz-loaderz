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

import java.io.IOException;
import java.io.InputStream;

/**
 * Created 26.07.13 12:14
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class CompositeEntry implements ResourceEntry {
  private final ResourceEntry myEntry;
  private final ResourceEntry myNextEntry;

  public CompositeEntry(@NotNull final ResourceEntry entry,
                        @NotNull final ResourceEntry nextEntry) {
    myEntry = entry;
    myNextEntry = nextEntry;
  }

  @Override
  @NotNull
  public byte[] getBytes() throws IOException {
    return myEntry.getBytes();
  }

  @Override
  @NotNull
  public InputStream getStream() throws IOException {
    return myEntry.getStream();
  }

  @NotNull
  @Override
  public ResourceEntry getNextEntry() {
    return myNextEntry;
  }
}
