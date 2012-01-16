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
 * $Id: StemMove.java,v 1.3 2009-03-29 21:17:21 shilen Exp $
 */
public class StemMove {

  private Stem stemToMove;

  private Stem destinationStem;

  private boolean assignAlternateName = true;

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
   * Whether to add the current names of the affected stems and groups to their alternate name lists.
   * Certain operations like group name queries (GroupFinder.findByName()) will find 
   * groups by their current and alternate names.  Currently, Grouper only supports one
   * alternate name per group or stem, so if they are moved/renamed multiple times, only the last name
   * will be kept as an alternate name.  Default is true.
   * @param value
   * @return StemMove
   */
  public StemMove assignAlternateName(boolean value) {
    this.assignAlternateName = value;
    return this;
  }

  /**
   * Move the stem using the options set in this class.
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void save() throws StemModifyException, InsufficientPrivilegeException {

    stemToMove.internal_move(destinationStem, assignAlternateName);
  }
}
