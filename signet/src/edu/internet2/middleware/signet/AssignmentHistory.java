/*--
 $Id: AssignmentHistory.java,v 1.7 2005-12-02 18:36:53 acohen Exp $
 $Date: 2005-12-02 18:36:53 $

 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Set;

import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface AssignmentHistory extends History
{
  /**
   * Returns the scope of the (@link Assignment} described by this historical
   * record, at the time this record was created.
   * 
   * @return the scope of the (@link Assignment} described by this historical
   * record, at the time this record was created.
   */
  public TreeNode getScope();
  
  
  /**
   * Returns the <code>Function</code> associated with the {@link Assignment}
   * described by this historical record, at the time this record was created.
   * 
   * @return the <code>Function</code> associated with the {@link Assignment}
   * described by this historical record, at the time this record was created.
   */
  public Function getFunction();
  
  
  /**
   * Returns the grantability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the grantability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public boolean canGrant();

  
  /**
   * Returns the useability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the useability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public boolean canUse();
  
  
  /**
   * Returns the {@link LimitValue}s of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the {@link LimitValue}s of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public Set getLimitValues();
}