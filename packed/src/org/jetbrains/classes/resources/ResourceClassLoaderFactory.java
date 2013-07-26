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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created 26.07.13 18:09
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClassLoaderFactory {
  public static ClassLoader forResources(@NotNull ClassLoader parent,
                                         @NotNull URL... resources) {
    final ClazzLoader cl;
    try {
      cl = new ClazzLoader();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load classes. " + e.getMessage(), e);
    }

    final Class<?> helper;
    try {
      helper = cl.loadClass("org.jetbrains.classes.resources.ResourceClassLoaderHelper");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to load helper class. " + e.getMessage(), e);
    }

    final Method method;
    try {
      method = helper.getMethod("forResources", ClassLoader.class, URL[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Failed to find helper method. " + e.getMessage(), e);
    }

    try {
      return (ClassLoader) method.invoke(null, parent, resources);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create classloader: " + e.getMessage(), e);
    }
  }

  private static class ClazzLoader extends ClassLoader {
    private final Map<String, byte[]> myClasses = new HashMap<String, byte[]>();

    public ClazzLoader() throws IOException {
      super(null);

      final ZipInputStream zis = new ZipInputStream(ResourceClassLoaderFactory.class.getClassLoader().getResourceAsStream("resource-classloader-factory.jonnyzzz"));
      try {
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
          if (ze.isDirectory()) continue;
          final String name = ze.getName();

          if (!name.endsWith(".class")) continue;
          definePackage(name);
          defineClass(zis, name);
        }
      } finally {
        zis.close();
      }

      //define class may call findClass/loadClass to resolve classes used inside
      for (String name : new ArrayList<String>(myClasses.keySet())) {
        final byte[] bytes = myClasses.get(name);
        if (bytes == null) continue;
        defineClass(name, bytes, 0, bytes.length);
      }
    }

    private void defineClass(@NotNull final ZipInputStream zis, @NotNull final String name) throws IOException {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      int x;
      while ((x = zis.read()) >= 0) bos.write(x);
      bos.close();

      final String clazz = name.substring(0, name.length() - ".class".length()).replace('/', '.');
      byte[] bytes = bos.toByteArray();
      myClasses.put(clazz, bytes);
    }

    @NotNull
    @Override
    protected Class<?> findClass(@NotNull final String name) throws ClassNotFoundException {
      final byte[] bytes = myClasses.remove(name);
      if (bytes != null) {
        return defineClass(name, bytes, 0, bytes.length);
      }
      return super.findClass(name);
    }

    private void definePackage(@NotNull final String name) {
      final int i = name.lastIndexOf('.');
      if (i >= 0) {
        final String pkgname = name.substring(0, i);
        // Check if package already loaded.
        final Package pkg = getPackage(pkgname);
        if (pkg == null) {
          try {
            definePackage(pkgname, null, null, null, null, null, null, null);
          } catch (IllegalArgumentException e) {
            // do nothing, package already defined by some other thread
          }
        }
      }
    }
  }
}
