/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import junit.framework.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

// In the upstream sources this class was nested inside
// ArrayBlockingQueueTests. It was extracted to the top level because the CTS
// runner does not support nested test classes. The same transformation was
// applied to all nested classes in the jsr166 suite (see the parent CL for the
// complete list). This should be reverted after CTS runner is fixed.
public class ArrayBlockingQueueFairTest extends BlockingQueueTest {

    protected BlockingQueue emptyCollection() {
        return new ArrayBlockingQueue(SIZE, true);
    }

}
