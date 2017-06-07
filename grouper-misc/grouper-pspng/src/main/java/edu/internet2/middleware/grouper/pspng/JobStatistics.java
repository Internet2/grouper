package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bert on 5/17/17.
 */
public class JobStatistics {
    Date processingStartTime = new Date();
    Date processingCompletedTime = null;

    AtomicInteger insertCount = new AtomicInteger(0),
            deleteCount = new AtomicInteger(0),
            updateCount = new AtomicInteger(0),
            totalCount = new AtomicInteger(0);

    public void add(JobStatistics stats) {
        this.insertCount.addAndGet(stats.insertCount.get());
        this.deleteCount.addAndGet(stats.deleteCount.get());
        this.updateCount.addAndGet(stats.updateCount.get());
        this.totalCount.addAndGet(stats.totalCount.get());

        // Our processing-completed time is the later of our time and the time 'stats' was completed
        if ( processingCompletedTime == null || processingCompletedTime.before(stats.processingCompletedTime)) {
            processingCompletedTime = stats.processingCompletedTime;
        }
    }

    public void done() {
        // If we don't have a completed time yet, set it now
        if ( this.processingCompletedTime == null ) {
            processingCompletedTime = new Date();
        }
    }

    public void updateLoaderLog(Hib3GrouperLoaderLog hib3GrouploaderLog) {
        hib3GrouploaderLog.setInsertCount(insertCount.get());
        hib3GrouploaderLog.setDeleteCount(deleteCount.get());
        hib3GrouploaderLog.setUpdateCount(updateCount.get());
        hib3GrouploaderLog.setTotalCount(totalCount.get());
    }

    @Override
    public String toString() {
        long secs;
        if ( processingCompletedTime == null ) {
            secs = -1;
        }
        else {
            secs = (processingCompletedTime.getTime() - processingStartTime.getTime())/1000;
        }
        return String.format("ins=%d|del=%d|upd=%d|tot=%d|t=%d secs",
                insertCount.get(), deleteCount.get(), updateCount.get(), totalCount.get(), secs);
    }
}
