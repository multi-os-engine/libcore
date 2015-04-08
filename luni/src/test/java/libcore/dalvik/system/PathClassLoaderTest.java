/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.dalvik.system;

import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import libcore.io.Streams;
import junit.framework.TestCase;

public final class PathClassLoaderTest extends TestCase {

    private static final File WORKING_DIR;
    static {
        // First try to use the test runner directory for cts, fall back to
        // shell-writable directory for vogar
        File runner_dir = new File("/data/data/android.core.tests.runner");
        if (runner_dir.exists()) {
            WORKING_DIR = runner_dir;
        } else {
            WORKING_DIR = new File("/data/local/tmp");
        }
    }
    private static final File TMP_DIR = new File(WORKING_DIR, "loading-test");
    private static final String PACKAGE_PATH = "dalvik/system/";
    private static final String JAR_NAME = "loading-test.jar";
    private static final File JAR_FILE = new File(TMP_DIR, JAR_NAME);

    /**
     * Make sure we're searching the application library path first.
     * http://b/issue?id=2933456
     */
    public void testLibraryPathSearchOrder() throws IOException {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File systemLibPath = new File(tmp, "systemLibPath");
        File applicationLibPath = new File(tmp, "applicationLibPath");
        makeTempFile(systemLibPath, "libduplicated.so");
        File applicationLib = makeTempFile(applicationLibPath, "libduplicated.so");

        System.setProperty("java.library.path", systemLibPath.toString());
        PathClassLoader pathClassLoader = new PathClassLoader(applicationLibPath.toString(),
                applicationLibPath.toString(), getClass().getClassLoader());

        String path = pathClassLoader.findLibrary("duplicated");
        assertEquals(applicationLib.toString(), path);
    }

    public void testAppUseOfPathClassLoader() throws Exception {
        PathClassLoader cl = new PathClassLoader(JAR_FILE.getPath(),
                ClassLoader.getSystemClassLoader());
        Class c = cl.loadClass("test.Test1");
        Method m = c.getMethod("test", (Class[]) null);
        String result = (String) m.invoke(null, (Object[]) null);
        assertSame("blort", result);
    }

    private File makeTempFile(File directory, String name) throws IOException {
        directory.mkdirs();
        File result = new File(directory, name);
        FileOutputStream stream = new FileOutputStream(result);
        stream.close();
        assertTrue(result.exists());
        return result;
    }

    @Override protected void setUp() throws Exception {
        super.setUp();

        assertTrue(TMP_DIR.exists() || TMP_DIR.mkdirs());
        ClassLoader cl = PathClassLoaderTest.class.getClassLoader();
        copyResource(cl, JAR_NAME, JAR_FILE);
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Copy a resource in the package directory to the indicated
     * target file, but only if the target file doesn't exist.
     */
    private static void copyResource(ClassLoader loader, String resourceName,
            File destination) throws IOException {
        if (destination.exists()) {
            return;
        }

        InputStream in =
            loader.getResourceAsStream(PACKAGE_PATH + resourceName);
        FileOutputStream out = new FileOutputStream(destination);
        Streams.copy(in, out);
        in.close();
        out.close();
    }
}
