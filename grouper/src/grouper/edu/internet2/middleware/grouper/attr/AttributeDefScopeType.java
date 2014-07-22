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
package edu.internet2.middleware.grouper.attr;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of scope, like
 * @author mchyzer
 */
public enum AttributeDefScopeType {

  /** for member type attributes, filter on sourceId (none means allow all) */
  sourceId {

    @Override
    public boolean allowedAssignment(AttributeDefScope attributeDefScope,
        AttributeAssignable attributeAssignable, AttributeDef attributeDef) {
      
      //if member, check source, else, just false, no error
      if (attributeAssignable instanceof Member) {
        if (StringUtils.equals(attributeDefScope.getScopeString(), ((Member)attributeAssignable).getSubjectSourceId())) {
          return true;
        }
      }
      
      if (attributeAssignable instanceof GroupMember) {
        GroupMember groupMember = (GroupMember)attributeAssignable;
        if (groupMember.getMember() != null) {
          return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), groupMember.getMember().getSubjectSourceId());
        }
        return false;
      }
      
      return false;
    }
  },
  
  /** this attribute can be assigned only if another attribute def name id is assigned */
  attributeDefNameIdAssigned {

    @Override
    public boolean allowedAssignment(final AttributeDefScope attributeDefScope,
        final AttributeAssignable attributeAssignable, final AttributeDef attributeDef) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();

      return (Boolean)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          return attributeAssignable.getAttributeDelegate().hasAttributeById(
              StringUtils.trimToNull(attributeDefScope.getScopeString2()), attributeDefScope.getScopeString());
          
        }
      });
      
    }
  },
  
  /** stemId of the stem the object needs to be in for this attribute to be assigned */
  inStem {

    @Override
    public boolean allowedAssignment(AttributeDefScope attributeDefScope,
        AttributeAssignable attributeAssignable, AttributeDef attributeDef) {

      if (attributeAssignable instanceof Group) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Group)attributeAssignable).getParentUuid());
      }
      
      if (attributeAssignable instanceof GroupMember) {
        GroupMember groupMember = (GroupMember)attributeAssignable;
        if (groupMember.getGroup() != null) {
          return StringUtils.equals(attributeDefScope.getScopeString(), groupMember.getGroup().getParentUuid());
        }
        return false;
      }
      
      if (attributeAssignable instanceof Stem) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Stem)attributeAssignable).getParentUuid());
      }
      
      if (attributeAssignable instanceof AttributeDef) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeDef)attributeAssignable).getStemId());
      }
      
      if (attributeAssignable instanceof AttributeAssign) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeAssign)attributeAssignable).getAttributeDefName().getStemId());
      }

      return false;
      
    }
  },
  
  /** matching generally on namepsace names, its a like string in DB.  Can be stem or substem */
  nameLike {

    @Override
    public boolean allowedAssignment(AttributeDefScope attributeDefScope,
        AttributeAssignable attributeAssignable, AttributeDef attributeDef) {
      if (attributeAssignable instanceof Group) {
        return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), ((Group) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof GroupMember) {
        GroupMember groupMember = (GroupMember)attributeAssignable;
        if (groupMember.getGroup() != null) {
          return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), groupMember.getGroup().getName());
        }
        return false;
      }
      
      if (attributeAssignable instanceof Stem) {
        return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), ((Stem) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof AttributeDef) {
        return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), ((AttributeDef) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof Member) {
        return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), ((Member) attributeAssignable).getSubjectId());
      }
      
      if (attributeAssignable instanceof AttributeAssign) {
        return GrouperUtil.matchSqlString(attributeDefScope.getScopeString(), ((AttributeAssign) attributeAssignable).getAttributeDefName().getName());
      }

      return false;
    }
  },
  
  /** matching exact name */
  nameEquals {

    @Override
    public boolean allowedAssignment(AttributeDefScope attributeDefScope,
        AttributeAssignable attributeAssignable, AttributeDef attributeDef) {
      if (attributeAssignable instanceof Group) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Group) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof GroupMember) {
        GroupMember groupMember = (GroupMember)attributeAssignable;
        if (groupMember.getGroup() != null) {
          return StringUtils.equals(attributeDefScope.getScopeString(), groupMember.getGroup().getName());
        }
        return false;
      }
      
      if (attributeAssignable instanceof Stem) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Stem) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof AttributeDef) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeDef) attributeAssignable).getName());
      }
      
      if (attributeAssignable instanceof AttributeAssign) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeAssign) attributeAssignable).getAttributeDefName().getName());
      }
      
      return false;
    }
  },
  
  /** matching exact id */
  idEquals {

    @Override
    public boolean allowedAssignment(AttributeDefScope attributeDefScope,
        AttributeAssignable attributeAssignable, AttributeDef attributeDef) {
      if (attributeAssignable instanceof Group) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Group) attributeAssignable).getId());
      }
      
      if (attributeAssignable instanceof GroupMember) {
        GroupMember groupMember = (GroupMember)attributeAssignable;
        if (groupMember.getGroup() != null) {
          return StringUtils.equals(attributeDefScope.getScopeString(), groupMember.getGroup().getId());
        }
        return false;
      }
      
      if (attributeAssignable instanceof Stem) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Stem) attributeAssignable).getUuid());
      }
      
      if (attributeAssignable instanceof AttributeDef) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeDef) attributeAssignable).getId());
      }
      
      if (attributeAssignable instanceof Membership) {
        //id can equal or immediate membership id and type is immediate
        return StringUtils.equals(attributeDefScope.getScopeString(), ((Membership) attributeAssignable).getUuid())
          || (StringUtils.equals("immediate", ((Membership)attributeAssignable).getType())
            &&  StringUtils.equals(attributeDefScope.getScopeString(), 
                ((Membership) attributeAssignable).getImmediateMembershipId()));
      }

      if (attributeAssignable instanceof AttributeAssign) {
        return StringUtils.equals(attributeDefScope.getScopeString(), ((AttributeAssign) attributeAssignable).getAttributeDefNameId());
      }
      
      return false;
    }
  };
  
  /**
   * 
   * @param attributeDefScope
   * @param attributeAssignable
   * @param attributeDef
   * @return true if allowed, false if not allowed
   */
  public abstract boolean allowedAssignment(AttributeDefScope attributeDefScope, 
      AttributeAssignable attributeAssignable, AttributeDef attributeDef);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefScopeType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefScopeType.class, 
        string, exceptionOnNull);

  }

  
}
