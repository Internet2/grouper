/*******************************************************************************
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
 ******************************************************************************/
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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
package edu.internet2.middleware.grouper.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectFinder.RestrictSourceForGroup;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * The default implementation of UIGroupPrivilegeResolver - which simply
 * applies the expected privilege resolution. Extend this class to add
 * your own business logic, and configure in media.properties using the key:
 * edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver
 * see https://bugs.internet2.edu/jira/browse/GRP-72
 *
 * @author Gary Brown.
 * @version $Id: DefaultUIGroupPrivilegeResolver.java,v 1.4 2009-03-15 06:37:51 mchyzer Exp $
 */
public class DefaultUIGroupPrivilegeResolver implements
		UIGroupPrivilegeResolver {
	protected Group group;
	protected Subject subject;
	protected GrouperSession s;
	protected boolean inited=false;

	Map privs;

  /** logger */
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(DefaultUIGroupPrivilegeResolver.class);


	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#init()
	 */
	public void init() {

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canEditGroup()
	 */
	public boolean canEditGroup() {
		try {
			return group.hasAdmin(subject);
			//return group.canWriteField(FieldFinder.find("extension", true));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageMembers()
	 */
	public boolean canManageMembers() {
		try {
			return group.canWriteField(FieldFinder.find("members", true)) && !this.group.isHasComposite();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManagePrivileges()
	 */
	public boolean canManagePrivileges() {
		try {
			return group.hasAdmin(subject);
			//return group.canWriteField(FieldFinder.find("extension", true));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageField(java.lang.String)
	 */
	public boolean canManageField(String field) {
		try {
		  String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
		  if (!field.startsWith(stemName)) {
		    return group.canWriteField(FieldFinder.find(field, true));
		  }

      String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
      AttributeDefName legacyAttribute = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(field, false);
      if (legacyAttribute == null) {
        // probably not allowed to see attribute def
        return false;
      }

      AttributeDef legacyAttributeDef = legacyAttribute.getAttributeDef();
      String groupTypeName = legacyAttributeDef.getExtension().substring(attributeDefPrefix.length());

      AttributeAssign groupTypeAssignment = group.internal_getGroupTypeAssignments().get(groupTypeName);

      try {
        groupTypeAssignment.getAttributeDelegate().assertCanUpdateAttributeDefName(legacyAttribute);
        return true;
      } catch (InsufficientPrivilegeException e) {
        return false;
      } catch (AttributeDefNotFoundException e) {
        return false;
      }
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canReadField(java.lang.String)
	 */
	public boolean canReadField(String field) {
		try {
      String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
      if (!field.startsWith(stemName)) {
        return group.canReadField(FieldFinder.find(field, true));
      }

      String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
      AttributeDefName legacyAttribute = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(field, false);
      if (legacyAttribute == null) {
        // probably not allowed to see attribute def
        return false;
      }

      AttributeDef legacyAttributeDef = legacyAttribute.getAttributeDef();
      String groupTypeName = legacyAttributeDef.getExtension().substring(attributeDefPrefix.length());

      AttributeAssign groupTypeAssignment = group.internal_getGroupTypeAssignments().get(groupTypeName);

      try {
        groupTypeAssignment.getAttributeDelegate().assertCanReadAttributeDefName(legacyAttribute);
        return true;
      } catch (InsufficientPrivilegeException e) {
        return false;
      } catch (AttributeDefNotFoundException e) {
        return false;
      }
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}


	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageAnyCustomField()
	 */
	public boolean canManageAnyCustomField() {
		try {
			return GrouperHelper.canUserEditAnyCustomAttribute(group);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canOptinGroup()
	 */
	public boolean canOptinGroup() {
		return group.hasOptin(subject);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canOptoutGroup()
	 */
	public boolean canOptoutGroup() {
		return group.hasOptout(subject);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canReadGroup()
	 */
	public boolean canReadGroup() {
		try {
			return group.canReadField(FieldFinder.find("members", true));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canViewGroup()
	 */
	public boolean canViewGroup() {
		try {
			return group.hasView(subject);
			//return group.canReadField(FieldFinder.find("displayExtension", true));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setGroup(edu.internet2.middleware.grouper.Group)
	 */
	public final void setGroup(Group g) {
		if(group!=null) throw new IllegalStateException("setGroup already called");
		group=g;
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setGrouperSession(edu.internet2.middleware.grouper.GrouperSession)
	 */
	public void setGrouperSession(GrouperSession s) {
		if(this.s!=null) throw new IllegalStateException("setGrouperSession already called");
		this.s=s;

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setSubject(edu.internet2.middleware.subject.Subject)
	 */
	public void setSubject(Subject subj) {
		if(subject!=null) throw new IllegalStateException("setSubject lready called");
		subject=subj;

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#asMap()
	 */
	public Map asMap() {
		Map map = new HashMap();
    map.put("canInviteExternalPeople", canInviteExternalPeople());
		map.put("canEditGroup", canEditGroup());
		map.put("canManageMembers", canManageMembers());
		map.put("canManagePrivileges", canManagePrivileges());
		map.put("canManageAnyCustomField", canManageAnyCustomField());
		map.put("canOptinGroup", canOptinGroup());
		map.put("canOptoutGroup", canOptoutGroup());
		map.put("canViewGroup", canViewGroup());
		map.put("canReadGroup", canReadGroup());
		Map fieldManageMap = new HashMap();
		Map fieldReadMap = new HashMap();
		map.put("canManageField", fieldManageMap);
		map.put("canReadField", fieldReadMap);
		Set<GroupType> types=group.getTypes();
		Set<Field> fields;
		for(GroupType type : types) {
			if(!type.isSystemType()) {
				fields = type.getFields();
				for(Field field : fields) {
					fieldManageMap.put(field.getName(), canManageField(field.getName()));
					fieldReadMap.put(field.getName(), canReadField(field.getName()));
				}

        for(AttributeDefName legacyAttribute : type.getLegacyAttributes()) {
          fieldManageMap.put(legacyAttribute.getName(), canManageField(legacyAttribute.getName()));
          fieldReadMap.put(legacyAttribute.getName(), canReadField(legacyAttribute.getName()));
        }
			}
		}
		return map;
	}


	/**
	 * @see UIGroupPrivilegeResolver#canInviteExternalPeople()
	 */
  @Override
  public boolean canInviteExternalPeople() {

    StringBuilder logMessage = new StringBuilder();

    boolean result = true;

    try {

      //if it is altogether enabled
      boolean mediaEnableInvitation = GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalMembers.enableInvitation", false);
      if (LOG.isDebugEnabled()) {
        logMessage.append("media.properties enableInvitation: ").append(mediaEnableInvitation).append(", ");
      }
      if (!mediaEnableInvitation) {
        result = false;
        return false;
      }

      //if this is the wheel group, and not allowed to invite wheel, then no
      boolean groupIsNull = this.group == null;

      // Originally from InviteExternalSubjects class, now moved to legacy UI
      //boolean filterGroup = InviteExternalSubjects.filterGroup(this.group);
      boolean filterGroup = false;
      boolean allowWheel = GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalMembers.allowWheelInInvite", false);
      boolean useWheel = GrouperConfig.retrieveConfig().propertyValueBoolean("groups.wheel.use", false);
      String wheelName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      if (!allowWheel && useWheel && !StringUtils.isBlank(wheelName) && StringUtils.equals(wheelName, this.group.getName())) {
        filterGroup = true;
      }

      if (LOG.isDebugEnabled()) {
        logMessage.append("groupIsNull: ").append(groupIsNull).append(", filterGroup: ").append(filterGroup).append(", ");
      }

      if (groupIsNull || filterGroup) {
        result = false;
        return false;
      }

      boolean canManageMembers = this.canManageMembers();

      if (LOG.isDebugEnabled()) {
        logMessage.append("canManageMembers: ").append(canManageMembers).append(", ");
      }

      if (!canManageMembers) {
        result = false;
        return false;
      }

      final String requireGroupName = GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.inviteExternalSubjects.logins");
      if (!StringUtils.isBlank(requireGroupName)) {
        GrouperSession grouperSession = this.s;
        final Subject currentUser = this.s.getSubject();
        if (!PrivilegeHelper.isWheelOrRoot(this.s.getSubject())) {
          grouperSession = this.s.internal_getRootSession();
        }
        boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

          /**
           *
           */
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            Group requireGroup = GroupFinder.findByName(theGrouperSession, requireGroupName, true);
            return requireGroup.hasMember(currentUser);
          }
        });
        if (LOG.isDebugEnabled()) {
          logMessage.append("group: ").append(requireGroupName).append(", currentUser: ")
            .append(GrouperUtil.subjectToString(currentUser)).append(", hasMember: ").append(allowed).append(", ");
        }
        if (!allowed) {
          result = false;
          return false;
        }
      }

      //lets see if the source is available for this group...
      String sourceId = ExternalSubject.sourceId();
      if (!StringUtils.isBlank(sourceId)) {

        String stemName = this.group.getParentStemName();
        RestrictSourceForGroup restrictSourceForGroup = SubjectFinder.restrictSourceForGroup(stemName, sourceId);

        //if restricting all, dont show the link
        if (restrictSourceForGroup.isRestrict() && restrictSourceForGroup.getGroup() == null) {
          result=false;
          return false;
        }
      }

      result = true;
      return true;
    } finally {
      if (LOG.isDebugEnabled()) {
        logMessage.append("result = ").append(result);
        LOG.debug(logMessage.toString());
      }
    }
  }
}
