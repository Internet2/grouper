/*
 * @author mchyzer $Id: ForeignKeysPoc.java,v 1.1 2008-07-29 17:54:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * this is a proof of concept which shows foreign keys printed from jdbc metadata.
 * in oracle only one key is printed, that is the problem.  in mysql, both are printed
 */
public class ForeignKeysPoc {

  /**
   * 
   */
  private static final String DRIVER = GrouperConfig.getHibernateProperty("hibernate.connection.driver_class");

  /**
   * 
   */
  private static final String PASS = GrouperConfig.getHibernateProperty("hibernate.connection.password");

  /**
   * 
   */
  private static final String URL = GrouperConfig.getHibernateProperty("hibernate.connection.url");

  /**
   * 
   */
  private static final String SCHEMA = GrouperConfig.getHibernateProperty("hibernate.connection.username");

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    Class.forName(DRIVER);
    Connection connection = DriverManager.getConnection(URL, SCHEMA, PASS);

    DatabaseMetaData databaseMetaData = connection.getMetaData();
    ResultSet fkData = null;

    try {
      fkData = databaseMetaData.getImportedKeys(null, SCHEMA.toUpperCase(), "GROUPER_ATTRIBUTES");
      ResultSetMetaData resultSetMetaData = fkData.getMetaData();
      int columnCount = resultSetMetaData.getColumnCount();
      int fk = 0;
      while (fkData.next()) {
        System.out.println(fk++ + ": ");

        for (int i = 1; i <= columnCount; i++) {
          System.out.println("  " + resultSetMetaData.getColumnName(i) + ": "
              + fkData.getString(i));
        }
      }
    } finally {
      if (fkData != null) {
        fkData.close();
      }
      connection.close();
    }

  }

}
