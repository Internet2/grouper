package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

/**
 * This interface captures the "head side" of the {@link BlockingQueue} interface
 */
public interface Pollable<E> {

    /**
     * Retrieves and removes the head of this queue. This method differs from {@link #poll poll} only in that it throws
     * an exception if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException
     *             if this queue is empty
     */
    E remove();

    /**
     * Retrieves and removes the head of this queue, or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    E poll();

    /**
     * Retrieves, but does not remove, the head of this queue. This method differs from {@link #peek peek} only in that
     * it throws an exception if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException
     *             if this queue is empty
     */
    E element();

    /**
     * Retrieves, but does not remove, the head of this queue, or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    E peek();

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     *
     * @return the head of this queue
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    E take() throws InterruptedException;

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an element
     * to become available.
     *
     * @param timeout
     *            how long to wait before giving up, in units of {@code unit}
     * @param unit
     *            a {@code TimeUnit} determining how to interpret the {@code timeout} parameter
     * @return the head of this queue, or {@code null} if the specified waiting time elapses before an element is
     *         available
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Removes all available elements from this queue and adds them to the given collection. This operation may be more
     * efficient than repeatedly polling this queue. A failure encountered while attempting to add elements to
     * collection {@code c} may result in elements being in neither, either or both collections when the associated
     * exception is thrown. Attempts to drain a queue to itself result in {@link IllegalArgumentException}. Further, the
     * behavior of this operation is undefined if the specified collection is modified while the operation is in
     * progress.
     *
     * @param c
     *            the collection to transfer elements into
     * @return the number of elements transferred
     * @throws UnsupportedOperationException
     *             if addition of elements is not supported by the specified collection
     * @throws ClassCastException
     *             if the class of an element of this queue prevents it from being added to the specified collection
     * @throws NullPointerException
     *             if the specified collection is null
     * @throws IllegalArgumentException
     *             if the specified collection is this queue, or some property of an element of this queue prevents it
     *             from being added to the specified collection
     */
    int drainTo(Collection<? super E> c);

    /**
     * Removes at most the given number of available elements from this queue and adds them to the given collection. A
     * failure encountered while attempting to add elements to collection {@code c} may result in elements being in
     * neither, either or both collections when the associated exception is thrown. Attempts to drain a queue to itself
     * result in {@link IllegalArgumentException}. Further, the behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c
     *            the collection to transfer elements into
     * @param maxElements
     *            the maximum number of elements to transfer
     * @return the number of elements transferred
     * @throws UnsupportedOperationException
     *             if addition of elements is not supported by the specified collection
     * @throws ClassCastException
     *             if the class of an element of this queue prevents it from being added to the specified collection
     * @throws NullPointerException
     *             if the specified collection is null
     * @throws IllegalArgumentException
     *             if the specified collection is this queue, or some property of an element of this queue prevents it
     *             from being added to the specified collection
     */
    int drainTo(Collection<? super E> c, int maxElements);

}