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
import java.util.List;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.shibboleth.AttributeIdentifier;
import edu.internet2.middleware.grouper.shibboleth.FieldMemberFilter;
import edu.internet2.middleware.grouper.shibboleth.GroupsField;
import edu.internet2.middleware.grouper.shibboleth.MembersField;
import edu.internet2.middleware.grouper.shibboleth.PrivilegeField;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;

public abstract class BaseGrouperDataConnector extends BaseDataConnector {

  private static final Logger LOG = GrouperUtil.getLogger(BaseGrouperDataConnector.class);

  public static final String PARENT_STEM_NAME_ATTR = "parentStemName";

  protected GrouperSession grouperSession;

  private List<AttributeIdentifier> fieldIdentifiers;

  protected ArrayList<PrivilegeField> privilegeFields = new ArrayList<PrivilegeField>();

  protected ArrayList<MembersField> membersFields = new ArrayList<MembersField>();

  protected ArrayList<GroupsField> groupsFields = new ArrayList<GroupsField>();

  private GroupQueryFilter groupQueryFilter;

  public BaseGrouperDataConnector() {
    super();
  }

  public void initialize() {
    initializeFields();
  }

  /**
   * Get the grouper session. Starts a new root session if necessary. Re-uses the same
   * session.
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

  private void initializeFields() {

    // TODO probably needs some work

    for (AttributeIdentifier fieldIdentifier : fieldIdentifiers) {
      LOG.debug("handling field '{}'", fieldIdentifier.getId());

      // admins
      // groups
      // groups:all|eff|imm|comp
      // members
      // members:all|eff|imm|comp:field

      List<String> ids = GrouperUtil.splitTrimToList(fieldIdentifier.getId(), ":");

      if (ids.get(0).equals("groups")) {

        String fieldName = GrouperConfig.LIST;
        FieldMemberFilter mf = FieldMemberFilter.all;

        if (ids.size() == 2) {
          FieldMemberFilter.valueOf(ids.get(1));
        } else if (ids.size() == 3) {
          FieldMemberFilter.valueOf(ids.get(1));
          fieldName = ids.get(2);
        }

        Field field = FieldFinder.find(fieldName, true);
        // TODO null or exception ?

        groupsFields.add(new GroupsField(fieldIdentifier.getId(), mf, field));

      } else if (ids.get(0).equals("members")) {

        String fieldName = GrouperConfig.LIST;
        FieldMemberFilter mf = FieldMemberFilter.all;

        if (ids.size() == 2) {
          FieldMemberFilter.valueOf(ids.get(1));
        } else if (ids.size() == 3) {
          FieldMemberFilter.valueOf(ids.get(1));
          fieldName = ids.get(2);
        }

        Field field = FieldFinder.find(fieldName, true);
        // TODO null or exception ?

        membersFields.add(new MembersField(fieldIdentifier.getId(), mf, field));

      } else {

        String fieldName = ids.get(0);

        Field field = FieldFinder.find(fieldName, true);

        if (field.getType().equals(FieldType.ACCESS)) {
          Privilege privilege = AccessPrivilege.listToPriv(field.getName());
          if (privilege == null) {
            LOG.error("Unable to initialize privilege {}", fieldName);
            throw new GrouperException("Unable to initialize privilege " + fieldName);
          }

          privilegeFields
              .add(new PrivilegeField(fieldIdentifier.getId(), grouperSession.getAccessResolver(), privilege));

        } else {
          LOG.error("Unable to initialize privilege {} with type {}", fieldName, field.getType());
          throw new GrouperException("Unable to initialize privilege " + fieldName + " of type " + field.getType());
        }
      }
    }

    privilegeFields.trimToSize();
    membersFields.trimToSize();
    groupsFields.trimToSize();
  }

  public void setFieldIdentifiers(List<AttributeIdentifier> fieldIdentifiers) {
    this.fieldIdentifiers = fieldIdentifiers;
  }

  /**
   * Get the filter which determines the groups which will be considered by this data
   * connector.
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

}
