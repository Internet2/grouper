package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.EmailValidator;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider;
import edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2_5;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcJdbcConnectionProvider;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter2;
import edu.internet2.middleware.subject.provider.SourceManager;

public class SubjectSourceDiagnostics {

  /**
   * 
   * @param args
   */
  public static void main(final String[] args) {
    
    if (args.length > 4) {
      throw new RuntimeException("Pass in sourceId, subjectId, subjectIdentifier, searchString");
    }
    if (args.length < 1) {
      throw new RuntimeException("At least pass in sourceId.  You can also pass in subjectId, subjectIdentifier, searchString");
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        new SubjectSourceDiagnostics().assignSourceId(args[0])
          .assignSubjectId(args.length > 1 ? args[1] : null)
          .assignSubjectIdentifier(args.length > 2 ? args[2] : null)
          .assignSearchString(args.length > 3 ? args[3] : null)
          .subjectSourceDiagnostics();
        
        return null;
      }
    });
    
  }
  
  private String sourceId;
  
  private String subjectIdentifier;
  
  private String subjectId;
  
  private String searchString;
  
  public SubjectSourceDiagnostics assignSourceId(String theSourceId) {
    this.sourceId = theSourceId;
    return this;
  }
  
  public SubjectSourceDiagnostics assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }
  
  public SubjectSourceDiagnostics assignSubjectIdentifier(String theSubjectIdentifier) {
    this.subjectIdentifier = theSubjectIdentifier;
    return this;
  }
  
  public SubjectSourceDiagnostics assignSearchString(String theSearchString) {
    this.searchString = theSearchString;
    return this;
  }

  public String subjectSourceDiagnosticsFromGsh() {

    ConfigPropertiesCascadeBase.clearCache();
    
    GrouperRequestContainer.assignUseStaticRequestContainer(true);
    try {
      String report = subjectSourceDiagnostics().toString();
      
      //  <pre>
      report = GrouperUtil.replace(report, "<pre>", "");
      
      //  <font color='green'>SUCCESS:</font> Found subject by id in 46ms: 'empl1'
      report = GrouperUtil.replace(report, "<font color='green'>", "");
      report = GrouperUtil.replace(report, "</font>", "");

      //  <font color='orange'>WARNING:</font> No subject found by identifier in 19ms: 'netid@school.edu'
      report = GrouperUtil.replace(report, "<font color='orange'>", "");

      //  <font color='red'>ERROR:</font> Grouper connection provider
      report = GrouperUtil.replace(report, "<font color='red'>", "");

      //  </pre>
      report = GrouperUtil.replace(report, "</pre>", "");
      
      return report;
      
    } finally {
      GrouperRequestContainer.clearStaticRequestContainer();
    }
    
  }

  public StringBuilder subjectSourceDiagnostics() {
    
    StringBuilder subjectApiReport = new StringBuilder();
    
    subjectApiReport.append("<pre>\n");
    
    // make sure theres a grouper session
    GrouperSession.staticGrouperSession(true);
    
    Subject theSubject = null;
    
    Set<Subject> subjectsSearch = null;
    Set<Subject> subjectsPage = null;
    
    if (StringUtils.isBlank(sourceId)) {
      
      subjectApiReport.append("<font color='red'>ERROR:</font> No source ID specified\n");
      
    } else {
      Source source = SourceManager.getInstance().getSource(sourceId);
      
      if (source == null) {
        throw new RuntimeException("Cant find source by id: '" + sourceId + "'");
      }
  
      {
        long now = System.nanoTime();
        Exception exception = null;
        Subject subject = null;
        try {
          subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
          theSubject = subject;
        } catch (Exception e) {
          exception = e;
        }
        long millis = (System.nanoTime() - now) / 1000000L;
        if (subject != null) {
          subjectApiReport.append("<font color='green'>SUCCESS:</font> Found subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          if (StringUtils.equals(subjectId, subject.getId())) {
            subjectApiReport.append("<font color='green'>SUCCESS:</font> Subject id in returned subject matches the subject id searched for: '" + GrouperUtil.xmlEscape(subjectId) + "'\n");
          } else {
            subjectApiReport.append("<font color='red'>ERROR:</font> Subject id in returned subject '" + GrouperUtil.xmlEscape(subject.getId()) + "'does not match the subject id searched for: '" + GrouperUtil.xmlEscape(subjectId) + "'\n");
          }
        } else if (exception == null) {
          subjectApiReport.append("<font color='orange'>WARNING:</font> No subject found by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
        } else {
          subjectApiReport.append("<font color='red'>ERROR:</font> Exception thrown when finding subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdAndSource(\"" + GrouperUtil.xmlEscape(subjectId) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
        }
      }        
      
      {
        long now = System.nanoTime();
        Exception exception = null;
        Subject subject = null;
        try {
          subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
          if (theSubject == null) {
            theSubject = subject;
          }
        } catch (Exception e) {
          exception = e;
        }
        long millis = (System.nanoTime() - now) / 1000000L;
        if (subject != null) {
          subjectApiReport.append("<font color='green'>SUCCESS:</font> Found subject by identifier in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectIdentifier) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
        } else if (exception == null) {
          subjectApiReport.append("<font color='orange'>WARNING:</font> No subject found by identifier in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectIdentifier) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
        } else {
          subjectApiReport.append("<font color='red'>ERROR:</font> Exception thrown when finding subject by id in " + millis + "ms: '" + GrouperUtil.xmlEscape(subjectId) + "'\n         with SubjectFinder.findByIdentifierAndSource(\"" + GrouperUtil.xmlEscape(subjectIdentifier) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\", false)\n");
          subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
        }
      }        
      
      {
        long now = System.nanoTime();
        Exception exception = null;
        Set<Subject> subjects = null;
        try {
          subjects = SubjectFinder.findAll(searchString, sourceId);
          subjectsSearch = subjects;
          if (theSubject == null && GrouperUtil.length(subjects) > 0) {
            theSubject = subjects.iterator().next();
          }
        } catch (Exception e) {
          exception = e;
        }
        long millis = (System.nanoTime() - now) / 1000000L;
        if (GrouperUtil.length(subjects) > 0) {
          subjectApiReport.append("<font color='green'>SUCCESS:</font> Found " + GrouperUtil.length(subjects) + " subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
        } else if (exception == null) {
          subjectApiReport.append("<font color='orange'>WARNING:</font> Found no subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
        } else {
          subjectApiReport.append("<font color='red'>ERROR:</font> Exception finding subjects by search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findAll(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
        }
      }        
      
      {
        long now = System.nanoTime();
        Exception exception = null;
        Set<Subject> subjects = null;
        SearchPageResult searchPageResult = null;
        try {
          searchPageResult = SubjectFinder.findPage(searchString, sourceId);
          if (searchPageResult != null) { 
            subjects = searchPageResult.getResults();
            subjectsPage = subjects;
            if (theSubject == null && GrouperUtil.length(subjects) > 0) {
              theSubject = subjects.iterator().next();
            }
          }
        } catch (Exception e) {
          exception = e;
        }
        long millis = (System.nanoTime() - now) / 1000000L;
        if (GrouperUtil.length(subjects) > 0) {
          subjectApiReport.append("<font color='green'>SUCCESS:</font> Found " + GrouperUtil.length(subjects) + " subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
        } else if (exception == null) {
          subjectApiReport.append("<font color='orange'>WARNING:</font> Found no subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
        } else {
          subjectApiReport.append("<font color='red'>ERROR:</font> Exception finding subjects by paged search string in " + millis + "ms: '" + GrouperUtil.xmlEscape(searchString) + "'\n         with SubjectFinder.findPage(\"" + GrouperUtil.xmlEscape(searchString) + "\", \"" + GrouperUtil.xmlEscape(sourceId) + "\")\n");
          subjectApiReport.append(ExceptionUtils.getFullStackTrace(exception));
        }
      }       
      subjectApiReport.append("\n######## SUBJECT ATTRIBUTES ########\n\n");
      
      if (theSubject == null) {
        subjectApiReport.append("<font color='red'>ERROR:</font> Cannot list attributes of a subject if cannot find any subjects\n");
      } else {
        subjectApiReport.append("Subject id: '" + GrouperUtil.xmlEscape(theSubject.getId()) + "' with subject.getId()\n");
        subjectApiReport.append("  - the subject id should be an unchanging opaque identifier\n");
        subjectApiReport.append("  - the subject id is stored in the grouper_members table\n");
        subjectApiReport.append("Subject name: '" + GrouperUtil.xmlEscape(theSubject.getName()) + "' with subject.getName()\n");
        subjectApiReport.append("  - the subject name is generally first last\n");
        subjectApiReport.append("Subject description: '" + GrouperUtil.xmlEscape(theSubject.getDescription()) + "' with subject.getDescription()\n");
        subjectApiReport.append("  - the subject description can have more info such as the id, name, dept, etc\n");
        subjectApiReport.append("Subject type: '" + GrouperUtil.xmlEscape(theSubject.getTypeName()) + "' with subject.getTypeName()\n");
        subjectApiReport.append("  - the subject type is not really used\n");
        Map<String, Set<String>> attributes = theSubject.getAttributes(true);
        if (attributes != null) {
          for (String attributeName : attributes.keySet()) {
            Set<String> values = attributes.get(attributeName);
            if (GrouperUtil.length(values) == 1) {
              subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has 1 value: '" + GrouperUtil.xmlEscape(theSubject.getAttributeValue(attributeName)) + "'\n  - with subject.getAttributeValue(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
              
            } else if (GrouperUtil.length(values) > 1) {
              subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has " + GrouperUtil.length(values) + " value: '" + GrouperUtil.xmlEscape(theSubject.getAttributeValueOrCommaSeparated(attributeName)) + "'\n  - with subject.getAttributeValues(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
            } else {
              subjectApiReport.append("Subject attribute '" + GrouperUtil.xmlEscape(attributeName) + "' has no value\n  - with subject.getAttributeValue(\"" + GrouperUtil.xmlEscape(attributeName) + "\")\n");
            }
          }
        }
        
        // dont check for internal sources
        if (!StringUtils.equals(GrouperSourceAdapter.groupSourceId(), sourceId) && !StringUtils.equals(InternalSourceAdapter.ID, sourceId)) {
          String emailAttributeNameForSource = GrouperEmailUtils.emailAttributeNameForSource(sourceId);
          if (StringUtils.isBlank(emailAttributeNameForSource)) {
            subjectApiReport.append("<font color='blue'>NOTE:</font> This source does not list an attribute named emailAttributeName so Grouper will not be able to get the email address of a subject from this source\n");
          } else {
            
            subjectApiReport.append("<font color='green'>SUCCESS:</font> The emailAttributeName is configured to be: '" + GrouperUtil.xmlEscape(emailAttributeNameForSource) + "'\n");
            
            String emailAddress = theSubject.getAttributeValue(emailAttributeNameForSource);
            
            if (!StringUtils.isBlank(emailAddress)) {
              
              if (EmailValidator.getInstance().isValid(emailAddress)) {
                
                subjectApiReport.append("<font color='green'>SUCCESS:</font> The email address '" + GrouperUtil.xmlEscape(emailAddress) + "' was found and has a valid format\n");
  
              } else {
                
                subjectApiReport.append("<font color='red'>ERROR:</font> The email address '" + GrouperUtil.xmlEscape(emailAddress) + "' was found but does not have valid format\n");
              }
              
            } else {
              
              subjectApiReport.append("<font color='orange'>WARNING:</font> The email attribute value is blank for this subject\n");
            }
          }
        }
  
      }
  
      subjectApiReport.append("\n######## SUBJECT IN UI ########\n\n");
      {
        if (theSubject == null) {
          subjectApiReport.append("<font color='red'>ERROR:</font> Cannot show subject UI view if cannot find any subjects\n");
        } else {
  
          if (StringUtils.equals(GrouperSourceAdapter.groupSourceId(), theSubject.getSourceId())) {
            GuiGroup guiGroup = new GuiGroup(((GrouperSubject)theSubject).internal_getGroup());
            subjectApiReport.append("Short link with icon: " + guiGroup.getShortLinkWithIcon() + "\n");
            subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiGroupShortLink\n");
            subjectApiReport.append("  - By default this is the display extension with a tooltip for path and description\n");
            
            subjectApiReport.append("Link with icon: " + guiGroup.getLinkWithIcon() + "\n");
            subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiGroupLink\n");
            subjectApiReport.append("  - By default this is the display name with a tooltip for path and description\n");
            
          } else {
            GuiSubject guiSubject = new GuiSubject(theSubject);
            subjectApiReport.append("Short link with icon: " + guiSubject.getShortLinkWithIcon() + "\n");
            subjectApiReport.append("  - This is configured in grouper.text.en.us.base.properties with guiSubjectShortLink\n");
            subjectApiReport.append("  - Also configured in grouper-ui.properties with grouperUi.screenLabel2.sourceId.X\n");
            subjectApiReport.append("  - By default this is the name of the subject with a tooltip for description\n");
            
            subjectApiReport.append("Long label with icon: " + guiSubject.getScreenLabelLongWithIcon() + "\n");
            subjectApiReport.append("  - This is not used in the new UI\n");
            subjectApiReport.append("  - It is configured in grouper-ui.properties with grouperUi.subjectImg.screenEl.\n");
            subjectApiReport.append("  - By default this is the description of the subject\n");
            
          }
        }
        
      }
  
      
      subjectApiReport.append("\n######## SUBJECT IN WS ########\n\n");
      {
        if (theSubject == null) {
          subjectApiReport.append("<font color='red'>ERROR:</font> Cannot show subject WS view if cannot find any subjects\n");
        } else {
          subjectApiReport.append("Look in grouper-ws.properties to see how the WS uses subjects.  This is the default configuation:\n\n");
          subjectApiReport.append("# subject attribute names to send back when a WsSubjectResult is sent, comma separated\n");
          subjectApiReport.append("# e.g. name, netid\n");
          subjectApiReport.append("# default is none\n");
          subjectApiReport.append("ws.subject.result.attribute.names = \n\n");
          subjectApiReport.append("# subject result attribute names when extended data is requested (comma separated)\n");
          subjectApiReport.append("# default is name, description\n");
          subjectApiReport.append("# note, these will be in addition to ws.subject.result.attribute.names\n");
          subjectApiReport.append("ws.subject.result.detail.attribute.names = \n");
  
        }
        
      }
  
      subjectApiReport.append("\n######## SOURCE CONFIGURATION ########\n\n");
      
      {
        String sourceConfigId = null;
        Pattern sourceIdConfigPattern = Pattern.compile("^subjectApi\\.source\\.([^.]+)\\.id$");
        
        for (String configName : SubjectConfig.retrieveConfig().propertyNames()) {
          
          //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
          //  # subjectApi.source.<configName>.id = sourceId
  
          Matcher matcher = sourceIdConfigPattern.matcher(configName);
          if (matcher.matches()) {
            sourceConfigId = matcher.group(1);
            if (StringUtils.equals(sourceId, SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".id"))) {
              break;
            }
          }
        }
        
        if (StringUtils.isBlank(sourceConfigId)) {
  
          subjectApiReport.append("<font color='red'>ERROR:</font> Cannot find source in subject.properties\n");
          
        } else {
  
          BaseSourceAdapter sourceAdapter = null;
          {
          
            String adapterClassName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".adapterClass");
            subjectApiReport.append("Adapter class: '" + GrouperUtil.xmlEscape(adapterClassName) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".adapterClass\n");
            
            Class<?> adapterClassClass = null;
            
            try {
              adapterClassClass = SubjectUtils.forName(adapterClassName);
              subjectApiReport.append("<font color='green'>SUCCESS:</font> Found adapter class\n");
              try {
                sourceAdapter = (BaseSourceAdapter)SubjectUtils.newInstance(adapterClassClass);
                subjectApiReport.append("<font color='green'>SUCCESS:</font> Instantiated adapter class\n");
              } catch (Exception e) {
                subjectApiReport.append("<font color='red'>ERROR:</font> Cannot instantiate adapter class\n");
              }                
            } catch (Exception e) {
              subjectApiReport.append("<font color='red'>ERROR:</font> Cannot find adapter class\n");
            }
  
          }              
          
          if ( (sourceAdapter instanceof JDBCSourceAdapter || sourceAdapter instanceof JDBCSourceAdapter2) && !(sourceAdapter instanceof GrouperJdbcSourceAdapter2_5) ) {
            
            String jdbcConnectionProviderClassName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".param.jdbcConnectionProvider.value"); 
            subjectApiReport.append("JDBC connection provider class: '" + GrouperUtil.xmlEscape(jdbcConnectionProviderClassName) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".param.jdbcConnectionProvider.value\n");
            
            Class<?> jdbcConnectionProviderClassClass = null;
            
            try {
              jdbcConnectionProviderClassClass = SubjectUtils.forName(jdbcConnectionProviderClassName);
              subjectApiReport.append("<font color='green'>SUCCESS:</font> Found connection provider class\n");
              try {
                GcJdbcConnectionProvider gcJdbcConnectionProvider = (GcJdbcConnectionProvider)SubjectUtils.newInstance(jdbcConnectionProviderClassClass);
                subjectApiReport.append("<font color='green'>SUCCESS:</font> Instantiated connection provider class\n");
  
                if (gcJdbcConnectionProvider instanceof GrouperJdbcConnectionProvider) {
                  
                  String jdbcConfigId = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".param.jdbcConfigId.value");
                  subjectApiReport.append("JDBC config ID: '" + GrouperUtil.xmlEscape(jdbcConfigId) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".param.jdbcConfigId.value\n");
                  
                  if (StringUtils.isBlank(jdbcConfigId) || StringUtils.equalsIgnoreCase("grouper", jdbcConfigId)) {
                    subjectApiReport.append("<font color='green'>SUCCESS:</font> Grouper connection provider\n");
                    
                  } else {
                    
                    try {
                      GrouperUtil.closeQuietly(new GrouperLoaderDb(jdbcConfigId).connection());
                      subjectApiReport.append("<font color='green'>SUCCESS:</font> '" + jdbcConfigId + "' is a valid connection provider\n");
                    } catch (Exception e) {
                      subjectApiReport.append("<font color='red'>ERROR:</font> '" + jdbcConfigId + "' is an invalid connection provider\n" + ExceptionUtils.getFullStackTrace(e) + "\n");
                    }
                    
                  }
                }
                
              } catch (Exception e) {
                subjectApiReport.append("<font color='red'>ERROR:</font> Cannot instantiate adapter class\n");
              }                
            } catch (Exception e) {
              subjectApiReport.append("<font color='red'>ERROR:</font> Cannot find adapter class\n");
            }
  
          }              
          
          //  # generally the <configName> is the same as the source id.  Generally this should not have special characters
          //  # subjectApi.source.<configName>.id = sourceId
          subjectApiReport.append("Source id: '" + GrouperUtil.xmlEscape(sourceId) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".id\n");
          
          //  # this is a friendly name for the source
          //  # subjectApi.source.<configName>.name = sourceName
          subjectApiReport.append("Source name: '" + GrouperUtil.xmlEscape(source.getName()) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".name\n");
  
          //  # type is not used all that much 
          //  # subjectApi.source.<configName>.types = person, application
          subjectApiReport.append("Source types: '" + GrouperUtil.xmlEscape(SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".types")) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".types\n");
  
          {
            Pattern paramValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.param\\.([^.]+)\\.value$");
            
            //params (note, name is optional and generally not there)
            //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
            for (String paramValueKey : SubjectConfig.retrieveConfig().propertyNames()) {
              
              if (paramValueKey.startsWith("subjectApi.source." + sourceConfigId + ".param") 
                  && paramValueKey.endsWith(".value") ) {
                String paramValue = SubjectConfig.retrieveConfig().propertyValueString(paramValueKey);
                Matcher paramValueMatcher = paramValueConfigPattern.matcher(paramValueKey);
                paramValueMatcher.matches();
                String paramConfigId = paramValueMatcher.group(1);
                String paramName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".param." + paramConfigId + ".name");
                boolean hasParamName = true;
                if (StringUtils.isBlank(paramName)) {
                  paramName = paramConfigId;
                  hasParamName = false;
                }
                subjectApiReport.append("Source param name: '" + GrouperUtil.xmlEscape(paramName) + "' has value: '");
                if (paramName.toLowerCase().contains("pass") || paramName.toLowerCase().contains("cred") || paramName.toLowerCase().contains("pwd")) {
                  subjectApiReport.append("*******'\n");
                } else {
                  subjectApiReport.append(GrouperUtil.xmlEscape(paramValue) + "'\n");
                }
                if (hasParamName) {
                  subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".param." + paramConfigId + ".name\n");
                }
                subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".param." + paramConfigId + ".value\n");
              }
            }
          }
          
          {
  
            //  # internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
            //  # subjectApi.source.<configName>.internalAttributes = someName, anotherName
            String internalAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".internalAttributes");
            if (StringUtils.isBlank(internalAttributes)) {
              subjectApiReport.append("No internal attributes configured\n");
            } else {
              subjectApiReport.append("Internal attributes: '" + GrouperUtil.xmlEscape(internalAttributes) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".internalAttributes\n");
            }
          }
  
          {
            //  # attributes from ldap object to become subject attributes.  comma separated
            //  # subjectApi.source.<configName>.attributes = cn, sn, uid, department, exampleEduRegId
            String attributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".attributes");
            if (StringUtils.isBlank(attributes)) {
              subjectApiReport.append("No attributes configured\n");
            } else {
              subjectApiReport.append("Attributes: '" + GrouperUtil.xmlEscape(attributes) + "'\n  - configured in subject.properties: subjectApi.source." + sourceId + ".attributes\n");
            }
          }
  
          //  digester.addObjectCreate("sources/source/search",
          //      "edu.internet2.middleware.subject.provider.Search");
          //  digester.addCallMethod("sources/source/search/searchType", "setSearchType", 0);
          //  digester.addCallMethod("sources/source/search/param", "addParam", 2);
          //  digester.addCallParam("sources/source/search/param/param-name", 0);
          //  digester.addCallParam("sources/source/search/param/param-value", 1);
          //  digester.addSetNext("sources/source/search", "loadSearch");
  
          //  # searchTypes are: 
          //  #   searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.  Each subject has one and only on ID.  Returns one result when searching for one ID.
          //  #   searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely identifies the user, e.g. jsmith or jsmith@institution.edu.  
          //  #        Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique even across sources.  Returns one result when searching for one identifier.
          //  #   search: find subjects by free form search.  Returns multiple results.
          //  # subjectApi.source.<configName>.search.<searchType>.param.<paramName>.value = something
          {
            Pattern searchParamValueConfigPattern = Pattern.compile("^subjectApi\\.source\\.[^.]+\\.search\\.[^.]+\\.param\\.([^.]+)\\.value$");
            //params (note, name is optional and generally not there)
            //  # subjectApi.source.<configName>.param.throwErrorOnFindAllFailure.value = true
            for (String searchType : new String[] {"searchSubject", "searchSubjectByIdentifier", "search"}) {
                              
              for (String paramValueKey : SubjectConfig.retrieveConfig().propertyNames()) {
                
                //all search params has a value
                if (paramValueKey.startsWith("subjectApi.source." + sourceId + ".search." + searchType + ".param.") 
                    && paramValueKey.endsWith(".value") ) {
                  String paramValue = SubjectConfig.retrieveConfig().propertyValueString(paramValueKey);
                  Matcher paramValueMatcher = searchParamValueConfigPattern.matcher(paramValueKey);
                  paramValueMatcher.matches();
                  String paramConfigId = paramValueMatcher.group(1);
                  String paramName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".name");
                  boolean hasParamName = true;
  
                  //if name is not specified used the config id (most arent specified)
                  if (StringUtils.isBlank(paramName)) {
                    paramName = paramConfigId;
                    hasParamName = false;
                  }
  
                  subjectApiReport.append("Search '" + searchType + "' param name: '" + GrouperUtil.xmlEscape(paramName) + "' has value: '");
                  subjectApiReport.append(GrouperUtil.xmlEscape(paramValue) + "'\n");
                  
                  if (hasParamName) {
                    subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".name\n");
                  }
                  subjectApiReport.append("  - configured in subject.properties: subjectApi.source." + sourceId + ".search." + searchType + ".param." + paramConfigId + ".value\n");
  
                }
              }
              
            }
          }
        }
      }
    }
    
    subjectApiReport.append("\n######## SUBJECT SEARCH RESULTS ########\n\n");
    if (GrouperUtil.length(subjectsSearch) == 0) {
      
      subjectApiReport.append("No subjects found in search\n");
      
    } else {
      int subjectCount = 0;
      for (Subject subject : subjectsSearch) {
        if (subjectCount >= 99) {
          subjectApiReport.append("Only first 100 subjects displayed... of " + GrouperUtil.length(subjectsSearch) + "\n");
          break;
        }
        subjectApiReport.append("Subject " + subjectCount +  ": id: " + GrouperUtil.xmlEscape(subject.getId()) 
            + ", name: " + GrouperUtil.xmlEscape(subject.getName()) + "\n  - description: " + GrouperUtil.xmlEscape(subject.getDescription()) + "\n");
        subjectCount++;
      }
    }
    
    subjectApiReport.append("\n######## SUBJECT PAGE RESULTS ########\n\n");
    if (GrouperUtil.length(subjectsPage) == 0) {
      
      subjectApiReport.append("No subjects found in search page\n");
      
    } else {
      int subjectCount = 0;
      for (Subject subject : subjectsPage) {
        if (subjectCount >= 99) {
          subjectApiReport.append("Only first 100 subjects displayed... of " + GrouperUtil.length(subjectsPage) + "\n");
          break;
        }
        subjectApiReport.append("Subject " + subjectCount +  ": id: " + GrouperUtil.xmlEscape(subject.getId()) 
            + ", name: " + GrouperUtil.xmlEscape(subject.getName()) + "\n  - description: " + GrouperUtil.xmlEscape(subject.getDescription()) + "\n");
        subjectCount++;
      }
    }
    
    
    subjectApiReport.append("</pre>");
    
    return subjectApiReport;
  }

}
