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

package edu.internet2.middleware.ldappc;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

/**
 * Class for finding subjects.
 * 
 * @author Gil Singer
 */
public class StemProcessor {

  /**
   * Stem delimiter.
   */
  static final String STEM_DELIMITER = ":";

  /**
   * Group session.
   */
  private GrouperSession grouperSession;

  /**
   * the root stem.
   */
  private Stem rootStem;

  /**
   * Constructor: Creates and deletes stems and adds and deletes groups.
   * 
   * @param grouperSession
   *          the grouper session.
   */
  public StemProcessor(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
    // Find root stem.
    rootStem = StemFinder.findRootStem(this.grouperSession);
    if (grouperSession == null) {
      ErrorLog
          .fatal(this.getClass(), "In StemProcessor addStem, grouperSession is NULL.");
    }
  }

  /**
   * Add a stem to the root stem.
   * 
   * @param stemExtension
   *          the extension of the stem to be added to the root stem
   * @param stemDisplayExtension
   *          the display extension of the stem to be added to the root stem
   * @return true if operation is successful
   */
  public Stem addStem(String stemExtension, String stemDisplayExtension) {
    Stem stem = addStem(rootStem, stemExtension, stemDisplayExtension);
    return stem;
  }

  /**
   * Add a stem to another stem.
   * 
   * @param stemBase
   *          the stem to which a new stem is to be added
   * @param stemExtension
   *          the extension of the stem to be added
   * @param stemDisplayExtension
   *          the extension display of the stem to be added
   * @return true if operation is successful
   */
  public Stem addStem(Stem stemBase, String stemExtension, String stemDisplayExtension) {
    Stem stem = null;
    if (stemBase != null) {
      try {
        String stemBaseExtension = stemBase.getExtension();
        stem = StemFinder.findByName(grouperSession, stemBaseExtension + STEM_DELIMITER
            + stemExtension, true);
        // If found, use the one that already exists; assume not an
        // error.
        ErrorLog.warn(this.getClass(), "DEBUG: Stem not added as it already exists: "
            + stem.getName());
      } catch (StemNotFoundException snfe) {
        // Normal case
        try {
          stem = stemBase.addChildStem(stemExtension, stemDisplayExtension);
        } catch (StemAddException sae) {
          ErrorLog.error(this.getClass(), "Stem not added: " + sae.getMessage());
        } catch (InsufficientPrivilegeException ipe) {
          ErrorLog.error(this.getClass(), "Stem not added: " + ipe.getMessage());
        }
      }
    } else {
      ErrorLog.error(this.getClass(),
          "Error attempting to to add a stem to an non-existent stem.");
    }
    return stem;
  }

  /**
   * Delete a stem by name.
   * 
   * @param stemName
   *          the name of the stem to delete
   * @param ignoreNotFound
   *          if true, do not log as an error if not found
   * @return true if operation is successful
   */
  public boolean deleteStemByName(String stemName, boolean ignoreNotFound) {
    Stem stem = null;
    boolean success = true;
    try {
      stem = StemFinder.findByName(grouperSession, stemName, true);
      success = deleteStem(stem);
    } catch (StemNotFoundException snfe) {
      if (!ignoreNotFound) {
        ErrorLog.error(this.getClass(), "Could not delete stem named " + stemName + "; "
            + snfe.getMessage());
        success = false;
      }
    }
    return success;
  }

  /**
   * Delete a stem.
   * 
   * @param stem
   *          the stem to delete
   * @return true if operation is successful
   */
  public boolean deleteStem(Stem stem) {
    boolean success = true;
    //
    // Delete the stem
    //

    if (stem != null) {
      try {
        stem.delete();
      } catch (StemDeleteException sde) {
        success = false;
        ErrorLog.error(this.getClass(), "Could not delete stem: " + stem.getName()
            + " -- " + sde.getMessage());
      } catch (InsufficientPrivilegeException ipe) {
        success = false;
        ErrorLog.error(this.getClass(), "Insufficent privilege for deleting stem: "
            + stem.getName() + " -- " + ipe.getMessage());
      }
    } else {
      ErrorLog.error(this.getClass(), "Attempting to delete a null stem.");
    }
    return success;
  }

  /**
   * Add a group to a stem.
   * 
   * @param stemBase
   *          the stem to which a new group is to be added
   * @param groupExtension
   *          the extension of the group to be added
   * @param groupDisplayExtension
   *          the extension display of the group to be added
   * @return the added group
   */
  public Group addGroup(Stem stemBase, String groupExtension, String groupDisplayExtension) {
    Group group = null;
    if (stemBase != null) {
      try {
        String stemBaseExtension = stemBase.getExtension();
        group = GroupFinder.findByName(grouperSession, stemBaseExtension + STEM_DELIMITER
            + groupExtension, true);
        // If found, use the one that already exists; assume not an
        // error.
      } catch (GroupNotFoundException snfe) {
        // Normal Case
        try {
          group = stemBase.addChildGroup(groupExtension, groupDisplayExtension);
        } catch (GroupAddException sae) {
          ErrorLog.error(this.getClass(), "Group not added: " + sae.getMessage());
        } catch (InsufficientPrivilegeException ipe) {
          ErrorLog.error(this.getClass(), "Group not added: " + ipe.getMessage());
        }
      }

    } else {
      ErrorLog.error(this.getClass(), "Error attempting to to add a group, "
          + groupExtension + ", to an non-existent stem.");
    }
    return group;
  }

  /**
   * Delete a group.
   * 
   * @param group
   *          the group to delete
   * @return true if operation is successful
   */
  public boolean deleteGroup(Group group) {
    boolean success = true;
    //
    // Delete the group
    //

    if (group != null) {
      try {
        group.delete();
      } catch (GroupDeleteException gde) {
        success = false;
        ErrorLog.error(this.getClass(), "Could not delete group: " + group.getName()
            + " -- " + gde.getMessage());
      } catch (InsufficientPrivilegeException ipe) {
        success = false;
        ErrorLog.error(this.getClass(), "Insufficent privilege for deleting group: "
            + group.getName() + " -- " + ipe.getMessage());
      }
    } else {
      ErrorLog.error(this.getClass(), "Attempting to delete a null group.");

    }
    return success;
  }

  /**
   * Delete a group by name.
   * 
   * @param groupName
   *          the name of the group to delete
   * @param ignoreNotFound
   *          if true, do not log as an error if not found
   * @return true if operation is successful
   */
  public boolean deleteGroupByName(String groupName, boolean ignoreNotFound) {
    Group group = null;
    boolean success = true;
    try {
      group = GroupFinder.findByName(grouperSession, groupName, true);
      success = deleteGroup(group);
    } catch (GroupNotFoundException gnfe) {
      if (!ignoreNotFound) {
        ErrorLog.error(this.getClass(), "Could not delete group named " + groupName
            + "; " + gnfe.getMessage());
        success = false;
      }
    }
    return success;
  }

  /**
   * Determine if a stem exists.
   * 
   * @param grouperSession
   *          the grouper session
   * @param stemName
   *          name to checked for existence
   * @return true if stem exists
   */
  public static boolean doesStemExist(GrouperSession grouperSession, String stemName) {
    boolean exists = false;

    if (stemName != null) {
      try {
        StemFinder.findByName(grouperSession, stemName, true);
        exists = true;
      } catch (StemNotFoundException sde) {
        exists = false;
      }
    }
    return exists;
  }
}
