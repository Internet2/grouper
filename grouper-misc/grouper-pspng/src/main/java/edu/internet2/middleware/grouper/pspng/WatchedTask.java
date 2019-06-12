package edu.internet2.middleware.grouper.pspng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * This class can be used to track activities that are underway, and report
 * when they are "tardy" according to expectations. You can think of this as
 * an alternative to jstack that automatically logs what the system is doing
 * when things are taking longer than expected.
 *
 * Without an AOP library, the easiest way to use this class is to create
 * try-resource blocks around different steps of a process:
 * try (WatchedTask t=new WatchedTask("FullSync", "FullSync of group %s", groupName) {
 *   ... code ...
 * }
 *
 * If "...code..." takes too long, then information about the WatchedTask and
 * its children will be logged.
 *
 * Status: Early development, needs the ability to configure timing expectations.
 *
 * This is abstract for now to prevent its use. In the end, it will not be abstract.
 */
abstract class WatchedTask implements AutoCloseable{
  private static final Logger LOG = LoggerFactory.getLogger(WatchedTask.class);


  // Each thread keeps a sequence for its tasks
  private static ThreadLocal<AtomicInteger> taskSequences = new ThreadLocal<AtomicInteger>() {
    @Override
    protected AtomicInteger initialValue() {
      return new AtomicInteger();
    }
  };

  // A thread that wakes up (via Timers) and reports about tardy tasks
  private final static ScheduledExecutorService timerScheduler
          = Executors.newScheduledThreadPool(1);

  // Each thread keeps its call stack
  private static ThreadLocal<Stack<WatchedTask>> taskStacks = new ThreadLocal<Stack<WatchedTask>>() {
    @Override
    protected Stack<WatchedTask> initialValue() {
      Stack<WatchedTask> result = new Stack<>();

      return result;
    }
  };

  // What stack are is this stack within
  private final Stack<WatchedTask> ourStack = taskStacks.get();

  private final String taskId = String.format("%s-%s",
          PspUtils.getThreadId(), PspUtils.getIdString(taskSequences.get().incrementAndGet()) );

  // What Task started us
  private WatchedTask parentTask;

  // Statistics are accumulated by shortName
  // Warning Durations are looked up by shortName
  private String shortName;

  private String detailedName;

  private final Date startDate = new Date();
  private ScheduledFuture timer;

  private double warningDuration_secs;
  private AtomicReference<Date> finishTime = new AtomicReference<>();


  // Keep track of the number of times each subtask has been started,
  // indexed by shortName
  private Map<String, AtomicInteger> subTaskCounts = new HashMap<String, AtomicInteger>() {
    @Override
    public AtomicInteger get(Object key) {
      // Default value for missing key is AtomicInteger(0)
      if ( !containsKey(key) ) {
        put((String)key, new AtomicInteger(0));
      }
      return super.get(key);
    }
  };


  // Used to create a root watchdog
  private WatchedTask() {
    shortName = detailedName = null;
    parentTask = null;
    warningDuration_secs = -1;
  }


  public WatchedTask(String shortName, String detailedNameFormat, Object... detailedNameArgs) {
    if ( taskStacks.get().isEmpty() ) {
      taskStacks.get().push(new WatchedTask() {});
    }

    parentTask = taskStacks.get().peek();
    this.shortName = shortName;
    if ( detailedNameFormat == null ) {
      this.detailedName = shortName;
    } else {
      this.detailedName = String.format(detailedNameFormat, detailedNameArgs);
    }

    parentTask.subTaskCounts.get(shortName).incrementAndGet();

    this.warningDuration_secs = 0.25;

    if ( warningDuration_secs > 0 ) {
      timer = scheduleFutureWarning(warningDuration_secs);
    }
  }


  public WatchedTask(String shortName) {
    this(shortName, null);
  }


  private ScheduledFuture scheduleFutureWarning(double timeUntilWarning_secs) {
    ScheduledFuture result = timerScheduler.schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                          synchronized (ourStack) {
                                            // Have we finished in the interval between being run
                                            // by scheduler and synchronizing?
                                            if ( finishTime.get() != null ) {
                                              warningTimerExpired();
                                              timer = scheduleFutureWarning(60);
                                            }
                                          }
                                        }
                                      },
            (int)(1000*warningDuration_secs), TimeUnit.MILLISECONDS);
    return result;
  }

  private void warningTimerExpired() {
    LOG.warn("Task is taking longer than expected: {} has taken {} vs expected {} secs",
            detailedName, PspUtils.formatElapsedTime(startDate, null), warningDuration_secs);
    LinkedList<WatchedTask> ourChildrenTasks = getChildrenTasks();

    ourChildrenTasks.add(0, this);
  }

  private LinkedList<WatchedTask> getChildrenTasks() {
    // Find the stack of tasks from us to our children
    LinkedList<WatchedTask> ourChildrenTasks = new LinkedList<>();

    synchronized (ourStack) {
      WatchedTask task = ourStack.peek();

      while ( task != this && task != null ) {
        ourChildrenTasks.add(0, task);
      }
    }

    // We should either have no children or our first child should have us as a parent
    if ( ourChildrenTasks.size()==0 || ourChildrenTasks.get(0).parentTask!=this ) {
      LOG.error("Task Watchdog: Invalid task chain... active tasks did not lead to us");
    }
    return ourChildrenTasks;
  }


  @Override
  public void close() {
    finish();
  }

  public void finish() {
    // We synchronize to make sure scheduled task isn't running, otherwise
    // the scheduled task might reschedule a warning timer after we cancel it
    synchronized (ourStack) {
      if (finishTime.get() != null) {
        return;
      }

      // Finish our children if they haven't finished themselves
      LinkedList<WatchedTask> ourChildren = getChildrenTasks();
      Iterator<WatchedTask> childIterator = ourChildren.descendingIterator();
      while (childIterator.hasNext()) {
        WatchedTask child = childIterator.next();

        LOG.warn("Task Watchdog: Task not finished properly: {}", child);
        child.finish();
      }

      finishTime.set(new Date());
      if (timer != null) {
        timer.cancel(false);
        timer = null;
      }

      if ( ourStack.peek() == this ) {
        ourStack.pop();
      } else {
        LOG.warn("Task Watchdog: Expected {} to be the top task, but {} was instead",
                this, ourStack.peek());
      }
    }
  }
}
