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
