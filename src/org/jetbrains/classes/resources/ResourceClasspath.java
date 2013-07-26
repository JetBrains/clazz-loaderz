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

package org.jetbrains.classes.resources;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created 24.07.13 11:49
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClasspath {
  private static final String PROTOCOL = "jonnyzzz";
  private static final int BUFFER = 65536;

  private final Map<String, ResourceEntry> myCache = new HashMap<String, ResourceEntry>();

  private static class ResourceEntry {
    private final int mySize;
    private final byte[] myData;

    private ResourceEntry(final int actualSize, @NotNull byte[] data) {
      mySize = actualSize;
      myData = data;
    }

    @NotNull
    public byte[] getBytes() throws IOException {
      byte[] result = new byte[mySize];
      final InputStream stream = getStream();
      int i = 0;
      while(i + 1 < mySize) {
        int read = stream.read(result, i, mySize - i);
        if (read <= 0) throw new EOFException();
        i += read;
      }
      return result;
    }

    @NotNull
    public InputStream getStream() throws IOException {
      return new GZIPInputStream(new ByteArrayInputStream(myData));
    }
  }

  public void addResource(@NotNull ResourceHolder resource) throws IOException {
    final byte[] buff = new byte[BUFFER];
    final ZipInputStream jos = new ZipInputStream(resource.getContent());
    try {
      while(true) {
        final ZipEntry ze = jos.getNextEntry();
        if (ze == null) break;
        if (ze.isDirectory()) continue;

        int actualSize = 0;
        final int entrySize = (int)ze.getSize();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(entrySize > 0 ? entrySize : BUFFER);
        final GZIPOutputStream gos = new GZIPOutputStream(bos);
        int x;
        while ((x = jos.read(buff)) > 0) {
          actualSize += x;
          gos.write(buff, 0, x);
        }
        gos.close();

        myCache.put(ze.getName(), new ResourceEntry(actualSize, bos.toByteArray()));
      }
    } finally {
      jos.close();
    }
  }

  @NotNull
  public URL getResourceAsURL(@NotNull final String name) throws IOException {
    final ResourceEntry holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException();

    return new URL(PROTOCOL, "classloader", 42, "/" + name, HANDLER);
  }

  @NotNull
  public Enumeration<URL> getResources(@NotNull final String name) throws IOException {
    final URL url = getResourceAsURL(name);

    return new Enumeration<URL>() {
      private boolean myVisited;
      @Override
      public boolean hasMoreElements() {
        return !myVisited;
      }

      @Override
      public URL nextElement() {
        myVisited = true;
        return url;
      }
    };
  }

  @NotNull
  public InputStream getResourceAsStream(@NotNull final String name) throws IOException {
    final ResourceEntry holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException(name);
    return holder.getStream();
  }

  @NotNull
  public byte[] getClassResource(@NotNull final String name) throws IOException {
    final ResourceEntry holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException(name);
    return holder.getBytes();
  }

  @NotNull
  private String trimSlashes(@NotNull String n) {
    while (n.startsWith("/")) n = n.substring(1);
    return n;
  }

  private final URLStreamHandler HANDLER = new URLStreamHandler() {
    @Override
    protected URLConnection openConnection(@NotNull final URL u) throws IOException {
      if (!u.getProtocol().equals(PROTOCOL)) throw new IOException("Unsupported URL: " + u);
      final String name = trimSlashes(u.getPath());
      return new URLConnection(u) {
        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
          return getResourceAsStream(name);
        }
      };
    }
  };
}
