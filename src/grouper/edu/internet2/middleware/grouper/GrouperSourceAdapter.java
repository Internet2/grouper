/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
import  net.sf.hibernate.*;


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
 *   &lt;id&gt;grouperAdapter&lt;/id&gt;
 *   &lt;name&gt;Grouper Adapter&lt;/name&gt;
 *   &lt;type&gt;group&lt;/type&gt;
 * &lt;/source&gt;
 * </pre>
 * @author  blair christensen.
 * @version $Id: GrouperSourceAdapter.java,v 1.3 2005-11-28 19:21:48 blair Exp $
 */
public class GrouperSourceAdapter extends BaseSourceAdapter {
  // TODO Is configuration necessary if used within Grouper?

  // Private Instance Variables
  private Set types = new LinkedHashSet();


  // Constructors

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter() {
    super();
  }

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter(String id, String name) {
    super(id, name);
  }


  // Public Instance Methods

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
  public Subject getSubject(String id) 
    throws SubjectNotFoundException 
  {
    try {
      return new GrouperSubject(
        GroupFinder.findByUuid(id), this
      );
    }
    catch (GroupNotFoundException e) {
      throw new SubjectNotFoundException(
        "subject not found: " + e.getMessage()
      );
    }
  } // public Subject getSubject(id)

  /**
   * Gets a {@link Group} subject by its name.
   * <p />
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
  public Subject getSubjectByIdentifier(String name) 
    throws SubjectNotFoundException 
  {
    try {
      return new GrouperSubject(
        GroupFinder.findByName(name), this
      );
    }
    catch (GroupNotFoundException e) {
      throw new SubjectNotFoundException(
        "subject not found: " + e.getMessage()
      );
    }
  } // public Subject getSubjectByIdentifier(name)

  /**
   * Gets the SubjectTypes supported by this source.
   * <pre class="eg">
   * // Return subject types supported by this source.
   * SourceAdapter  sa    = new GrouperSourceAdapter();
   * Set            types = sa.getSubjectTypes();
   * </pre>
   * @return  Subject types supported by this source.
   */
  public Set getSubjectTypes() {
    if (types.size() != 1) {
      types.add( SubjectTypeEnum.valueOf("group") );
    }
    return types;
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

  /**
   * Searches for {@link Group} subjects by naming attributes.
   * <p />
   * <p>
   * This method performs a fuzzy search on the <i>stem</i>,
   * <i>extension</i>, <i>displayExtension</i>, <i>name</i> and
   * <i>displayName</i> group attributes.
   * </p>
   * <pre class="eg">
   * // Use it within the Grouper API
   * Set subjects = SubjectFactory.search("admins");
   *
   * // Use it directly
   * Set subjects = source.search("admins");
   * </pre>
   */
  public Set search(String searchValue) {
    // TODO Implement and document
    return new LinkedHashSet();
  } // public Set search(searchValue)

}

