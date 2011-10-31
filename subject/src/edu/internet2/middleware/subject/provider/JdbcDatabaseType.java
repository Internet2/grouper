/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.subject.provider;

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
        if (i < aliases.size()-1) {
          result.append(", ");
        }
      }
      result.append(" from (").append(query).append(") where rownum <= ").append(pageSize);
      return result.toString();
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
      return null;
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
      return null;
    }
  },
  
  /** sqlserver */
  sqlserver {

    /**
     * 
     * @see edu.internet2.middleware.subject.provider.JdbcDatabaseType#pageQuery(java.lang.String, int)
     */
    @Override
    public String pageQuery(String query, int pageSize) {
      return null;
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
  };

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
