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
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Created 24.07.13 11:49
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClasspath {
  private final Map<String, ResourceHolder> myCache = new HashMap<String, ResourceHolder>();

  public void addResource(@NotNull ResourceHolder resource) throws IOException {
    final JarInputStream jos = new JarInputStream(resource.getContent(), false);
    try {
      while(true) {
        ZipEntry ze = jos.getNextEntry();
        if (ze == null) break;
        if (ze.isDirectory()) continue;
        myCache.put(ze.getName(), resource);
      }
    } finally {
      jos.close();
    }
  }

  @NotNull
  public URL getResourceAsURL(@NotNull final String name) throws IOException {
    final ResourceHolder holder = myCache.get(name);
    if (holder == null) throw new FileNotFoundException();

    return new URL("jonnyzzz", "classloader", 42, name, new URLStreamHandler() {
      @Override
      protected URLConnection openConnection(@NotNull final URL u) throws IOException {
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
    });
  }

  @NotNull
  public Enumeration<URL> getResources(@NotNull String name) throws IOException {
    return new Enumeration<URL>() {
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

  @NotNull
  public InputStream getResourceAsStream(@NotNull final String name) throws IOException {
    return extract(name, myCache.get(name));
  }

  @NotNull
  public byte[] getClassResource(@NotNull final String name) throws IOException {
    return readFully(getResourceAsStream(name));
  }

  @NotNull
  private byte[] readFully(@NotNull final InputStream is) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(65536);
    byte[] buff = new byte[65536];
    int x;
    while ((x = is.read(buff)) > 0) bos.write(buff, 0, x);
    return bos.toByteArray();
  }

  @NotNull
  private InputStream extract(@NotNull final String name,
                              @Nullable final ResourceHolder holder) throws IOException {
    if (holder == null) throw new FileNotFoundException(name);

    final JarInputStream jos = new JarInputStream(holder.getContent(), false);
    try {
      while (true) {
        final ZipEntry ze = jos.getNextEntry();
        if (ze == null) {
          throw new FileNotFoundException(name);
        }
        if (ze.isDirectory()) continue;
        if (ze.getName().equals(name)) {
          final int size = (int) ze.getSize();
          return new JarItemStream(size, jos);
        }
      }
    } catch (IOException e) {
      jos.close();
      throw new IOException(e);
    }
  }


  private static class JarItemStream extends InputStream {
    private final int mySize;
    private final InputStream myJos;
    private int myBytesToRead;

    public JarItemStream(int size, @NotNull InputStream jos) {
      mySize = size;
      myJos = jos;
      myBytesToRead = mySize;
    }

    @Override
    public int read() throws IOException {
      if (myBytesToRead <= 0) return -1;
      myBytesToRead--;
      return myJos.read();
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
      len = Math.min(myBytesToRead, len);
      myBytesToRead -= len;
      return myJos.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
      n = Math.min(n, myBytesToRead);
      myBytesToRead -= n;
      return myJos.skip(n);
    }

    @Override
    public void close() throws IOException {
      myJos.close();
    }
  }
}
