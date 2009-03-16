/**
 * 
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemModifyException;

/**
 * Use this class to move a stem to another stem.
 * 
 * @author shilen
 * $Id: StemMove.java,v 1.1 2009-03-16 23:22:52 shilen Exp $
 */
public class StemMove {

  private Stem stemToMove;

  private Stem destinationStem;

  private boolean assignOldName = false;

  /**
   * Create a new instance of this class if you would like to specify
   * specific options for a stem move.  After setting the options,
   * call save().
   * @param stemToMove Stem to move
   * @param destinationStem  Stem where the stem should be moved
   */
  public StemMove(Stem stemToMove, Stem destinationStem) {
    this.stemToMove = stemToMove;
    this.destinationStem = destinationStem;
  }

  /**
   * Whether to add the current name of the affected groups to the groups' old names list.  
   * Certain operations like group name queries (GroupFinder.findByName()) will find 
   * groups by their current and old names.  Currently, Grouper only supports one
   * old name per group, so if groups are moved/renamed multiple times, only the last name
   * will be kept as an old name.  Default is false.
   * @param value
   * @return StemMove
   */
  public StemMove assignOldName(boolean value) {
    this.assignOldName = value;
    return this;
  }

  /**
   * Move the stem using the options set in this class.
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void save() throws StemModifyException, InsufficientPrivilegeException {

    stemToMove.internal_move(destinationStem, assignOldName);
  }
}
