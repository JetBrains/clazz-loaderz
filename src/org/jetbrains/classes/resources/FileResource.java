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

import java.io.*;

/**
 * Created 24.07.13 15:47
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class FileResource implements ResourceHolder {
  private final File myFile;

  public FileResource(@NotNull File file) {
    myFile = file;
  }

  @NotNull
  @Override
  public String getResourceName() {
    return myFile.getPath();
  }

  @NotNull
  @Override
  public InputStream getContent() throws IOException {
    return new BufferedInputStream(new FileInputStream(myFile));
  }
}
