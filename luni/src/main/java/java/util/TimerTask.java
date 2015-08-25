/*
 * Copyright (C) 2015 The Android Open Source Project
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

package java.util;

/**
 * Task to be scheduled in a {@link Timer}.
 *
 * Is it assumed that each {@link TimerTask} is at most once in a {@link Timer} and in at most
 *
 */
public abstract class TimerTask implements Runnable {
    private long scheduledExecutionTime = -1;
    private boolean[] cancelled = { false };
    private boolean scheduled;

    protected TimerTask() {

    }

    /** Inherited from {@link Runnable} */
    public abstract void run();

    /**
     * Cancels this task. Guarantees that this task will never run again.
     *
     * If this task is currently running, the current run() will be allowed to complete.
     * Can be called more than once. Will be a no-op after the first call.
     *
     * Returns true iff. at least one run of the task was cancelled. Note that if the given task wasn’t
     * scheduled, there’s nothing to cancel (by definition) so this method must return false in that case.
     */
    public boolean cancel() {
        synchronized(cancelled) {
            if (cancelled[0])
                return false;
            cancelled[0] = true;
            return scheduled;
        }
    }

    // Returns the time at which the *most recent* execution of this task was *scheduled* to occur.
    // (as opposed to when it really occurred - the two can differ if the execution thread is busy).
    //
    // This is typically used within the run() method to find out how “late” the task is executed.
    //
    // The return value is undefined for tasks that have never been run.
    public long scheduledExecutionTime() {
        return scheduledExecutionTime;
    }

    boolean isCancelled() {
        return cancelled[0];
    }

    void setScheduledExecutionTime(long timeMillis) {
        this.scheduledExecutionTime = timeMillis;
    }

    void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
