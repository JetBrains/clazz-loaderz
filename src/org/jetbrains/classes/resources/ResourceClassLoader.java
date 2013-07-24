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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created 23.07.13 16:54
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClassLoader extends ClassLoader {
  private static final String CLASS_EXTENSION = ".class";

  @NotNull
  private final Delegation myDelegation;
  @NotNull
  private final ClassLoader myParent;
  @NotNull
  private final ResourceClasspath myClasspath;

  public ResourceClassLoader(@NotNull final Delegation delegation,
                             @Nullable final ClassLoader parent,
                             @NotNull final ResourceClasspath classpath) {
    super(parent);
    myDelegation = delegation;
    myParent = parent != null ? parent : ClassLoader.getSystemClassLoader();
    myClasspath = classpath;
  }

  @NotNull
  @Override
  protected Class<?> findClass(@NotNull final String name) throws ClassNotFoundException {
    final Class<?> clazz = findClassImpl(name);
    if (clazz != null) return clazz;

    throw new ClassNotFoundException(name);
  }

  @Nullable
  private Class<?> findClassImpl(@NotNull final String name) {
    final String classResource = name.replace('.', '/').concat(CLASS_EXTENSION);
    final ResourceClass res = myClasspath.getClassResource(classResource);
    if (res == null) return null;

    try {
      return defineClass(name, res);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  protected Class<?> loadClass(@NotNull final String name, final boolean resolve) throws ClassNotFoundException {
    Class<?> found = findLoadedClass(name);
    if (found != null) return found;

    found = myDelegation.apply(LOAD_CLASS, name);
    if (found == null) throw new ClassNotFoundException(name);

    if (resolve) {
      resolveClass(found);
    }

    return found;
  }

  @NotNull
  private Class<?> defineClass(@NotNull final String name,
                               @NotNull final ResourceClass res) throws IOException {
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

    final byte[] b = res.getBytes();
    return defineClass(name, b, 0, b.length);
  }

  @Override
  @Nullable
  public URL findResource(@NotNull final String name) {
    final Resource res = getResourceImpl(name);
    if (res == null) return null;
    return res.getURL();
  }

  @Nullable
  private Resource getResourceImpl(@NotNull final String name) {
    String n = name;
    while (n.startsWith("/")) n = n.substring(1);
    return myClasspath.getResource(n);
  }

  @Override
  protected Enumeration<URL> findResources(@NotNull final String name) throws IOException {
    return myClasspath.getResources(name);
  }

  @Nullable
  @Override
  public InputStream getResourceAsStream(@NotNull final String name) {
    return myDelegation.apply(LOAD_RESOURCE_STREAM, name);
  }

  @Nullable
  @Override
  public URL getResource(@NotNull final String name) {
    return myDelegation.apply(LOAD_RESOURCE_URL, name);
  }

  private final Delegation.ValueAction<String, Class<?>> LOAD_CLASS = new Delegation.ValueAction<String, Class<?>>() {
    @Nullable
    @Override
    public Class<?> callParent(@NotNull String name) {
      try {
        return myParent.loadClass(name);
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    @Nullable
    @Override
    public Class<?> callSelf(@NotNull String name) {
      return findClassImpl(name);
    }
  };

  private final Delegation.ValueAction<String, URL> LOAD_RESOURCE_URL = new Delegation.ValueAction<String, URL>() {
    @Nullable
    @Override
    public URL callParent(@NotNull String name) {
      return myParent.getResource(name);
    }

    @Nullable
    @Override
    public URL callSelf(@NotNull String name) {
      return findResource(name);
    }
  };

  private final Delegation.ValueAction<String, InputStream> LOAD_RESOURCE_STREAM = new Delegation.ValueAction<String, InputStream>() {
    @Nullable
    @Override
    public InputStream callParent(@NotNull final String name) {
      //call parent first
      return myParent.getResourceAsStream(name);
    }

    @Nullable
    @Override
    public InputStream callSelf(@NotNull final String name) {
      final Resource res = getResourceImpl(name);
      if (res == null) return null;

      try {
        return res.getInputStream();
      } catch (IOException e) {
        return null;
      }
    }
  };
}
