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
import org.jetbrains.classes.resources.entry.CompositeEntry;
import org.jetbrains.classes.resources.entry.ResourceEntry;
import org.jetbrains.classes.resources.entry.SizedGZipResourceEntry;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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

        final SizedGZipResourceEntry entry = new SizedGZipResourceEntry(actualSize, bos.toByteArray());

        final String key = trimSlashes(ze.getName());
        final ResourceEntry prev = myCache.put(key, entry);
        if (prev != null) {
          myCache.put(key, new CompositeEntry(entry, prev));
        }
      }
    } finally {
      jos.close();
    }
  }

  @NotNull
  public URL getResourceAsURL(@NotNull final String name) throws IOException {
    final ResourceEntry holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException();
    int id = 0;
    return createURL(name, id);
  }

  @NotNull
  private URL createURL(@NotNull final String name, int id) {
    try {
    return new URL(PROTOCOL, "classloader", 42 + id, "/" + name, HANDLER);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to create URL  for " + name + ", id=" + id + " " + e.getMessage(), e);
    }
  }

  @NotNull
  public Enumeration<URL> getResources(@NotNull final String name) {
    final ResourceEntry entry = myCache.get(name);
    if (entry == null) return EMPTY;

    return new Enumeration<URL>() {
      private int myCount = 0;
      private ResourceEntry myEntry = entry;
      @Override
      public boolean hasMoreElements() {
        return myEntry != null;
      }

      @Override
      public URL nextElement() {
        final URL url = createURL(name, myCount++);
        myEntry = myEntry.getNextEntry();
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
      final int idx = u.getPort() - 42;

      return new URLConnection(u) {
        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
          ResourceEntry holder = myCache.get(name);
          for (int cnt = idx; cnt > 0 && holder != null; cnt--) {
            holder = holder.getNextEntry();
          }

          if (holder == null) throw new FileNotFoundException(name);
          return holder.getStream();
        }
      };
    }
  };

  private static final Enumeration<URL> EMPTY = new Enumeration<URL>() {
    @Override
    public boolean hasMoreElements() {
      return false;
    }

    @Override
    public URL nextElement() {
      return null;
    }
  };
}
