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

import org.jetbrains.classes.resources.entry.BytesEntry;
import org.jetbrains.classes.resources.entry.GZipResourceEntry;
import org.jetbrains.classes.resources.entry.ResourceEntry;
import org.jetbrains.classes.resources.entry.SizedGZipResourceEntry;
import org.jetbrains.classes.resources.util.Streams;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created 26.07.13 16:36
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ResourceEntryTest {
  @DataProvider(name = "sizes")
  public Object[][] parameters() {
    return new Object[][]{
            {0}, {1}, {5}, {10}, {50}, {1024}, {Streams.CACHE_SIZE}, {Streams.GZIP_SIZE}
    };
  }

  @Test(dataProvider = "sizes")
  public void testSizedGZip(int sz) throws IOException {
    byte[] data = new byte[sz];
    for (int i = 0; i < sz; i++) data[i] = (byte) (i * i - 1);

    ResourceEntry e = new SizedGZipResourceEntry(data.length, Streams.gzip(data, 0, data.length));

    Assert.assertEquals(data, e.getBytes());
    TestStreams.assertStreamsEqual(new ByteArrayInputStream(data), e.getStream());
  }

  @Test(dataProvider = "sizes")
  public void testGZip(int sz) throws IOException {
    byte[] data = new byte[sz];
    for (int i = 0; i < sz; i++) data[i] = (byte) (i * i - 1);

    ResourceEntry e = new GZipResourceEntry(Streams.gzip(data, 0, data.length));

    Assert.assertEquals(data, e.getBytes());
    TestStreams.assertStreamsEqual(new ByteArrayInputStream(data), e.getStream());
  }

  @Test(dataProvider = "sizes")
  public void testBytes(int sz) throws IOException {
    byte[] data = new byte[sz];
    for (int i = 0; i < sz; i++) data[i] = (byte) (i * i - 1);

    ResourceEntry e = new BytesEntry(data);

    Assert.assertEquals(data, e.getBytes());
    TestStreams.assertStreamsEqual(new ByteArrayInputStream(data), e.getStream());
  }
}
