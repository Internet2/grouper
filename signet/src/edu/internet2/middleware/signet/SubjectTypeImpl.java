/*--
 $Id: SubjectTypeImpl.java,v 1.4 2005-05-12 22:04:35 acohen Exp $
 $Date: 2005-05-12 22:04:35 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.lang.reflect.Constructor;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectTypeAdapter;

/* These are the columns in the SubjectType table in the database:
 * 
 * subjectTypeID
 * name
 * adapterClass
 * createDatetime
 * createDbAccount
 * createUserID
 * createContext
 * modifyDatetime
 * modifyDbAccount
 * modifyUserID
 * modifyContext
 * comment
 */
/* Hibernate requires this class to be non-final. */

class SubjectTypeImpl
  extends EntityImpl
  implements SubjectType
{
  private SubjectTypeAdapter adapter;

  public SubjectTypeImpl()
  {
    super();
  }

  public SubjectTypeImpl(Signet signet, String id, String name,
      SubjectTypeAdapter adapter)
  {
    super(signet, id, name, Status.ACTIVE);
    this.adapter = adapter;

    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }
  }

  void setSignet(Signet signet)
  {
    super.setSignet(signet);

    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.SubjectType#getAdapter()
   */
  public SubjectTypeAdapter getAdapter()
  {
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }

    return this.adapter;
  }

  void setAdapter(SubjectTypeAdapter adapter)
  {
    this.adapter = adapter;

    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.getId();
  }

  void setAdapterClassName(String name)
  {
    Class clazz = null;

    try
    {
      clazz = Class.forName(name);
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new SignetRuntimeException(
          "A PrivilegedSubject in the Signet database refers to a Subject"
              + " whose SubjectTypeAdapter is implemented by the class named '"
              + name + "'. This class cannot be found in Signet's classpath.",
          cnfe);
    }

    try
    {
      this.adapter = (SubjectTypeAdapter) (clazz.newInstance());
    }
    catch (Exception e)
    {
      throw new SignetRuntimeException
        ("A PrivilegedSubject in the Signet database refers to a Subject"
         + " whose SubjectTypeAdapter is implemented by the class named '"
         + name
         + "'. This class is in Signet's classpath, but Signet did not"
         + " succeed in invoking its default constructor.",
         e);
    }

    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }
  }

  String getAdapterClassName()
  {
    return this.adapter.getClass().getName();
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof SubjectTypeImpl))
    {
      return false;
    }

    SubjectTypeImpl rhs = (SubjectTypeImpl) obj;
    return new EqualsBuilder().append(this.getId(), rhs.getId()).isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(this.getId()).toHashCode();
  }
}