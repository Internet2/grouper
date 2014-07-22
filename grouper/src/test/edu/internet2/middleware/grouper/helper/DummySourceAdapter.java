/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.helper;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * Dummy <i>SourceAdapter</i> for retrieving any subject (not fromadata source)
 * <p>
 * This subject adapter resolves any subject
 * </p>
 * @author  chris hyzer
 * @version $Id: DummySourceAdapter.java,v 1.2 2009-09-02 05:57:26 mchyzer Exp $
 */
public class DummySourceAdapter extends BaseSourceAdapter {

  /** types supported */
  private Set     _types  = new LinkedHashSet();

  /**
   * Allocates new InternalSourceAdapter.
   * <pre class="eg">
   * InternalSourceAdapter msa = new InternalSourceAdapter();
   * </pre>
   */
  public DummySourceAdapter() {
    super();
  } // public InternalSourceAdapter()

  /**
   * Allocates new InternalSourceAdapter.
   * <pre class="eg">
   * SourceAdapter sa = new InternalSourceAdapter(id, name);
   * </pre>
   * @param id    Identity of the adapter.
   * @param name  Name of the adapter.
   */
  public DummySourceAdapter(String id, String name) {
    super(id, name);
  } // public InternalSourceAdapter(name, id)


  // PUBLIC INSTANCE METHODS //

  /**
   * Gets a Subject by its ID.
   * <pre class="eg">
   * // Return a subject with the id <i>john</i>.
   * SourceAdapter  sa    = new InternalSourceAdapter();
   * Subject        subj  = sa.getSubject("john");
   * </pre>
   * @param   id  Subject id to return.
   * @return  An internal subject.
   * @throws  SubjectNotFoundException
   */
  public Subject getSubject(String id) 
    throws SubjectNotFoundException
  {
    return this._resolveSubject(id);
  } // public Subject getsubject(id)

  /**
   * Gets a Subject by other well-known identifiers, aside from the
   * subject ID.
   * <pre class="eg">
   * // Return a subject with the identity <i>john</i>.
   * SourceAdapter  sa    = new InternalSourceAdapter();
   * Subject        subj  = sa.getSubjectByIdentifier("john");
   * </pre>
   * @param   id  Identity of subject to return.
   * @return  An internal subject.
   * @throws  SubjectNotFoundException
   */
  public Subject getSubjectByIdentifier(String id) 
    throws SubjectNotFoundException
  {
    return this._resolveSubject(id);
  } // public Subject getSubjectByIdentifier(id)

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * SourceAdapter  sa    = new InternalSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject type supported by this source.
   */
  public Set getSubjectTypes() {
    if (_types.size() != 1) {
      _types.add( SubjectTypeEnum.PERSON );
    }
    return _types;
  } // public Set getSubjectTypes()

  /**
   * Called by SourceManager when it loads this source.
   * <p>No initialization is performed by this source adapter.</p>
   * <pre class="eg">
   * // Initialize this source adapter.
   * SourceAdapter sa = new InternalSourceAdapter();
   * sa.init();
   * </pre>
   */
  public void init() {
    // Nothing
  } 

  /**
   * Unstructured search for Subjects.
   * <pre class="eg">
   * // Search for subjects with the query string <i>test</i>.
   * SourceAdapter  sa        = new InternalSourceAdapter();
   * Set            subjects  = sa.searchValue("test");
   * </pre>
   * @param   searchValue Query string for finding subjects.
   * @return  Subjects matching search value.
   */
  public Set search(String searchValue) {
    Set results = new LinkedHashSet();
    try {
      results.add(this._resolveSubject(searchValue));
    }
    catch (SubjectNotFoundException eSNF) {
      // Ignore 
    }
    return results;
  } 

  /**
   * all subjects are found
   * @param qry
   * @return the subject
   * @throws SubjectNotFoundException
   */
  private Subject _resolveSubject(String qry) 
      throws  SubjectNotFoundException { 
    return new DummySubject(qry, this.getId());
  }

  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {
  } 

  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {
    String message = "sources.xml dummy source id:  " + this.getId();
    return message;
  }
} 

