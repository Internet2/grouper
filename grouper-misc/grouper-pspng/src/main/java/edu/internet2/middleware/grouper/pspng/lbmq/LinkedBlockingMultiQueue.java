/*
 * Derived from work made by Doug Lea with assistance from members of JCP JSR-166 Expert Group
 * (https://jcp.org/en/jsr/detail?id=166). The original work is in the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package edu.internet2.middleware.grouper.pspng.lbmq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An optionally-bounded blocking "multi-queue" based on linked nodes. A multi-queue is actually a set of queues that
 * are connected at the heads and have independent tails (the head of the queue is that element that has been on the
 * queue the longest time. The tail of the queue is that element that has been on the queue the shortest time). New
 * elements are added at the tail of one of the queues, and the queue retrieval operations obtain elements from the head
 * of some of the queues, according to a policy that is described below.
 * <p>
 * This class essentially allows a consumer to efficiently block a single thread on a set of queues, until one becomes
 * available. The special feature is that individual queues can be enabled or disabled. A disabled queue is not
 * considered for polling (in the event that all the queue are disabled, any blocking operation would do so trying to
 * read, as if all the queues were empty). Elements are taken from the set of enabled queues, obeying the established
 * priority (queues with the same priority are served round robin).
 * <p>
 * A disabled queue accepts new elements normally until it reaches the maximum capacity (if any).
 * <p>
 * Individual queues can be added, removed, enabled or disabled at any time.
 * <p>
 * The optional capacity bound constructor argument serves as a way to prevent excessive queue expansion. The capacity,
 * if unspecified, is equal to Int.MaxVaue. Linked nodes are dynamically created upon each insertion unless this would
 * bring the queue above capacity.
 * <p>
 * Not being actually a linear queue, this class does not implement the {@code Collection} or {@code Queue} interfaces.
 * The traditional queue interface is split in the traits: {@code Offerable} and {@code Pollable}. Sub-queues do however
 * implement Collection.
 * 
 * @see java.util.concurrent.LinkedBlockingQueue
 */
public class LinkedBlockingMultiQueue<K, E> extends AbstractPollable<E> {

    /*
     * This implementation is inspired by the LinkedBlockingQueue, made by Doug Lea with assistance from members of JCP
     * JSR-166 Expert Group (https://jcp.org/en/jsr/detail?id=166).
     * 
     * Each sub-queue uses, as does the LinkedBlockingQueue, a variant of the "two lock queue" algorithm. The putLock
     * gates entry to put (and offer), and has an associated condition for waiting puts. The takeLock, on the other
     * hand, is unique and shared among all the sub-queues.
     * 
     * Each subqueue has a "count" field, that is maintained as an atomic to avoid needing to get both locks in most
     * cases. Also, to minimize need for puts to get takeLock and vice-versa, cascading notifies are used. When a put
     * notices that it has enabled at least one take, it signals taker. That taker in turn signals others if more items
     * have been entered since the signal. And symmetrically for takes signaling puts.
     * 
     * The possibility of disabling sub-queues introduces the necessity of an additional centralized atomic count field,
     * which is also updated in every operation and represents, at any time, how many elements can be taken before
     * exhausting the queue.
     * 
     * Operations such as remove(Object) and iterators acquire both the corresponding putLock and the takeLock.
     * 
     * Visibility between writers and readers is provided as follows:
     * 
     * Whenever an element is enqueued, the putLock is acquired and count updated. A subsequent reader guarantees
     * visibility to the enqueued Node by either acquiring the putLock (via fullyLock) or by acquiring the takeLock, and
     * then reading n = count.get(); this gives visibility to the first n items.
     * 
     * To implement weakly consistent iterators, it appears we need to keep all Nodes GC-reachable from a predecessor
     * dequeued Node. That would cause two problems:
     * 
     * - allow a rogue Iterator to cause unbounded memory retention
     * 
     * - cause cross-generational linking of old Nodes to new Nodes if a Node was tenured while live, which generational
     * GCs have a hard time dealing with, causing repeated major collections. However, only non-deleted Nodes need to be
     * reachable from dequeued Nodes, and reachability does not necessarily have to be of the kind understood by the GC.
     * We use the trick of linking a Node that has just been dequeued to itself. Such a self-link implicitly means to
     * advance to head.next.
     */

