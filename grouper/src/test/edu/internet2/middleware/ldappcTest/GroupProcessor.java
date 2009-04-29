/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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

package edu.internet2.middleware.ldappcTest;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Class for finding subjects.
 * 
 * @author Gil Singer
 */
public class GroupProcessor {

  /**
   * Group session.
   */
  private GrouperSession grouperSession;

  /**
   * Constructor: Creates and deletes stems and adds and deletes groups.
   * 
   * @param grouperSession
   *          the grouper session.
   */
  public GroupProcessor(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;

    if (this.grouperSession == null) {
      ErrorLog.fatal(this.getClass(),
          "DEBUG in GroupProcessor constructor, grouperSession is NULL.");
    }
  }

  /**
   * Delete a member.
   * 
   * @param group
   *          the group containing the member to be deleted
   * @param subject
   *          the subject to delete
   * @return true if operation is successful
   */
  public boolean deleteMember(Group group, Subject subject) {
    boolean success = true;
    //
    // Delete the member subject
    //

    if (subject != null) {
      try {
        group.deleteMember(subject);
        DebugLog.info(this.getClass(), "Deleted member: " + subject.getName());
      } catch (MemberDeleteException mde) {
        success = false;
        ErrorLog.error(this.getClass(), "Could not delete member: " + subject.getName()
            + " -- " + mde.getMessage());
      } catch (InsufficientPrivilegeException ipe) {
        success = false;
        ErrorLog.error(this.getClass(), "Insufficent privilege for deleting member: "
            + subject.getName() + " -- " + ipe.getMessage());
      }
    } else {
      ErrorLog.error(this.getClass(),
          "Attempting to delete a null member with a null subject.");
    }
    return success;
  }

  /**
   * Delete a member.
   * 
   * @param group
   *          the group containing the member to be deleted
   * @param member
   *          the member to delete
   * @return true if operation is successful
   */
  public boolean deleteMember(Group group, Member member) {
    boolean success = true;
    Subject subject = null;
    //
    // Delete the member
    //

    if (member != null) {
      try {
        subject = member.getSubject();
        group.deleteMember(member.getSubject());
        DebugLog.info(this.getClass(), "Deleted member: " + subject.getName());

      } catch (MemberDeleteException mde) {
        success = false;
        ErrorLog.error(this.getClass(), "Could not delete member: " + member.toString()
            + " -- " + mde.getMessage());
      } catch (InsufficientPrivilegeException ipe) {
        success = false;
        ErrorLog.error(this.getClass(), "Insufficent privilege for deleting member: "
            + subject.getName() + " -- " + ipe.getMessage());
      } catch (SubjectNotFoundException snfe) {
        ErrorLog.error(this.getClass(),
            "Trying to delete a member that can not be found: " + snfe.getMessage());
      }
    } else {
      ErrorLog.error(this.getClass(), "Attempting to delete a null member.");
    }
    return success;
  }

  /**
   * Add a member to a group.
   * 
   * @param group
   *          the group to which a new member is to be added
   * @param subject
   *          the subject to be added as a member of the group
   * @return true if member was added else false which include not added because member
   *         already exists.
   */
  public boolean addMember(Group group, Subject subject) {
    boolean added = false;
    if (subject == null) {
      ErrorLog.error(this.getClass(), "Trying to add a member with a null subject.");
    } else if (group != null) {
      try {
        group.addMember(subject);
        added = true;
      } catch (MemberAddException mae) {
        added = false;
        ErrorLog.error(this.getClass(), "Member not added for subject: "
            + subject.getName() + "\n" + mae.getMessage());
      } catch (InsufficientPrivilegeException ipe) {
        added = false;
        ErrorLog.error(this.getClass(), "Member not added for subject: "
            + subject.getName() + "\n" + ipe.getMessage());
      }
    } else {
      ErrorLog.error(this.getClass(), "Error attempting to to add a member, "
          + subject.toString() + ", to an non-existent group.");
    }
    return added;
  }
}
