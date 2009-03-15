/**
 * 
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;


/**
 * Use this class to copy a stem to another stem.
 * 
 * @author shilen
 * $Id: StemCopy.java,v 1.1 2009-03-15 23:13:50 shilen Exp $
 */
public class StemCopy {

  private Stem stemToCopy;

  private Stem destinationStem;

  private boolean privilegesOfStem = false;
  
  private boolean privilegesOfGroup = false;

  private boolean groupAsPrivilege = false;

  private boolean listMembersOfGroup = false;

  private boolean listGroupAsMember = false;

  private boolean attributes = false;

  /**
   * Create a new instance of this class if you would like to specify
   * specific options for a stem copy.  After setting the options,
   * call save().
   * @param stemToCopy the stem to copy
   * @param destinationStem the destination stem for the copy
   */
  public StemCopy(Stem stemToCopy, Stem destinationStem) {
    this.stemToCopy = stemToCopy;
    this.destinationStem = destinationStem;
  }
  
  /**
   * Whether to copy privileges of stems.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyPrivilegesOfStem(boolean value) {
    this.privilegesOfStem = value;
    return this;
  }

  /**
   * Whether to copy privileges of groups.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyPrivilegesOfGroup(boolean value) {
    this.privilegesOfGroup = value;
    return this;
  }

  /**
   * Whether to copy privileges where groups are a member.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyGroupAsPrivilege(boolean value) {
    this.groupAsPrivilege = value;
    return this;
  }

  /**
   * Whether to copy the list memberships of groups.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyListMembersOfGroup(boolean value) {
    this.listMembersOfGroup = value;
    return this;
  }

  /**
   * Whether to copy list memberships where groups are a member.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyListGroupAsMember(boolean value) {
    this.listGroupAsMember = value;
    return this;
  }

  /**
   * Whether to copy attributes.  Default is false.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyAttributes(boolean value) {
    this.attributes = value;
    return this;
  }

  /**
   * Copy the stem using the options set in this class.
   * @return Stem the new stem
   * @throws StemAddException 
   * @throws InsufficientPrivilegeException 
   */
  public Stem save() throws StemAddException, InsufficientPrivilegeException {

    GrouperSession.validate(GrouperSession.staticGrouperSession());

    return stemToCopy.internal_copy(destinationStem, privilegesOfStem, privilegesOfGroup,
        groupAsPrivilege, listMembersOfGroup, listGroupAsMember, attributes);
  }
}
