/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 *
 */
public class MssqlExample {

  /**
   * @param args
   */
  @SuppressWarnings("unused")
  public static void main(String[] args) throws Exception {
    Driver driver = (Driver)Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
    Connection connection = DriverManager.getConnection("jdbc:sqlserver://localhost:3280", "grouper", "*******");
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select * from test_table");
    while (resultSet.next()) {
        String col = resultSet.getString("test1");
//        System.out.println(col);
    }
    statement.close();
    connection.close();


  }

}
