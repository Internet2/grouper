/*--
$Id: SubsystemImpl.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;

/* Hibernate requires this class to be non-final. */

class SubsystemImpl
extends EntityImpl
implements Subsystem
{
private Signet	signet;
private String  helpText;
private Set	    categories;
private Set	    functions;
private Set			permissions;
private Tree		tree;

private Map			functionsMap;
private boolean	functionsNotYetFetched = true;
//private Map			scopesMap;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public SubsystemImpl()
  {
      super();
      this.categories = new HashSet();
      this.functions = new HashSet();
      this.permissions = new HashSet();
      
      this.functionsMap = new HashMap();
  }

  /**
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   * @param name
   *            A descriptive name which will appear in UIs and documents
   *            exposed to users.
   * @param description
   *            A prose description which will appear in help-text and other
   *            explanatory materials.
   * @param status
   * 			The {@link Status} of this Proxy.
   */
  SubsystemImpl
  	(Signet signet,
  	 String id,
  	 String name,
  	 String helpText,
  	 Status	status)
  {
      super(id, name, status);
      this.signet = signet;
      this.helpText = helpText;
      this.categories = new HashSet();
      this.functions = new HashSet();
      this.permissions = new HashSet();
      
      this.functionsMap = buildMap(this.functions);
  }

//  /**
//   * @return Returns every {@link Category} associated with this
//   *		Subsystem.
//   */
//  public Category[] getCategoriesArray()
//  {
//    Category[] categoriesArray;
//    
//    if (this.categories == null)
//    {
//      categoriesArray = new Category[0];
//    }
//    else
//    {
//      categoriesArray = new Category[this.categories.size()];
//      Iterator categoriesIterator = this.categories.iterator();
//      int i = 0;
//      while (categoriesIterator.hasNext())
//      {
//        categoriesArray[i] = (Category)(categoriesIterator.next());
//        i++;
//      }
//    }
//    
//    return categoriesArray;
//  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Subsystem#setCategories(java.util.Set)
   */
//  public void setCategoriesArray(Category[] categories)
//  {
//    int categoryCount = (categories == null ? 0 : categories.length);
//    this.categories = new HashSet(categoryCount);
//      
//    for (int i = 0; i < categoryCount; i++)
//    {
//      this.categories.add(categories[i]);
//    }
//  }
  
  /* This method exists only for use by Hibernate. */
  public void setCategories(Set categories)
  {
    this.categories = categories;
  }

//  /**
//   * @return Returns every {@link Function} associated with this
//   *		Subsystem.
//   */
//  public Function[] getFunctionsArray()
//  {
//    Function[] functionsArray;
//    
//    if (this.functions == null)
//    {
//      functionsArray = new Function[0];
//    }
//    else
//    {
//      functionsArray = new Function[this.functions.size()];
//      Iterator functionsIterator = this.functions.iterator();
//      int i = 0;
//      while (functionsIterator.hasNext())
//      {
//        functionsArray[i] = (Function)(functionsIterator.next());
//        i++;
//      }
//    }
//    
//    return functionsArray;
//  }

  public void setFunctionsArray(Function[] functions)
  {
    int functionCount = (functions == null ? 0 : functions.length);
    this.functions = new HashSet(functionCount);
      
    for (int i = 0; i < functionCount; i++)
    {
      this.functions.add(functions[i]);
    }
    
    this.functionsMap = buildMap(this.functions);
  }
  
  /* This method exists only for use by Hibernate. */
  public void setFunctions(Set functions)
  {
    this.functions = functions;
    this.functionsMap = buildMap(this.functions);
  }


/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object o)
{
  if ( !(o instanceof SubsystemImpl) )
  {
    return false;
  }
  
  SubsystemImpl rhs = (SubsystemImpl) o;
  return new EqualsBuilder()
                  .append(this.getId(), rhs.getId())
                  .isEquals();
}


/* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */   
public int hashCode()
{
  // you pick a hard-coded, randomly chosen, non-zero, odd number
  // ideally different for each class
  return new HashCodeBuilder(17, 37).   
     append(this.getId()).
     toHashCode();
 }

  /**
   * @param helpText A prose description which will appear in help-text and
   * 		other explanatory materials.
   */
  public void setHelpText(String helpText)
  {
      this.helpText = helpText;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getHelpText()
   */
  public String getHelpText()
  {
      return this.helpText;
  }

  /**
   * @return Returns the {@link Tree} associated
   * 		with this Subsystem.
   */
  public Tree getTree()
  {
    if (tree instanceof TreeImpl)
    {
      ((TreeImpl)tree).setSignet(this.signet);
    }
    
    return this.tree;
  }

  public void setTree(Tree tree)
  {
    if (tree instanceof TreeImpl)
    {
      ((TreeImpl)tree).setSignet(this.signet);
    }

    this.tree = tree;
  }

  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getCategories()
  {
    return this.categories;
    // return UnmodifiableSet.decorate(this.categories);
  }
  
  /**
   * @return Returns the Functions associated with this Subsystem.
   * @throws ObjectNotFoundException
   */
  public Set getFunctions()
  {
    // I really want to handle this purely through Hibernate mappings, but
    // I haven't figured out how yet.
    
    if (this.functionsNotYetFetched == true)
    {
        // We have not yet fetched the Functions associated with this
        // Subsystem from the database. Let's make a copy of
        // whatever in-memory Functions we DO have, because they represent
        // defined-but-not-necessarily-yet-persisted Functions.
        Set unsavedFunctions = this.functions;
        
        this.functions
        	= this.signet
        			.getFunctionsBySubsystem(this);

        this.functions.addAll(unsavedFunctions);
        
        this.functionsMap = buildMap(this.functions);
        
        this.functionsNotYetFetched = false;
    }
    
    Set resultSet = UnmodifiableSet.decorate(this.functions);

    return resultSet;
  }

//  /* (non-Javadoc)
//   * @see edu.internet2.middleware.signet.Subsystem#setCategories(java.util.Set)
//   */
//  public void setScopesArray(Scope[] scopesArray)
//  {
//    int scopeCount = (scopesArray == null ? 0 : scopesArray.length);
//    this.trees = new HashSet(scopeCount);
//      
//    for (int i = 0; i < scopeCount; i++)
//    {
//      this.trees.add(scopesArray[i]);
//    }
//    
////    this.scopesMap = buildMap(this.trees);
//  }
  
  private Map buildMap(Set set)
  {
    Map 			map = new HashMap(set.size());
    Iterator 	iterator = set.iterator();
    Class[] 	noParams = new Class[0];
    String 		id;
    
    while (iterator.hasNext())
    {
      Object obj = iterator.next();
      
      try
      {
        Method getIdMethod
        	= obj.getClass().getMethod("getId", noParams);
        id = (String)(getIdMethod.invoke(obj, noParams));
      }
      catch (Exception e)
      {
        throw new SignetRuntimeException
        	("Failed to execute 'getID()' method of object '"
        	 + obj
        	 + "' in class '"
        	 + obj.getClass()
        	 + "'.",
        	 e);
      }
      
      map.put(id, obj);
    }
    
    return map;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Subsystem#getFunction(java.lang.String)
   */
  public Function getFunction(String functionId)
  throws ObjectNotFoundException
  {
    // First, let's make sure the Functions are loaded from the database.
    this.getFunctions();
    
    FunctionImpl function
    	= (FunctionImpl)(this.functionsMap.get(functionId));
    
    if (function == null)
    {
      throw new ObjectNotFoundException
      	("Unable to find the Function with ID '" 
      	 + functionId + "' in the Subsystem '" + this.getId() + "'.");
    }
    
    if (this.signet != null)
    {
      function.setSignet(this.signet);
    }
    
    return function;
  }

//  public Scope getScope(TreeType treeType, String treeId)
//  {
//    return (Tree)(this.scopesMap.get(treeId));
//  }
  
  void add(Function function)
  {
    this.functions.add(function);
    this.functionsMap.put(function.getId(), function);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Subsystem#add(edu.internet2.middleware.signet.Category)
   */
  public void add(Category category)
  {
    this.categories.add(category);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((Subsystem)o).getName();
    
    return thisName.compareToIgnoreCase(otherName);
  }
  
  void setSignet(Signet signet)
  {
    this.signet = signet;
  }
}
