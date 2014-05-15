/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package jsr166;

import junit.framework.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class LinkedBlockingDequeBoundedTest extends JSR166TestCase {

    protected BlockingQueue emptyCollection() {
        return new LinkedBlockingDeque(SIZE);
    }

}
