package edu.internet2.middleware.grouper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperProxyBean {

  /**
   * match a url
   */
  static Pattern urlPattern = Pattern.compile("^([^:]+)://([^:/]+):?([0-9]*)/?$");

  /**
   * domain name or ip address to connect to
   */
  private String hostname;
  
  /**
   * tcp port number to connect to
   */
  private int port;
  
  /**
   * scheme e.g. http or https
   */
  private String scheme;

  /**
   * scheme e.g. http or https
   * @return the scheme
   */
  public String getScheme() {
    // prime the pump
    this.getHostname();
    return this.scheme;
  }
  
  /**
   * numeric port
   * @return the port
   */
  public int getPort() {
    // prime the pump
    this.getHostname();
    return this.port;    
  }
  
  /**
   * domain name or ip address to connect to, cached
   * @return the hostname
   */
  public String getHostname() {
    if (StringUtils.isBlank(this.hostname)) {
      if (!StringUtils.isBlank(this.proxyUrl)) {
        Matcher matcher = GrouperProxyBean.urlPattern.matcher(this.proxyUrl);
        if (!matcher.matches()) {
          throw new RuntimeException("Not expecting URL '" + this.proxyUrl + "', expecting something like http://some.host or https://some.other.host:1234");
        } else {
          this.scheme = matcher.group(1);
          this.hostname = matcher.group(2);
          String portString = matcher.group(3);
          if (!StringUtils.isBlank(portString)) {
            this.port = GrouperUtil.intValue(portString);
          } else {
            if (StringUtils.equalsIgnoreCase("http", this.scheme)) {
              this.port = 80;
            } else if (StringUtils.equalsIgnoreCase("https", this.scheme)) {
              this.port = 443;
            } else {
              // TODO is there a default socks port?
              throw new RuntimeException("Not expecting URL '" + this.proxyUrl + "', expecting something like http://some.host or https://some.other.host:1234");
            }
          }
        }
      }
    }
    return this.hostname;
  }
  

  /**
   * 
   * @param grouperProxyType
   * @param proxyDomainName
   * @param proxyPort
   * @param proxySsl
   * @param targetUrl
   * @return the bean for proxy or null if not proxying
   */
  public static GrouperProxyBean proxyConfig(GrouperProxyType proxyGrouperProxyType, String proxyUrl, String targetUrl) {

    if (proxyGrouperProxyType == null != StringUtils.isBlank(proxyUrl)) {
      throw new RuntimeException("If you have a proxy URL or a proxy type then you need the other one: " 
          + proxyGrouperProxyType + ", " + proxyUrl);
    }

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    String grouperProxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.http.proxy.url");

    if (StringUtils.isBlank(grouperProxyUrl) && StringUtils.isBlank(proxyUrl)) {
      return null;
    }

    if (!StringUtils.isBlank(proxyUrl)) {
      GrouperProxyBean proxyGrouperProxyBean = null;
      proxyGrouperProxyBean = new GrouperProxyBean();
      proxyGrouperProxyBean.setGrouperProxyType(proxyGrouperProxyType);
      proxyGrouperProxyBean.setProxyUrl(proxyUrl);
      
      // if this is there it overrides
      return proxyGrouperProxyBean;
    }

    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    String grouperProxyTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouper.http.proxy.type");

    GrouperProxyType grouperProxyType = StringUtils.isBlank(grouperProxyTypeString) ? null 
        : GrouperProxyType.valueOfIgnoreCase(grouperProxyTypeString, false);

    GrouperProxyBean grouperProxyBean = null;
    if (!StringUtils.isBlank(grouperProxyUrl)) {
      grouperProxyBean = new GrouperProxyBean();
      grouperProxyBean.setGrouperProxyType(grouperProxyType);
      grouperProxyBean.setProxyUrl(grouperProxyUrl);

      //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
      //  # {valueType: "string"}
      //  grouper.http.proxy.excludeUrlRegexPattern =
      String grouperProxyExcludeRegex = GrouperConfig.retrieveConfig().propertyValueString("grouper.http.proxy.excludeUrlRegexPattern");
      
      // first see if excluded
      if (!StringUtils.isBlank(grouperProxyExcludeRegex)) {
        Pattern pattern = Pattern.compile(grouperProxyExcludeRegex);
        Matcher matcher = pattern.matcher(targetUrl);
        if (matcher.matches()) {
          return null;
        }
      }

      // check the regexes
      //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
      //  # {valueType: "string"}
      //  grouper.http.proxy.includeUrlRegexPattern = 
      String grouperProxyIncludeRegex = GrouperConfig.retrieveConfig().propertyValueString("grouper.http.proxy.includeUrlRegexPattern");

      // then see if not included
      if (!StringUtils.isBlank(grouperProxyIncludeRegex)) {
        Pattern pattern = Pattern.compile(grouperProxyIncludeRegex);
        Matcher matcher = pattern.matcher(targetUrl);
        if (!matcher.matches()) {
          return null;
        }
      }
    }

    // see if there is one or the other
    return grouperProxyBean;
  }
  

  /**
   * type of proxy e.g. http or socks
   */
  private GrouperProxyType grouperProxyType;
  
  /**
   * proxy url
   */
  private String proxyUrl;

  /**
   * type of proxy e.g. http or socks
   * @return
   */
  public GrouperProxyType getGrouperProxyType() {
    return grouperProxyType;
  }

  /**
   * type of proxy e.g. http or socks
   * @param grouperProxyType
   */
  public void setGrouperProxyType(GrouperProxyType grouperProxyType) {
    this.grouperProxyType = grouperProxyType;
  }

  /**
   * proxy url
   * @return
   */
  public String getProxyUrl() {
    return proxyUrl;
  }

  /**
   * proxy url
   * @param proxyUrl
   */
  public void setProxyUrl(String proxyUrl) {
    this.proxyUrl = proxyUrl;
    this.hostname = null;
    this.port = -1;
    this.scheme = null;
  }

  /**
   * create a proxy url based on type, host, and port
   * @param grouperProxyType2
   * @param proxyHost
   * @param proxyPort
   * @return
   */
  public static String proxyUrl(String proxyTypeString, String proxyHost,
      Integer proxyPort) {
    GrouperProxyType grouperProxyType = GrouperProxyType.valueOfIgnoreCase(proxyTypeString, false);
    if (grouperProxyType == null != StringUtils.isBlank(proxyHost)) {
      throw new RuntimeException("If you set a proxy host then you must set a proxy type and vise versa: " + grouperProxyType + ", '" + proxyHost + "'");
    }
    if (grouperProxyType == null) {
      return null;
    }
    if (proxyPort == null) {
      return grouperProxyType.getScheme() + "://" + proxyHost;
    }
    return grouperProxyType.getScheme() + "://" + proxyHost + ":" + proxyPort;
    
  }
  
  
  
}
