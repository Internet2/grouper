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

package edu.internet2.middleware.grouper.subj;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.provider.SubjectStatusProcessor;
import edu.internet2.middleware.subject.provider.SubjectStatusResult;
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
 * @version $Id: InternalSourceAdapter.java,v 1.6 2009-09-02 05:57:26 mchyzer Exp $
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
  
  private List<String> searchAttributeEl = new LinkedList<String>();
  private List<String> sortAttributeEl = new LinkedList<String>();
  

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
   * <pre class="eg">
   * // Initialize this source adapter.
   * SourceAdapter sa = new InternalSourceAdapter();
   * sa.init();
   * </pre>
   */
  public void init() {
    this.sortAttributes = null;
    this.searchAttributes = null;
    this.internalAttributes = new HashSet<String>();
    this.params = new Properties();
    sortAttributeEl.clear();
    searchAttributeEl.clear();
    this.all = null;
    this.root = null;
    
    sortAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.sortAttribute0.el"));
    sortAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.sortAttribute1.el"));
    sortAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.sortAttribute2.el"));
    sortAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.sortAttribute3.el"));
    sortAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.sortAttribute4.el"));
    searchAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.searchAttribute0.el"));
    searchAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.searchAttribute1.el"));
    searchAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.searchAttribute2.el"));
    searchAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.searchAttribute3.el"));
    searchAttributeEl.add(GrouperConfig.retrieveConfig().propertyValueString("internalSubjects.searchAttribute4.el"));
    
    for (int i = 0; i < sortAttributeEl.size(); i++) {
      if (!GrouperUtil.isEmpty(sortAttributeEl.get(i))) {
        this.addInitParam("subjectVirtualAttribute_" + i + "_sortAttribute" + i, sortAttributeEl.get(i));
        this.addInternalAttribute("sortAttribute" + i);
        this.addInitParam("sortAttribute" + i, "sortAttribute" + i);
      }
    }
    
    for (int i = 0; i < searchAttributeEl.size(); i++) {
      if (!GrouperUtil.isEmpty(searchAttributeEl.get(i))) {
        this.addInitParam("subjectVirtualAttribute_" + i + "_searchAttribute" + i, searchAttributeEl.get(i));
        this.addInternalAttribute("searchAttribute" + i);
        this.addInitParam("searchAttribute" + i, "searchAttribute" + i);
      }
    }
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
    
    //if this is a search and not by id or identifier, strip out the status part
    {
      SubjectStatusResult subjectStatusResult = null;
      
      //see if we are doing status
      SubjectStatusProcessor subjectStatusProcessor = new SubjectStatusProcessor(searchValue, this.getSubjectStatusConfig());
      subjectStatusResult = subjectStatusProcessor.processSearch();

      //strip out status parts
      searchValue = subjectStatusResult.getStrippedQuery();
    }      

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
    String queryLower = qry.toLowerCase();
    
    if(qry.equals(GrouperConfig.ALL) || (fuzzy && qry.length() > 4 &&  (
        allName.toLowerCase().contains(queryLower) || GrouperConfig.ALL.toLowerCase().contains(queryLower)
        || "everyentity".contains(queryLower)
    	)	
    	))
    		 {
      if (this.all == null) {
        this.all = this.createSubject(GrouperConfig.ALL, allName);
      }
      return this.all;
    }
    else if (qry.equals(GrouperConfig.ROOT)|| (fuzzy && qry.length() > 4 && (
        rootName.toLowerCase().contains(queryLower) || GrouperConfig.ROOT.toLowerCase().contains(queryLower)
        || "groupersysadmin".contains(queryLower)
    		)
	))	 {
      if (this.root == null) {
        this.root = this.createSubject(GrouperConfig.ROOT, rootName);
      }
      return this.root;
    }
    if (exceptionIfNotFound) {
      throw new SubjectNotFoundException("subject not found: " + qry);
    }
    return null;
  } // private Subject _resolveSubject(qry)
  
  private String getAllName() {
	  String name = GrouperConfig.retrieveConfig().propertyValueString("subject.internal.grouperall.name");
	  if(name==null || "".equals(name)) {
		  name = GrouperConfig.ALL_NAME;
	  }
	  return name;
  }
  
  private String getRootName() {
	  String name = GrouperConfig.retrieveConfig().propertyValueString("subject.internal.groupersystem.name");
	  if(name==null || "".equals(name)) {
		  name = GrouperConfig.ROOT_NAME;
	  }
	  return name;
  }

  /**
   * create a subject
   * @param id
   * @param name
   * @return the subject
   */
  private Subject createSubject(String id, String name) {
    Subject subject = new SubjectImpl(id, name, name, 
        SubjectTypeEnum.APPLICATION.getName(), this.getId(), 
        new HashMap<String, Set<String>>());
    subject.getAttributes(false).put("name", GrouperUtil.toSet(allName));
    
    return subject;
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

