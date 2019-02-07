/*
 * $Id$
 *
 * Copyright (c) 2018 AspectWorks, spol. s r.o.
 */
package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.ArrayList;

/**
 * Chooses the next queue to be used from the highest priority priority group.
 * If no queue is found it searches the lower priority groups and so on until
 * it finds a queue.
 */
public class DefaultSubQueueSelection<K, E> implements LinkedBlockingMultiQueue.SubQueueSelection<K, E> {

    private ArrayList<LinkedBlockingMultiQueue<K, E>.PriorityGroup> priorityGroups;

    @Override
    public LinkedBlockingMultiQueue.SubQueue getNext() {
        for (int i = 0; i < priorityGroups.size(); i++) {
            LinkedBlockingMultiQueue.SubQueue subQueue = priorityGroups.get(i).getNextSubQueue();
            if (subQueue != null) {
                return subQueue;
            }
        }
        return null;
    }

    @Override
    public E peek() {
        // assert takeLock.isHeldByCurrentThread();
        for (int i = 0; i < priorityGroups.size(); i++) {
            E dequed = priorityGroups.get(i).peek();
            if (dequed != null) {
                return dequed;
            }
        }
        return null;
    }

    @Override
    public void setPriorityGroups(ArrayList<LinkedBlockingMultiQueue<K, E>.PriorityGroup> priorityGroups) {
        this.priorityGroups = priorityGroups;
    }

}
