/*
 * Copyright (C) 2013 The Android Open Source Project
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

package libcore.util;

import java.io.File;
import java.io.FileOutputStream;

public class ZoneInfoDBTest extends junit.framework.TestCase {
  private static final String CURRENT_VERSION = ZoneInfoDB.getVersion();
  private static final String DEFAULT_FILE = System.getenv("ANDROID_ROOT") + "/usr/share/zoneinfo/";

  // An empty override file should fall back to the default file.
  public void testEmptyOverrideFile() throws Exception {
    String emptyFile = makeTemporaryFile("");
    ZoneInfoDB.TzData data = new ZoneInfoDB.TzData(emptyFile, DEFAULT_FILE);
    assertEquals(CURRENT_VERSION, data.getVersion());
    assertTrue(data.getAvailableIDs().length > 100);
  }

  // A corrupt override file should fall back to the default file.
  public void testCorruptOverrideFile() throws Exception {
    String corruptFile = makeTemporaryFile("invalid content");
    ZoneInfoDB.TzData data = new ZoneInfoDB.TzData(corruptFile, DEFAULT_FILE);
    assertEquals(CURRENT_VERSION, data.getVersion());
    assertTrue(data.getAvailableIDs().length > 100);
  }

  // Given no tzdata files we can use, we should fall back to built-in "GMT".
  public void testNoGoodFile() throws Exception {
    String emptyFile = makeTemporaryFile("");
    ZoneInfoDB.TzData data = new ZoneInfoDB.TzData(emptyFile);
    assertEquals("missing", data.getVersion());
    assertEquals(1, data.getAvailableIDs().length);
    assertEquals("GMT", data.getAvailableIDs()[0]);
  }

  private static String makeTemporaryFile(String content) throws Exception {
    File f = File.createTempFile("temp-", ".txt");
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(content.getBytes());
    fos.close();
    return f.getPath();
  }
}
