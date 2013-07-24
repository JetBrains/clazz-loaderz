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
import sun.misc.CompoundEnumeration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created 24.07.13 11:49
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClasspath {
  @NotNull
  public Enumeration<URL> getResources(@NotNull String name) throws IOException {
    return new CompoundEnumeration<URL>(new Enumeration[0]);
  }

  @NotNull
  public InputStream getResourceAsStream(@NotNull final String name) throws IOException {
    throw new FileNotFoundException();
  }

  @NotNull
  public URL getResourceAsURL(@NotNull final String name) throws IOException {
    throw new FileNotFoundException();
  }

  @NotNull
  public byte[] getClassResource(@NotNull final String classResource) throws IOException {
    throw new FileNotFoundException();
  }
}
