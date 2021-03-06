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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import static org.jetbrains.classes.resources.RunTestNG.callTestNGMain;
import static org.jetbrains.classes.resources.TestStreams.assertStreamsEqual;

/**
 * Created 24.07.13 15:41
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SmokeTests {
  @Test
  public void should_load_annotations_classes() throws IOException, ClassNotFoundException {
    ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new FileResource(new File("lib/annotations/annotations.jar")));

    ResourceClassLoader rcl = new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp);

    //should see class
    rcl.loadClass(NotNull.class.getName()).getMethods();
  }

  @NotNull
  private ResourceClassLoader loadTestNG() throws IOException {
    final ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new FileResource(new File("lib/testng/testng-6.8.jar")));

    return new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp
    );
  }

  @NotNull
  private ResourceClassLoader resResLoadTestNG() throws IOException {
    //this test requires `test-data-2` artifact to be compiled
    final File jar = new File("testData/data-2/jar3.jar");
    Assert.assertTrue(jar.isFile());
    final URLClassLoader cl3 = new URLClassLoader(new URL[] { jar.toURI().toURL()}, null);

    final ResourceClasspath cp2 = new ResourceClasspath();
    cp2.addResource(new ClassloaderResource(cl3, "jar2.jar"));
    final ResourceClassLoader cl2 = new ResourceClassLoader(Delegation.CALL_PARENT_FIRST, cl3, cp2);

    final ResourceClasspath cp1 = new ResourceClasspath();
    cp1.addResource(new ClassloaderResource(cl2, "jar1.jar"));
    final ResourceClassLoader cl1 = new ResourceClassLoader(Delegation.CALL_PARENT_FIRST, cl2, cp1);

    final ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new ClassloaderResource(cl1, "jar.jar"));
    final ResourceClassLoader cl = new ResourceClassLoader(Delegation.CALL_PARENT_FIRST, cl1, cp);

    final ResourceClasspath ng = new ResourceClasspath();
    ng.addResource(new ClassloaderResource(cl, "testng-6.8.jar"));

    return new ResourceClassLoader(Delegation.CALL_SELF_FIRST, null, ng);
  }

  @NotNull
  private ResourceClassLoader resLoadTestNG() throws IOException {
    //this test requires `test-data-1` artifact to be compiled
    final File jar = new File("testData/data-1/jar.jar");
    Assert.assertTrue(jar.isFile());

    final URLClassLoader parent = new URLClassLoader(new URL[] { jar.toURI().toURL()}, null);
    final ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new ClassloaderResource(parent, "testng-6.8.jar"));



    return new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp
    );
  }

  @Test
  public void should_load_testng_classes() throws IOException, ClassNotFoundException {
    final ResourceClassLoader rcl = loadTestNG();

    //should see class
    rcl.loadClass(Test.class.getName()).getMethods();
  }

  @Test
  public void should_load_testng_stream() throws IOException, ClassNotFoundException {
    final ResourceClassLoader rcl = loadTestNG();

    //should see class
    final InputStream stream = rcl.getResourceAsStream("/org/junit/Test.class");
    Assert.assertNotNull(stream);
    stream.close();
  }

  @Test
  public void should_load_testng_URL() throws IOException, ClassNotFoundException {
    final ResourceClassLoader rcl = loadTestNG();

    //should see class
    final URL url = rcl.getResource("/org/junit/Test.class");
    Assert.assertNotNull(url);

    assertStreamsEqual(url.openStream(), rcl.getResourceAsStream("/org/junit/Test.class"));
  }

  @Test
  public void should_run_testNG() throws Exception {
    final ResourceClassLoader rcl = loadTestNG();

    //should see class
    callTestNGMain(rcl);
  }

  @Test
  public void should_run_resTestNG() throws Exception {
    //this test requires `test-data-1` artifact to be compiled
    final ResourceClassLoader rcl = resLoadTestNG();

    callTestNGMain(rcl);
  }

  @Test
  public void should_run_res_resTestNG() throws Exception {
    //this test requires `test-data-2` artifact to be compiled
    final ResourceClassLoader rcl = resResLoadTestNG();

    //should see class
    callTestNGMain(rcl);
  }

  @Test
  public void should_see_duplicating_resources() throws IOException {
    ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new FileResource(new File("lib/annotations/annotations.jar")));
    cp.addResource(new FileResource(new File("lib/annotations/annotations.jar")));
    cp.addResource(new FileResource(new File("lib/annotations/annotations.jar")));
    cp.addResource(new FileResource(new File("lib/annotations/annotations.jar")));

    ResourceClassLoader rcl = new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp);

    //should see class
    final List<URL> result = new ArrayList<URL>();
    for (Enumeration<URL> en = rcl.getResources("/" + NotNull.class.getName().replace(".", "/") + ".class"); en.hasMoreElements(); ) result.add(en.nextElement());
    Assert.assertEquals(result.size(), 4);
    Assert.assertEquals(new HashSet<URL>(result).size(), 4);
  }

  @Test
  public void should_fetch_duplicating_resources() throws IOException {
    ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new FileResource(new File("testData/data-3/j1.jar")));
    cp.addResource(new FileResource(new File("testData/data-3/j2.jar")));
    cp.addResource(new FileResource(new File("testData/data-3/j3.jar")));

    ResourceClassLoader rcl = new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp);

    //should see class
    final List<URL> result = new ArrayList<URL>();
    for (Enumeration<URL> en = rcl.getResources("foo.txt"); en.hasMoreElements(); ) result.add(en.nextElement());
    Assert.assertEquals(result.size(), 3);

    assertStreamsEqual(result.get(2).openStream(), new ByteArrayInputStream("1".getBytes("utf-8")));
    assertStreamsEqual(result.get(1).openStream(), new ByteArrayInputStream("2 ".getBytes("utf-8")));
    assertStreamsEqual(result.get(0).openStream(), new ByteArrayInputStream(" 3 ".getBytes("utf-8")));
  }

  @Test(expectedExceptions = ClassNotFoundException.class)
  public void should_not_see_this_class() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final ResourceClassLoader rcl = loadTestNG();

    rcl.loadClass(getClass().getName());
  }

  @Test
  public void should_work_with_vcs_worker() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    //requires VCS Worker client library to be placed in testData/worker
    final File home = new File("testData/worker");
    if (!home.isDirectory()) return;

    final ResourceClasspath cp = new ResourceClasspath();

    final File[] files = home.listFiles();
    Assert.assertNotNull(files);
    for (File file : files) {
      if (file.getName().endsWith(".jar")) {
        cp.addResource(new FileResource(file));
      }
    }

    ResourceClassLoader rcl = new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp);

    final Object clientSettings = rcl.loadClass("jetbrains.vcs.api.remote.VcsClientSettings").getConstructor(String.class).newInstance("http://localhost:9888");

    final Class<?> worker = rcl.loadClass("jetbrains.vcs.api.remote.impl.VcsRemoteClientFactoryImpl");
    final Object instance = worker.newInstance();
    final Object connection = worker.getMethod("openConnection", clientSettings.getClass()).invoke(instance, clientSettings);
    connection.getClass().getMethod("ping").invoke(connection);

    System.out.println("services = " + connection.getClass().getMethod("getAvailablePlugins").invoke(connection));
  }
}
