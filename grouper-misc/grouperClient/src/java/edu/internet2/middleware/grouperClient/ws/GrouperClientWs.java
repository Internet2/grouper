/*
 * @author mchyzer
 * $Id: GrouperClientWs.java,v 1.11 2009-11-17 06:25:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.internet2.middleware.grouperClient.GrouperClientWsException;
import edu.internet2.middleware.grouperClient.discovery.DiscoveryClient;
import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientXstreamUtils;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.CompactWriter;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpStatus;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * this is the client that all requests go through.  if you add an instance field, make sure to add to copyFrom()
 */
public class GrouperClientWs {
  
  /**
   * 
   */
  private XStream xStream;
  
  /**
   * 
   */
  private PostMethod method;
  
  /** */
  private String response;
  
  /**
   * 
   */
  private boolean success = false;

  /**
   * 
   */
  private String resultCode = null;
  
  /**
   * content type
   */
  private String contentType = null;
  
  /**
   * copy from the argument to this object
   * @param grouperClientWs
   */
  public void copyFrom(GrouperClientWs grouperClientWs) {
    this.contentType = grouperClientWs.contentType;
    this.method = grouperClientWs.method;
    this.response = grouperClientWs.response;
    this.resultCode = grouperClientWs.resultCode;
    this.success = grouperClientWs.success;
    this.xStream = grouperClientWs.xStream;
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientWs.class);

  /**
   * assign the content type, defaults to xml
   * @param theContentType
   * @return this for chaining
   */
  public GrouperClientWs assignContentType(String theContentType) {
    this.contentType = theContentType;
    return this;
  }

  /**
   * 
   */
  public GrouperClientWs() {
    this.xStream = GrouperClientXstreamUtils.retrieveXstream();
  }
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentRequest = null;
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentResponse = null;

  /** when was the failover client last configured */
  private static Long lastFailoverConfigure = null;
  
  /** how often should we reconfigure the failover client */
  private static Integer configureEverySeconds = null;
  
  /** cache this so we know if we need to reconfigure */
  private static File lastDiscoveryConfigFile = null;

