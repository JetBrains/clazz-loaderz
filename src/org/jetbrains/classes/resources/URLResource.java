package org.jetbrains.classes.resources;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created 24.07.13 14:58
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class URLResource implements ResourceHolder {
  private final URL myURL;

  public URLResource(@NotNull final URL URL) {
    myURL = URL;
  }

  @NotNull
  @Override
  public String getResourceName() {
    return myURL.toString();
  }

  @NotNull
  @Override
  public InputStream getContent() throws IOException {
    return myURL.openStream();
  }
}
