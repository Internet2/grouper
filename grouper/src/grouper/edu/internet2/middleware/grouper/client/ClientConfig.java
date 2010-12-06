package edu.internet2.middleware.grouper.client;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;

/**
 * connection config information cached from grouper.properties
 * @author mchyzer
 *
 */
public class ClientConfig {

  /**
   * holds the state of one connection in the
   *
   */
  public static class ClientConnectionSourceConfigBean {
    
    /**
     * sourceId can be blank if you dont want to specify
     */
    private String localSourceId;
    
    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     */
    private String localReadSubjectId;
    
    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     */
    private String localWriteSubjectId;
    
    
    /**
     * sourceId can be blank if you dont want to specify
     */
    private String remoteSourceId;
    
    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     */
    private String remoteReadSubjectId;
    
    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     */
    private String remoteWriteSubjectId;
    
    /**
     * if subjects are external and should be created if not exist
     */
    private Boolean addExternalSubjectIfNotFound;
    
    /**
     * sourceId can be blank if you dont want to specify
     * @return sourceId
     */
    public String getLocalSourceId() {
      return this.localSourceId;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @param localSourceId1
     */
    public void setLocalSourceId(String localSourceId1) {
      this.localSourceId = localSourceId1;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @return local read subject id
     */
    public String getLocalReadSubjectId() {
      return this.localReadSubjectId;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @param localReadSubjectId1
     */
    public void setLocalReadSubjectId(String localReadSubjectId1) {
      this.localReadSubjectId = localReadSubjectId1;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     * @return identifier
     */
    public String getLocalWriteSubjectId() {
      return this.localWriteSubjectId;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     * @param localWriteSubjectId1
     */
    public void setLocalWriteSubjectId(String localWriteSubjectId1) {
      this.localWriteSubjectId = localWriteSubjectId1;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @return sourceId
     */
    public String getRemoteSourceId() {
      return this.remoteSourceId;
    }

    /**
     * sourceId can be blank if you dont want to specify
     * @param remoteSourceId1
     */
    public void setRemoteSourceId(String remoteSourceId1) {
      this.remoteSourceId = remoteSourceId1;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @return remoteReadSubjectId
     */
    public String getRemoteReadSubjectId() {
      return this.remoteReadSubjectId;
    }

    /**
     * this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
     * @param remoteReadSubjectId1
     */
    public void setRemoteReadSubjectId(String remoteReadSubjectId1) {
      this.remoteReadSubjectId = remoteReadSubjectId1;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     * @return remote write subject id
     */
    public String getRemoteWriteSubjectId() {
      return this.remoteWriteSubjectId;
    }

    /**
     * this is the identifier to lookup to add a subject, should be "id" or "identifier"
     * @param remoteWriteSubjectId1
     */
    public void setRemoteWriteSubjectId(String remoteWriteSubjectId1) {
      this.remoteWriteSubjectId = remoteWriteSubjectId1;
    }

    /**
     * if subjects are external and should be created if not exist
     * @return if add
     */
    public Boolean getAddExternalSubjectIfNotFound() {
      return this.addExternalSubjectIfNotFound;
    }

    /**
     * if subjects are external and should be created if not exist
     * @param addExternalSubjectIfNotFound1
     */
    public void setAddExternalSubjectIfNotFound(Boolean addExternalSubjectIfNotFound1) {
      this.addExternalSubjectIfNotFound = addExternalSubjectIfNotFound1;
    }

  }
  
  /**
   * holds the state of one connection in the
   *
   */
  public static class ClientConnectionConfigBean {

    /**
     * describes sources in the grouper config
     */
    private Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans = null;

    /**
     * describes sources in the grouper config
     * @return the sources
     */
    public Map<String,ClientConnectionSourceConfigBean> getClientConnectionSourceConfigBeans() {
      return this.clientConnectionSourceConfigBeans;
    }

    /**
     * describes sources in the grouper config
     * @param clientConnectionSourceConfigBeans1
     */
    public void setClientConnectionSourceConfigBeans(
        Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans1) {
      this.clientConnectionSourceConfigBeans = clientConnectionSourceConfigBeans1;
    }
    
    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     */
    private String localActAsSubject;

    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     * @return local act as subject
     */
    public String getLocalActAsSubject() {
      return this.localActAsSubject;
    }

    /**
     * this is the subject to act as local, if blank, act as GrouperSystem, specify with SubjectFinder packed string, e.g.
     * subjectIdOrIdentifier  or  sourceId::::subjectId  or  ::::subjectId  or  sourceId::::::subjectIdentifier  or  ::::::subjectIdentifier
     * sourceId::::::::subjectIdOrIdentifier  or  ::::::::subjectIdOrIdentifier
     * @param localActAsSubject1
     */
    public void setLocalActAsSubject(String localActAsSubject1) {
      this.localActAsSubject = localActAsSubject1;
    }

    
    //# the part between "grouperClient.localhost.source." and ".id" links up the configs, 
    //# in this case, "jdbc", make sure it has no special chars.  sourceId can be blank if you dont want to specify
    //grouperClient.localhost.source.jdbc.local.sourceId = jdbc
    //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
    //grouperClient.localhost.source.jdbc.local.read.subjectId = identifier
    //# this is the identifier to lookup to add a subject, should be "id" or "identifier"
    //grouperClient.localhost.source.jdbc.local.write.subjectId = identifier
    //# sourceId of the remote system, can be blank
    //grouperClient.localhost.source.jdbc.remote.sourceId = jdbc
    //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
    //grouperClient.localhost.source.jdbc.remote.read.subjectId = 
    //# this is the identifier to lookup to add a subject, should be "id" or "identifier"
    //grouperClient.localhost.source.jdbc.remote.write.subjectId = 
    //# if subjects are external and should be created if not exist
    //grouperClient.localhost.source.jdbc.addExternalSubjectIfNotFound = true

  }
  
  /** cache this so if file changes it will pick it back up */
  private static GrouperCache<Boolean, Map<String, ClientConnectionConfigBean>> clientConnectionConfigBeanCache = new GrouperCache<Boolean, Map<String, ClientConnectionConfigBean>>(
      ClientConfig.class.getName() + ".clientConnectionConfigBeanCache", 50, false, 300, 300, false);
  
  /**
   * clear the config cache (e.g. for testing)
   */
  public static void clearCache() {
    clientConnectionConfigBeanCache.clear();
  }
  
  /**
   * get the bean map from cache or configure a new one
   * @return the config bean
   */
  public static Map<String, ClientConnectionConfigBean> clientConnectionConfigBeanCache() {
    Map<String, ClientConnectionConfigBean> theClientConnectionConfigBeanCache = clientConnectionConfigBeanCache.get(Boolean.TRUE);
    
    if (theClientConnectionConfigBeanCache == null) {
      
      synchronized (ExternalSubjectConfig.class) {

        //try again
        theClientConnectionConfigBeanCache = clientConnectionConfigBeanCache.get(Boolean.TRUE);
        if (theClientConnectionConfigBeanCache == null) {
          
          theClientConnectionConfigBeanCache = new HashMap<String, ClientConnectionConfigBean>();
          
          for (String propertyName : GrouperConfig.getPropertyNames()) {
            Matcher matcher = grouperClientConnectionIdPattern.matcher(propertyName);
            if (matcher.matches()) {

              //this is the ID
              String connectionId = matcher.group(1);
              
              ClientConnectionConfigBean clientConnectionConfigBean = new ClientConnectionConfigBean();
              
              //note, doesnt really matter what the id is... but it is a mandatory config
              
              //get the act as subject
              //grouperClient.localhost.localActAsSubject
              clientConnectionConfigBean.setLocalActAsSubject(GrouperConfig.getProperty(
                  "grouperClient." + connectionId + ".localActAsSubject"));
              
              clientConnectionConfigBean.setClientConnectionSourceConfigBeans(
                  ClientConfig.clientConnectionSourceConfigBeans(connectionId));
              
              theClientConnectionConfigBeanCache.put(connectionId, clientConnectionConfigBean);

            }
          }
          clientConnectionConfigBeanCache.put(Boolean.TRUE, theClientConnectionConfigBeanCache);
        }        
      }
    }
    return theClientConnectionConfigBeanCache;
  }

  /**
   * get the client connection source config beans based on connection id
   * @param connectionId
   * @return the beans
   */
  private static Map<String,ClientConnectionSourceConfigBean> clientConnectionSourceConfigBeans(String connectionId) {

    //grouperClient.localhost.source.jdbc.id
    Pattern pattern = Pattern.compile("^grouperClient\\." + connectionId + "\\.source\\.([^.]+)\\.id$");
    
    
    
    //lets get the sources
    for (String sourcePropertyName : GrouperConfig.getPropertyNames()) {
      Matcher sourceMatcher = grouperClientConnectionIdPattern.matcher(sourcePropertyName);
      if (sourceMatcher.matches()) {

        //this is the ID
        String sourceId = sourceMatcher.group(1);
        
        ClientConnectionSourceConfigBean clientConnectionSourceConfigBean = new ClientConnectionSourceConfigBean();
        
        //note, doesnt really matter what the id is... but it is a mandatory config

        //# the part between "grouperClient.localhost.source." and ".id" links up the configs, 
        //# in this case, "jdbc", make sure it has no special chars.  sourceId can be blank if you dont want to specify
        //grouperClient.localhost.source.jdbc.local.sourceId = jdbc
        clientConnectionSourceConfigBean.setLocalSourceId(GrouperConfig.getProperty(
            "grouperClient." + connectionId + ".source." + sourceId + ".sourceId"));
        
        //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
        //grouperClient.localhost.source.jdbc.local.read.subjectId = identifier
        //# this is the identifier to lookup to add a subject, should be "id" or "identifier"
        //grouperClient.localhost.source.jdbc.local.write.subjectId = identifier
        //# sourceId of the remote system, can be blank
        //grouperClient.localhost.source.jdbc.remote.sourceId = jdbc
        //# this is the identifier that goes between them, it is "id" or an attribute name.  subjects without this attribute will not be processed
        //grouperClient.localhost.source.jdbc.remote.read.subjectId = 
        //# this is the identifier to lookup to add a subject, should be "id" or "identifier"
        //grouperClient.localhost.source.jdbc.remote.write.subjectId = 
        //# if subjects are external and should be created if not exist
        //grouperClient.localhost.source.jdbc.addExternalSubjectIfNotFound = true
        
        
      }
    }
    return null;
  }
  
  /**
   * grouperClient.localhost.id
   * <pre>
   * ^grouperClient\.           matches start of string, externalSubjects, then a dot
   * ([^.]+)\.                  matches something not a dot, captures that, then a dot
   * id$                        matches id, and end of string
   * </pre>
   */
  private static final Pattern grouperClientConnectionIdPattern = Pattern.compile("^grouperClient\\.([^.]+)\\.id$");
  
}