  /**
   * configure the failover client every so often
   */
  private static void configureFailoverClient() {
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (debugLog != null) {
      debugLog.put("method", "GrouperClientWs.configureFailoverClient");
    }
    
    //see if we know how often to check for new config
    if (configureEverySeconds == null) {
      
      //configure every x/5 (at least 20 seconds)
      int cacheForSeconds = GrouperClientUtils.propertiesValueInt("grouperClient.cacheDiscoveryPropertiesForSeconds", 120, false);
      configureEverySeconds = cacheForSeconds / 5;
      if (configureEverySeconds < 20) {
        configureEverySeconds = 20;
      }
    }
    
    //if the amount of time since the last configure is greater than the max, then reconfigure
    boolean needsReconfigure = needsReconfigure();
    
    if (debugLog != null) {
      debugLog.put("needsReconfigure", needsReconfigure);
    }
    
    if (needsReconfigure) {

      synchronized (GrouperClientWs.class) {
        
        if (needsReconfigure()) {
          
          //see if the discovery file has changed...
          String fileName = "grouper.client.discovery.properties";
          String directoryName = GrouperClientUtils.propertiesValue("grouperClient.discoveryGrouperClientPropertiesDirectory", false);
          if (!GrouperClientUtils.isBlank(directoryName)) {
            directoryName = GrouperClientUtils.stripLastSlashIfExists(directoryName);
            fileName = directoryName + "/" + fileName;
          }
          File discoveryFile = DiscoveryClient.retrieveFile(fileName, false);
          
          if (discoveryFile == null) {
            
            if (debugLog != null) {
              debugLog.put("discoveryFile", "not found");
            }

            //if we have reconfigured before, we dont need to do this again
            if (lastFailoverConfigure != null) {
              needsReconfigure = false;
            }
            
            LOG.error("Cant find discovery file: '" + fileName + "'!!!!!!!");
          } else {

            if (debugLog != null) {
              debugLog.put("discoveryFile", discoveryFile.getAbsolutePath());
            }
          
            //see if the same as before
            if (lastDiscoveryConfigFile != null && lastDiscoveryConfigFile.equals(discoveryFile)) {
              needsReconfigure = false;
            }
          }
          
          if (debugLog != null) {
            debugLog.put("needsReconfigureFile", needsReconfigure);
          }
          
          if (needsReconfigure) {
            
            //register the failover client
            FailoverConfig failoverConfig = new FailoverConfig();
            
            //lets get the defaults
            
            {
              boolean foundOne = false;
              //grouperClient.discoveryDefault.webService.readWrite.0.url = 
              List<String> readWriteUrls = new ArrayList<String>();
              for (int i=0;i<100;i++) {
                String readWriteUrl = GrouperClientUtils.propertiesValue("grouperClient.discoveryDefault.webService.readWrite." + i + ".url", false);
                if (GrouperClientUtils.isBlank(readWriteUrl)) {
                  break;
                }
                foundOne = true;
                if (!GrouperClientUtils.isBlank(readWriteUrl)) {
                  readWriteUrls.add(readWriteUrl);
                }
              }
              if (foundOne) {
                failoverConfig.setConnectionNames(readWriteUrls);
              }
            }
            
            
            {
              boolean foundOne = false;
              //grouperClient.discoveryDefault.webService.readOnly.0.url = 
              List<String> readOnlyUrls = new ArrayList<String>();
              for (int i=0;i<100;i++) {
                String readOnlyUrl = GrouperClientUtils.propertiesValue("grouperClient.discoveryDefault.webService.readOnly." + i + ".url", false);
                if (GrouperClientUtils.isBlank(readOnlyUrl)) {
                  break;
                }
                foundOne = true;
                if (!GrouperClientUtils.isBlank(readOnlyUrl)) {
                  readOnlyUrls.add(readOnlyUrl);
                }
              }
              if (foundOne) {
                failoverConfig.setConnectionNamesSecondTier(readOnlyUrls);
              }
            }            
            
            //grouperClient.discoveryDefault.webService.loadBalancing = active/active
            FailoverStrategy failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                GrouperClientUtils.propertiesValue("grouperClient.discoveryDefault.webService.loadBalancing", false), false);
            if (failoverStrategy != null) {
              failoverConfig.setFailoverStrategy(failoverStrategy);
            }
            
            //grouperClient.discoveryDefault.webService.preferReadWrite = true
            boolean preferReadWrite = true;
            preferReadWrite = GrouperClientUtils.propertiesValueBoolean("grouperClient.discoveryDefault.webService.preferReadWrite", preferReadWrite, false);
            //TODO deal with this
            
            //grouperClient.discoveryDefault.webService.affinitySeconds = 28800
            int affinitySeconds = failoverConfig.getAffinitySeconds();
            affinitySeconds = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryDefault.webService.affinitySeconds", affinitySeconds, false);
            failoverConfig.setAffinitySeconds(affinitySeconds);
            
            //grouperClient.discoveryDefault.webService.lowerConnectionPriorityOnErrorForMinutes = 180
            int lowerConnectionPriorityOnErrorForMinutes = failoverConfig.getMinutesToKeepErrors();
            lowerConnectionPriorityOnErrorForMinutes = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryDefault.webService.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes, false);
            failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
            
            //grouperClient.discoveryDefault.webService.timeoutSeconds = 60
            int timeoutSeconds = failoverConfig.getTimeoutSeconds();
            timeoutSeconds = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryDefault.webService.timeoutSeconds", timeoutSeconds, false);
            failoverConfig.setTimeoutSeconds(timeoutSeconds);
            
            //grouperClient.discoveryDefault.webService.extraTimeoutSeconds = 30
            int extraTimeoutSeconds = failoverConfig.getExtraTimeoutSeconds();
            extraTimeoutSeconds = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryDefault.webService.extraTimeoutSeconds", extraTimeoutSeconds, false);
            failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
            
            //if there is a discovery file, then use it
            if (discoveryFile != null) {
              Properties properties = GrouperClientUtils.propertiesFromFile(discoveryFile);
              
              {
                boolean foundOne = false;
                //grouperClient.discovery.webService.readWrite.0.url = 
                List<String> readWriteUrls = new ArrayList<String>();
                for (int i=0;i<100;i++) {
                  String readWriteUrl = GrouperClientUtils.propertiesValue(properties, "grouperClient.discovery.webService.readWrite." + i + ".url");
                  if (GrouperClientUtils.isBlank(readWriteUrl)) {
                    break;
                  }
                  foundOne = true;
                  if (!GrouperClientUtils.isBlank(readWriteUrl)) {
                    readWriteUrls.add(readWriteUrl);
                  }
                }
                if (foundOne) {
                  failoverConfig.setConnectionNames(readWriteUrls);
                }
              }
              
              {
                boolean foundOne = false;
                //grouperClient.discovery.webService.readOnly.0.url = 
                List<String> readOnlyUrls = new ArrayList<String>();
                for (int i=0;i<100;i++) {
                  String readOnlyUrl = GrouperClientUtils.propertiesValue(properties, "grouperClient.discovery.webService.readOnly." + i + ".url");
                  if (GrouperClientUtils.isBlank(readOnlyUrl)) {
                    break;
                  }
                  foundOne = true;
                  if (!GrouperClientUtils.isBlank(readOnlyUrl)) {
                    readOnlyUrls.add(readOnlyUrl);
                  }
                }
                if (foundOne) {
                  failoverConfig.setConnectionNamesSecondTier(readOnlyUrls);
                }
              }            
              
              //grouperClient.discovery.webService.loadBalancing = active/active
              failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                  GrouperClientUtils.propertiesValue(properties, "grouperClient.discovery.webService.loadBalancing"), false);
              if (failoverStrategy != null) {
                failoverConfig.setFailoverStrategy(failoverStrategy);
              }
              
              //grouperClient.discovery.webService.preferReadWrite = true
              preferReadWrite = GrouperClientUtils.propertiesValueBoolean(properties, "grouperClient.discovery.webService.preferReadWrite", preferReadWrite);
              
              //grouperClient.discovery.webService.affinitySeconds = 28800
              affinitySeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.webService.affinitySeconds", affinitySeconds);
              failoverConfig.setAffinitySeconds(affinitySeconds);
              
              //grouperClient.discovery.webService.lowerConnectionPriorityOnErrorForMinutes = 3
              lowerConnectionPriorityOnErrorForMinutes = GrouperClientUtils.propertiesValueInt(properties, 
                  null, "grouperClient.discovery.webService.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes);
              failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
              
              //grouperClient.discovery.webService.timeoutSeconds = 60
              timeoutSeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.webService.timeoutSeconds", timeoutSeconds);
              failoverConfig.setTimeoutSeconds(timeoutSeconds);
              
              //grouperClient.discovery.webService.extraTimeoutSeconds = 30
              extraTimeoutSeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.webService.extraTimeoutSeconds", extraTimeoutSeconds);
              failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
              
            }
            
            {
              boolean foundOne = false;
              //#grouperClient.discoveryOverride.webService.readWrite.0.url = 
              List<String> readWriteUrls = new ArrayList<String>();
              for (int i=0;i<100;i++) {
                String readWriteUrl = GrouperClientUtils.propertiesValue("grouperClient.discoveryOverride.webService.readWrite." + i + ".url", false);
                if (GrouperClientUtils.isBlank(readWriteUrl)) {
                  break;
                }
                foundOne = true;
                if (!GrouperClientUtils.isBlank(readWriteUrl)) {
                  readWriteUrls.add(readWriteUrl);
                }
              }
              if (foundOne) {
                failoverConfig.setConnectionNames(readWriteUrls);
              }
            }
            
            
            {
              boolean foundOne = false;
              //#grouperClient.discoveryOverride.webService.readOnly.0.url = 
              List<String> readOnlyUrls = new ArrayList<String>();
              for (int i=0;i<100;i++) {
                String readOnlyUrl = GrouperClientUtils.propertiesValue("grouperClient.discoveryOverride.webService.readOnly." + i + ".url", false);
                if (GrouperClientUtils.isBlank(readOnlyUrl)) {
                  break;
                }
                foundOne = true;
                if (!GrouperClientUtils.isBlank(readOnlyUrl)) {
                  readOnlyUrls.add(readOnlyUrl);
                }
              }
              if (foundOne) {
                failoverConfig.setConnectionNamesSecondTier(readOnlyUrls);
              }
            }            
            
            //#grouperClient.discoveryOverride.webService.loadBalancing = active/active
            failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                GrouperClientUtils.propertiesValue("grouperClient.discoveryOverride.webService.loadBalancing", false), false);
            if (failoverStrategy != null) {
              failoverConfig.setFailoverStrategy(failoverStrategy);
            }
            
            //#grouperClient.discoveryOverride.webService.preferReadWrite = true
            preferReadWrite = GrouperClientUtils.propertiesValueBoolean("grouperClient.discoveryOverride.webService.preferReadWrite", preferReadWrite, false);
            
            //#grouperClient.discoveryOverride.webService.affinitySeconds = 28800
            affinitySeconds = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryOverride.webService.affinitySeconds", affinitySeconds, false);
            failoverConfig.setAffinitySeconds(affinitySeconds);
            
            //#grouperClient.discoveryOverride.webService.lowerConnectionPriorityOnErrorForMinutes = 180
            lowerConnectionPriorityOnErrorForMinutes = GrouperClientUtils.propertiesValueInt("grouperClient.discoveryOverride.webService.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes, false);
            failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
            
            //#grouperClient.discoveryOverride.webService.timeoutSeconds = 60
            timeoutSeconds = GrouperClientUtils.propertiesValueInt(
                "grouperClient.discoveryOverride.webService.timeoutSeconds", timeoutSeconds, false);
            failoverConfig.setTimeoutSeconds(timeoutSeconds);
            
            //#grouperClient.discoveryOverride.webService.extraTimeoutSeconds = 30
            extraTimeoutSeconds = GrouperClientUtils.propertiesValueInt(
                "grouperClient.discoveryOverride.webService.extraTimeoutSeconds", extraTimeoutSeconds, false);
            failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
            
            {
              FailoverConfig failoverConfigReadWrite = new FailoverConfig();
              failoverConfigReadWrite.copyFromArgument(failoverConfig);
              //we dont want the second tier since no readonly urls
              failoverConfigReadWrite.setConnectionNamesSecondTier(null);
              //if there are no urls, then add the default one
              if (GrouperClientUtils.length(failoverConfigReadWrite.getConnectionNames()) == 0) {
                failoverConfigReadWrite.setConnectionNames(GrouperClientUtils.toList(
                    GrouperClientUtils.propertiesValue("grouperClient.webService.url", true)));
              }
              failoverConfigReadWrite.setConnectionType(READ_WRITE_FAILOVER_CONFIG_NAME);
              FailoverClient.initFailoverClient(failoverConfigReadWrite);
            }            
            
            {
              FailoverConfig failoverConfigReadOnly = new FailoverConfig();
              failoverConfigReadOnly.copyFromArgument(failoverConfig);
              if (!preferReadWrite && GrouperClientUtils.length(failoverConfig.getConnectionNamesSecondTier()) > 0) {

                //if not prefer readwrite, then add those to the first tier, and remove from second tier
                if (failoverConfigReadOnly.getConnectionNames() == null) {
                  failoverConfigReadOnly.setConnectionNames(new ArrayList<String>());
                }
                failoverConfigReadOnly.getConnectionNames().addAll(failoverConfigReadOnly.getConnectionNamesSecondTier());
                failoverConfigReadOnly.setConnectionNamesSecondTier(null);
              }

              if (GrouperClientUtils.length(failoverConfigReadOnly.getConnectionNames()) == 0
                  && GrouperClientUtils.length(failoverConfigReadOnly.getConnectionNamesSecondTier()) == 0) {
                failoverConfigReadOnly.setConnectionNames(GrouperClientUtils.toList(
                    GrouperClientUtils.propertiesValue("grouperClient.webService.url", true)));
              }

              failoverConfigReadOnly.setConnectionType(READ_ONLY_FAILOVER_CONFIG_NAME);
              FailoverClient.initFailoverClient(failoverConfigReadOnly);
            }            
            
          }
        }
      }
    }
    
  }

  /** readonly failover config name */
  public static final String READ_ONLY_FAILOVER_CONFIG_NAME = "grouperWsReadOnly";
  
  /** readwrite failover config name */
  public static final String READ_WRITE_FAILOVER_CONFIG_NAME = "grouperWsReadWrite";
  
  /**
   * see if needs reconfigure
   * @return true or false
   */
  private static boolean needsReconfigure() {
    boolean needsReconfigure = lastFailoverConfigure == null || (System.currentTimeMillis() - lastFailoverConfigure) / 1000 > configureEverySeconds;
    if (!DiscoveryClient.hasDiscovery() && lastFailoverConfigure != null) {
      needsReconfigure = false;
    }
    return needsReconfigure;
  }

  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param readOnly true if readonly, false if readwrite
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  public Object executeService(final String urlSuffix, final Object toSend, 
      final String labelForLog, final String clientVersion, final boolean readOnly)  {
    
    //configure the failover client (every 30 seconds)
    configureFailoverClient();
    
    String url = GrouperClientUtils.propertiesValue("grouperClient.webService.url", true);
    
    //copy the grouper client ws instance to this
    
    String connectionType = readOnly ? READ_ONLY_FAILOVER_CONFIG_NAME : READ_WRITE_FAILOVER_CONFIG_NAME;
    
    return FailoverClient.failoverLogic(connectionType, new FailoverLogic<Object>() {

      @Override
      public Object logic(FailoverLogicBean failoverLogicBean) {
        
        //if not last connection then throw exception if not success.  If last connection then return the object
        return executeServiceHelper(failoverLogicBean.getConnectionName(), 
            urlSuffix, toSend, labelForLog, clientVersion, !failoverLogicBean.isLastConnection());
      }
    });
    
  }

  /**
   * @param url to hit, could be multiple
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param exceptionOnNonSuccess if non success should exception be thrown
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  private Object executeServiceHelper(String url, String urlSuffix, Object toSend, String labelForLog, String clientVersion, boolean exceptionOnNonSuccess)  {
    
    String logDir = GrouperClientUtils.propertiesValue("grouperClient.logging.webService.documentDir", false);
    File requestFile = null;
    File responseFile = null;
    
    if (!GrouperClientUtils.isBlank(logDir)) {
      
      logDir = GrouperClientUtils.stripEnd(logDir, "/");
      logDir = GrouperClientUtils.stripEnd(logDir, "\\");
      Date date = new Date();
      String logName = logDir  + File.separator + "wsLog_" 
        + new SimpleDateFormat("yyyy_MM").format(date)
        + File.separator + "day_" 
        + new SimpleDateFormat("dd" + File.separator + "HH_mm_ss_SSS").format(date)
        + "_" + ((int)(1000 * Math.random())) + "_" + labelForLog;
      
      requestFile = new File(logName + "_request.log");
      
      responseFile = new File(logName + "_response.log");

      //make parents
      GrouperClientUtils.mkdirs(requestFile.getParentFile());
      
    }
    int[] responseCode = new int[1];
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    this.method = postMethod(url, this.xStream, urlSuffix, toSend, requestFile, responseCode, clientVersion);

    //make sure a request came back
    Header successHeader = this.method.getResponseHeader("X-Grouper-success");
    String successString = successHeader == null ? null : successHeader.getValue();
    if (GrouperClientUtils.isBlank(successString)) {
      if (LOG.isDebugEnabled()) {
        String theResponse = null;
        try {
          theResponse = GrouperClientUtils.responseBodyAsString(this.method);
        } catch (Exception e) {
          //ignore
        }
        LOG.debug("Response: " + theResponse);
      }
      throw new RuntimeException("Web service did not even respond! " + webServiceUrl(url, urlSuffix));
    }
    this.success = "T".equals(successString);
    this.resultCode = this.method.getResponseHeader("X-Grouper-resultCode").getValue();
    
    this.response = GrouperClientUtils.responseBodyAsString(this.method);

    mostRecentResponse = this.response;

    if (responseFile != null || GrouperClientLog.debugToConsole()) {
      if (responseFile != null) {
        LOG.debug("WebService: logging response to: " + GrouperClientUtils.fileCanonicalPath(responseFile));
      }
      
      String theResponse = this.response;
      Exception indentException = null;

      boolean isIndent = GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true);
      if (isIndent) {
        try {
          theResponse = GrouperClientUtils.indent(theResponse, true);
        } catch (Exception e) {
          indentException = e;
        }
      }
      
      StringBuilder headers = new StringBuilder();

      headers.append("HTTP/1.1 ").append(responseCode[0]).append(" ").append(HttpStatus.getStatusText(responseCode[0])).append("\n");
      
      for (Header header : this.method.getResponseHeaders()) {
        String name = header.getName();
        String value = header.getValue();
        
        //dont allow cookies to go to logs
        if (GrouperClientUtils.equals(name, "Set-Cookie")) {
          value = value.replaceAll("JSESSIONID=(.*)?;", "JSESSIONID=xxxxxxxxxxxx;");
        }
        headers.append(name).append(": ").append(value).append("\n");
      }
      headers.append("\n");
      String theResponseTotal = headers + theResponse;
      if (responseFile != null) {
        GrouperClientUtils.saveStringIntoFile(responseFile, theResponseTotal);
      }
      if (GrouperClientLog.debugToConsole()) {
        System.err.println("\n################ RESPONSE START " + (isIndent ? "(indented) " : "") + "###############\n");
        System.err.println(theResponseTotal);
        System.err.println("\n################ RESPONSE END ###############\n\n");
      }
      if (indentException != null) {
        throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
            "grouper.client.properties: grouperClient.logging.webService.indent", indentException);
      }
    }

    Object resultObject = toSend instanceof String ? this.response : this.xStream.fromXML(this.response);
    
    //see if problem
    if (resultObject instanceof WsRestResultProblem) {
      throw new GrouperClientWsException(resultObject, ((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
    }

    if (exceptionOnNonSuccess && !this.success) {
      throw new GrouperClientWsException(resultObject, "Result code: " + this.resultCode + ", on url: " + url );
    }
    
    return resultObject;
  }

  /**
   * if failure, handle it
   * @param responseContainer is the object that everything marshaled to
   * @param resultMetadataHolders
   * @param resultMessage
   * @throws GcWebServiceError if there is a problem
   */
  public void handleFailure(Object responseContainer, ResultMetadataHolder[] resultMetadataHolders, String resultMessage) {
    // see if request worked or not
    if (!this.success) {
      StringBuilder error = new StringBuilder("Bad response from web service: resultCode: " + this.resultCode
        + ", " + resultMessage);
      int errorIndex = 0;
      for (int i=0;i<GrouperClientUtils.length(resultMetadataHolders);i++) {
        try {
          WsResultMeta resultMetadata = resultMetadataHolders[i].getResultMetadata();
          if (!GrouperClientUtils.equals(resultMetadata.getSuccess(), "T")) {
            error.append("\nError ").append(errorIndex).append(", result index: ").append(i).append(", code: ").append(resultMetadata.getResultCode())
              .append(", message: ").append(resultMetadata.getResultMessage());
            errorIndex++;
          }
        } catch (Exception e) {
          //object not there
          LOG.debug("issue with error message: ", e);
        }
      }
      throw new GcWebServiceError(responseContainer, error.toString());
    }

  }
  
  
  /**
   * http client
   * @return the http client
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  private static HttpClient httpClient() {
    
    //see if invalid SSL
    String httpsSocketFactoryName = GrouperClientUtils.propertiesValue("grouperClient.https.customSocketFactory", false);
    
    //is there overhead here?  should only do this once?
    //perhaps give a custom factory
    if (!GrouperClientUtils.isBlank(httpsSocketFactoryName)) {
      Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = GrouperClientUtils.forName(httpsSocketFactoryName);
      SecureProtocolSocketFactory httpsSocketFactoryInstance = GrouperClientUtils.newInstance(httpsSocketFactoryClass);
      Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
      Protocol.registerProtocol("https", easyhttps);
    }
    
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = GrouperClientUtils.propertiesValueInt(
        "grouperClient.webService.httpSocketTimeoutMillis", 90000, true);
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
    int connectionManagerMillis = GrouperClientUtils.propertiesValueInt(
        "grouperClient.webService.httpConnectionManagerTimeoutMillis", 90000, true);
    
    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

    String userLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
    String user = GrouperClientUtils.propertiesValue("grouperClient.webService." + userLabel, true);
    
    LOG.debug("WebService: connecting as user: '" + user + "'");
    
    boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
        "encrypt.disableExternalFileLookup", false, true);
    
    //lets lookup if file
    String wsPass = GrouperClientUtils.propertiesValue("grouperClient.webService.password", true);
    String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

    String passPrefix = null;

    if (!GrouperClientUtils.equals(wsPass, wsPassFromFile)) {

      passPrefix = "WebService pass: reading encrypted value from file: " + wsPass;

      String encryptKey = GrouperClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    } else {
      passPrefix = "WebService pass: reading scalar value from grouper.client.properties";
    }
    
    if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.logMaskedPassword", false, false)) {
      LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", wsPass.length()));
    }

    Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);

    return httpClient;
  }

  /**
   * @param url
   * @param suffix of the url
   * @return the url
   */
  private String webServiceUrl(String url, String suffix) {
    
    //CH 20120208: I guess this isnt used since the base URL and object is used
    suffix = GrouperClientUtils.trimToEmpty(suffix);
    
    suffix = GrouperClientUtils.stripStart(suffix, "/");
    
    //String url = GrouperClientUtils.propertiesValue("grouperClient.webService.url", true);
    
    url = GrouperClientUtils.stripEnd(url, "/");
    return url;
  }

  /**
   * @param url is the url to use
   * @param suffix e.g. groups/aStem:aGroup/members
   * @param clientVersion
   * @return the method
   */
  private PostMethod postMethod(String url, String suffix, String clientVersion) {
    
    url = webServiceUrl(url, suffix);
    
    String webServiceVersion = GrouperClientUtils.propertiesValue("grouperClient.webService.client.version", true);

    if (!GrouperClientUtils.isBlank(clientVersion)) {
      webServiceVersion = clientVersion;
    }
    
    webServiceVersion = GrouperClientUtils.stripStart(webServiceVersion, "/");
    webServiceVersion = GrouperClientUtils.stripEnd(webServiceVersion, "/");

    url = url + "/" + webServiceVersion + "/" + suffix;

    LOG.debug("WebService: connecting to URL: '" + url + "'");

    //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    PostMethod postMethod = new PostMethod(url);

    //no keep alive so response is easier to indent for tests
    postMethod.setRequestHeader("Connection", "close");
    
    return postMethod;
  }

  /**
   * @param url to use
   * @param theXstream
   * @param urlSuffix to put on end of base url, e.g. groups/aStem:aGroup/members
   * @param objectToMarshall is the bean to convert to XML, or it could be a string of xml
   * @param logFile if not null, log the contents of the request there
   * @param responseCode array of size one to get the response code back
   * @param clientVersion 
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private PostMethod postMethod(String url, XStream theXstream, 
      String urlSuffix, Object objectToMarshall, File logFile, int[] responseCode, String clientVersion)  {
    
    try {
      String theContentType = GrouperClientUtils.defaultIfBlank(this.contentType, "text/xml");
      
      HttpClient httpClient = httpClient();
  
      PostMethod postMethod = postMethod(url, urlSuffix, clientVersion);
  
      String requestDocument = objectToMarshall instanceof String ? (String)objectToMarshall : marshalObject(theXstream, objectToMarshall);
      
      postMethod.setRequestEntity(new StringRequestEntity(requestDocument, theContentType, "UTF-8"));
      
      if (logFile != null || GrouperClientLog.debugToConsole()) {
        if (logFile != null) {
          LOG.debug("WebService: logging request to: " + GrouperClientUtils.fileCanonicalPath(logFile));
        }
        String theRequestDocument = requestDocument;
        Exception indentException = null;
        boolean isIndent = GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true);
        if (isIndent) {
          try {
            theRequestDocument = GrouperClientUtils.indent(theRequestDocument, true);
          } catch (Exception e) {
            indentException = e;
          }
        }
  
        StringBuilder headers = new StringBuilder();
  //      POST /grouper-ws/servicesRest/v1_4_000/subjects HTTP/1.1
  //      Connection: close
  //      Authorization: Basic bWNoeXplcjpEaxxxxxxxxxx==
  //      User-Agent: Jakarta Commons-HttpClient/3.1
  //      Host: localhost:8090
  //      Content-Length: 226
  //      Content-Type: text/xml; charset=UTF-8
        headers.append("POST ").append(postMethod.getURI().getPathQuery()).append(" HTTP/1.1\n");
        headers.append("Connection: close\n");
        headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
        headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
        headers.append("Host: ").append(postMethod.getURI().getHost()).append(":")
          .append(postMethod.getURI().getPort()).append("\n");
        headers.append("Content-Length: ").append(
            postMethod.getRequestEntity().getContentLength()).append("\n");
        headers.append("Content-Type: ").append(
            postMethod.getRequestEntity().getContentType()).append("\n");
        headers.append("\n");
        
        String theRequest = headers + theRequestDocument;
        if (logFile != null) {
          GrouperClientUtils.saveStringIntoFile(logFile, theRequest);
        }
        if (GrouperClientLog.debugToConsole()) {
          System.err.println("\n################ REQUEST START " + (isIndent ? "(indented) " : "") + "###############\n");
          System.err.println(theRequest);
          System.err.println("\n################ REQUEST END ###############\n\n");
        }
        if (indentException != null) {
          throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
          		"grouper.client.properties: grouperClient.logging.webService.indent", indentException);
        }
      }
      
      mostRecentRequest = requestDocument;
      
      int responseCodeInt = httpClient.executeMethod(postMethod);
  
      if (responseCode != null && responseCode.length > 0) {
        responseCode[0] = responseCodeInt;
      }
      
      return postMethod;
    } catch (Exception e) {
      
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      
      throw new RuntimeException("Problem in url: " + url, e);
    }
  }
  
  /**
   * 
   * @param xStream
   * @param object
   * @return the xml
   */
  private static String marshalObject(XStream xStream, Object object) {
    StringWriter stringWriter = new StringWriter();
    //dont indent
    xStream.marshal(object, new CompactWriter(stringWriter));

    String requestDocument = stringWriter.toString();
    return requestDocument;
  }
  


}