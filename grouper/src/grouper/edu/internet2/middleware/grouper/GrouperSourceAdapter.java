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

package edu.internet2.middleware.grouper;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.filter.GroupNameFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * Source adapter for using Grouper groups as I2MI Subjects.
 * <p/>
 * <p>
 * This is an adapter I2MI Subjects of type <i>group</i>.  It allows
 * groups within a Group Groups Registry to be referenced as I2MI
 * Subjects.  
 * <p>
 * To use, add the following to your <i>sources.xml</i> file:
 * </p>
 * <pre class="eg">
 * &lt;source adapterClass="edu.internet2.middleware.grouper.GrouperSourceAdapter"&gt;
 *   &lt;id&gt;g:gsa&lt;/id&gt;
 *   &lt;name&gt;Grouper: Grouper Source Adapter&lt;/name&gt;
 *   &lt;type&gt;group&lt;/type&gt;
 * &lt;/source&gt;
 * </pre>
 * @author  blair christensen.
 * @version $Id: GrouperSourceAdapter.java,v 1.31 2009-08-12 04:52:21 mchyzer Exp $
 */
public class GrouperSourceAdapter extends BaseSourceAdapter {

  /** types */
  private Set _types  = new LinkedHashSet();
  
  /** root grouper session */
  private GrouperSession  rootSession      = null;


  // CONSTRUCTORS //

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter() {
    super();
  } // public GrouperSourceAdapter()

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter(String id, String name) {
    super(id, name);
  } // public GrouperSourceAdapter(id, name)

  /**
   * Get a {@link Group} subject by UUID.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   id  Group UUID
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  @Deprecated
  public Subject getSubject(String id) 
    throws SubjectNotFoundException 
  {
    return getSubject(id, true);
  }

  /**
   * Get a {@link Group} subject by UUID.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubject(uuid, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   id  Group UUID
   * @param exceptionIfNotFound 
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public Subject getSubject(String id, boolean exceptionIfNotFound) throws SubjectNotFoundException {
    try {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(id, exceptionIfNotFound);
      if (group == null && !exceptionIfNotFound) {
        return null;
      }
      return new GrouperSubject(group);
    }
    catch (GroupNotFoundException eGNF) {
      throw new SubjectNotFoundException( "subject not found: " + eGNF.getMessage(), eGNF );
    }
  }
  
  /**
   * Gets a {@link Group} subject by its name.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubjectByIdentifier(name, "group");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubjectByIdentifier(name, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   name  Group name
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  @Deprecated
  public Subject getSubjectByIdentifier(String name) 
    throws SubjectNotFoundException 
  {
    return getSubjectByIdentifier(name, true);
  }
  /**
   * Gets a {@link Group} subject by its name.
   * <p/>
   * <pre class="eg">
   * // Use it within the Grouper API
   * try {
   *   Subject subj = SubjectFinder.getSubjectByIdentifier(name, "group");
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   *
   * // Use it directly
   * try {
   *   Subject subj = source.getSubjectByIdentifier(name, "group");
   * } 
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @param   name  Group name
   * @param exceptionIfNull 
   * @return  A {@link Subject}
   * @throws  SubjectNotFoundException
   */
  public Subject getSubjectByIdentifier(String name, boolean exceptionIfNull) 
    throws SubjectNotFoundException 
  {
    try {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByName(name, exceptionIfNull);
      if (group == null && !exceptionIfNull) {
        return null;
      }
      return new GrouperSubject(group);
    }
    catch (GroupNotFoundException eGNF) {
      throw new SubjectNotFoundException( "subject not found: " + eGNF.getMessage(), eGNF );
    }
  } // public Subject getSubjectByIdentifier(name)

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * SourceAdapter  sa    = new GrouperSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject types supported by this source.
   */
  public Set getSubjectTypes() {
    if (_types.size() != 1) {
      _types.add( SubjectTypeEnum.valueOf("group") );
    }
    return _types;
  } // public Set getSubjectTypes()

  /** 
   * Initializes the Grouper source adapter.
   * <p/>
   * <p>
   * No initialization is currently performed by this adapter.
   * </p>
   * @throws  SourceUnavailableException
   */
  public void init() throws SourceUnavailableException {
    // Nothing
  } // public void init()

  /** for testing if we should fail on testing */
  public static boolean failOnSearchForTesting = false;
  
  /**
   * Searches for {@link Group} subjects by naming attributes.
   * <p/>
   * <p>
   * This method performs a fuzzy search on the <i>stem</i>,
   * <i>extension</i>, <i>displayExtension</i>, <i>name</i> and
   * <i>displayName</i> group attributes.
   * </p>
   * <pre class="eg">
   * // Use it within the Grouper API
   * Set subjects = SubjectFactory.search(Field.FIELD_NAME_ADMINS);
   *
   * // Use it directly
   * Set subjects = source.search(Field.FIELD_NAME_ADMINS);
   * </pre>
   * @throws  IllegalArgumentException if <i>searchValue</i> is null.
   */
  public Set<Subject> search(final String searchValue) 
    throws  IllegalArgumentException
  {
    
    String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
    final boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

    final Set<Subject>   subjs  = new LinkedHashSet();
    GrouperSession.callbackGrouperSession(this._getSession(), new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        GrouperValidator v = NotNullValidator.validate(searchValue);
        if (v.isInvalid()) {
          throw new IllegalArgumentException( v.getErrorMessage() );
        }
        
        Stem  root   = StemFinder.findRootStem(grouperSession);
        try {
          
          if (failOnSearchForTesting) {
            throw new RuntimeException("failOnSearchForTesting");
          }
          
          GrouperQuery gq = null;
          
          if (GrouperConfig.getPropertyBoolean("pageGroupSource", true)) {
            //only search for groups and roles, entitys have their own source
            gq = GrouperQuery.createQuery(
              grouperSession, new GroupNameFilter(searchValue, root, "displayName", true, 1, 1000, GrouperUtil.toSet(TypeOfGroup.group, TypeOfGroup.role))
            );
          } else {
            gq = GrouperQuery.createQuery(
                grouperSession, new GroupNameFilter(searchValue, root)
              );
          }
           
          Group     g;
          Iterator  iter  = gq.getGroups().iterator();
          while (iter.hasNext()){
            g = (Group) iter.next();
            subjs.add(g.toSubject()); 
          }
        }
        catch (Exception ex) {
 
          if (!throwErrorOnFindAllFailure) {
            LOG.error(ex.getMessage() + ", source: " + GrouperSourceAdapter.this.getId() + ", searchValue: "
              + searchValue, ex);
          } else {
            throw new SourceUnavailableException(ex.getMessage() + ", source: " 
                + GrouperSourceAdapter.this.getId() + ", searchValue: "
                + searchValue, ex);
          }

        } 
        return null;
      }
      
    });
    
    return subjs;
  } // public Set search(searchValue)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSourceAdapter.class);


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private GrouperSession _getSession() {
	//If we have a thread local session then let's use it to ensure 
	//proper VIEW privilege enforcement
	GrouperSession activeSession = GrouperSession.staticGrouperSession(false);
	if(activeSession !=null) {
		return activeSession;
	}
    if (this.rootSession == null) {
      try {
        //dont replace the currently active session
        this.rootSession = GrouperSession.start( SubjectFinder.findRootSubject(), false );
      }
      catch (SessionException eS) {
        throw new GrouperException(E.S_NOSTARTROOT + eS.getMessage());
      }
    }
    return this.rootSession;
  } // private GrouperSession _getSession()

  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {
  }

  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {
    String message = "sources.xml groupersource id: " + this.getId();
    return message;
  }
}

