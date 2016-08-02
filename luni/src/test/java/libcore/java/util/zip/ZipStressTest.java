/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.util.zip;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipStressTest extends TestCase {
    public void testStress() throws Exception {
        ZipFile zf = new ZipFile("/system/framework/ext.jar");
        List<String> entryNames = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            entryNames.add(entries.nextElement().getName());
        }
        zf.close();

        PathClassLoader pcl = new PathClassLoader("/system/framework/ext.jar", null);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            threads.add(new Thread(new ReadRandomEntryRunnable(entryNames, pcl)));
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    static class ReadRandomEntryRunnable implements Runnable {
        private final List<String> entryNames;
        private final ClassLoader cl;
        private Exception e;

        ReadRandomEntryRunnable(List<String> entryNames, ClassLoader cl) {
            this.entryNames = entryNames;
            this.cl = cl;
        }

        @Override
        public void run() {
            Random r = new Random();
            for (int j = 0; j < 32 * 32; ++j) {
                try {
                    int entry = j; //r.nextInt(entryNames.size());
                    InputStream is = cl.getResourceAsStream(
                            "com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto_DO");

                    int i = 0;
                    byte[] buf = new byte[256];
                    while (is.read(buf) != -1) {
                        ++i;
                    }
                    is.close();
                } catch (Exception e) {
                    System.logE("Exception: ", e);
                }
            }
        }
    }
}
