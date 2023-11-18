/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer $Id: DbMetadataPoc.java,v 1.2 2008-08-24 06:10:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * this is a proof of concept which shows foreign keys printed from jdbc metadata.
 * in oracle only one key is printed, that is the problem.  in mysql, both are printed
 */
public class DbMetadataPoc {

  /**
   * 
   */
  private static final String PASS = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.password");

  /**
   * 
   */
  private static final String URL = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url");

  /**
   * 
   */
  private static final String DRIVER = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(URL, 
      GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.driver_class"));

  /**
   * 
   */
  private static final String SCHEMA = GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.username");

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    printTables();

  }

  /**
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  private static void printForeignKeys() throws ClassNotFoundException, SQLException {
    Class.forName(DRIVER);
    Connection connection = DriverManager.getConnection(URL, SCHEMA, PASS);

    DatabaseMetaData databaseMetaData = connection.getMetaData();
    ResultSet fkData = null;

    try {
      //fkData = databaseMetaData.getTables(null, "public", "grouper_%", null);
      fkData = databaseMetaData.getImportedKeys(null, null, null);
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

  /**
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  private static void printTables() throws ClassNotFoundException, SQLException {
    Class.forName(DRIVER);
    Connection connection = DriverManager.getConnection(URL, SCHEMA, PASS);

    DatabaseMetaData databaseMetaData = connection.getMetaData();
    ResultSet resultSet = null;
    try {
      System.out.println("Schemas: ");
      resultSet = databaseMetaData.getSchemas();
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnCount = resultSetMetaData.getColumnCount();
      int index = 0;
      while (resultSet.next()) {
        System.out.println(index++ + ": ");

        for (int i = 1; i <= columnCount; i++) {
          System.out.println("  " + resultSetMetaData.getColumnName(i) + ": "
              + resultSet.getString(i));
        }
      }
    } finally {
      GrouperUtil.closeQuietly(resultSet);
    }
    ResultSet fkData = null;

    try {
      fkData = databaseMetaData.getTables(null, null, "GROUPER_%", null);
      ResultSetMetaData resultSetMetaData = fkData.getMetaData();
      System.out.println("Schema name: " + resultSetMetaData.getSchemaName(1));
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
