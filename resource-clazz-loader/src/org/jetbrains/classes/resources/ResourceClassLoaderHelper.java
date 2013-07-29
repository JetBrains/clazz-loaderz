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

import java.io.IOException;
import java.net.URL;

/**
 * Created 26.07.13 18:18
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceClassLoaderHelper {
  @NotNull
  public static ClassLoader forResources(@NotNull ClassLoader parent,
                                         @NotNull URL... resources) throws IOException {

    final ResourceClasspath path = new ResourceClasspath();
    for (int i = resources.length - 1; i >= 0; i--) {
      path.addResource(new URLResource(resources[i]));
    }
    return new ResourceClassLoader(Delegation.CALL_SELF_FIRST, parent, path);
  }
}
