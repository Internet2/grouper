package edu.internet2.middleware.grouper.util;

public enum GrouperProxyType {

  PROXY_HTTP {

    @Override
    public String getScheme() {
      return "http";
    }
    
  }, 
  
  PROXY_SOCKS5 {

    @Override
    public String getScheme() {
      return "socks";
    }
    
  }, 
  
  PROXY_STREAM {

    @Override
    public String getScheme() {
      return "stream";
    }
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProxyType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProxyType.class, 
        string, exceptionOnNull);

  }

  /**
   * return the scheme of this type
   * @return the scheme
   */
  public abstract String getScheme();
  
}
