/*
 * @author mchyzer
 * $Id: Hib3IdentifierGenerator.java,v 1.1.2.1 2008-03-19 18:46:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;


/**
 *
 */
public class Hib3IdentifierGenerator implements IdentifierGenerator {

  /**
   * @see org.hibernate.id.IdentifierGenerator#generate(org.hibernate.engine.SessionImplementor, java.lang.Object)
   */
  public Serializable generate(SessionImplementor session, Object object)
      throws HibernateException {
    return GrouperUuid.getUuid();
  }

}
