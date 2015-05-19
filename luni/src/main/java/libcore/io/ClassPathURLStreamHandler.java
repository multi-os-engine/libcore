/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarFile;
import java.util.jar.StrictJarFile;
import java.util.zip.ZipEntry;

/**
 * A {@link URLStreamHandler} for a class path {@link JarFile}. This class avoids the need to open
 * a jar file multiple times to read resources if the jar file can be held open. The
 * {@link URLConnection} objects created are a subclass of {@link JarURLConnection}.
 */
public class ClassPathURLStreamHandler extends URLStreamHandler {
  private final String fileUri;
  private final StrictJarFile jarFile;

  public ClassPathURLStreamHandler(String jarFileName) throws IOException {
    // We use StrictJarFile because it is much less heap memory hungry than ZipFile / JarFile.
    jarFile = new StrictJarFile(jarFileName);

    /*
     * File.toURI() is compliant with RFC 1738 in always creating absolute path names. If we
     * construct the URL by concatenating strings, we might end up with illegal URLs for relative
     * names.
     */
    this.fileUri = new File(jarFileName).toURI().toString();
  }

  public URL getEntryUrlOrNull(String entryName) {
    if (jarFile.findEntry(entryName) != null) {
      try {
        // TODO Should be URL encoding name?
        return new URL("jar", null, -1, fileUri + "!/" + entryName, this);
      } catch (MalformedURLException e) {
        throw new RuntimeException("Unexpectedly invalid entry name", e);
      }
    }
    return null;
  }

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    return new ClassPathURLConnection(url, jarFile);
  }

  private static class ClassPathURLConnection extends JarURLConnection {

    private final StrictJarFile jarFile;
    private final ZipEntry jarEntry;
    private InputStream jarInput;
    private boolean closed;

    public ClassPathURLConnection(URL url, StrictJarFile jarFile) throws MalformedURLException {
      super(url);
      this.jarFile = jarFile;
      this.jarEntry = jarFile.findEntry(getEntryName());
      if (jarEntry == null) {
        throw new MalformedURLException(
            "URL does not correspond to an entry in the zip file. URL=" + url
                + ", zipfile=" + jarFile.getName());
      }
    }

    @Override
    public void connect() {
      connected = true;
    }

    @Override
    public JarFile getJarFile() throws IOException {
      // TODO Expensive!
      return new JarFile(jarFile.getName());
    }

    @Override
    public InputStream getInputStream() throws IOException {
      if (closed) {
        throw new IllegalStateException("JarURLConnection InputStream has been closed");
      }
      connect();
      if (jarInput != null) {
        return jarInput;
      }
      return jarInput = new FilterInputStream(jarFile.getInputStream(jarEntry)) {
        @Override
        public void close() throws IOException {
          super.close();
          closed = true;
        }
      };
    }

    /**
     * Returns the content type of the entry based on the name of the entry. Returns
     * non-null results ("content/unknown" for unknown types).
     *
     * @return the content type
     */
    @Override
    public String getContentType() {
      String cType = guessContentTypeFromName(getEntryName());
      if (cType == null) {
        cType = "content/unknown";
      }
      return cType;
    }

    @Override
    public int getContentLength() {
      connect();
      return (int) jarEntry.getSize();
    }
  }
}
