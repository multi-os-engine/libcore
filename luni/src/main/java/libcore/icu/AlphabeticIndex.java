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

import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.UnicodeSet;

/**
 * Uses icu4j's AlphabeticIndex.
 */
public final class AlphabeticIndex {

  private com.ibm.icu.text.AlphabeticIndex<Object> alphaIndex;

  /**
   * Exposes icu4j's ImmutableIndex. This exposes a read-only, thread safe
   * snapshot view of an AlphabeticIndex at the moment it was
   * created, and allows for random access to buckets by index.
   */
  public static final class ImmutableIndex {

    private com.ibm.icu.text.AlphabeticIndex.ImmutableIndex<Object> immutableIndex;

    private ImmutableIndex(com.ibm.icu.text.AlphabeticIndex.ImmutableIndex<Object>
        incomingIndex) {
      immutableIndex = incomingIndex;
    }

    @Override protected synchronized void finalize() throws Throwable {
      super.finalize();
    }

    /**
     * Returns the number of the label buckets in this index.
     */
    public int getBucketCount() {
      return immutableIndex.getBucketCount();
    }

    /**
     * Returns the index of the bucket in which 's' should appear.
     * Function is synchronized because underlying routine walks an iterator
     * whose state is maintained inside the index object.
     */
    public int getBucketIndex(String s) {
      return immutableIndex.getBucketIndex(s);
    }

    /**
     * Returns the label for the bucket at the given index (as returned by getBucketIndex).
     */
    public String getBucketLabel(int index) {
      return immutableIndex.getBucket(index).getLabel();
    }

  }

  /**
   * Creates a new AlphabeticIndex for the given locale.
   */
  public AlphabeticIndex(Locale locale) {
    alphaIndex = new com.ibm.icu.text.AlphabeticIndex<Object>(locale);
  }

  @Override protected synchronized void finalize() throws Throwable {
    super.finalize();
  }

  /**
   * Returns the max number of the label buckets allowed in this index.
   */
  public synchronized int getMaxLabelCount() {
    return alphaIndex.getMaxLabelCount();
  }

  /**
   * Sets the max number of the label buckets in this index.
   * (ICU 51 default is 99)
   */
  public synchronized AlphabeticIndex setMaxLabelCount(int count) {
    alphaIndex.setMaxLabelCount(count);
    return this;
  }

  /**
   * Adds the index characters from the given locale to the index.
   * The labels are added to those that are already in the index;
   * they do not replace the existing index characters.
   * The collation order for this index is not changed;
   * it remains that of the locale that was originally specified
   * when creating this index.
   */
  public synchronized AlphabeticIndex addLabels(Locale locale) {
    alphaIndex.addLabels(locale);
    return this;
  }

  /**
   * Adds the index characters in the range between the specified start and
   * end code points, inclusive.
   */
  public synchronized AlphabeticIndex addLabelRange(int codePointStart, int codePointEnd) {
    UnicodeSet additions = new UnicodeSet(codePointStart, codePointEnd);
    alphaIndex.addLabels(additions);
    return this;
  }

  /**
   * Returns the number of the label buckets in this index.
   */
  public synchronized int getBucketCount() {
    return  alphaIndex.getBucketCount();
  }

  /**
   * Returns the index of the bucket in which 's' should appear.
   * Function is synchronized because underlying routine walks an iterator
   * whose state is maintained inside the index object.
   */
  public synchronized int getBucketIndex(String s) {
    return alphaIndex.getBucketIndex(s);
  }

  /**
   * Returns the label for the bucket at the given index (as returned by getBucketIndex).
   */
  public synchronized String getBucketLabel(int index) {
    List<String> labels = alphaIndex.getBucketLabels();
    return labels.get(index);
  }

  /**
   * Returns an ImmutableIndex created from this AlphabeticIndex.
   */
  public synchronized ImmutableIndex getImmutableIndex() {
    return new ImmutableIndex(alphaIndex.buildImmutableIndex());
  }

}
