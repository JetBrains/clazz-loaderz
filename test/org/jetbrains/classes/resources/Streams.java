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

import org.jetbrains.annotations.Nullable;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created 26.07.13 11:12
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class Streams {
  public static void assertStreamsEqual(@Nullable InputStream is1, @Nullable InputStream is2) throws IOException {
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
