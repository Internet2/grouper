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
 * Implementation of the I2MI {@link SubjectType} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectTypeImpl.java,v 1.6 2004-11-23 17:08:37 blair Exp $
 */
public class SubjectTypeImpl implements SubjectType {

  // What we need to identify a subject type
  private String adapterClass;
  private String name;
  private String typeID;


  /**
   * Create a {@link SubjectTypeImpl} object.
   */
  public SubjectTypeImpl() {
    super();
  }


  /*
   * PUBLIC INSTANCE METHODS 
   */

  /**
   * Return an instance of the {@link SubjectTypeAdapter} class for
   * this {@link SubjectType}.
   *
   * @return {@link SubjectTypeAdapter} object.
   */
  public SubjectTypeAdapter getAdapter() {
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
   * Return subject type ID.
   *
   * @return ID of type.
   */

  public String getId() {
    return this.getSubjectTypeID();
  }

  /**
   * Return subject type name.
   *
   * @return  Name of type.
   */
  public String getName() {
    return this.name;
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.getId()               + ":" +
           this.getName()             + ":" +
           this.getAdapter();
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
   *
   * private String getName() { ... }
   */

  private void setName(String name) {
    this.name = name;
  }

}

