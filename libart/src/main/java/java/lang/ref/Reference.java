/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
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

package java.lang.ref;

/**
 * Provides an abstract class which describes behavior common to all reference
 * objects. It is not possible to create immediate subclasses of
 * {@code Reference} in addition to the ones provided by this package. It is
 * also not desirable to do so, since references require very close cooperation
 * with the system's garbage collector. The existing, specialized reference
 * classes should be used instead.
 *
 * <p>Three different type of references exist, each being weaker than the preceding one:
 * {@link java.lang.ref.SoftReference}, {@link java.lang.ref.WeakReference}, and
 * {@link java.lang.ref.PhantomReference}. "Weakness" here means that less restrictions are
 * being imposed on the garbage collector as to when it is allowed to
 * actually garbage-collect the referenced object.
 *
 * <p>In order to use reference objects properly it is important to understand
 * the different types of reachability that trigger their clearing and
 * enqueueing. The following table lists these, from strongest to weakest.
 * For each row, an object is said to have the reachability on the left side
 * if (and only if) it fulfills all of the requirements on the right side. In
 * all rows, consider the <em>root set</em> to be a set of references that
 * are "resistant" to garbage collection (that is, running threads, method
 * parameters, local variables, static fields and the like).
 *
 * <p><table>
 * <tr>
 * <td>Strongly reachable</td>
 * <td> <ul>
 * <li>There exists at least one path from the root set to the object that does not traverse any
 * instance of a {@code java.lang.ref.Reference} subclass.
 * </li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Softly reachable</td>
 * <td> <ul>
 * <li>The object is not strongly reachable.</li>
 * <li>There exists at least one path from the root set to the object that does traverse
 * a {@code java.lang.ref.SoftReference} instance, but no {@code java.lang.ref.WeakReference}
 * or {@code java.lang.ref.PhantomReference} instances.</li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Weakly reachable</td>
 * <td> <ul>
 * <li>The object is neither strongly nor softly reachable.</li>
 * <li>There exists at least one path from the root set to the object that does traverse a
 * {@code java.lang.ref.WeakReference} instance, but no {@code java.lang.ref.PhantomReference}
 * instances.</li>
 * </ul> </td>
 * </tr>
 *
 * <tr>
 * <td>Phantom-reachable</td>
 * <td> <ul>
 * <li>The object is neither strongly, softly, nor weakly reachable.</li>
 * <li>The object is referenced by a {@code java.lang.ref.PhantomReference} instance.</li>
 * <li>The object has already been finalized.</li>
 * </ul> </td>
 * </tr>
 * </table>
 */
public abstract class Reference<T> {

    /**
     * Forces JNI path.
     * If GC is not in progress (ie: not going through slow path), the referent
     * can be quickly returned through intrinsic without passing through JNI.
     * This flag forces the JNI path so that it can be tested and benchmarked.
     */
    private static boolean disableIntrinsic = false;

    /**
     * Slow path flag for the reference processor.
     * Used by the reference processor to determine whether or not the referent
     * can be immediately returned. Because the referent might get swept during
     * GC, the slow path, which passes through JNI, must be taken.
     */
    private static boolean slowPathEnabled = false;

    /**
     * The object to which this reference refers.
     * VM requirement: this field <em>must</em> be called "referent"
     * and be an object.
     */
    volatile T referent;

    /**
     * If non-null, the queue on which this reference will be enqueued
     * when the referent is appropriately reachable.
     * VM requirement: this field <em>must</em> be called "queue"
     * and be a java.lang.ref.ReferenceQueue.
     */
    volatile ReferenceQueue<? super T> queue;

    /**
     * Used internally by java.lang.ref.ReferenceQueue.
     * VM requirement: this field <em>must</em> be called "queueNext"
     * and be a java.lang.ref.Reference.
     */
    @SuppressWarnings("unchecked")
    volatile Reference queueNext;

