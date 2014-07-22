/**
 * Copyright 2012 Internet2
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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.SubjectUtils;



/**
 * type of database we are connecting to
 */
public enum JdbcDatabaseType {

  /** oracle db */
  oracle {

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#pageQuery(java.lang.String, int)
     */
    @Override
    public String pageQuery(String query, int pageSize) {
      
      String selectPart = selectPart(query);
      if (selectPart == null) {
        return query;
      }
      
      List<String> aliases = columnAliases(selectPart);
      
      if (aliases == null) {
        return query;
      }
      
      //lets build the new statement
      StringBuilder result = new StringBuilder("select ");
      
      for (int i=0;i<aliases.size();i++) {
        result.append(aliases.get(i));
        if (i < (aliases.size()-1)) {
          result.append(", ");
        }
      }
      result.append(" from (").append(query).append(") where rownum <= ").append(pageSize);
      return result.toString();
    }


    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlDefinitely(java.lang.String)
     */
    @Override
    public boolean matchesUrlDefinitely(String url) {
      return url != null && url.toLowerCase().startsWith("jdbc:oracle:");
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlMaybe(java.lang.String)
     */
    @Override
    public boolean matchesUrlMaybe(String url) {
      return url != null && url.toLowerCase().contains("oracle");
    }

  },
  
  /** mysql db */
  mysql {

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#pageQuery(java.lang.String, int)
     */
    @Override
    public String pageQuery(String query, int pageSize) {
      return query + " limit 0," + pageSize;//
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlDefinitely(java.lang.String)
     */
    @Override
    public boolean matchesUrlDefinitely(String url) {
      return url != null && url.toLowerCase().startsWith("jdbc:mysql:");
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlMaybe(java.lang.String)
     */
    @Override
    public boolean matchesUrlMaybe(String url) {
      return url != null && url.toLowerCase().contains("mysql");
    }
  },
  
  /** postgres */
  postgres {

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#pageQuery(java.lang.String, int)
     */
    @Override
    public String pageQuery(String query, int pageSize) {
      return query + " limit " + pageSize;//
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlDefinitely(java.lang.String)
     */
    @Override
    public boolean matchesUrlDefinitely(String url) {
      return url != null && url.toLowerCase().startsWith("jdbc:postgresql:");
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlMaybe(java.lang.String)
     */
    @Override
    public boolean matchesUrlMaybe(String url) {
      return url != null && url.toLowerCase().contains("postgres");
    }

  },
  
  /** hsql */
  hsqldb {

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#pageQuery(java.lang.String, int)
     */
    @Override
    public String pageQuery(String query, int pageSize) {
      return null;
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlDefinitely(java.lang.String)
     */
    @Override
    public boolean matchesUrlDefinitely(String url) {
      return url != null && url.toLowerCase().startsWith("jdbc:hsqldb:");
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#matchesUrlMaybe(java.lang.String)
     */
    @Override
    public boolean matchesUrlMaybe(String url) {
      return url != null && url.toLowerCase().contains("hsql");
    }
  };

  /**
   * return the database type for this connection or null
   * @param connection
   * @return the database type
   */
  public static JdbcDatabaseType resolveDatabaseType(Connection connection) {
    String url = null;
    
    try {
      url = connection.getMetaData().getURL();
    } catch (SQLException sqle) {
      return null;
    }
    
    for (JdbcDatabaseType jdbcDatabaseType : JdbcDatabaseType.values()) {
      if (jdbcDatabaseType.matchesUrlDefinitely(url)) {
        return jdbcDatabaseType;
      }
    }
    
    for (JdbcDatabaseType jdbcDatabaseType : JdbcDatabaseType.values()) {
      if (jdbcDatabaseType.matchesUrlMaybe(url)) {
        return jdbcDatabaseType;
      }
    }
    
    return null;
   }
  
  /** get the select part of a SQL */
  private static Pattern selectClausePattern = Pattern.compile("^(.*?\\s)(from\\s.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  /** get the select part of a SQL */
  private static Pattern lastWordPattern = Pattern.compile("^.*\\s(.*)$", Pattern.DOTALL);

  /**
   * get the column aliases from a query select a,b,c from
   * @param fromClause
   * @return the aliases
   */
  public static List<String> columnAliases(String fromClause) {
    if (fromClause == null) {
      return null;
    }
    String[] cols = SubjectUtils.splitTrim(fromClause, ",");
    List<String> result = new ArrayList<String>();
    //we need the last whole string
    String alias = null;
    for (String col : cols) {
      Matcher matcher = lastWordPattern.matcher(col);
      //see if there are spaces
      if (matcher.matches()) {
        alias = matcher.group(1);
      } else {
        alias = col;
      }
      //we dont do this...
      if (StringUtils.equals("*", alias)) {
        return null;
      }
      result.add(alias);
    }
    return result;
  }
  
  /**
   * get the select part from a query
   * @param query
   * @return the select part, everything up to the from, or null if cant find
   */
  public static String selectPart(String query) {
    
    if (query == null) {
      return null;
    }
    
    String fromAndAfter = query;
    String upToFrom = "";
    
    while (true) {
      Matcher matcher = selectClausePattern.matcher(fromAndAfter);
      
      if (!matcher.matches()) {
        return null;
      }
      
      upToFrom += matcher.group(1);
      fromAndAfter = matcher.group(2);
      
      //see if up to from is good with parens
      int leftParenCount = StringUtils.countMatches(upToFrom, "(");
      int rightParenCount = StringUtils.countMatches(upToFrom, ")");
      
      if (leftParenCount == rightParenCount) {
        return upToFrom;
      }
    }    
    
  }
  
  /**
   * change a query into a paging query
   * @param query
   * @param pageSize
   * @return the new query
   */
  public abstract String pageQuery(String query, int pageSize);
  
  /**
   * 
   * @param url
   * @return true if this is definitely this db type
   */
  public abstract boolean matchesUrlDefinitely(String url);

  /**
   * 
   * @param url
   * @return true if this is maybe this db type
   */
  public abstract boolean matchesUrlMaybe(String url);

  
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static JdbcDatabaseType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return SubjectUtils.enumValueOfIgnoreCase(JdbcDatabaseType.class, 
        string, exceptionOnNull);
  
  }
  
  
  
}
