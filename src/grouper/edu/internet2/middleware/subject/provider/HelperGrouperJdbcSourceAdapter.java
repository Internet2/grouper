/*
 * @author mchyzer
 * $Id: HelperGrouperJdbcSourceAdapter.java,v 1.4 2008-09-14 04:54:00 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;



/**
 * Some methods in subject api have package security, so this class is a temporary
 * measure to get access to them
 */
public class HelperGrouperJdbcSourceAdapter extends JDBCSourceAdapter {

  /**
   * 
   */
  public HelperGrouperJdbcSourceAdapter() {
    super();
  }

  /**
   * @param arg0
   * @param arg1
   */
  public HelperGrouperJdbcSourceAdapter(String arg0, String arg1) {
    super(arg0, arg1);
  }

}
