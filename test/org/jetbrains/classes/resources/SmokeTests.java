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
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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
  public void should_run_testNG() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final ResourceClassLoader rcl = loadTestNG();

    //should see class
    callTestNGMain(rcl);
  }

  @Test
  public void should_run_resTestNG() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    //this test requires `test-data-1` artifact to be compiled
    final ResourceClassLoader rcl = resLoadTestNG();

    callTestNGMain(rcl);
  }

  private void callTestNGMain(ResourceClassLoader rcl) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    //should see class
    final Class<?> testNG = rcl.loadClass(TestNG.class.getName());
    final Class<?> listener = rcl.loadClass(ITestListener.class.getName());
    final Method main = testNG.getMethod("privateMain", String[].class, listener);
    main.invoke(null, new String[]{"-junit"}, null);
  }

  @Test
  public void should_run_res_resTestNG() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    //this test requires `test-data-2` artifact to be compiled
    final ResourceClassLoader rcl = resResLoadTestNG();

    //should see class
    callTestNGMain(rcl);
  }

  @Test(expectedExceptions = ClassNotFoundException.class)
  public void should_not_see_this_class() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final ResourceClassLoader rcl = loadTestNG();

    rcl.loadClass(getClass().getName());
  }

  private void assertStreamsEqual(@Nullable InputStream is1, @Nullable InputStream is2) throws IOException {
    if (is1 == null && is2 == null) return;

    Assert.assertNotNull(is1);
    Assert.assertNotNull(is2);
    while(true) {
      int x1 = is1.read();
      int x2 = is2.read();

      Assert.assertEquals(x1, x2);

      if (x1 < 0) break;
    }
    is1.close();
    is2.close();
  }
}
