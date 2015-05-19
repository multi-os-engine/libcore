package benchmarks;

import junit.framework.TestCase;

import android.os.Debug;

/**
 * Created by nfuller on 5/19/15.
 */
public class ClassLoaderAppTest extends TestCase {

  public void testGetResource() throws Exception {
    Debug.waitForDebugger();
    ClassLoader currentClassLoader = getClass().getClassLoader();
    for (int rep = 0; rep < 10; ++rep) {
      long start = System.nanoTime();
      currentClassLoader.getResource("java/util/logging/logging.properties");
      System.out.println("Hit time taken (ns): " + (System.nanoTime() - start));
    }
    for (int rep = 0; rep < 10; ++rep) {
      long start = System.nanoTime();
      currentClassLoader.getResource("miss");
      System.out.println("Miss time taken (ns): " + (System.nanoTime() - start));
    }
  }

  public void testGetResources() throws Exception {
    ClassLoader currentClassLoader = getClass().getClassLoader();
    for (int rep = 0; rep < 10; ++rep) {
      long start = System.nanoTime();
      currentClassLoader.getResources("java/util/logging/logging.properties");
      System.out.println("Hit time taken (ns): " + (System.nanoTime() - start));
    }
    for (int rep = 0; rep < 10; ++rep) {
      long start = System.nanoTime();
      currentClassLoader.getResources("miss");
      System.out.println("Miss time taken (ns): " + (System.nanoTime() - start));
    }
  }
}
