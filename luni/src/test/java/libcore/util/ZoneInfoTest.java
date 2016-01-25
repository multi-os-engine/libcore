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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import junit.framework.TestCase;
import libcore.io.BufferIterator;

/**
 * Tests for {@link ZoneInfo}
 */
public class ZoneInfoTest extends TestCase {

    /**
     * No transitions should prevent the {@link ZoneInfo} being created.
     */
    public void testMakeTimeZone_NoTransitions() throws IOException {

        int[][] times = {};
        int[][] offsets = {};

        try {
            createZoneInfo(times, offsets);
            fail("Did not detect no transitions");
        } catch (IllegalStateException expected) {
            // Expected this to happen
        }
    }

    /**
     * One non-DST transition is all that is required to work.
     */
    public void testMakeTimeZone_OneNonDstTransition() throws IOException {

        int[][] times = {
                {0, 0}
        };
        int[][] offsets = {
                {3600, 0}
        };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets);
        // Any time before the first transition is assumed to use the first standard transition.
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(-2)));
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(0));
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(2)));
        assertFalse("Doesn't use DST", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());
        assertEquals(secondsInMillis(3600), zoneInfo.getRawOffset());
    }

    /**
     * One DST transition is not sufficient.
     */
    public void testMakeTimeZone_OneDstTransition() throws IOException {

        int[][] times = {
                {0, 0}
        };
        int[][] offsets = {
                {3600, 1}
        };

        try {
            createZoneInfo(times, offsets);
            fail("Did not detect no non-DST transitions");
        } catch (IllegalStateException expected) {
            // Expected this to happen
        }
    }

    /**
     * Check to make sure that rounding the time from milliseconds to seconds does not cause issues
     * around the boundary of negative transitions.
     */
    public void testMakeTimeZone_NegativeTransition() throws IOException {

        int[][] times = {
                {-2000, 0},
                {-5, 1},
                {0, 2},
        };
        int[][] offsets = {
                {1800, 0},
                {3600, 1},
                {5400, 0}
        };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets);

        // Even a millisecond before a transition means that the transition is not active.
        assertEquals(1800000, zoneInfo.getOffset(secondsInMillis(-5) - 1));
        assertFalse(zoneInfo.inDaylightTime(new Date(secondsInMillis(-5) - 1)));

        // A time equal to the transition point activates the transition.
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(-5)));
        assertTrue(zoneInfo.inDaylightTime(new Date(secondsInMillis(-5))));

        // A time after the transition point but before the next activates the transition.
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(-5) + 1));
        assertTrue(zoneInfo.inDaylightTime(new Date(secondsInMillis(-5) + 1)));

        assertFalse("Doesn't use DST", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());
        assertEquals(secondsInMillis(5400), zoneInfo.getRawOffset());
    }

    /**
     * Check to make sure that rounding the time from milliseconds to seconds does not cause issues
     * around the boundary of positive transitions.
     */
    public void testMakeTimeZone_PositiveTransition() throws IOException {

        int[][] times = {
                {0, 0},
                {5, 1},
                {2000, 2},
        };
        int[][] offsets = {
                {1800, 0},
                {3600, 1},
                {5400, 0}
        };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets);

        // Even a millisecond before a transition means that the transition is not active.
        assertEquals(secondsInMillis(1800), zoneInfo.getOffset(secondsInMillis(5) - 1));
        assertFalse(zoneInfo.inDaylightTime(new Date(secondsInMillis(5) - 1)));

        // A time equal to the transition point activates the transition.
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(5)));
        assertTrue(zoneInfo.inDaylightTime(new Date(secondsInMillis(5))));

        // A time after the transition point but before the next activates the transition.
        assertEquals(secondsInMillis(3600), zoneInfo.getOffset(secondsInMillis(5) + 1));
        assertTrue(zoneInfo.inDaylightTime(new Date(secondsInMillis(5) + 1)));

        assertFalse("Doesn't use DST", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());
        assertEquals(secondsInMillis(5400), zoneInfo.getRawOffset());
    }

    /**
     * Check that creating a {@link ZoneInfo} with future DST transitions but no past DST
     * transitions where the transition times are negative is not affected by rounding issues.
     */
    public void testMakeTimeZone_HasFutureDST_NoPastDST_NegativeTransitions() throws IOException {

        int[][] times = {
                {-2000, 0},
                {-500, 1},
                {-100, 2},
        };
        int[][] offsets = {
                {1800, 0},
                {3600, 0},
                {5400, 1}
        };

        // The expected DST savings is the difference between the DST offset (which includes the
        // raw offset) and the preceding non-DST offset (which should just be the raw offset).
        // Or in other words (5400 - 3600) * 1000
        int expectedDSTSavings = secondsInMillis(5400 - 3600);

        ZoneInfo zoneInfo = createZoneInfo(times, offsets, secondsInMillis(-700));

        assertTrue("Should use DST but doesn't", zoneInfo.useDaylightTime());
        assertEquals(expectedDSTSavings, zoneInfo.getDSTSavings());

        // Now create one a few milliseconds before the DST transition to make sure that rounding
        // errors don't cause a problem.
        zoneInfo = createZoneInfo(times, offsets, secondsInMillis(-100) - 5);

        assertTrue("Should use DST but doesn't", zoneInfo.useDaylightTime());
        assertEquals(expectedDSTSavings, zoneInfo.getDSTSavings());
    }

    /**
     * Check that creating a {@link ZoneInfo} with future DST transitions but no past DST
     * transitions where the transition times are positive is not affected by rounding issues.
     */
    public void testMakeTimeZone_HasFutureDST_NoPastDST_PositiveTransitions() throws IOException {

        int[][] times = {
                {4000, 0},
                {5500, 1},
                {6000, 2},
        };
        int[][] offsets = {
                {1800, 0},
                {3600, 0},
                {7200, 1}
        };

        // The expected DST savings is the difference between the DST offset (which includes the
        // raw offset) and the preceding non-DST offset (which should just be the raw offset).
        // Or in other words (7200 - 3600) * 1000
        int expectedDSTSavings = secondsInMillis(7200 - 3600);


        ZoneInfo zoneInfo = createZoneInfo(times, offsets, secondsInMillis(4500));

        assertTrue("Should use DST but doesn't", zoneInfo.useDaylightTime());
        assertEquals(expectedDSTSavings, zoneInfo.getDSTSavings());

        // Now create one a few milliseconds before the DST transition to make sure that rounding
        // errors don't cause a problem.
        zoneInfo = createZoneInfo(times, offsets, secondsInMillis(6000) - 5);

        assertTrue("Should use DST but doesn't", zoneInfo.useDaylightTime());
        assertEquals(expectedDSTSavings, zoneInfo.getDSTSavings());
    }

    /**
     * Check that creating a {@link ZoneInfo} with past DST transitions but no future DST
     * transitions where the transition times are negative is not affected by rounding issues.
     */
    public void testMakeTimeZone_HasPastDST_NoFutureDST_NegativeTransitions() throws IOException {

        int[][] times = {
                {-5000, 0},
                {-2000, 1},
                {-500, 0},
                {0, 2},
        };
        int[][] offsets = {
                {3600, 0},
                {1800, 1},
                {5400, 0}
        };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets, secondsInMillis(-1));

        assertFalse("Shouldn't use DST but does", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());

        // Now create one a few milliseconds after the DST transition to make sure that rounding
        // errors don't cause a problem.
        zoneInfo = createZoneInfo(times, offsets, secondsInMillis(-2000) + 5);

        assertFalse("Shouldn't use DST but does", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());
    }

    /**
     * Check that creating a {@link ZoneInfo} with past DST transitions but no future DST
     * transitions where the transition times are positive is not affected by rounding issues.
     */
    public void testMakeTimeZone_HasPastDST_NoFutureDST_PositiveTransitions() throws IOException {

        int[][] times = {
                {1000, 0},
                {4000, 1},
                {5500, 0},
                {6000, 2},
        };
        int[][] offsets = {
                {3600, 0},
                {1800, 1},
                {5400, 0}
        };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets, secondsInMillis(4700));

        assertFalse("Shouldn't use DST but does", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());

        // Now create one a few milliseconds after the DST transition to make sure that rounding
        // errors don't cause a problem.
        zoneInfo = createZoneInfo(times, offsets, secondsInMillis(4000) + 5);

        assertFalse("Shouldn't use DST but does", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());
    }

    /**
     * Checks to make sure that it can handle up to 256 offsets.
     */
    public void testMakeTimeZone_LotsOfOffsets() throws IOException {

        int[][] times = {
                {-2000, 255},
        };
        int[][] offsets = new int[256][];
        Arrays.fill(offsets, new int[2]);
        offsets[255] = new int[] { 3600, 0 };

        ZoneInfo zoneInfo = createZoneInfo(times, offsets, Integer.MIN_VALUE);

        assertFalse("Shouldn't use DST but does", zoneInfo.useDaylightTime());
        assertEquals(0, zoneInfo.getDSTSavings());

        // Make sure that WallTime works properly with a ZoneInfo with 256 offsets.
        ZoneInfo.WallTime wallTime = new ZoneInfo.WallTime();
        wallTime.localtime(0, zoneInfo);
        wallTime.mktime(zoneInfo);
    }

    /**
     * Checks to make sure that it rejects more than 256 offsets.
     */
    public void testMakeTimeZone_TooManyOffsets() throws IOException {

        int[][] times = {
                {-2000, 255},
        };
        int[][] offsets = new int[257][];
        Arrays.fill(offsets, new int[2]);
        offsets[255] = new int[] { 3600, 0 };

        try {
            createZoneInfo(times, offsets);
            fail("Did not detect too many offsets");
        } catch (IllegalStateException expected) {
            // Expected this to happen
        }
    }

    /**
     * Create an instance for every available time zone for which we have data to ensure that they
     * can all be handled correctly.
     *
     * <p>This is to ensure that ZoneInfo can read all time zone data without failing, it doesn't
     * check that it reads it correctly or that the data itself is correct. This is a sanity test
     * to ensure that any additional checks added to the code that reads the data source and
     * creates the {@link ZoneInfo} instances does not prevent any of the time zones being loaded.
     */
    public void testMakeTimeZone_All() throws IOException {
        ZoneInfoDB.TzData instance = ZoneInfoDB.getInstance();
        String[] availableIDs = instance.getAvailableIDs();
        Arrays.sort(availableIDs);
        for (String id : availableIDs) {
            BufferIterator bufferIterator = instance.getBufferIterator(id);

            // Create a ZoneInfo at the earliest possible time to allow us to use the
            // useDaylightTime() method to check whether it ever has or ever will support daylight
            // savings time.
            ZoneInfo zoneInfo = ZoneInfo.makeTimeZone(id, bufferIterator, Long.MIN_VALUE);
            assertNotNull("TimeZone " + id + " was not created", zoneInfo);
            assertEquals(id, zoneInfo.getID());
        }
    }

    /**
     * Check that we can read the serialized form of a {@link ZoneInfo} created in pre-OpenJDK
     * AOSP.
     *
     * <p>One minor difference is that in pre-OpenJDK {@link ZoneInfo#mDstSavings} can be non-zero
     * even if {@link ZoneInfo#mUseDst} was false. That was not visible externally (except through
     * the {@link ZoneInfo#toString()} method) as the {@link ZoneInfo#getDSTSavings()} would check
     * {@link ZoneInfo#mUseDst} and if it was false then would return 0. This checks to make sure
     * that is handled properly. See {@link ZoneInfo#readObject(ObjectInputStream)}.
     */
    public void testReadSerialized() throws IOException, ClassNotFoundException {

        ZoneInfo zoneInfoRead;
        try (InputStream is = getClass().getResourceAsStream("ZoneInfoTest_ZoneInfo.golden.ser");
             ObjectInputStream ois = new ObjectInputStream(is)) {
            Object object = ois.readObject();
            assertTrue("Not a ZoneInfo instance", object instanceof ZoneInfo);
            zoneInfoRead = (ZoneInfo) object;
        }

        int[][] times = {
                {-5000, 0},
                {-2000, 1},
                {-500, 0},
                {0, 2},
        };
        int[][] offsets = {
                {3600, 0},
                {1800, 1},
                {5400, 0}
        };

        ZoneInfo zoneInfoCreated = createZoneInfo(times, offsets, secondsInMillis(-1));

        assertEquals("Read ZoneInfo does not match created one", zoneInfoCreated, zoneInfoRead);
        assertEquals("useDaylightTime() mismatch",
                zoneInfoCreated.useDaylightTime(), zoneInfoRead.useDaylightTime());
        assertEquals("getDSTSavings() mismatch",
                zoneInfoCreated.getDSTSavings(), zoneInfoRead.getDSTSavings());
    }

    private static int secondsInMillis(int seconds) {
        return seconds * 1000;
    }

    private ZoneInfo createZoneInfo(int[][] transitionTimes, int[][] transitionTypes)
            throws IOException {
        return createZoneInfo(transitionTimes, transitionTypes, System.currentTimeMillis());
    }

    private ZoneInfo createZoneInfo(int[][] transitionTimes, int[][] transitionTypes,
            long currentTimeMillis)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Magic number.
        writeInt(baos, 0x545a6966);

        // Some useless stuff in the header.
        for (int i = 0; i < 28; ++i) {
            baos.write(i);
        }

        // Transition time count
        writeInt(baos, transitionTimes.length);
        // Transition type count.
        writeInt(baos, transitionTypes.length);
        // Useless stuff.
        writeInt(baos, 0xdeadbeef);

        // Transition time array, as ints.
        for (int[] transitionTime : transitionTimes) {
            int transition = transitionTime[0];
            writeInt(baos, transition);
        }

        // Transition type array.
        for (int[] transitionTime : transitionTimes) {
            byte type = (byte) transitionTime[1];
            baos.write(type);
        }

        for (int i = 0; i < transitionTypes.length; i++) {
            int[] transitionType = transitionTypes[i];
            int offset = transitionType[0];
            byte dst = (byte) transitionType[1];
            writeInt(baos, offset);
            baos.write(dst);

            // Useless stuff.
            baos.write(i);
        }

        return ZoneInfo.makeTimeZone("TimeZone for '" + getName() + "'",
                new ByteBufferIterator(ByteBuffer.wrap(baos.toByteArray())), currentTimeMillis);
    }

    private static void writeInt(OutputStream os, int value) throws IOException {
        byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
        os.write(bytes);
    }

    /**
     * A {@link BufferIterator} that wraps a {@link ByteBuffer}.
     */
    private static class ByteBufferIterator extends BufferIterator {

        private final ByteBuffer buffer;

        public ByteBufferIterator(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void seek(int offset) {
            buffer.position(offset);
        }

        @Override
        public void skip(int byteCount) {
            buffer.position(buffer.position() + byteCount);
        }

        @Override
        public void readByteArray(byte[] dst, int dstOffset, int byteCount) {
            buffer.get(dst, dstOffset, byteCount);
        }

        @Override
        public byte readByte() {
            return buffer.get();
        }

        @Override
        public int readInt() {
            int value = buffer.asIntBuffer().get();
            // Using a separate view does not update the position of this buffer so do it
            // explicitly.
            skip(4);
            return value;
        }

        @Override
        public void readIntArray(int[] dst, int dstOffset, int intCount) {
            buffer.asIntBuffer().get(dst, dstOffset, intCount);
            // Using a separate view does not update the position of this buffer so do it
            // explicitly.
            skip(4 * intCount);
        }

        @Override
        public short readShort() {
            short value = buffer.asShortBuffer().get();
            // Using a separate view does not update the position of this buffer so do it
            // explicitly.
            skip(2);
            return value;
        }
    }
}
