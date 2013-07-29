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

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created 26.07.13 13:25
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class Streams {
  public static final int CACHE_SIZE = 256 * 1024;
  public static final int GZIP_SIZE = 128;

  public static int copyStreams(@NotNull final byte[] buff,
                                @NotNull final InputStream input,
                                @NotNull final OutputStream output) throws IOException {
    int x;
    int actualSize = 0;
    while ((x = input.read(buff)) > 0) {
      actualSize += x;
      output.write(buff, 0, x);
    }
    return actualSize;
  }

  /**
   * Reads up to all bytes to full result parameter
   *
   * @param stream stream
   * @param result array
   * @return number of bytes loaded. The value is result.length+1 if stream was not ended and the buffer is full
   * @throws IOException on error
   */
  public static int readFully(@NotNull final InputStream stream,
                              @NotNull final byte[] result) throws IOException {

    int i = 0;
    while(i + 1 < result.length) {
      int read = stream.read(result, i, result.length - i);
      if (read <= 0) return i;
      i += read;
    }
    return result.length + 1;
  }

  @NotNull
  public static byte[] readFully(@NotNull final InputStream stream) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(CACHE_SIZE);
    copyStreams(new byte[CACHE_SIZE], stream, bos);
    return bos.toByteArray();
  }

  @NotNull
  public static byte[] gzip(@NotNull final byte[] data, int offset, int len) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
    final GZIPOutputStream gos = new GZIPOutputStream(bos);
    gos.write(data, offset, len);
    gos.close();
    return bos.toByteArray();
  }

  public static void close(@Nullable Closeable c) {
    if (c == null) return;
    try {
      c.close();
    } catch (IOException e) {
      //NOP
    }
  }
}
