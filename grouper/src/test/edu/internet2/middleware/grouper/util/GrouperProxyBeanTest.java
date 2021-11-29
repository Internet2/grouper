package edu.internet2.middleware.grouper.util;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperProxyBeanTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProxyBeanTest("testProxyConfigurationGrouperIncludeExcludeNo"));
  }
  
  public GrouperProxyBeanTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperExcludeNo() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.excludeUrlRegexPattern", "^.*yahoo.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNull(grouperProxyBean);
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperExcludeYes() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.excludeUrlRegexPattern", "^.*google.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("https://something.com", grouperProxyBean.getProxyUrl());
    assertEquals("https", grouperProxyBean.getScheme());
    assertEquals(443, grouperProxyBean.getPort());
    assertEquals("something.com", grouperProxyBean.getHostname());
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperIncludeExcludeYes() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.includeUrlRegexPattern", "^.*yahoo.*$");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.excludeUrlRegexPattern", "^.*google.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("https://something.com", grouperProxyBean.getProxyUrl());
    assertEquals("https", grouperProxyBean.getScheme());
    assertEquals(443, grouperProxyBean.getPort());
    assertEquals("something.com", grouperProxyBean.getHostname());
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperIncludeExcludeNo() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.includeUrlRegexPattern", "^.*yahoo.*$");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.excludeUrlRegexPattern", "^.*aho.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNull(grouperProxyBean);
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperIncludeYes() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.includeUrlRegexPattern", "^.*yahoo.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("https://something.com", grouperProxyBean.getProxyUrl());
    assertEquals("https", grouperProxyBean.getScheme());
    assertEquals(443, grouperProxyBean.getPort());
    assertEquals("something.com", grouperProxyBean.getHostname());
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperIncludeNo() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.includeUrlRegexPattern", "^.*google.*$");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNull(grouperProxyBean);
  }

  /**
   * 
   */
  public void testProxyConfigurationNone() {
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNull(grouperProxyBean);
    
  }

  /**
   * 
   */
  public void testProxyConfigurationGrouperOnly() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "https://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(null, null, "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("https://something.com", grouperProxyBean.getProxyUrl());
    assertEquals("https", grouperProxyBean.getScheme());
    assertEquals(443, grouperProxyBean.getPort());
    assertEquals("something.com", grouperProxyBean.getHostname());
  }

  /**
   * 
   */
  public void testProxyConfigurationOverrideOnly() {
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(GrouperProxyType.PROXY_HTTP, "http://whatever.com", "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("http://whatever.com", grouperProxyBean.getProxyUrl());
    assertEquals("http", grouperProxyBean.getScheme());
    assertEquals(80, grouperProxyBean.getPort());
    assertEquals("whatever.com", grouperProxyBean.getHostname());
  }

  /**
   * 
   */
  public void testProxyConfigurationOverrideGrouper() {

    //  # proxy requests here, e.g. https://server:1234
    //  # {valueType: "string"}
    //  grouper.http.proxy.url =
    //
    //  # socks or http
    //  # {valueType: "string", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
    //  grouper.http.proxy.type = 
    //
    //  # if this is blank then all urls are included by default.  If there is a regex here, then only include urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.includeUrlRegexPattern = 
    //
    //
    //  # if this is blank then excludes are not considered by default.  If there is a regex here, then only exclude urls that match, e.g. ^abc$
    //  # {valueType: "string"}
    //  grouper.http.proxy.excludeUrlRegexPattern =

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.url", "http://something.com");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.http.proxy.type", "PROXY_HTTP");
    
    GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(GrouperProxyType.PROXY_HTTP, "http://whatever.com", "http://yahoo.com");
    assertNotNull(grouperProxyBean);
    assertEquals(GrouperProxyType.PROXY_HTTP, grouperProxyBean.getGrouperProxyType());
    assertEquals("http://whatever.com", grouperProxyBean.getProxyUrl());
    assertEquals("http", grouperProxyBean.getScheme());
    assertEquals(80, grouperProxyBean.getPort());
    assertEquals("whatever.com", grouperProxyBean.getHostname());
  }

  
  public void testRegex() {

    Matcher matcher = GrouperProxyBean.urlPattern.matcher("http://www.yahoo.com");
    assertTrue(matcher.matches());
    assertEquals("http", matcher.group(1));
    assertEquals("www.yahoo.com", matcher.group(2));
    assertTrue(StringUtils.isBlank(matcher.group(3)));
    
    matcher = GrouperProxyBean.urlPattern.matcher("https://www.yahoo.com:832");
    assertTrue(matcher.matches());
    assertEquals("https", matcher.group(1));
    assertEquals("www.yahoo.com", matcher.group(2));
    assertEquals("832", matcher.group(3));

    matcher = GrouperProxyBean.urlPattern.matcher("http://www.yahoo.com/");
    assertTrue(matcher.matches());
    assertEquals("http", matcher.group(1));
    assertEquals("www.yahoo.com", matcher.group(2));
    assertTrue(StringUtils.isBlank(matcher.group(3)));
    
    matcher = GrouperProxyBean.urlPattern.matcher("https://www.yahoo.com:832/");
    assertTrue(matcher.matches());
    assertEquals("https", matcher.group(1));
    assertEquals("www.yahoo.com", matcher.group(2));
    assertEquals("832", matcher.group(3));
  }
  
}
