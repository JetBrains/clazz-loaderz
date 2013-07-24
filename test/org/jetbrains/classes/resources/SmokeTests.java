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
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
            cp
            );

    //should see class
    rcl.loadClass(NotNull.class.getName()).getMethods();
  }

  @Test
  public void should_load_testng_classes() throws IOException, ClassNotFoundException {
    ResourceClasspath cp = new ResourceClasspath();
    cp.addResource(new FileResource(new File("lib/testng/testng-6.8.jar")));

    ResourceClassLoader rcl = new ResourceClassLoader(
            Delegation.CALL_SELF_FIRST,
            null,
            cp
            );

    //should see class
    rcl.loadClass(Test.class.getName()).getMethods();
  }
}
