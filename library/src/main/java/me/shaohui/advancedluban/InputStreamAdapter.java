package me.shaohui.advancedluban;

import java.io.IOException;
import java.io.InputStream;

/**
 * Automatically close the previous InputStream when opening a new InputStream,
 * and finally need to manually call {@link #close()} to release the resource.
 */
public abstract class InputStreamAdapter<T> implements InputStreamProvider<T> {

  private InputStream inputStream;

  @Override
  public InputStream open() throws IOException {
    close();
    inputStream = openInternal();
    return inputStream;
  }

  public abstract InputStream openInternal() throws IOException;

  @Override
  public void close() {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException ignore) {
      }finally {
        inputStream = null;
      }
    }
  }
}