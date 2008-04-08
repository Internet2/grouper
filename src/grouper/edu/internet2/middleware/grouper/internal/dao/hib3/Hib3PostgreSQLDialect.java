/*
 * @author mchyzer
 * $Id: Hib3PostgreSQLDialect.java,v 1.1 2008-04-08 19:08:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;


/**
 * fix column type in postgres
 */
public class Hib3PostgreSQLDialect extends PostgreSQLDialect {

  /**
   * 
   */
  public Hib3PostgreSQLDialect() {
    
    super();
    registerColumnType( Types.BOOLEAN, "boolean" );

    
  }

}
