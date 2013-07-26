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
import org.testng.ITestListener;
import org.testng.TestNG;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;

/**
 * Created 26.07.13 11:09
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class RunTestNG {
  protected static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("There is no escape!");
      this.status = status;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // allow anything.
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
      // allow anything.
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }

  public static void callTestNGMain(@NotNull final ResourceClassLoader rcl) throws Exception {
    System.setSecurityManager(new NoExitSecurityManager());

    try {
      //should see class
      final Class<?> testNG = rcl.loadClass(TestNG.class.getName());
      final Class<?> listener = rcl.loadClass(ITestListener.class.getName());
      final Method main = testNG.getMethod("privateMain", String[].class, listener);
      main.invoke(null, new String[]{"--help"}, null);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof ExitException) return;
      throw e;
    } finally {
      System.setSecurityManager(null);
    }
  }
}
