/*--
$Id: Tree.java,v 1.3 2005-01-12 17:28:05 acohen Exp $
$Date: 2005-01-12 17:28:05 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import java.util.Set;

/**
*
* A Tree is a root {@link TreeNode}, followed by some set of child
* {@link TreeNode}s, each of which may have its own children.
* 
*/
public interface Tree
{    
/**
 * @return Returns a short mnemonic ID which will appear in XML
 * 		documents and other documents used by analysts. This ID
 * 		uniquely identifies a single Tree.
 */
public String getId();

/**
 * 
 * @return A printable String, containing the name of this Tree in
 * 		some default displayable format. The exact details
 * 		of the representation are unspecified and subject to change.
 * @throws TreeNotFoundException
 */
public String getName()
  throws TreeNotFoundException;

public void addRoot(TreeNode rootNode);

public Set getRoots();

public Set getTreeNodes();

public TreeNode getNode(String nodeId);

public TreeAdapter getAdapter();
}
