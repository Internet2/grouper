package edu.internet2.middleware.grouper.app.ldapToSql;


public class LdapToSqlSyncColumn {

  public LdapToSqlSyncColumn() {
  }

  private String ldapName;
  
  private String sqlColumn;
  
  private boolean uniqueKey;
  
  private String translation;

  
  public String getLdapName() {
    return ldapName;
  }

  
  public void setLdapName(String ldapName) {
    this.ldapName = ldapName;
  }

  
  public String getSqlColumn() {
    return sqlColumn;
  }

  
  public void setSqlColumn(String sqlColumn) {
    this.sqlColumn = sqlColumn;
  }

  
  public boolean isUniqueKey() {
    return uniqueKey;
  }

  
  public void setUniqueKey(boolean uniqueKey) {
    this.uniqueKey = uniqueKey;
  }

  
  public String getTranslation() {
    return translation;
  }

  
  public void setTranslation(String translation) {
    this.translation = translation;
  }
  

  
}
