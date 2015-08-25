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
 * Each timer is associated with precisely one “timer thread”, which it uses to run TimerTasks in
 * serial. TimerTasks may either be one-shot or recurring. Recurring tasks can be scheduled as
 * fixed-period (using the schedule variants) or as fixed-rate (using the scheduleAtFixedRate
 * variants).
 *
 * Fixed period tasks are scheduled with respect to the time of the last run, whereas fixed rate
 * tasks do not take the last task run into account.
 *
 * Must be thread safe.
 */

public class Timer {
    // Counter to create unique names.
    private static final AtomicLong NAME_COUNTER = new AtomicLong();
    private final String name;
    private final Thread timerThread;
    // Use an array as communicate the mutable value with the timer thread.
    private AtomicBoolean isCancelled = new AtomicBoolean(false);
    // Semaphore for the scheduling thread to notify the timer thread about changes
    // (cancellation, new tasks).
    private final Semaphore timerThreadSemaphore = new Semaphore(0);
    // Store scheduled tasks, ordered by next time of execution.
    private final PriorityBlockingQueue<ScheduledTask> scheduledTasksPriorityQueue;

    /**
     * Create a new timer with name {@code name}. {@code isDaemon} specifies whether the
     * associated thread is a daemon or not.
     */
    public Timer(String name, boolean isDaemon) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        this.name = name;
        // Store scheduled tasks, ordered by next time of execution.
        scheduledTasksPriorityQueue = new PriorityBlockingQueue<ScheduledTask>(10,
                new Comparator<ScheduledTask>() {
                    public int compare(ScheduledTask t1, ScheduledTask t2) {
                        return Long.compare(t1.nextExecution, t2.nextExecution);
                    }
                });
        timerThread = new Thread(new TimerThreadRunnable(
                timerThreadSemaphore, scheduledTasksPriorityQueue, isCancelled));
        timerThread.setDaemon(isDaemon);
        timerThread.setName(this.name);
        timerThread.start();
    }

    /** Equivalent to {@code this(name, false)}. */
    public Timer(String name) {
        this(name, false);
    }

    /** Creates a new timer with a default (unique) name. */
    public Timer(boolean isDaemon) {
        this("timerThread" + NAME_COUNTER.getAndIncrement(), isDaemon);
    }

    /** Equivalent to {@code this(false)} */
    public Timer() {
        this(false);
    }

    public void cancel() {
        synchronized (scheduledTasksPriorityQueue) {
            for (ScheduledTask scheduledTask : scheduledTasksPriorityQueue) {
                scheduledTask.timerTask.cancel();
            }
            scheduledTasksPriorityQueue.clear();
        }
        isCancelled.set(true);
        timerThreadSemaphore.release();
    }

    public int purge() {
        if (isCancelled.get()) {
            return 0;
        }
        synchronized(scheduledTasksPriorityQueue) {
            List<ScheduledTask> tasksToRemove = new LinkedList<ScheduledTask>();
            for (ScheduledTask scheduledTask : scheduledTasksPriorityQueue) {
                if (scheduledTask.timerTask.isCancelled()) {
                    tasksToRemove.add(scheduledTask);
                }
            }
            scheduledTasksPriorityQueue.removeAll(tasksToRemove);
            return tasksToRemove.size();
        }
    }

    // Valid date for scheduling to be used as default in a convenience method.
    private static final Date VALID_DATE = new Date(System.currentTimeMillis());

    // Schedules a one-shot task at a fixed time |when|.
    public void schedule(TimerTask task, Date when) {
        validateScheduling(task, when, 1, 1);
        doSchedule(task, when.getTime(), null);
    }

    // Schedules a one-shot task after a fixed delay |delay|, specified in milliseconds.
    public void schedule(TimerTask task, long delay) {
        validateScheduling(task, VALID_DATE, delay, 1);
        doSchedule(task, System.currentTimeMillis() + delay, null);
    }

    // Schedules a recurring fixed-period task after a fixed delay |delay| with period |period|.
    // Both delay and period are specified in milliseconds.
    public void schedule(TimerTask task, long delay, long period) {
        validateScheduling(task, VALID_DATE, delay, period);
        doSchedule(task, System.currentTimeMillis() + delay, period);
    }

    // Schedules a recurring fixed-period task after a fixed time |when| with period |period|.
    // Period is specified in milliseconds.
    public void schedule(TimerTask task, Date when, long period) {
        validateScheduling(task, when, 1, period);
        doSchedule(task, when.getTime(), period);
    }

    // Schedules a recurring fixed-rate task after a fixed delay |delay| with period |period|.
    // Both delay and period are specified in milliseconds.
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        validateScheduling(task, VALID_DATE, delay, period);
        doSchedule(task, System.currentTimeMillis() + delay, period, true);
    }

    // Schedules a recurring fixed-rate task after a fixed time |when| with period |period|.
    // Period is specified in milliseconds.
    public void scheduleAtFixedRate(TimerTask task, Date when, long period) {
        validateScheduling(task, when, 1, period);
        doSchedule(task, when.getTime(), period, true);
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
        if(when.getTime() < 0) {
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
        scheduledTasksPriorityQueue.add(
                ScheduledTask.create(timerTask, nextExecution, period, isAtFixedRate));
        timerThreadSemaphore.release();
    }

    private static class ScheduledTask {
        private TimerTask timerTask;
        private Long nextExecution;
        private Long period;
        private boolean isAtFixedRate;

        private static ScheduledTask create(
                TimerTask timerTask, Long nextExecution, Long period, boolean isAtFixedRate) {
            ScheduledTask scheduledTask = new ScheduledTask();
            scheduledTask.timerTask = timerTask;
            scheduledTask.nextExecution = nextExecution;
            scheduledTask.period = period;
            scheduledTask.isAtFixedRate = isAtFixedRate;
            return scheduledTask;
        }
    }

    private static class TimerThreadRunnable implements Runnable {
        private final Semaphore timerThreadSemaphore;
        private final PriorityBlockingQueue<ScheduledTask> scheduledTaskPriorityQueue;
        private final AtomicBoolean isCancelled;

        private TimerThreadRunnable (
                Semaphore timerThreadSemaphore,
                PriorityBlockingQueue<ScheduledTask> scheduledTaskPriorityQueue,
                AtomicBoolean isCancelled) {
            this.timerThreadSemaphore = timerThreadSemaphore;
            this.scheduledTaskPriorityQueue = scheduledTaskPriorityQueue;
            this.isCancelled = isCancelled;
        }

        public void run() {
            while(!isCancelled.get()) {
                Long delayUntilNextTask;
                ScheduledTask nextTask = scheduledTaskPriorityQueue.peek();
                delayUntilNextTask = computeDelayUntilNextTask(nextTask);
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
                if (executeNextTask) {
                    // We must execute a task right now. Either because at the beginning of the
                    // loop it was time to execute, or because we waited enough time in the
                    // semaphore.
                    runNextTask(nextTask);
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

        private static void runNextTask(ScheduledTask sc) {
            if (!sc.timerTask.isCancelled()) {
                sc.timerTask.setScheduledExecutionTime(sc.nextExecution);
                // The task is not recurrent. There's no future schedule of the task.
                if (sc.period == null) {
                    sc.timerTask.setScheduled(false);
                }
                sc.timerTask.run();
            }
        }

        private void updatePriorityQueue(ScheduledTask taskExecuted, long timeFinished) {
            synchronized(scheduledTaskPriorityQueue) {
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
