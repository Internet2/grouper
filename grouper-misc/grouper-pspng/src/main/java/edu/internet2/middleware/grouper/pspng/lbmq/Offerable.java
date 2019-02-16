package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

/**
 * This trait captures the "tail side" of the {@link BlockingQueue} interface.
 */
public interface Offerable<E> {

    /**
     * Inserts the specified element into this queue if it is possible to do so immediately without violating capacity
     * restrictions, returning {@code true} upon success and throwing an {@link IllegalStateException} if no space is
     * currently available.
     *
     * @param e
     *            the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException
     *             if the element cannot be added at this time due to capacity restrictions
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified element is null and this queue does not permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added to this queue
     */
    boolean add(E e);

    /**
     * Inserts the specified element into this queue if it is possible to do so immediately without violating capacity
     * restrictions. When using a capacity-restricted queue, this method is generally preferable to {@link #add}, which
     * can fail to insert an element only by throwing an exception.
     *
     * @param e
     *            the element to add
     * @return {@code true} if the element was added to this queue, else {@code false}
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified element is null and this queue does not permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added to this queue
     */
    boolean offer(E e);

    /**
     * Inserts the specified element into this queue, waiting if necessary for space to become available.
     *
     * @param e
     *            the element to add
     * @throws InterruptedException
     *             if interrupted while waiting
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified element is null
     * @throws IllegalArgumentException
     *             if some property of the specified element prevents it from being added to this queue
     */
    void put(E e) throws InterruptedException;

    /**
     * Inserts the specified element into this queue, waiting up to the specified wait time if necessary for space to
     * become available.
     *
     * @param e
     *            the element to add
     * @param timeout
     *            how long to wait before giving up, in units of {@code unit}
     * @param unit
     *            a {@link TimeUnit} determining how to interpret the {@code timeout} parameter
     * @return {@code true} if successful, or {@code false} if the specified waiting time elapses before space is
     *         available
     * @throws InterruptedException
     *             if interrupted while waiting
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being added to this queue
     * @throws NullPointerException
     *             if the specified element is null
     * @throws IllegalArgumentException
     *             if some property of the specified element prevents it from being added to this queue
     */
    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Returns the number of additional elements that this queue can ideally (in the absence of memory or resource
     * constraints) accept without blocking, or {@code Integer.MAX_VALUE} if there is no intrinsic limit.
     *
     * <p>
     * Note that you <em>cannot</em> always tell if an attempt to insert an element will succeed by inspecting
     * {@code remainingCapacity} because it may be the case that another thread is about to insert or remove an element.
     *
     * @return the remaining capacity
     */
    int remainingCapacity();

}