    /**
     * Used internally by the VM.  This field forms a circular and
     * singly linked list of reference objects discovered by the
     * garbage collector and awaiting processing by the reference
     * queue thread.
     *
     * @hide
     */
    public volatile Reference<?> pendingNext;

    /**
     * Constructs a new instance of this class.
     */
    Reference() {
    }

    Reference(T r, ReferenceQueue<? super T> q) {
        referent = r;
        queue = q;
    }

    /**
     * Makes the referent {@code null}. This does not force the reference
     * object to be enqueued.
     */
    public void clear() {
        referent = null;
    }

    /**
     * Adds an object to its reference queue.
     *
     * @return {@code true} if this call has caused the {@code Reference} to
     * become enqueued, or {@code false} otherwise
     *
     * @hide
     */
    public final synchronized boolean enqueueInternal() {
        if (queue != null && queueNext == null) {
            queue.enqueue(this);
            queue = null;
            return true;
        }
        return false;
    }

    /**
     * Forces the reference object to be enqueued if it has been associated with
     * a queue.
     *
     * @return {@code true} if this call has caused the {@code Reference} to
     * become enqueued, or {@code false} otherwise
     */
    public boolean enqueue() {
        return enqueueInternal();
    }

    /**
     * Returns the referent of the reference object.
     *
     * @return the referent to which reference refers, or {@code null} if the
     *         object has been cleared.
     */
    public T get() {
        return getReferent();
    }

    /**
     * Returns the referent of the reference object.
     *
     * @return the referent to which reference refers, or {@code null} if the
     *         object has been cleared. Required since the compiler
     *         intrisifies getReferent() since we can't intrinsify Reference.get()
     *         due to incorrect devirtualization (and inlining) of PhantomReference.get().
     */
    private final native T getReferent();

    /**
     * Checks whether the reference object has been enqueued.
     *
     * @return {@code true} if the {@code Reference} has been enqueued, {@code
     *         false} otherwise
     */
    public boolean isEnqueued() {
        return queueNext != null;
    }


