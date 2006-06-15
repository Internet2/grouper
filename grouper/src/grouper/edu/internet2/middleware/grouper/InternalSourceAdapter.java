/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Internal <i>SourceAdapter</i> for retrieving {@link InternalSubject}s.
 * <p>
 * This subject adapter resolves two subjects:
 * </p>
 * <ul>
 * <li><i>GrouperAll</i></li>
 * <li><i>GrouperSystem</i></li>
 * </ul>
 * @author  blair christensen.
 * @version $Id: InternalSourceAdapter.java,v 1.11 2006-06-15 03:58:30 blair Exp $
 */
public class InternalSourceAdapter extends BaseSourceAdapter {

  // PUBLIC CLASS CONSTANTS //
  public static final String ID   = "g:isa";
  public static final String NAME = "Grouper: Internal Source Adapter";


  // PRIVATE INSTANCE VARIABLES //
  private Subject all     = null;
  private Subject root    = null;
  private Set     _types  = new LinkedHashSet();


  // CONSTRUCTORS //

  /**
   * Allocates new InternalSourceAdapter.
   * <pre class="eg">
   * InternalSourceAdapter msa = new InternalSourceAdapter();
   * </pre>
   */
  public InternalSourceAdapter() {
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
  public InternalSourceAdapter(String id, String name) {
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
      _types.add( SubjectTypeEnum.valueOf(GrouperConfig.IST) );
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
  } // public void init()

  /**
   * Unstructured search for Subjects.
   * <p>
   * This method is not implemented in this source adapter and will
   * return an empty set whenever called.
   * </p>
   * <pre class="eg">
   * // Search for subjects with the query string <i>test</i>.
   * SourceAdapter  sa        = new InternalSourceAdapter();
   * Set            subjects  = sa.searchValue("test");
   * </pre>
   * @param   searchValue Query string for finding subjects.
   * @return  Subjects matching search value.
   */
  public Set search(String searchValue) {
    // TODO The javadoc says this will never resolve anything
    //      but the code actually can resolve.  Which correct?
    Set results = new LinkedHashSet();
    try {
      results.add(this._resolveSubject(searchValue));
    }
    catch (SubjectNotFoundException eSNF) {
      // Ignore 
    }
    return results;
  } // public Set search(searchValue)


  // PRIVATE INSTANCE METHODS //

  // Resolve an internal subject
  private Subject _resolveSubject(String qry) 
    throws  SubjectNotFoundException
  {
    // TODO What attributes do I need to set?  Check with Gary at some point.
    if      (qry.equals(GrouperConfig.ALL)) {
      if (this.all == null) {
        this.all = new InternalSubject(qry, qry, this);
      }
      return this.all;
    }
    else if (qry.equals(GrouperConfig.ROOT)) {
      if (this.root == null) {
        this.root = new InternalSubject(qry, qry, this);
      }
      return this.root;
    }
    throw new SubjectNotFoundException("subject not found: " + qry);
  } // private Subject _resolveSubject(qry)

}

