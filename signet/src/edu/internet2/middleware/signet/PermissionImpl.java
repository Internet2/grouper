/*--
 $Id: PermissionImpl.java,v 1.6 2005-03-03 18:29:00 acohen Exp $
 $Date: 2005-03-03 18:29:00 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
 * PermissionImpl describes an application-level action that a {@link Subject}
 * may be allowed to perform.
 * 
 */
/* Hibernate requires this class to be non-final. */

class PermissionImpl
extends EntityImpl
implements Permission
{
  private Subsystem	subsystem;
  private Set				functions;
  private Set				limits;
  
  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public PermissionImpl()
  {
    super();
    this.functions = new HashSet();
    this.limits = new HashSet();
  }
  
  /**
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   */
  PermissionImpl
    (SubsystemImpl 	subsystem,
     String 				id,
     Status					status)
  {
    super(subsystem.getSignet(), id, null, status);
    this.setSubsystem(subsystem);
    this.functions = new HashSet();
    this.limits = new HashSet();
  }
  
  /**
   * @return Returns the {@link Function}s associated with this Permission.
   */
  public Function[] getFunctionsArray()
  {
      Function[] functionsArray = new Function[0];
      
      return (Function[])(this.functions.toArray(functionsArray));
  }
  
  public Limit[] getLimitsArray()
  {
      Comparator displayOrderComparator
      	= new Comparator()
      	    {
              public int compare(Object o1, Object o2)
              {
                return
                	((Limit)o1).getDisplayOrder() - ((Limit)o2).getDisplayOrder();
              }
      	    };

      Set limits = this.getLimits();
      Limit[] limitArray = new Limit[0];
      limitArray = (Limit[])(limits.toArray(limitArray));
      	    
      Arrays.sort(limitArray, displayOrderComparator);
      return limitArray;
  }
  
  /**
   * @param functions The Functions to associate with this Permission.
   */
  public void setFunctionsArray(Function[] functions)
  {
    int functionCount = (functions == null ? 0 : functions.length);
    this.functions = new HashSet(functionCount);
    
    for (int i = 0; i < functionCount; i++)
    {
      this.functions.add(functions[i]);
    }
  }
  
  /* This method exists only for use by Hibernate. */
  void setFunctions(Set functions)
  {
    this.functions = functions;
  }
  
  /* This method exists only for use by Hibernate.
   */
  Set getFunctions()
  {
    return this.functions;
  }
  
  /* This method exists only for use by Hibernate. */
  void setLimits(Set limits)
  {
    this.limits = limits;
  }
  
  /* This method exists only for use by Hibernate.
   */
  Set getLimits()
  {
    return this.limits;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Permission#addFunction(edu.internet2.middleware.signet.Function)
   */
  public void addFunction(Function function)
  {
    // Do we have this Function already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Functions to each other.
    
    if (!(this.functions.contains(function)))
    {
      this.functions.add(function);
      function.addPermission(this);
    }
  }

  public void addLimit(Limit limit)
  {
    // Do we have this Limit already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Limits to each other.
    
    if (!(this.limits.contains(limit)))
    {
      this.limits.add(limit);
      ((LimitImpl)limit).add(this);
    }
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#getSubsystem()
   */
  public Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#setSubsystem(edu.internet2.middleware.signet.Subsystem)
   */
  public void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
}