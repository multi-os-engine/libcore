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

package libcore.icu;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Exposes icu4j's Transliterator.
 */
public final class Transliterator {

  private com.ibm.icu.text.Transliterator transliterator;
  private static String[] availableIDs = null;

  /**
   * Creates a new Transliterator for the given id.
   */
  public Transliterator(String id) {
    transliterator = com.ibm.icu.text.Transliterator.getInstance(id);
  }


  /**
   * Returns the ids of all known transliterators.
   */
  public static String[] getAvailableIDs() {
    // Cache the list of transliterators.
    if (availableIDs == null) {
      ArrayList<String> collector = new ArrayList<>();
      Enumeration<String> ids = com.ibm.icu.text.Transliterator.getAvailableIDs();
      while (ids.hasMoreElements()) {
        collector.add(ids.nextElement());
      }

      availableIDs = new String[collector.size()];
      for (int i = 0; i < availableIDs.length; i++) {
        availableIDs[i] = collector.get(i);
      }
    }

    return availableIDs;
  }


  /**
   * Transliterates the specified string.
   */
  public String transliterate(String s) {
    return transliterator.transliterate(s);
  }

}
