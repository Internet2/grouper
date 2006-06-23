/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.subject;

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Mock <i>SourceAdapter</i> for a {@link MockSubject}.
 * <p />
 * @author  blair christensen.
 * @version $Id: MockSourceAdapter.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class MockSourceAdapter extends BaseSourceAdapter {

  /*
   * CONSTRUCTORS
   */

  /**
   * Allocates new MockSourceAdapter.
   * <pre class="eg">
   * MockSourceAdapter msa = new MockSourceAdapter();
   * </pre>
   */
  public MockSourceAdapter() {
    super();
  } // public MockSourceAdapter()

  /**
   * Allocates new MockSourceAdapter.
   * <pre class="eg">
   * SourceAdapter sa = new MockSourceAdapter(name, id);
   * </pre>
   * @param name  Name of the adapter.
   * @param id    Identity of the adapter.
   */
  public MockSourceAdapter(String name, String id) {
    super(id, name);
  } // public MockSourceAdapter(name, id)

  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Gets a Subject by its ID.
   * <pre class="eg">
   * // Return a subject with the id <i>john</i>.
   * SourceAdapter  sa    = new MockSourceAdapter();
   * Subject        subj  = sa.getSubject("john");
   * </pre>
   * @param   id  Subject id to return.
   * @return  A mock subject.
   */
  public Subject getSubject(String id) {
    return new MockSubject(id, id, this);
  } // public Subject getsubject(id)

  /**
   * Gets a Subject by other well-known identifiers, aside from the
   * subject ID.
   * <pre class="eg">
   * // Return a subject with the identity <i>john</i>.
   * SourceAdapter  sa    = new MockSourceAdapter();
   * Subject        subj  = sa.getSubjectByIdentifier("john");
   * </pre>
   * @param   id  Identity of subject to return.
   * @return  A mock subject.
   */
  public Subject getSubjectByIdentifier(String id) {
    return new MockSubject(id, id, this);
  } // public Subject getSubjectByIdentifier(id)

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * // Return subject types supported by this source.
   * SourceAdapter  sa    = new MockSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject type supported by this source.
   */
  public Set getSubjectTypes() {
    return new HashSet();
  } // public Set getSubjectTypes()

  /**
   * Called by SourceManager when it loads this source.
   * <p>No initialization is performed by this source adapter.</p>
   * <pre class="eg">
   * // Initialize this source adapter.
   * SourceAdapter sa = new MockSourceAdapter();
   * sa.init();
   * </pre>
   */
  public void init() {
    // Nothing
  } // public void init()

  /**
   * Unstructured search for Subjects.
   * <p>
   * This method is not implemented in this source adapter and will
   * return an empty set whenever called.
   * </p>
   * <pre class="eg">
   * // Search for subjects with the query string <i>test</i>.
   * SourceAdapter  sa        = new MockSourceAdapter();
   * Set            subjects  = sa.searchValue("test");
   * </pre>
   * @param   searchValue Query string for finding subjects.
   * @return  Subjects matching search value.
   */
  public Set search(String searchValue) {
    return new HashSet();
  } // public Set search(searchValue)

}

