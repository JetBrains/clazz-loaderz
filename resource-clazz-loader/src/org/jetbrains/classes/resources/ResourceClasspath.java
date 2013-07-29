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
import org.jetbrains.classes.resources.entry.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.jetbrains.classes.resources.util.Streams.*;

/**
 * Created 24.07.13 11:49
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClasspath {
  private static final String PROTOCOL = "jonnyzzz";

  private final String myId = UUID.randomUUID().toString();
  private final Map<String, ResourceEntry> myCache = new HashMap<String, ResourceEntry>();

  public void addResource(@NotNull ResourceHolder resource) throws IOException {
    final byte[] buff = new byte[CACHE_SIZE];
    final ZipInputStream jos = new ZipInputStream(resource.getContent());
    try {
      while(true) {
        final ZipEntry ze = jos.getNextEntry();
        if (ze == null) break;
        if (ze.isDirectory()) continue;

        addEntry(ze, processEntry(resource, buff, jos, ze));
      }
    } finally {
      close(jos);
    }
  }

  @NotNull
  private ResourceEntry processEntry(@NotNull final ResourceHolder resource,
                                     @NotNull final byte[] buff,
                                     @NotNull final ZipInputStream jos,
                                     @NotNull final ZipEntry ze) throws IOException {
    final boolean isClazz = ze.getName().endsWith(".class");
    final int sz = readFully(jos, buff);

    if (sz <= GZIP_SIZE) return new BytesEntry(Arrays.copyOf(buff, sz));
    if (sz < buff.length) {
      return isClazz
              ? new SizedGZipResourceEntry(sz, gzip(buff, 0, sz))
              : new GZipResourceEntry(gzip(buff, 0, sz));
    }

    return new ScanEntry(resource, ze);
  }

  private void addEntry(@NotNull final ZipEntry ze,
                        @NotNull final ResourceEntry entry) {
    final String key = trimSlashes(ze.getName());
    final ResourceEntry prev = myCache.put(key, entry);
    if (prev != null) {
      myCache.put(key, new CompositeEntry(entry, prev));
    }
  }

  @NotNull
  public URL getResourceAsURL(@NotNull final String name) throws IOException {
    final ResourceEntry holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException();
    return createURL(name, 0);
  }

  @NotNull
  private URL createURL(@NotNull final String name, final int id) {
    try {
    return new URL(PROTOCOL, myId, 42 + id, "/" + name, HANDLER);
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
      if (!u.getHost().equals(myId)) throw new IOException("Unsupported URL: " + u);
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
