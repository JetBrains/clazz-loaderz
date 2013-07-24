package org.jetbrains.classes.resources;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created 24.07.13 13:02
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface ResourceHolder {
  @NotNull String getResourceName();
  @NotNull InputStream getContent() throws IOException;
}
