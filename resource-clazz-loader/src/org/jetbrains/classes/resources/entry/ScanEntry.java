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
import org.jetbrains.classes.resources.ResourceHolder;
import org.jetbrains.classes.resources.util.SearchingZipScan;
import org.jetbrains.classes.resources.util.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created 26.07.13 13:11
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ScanEntry extends BaseEntry {
  private final ResourceHolder myEntry;
  private final String myName;

  public ScanEntry(@NotNull final ResourceHolder entry,
                   @NotNull final ZipEntry ze) {
    myEntry = entry;
    myName = ze.getName();
  }

  @NotNull
  @Override
  public byte[] getBytes() throws IOException {
    return SCAN_BYTES.processEntries(myEntry, myName);
  }

  @NotNull
  @Override
  public InputStream getStream() throws IOException {
    return SCAN_STREAM.processEntries(myEntry, myName);
  }

  private static final SearchingZipScan<byte[]> SCAN_BYTES = new SearchingZipScan<byte[]>() {
    @NotNull
    @Override
    protected byte[] processMatchingItem(@NotNull final ZipEntry ze,
                                         @NotNull final String nane,
                                         @NotNull final ZipInputStream stream) throws IOException {

      final int entrySize = (int) ze.getSize();
      final int size = entrySize > 0 ? entrySize : 65536;

      final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
      Streams.copyStreams(new byte[size], stream, bos);

      bos.close();
      stream.close();

      return bos.toByteArray();
    }
  };

  private final SearchingZipScan<InputStream> SCAN_STREAM = new SearchingZipScan<InputStream>() {
    @NotNull
    @Override
    protected InputStream processMatchingItem(@NotNull ZipEntry entry, @NotNull String name, @NotNull ZipInputStream stream) throws IOException {
      return stream;
    }
  };
}
