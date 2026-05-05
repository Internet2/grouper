package edu.internet2.middleware.subject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import edu.internet2.middleware.subject.provider.SubjectImpl;
import junit.framework.TestCase;

/**
 * Regression test for the ConcurrentModificationException seen in
 * SubjectSourceCache.getSubjectByIdOrIdentifierFromCacheOrSource ->
 * SubjectImpl.cloneSubject -> new SubjectCaseInsensitiveMapImpl(Map).
 *
 * The cached SubjectImpl is shared across threads; cloneSubject was iterating
 * the source's attribute LinkedHashMap while another thread mutated it
 * (e.g. inside initAttributesIfNeeded, which holds synchronized(this)).
 */
public class SubjectImplCloneThreadSafetyTest extends TestCase {

  public void testConcurrentCloneAndAttributeMutation() throws Exception {

    // sourceId blank so initAttributesIfNeeded short-circuits and we don't need
    // a registered Source for this isolated test
    final SubjectImpl subject = new SubjectImpl("id1", "name1", "desc1", "person", "", null, null);
    final Map<String, Set<String>> attrs = subject.getAttributes(false);
    for (int i = 0; i < 50; i++) {
      Set<String> v = new HashSet<String>();
      v.add("v" + i);
      attrs.put("k" + i, v);
    }

    final AtomicBoolean stop = new AtomicBoolean(false);
    final AtomicReference<Throwable> readerError = new AtomicReference<Throwable>();

    // Mirrors SubjectImpl.initAttributesIfNeeded: mutates 'attributes' while
    // holding the monitor on the SubjectImpl.
    Thread writer = new Thread("clone-thread-safety-writer") {
      @Override
      public void run() {
        while (!stop.get()) {
          synchronized (subject) {
            Set<String> v = new HashSet<String>();
            v.add("vx");
            attrs.put("kx", v);
            attrs.remove("kx");
          }
        }
      }
    };

    Thread reader = new Thread("clone-thread-safety-reader") {
      @Override
      public void run() {
        try {
          for (int i = 0; i < 20000; i++) {
            SubjectImpl.cloneSubject(subject);
          }
        } catch (Throwable t) {
          readerError.set(t);
        }
      }
    };

    writer.start();
    reader.start();
    reader.join();
    stop.set(true);
    writer.join();

    Throwable t = readerError.get();
    if (t != null) {
      AssertionError ae = new AssertionError("cloneSubject failed under concurrent mutation: " + t);
      ae.initCause(t);
      throw ae;
    }
  }
}
