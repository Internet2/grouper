package edu.internet2.middleware.grouper.pspng;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;

/**
 * This classs helps report how much of a long task has been completed
 * and how long the rest is expected to take
 */
public class ProgressMonitor {
  final Logger LOG;
  final boolean minimalLogging;
  final String taskLabel;
  final int totalAmountOfWorkExpected;
  int amountOfWorkCompleted;
  final Instant timeStarted = Instant.now();
  Instant timeFinished;
  Instant lastTimeUpdatePrinted;
  final Duration logUpdateInterval;

  int lastLog_amountOfWorkCompleted=0;

  // Work that has been started but not completed
  int amountOfWorkBeingWorkedOn=0;

  public ProgressMonitor(int totalAmountOfWorkExpected, Logger LOG, boolean minimalLogging, int loggingInterval_secs, String taskLabelFormat, Object... taskLabelArgs) {
    this.LOG = LOG;
    this.minimalLogging=minimalLogging;
    this.logUpdateInterval = Duration.standardSeconds(loggingInterval_secs);
    this.taskLabel = String.format(taskLabelFormat, taskLabelArgs);
    this.totalAmountOfWorkExpected = totalAmountOfWorkExpected;

    if ( !minimalLogging ) {
      if (totalAmountOfWorkExpected >= 0) {
        LOG.info("{} Started: TotalWorkExpected={}", taskLabel, totalAmountOfWorkExpected);
      } else {
        LOG.info("{} Started", taskLabel);
      }
    }

    lastTimeUpdatePrinted = Instant.now();
  }

  public ProgressMonitor(Logger LOG, boolean minimalLogging, int loggingInterval_secs, String taskLabelFormat, Object... taskLabelArgs) {
    this(0, LOG, minimalLogging, loggingInterval_secs, taskLabelFormat, taskLabelArgs);
  }


  public void startWork(int amountOfWork) {
    // Have we been told about other work that had started
    if ( amountOfWorkBeingWorkedOn > 0 ) {
      // workCompleted() will also mark amountOfWorkBeingWordOn as complete,
      // so we have to zero it before calling it
      int temp = amountOfWorkBeingWorkedOn;
      amountOfWorkBeingWorkedOn=0;
      workCompleted(temp);
    }

    amountOfWorkBeingWorkedOn = amountOfWork;
  }

  public void workCompleted(int amountOfWork) {
    amountOfWorkCompleted += amountOfWorkBeingWorkedOn;
    amountOfWorkBeingWorkedOn=0;
    amountOfWorkCompleted += amountOfWork;

    logProgressIfNecessary();
  }

  public void completelyDone(String completionLabel) {
    // Only handle completion once
    if ( timeFinished!=null ) {
      return;
    }
    timeFinished = Instant.now();
    Duration overallTaskDuration = new Duration(timeStarted, timeFinished);

    // Log when either not logging minimally or when we've run long enough
    if ( !minimalLogging || overallTaskDuration.isLongerThan(logUpdateInterval) ) {
      LOG.info(String.format("%s Completed (%s): %d items in %s (%.1f items/minute)",
              taskLabel, completionLabel, amountOfWorkCompleted,
              PspUtils.formatElapsedTime(overallTaskDuration),
              1.0 * amountOfWorkCompleted / overallTaskDuration.getMillis() * 1000 * 60));
    }
  }


  protected void logProgressIfNecessary() {
    Duration howLongSinceProgressWasPrinted = new Duration(lastTimeUpdatePrinted, Instant.now());
    if ( howLongSinceProgressWasPrinted.isLongerThan(logUpdateInterval) ) {
      Duration howLongSinceStarted = new Duration(timeStarted, Instant.now());

      long workDone_sinceLastLog = amountOfWorkCompleted - lastLog_amountOfWorkCompleted;
      lastLog_amountOfWorkCompleted=amountOfWorkCompleted;

      double workDonePerMinute_sinceLastLog = 60.0*1000.0*workDone_sinceLastLog/howLongSinceProgressWasPrinted.getMillis();
      double workDonePerMinute_overall = 60.0*1000.0*amountOfWorkCompleted/howLongSinceStarted.getMillis();

      if ( totalAmountOfWorkExpected>=0 ) {
        long workLeftToDo = totalAmountOfWorkExpected - amountOfWorkCompleted;
        long estimatedTimeLeft_seconds = (long) (60.0*workLeftToDo/workDonePerMinute_sinceLastLog);

        Duration estimatedTimeLeft = Duration.standardSeconds(estimatedTimeLeft_seconds);

        LOG.info(String.format("%s Progress: %d of %d (%s%%) in %s (%s items/min overall, %s items/min recently). %d work to go, ETA: %s",
                taskLabel, amountOfWorkCompleted, totalAmountOfWorkExpected,
                PspUtils.formatWithSignificantDigits(100.0 * amountOfWorkCompleted / totalAmountOfWorkExpected, 2),
                PspUtils.formatElapsedTime(howLongSinceStarted),
                PspUtils.formatWithSignificantDigits(workDonePerMinute_overall, 2),
                PspUtils.formatWithSignificantDigits(workDonePerMinute_sinceLastLog, 2),
                workLeftToDo,
                PspUtils.formatElapsedTime(estimatedTimeLeft)));
      }
      else {
        // Log what we know when we don't know how much work to expect

        LOG.info(String.format("%s Progress: %d in %s (%s items/min overall, %s items/min recently).",
                taskLabel, amountOfWorkCompleted,
                PspUtils.formatElapsedTime(howLongSinceStarted),
                PspUtils.formatWithSignificantDigits(workDonePerMinute_overall, 2),
                PspUtils.formatWithSignificantDigits(workDonePerMinute_sinceLastLog, 2)));
      }

      lastTimeUpdatePrinted = Instant.now();
    }
  }
}
