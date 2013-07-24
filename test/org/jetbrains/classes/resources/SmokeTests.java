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