    private final ConcurrentHashMap<K, SubQueue> subQueues = new ConcurrentHashMap<>();

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Current number of elements in enabled sub-queues */
    private final AtomicInteger totalCount = new AtomicInteger();

    /**
     * A list of priority groups. Group consists of multiple queues.
     */
    private final ArrayList<PriorityGroup> priorityGroups = new ArrayList<>();

    /**
     * Allows to choose the next subQueue to be used.
     */
    private final SubQueueSelection<K, E> subQueueSelection;

    /**
     * Constructor. The default {@link DefaultSubQueueSelection} will be used.
     */
    public LinkedBlockingMultiQueue() {
        this(new DefaultSubQueueSelection<K,E>());
    }

    /**
     * Constructor.
     *
     * @param subQueueSelection an implementation of {@link SubQueueSelection}
     */
    public LinkedBlockingMultiQueue(SubQueueSelection<K, E> subQueueSelection) {
        this.subQueueSelection = subQueueSelection;
        this.subQueueSelection.setPriorityGroups(this.priorityGroups);
    }

    /**
     * Set of sub-queues with the same priority
     */
    class PriorityGroup {

        final int priority;
        final ArrayList<SubQueue> queues = new ArrayList<>(0);

        PriorityGroup(int priority) {
            this.priority = priority;
        }

        int nextIdx = 0;

        void addQueue(SubQueue subQueue) {
            queues.add(subQueue);
            subQueue.priorityGroup = this;
        }

        void removeQueue(SubQueue removed) {
            Iterator<SubQueue> it = queues.iterator();
            while (it.hasNext()) {
                SubQueue subQueue = it.next();
                if (subQueue.key == removed.key) {
                    removed.putLock.lock();
                    try {
                        it.remove();
                        if (nextIdx == queues.size())
                            nextIdx = 0;
                        if (subQueue.enabled)
                            totalCount.getAndAdd(-removed.size());
                        return;
                    } finally {
                        removed.putLock.unlock();
                    }
                }
            }
        }

        SubQueue getNextSubQueue() {
            // assert takeLock.isHeldByCurrentThread();
            int startIdx = nextIdx;
            ArrayList<SubQueue> queues = this.queues;
            do {
                SubQueue child = queues.get(nextIdx);
                nextIdx += 1;
                if (nextIdx == queues.size())
                    nextIdx = 0;
                if (child.enabled && child.size() > 0)
                    return child;
            } while (nextIdx != startIdx);
            return null;
        }

        int drainTo(Collection<? super E> c, int maxElements) {
            // assert takeLock.isHeldByCurrentThread();
            int drained = 0;
            int emptyQueues = 0;
            do {
                SubQueue child = queues.get(nextIdx);
                nextIdx += 1;
                if (nextIdx == queues.size())
                    nextIdx = 0;
                if (child.enabled && child.size() > 0) {
                    emptyQueues = 0;
                    c.add(child.dequeue());
                    drained += 1;
                    int oldSize = child.count.getAndDecrement();
                    if (oldSize == child.capacity)
                        child.signalNotFull();
                } else {
                    emptyQueues += 1;
                }
            } while (drained < maxElements && emptyQueues < queues.size());
            return drained;
        }

        E peek() {
            // assert takeLock.isHeldByCurrentThread();
            int startIdx = nextIdx;
            do {
                SubQueue child = queues.get(nextIdx);
                if (child.enabled && child.size() > 0) {
                    return child.head.next.item;
                } else {
                    nextIdx += 1;
                    if (nextIdx == queues.size())
                        nextIdx = 0;
                }
            } while (nextIdx != startIdx);
            return null;
        }
    }

    /**
     * Add a sub queue if absent
     *
     * @param key
     *            the key used to identify the queue
     * @param priority
     *            the queue priority, a lower number means higher priority
     * @return the previous queue associated with the specified key, or {@code null} if there was no queue for the key
     */
    public SubQueue addSubQueue(K key, int priority) {
        return addSubQueue(key, priority, Integer.MAX_VALUE);
    }

