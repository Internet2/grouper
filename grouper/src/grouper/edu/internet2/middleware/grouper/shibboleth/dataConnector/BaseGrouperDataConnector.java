/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.MembersField;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.field.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.grouper.shibboleth.util.AttributeIdentifier;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;

public abstract class BaseGrouperDataConnector extends BaseDataConnector {

  /** logger */
  private static final Logger LOG = GrouperUtil.getLogger(BaseGrouperDataConnector.class);

  /** the name of the attribute whose value is the name of the parent stem */
  public static final String PARENT_STEM_NAME_ATTR = "parentStemName";

  /** the grouper session, initialized for each grouper data connector */
  private GrouperSession grouperSession;

  /** the attributes which should be returned by this data connector */
  private List<AttributeIdentifier> fieldIdentifiers;

  /** the groups to return as attributes */
  private ArrayList<GroupsField> groupsFields = new ArrayList<GroupsField>();

  /** the members to return as attributes */
  private ArrayList<MembersField> membersFields = new ArrayList<MembersField>();

  /** the privileges to return as attributes */
  private ArrayList<PrivilegeField> privilegeFields = new ArrayList<PrivilegeField>();

  /** the query which filters the groups returned by this data connector */
  private GroupQueryFilter groupQueryFilter;

  /** a set of valid names for the first element of an attribute identifier */
  private static Set<String> validFirstIdElements = new HashSet<String>();
  static {
    validFirstIdElements.add(GroupsField.NAME);
    validFirstIdElements.add(MembersField.NAME);
    validFirstIdElements.addAll(AccessPrivilege.getAllPrivilegeNames());
  }

  /**
   * Constructor.
   */
  public BaseGrouperDataConnector() {
    super();
  }

  /**
   * Make sure that the attributes to return as specified in the data connector configuration are valid, and initialize
   * the necessary objects.
   * 
   * @throws GrouperException
   */
  // FUTURE probably should use spring for this configuration
  public void initialize() throws GrouperException {

    for (AttributeIdentifier fieldIdentifier : fieldIdentifiers) {

      BaseField bf = new BaseField(fieldIdentifier.getId());

      if (!validFirstIdElements.contains(bf.getFirstIdElement())) {
        throw new GrouperException("Invalid identifer '" + fieldIdentifier.getId() + "', should start with one of "
            + validFirstIdElements);
      }

      if (bf.getFirstIdElement().equals(GroupsField.NAME)) {

        groupsFields.add(new GroupsField(fieldIdentifier.getId()));

      } else if (bf.getFirstIdElement().equals(MembersField.NAME)) {

        membersFields.add(new MembersField(fieldIdentifier.getId()));

      } else {
        privilegeFields.add(new PrivilegeField(fieldIdentifier.getId(), getGrouperSession().getAccessResolver()));
      }
    }

    privilegeFields.trimToSize();
    membersFields.trimToSize();
    groupsFields.trimToSize();
  }

  /**
   * Get the grouper session. Starts a new root session if necessary. Re-uses the same session.
   * 
   * @return the grouper session
   */
  public GrouperSession getGrouperSession() {
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession();
      LOG.debug("started grouper session '{}'", grouperSession);
    }
    return grouperSession;
  }

  /**
   * Get the filter which determines the groups which will be considered by this data connector.
   * 
   * @return the GroupQueryFilter or <tt>null</tt> if all groups should be considered
   */
  public GroupQueryFilter getGroupQueryFilter() {
    return groupQueryFilter;
  }

  /**
   * Set the group query filter
   * 
   * @param groupQueryFilter
   *          the GroupQueryFilter
   */
  public void setGroupQueryFilter(GroupQueryFilter groupQueryFilter) {
    this.groupQueryFilter = groupQueryFilter;
  }

  /**
   * Set the identifiers of the attributes to return.
   * 
   * @param fieldIdentifiers
   */
  public void setFieldIdentifiers(List<AttributeIdentifier> fieldIdentifiers) {
    this.fieldIdentifiers = fieldIdentifiers;
  }

  /**
   * The representation of the attributes which return groups.
   * 
   * @return the groups fields
   */
  public List<GroupsField> getGroupsFields() {
    return groupsFields;
  }

  /**
   * The representation of the attributes which return members.
   * 
   * @return the members fields
   */
  public List<MembersField> getMembersFields() {
    return membersFields;
  }

  /**
   * The representation of the attributes which return privileges.
   * 
   * @return the privileges fields
   */
  public List<PrivilegeField> getPrivilegeFields() {
    return privilegeFields;
  }

}