    // BEGIN Android-added: reachabilityFence() from upstream OpenJDK9+181.
    // The actual implementation differs from OpenJDK9.
    /**
     * Ensures that the object referenced by the given reference remains
     * <a href="package-summary.html#reachability"><em>strongly reachable</em></a>,
     * regardless of any prior actions of the program that might otherwise cause
     * the object to become unreachable; thus, the referenced object is not
     * reclaimable by garbage collection at least until after the invocation of
     * this method.  Invocation of this method does not itself initiate garbage
     * collection or finalization.
     *
     * <p> This method establishes an ordering for
     * <a href="package-summary.html#reachability"><em>strong reachability</em></a>
     * with respect to garbage collection.  It controls relations that are
     * otherwise only implicit in a program -- the reachability conditions
     * triggering garbage collection.  This method is designed for use in
     * uncommon situations of premature finalization where using
     * {@code synchronized} blocks or methods, or using other synchronization
     * facilities are not possible or do not provide the desired control.  This
     * method is applicable only when reclamation may have visible effects,
     * which is possible for objects with finalizers (See
     * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.6">
     * Section 12.6 17 of <cite>The Java&trade; Language Specification</cite></a>)
     * that are implemented in ways that rely on ordering control for correctness.
     *
     * @apiNote
     * Finalization may occur whenever the virtual machine detects that no
     * reference to an object will ever be stored in the heap: The garbage
     * collector may reclaim an object even if the fields of that object are
     * still in use, so long as the object has otherwise become unreachable.
     * This may have surprising and undesirable effects in cases such as the
     * following example in which the bookkeeping associated with a class is
     * managed through array indices.  Here, method {@code action} uses a
     * {@code reachabilityFence} to ensure that the {@code Resource} object is
     * not reclaimed before bookkeeping on an associated
     * {@code ExternalResource} has been performed; in particular here, to
     * ensure that the array slot holding the {@code ExternalResource} is not
     * nulled out in method {@link Object#finalize}, which may otherwise run
     * concurrently.
     *
     * <pre> {@code
     * class Resource {
     *   private static ExternalResource[] externalResourceArray = ...
     *
     *   int myIndex;
     *   Resource(...) {
     *     myIndex = ...
     *     externalResourceArray[myIndex] = ...;
     *     ...
     *   }
     *   protected void finalize() {
     *     externalResourceArray[myIndex] = null;
     *     ...
     *   }
     *   public void action() {
     *     try {
     *       // ...
     *       int i = myIndex;
     *       Resource.update(externalResourceArray[i]);
     *     } finally {
     *       Reference.reachabilityFence(this);
     *     }
     *   }
     *   private static void update(ExternalResource ext) {
     *     ext.status = ...;
     *   }
     * }}</pre>
     *
     * Here, the invocation of {@code reachabilityFence} is nonintuitively
     * placed <em>after</em> the call to {@code update}, to ensure that the
     * array slot is not nulled out by {@link Object#finalize} before the
     * update, even if the call to {@code action} was the last use of this
     * object.  This might be the case if, for example a usage in a user program
     * had the form {@code new Resource().action();} which retains no other
     * reference to this {@code Resource}.  While probably overkill here,
     * {@code reachabilityFence} is placed in a {@code finally} block to ensure
     * that it is invoked across all paths in the method.  In a method with more
     * complex control paths, you might need further precautions to ensure that
     * {@code reachabilityFence} is encountered along all of them.
     *
     * <p> It is sometimes possible to better encapsulate use of
     * {@code reachabilityFence}.  Continuing the above example, if it were
     * acceptable for the call to method {@code update} to proceed even if the
     * finalizer had already executed (nulling out slot), then you could
     * localize use of {@code reachabilityFence}:
     *
     * <pre> {@code
     * public void action2() {
     *   // ...
     *   Resource.update(getExternalResource());
     * }
     * private ExternalResource getExternalResource() {
     *   ExternalResource ext = externalResourceArray[myIndex];
     *   Reference.reachabilityFence(this);
     *   return ext;
     * }}</pre>
     *
     * <p> Method {@code reachabilityFence} is not required in constructions
     * that themselves ensure reachability.  For example, because objects that
     * are locked cannot, in general, be reclaimed, it would suffice if all
     * accesses of the object, in all methods of class {@code Resource}
     * (including {@code finalize}) were enclosed in {@code synchronized (this)}
     * blocks.  (Further, such blocks must not include infinite loops, or
     * themselves be unreachable, which fall into the corner case exceptions to
     * the "in general" disclaimer.)  However, method {@code reachabilityFence}
     * remains a better option in cases where this approach is not as efficient,
     * desirable, or possible; for example because it would encounter deadlock.
     *
     * @param ref the reference. If {@code null}, this method has no effect.
     * @since 9
     */
    // @DontInline
    public static void reachabilityFence(Object ref) {
        // This code is usually replaced by much faster intrinsic implementations.
        // It will be executed for tests run with the access checks interpreter in
        // ART, e.g. with --verify-soft-fail.  Since this is a volatile store, it
        // cannot easily be moved up past prior accesses, even if this method is
        // inlined.
        SinkHolder.sink = ref;
        // Leaving SinkHolder set to ref is unpleasant, since it keeps ref live
        // until the next reachabilityFence call. This causes e.g. 036-finalizer
        // to fail. Clear it again in a way that's unlikely to be optimizable.
        // The fact that finalize_count is volatile makes it hard to move the test up.
        if (SinkHolder.finalize_count == 0) {
            SinkHolder.sink = null;
        }
    }
    private static class SinkHolder {
        static volatile Object sink;
        // Ensure that sink looks live to even a reasonably clever compiler.
        private static volatile int finalize_count = 0;
        private static Object sinkUser = new Object() {
            protected void finalize() {
                if (sink == null && finalize_count > 0) {
                    throw new AssertionError("Can't get here");
                }
                finalize_count++;
            }
        };
    }
    // END Android-added: reachabilityFence() from upstream OpenJDK9+181.

}
