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
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
