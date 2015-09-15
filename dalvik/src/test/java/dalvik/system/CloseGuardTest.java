/*
 * Copyright (C) 2014 The Android Open Source Project
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
package dalvik.system;

import junit.framework.TestCase;

public class CloseGuardTest extends TestCase {

  private TestReporter reporter;
  private CloseGuard.Reporter originalReporter;

  @Override public void setUp() throws Exception {
    reporter = new TestReporter();
    originalReporter = CloseGuard.getReporter();
    CloseGuard.setReporter(reporter);
  }

  @Override public void tearDown() throws Exception {
    CloseGuard.setReporter(originalReporter);
  }

  public void testCloseGuardReports() throws Exception {
    reporter.assertReported(false);

    CloseGuard closeGuard = createAndOpenCloseGuard();
    closeGuard.warnIfOpen();

    reporter.assertReported(true);
  }

  public void testCloseGuard_pauseResume() throws Exception {
    reporter.assertReported(false);

    // Create and open the CloseGuard instance while CloseGuards are paused for this thread.
    CloseGuard.pauseForCurrentThread();
    CloseGuard closeGuard;
    try {
      closeGuard = createAndOpenCloseGuard();
    } finally {
      CloseGuard.resumeForCurrentThread();
    }

    // Now close it after CloseGuards have been resumed.
    closeGuard.warnIfOpen();

    reporter.assertReported(false);
  }

  private static CloseGuard createAndOpenCloseGuard() {
    // Simulates an open / close cycle without using GC.
    CloseGuard closeGuard = CloseGuard.get();
    closeGuard.open("foo");
    return closeGuard;
  }

  private static class TestReporter implements CloseGuard.Reporter {

    private boolean reported;

    @Override public void report(String message, Throwable allocationSite) {
      reported = true;
    }

    public void assertReported(boolean expected) {
      assertEquals(expected, reported);
    }
  }


}
