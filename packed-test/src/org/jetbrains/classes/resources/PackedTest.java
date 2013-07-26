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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 26.07.13 18:23
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackedTest {
  @Test
  public void should_be_able_to_create_classloader() throws IOException {
    ResourceClassLoaderFactory.forResources(getClass().getClassLoader());
  }

  @Test
  public void abstract_library_use_case() throws IOException {

  }

  @Test
  public void should_work_with_vcs_worker() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    //requires VCS Worker client library to be placed in testData/worker
    final File home = new File("testData/worker");
    if (!home.isDirectory()) return;

    final List<URL> urls = new ArrayList<URL>();
    final File[] files = home.listFiles();
    Assert.assertNotNull(files);
    for (File file : files) {
      if (file.getName().endsWith(".jar")) {
        urls.add(file.toURI().toURL());
      }
    }

    final ClassLoader rcl = ResourceClassLoaderFactory.forResources(getClass().getClassLoader(), urls.toArray(new URL[urls.size()]));

    final Object clientSettings = rcl.loadClass("jetbrains.vcs.api.remote.VcsClientSettings").getConstructor(String.class).newInstance("http://localhost:9888");

    final Class<?> worker = rcl.loadClass("jetbrains.vcs.api.remote.impl.VcsRemoteClientFactoryImpl");
    final Object instance = worker.newInstance();
    final Object connection = worker.getMethod("openConnection", clientSettings.getClass()).invoke(instance, clientSettings);
    connection.getClass().getMethod("ping").invoke(connection);

    System.out.println("services = " + connection.getClass().getMethod("getAvailablePlugins").invoke(connection));
  }

}
