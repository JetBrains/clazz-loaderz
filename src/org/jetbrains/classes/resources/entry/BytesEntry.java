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

/**
 * Created 26.07.13 13:11
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class BytesEntry extends BaseEntry {
  private final byte[] myData;

  public BytesEntry(@NotNull final byte[] data) {
    myData = data;
  }

  @NotNull
  @Override
  public byte[] getBytes() throws IOException {
    return myData;
  }

  @NotNull
  @Override
  public InputStream getStream() throws IOException {
    return new ByteArrayInputStream(myData);
  }

  @Nullable
  @Override
  public ResourceEntry getNextEntry() {
    return null;
  }
}
