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

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to schedule tasks in a separate thread.
 *
 * <p>Each timer is associated with precisely one “execution thread”, which it uses to run
 * {@code TimerTask} instances serially. Scheduled {@code TimerTask} instances may either be
 * one-shot or recurring. Recurring tasks can be scheduled as fixed-period with {@link #schedule}
 * or as fixed-rate with {@link #scheduleAtFixedRate}.
 *
 * <p><Fixed period tasks are scheduled with respect to the time of the last run, whereas
 * fixed rate tasks do not take the last task run into account.
 *
 * <p>It is thread safe in the sense that several threads can operate on one instance at the same
 * time.
 */

public class Timer {
    // Counter to create unique names.
    private static final AtomicLong NAME_COUNTER = new AtomicLong();
    private final String name;
    private final Thread executionThread;
    // Use an {@code AtomicBoolean} to communicate the mutable value with the timer thread.
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    // Semaphore for the scheduling thread to notify the execution thread about changes
    // (cancellation, new tasks). Each permit indicates a change that might affect the
    // execution thread.
    private final Semaphore executionThreadSemaphore = new Semaphore(0);
    // Store or scheduled tasks ordered by next execution time.
    private final PriorityBlockingQueue<ScheduledTask> scheduledTasksPriorityQueue =
            new PriorityBlockingQueue<ScheduledTask>(
                    10 /* initialCapacity */,
                    new Comparator<ScheduledTask>() {
                        public int compare(ScheduledTask t1, ScheduledTask t2) {
                            return Long.compare(t1.nextExecution, t2.nextExecution);
                        }
                    });
    // Lock to synchronize on for operations changing members of the class.
    private final Object lock = new Object();

    /**
     * Create a new timer with name {@code name}. {@code isDaemon} specifies whether the
     * execution thread is a daemon or not.
     */
    public Timer(String name, boolean isDaemon) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        this.name = name;
        executionThread = new Thread(new ExecutionThreadRunnable(
                this, executionThreadSemaphore, scheduledTasksPriorityQueue, lock, isCancelled));
        executionThread.setDaemon(isDaemon);
        executionThread.setName(this.name);
        executionThread.start();
    }

    /** Equivalent to {@code this(name, false)}. */
    public Timer(String name) {
        this(name, false);
    }

    /** Creates a new timer with a default (unique) name. */
    public Timer(boolean isDaemon) {
        this("executionTimerThread" + NAME_COUNTER.getAndIncrement(), isDaemon);
    }

    /** Equivalent to {@code this(false)} */
    public Timer() {
        this(false);
    }

    /**
     * Make sure the thread is stopped when the timer is garbage-collected.
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cancel();
    }

    /**
     * Cancels all remaining tasks on the timer.
     *
     * The task that’s currently running (if any) must be allowed to complete. No further tasks can
     * be enqueued on this timer. Also, Releases all resources associated with this timer. All
     * subsequent calls to methods on this instance must be no-ops.
     */
    public void cancel() {
        isCancelled.set(true);
        // Notify the execution thread about the cancellation.
        executionThreadSemaphore.release();
    }

    /**
     * Removes all references held by this instance to cancelled timer-tasks.
     *
     * Those tasks will be collectible at the end of this method if no other references to them
     * exist. Returns the number of tasks purged.
     */
    public int purge() {
        if (isCancelled.get()) {
            return 0;
        }
        List<ScheduledTask> tasksToRemove = new LinkedList<ScheduledTask>();
        synchronized(lock) {
            for (ScheduledTask scheduledTask : scheduledTasksPriorityQueue) {
                if (scheduledTask.timerTask.isCancelled()) {
                    tasksToRemove.add(scheduledTask);
                }
            }
            scheduledTasksPriorityQueue.removeAll(tasksToRemove);
        }
        return tasksToRemove.size();
    }

    // Valid date for scheduling to be used as default in a convenience method.
    private static final Date VALID_DATE = new Date(System.currentTimeMillis());

    /** Schedules a one-shot task at a fixed time {@code when}. */
    public void schedule(TimerTask task, Date when) {
        validateScheduling(task, when, 1, 1);
        doSchedule(task, when.getTime(), null);
    }

    /** Schedules a one-shot task after a fixed delay {@code delay}, specified in milliseconds. */
    public void schedule(TimerTask task, long delay) {
        validateScheduling(task, VALID_DATE, delay, 1);
        doSchedule(task, System.currentTimeMillis() + delay, null);
    }

    /**
     * Schedules a recurring fixed-period task after a fixed delay {@code delay}
     * with period {@code period}.
     *
     * Both delay and period are specified in milliseconds.
     */
    public void schedule(TimerTask task, long delay, long period) {
        validateScheduling(task, VALID_DATE, delay, period);
        doSchedule(task, System.currentTimeMillis() + delay, period);
    }

    /**
     * Schedules a recurring fixed-period task after a fixed time {@code when} with period
     * {@code period}.

     * Period is specified in milliseconds.
     */
    public void schedule(TimerTask task, Date when, long period) {
        validateScheduling(task, when, 1, period);
        doSchedule(task, when.getTime(), period);
    }

    /**
     * Schedules a recurring fixed-rate task after a fixed delay {@code delay}
     * with period {@code period}.
     *
     * Both delay and period are specified in milliseconds.
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        validateScheduling(task, VALID_DATE, delay, period);
        doSchedule(task, System.currentTimeMillis() + delay, period, true);
    }

    /**
     * Schedules a recurring fixed-rate task after a fixed time {@code when}
     * with period {@code period}.
     *
     * Period is specified in milliseconds.
     */
    public void scheduleAtFixedRate(TimerTask task, Date when, long period) {
        validateScheduling(task, when, 1, period);
        doSchedule(task, when.getTime(), period, true);
    }

    // Throws runtime exception in case of invalid parameters.
    private void validateScheduling(TimerTask task, long delay, long period) {
        validateScheduling(task, new Date(1), delay, period);
    }

    // Throws runtime exception in case of invalid parameters.
    private void validateScheduling(TimerTask task, Date when, long delay, long period) {
        if (period <= 0) {
            throw new IllegalArgumentException(
                    "Scheduling task with non-positive period in timer [" + name + "]");
        }
        if (delay < 0) {
            throw new IllegalArgumentException(
                    "Scheduling task with negative delay in timer [" + name + "]");
        }
        if (when == null) {
            throw new NullPointerException();
        }
        if (when.getTime() < 0) {
            throw new IllegalArgumentException(
                    "Date before the epoch scheduling task in timer [" + name + "]");
        }
        if (task == null) {
            throw new NullPointerException();
        }
    }

    private void doSchedule(TimerTask timerTask, Long nextExecution, Long period) {
        doSchedule(timerTask, nextExecution, period, false);
    }

    private void doSchedule(
            TimerTask timerTask, Long nextExecution, Long period, boolean isAtFixedRate) {
        if (isCancelled.get()) {
            throw new IllegalStateException(
                    "Trying to schedule task in timer [" + name + "], which is already cancelled");
        }
        if (timerTask.isCancelled()) {
            throw new IllegalStateException(
                    "Trying to schedule cancelled task in timer [" + name + "]");
        }
        timerTask.setScheduled(true);
        synchronized(lock) {
            scheduledTasksPriorityQueue.add(
                    new ScheduledTask(timerTask, nextExecution, period, isAtFixedRate));
        }
        executionThreadSemaphore.release();
    }

    private static class ScheduledTask {
        final TimerTask timerTask;
        final Long period;
        final boolean isAtFixedRate;
        Long nextExecution;

        ScheduledTask(
                TimerTask timerTask, Long nextExecution, Long period, boolean isAtFixedRate) {
            this.timerTask = timerTask;
            this.nextExecution = nextExecution;
            this.period = period;
            this.isAtFixedRate = isAtFixedRate;
        }
    }

    private static class ExecutionThreadRunnable implements Runnable {
        private final Timer timer;
        private final Semaphore timerThreadSemaphore;
        private final PriorityBlockingQueue<ScheduledTask> scheduledTaskPriorityQueue;
        private final Object priorityQueueLock;
        private final AtomicBoolean isCancelled;

        private ExecutionThreadRunnable(
                Timer timer,
                Semaphore timerThreadSemaphore,
                PriorityBlockingQueue<ScheduledTask> scheduledTaskPriorityQueue,
                // Access to the queue must be synchronized on this object.
                Object priorityQueueLock,
                AtomicBoolean isCancelled) {
            this.timer = timer;
            this.timerThreadSemaphore = timerThreadSemaphore;
            this.scheduledTaskPriorityQueue = scheduledTaskPriorityQueue;
            this.priorityQueueLock = priorityQueueLock;
            this.isCancelled = isCancelled;
        }

        public void run() {
            while(!isCancelled.get()) {
                ScheduledTask nextTask = scheduledTaskPriorityQueue.peek();
                Long delayUntilNextTask = computeDelayUntilNextTask(nextTask);
                boolean executeNextTask = false;
                try {
                    if (delayUntilNextTask != null) {
                        // There is a task to execute in the future.
                        // If the task is scheduled to execute right now, delayUntilNextTask is 0.
                        // In that case, check for a signal but do not block (allows to cancel the
                        // timer even in case a task keeps it busy all the time).
                        executeNextTask = !timerThreadSemaphore.tryAcquire(
                                delayUntilNextTask, TimeUnit.MILLISECONDS);
                    } else {
                        timerThreadSemaphore.acquire();
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Unexpected interruption in timer thread ", e);
                }

                if (executeNextTask && !isCancelled.get()) {
                    // We must execute a task right now. Either because at the beginning of the
                    // loop it was time to execute, or because we waited enough time in the
                    // semaphore.
                    runTask(nextTask);
                    updatePriorityQueue(nextTask, System.currentTimeMillis());
                }
                // On the contrary, if the thread acquired from the semaphore, restart the loop
                // as the scheduling thread signalled a change in the scheduled tasks (or
                // cancellation).
            }
        }

        // In millis. It's zero in case the task was scheduled to execute previous to current time.
        private static Long computeDelayUntilNextTask(ScheduledTask scheduledTask) {
            if (scheduledTask == null) {
                return null;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (scheduledTask.nextExecution < currentTimeMillis) {
                return 0L;
            } else {
                return scheduledTask.nextExecution - currentTimeMillis;
            }
        }

        private void runTask(ScheduledTask sc) {
            try {
                sc.timerTask.updateScheduledExecutionTimeAndRun(
                        sc.nextExecution,
                        sc.period != null /* hasAnotherFutureExecutionScheduled */);
            } catch (Error | RuntimeException e) {
                timer.cancel();
                throw(e);
            }
        }

        private void updatePriorityQueue(ScheduledTask taskExecuted, long timeFinished) {
            synchronized(priorityQueueLock) {
                scheduledTaskPriorityQueue.remove(taskExecuted);
                if (taskExecuted.period == null) {
                    // Was a one off.
                    return;
                }
                taskExecuted.nextExecution = taskExecuted.period
                        + ((taskExecuted.isAtFixedRate)
                                ? taskExecuted.nextExecution : timeFinished);
                // Adding with a different {@code nextExecution}, may occupy a different place
                // in the priority queue.
                scheduledTaskPriorityQueue.add(taskExecuted);
            }
        }
    }
}
