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
 * $Id: StemCopy.java,v 1.2 2009-03-29 21:17:21 shilen Exp $
 */
public class StemCopy {

  private Stem stemToCopy;

  private Stem destinationStem;

  private boolean privilegesOfStem = true;
  
  private boolean privilegesOfGroup = true;

  private boolean groupAsPrivilege = true;

  private boolean listMembersOfGroup = true;

  private boolean listGroupAsMember = true;

  private boolean attributes = true;

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
   * Whether to copy privileges of stems.  Default is true.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyPrivilegesOfStem(boolean value) {
    this.privilegesOfStem = value;
    return this;
  }

  /**
   * Whether to copy privileges of groups.  Default is true.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyPrivilegesOfGroup(boolean value) {
    this.privilegesOfGroup = value;
    return this;
  }

  /**
   * Whether to copy privileges where groups are a member.  Default is true.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyGroupAsPrivilege(boolean value) {
    this.groupAsPrivilege = value;
    return this;
  }

  /**
   * Whether to copy the list memberships of groups.  Default is true.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyListMembersOfGroup(boolean value) {
    this.listMembersOfGroup = value;
    return this;
  }

  /**
   * Whether to copy list memberships where groups are a member.  Default is true.
   * @param value
   * @return StemCopy
   */
  public StemCopy copyListGroupAsMember(boolean value) {
    this.listGroupAsMember = value;
    return this;
  }

  /**
   * Whether to copy attributes.  Default is true.
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
