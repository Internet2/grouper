# Linked Blocking Multi Queue

This was found in this (StackExchange(https://stackoverflow.com/a/30588574) Q&A and was imported from [linked-blocking-multi-queue@github](https://github.com/marianobarrios/linked-blocking-multi-queue).

_Linked Blocking Multi Queue_ is a concurrent collection that extends the existing [Java concurrent collection library](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html), offering an optionally-bounded blocking "multi-queue" based on linked nodes. That is, essentially, a data structure with several tails but one head, that allows a reader, crucially, to block on more than one queue.

[![Build Status](https://travis-ci.org/marianobarrios/linked-blocking-multi-queue.svg?branch=master)](https://travis-ci.org/marianobarrios/linked-blocking-multi-queue)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marianobarrios/linked-blocking-multi-queue/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marianobarrios/linked-blocking-multi-queue)
[![Scaladoc](http://javadoc-badge.appspot.com/com.github.marianobarrios/linked-blocking-multi-queue.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.github.marianobarrios/linked-blocking-multi-queue)

## Rationale

 A notorious limitation of Java blocking primitives is that a given thread can only block on one synchronizing object at a time. Blocking on several resources is a generally useful technique, already available in selectors (for channels) in Java. It is also common in other languages. This library offers a collection that can be used when a queue consumer (or consumers) needs to block on several queues.

Features:

- Priorities for different sub-queues
- Customizable priority evaluation (by default, fair (round-robin) selection of elements among same-priority sub-queues).
- Mid-flight addition and removal of sub-queues.
- Mid-flight change of sub-queue status (enabled/disabled).

## Use case

As mentioned, this class essentially allows a consumer to efficiently block a single thread on a set of queues, until one becomes available. 

Multiple queues (instead of just one collecting everything) are usually necessary when:

- Not all elements need the same capacity limit.
- Not all elements have the same priority.
- Among the same priority, round-robin (fair) consumption is desired (avoiding that prolific producers starve occasional ones).
- Some subset of enqueued elements may need to be discarded or suspended, while keeping the rest.

## Example

The multi-queue has a no-argument constructor. The class as two type arguments. The first one is the type of the queue key, that is, the type of the values used as keys to identify each sub-queue. The second is the element type. Sub-queues are created from it in a second step:

```java
LinkedBlockingMultiQueue<Int, String> q = new LinkedBlockingMultiQueue<>();
q.addSubQueue(1 /* key */, 10 /* priority */);
q.addSubQueue(2 /* key */, 10 /* priority */, 10000 /* capacity */);
LinkedBlockingMultiQueue<Int, String>.SubQueue sq1 = q.getSubQueue(1);
LinkedBlockingMultiQueue<Int, String>.SubQueue sq2 = q.getSubQueue(2);
```

Then it is possible to offer and poll:

```java
sq1.offer("x1");
q.poll(); // "x1"
sq2.offer("x2");
q.poll(); // "x2"
```

## Features

_Linked Blocking Multi Queue_ is an optionally-bounded blocking "multi-queue" based on linked nodes, defining multi-queue as a set of queues that are connected at the heads and have independent tails (the head of the queue is that element that has been on the queue the longest time, the tail of the queue is that element that has been on the queue the shortest time). New elements are added at the tail of one of the queues, and the queue retrieval operations obtain elements from the head of some of the queues, according to a policy that is described below.

The factory method for sub-queues has an optional capacity argument, as a way to prevent excessive queue expansion. The capacity, if unspecified, is equal to `Integer.MAX_VALUE`. Linked nodes are dynamically created upon each insertion unless this would bring the queue above capacity.

### Priorities

Sub-queues can have different priorities, meaning that elements from higher priority queues will be offered first to consumers. Inside the same priority queues are drained round-robin.

### Enabling, disabling, adding and removing queues

A special feature is that individual queues can be enabled or disabled. A disabled queue is not considered for polling (in the event that all the queue are disabled, any blocking operation would do so trying to read, as if all the queues were empty). Elements are taken from the set of enabled queues ()obeying the established priority).

A disabled queue accepts new elements normally until it reaches the maximum capacity (if any).

Individual queues can be enabled or disabled (and also added or removed) at any time.

## Compatibility

Not being actually a linear queue, this class does not implement the `Collection` or `Queue` interfaces. The traditional queue interface is split in the traits: `Offerable` and `Pollable`. Sub-queues do however implement Collection.

## Implementation notes

This implementation is inspired by the
[LinkedBlockingQueue](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/LinkedBlockingQueue.html), made by Doug Lea with assistance from members of [JCP JSR-166 Expert Group](https://jcp.org/en/jsr/detail?id=166).
 
Each sub-queue uses, as does the `LinkedBlockingQueue`, a variant of the "two lock queue" algorithm. The `putLock` gates entry to `put` (and `offer`), and has an associated condition for waiting puts. The `takeLock`, on the other hand, is unique and shared among all the sub-queues.

Each subqueue has a "count" field, that is maintained as an atomic to avoid needing to get both locks in most cases. Also, to minimize need for puts to get takeLock and vice-versa, cascading notifies are used. When a put notices that it has enabled at  least one take, it signals taker. That taker in turn signals others if more items have been entered since the signal. And symmetrically for takes signaling puts.

The possibility of disabling sub-queues introduces the necessity of an additional centralized atomic count field, which is also updated in every operation and represents, at any time, how many elements can be taken before exhausting the queue.
     
Operations such as `remove(Object)` and iterators acquire both the corresponding putLock and the takeLock.
     
Visibility between writers and readers is provided as follows:
 
Whenever an element is enqueued, the `putLock` is acquired and count updated. A subsequent reader guarantees visibility to the enqueued Node by either acquiring the `putLock` (via `fullyLock`) or by acquiring the `takeLock`, and then reading 
`n = count.get()`; this gives visibility to the first `n` items.
    
To implement weakly consistent iterators, it appears we need to keep all Nodes GC-reachable from a predecessor dequeued Node. That would cause two problems:

- Allow a rogue Iterator to cause unbounded memory retention
 
- Cause cross-generational linking of old Nodes to new Nodes if a Node was tenured while live, which generational garbage collectors have a hard time dealing with, causing repeated major collections. However, only non-deleted Nodes need to be reachable from dequeued Nodes, and reachability does not necessarily have to be of the kind understood by the garbage collector. We use the trick of linking a Node that has just been dequeued to itself. Such a self-link implicitly means to advance to 
`head.next`.