    /**
     * Add a sub-queue if absent
     *
     * @param key
     *            the key used to identify the queue
     * @param priority
     *            the queue priority, a lower number means higher priority
     * @param capacity
     *            the capacity of the new sub-queue
     * @return the previous queue associated with the specified key, or {@code null} if there was no queue for the key
     */
    public SubQueue addSubQueue(K key, int priority, int capacity) {
        SubQueue subQueue = new SubQueue(key, capacity);
        takeLock.lock();
        try {
            SubQueue old = subQueues.putIfAbsent(key, subQueue);
            if (old == null) {
                int i = 0;
                boolean added = false;
                for (PriorityGroup pg : priorityGroups) {
                    if (pg.priority == priority) {
                        pg.addQueue(subQueue);
                        added = true;
                        break;
                    } else if (pg.priority > priority) {
                        PriorityGroup newPg = new PriorityGroup(priority);
                        priorityGroups.add(i, newPg);
                        newPg.addQueue(subQueue);
                        added = true;
                        break;
                    }
                    i += 1;
                }
                if (!added) {
                    PriorityGroup newPg = new PriorityGroup(priority);
                    priorityGroups.add(newPg);
                    newPg.addQueue(subQueue);
                }
            }
            return old;
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Remove a sub-queue
     * 
     * @param key
     *            the key f the sub-queue that should be removed
     * @return the removed SubQueue or null if the key was not in the map
     */
    public SubQueue removeSubQueue(K key) {
        takeLock.lock();
        try {
            SubQueue removed = subQueues.remove(key);
            if (removed != null) {
                removed.priorityGroup.removeQueue(removed);
                if (removed.priorityGroup.queues.size() == 0) {
                    this.priorityGroups.remove(removed.priorityGroup);
                }
            }
            return removed;
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Gets a sub-queue
     * 
     * @param key
     *            the key f the sub-queue that should be returned
     * @return the sub-queue with the corresponding key or null if it does not exist
     */
    public SubQueue getSubQueue(K key) {
        return subQueues.get(key);
    }

    /**
     * Signals a waiting take. Called only from put/offer (which do not otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long remaining = unit.toNanos(timeout);
        SubQueue subQueue;
        E element;
        int oldSize;
        takeLock.lockInterruptibly();
        try {
            while (totalCount.get() == 0) {
                if (remaining <= 0)
                    return null;
                remaining = notEmpty.awaitNanos(remaining);
            }
            // at this point we know there is an element
            subQueue = subQueueSelection.getNext();
            element = subQueue.dequeue();
            oldSize = subQueue.count.getAndDecrement();
            if (totalCount.getAndDecrement() > 1) {
                // sub-queue still has elements, notify next poller
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (oldSize == subQueue.capacity) {
            // we just took an element from a full queue, notify any blocked offers
            subQueue.signalNotFull();
        }
        return element;
    }

    public E take() throws InterruptedException {
        SubQueue subQueue;
        int oldSize;
        E element;
        takeLock.lockInterruptibly();
        try {
            while (totalCount.get() == 0) {
                notEmpty.await();
            }
            // at this point we know there is an element
            subQueue = subQueueSelection.getNext();
            element = subQueue.dequeue();
            oldSize = subQueue.count.getAndDecrement();
            if (totalCount.getAndDecrement() > 1) {
                // sub-queue still has elements, notify next poller
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (oldSize == subQueue.capacity) {
            // we just took an element from a full queue, notify any blocked offers
            subQueue.signalNotFull();
        }
        return element;
    }

    public E poll() {
        SubQueue subQueue;
        E element;
        int oldSize;
        takeLock.lock();
        try {
            if (totalCount.get() == 0)
                return null;
            // at this point we know there is an element
            subQueue = subQueueSelection.getNext();
            element = subQueue.dequeue();
            oldSize = subQueue.count.getAndDecrement();
            if (totalCount.getAndDecrement() > 1) {
                // sub-queue still has elements, notify next poller
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (oldSize == subQueue.capacity) {
            // we just took an element from a full queue, notify any blocked offers
            subQueue.signalNotFull();
        }
        return element;
    }

    public E peek() {
        takeLock.lock();
        try {
            if (totalCount.get() == 0)
                return null;
            else
                return subQueueSelection.peek();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Returns the total size of this multi-queue, that is, the sum of the sizes of all the enabled sub-queues.
     * 
     * @return the total size of this multi-queue
     */
    public int totalSize() {
        return totalCount.get();
    }

    /**
     * Returns whether this multi-queue is empty, that is, whether there is any element ready to be taken from the head.
     * 
     * @return whether this multi-queue is empty.
     */
    public boolean isEmpty() {
        return totalSize() == 0;
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        takeLock.lock();
        try {
            int n = Math.min(maxElements, totalCount.get());
            // ordered iteration, begin with lower index (highest priority)
            int drained = 0;
            for (int i = 0; i < priorityGroups.size() && drained < n; i++) {
                drained += priorityGroups.get(i).drainTo(c, n - drained);
            }
            // assert drained == n;
            totalCount.getAndAdd(-drained);
            return drained;
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Counts the priority groups currently registered in {@link LinkedBlockingMultiQueue}. Suitable
     * for debugging and testing.
     *
     * @return the number of priority groups currently registered
     */
    public int getPriorityGroupsCount() {
        return priorityGroups.size();
    }

    /**
     * Represent a sub-queue inside a multi-queue. Instances of this class are just like any blocking queue except that
     * elements cannot be taken from their heads.
     */
    public class SubQueue extends AbstractOfferable<E> {

        private final K key;
        private final int capacity;
        private PriorityGroup priorityGroup;

        SubQueue(K key, int capacity) {
            if (capacity <= 0)
                throw new IllegalArgumentException();
            this.key = key;
            this.capacity = capacity;
        }

        private final ReentrantLock putLock = new ReentrantLock();
        private final Condition notFull = putLock.newCondition();

        private final AtomicInteger count = new AtomicInteger();
        private boolean enabled = true;

        public int remainingCapacity() {
            return capacity - count.get();
        }

        /**
         * Head of linked list. Invariant: head.item == null
         */
        private Node<E> head = new Node<>(null);

        /**
         * Tail of linked list. Invariant: last.next == null
         */
        private Node<E> last = head;

        /**
         * Atomically removes all of the elements from this queue. The queue will be empty after this call returns.
         */
        public void clear() {
            fullyLock();
            try {
                Node<E> h = head;
                Node<E> p = h.next;
                while (p != null) {
                    h.next = h;
                    p.item = null; // help GC
                    h = p;
                    p = h.next;
                }
                head = last;
                int oldCapacity = count.getAndSet(0);
                if (oldCapacity == capacity)
                    notFull.signal();
                if (enabled)
                    totalCount.getAndAdd(-oldCapacity);
            } finally {
                fullyUnlock();
            }
        }

        /**
         * Enable or disable this sub-queue. Enabled queues's elements are taken from the common head of the
         * multi-queue. Elements from disabled queues are never taken. Elements can be added to a queue regardless of
         * this status (if there is enough remaining capacity).
         * 
         * @param status
         *            true to enable, false to disable
         */
        public void enable(boolean status) {
            fullyLock();
            try {
                enabled = status;
                if (status) {
                    // potentially unblock waiting polls
                    int c = count.get();
                    if (c > 0) {
                        totalCount.getAndAdd(c);
                        notEmpty.signal();
                    }
                } else {
                    totalCount.getAndAdd(-count.get());
                }
            } finally {
                fullyUnlock();
            }
        }

        /**
         * Returns whether this sub-queue is enabled
         * 
         * @return true is this sub-queue is enabled, false if is disabled.
         */
        public boolean isEnabled() {
            takeLock.lock();
            try {
                return enabled;
            } finally {
                takeLock.unlock();
            }
        }

        private void signalNotFull() {
            putLock.lock();
            try {
                notFull.signal();
            } finally {
                putLock.unlock();
            }
        }

        private void enqueue(Node<E> node) {
            last.next = node;
            last = node;
        }

        /**
         * Return the number of elements in this sub queue. This method returns the actual number of elements,
         * regardless of whether the queue is enabled or not.
         */
        public int size() {
            return count.get();
        }

        /**
         * Return whether the queue is empty. This method bases its return value in the actual number of elements,
         * regardless of whether the queue is enabled or not.
         */
        public boolean isEmpty() {
            return size() == 0;
        }

        public void put(E e) throws InterruptedException {
            if (e == null)
                throw new NullPointerException();
            long oldSize = -1;
            /*
             * As this method never fails to insert, it is more efficient to pre-create the node outside the lock, to 
             * reduce contention
             */
            Node<E> node = new Node<>(e);
            putLock.lockInterruptibly();
            try {
                /*
                 * Note that count is used in wait guard even though it is not protected by lock. This works because
                 * count can only decrease at this point (all other puts are shut out by lock), and we (or some other
                 * waiting put) are signaled if it ever changes from capacity. Similarly for all other uses of count in
                 * other wait guards.
                 */
                while (count.get() == capacity) {
                    notFull.await();
                }
                enqueue(node);
                if (count.getAndIncrement() + 1 < capacity) {
                    // queue not full after adding, notify next offerer
                    notFull.signal();
                }
                if (enabled)
                    oldSize = totalCount.getAndIncrement();
            } finally {
                putLock.unlock();
            }
            if (oldSize == 0) {
                // just added an element to an empty queue, notify pollers
                signalNotEmpty();
            }
        }

        public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
            if (e == null)
                throw new NullPointerException();
            long nanos = unit.toNanos(timeout);
            long oldSize = -1;
            putLock.lockInterruptibly();
            try {
                while (count.get() == capacity) {
                    if (nanos <= 0)
                        return false;
                    nanos = notFull.awaitNanos(nanos);
                }
                enqueue(new Node<>(e));
                if (count.getAndIncrement() + 1 < capacity) {
                    // queue not full after adding, notify next offerer
                    notFull.signal();
                }
                if (enabled)
                    oldSize = totalCount.getAndIncrement();
            } finally {
                putLock.unlock();
            }
            if (oldSize == 0) {
                // just added an element to an empty queue, notify pollers
                signalNotEmpty();
            }
            return true;
        }

        public boolean offer(E e) {
            if (e == null)
                throw new NullPointerException();
            long oldSize = -1;
            if (count.get() == capacity)
                return false;
            putLock.lock();
            try {
                if (count.get() == capacity)
                    return false;
                enqueue(new Node<>(e));
                if (count.getAndIncrement() + 1 < capacity) {
                    // queue not full after adding, notify next offerer
                    notFull.signal();
                }
                if (enabled)
                    oldSize = totalCount.getAndIncrement();
            } finally {
                putLock.unlock();
            }
            if (oldSize == 0) {
                // just added an element to an empty queue, notify pollers
                signalNotEmpty();
            }
            return true;
        }

        public boolean remove(Object o) {
            if (o == null)
                return false;
            fullyLock();
            try {
                for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
                    if (o.equals(p.item)) {
                        unlink(p, trail);
                        return true;
                    }
                }
                return false;
            } finally {
                fullyUnlock();
            }
        }

        public boolean contains(Object o) {
            if (o == null)
                return false;
            fullyLock();
            try {
                for (Node<E> p = head.next; p != null; p = p.next)
                    if (o.equals(p.item))
                        return true;
                return false;
            } finally {
                fullyUnlock();
            }
        }

        /**
         * Unlinks interior Node p with predecessor trail.
         */
        void unlink(Node<E> p, Node<E> trail) {
            // assert isFullyLocked();
            // p.next is not changed, to allow iterators that are traversing p to maintain their weak-consistency
            // guarantee.
            p.item = null;
            trail.next = p.next;
            if (last == p)
                last = trail;
            if (count.getAndDecrement() == capacity)
                notFull.signal();
            if (enabled)
                totalCount.getAndDecrement();
        }

        /**
         * Locks to prevent both puts and takes.
         */
        private void fullyLock() {
            takeLock.lock();
            putLock.lock();
        }

        /**
         * Unlocks to allow both puts and takes.
         */
        private void fullyUnlock() {
            putLock.unlock();
            takeLock.unlock();
        }

        /**
         * Tells whether both locks are held by current thread.
         */
        // private boolean isFullyLocked() {
        // return putLock.isHeldByCurrentThread() && takeLock.isHeldByCurrentThread();
        // }

        /**
         * Removes a node from head of queue.
         * 
         * @return the node
         */
        private E dequeue() {
            // assert takeLock.isHeldByCurrentThread();
            // assert size() > 0;
            Node<E> h = head;
            Node<E> first = h.next;
            h.next = h; // help GC
            head = first;
            E x = first.item;
            first.item = null;
            return x;
        }

        public String toString() {
            fullyLock();
            try {
                Node<E> p = head.next;
                if (p == null)
                    return "[]";

                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (;;) {
                    E e = p.item;
                    sb.append(e == this ? "(this Collection)" : e);
                    p = p.next;
                    if (p == null)
                        return sb.append(']').toString();
                    sb.append(',').append(' ');
                }
            } finally {
                fullyUnlock();
            }
        }

        public Object[] toArray() {
            fullyLock();
            try {
                int size = count.get();
                Object[] a = new Object[size];
                int k = 0;
                for (Node<E> p = head.next; p != null; p = p.next)
                    a[k++] = p.item;
                return a;
            } finally {
                fullyUnlock();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            fullyLock();
            try {
                int size = count.get();
                if (a.length < size)
                    a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
                int k = 0;
                for (Node<E> p = head.next; p != null; p = p.next)
                    a[k++] = (T) p.item;
                if (a.length > k)
                    a[k] = null;
                return a;
            } finally {
                fullyUnlock();
            }
        }

        /**
         * Returns an iterator over the elements in this queue in proper sequence. The elements will be returned in
         * order from first (head) to last (tail).
         *
         * <p>
         * The returned iterator is <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
         *
         * @return an iterator over the elements in this queue in proper sequence
         */
        public Iterator<E> iterator() {
            return new Itr();
        }

        private class Itr implements Iterator<E> {
            /**
             * Basic weakly-consistent iterator. At all times hold the next item to hand out so that if hasNext()
             * reports true, we will still have it to return even if lost race with a take, etc.
             */

            private Node<E> current;
            private Node<E> lastRet;
            private E currentElement;

            Itr() {
                fullyLock();
                try {
                    current = head.next;
                    if (current != null)
                        currentElement = current.item;
                } finally {
                    fullyUnlock();
                }
            }

            public boolean hasNext() {
                return current != null;
            }

            /**
             * Returns the next live successor of p, or null if no such.
             *
             * Unlike other traversal methods, iterators need to handle both: - dequeued nodes (p.next == p) - (possibly
             * multiple) interior removed nodes (p.item == null)
             */
            private Node<E> nextNode(Node<E> p) {
                for (;;) {
                    Node<E> s = p.next;
                    if (s == p)
                        return head.next;
                    if (s == null || s.item != null)
                        return s;
                    p = s;
                }
            }

            public E next() {
                fullyLock();
                try {
                    if (current == null)
                        throw new NoSuchElementException();
                    E x = currentElement;
                    lastRet = current;
                    current = nextNode(current);
                    currentElement = (current == null) ? null : current.item;
                    return x;
                } finally {
                    fullyUnlock();
                }
            }

            public void remove() {
                if (lastRet == null)
                    throw new IllegalStateException();
                fullyLock();
                try {
                    Node<E> node = lastRet;
                    lastRet = null;
                    for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
                        if (p == node) {
                            unlink(p, trail);
                            break;
                        }
                    }
                } finally {
                    fullyUnlock();
                }
            }
        }

    }

    private static class Node<E> {

        E item;

        /*
         * One of: 
         * - the real successor Node 
         * - this Node, meaning the successor is head.next 
         * - null, meaning there is no successor (this is the last node)
         */
        Node<E> next = null;

        Node(E item) {
            this.item = item;
        }

    }

    /**
     * Allows to choose the next subQueue.
     */
    public interface SubQueueSelection<K, E> {

        /**
         * Returns the next subQueue to be used.
         *
         * @return a subQueue
         */
        LinkedBlockingMultiQueue.SubQueue getNext();

        /**
         * Returns the next element from the queue but keeps it in the queue.
         *
         * @return the next element from the queue
         */
        E peek();

        /**
         * Sets priority groups.
         *
         * @param priorityGroups priority groups
         */
        void setPriorityGroups(ArrayList<LinkedBlockingMultiQueue<K, E>.PriorityGroup> priorityGroups);

    }

}
