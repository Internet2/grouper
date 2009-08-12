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

package edu.internet2.middleware.grouper.subj;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

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
 * @version $Id: InternalSourceAdapter.java,v 1.5 2009-08-12 04:52:21 mchyzer Exp $
 */
public class InternalSourceAdapter extends BaseSourceAdapter {

  // PUBLIC CLASS CONSTANTS //
  public static final String ID   = "g:isa";
  public static final String NAME = "Grouper: Internal Source Adapter";


  // PRIVATE INSTANCE VARIABLES //
  private Subject all     = null;
  private Subject root    = null;
  private Set     _types  = new LinkedHashSet();
  private String allName = getAllName();
  private String rootName = getRootName();

  /** singleton */
  private static InternalSourceAdapter instance = new InternalSourceAdapter();
  
  /**
   * singleton
   * @return the singleton
   */
  public static InternalSourceAdapter instance() {
    return instance;
  }
  
  /**
   * Allocates new InternalSourceAdapter.
   * <pre class="eg">
   * InternalSourceAdapter msa = new InternalSourceAdapter();
   * </pre>
   */
  private InternalSourceAdapter() {
    super(InternalSourceAdapter.ID, InternalSourceAdapter.NAME);
  } // public InternalSourceAdapter()

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
  @Deprecated
  public Subject getSubject(String id) 
    throws SubjectNotFoundException {
    return getSubject(id, true);
  }

  /**
   * Gets a Subject by its ID.
   * <pre class="eg">
   * // Return a subject with the id <i>john</i>.
   * SourceAdapter  sa    = new InternalSourceAdapter();
   * Subject        subj  = sa.getSubject("john");
   * </pre>
   * @param   id  Subject id to return.
   * @param exceptionIfNull 
   * @return  An internal subject.
   * @throws  SubjectNotFoundException
   */
  public Subject getSubject(String id, boolean exceptionIfNull) 
    throws SubjectNotFoundException {
    return this._resolveSubject(id, false, exceptionIfNull);
  }
  
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
  @Deprecated
  public Subject getSubjectByIdentifier(String id) 
    throws SubjectNotFoundException
  {
    return this.getSubjectByIdentifier(id, true);
  }

  
  /**
   * Gets a Subject by other well-known identifiers, aside from the
   * subject ID.
   * <pre class="eg">
   * // Return a subject with the identity <i>john</i>.
   * SourceAdapter  sa    = new InternalSourceAdapter();
   * Subject        subj  = sa.getSubjectByIdentifier("john");
   * </pre>
   * @param   id  Identity of subject to return.
   * @param exceptionIfNull SubjectNotFoundException exception if null result
   * @return  An internal subject.
   * @throws  SubjectNotFoundException
   */
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull) 
    throws SubjectNotFoundException {
    return this._resolveSubject(id, false, exceptionIfNull);
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
    Subject subject = this._resolveSubject(searchValue,true, false);
    if (subject != null) {
      results.add(subject);
    }
    return results;
  } // public Set search(searchValue)


  private Subject _resolveSubject(String qry,boolean fuzzy, boolean exceptionIfNotFound) 
    throws  SubjectNotFoundException
  {
    if(qry.equals(GrouperConfig.ALL) || (fuzzy && (
    		qry.equalsIgnoreCase(allName) || qry.equalsIgnoreCase(GrouperConfig.ALL)
    	)	
    	))
    		 {
      if (this.all == null) {
        this.all = new InternalSubject(GrouperConfig.ALL, allName, this);
      }
      return this.all;
    }
    else if (qry.equals(GrouperConfig.ROOT)|| (fuzzy && (
    		qry.equalsIgnoreCase(rootName) || qry.equalsIgnoreCase(GrouperConfig.ROOT)
    		)
	))	 {
      if (this.root == null) {
        this.root = new InternalSubject(GrouperConfig.ROOT, rootName, this);
      }
      return this.root;
    }
    if (exceptionIfNotFound) {
      throw new SubjectNotFoundException("subject not found: " + qry);
    }
    return null;
  } // private Subject _resolveSubject(qry)
  
  private String getAllName() {
	  String name = GrouperConfig.getProperty("subject.internal.grouperall.name");
	  if(name==null || "".equals(name)) {
		  name = GrouperConfig.ALL_NAME;
	  }
	  return name;
  }
  
  private String getRootName() {
	  String name = GrouperConfig.getProperty("subject.internal.groupersystem.name");
	  if(name==null || "".equals(name)) {
		  name = GrouperConfig.ROOT_NAME;
	  }
	  return name;
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
    String message = "sources.xml internalsource id:" + this.getId();
    return message;
  }
} // public class InternalSourceAdapter

