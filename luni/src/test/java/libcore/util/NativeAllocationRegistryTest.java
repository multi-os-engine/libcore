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
 * limitations under the License.
 */

package libcore.util;

import junit.framework.TestCase;

public class NativeAllocationRegistryTest extends TestCase {

    static {
        System.loadLibrary("javacoretests");
    }

    private static class TestConfig {
        public boolean useAllocator;
        public boolean shareRegistry;

        public TestConfig(boolean useAllocator, boolean shareRegistry) {
            this.useAllocator = useAllocator;
            this.shareRegistry = shareRegistry;
        }
    }

    private static class Allocation {
        public byte[] javaAllocation;
        public long nativeAllocation;
    }

    // Verify that NativeAllocations and their referents are freed before we run
    // out of space for new allocations.
    private void testNativeAllocation(TestConfig config) {
        System.gc();
        final long max = Runtime.getRuntime().maxMemory();
        final long total = Runtime.getRuntime().totalMemory();
        final int size = 1024*1024;
        final int expectedMaxNumAllocations = (int)(max-total)/size;
        final int numSavedAllocations = expectedMaxNumAllocations/2;
        Allocation[] saved = new Allocation[numSavedAllocations];

        final int nativeSize = size/2;
        final int javaSize = size/2;
        NativeAllocationRegistry registry = new NativeAllocationRegistry(
                getNativeFinalizer(), nativeSize);

        // Allocate more native allocations than will fit in memory. This should
        // not throw OutOfMemoryError because the few allocations we save
        // references to should easily fit.
        for (int i = 0; i < expectedMaxNumAllocations * 10; i++) {
            if (!config.shareRegistry) {
                registry = new NativeAllocationRegistry(getNativeFinalizer(), nativeSize);
            }

            final Allocation alloc = new Allocation();
            alloc.javaAllocation = new byte[javaSize];
            if (config.useAllocator) {
                NativeAllocationRegistry.Allocator allocator
                  = new NativeAllocationRegistry.Allocator() {
                    public long allocate() {
                        alloc.nativeAllocation = doNativeAllocation(nativeSize);
                        return alloc.nativeAllocation;
                    }
                };
                registry.registerNativeAllocation(alloc, allocator);
            } else {
                alloc.nativeAllocation = doNativeAllocation(nativeSize);
                registry.registerNativeAllocation(alloc, alloc.nativeAllocation);
            }

            saved[i%numSavedAllocations] = alloc;
        }

        // Verify most of the allocations have been freed.
        assertTrue(getNumAllocations() < expectedMaxNumAllocations * 2);
    }

    public void testNativeAllocationAllocatorAndSharedRegistry() {
        testNativeAllocation(new TestConfig(true, true));
    }

    public void testNativeAllocationNoAllocatorAndSharedRegistry() {
        testNativeAllocation(new TestConfig(false, true));
    }

    public void testNativeAllocationAllocatorAndNoSharedRegistry() {
        testNativeAllocation(new TestConfig(true, false));
    }

    public void testNativeAllocationNoAllocatorAndNoSharedRegistry() {
        testNativeAllocation(new TestConfig(false, false));
    }

    private static native long getNativeFinalizer();
    private static native long doNativeAllocation(long size);
    private static native long getNumAllocations();
}
