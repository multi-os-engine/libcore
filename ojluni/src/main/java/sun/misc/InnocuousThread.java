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

package sun.misc;

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

import sun.misc.Unsafe;

/**
 * A thread that has no permissions, is not a member of any user-defined
 * ThreadGroup and supports the ability to erase ThreadLocals.
 *
 * @implNote Based on the implementation of InnocuousForkJoinWorkerThread.
 */
public final class InnocuousThread extends Thread {
    private static final Unsafe UNSAFE;
    private static final ThreadGroup THREADGROUP;
    private static final AccessControlContext ACC;
    private static final long THREADLOCALS;
    private static final long INHERITABLETHREADLOCALS;
    private static final long INHERITEDACCESSCONTROLCONTEXT;

    public InnocuousThread(Runnable target) {
        super(THREADGROUP, target, "anInnocuousThread");
        UNSAFE.putOrderedObject(this, INHERITEDACCESSCONTROLCONTEXT, ACC);
        eraseThreadLocals();
    }

    @Override
    public ClassLoader getContextClassLoader() {
        // always report system class loader
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler x) {
        // silently fail
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        throw new SecurityException("setContextClassLoader");
    }

    // ensure run method is run only once
    private volatile boolean hasRun;

    @Override
    public void run() {
        if (Thread.currentThread() == this && !hasRun) {
            hasRun = true;
            super.run();
        }
    }

    /**
     * Drops all thread locals (and inherited thread locals).
     */
    public void eraseThreadLocals() {
        UNSAFE.putObject(this, THREADLOCALS, null);
        UNSAFE.putObject(this, INHERITABLETHREADLOCALS, null);
    }

    // Use Unsafe to access Thread group and ThreadGroup parent fields
    static {
        try {
            ACC = new AccessControlContext(new ProtectionDomain[] {
                new ProtectionDomain(null, null)
            });

            // Find and use topmost ThreadGroup as parent of new group
            UNSAFE = Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            Class<?> gk = ThreadGroup.class;

            THREADLOCALS = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocals"));
            INHERITABLETHREADLOCALS = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("inheritableThreadLocals"));
            INHERITEDACCESSCONTROLCONTEXT = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("inheritedAccessControlContext"));

            long tg = UNSAFE.objectFieldOffset(tk.getDeclaredField("group"));
            long gp = UNSAFE.objectFieldOffset(gk.getDeclaredField("parent"));
            ThreadGroup group = (ThreadGroup)
                UNSAFE.getObject(Thread.currentThread(), tg);

            while (group != null) {
                ThreadGroup parent = (ThreadGroup)UNSAFE.getObject(group, gp);
                if (parent == null)
                    break;
                group = parent;
            }
            THREADGROUP = new ThreadGroup(group, "InnocuousThreadGroup");
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
