/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.lang.reflect.*;


/** 
 * Implementation of the I2MI {{@link SubjectType}} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectTypeImpl.java,v 1.1 2004-11-12 04:25:41 blair Exp $
 */
public class SubjectTypeImpl implements SubjectType {

  // Class reference to the appropriate adapter
  // private static SubjectTypeAdapter   sta;
  // Has adapter been initialized?
  // private static boolean              initialized = false;

  // What we need to identify a subject type
  private String adapterClass;
  private String name;
  private String typeID;


  /**
   * Create a {@link SubjectTypeImpl} object.
   */
  public SubjectTypeImpl() {
    super();
    this._init();
  }


  /*
   * PUBLIC INSTANCE METHODS 
   */

  /**
   * Return an instanc eof the {@link SubjectTypeAdapter} class for
   * this {@link SubjectTYpe}.
   *
   * @return {@link SubjectTypeAdapter} object.
   */
  public SubjectTypeAdapter adapterClass() {
    if (this.getAdapterClass() != null) {
      // Attempt to reflectively create an instance of the
      // appropriate subject type adapter
      try {
        Class classType     = Class.forName( this.getAdapterClass() );
        Class[] paramsClass = new Class[] { };
        Constructor con     = classType.getDeclaredConstructor(paramsClass);
        Object[] params     = new Object[] { };
        return (SubjectTypeAdapter) con.newInstance(params);
      } catch (Exception e) {
        // TODO Well, this is blatantly the wrong thing to do
        System.err.println(e);
        System.exit(1);
      }
    }
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouper.SubjectTypeImpl#adapterClass()
   */
  public SubjectTypeAdapter getAdapter() {
    return this.adapterClass();
  }

  /**
   * @see edu.internet2.middleware.grouper.SubjectTypeImpl#typeID()
   */
  public String getId() {
    return this.typeID();
  }

  /**
   * Return subject type name.
   *
   * @return  Name of type.
   */
  public String name() {
    return this.getName();
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.typeID()              + ":" +
           this.name()                + ":" +
           this.adapterClass();
  }

  /**
   * Return subject type ID.
   *
   * @return ID of type.
   */
  public String typeID() {
    return this.getSubjectTypeID();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.adapterClass = null;
    this.typeID       = null;
    this.name         = null;
  }


  /*
   * HIBERNATE
   */

  private String getAdapterClass() {
    return this.adapterClass;
  }

  private void setAdapterClass(String adapterClass) {
    this.adapterClass = adapterClass;
  }

  private String getSubjectTypeID() {
    return this.typeID;
  }

  private void setSubjectTypeID(String id) {
    this.typeID = id;
  }

  /*
   * Public due to `getName' being specified in the `SubjectType'
   * interface.
   */
  public String getName() {
    return this.name;
  }

  private void setName(String name) {
    this.name = name;
  }

}

