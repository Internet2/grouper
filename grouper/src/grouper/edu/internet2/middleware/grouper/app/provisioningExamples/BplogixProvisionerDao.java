package edu.internet2.middleware.grouper.app.provisioningExamples;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;

public class BplogixProvisionerDao extends GrouperProvisionerTargetDaoBase {

  public static class BplogixUser {

    String uid;
    String authType; // SAML
    String externalGUID; // mchyzer@upenn.edu
    String userName; // Chris Hyzer
    String userId; // mchyzer@upenn.edu
    String email; // mchyzer@upenn.edu
    String title; // Savant
    String dept; // isc
    String customString; // 10021368
    boolean autoDST = false;
    Boolean acctDisabled = null;
    Boolean acctLocked = null;
    Integer taskCount = null;
    Boolean loggedIn = null;
    
    public boolean userExists() {
      return StringUtils.isNotBlank(this.uid);
    }
    
    public String toString() {
      return "BplogixUser [uid=" + uid + ", authType=" + authType + ", externalGUID=" + externalGUID +
          ", userName=" + userName + ", userId=" + userId + ", email=" + email +
          ", title=" + title + ", dept=" + dept + ", customString=" + customString +
          ", autoDST=" + autoDST + ", acctDisabled=" + acctDisabled + ", acctLocked=" + 
          acctLocked + ", loggedIn=" + loggedIn + ", taskCount=" + taskCount + ", userExists()=" + userExists() + "]";
    }

  }
  public static class TheState {

    boolean isProd = false;
    boolean isStage = false;
    boolean isDev = false;

    String configSuffix;
    String logTableName;
    String userTableName;
    String groupNameFedFromBplogix;
    String groupNameAutomaticLicense;
    String groupNameCanClaimLicense;
    String groupNameHasSelfClaimedLicense;
    String groupNameRecentBpLogixUser;
    String groupNameUsersWithTasks;
    String groupNameUsersWithLicense;
    String groupNameCanLogIn;
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog;

    Map<String, BplogixUser> eppnToBplogixUser = new HashMap<String, BplogixUser>();
    
    Map<String, Subject> pennidToSubject;
    
    Map<String, String> groupExtensionToBplogixGid = new HashMap<String, String>();

    BplogixCommands bplogixCommands = null;

    public void initTheState(String theProvisionerConfigId) {
      if(StringUtils.equals("bplogixProd", theProvisionerConfigId)) {
        this.isProd = true;
      }
      if (StringUtils.equals("bplogixStage", theProvisionerConfigId)) {
        this.isStage = true;
      }
      if (StringUtils.equals("bplogixDev", theProvisionerConfigId)) {
        this.isDev = true;
      }

      this.configSuffix = this.isProd ? "Prod" : (this.isStage ? "Stage" : "Dev");
      this.logTableName = this.isProd ? "bplogix_provisioning_log_prod" : (this.isStage ? "bplogix_provisioning_log_stage" : "bplogix_provisioning_log_dev");
      this.userTableName = this.isProd ? "bplogix_active_users" : (this.isStage ? "bplogix_active_users_stage" : "bplogix_active_users_dev");
      this.groupNameFedFromBplogix = this.isProd ? "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsers" :
        (this.isStage ? "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsersStage" : "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsersDev");
      this.groupNameAutomaticLicense = this.isProd ? "penn:isc:ait:apps:bplogix:service:policy:licensingProd:bplogixAutomaticLicenseProd": ( 
          this.isStage ? "penn:isc:ait:apps:bplogix:service:policy:licensingStage:bplogixAutomaticLicenseStage" : 
            "penn:isc:ait:apps:bplogix:service:policy:licensingDev:bplogixAutomaticLicenseDev");
      this.groupNameCanClaimLicense = this.isProd ? "penn:community:affiliateMemberAlum" : (
              this.isStage ? "penn:isc:ait:apps:bplogix:service:policy:licensingStage:bplogixCanClaimLicenseStage"
                  : "penn:isc:ait:apps:bplogix:service:policy:licensingDev:bplogixCanClaimLicenseDev");
      this.groupNameHasSelfClaimedLicense = this.isProd ? "penn:isc:ait:apps:bplogix:service:policy:licensingProd:bplogixHasSelfClaimedLicenseProd" :
          (this.isStage ? "penn:isc:ait:apps:bplogix:service:policy:licensingStage:bplogixHasSelfClaimedLicenseStage"
              : "penn:isc:ait:apps:bplogix:service:policy:licensingDev:bplogixHasSelfClaimedLicenseDev");
      this.groupNameRecentBpLogixUser = this.isProd ? "penn:isc:ait:apps:bplogix:service:policy:licensingProd:bplogixRecentAuthenticationProd" : 
          (this.isStage
              ? "penn:isc:ait:apps:bplogix:service:policy:licensingStage:bplogixRecentAuthenticationStage"
              : "penn:isc:ait:apps:bplogix:service:policy:licensingDev:bplogixRecentAuthenticationDev");
      this.groupNameUsersWithTasks = this.isProd ? "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsersWithTask" : 
          (this.isStage
              ? "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsersWithTaskStage"
              : "penn:isc:ait:apps:bplogix:service:reports:BPLogixActiveUsersWithTaskDev");
      this.groupNameUsersWithLicense = this.isProd ? "penn:isc:ait:apps:bplogix:service:policy:licensingProd:bplogixHasLicenseProd" : 
          (this.isStage
              ? "penn:isc:ait:apps:bplogix:service:policy:licensingStage:bplogixHasLicenseStage"
              : "penn:isc:ait:apps:bplogix:service:policy:licensingDev:bplogixHasLicenseDev");
      this.groupNameCanLogIn = this.isProd ? "penn:isc:ait:apps:bplogix:service:policy:bplogixCanLogInProd"
          : (this.isStage
              ? "penn:isc:ait:apps:bplogix:service:policy:bplogixCanLogInStage"
              : "penn:isc:ait:apps:bplogix:service:policy:bplogixCanLogInDev");

      String url = GrouperConfig.retrieveConfig().propertyValueStringRequired("bplogixEndpoint" + this.configSuffix);
      url = GrouperUtil.stripLastSlashIfExists(url);
      String user = GrouperConfig.retrieveConfig().propertyValueStringRequired("bplogixUser" + this.configSuffix);
      String pass = GrouperConfig.retrieveConfig().propertyValueStringRequired("bplogixPassword" + this.configSuffix);
      pass = Morph.decryptIfFile(pass);
      
      
      this.bplogixCommands = new BplogixCommands(url, user, pass);

    }

  }
  
  public static class BplogixCommands {

    // if a token is needed, get it, otherwise re-use the existing one
    private String wsAuthnToken = null;
    private long wsAuthTokenRetrievedMillis = 0;
    private String wsPass;
    private String wsUrl;
    private String wsUser;

    public BplogixCommands(String url, String user, String pass) {
      this.wsUser = user;
      this.wsPass = pass;
      this.wsUrl = url;
    }
    
    /**
     * add user to group
     * @param subjectId
     * @return true if enabled
     */
    public void addUserToGroup(String bplogixUid, String bplogixGid) {
      
      retrieveWsAuthnToken();
    
      // DOC: https://bplogix.school.edu/Services/wsUser.asmx?op=AddUserToGroup
    
      //  POST https://bplogix.school.edu/Services/wsUser.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/AddUserToGroup
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <AddUserToGroup xmlns="http://www.bplogix.com/WebServices">
      //        <UID>ea57968f-424b-4dbf-a30e-9dd3e53d057b</UID>
      //        <GID>80e48834-33d6-4fa2-ae1d-22c5060c85cc</GID>
      //      </AddUserToGroup>
      //    </soap:Body>
      //  </soap:Envelope>
              
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <AddUserToGroup xmlns="http://www.bplogix.com/WebServices">
              <UID>%s</UID>
              <GID>%s</GID>
            </AddUserToGroup>
          </soap:Body>
        </soap:Envelope>
                  """.formatted(this.wsAuthnToken, bplogixUid, bplogixGid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/AddUserToGroup").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        // 200 success
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <AddUserToGroupResponse xmlns="http://www.bplogix.com/WebServices">
        //        <AddUserToGroupResult>true</AddUserToGroupResult>
        //      </AddUserToGroupResponse>
        //    </soap:Body>
        //  </soap:Envelope>
        boolean success = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/AddUserToGroupResponse/AddUserToGroupResult", document, XPathConstants.STRING));
        
        GrouperUtil.assertion(success, "AddUserToGroup failed: uid: " + bplogixUid + ", gid: " + bplogixGid + ", " + grouperHttpClient.getResponseBody());
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
      
    }

    /**
     * create a bp logix user, note you should retrieve it after this call
     * @param subjectId
     * @return the uid
     */
    public String createBplogixUser(String eppn, String email, String name) {
      
      retrieveWsAuthnToken();
    
      // DOCS: https://bplogix.school.edu/Services/wsUser.asmx?op=CreateExternalUser 
    
      //  POST https://bplogix.school.edu/Services/wsUser.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/CreateExternalUser
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>string</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <CreateExternalUser xmlns="http://www.bplogix.com/WebServices">
      //        <UserID>abc@upenn.edu</UserID>
      //        <Email>abc@isc.upenn.edu</Email>
      //        <UserName>Ab Cee</UserName>
      //        <GUID>abc@upenn.edu</GUID>
      //        <UserType>SAML</UserType>
      //      </CreateExternalUser>
      //    </soap:Body>
      //  </soap:Envelope>
              
      if (StringUtils.isBlank(email)) {
        email = eppn;
      }
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <CreateExternalUser xmlns="http://www.bplogix.com/WebServices">
              <UserID>%s</UserID>
              <Email>%s</Email>
              <UserName>%s</UserName>
              <GUID>%s</GUID>
              <UserType>SAML</UserType>
            </CreateExternalUser>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, eppn, email, name, eppn);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/CreateExternalUser").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <CreateExternalUserResponse xmlns="http://www.bplogix.com/WebServices">
        //        <CreateExternalUserResult>
        //          <UID>055dc291-cdb6-4b1f-bb7e-d9458bcf6163</UID>
        //          <AuthType>SAML</AuthType>
        //          <ExternalGUID>abc@upenn.edu</ExternalGUID>
        //          <UserName>Ab Cee</UserName>
        //          <UserID>abc@upenn.edu</UserID>
        //          <Email>abc@isc.upenn.edu</Email>
        //          <Title />
        //          <Phone />
        //          <Description />
        //          <Company />
        //          <Office />
        //          <Dept />
        //          <BusinessUnit />
        //          <LegalEntity />
        //          <CustomString />
        //          <CustomString2 />
        //          <CustomNumber>0</CustomNumber>
        //          <CustomDate>1798-12-31T16:00:00-08:00</CustomDate>
        //          <Culture />
        //          <AutoDST>false</AutoDST>
        //          <TimeZone />
        //          <LoggedIn>false</LoggedIn>
        //          <AvgLoginSecs>0</AvgLoginSecs>
        //          <AcctLocked>false</AcctLocked>
        //          <AcctDisabled>false</AcctDisabled>
        //          <Groups />
        //        </CreateExternalUserResult>
        //      </CreateExternalUserResponse>
        //    </soap:Body>
        //  </soap:Envelope>
    
        String uid = (String) xPath.evaluate("/Envelope/Body/CreateExternalUserResponse/CreateExternalUserResult/UID", document, XPathConstants.STRING);
        GrouperUtil.assertion(!StringUtils.isBlank(uid), "UID is not found after create for eppn: '" + eppn + "', " + grouperHttpClient.getResponseBody());
        
        return uid;
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    
    }

    /**
     * update a bp logix user, set the pennid
     * @param uid 
     * @param pennid 
     * @return the uid
     */
    public String updateBplogixUserPennid(String uid, String pennid) {
      
      retrieveWsAuthnToken();
    
      //  DOCS: https://upenn-dev.bplogix.net/Services/wsUser.asmx?op=UpdateUserFields
      //
      //  POST https://upenn-staging.bplogix.net/Services/wsUser.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/UpdateUserFields
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>string</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //    <UpdateUserFields xmlns="http://www.bplogix.com/WebServices">
      //     <pUser>
      //      <UID>abc-123-def-456</UID>
      //      <CustomString>12345678</CustomString>
      //     </pUser>
      //   </UpdateUserFields>
      //    </soap:Body>
      //  </soap:Envelope>
              
      GrouperUtil.assertion(!StringUtils.isBlank(uid), "uid is required to update user");
      GrouperUtil.assertion(!StringUtils.isBlank(pennid), "pennid is required to update user");
      String xml = """
          <?xml version="1.0" encoding="utf-8"?>
          <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
            <soap:Header>
              <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
                <AuthToken>%s</AuthToken>
              </bpSoapHeader>
            </soap:Header>
            <soap:Body>
              <UpdateUserFields xmlns="http://www.bplogix.com/WebServices">
                <pUser>
                  <UID>%s</UID>
                  <CustomString>%s</CustomString>
                </pUser>
              </UpdateUserFields>
            </soap:Body>
          </soap:Envelope>
            """
          .formatted(this.wsAuthnToken, uid, pennid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/UpdateUserFields").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc-123-def-456</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <UpdateUserFieldsResponse xmlns="http://www.bplogix.com/WebServices">
        //        <UpdateUserFieldsResult>true</UpdateUserFieldsResult>
        //      </UpdateUserFieldsResponse>
        //    </soap:Body>
        //  </soap:Envelope>
    
        String result = (String) xPath.evaluate("/Envelope/Body/UpdateUserFieldsResponse/UpdateUserFieldsResult", document, XPathConstants.STRING);
        GrouperUtil.assertion(StringUtils.equals(result, "true"), "Result should be true after update: '" + result + "', " + grouperHttpClient.getResponseBody());
        
        return uid;
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    
    }

    /**
     * diable bp logix user
     * @return true if disabled
     */
    public boolean disableBplogixUser(String bplogixUid) {
      
      retrieveWsAuthnToken();
    
      // DOC: https://bplogix.school.edu/Services/wsUser.asmx?op=DisableUserAccount
    
      //  POST https://bplogix.school.edu/Services/wsUser.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/DisableUserAccount
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //    <DisableUserAccount xmlns="http://www.bplogix.com/WebServices">
      //      <UID>def456</UID>
      //    </DisableUserAccount>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
          <DisableUserAccount xmlns="http://www.bplogix.com/WebServices">
            <UID>%s</UID>
          </DisableUserAccount>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, bplogixUid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/DisableUserAccount").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
      //  200 success
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //        <ThrowErrors>true</ThrowErrors>
      //        <Error>
      //          <RC>Good</RC>
      //        </Error>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <DisableUserAccountResponse xmlns="http://www.bplogix.com/WebServices">
      //        <DisableUserAccountResult>true</DisableUserAccountResult>
      //      </DisableUserAccountResponse>
      //    </soap:Body>
      //  </soap:Envelope>
      boolean acctDisabled = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/DisableUserAccountResponse/DisableUserAccountResult", document, XPathConstants.STRING));
      return acctDisabled;  
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
      
    }

    /**
     * enable bp logix user
     * @param subjectId
     * @return true if enabled
     */
    public boolean enableBplogixUser(String bplogixUid) {
      
      retrieveWsAuthnToken();
    
      // DOC: https://bplogix.school.edu/Services/wsUser.asmx?op=EnableUserAccount
    
      //  POST https://bplogix.school.edu/Services/wsUser.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/EnableUserAccount
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <EnableUserAccount xmlns="http://www.bplogix.com/WebServices">
      //        <UID>def456</UID>
      //      </EnableUserAccount>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <EnableUserAccount xmlns="http://www.bplogix.com/WebServices">
              <UID>%s</UID>
            </EnableUserAccount>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, bplogixUid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/EnableUserAccount").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        // 200 success
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <EnableUserAccountResponse xmlns="http://www.bplogix.com/WebServices">
        //        <EnableUserAccountResult>true</EnableUserAccountResult>
        //      </EnableUserAccountResponse>
        //    </soap:Body>
        //  </soap:Envelope>
        boolean enabled = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/EnableUserAccountResponse/EnableUserAccountResult", document, XPathConstants.STRING));
        return enabled;
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    }

    /**
     * rmove bp logix user from all groups
     * @param subjectId
     * @return true if enabled
     */
    public void removeBplogixUserFromAllGroups(String bplogixUid) {
      
      
      retrieveWsAuthnToken();
    
      // DOC: https://bplogix.school.edu/Services/wsUser.asmx?op=RemoveUserFromAllGroups 
    
      //  POST https://bplogix.school.edu/Services/wsUser.asmx
      //  Host: bplogix.school.edu
      //  Content-Type: text/xml; charset=utf-8
      //  Content-Length: length
      //  SOAPAction: http://www.bplogix.com/WebServices/RemoveUserFromAllGroups
      //  
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>string</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <RemoveUserFromAllGroups xmlns="http://www.bplogix.com/WebServices">
      //        <UID>055dc291-cdb6-4b1f-bb7e-d9458bcf6163</UID>
      //      </RemoveUserFromAllGroups>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <RemoveUserFromAllGroups xmlns="http://www.bplogix.com/WebServices">
              <UID>%s</UID>
            </RemoveUserFromAllGroups>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, bplogixUid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/RemoveUserFromAllGroups").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        // 200 success
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <RemoveUserFromAllGroupsResponse xmlns="http://www.bplogix.com/WebServices" />
        //    </soap:Body>
        //  </soap:Envelope>
        String errorRc = (String)xPath.evaluate("/Envelope/Header/bpSoapHeader/Error/RC", document, XPathConstants.STRING);
        GrouperUtil.assertion("Good".equals(errorRc), "Error removing user from all groups: " + errorRc + ", uid: " 
            + bplogixUid + ", " + grouperHttpClient.getResponseBody());
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    
    }

    /**
     * get a gid for a group
     * @return the gid and put in map in thestate
     */
    public String retrieveBplogixGroupGid(String groupExtension) {
      
      retrieveWsAuthnToken();
    
      // DOC: https://bplogix.school.edu/Services/wsGroup.asmx?op=GetGroupByName 
    
      //  POST /Services/wsGroup.asmx HTTP/1.1
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/GetGroupByName
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <GetGroupByName xmlns="http://www.bplogix.com/WebServices">
      //        <GroupName>ALL.FAC</GroupName>
      //      </GetGroupByName>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
      <?xml version="1.0" encoding="utf-8"?>
      <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Header>
          <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
            <AuthToken>%s</AuthToken>
          </bpSoapHeader>
        </soap:Header>
        <soap:Body>
          <GetGroupByName xmlns="http://www.bplogix.com/WebServices">
            <GroupName>%s</GroupName>
          </GetGroupByName>
        </soap:Body>
      </soap:Envelope>
          """.formatted(this.wsAuthnToken, groupExtension);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsGroup.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/GetGroupByName").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        // 200 success
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <GetGroupByNameResponse xmlns="http://www.bplogix.com/WebServices">
        //        <GetGroupByNameResult>
        //          <GID>80e48834-33d6-4fa2-ae1d-22c5060c85cc</GID>
        //          <GroupName>ALL.FAC</GroupName>
        //          <Description />
        //        </GetGroupByNameResult>
        //      </GetGroupByNameResponse>
        //    </soap:Body>
        //  </soap:Envelope>
        String gid = (String)xPath.evaluate("/Envelope/Body/GetGroupByNameResponse/GetGroupByNameResult/GID", document, XPathConstants.STRING);
        GrouperUtil.assertion(!StringUtils.isBlank(gid), "GID is not found for extension: '" + groupExtension + "'");
        
        return gid;
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    
      
    }

    /**
     * get the task count
     * @param subjectId
     * @return number of tasks
     */
    public int retrieveBplogixTaskCount(String bplogixUid) {
      
      retrieveWsAuthnToken();
    
      //  DOC: https://bplogix.school.edu/Services/wsUtil.asmx?op=GetUserTasksByUID
      //
      //  POST https://bplogix.school.edu/Services/wsUtil.asmx
      //  Content-Type: text/xml; charset=utf-8
      //  SOAPAction: http://www.bplogix.com/WebServices/GetUserTasksByUID
      //
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>abc123</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <GetUserTasksByUID xmlns="http://www.bplogix.com/WebServices">
      //        <UID>string</UID>
      //      </GetUserTasksByUID>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <GetUserTasksByUID xmlns="http://www.bplogix.com/WebServices">
              <UID>%s</UID>
            </GetUserTasksByUID>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, bplogixUid);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUtil.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/GetUserTasksByUID").assignAssertResponseCode(200).
          assignBody(xml).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      try {
    
        // 200 success
        //  <?xml version="1.0" encoding="utf-8"?>
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        //    <soap:Header>
        //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
        //        <AuthToken>abc123</AuthToken>
        //        <ThrowErrors>true</ThrowErrors>
        //        <Error>
        //          <RC>Good</RC>
        //        </Error>
        //      </bpSoapHeader>
        //    </soap:Header>
        //    <soap:Body>
        //      <GetUserTasksByUIDResponse xmlns="http://www.bplogix.com/WebServices">
        //        <GetUserTasksByUIDResult>
        //          <string>https://bplogix.school.edu/pd.aspx?tlid=ff8d9385-5506-4c36-9eb2-3695da53bd7a&amp;prinstid=cda4b4dc-1f03-45c2-a880-f3b25f630e65&amp;actuinstid=e489d7e7-0b85-43d2-a81f-2d620d12cc03</string>
        //          <string>https://bplogix.school.edu/pd.aspx?tlid=37b79dbe-94e3-4030-b5d5-5d428d399e49&amp;prinstid=4c9dd755-2b8f-4fd9-856d-ddb89dfe6c86&amp;actuinstid=fa4d96d8-8185-4572-b6d4-6cf597b3a735</string>
        //          <string>https://bplogix.school.edu/pd.aspx?tlid=ca7ba589-dcd7-40b3-96b6-eb762412c479&amp;prinstid=57755e55-ca8b-4e85-b81d-1214055bb57b&amp;actuinstid=1e917e42-8cc7-47f4-856f-6426215083fb</string>
        //          <string>https://bplogix.school.edu/pd.aspx?tlid=8fc7e408-5897-44cc-ba75-a8879741f118&amp;prinstid=c456bb1d-a95a-4622-a901-a27c85ab8a69&amp;actuinstid=5fd08661-1c5a-4baa-aa2f-d18601fbd5c7</string>
        //          <string>https://bplogix.school.edu/pd.aspx?tlid=d2e6c6ae-d635-4b5c-9e3a-81d98128f193&amp;prinstid=12e99185-46c5-4449-a36a-e954f06d38dd&amp;actuinstid=d8ed5210-5abd-46b7-be57-349da59a07d2</string>
        //        </GetUserTasksByUIDResult>
        //      </GetUserTasksByUIDResponse>
        //    </soap:Body>
        //  </soap:Envelope>
        NodeList nodeList = (NodeList)xPath.compile("/Envelope/Body/GetUserTasksByUIDResponse/GetUserTasksByUIDResult/string").evaluate(document, XPathConstants.NODESET);
        int taskCount = nodeList == null ? 0 : nodeList.getLength();
        return taskCount;
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }
    }

    /**
     * see if a user exists in BPLogix
     * @param subjectId
     * @return true if there false if not
     */
    public BplogixUser retrieveBplogixUser(String eppn) {
            
      retrieveWsAuthnToken();
    
      // POST https://bplogix.school.edu/Services/wsUser.asmx
      // Content-Type: text/xml; charset=utf-8
      // SOAPAction: http://www.bplogix.com/WebServices/GetUserByUserID
      //  <?xml version="1.0" encoding="utf-8"?>
      //  <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
      //    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" >
      //    <soap:Header>
      //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
      //        <AuthToken>{authnToken}</AuthToken>
      //      </bpSoapHeader>
      //    </soap:Header>
      //    <soap:Body>
      //      <GetUserByUserID xmlns="http://www.bplogix.com/WebServices">
      //        <UserID>mchyzer@upenn.edu</UserID>
      //      </GetUserByUserID>
      //    </soap:Body>
      //  </soap:Envelope>
      
      String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
          xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" >
          <soap:Header>
            <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
              <AuthToken>%s</AuthToken>
            </bpSoapHeader>
          </soap:Header>
          <soap:Body>
            <GetUserByUserID xmlns="http://www.bplogix.com/WebServices">
              <UserID>%s</UserID>
            </GetUserByUserID>
          </soap:Body>
        </soap:Envelope>
          """.formatted(this.wsAuthnToken, eppn);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(this.wsUrl + "/Services/wsUser.asmx").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "text/xml; charset=utf-8").
          addHeader("SOAPAction", "http://www.bplogix.com/WebServices/GetUserByUserID").
          assignBody(xml).executeRequest();
      
      if (grouperHttpClient.getResponseCode() != 200 && grouperHttpClient.getResponseCode() != 500) {
        throw new RuntimeException(
            "Error calling BPLogix: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody());
      }
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      BplogixUser bplogixUser = new BplogixUser();
      
      try {
    
        if (grouperHttpClient.getResponseCode() == 500) {
          
          // 500 not found
          //  <?xml version="1.0" encoding="utf-8"?>
          //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
          //    <soap:Body>
          //      <soap:Fault>
          //        <faultcode>soap:Server</faultcode>
          //        <faultstring>BP_INVALID_ID: Process Director SDK API error: GetUserByUserID, Data: mchyzersdf@upenn.edu</faultstring>
          //        <detail />
          //      </soap:Fault>
          //    </soap:Body>
          //  </soap:Envelope>
          String faultString = (String) xPath.evaluate("/Envelope/Body/Fault/faultstring", document, XPathConstants.STRING);
          if (StringUtils.contains(faultString, "BP_INVALID_ID")) {
            return null;
          }
          throw new RuntimeException("Error calling BPLogix for retrieve user: " + grouperHttpClient.getResponseCode() + ", " + faultString + ", " + grouperHttpClient.getResponseBody());
    
        } else if (grouperHttpClient.getResponseCode() == 200) {
          
          // 200 success
          //  <?xml version="1.0" encoding="utf-8"?>
          //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
          //    <soap:Header>
          //      <bpSoapHeader xmlns="http://www.bplogix.com/WebServices">
          //        <AuthToken>abc123</AuthToken>
          //        <ThrowErrors>true</ThrowErrors>
          //        <Error>
          //          <RC>Good</RC>
          //        </Error>
          //      </bpSoapHeader>
          //    </soap:Header>
          //    <soap:Body>
          //      <GetUserByUserIDResponse xmlns="http://www.bplogix.com/WebServices">
          //        <GetUserByUserIDResult>
          //          <UID>ea57968f-424b-4dbf-a30e-9dd3e53d057b</UID>
          //          <AuthType>SAML</AuthType>
          //          <ExternalGUID>mchyzer@upenn.edu</ExternalGUID>
          //          <UserName>Chris Hyzer</UserName>
          //          <UserID>mchyzer@upenn.edu</UserID>
          //          <Email>mchyzer@upenn.edu</Email>
          //          <Title>Savant</Title>
          //          <Phone />
          //          <Description />
          //          <Company />
          //          <Office />
          //          <Dept>isc</Dept>
          //          <BusinessUnit />
          //          <LegalEntity />
          //          <CustomString>10021368</CustomString>
          //          <CustomString2 />
          //          <CustomNumber>0</CustomNumber>
          //          <CustomDate>1798-12-31T16:00:00-08:00</CustomDate>
          //          <Culture />
          //          <AutoDST>false</AutoDST>
          //          <TimeZone />
          //          <LoggedIn>false</LoggedIn>
          //          <AvgLoginSecs>31053</AvgLoginSecs>
          //          <AcctLocked>false</AcctLocked>
          //          <AcctDisabled>false</AcctDisabled>
          //          <Groups />
          //        </GetUserByUserIDResult>
          //      </GetUserByUserIDResponse>
          //    </soap:Body>
          //  </soap:Envelope>
          bplogixUser.uid = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/UID", document, XPathConstants.STRING);
          if (StringUtils.isBlank(bplogixUser.uid)) {
            throw new RuntimeException("Error calling BPLogix for retrieve user uid: " + grouperHttpClient.getResponseCode() + ", " + bplogixUser.uid + ", " + grouperHttpClient.getResponseBody());
          }
          bplogixUser.authType = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/AuthType", document, XPathConstants.STRING);
          bplogixUser.externalGUID = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/ExternalGUID", document, XPathConstants.STRING);
          bplogixUser.userName = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/UserName", document, XPathConstants.STRING);
          bplogixUser.userId = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/UserID", document, XPathConstants.STRING);
          bplogixUser.email = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/Email", document, XPathConstants.STRING);
          bplogixUser.title = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/Title", document, XPathConstants.STRING);
          bplogixUser.dept = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/Dept", document, XPathConstants.STRING);
          bplogixUser.customString = (String) xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/CustomString", document, XPathConstants.STRING);
          bplogixUser.autoDST = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/AutoDST", document, XPathConstants.STRING));
          bplogixUser.acctLocked = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/AcctLocked", document, XPathConstants.STRING));
          bplogixUser.acctDisabled = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/AcctDisabled", document, XPathConstants.STRING));
          bplogixUser.loggedIn = GrouperUtil.booleanValue(xPath.evaluate("/Envelope/Body/GetUserByUserIDResponse/GetUserByUserIDResult/LoggedIn", document, XPathConstants.STRING));
          
        }
        
      } catch (XPathExpressionException e) {
        throw new RuntimeException("Error getting xpath: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody(), e);
      }

      return bplogixUser;
    }

    /**
     * if the token is retrieved, then use it, otherwise get one.  put it in the array.
     */
    public void retrieveWsAuthnToken() {
      if (!StringUtils.isBlank(this.wsAuthnToken) && wsAuthTokenRetrievedMillis > System.currentTimeMillis() - 1000 * 60 * 5) {
        return;
      }
      
      // POST https://bplogix.school.edu/Services/wsAdmin.asmx/AuthenticateJSON
      // Content-Type: application/x-www-form-urlencoded
      // userid=myUser&password=abc123
      // 200
      // <?xml version="1.0" encoding="utf-8"?>
      // <string xmlns="http://www.bplogix.com/WebServices">abc123</string>
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignUrl(wsUrl + "/Services/wsAdmin.asmx/AuthenticateJSON").
          assignGrouperHttpMethod("POST").addHeader("Content-Type", "application/x-www-form-urlencoded").
          assignBody("userid=" + wsUser + "&password=" + wsPass).assignAssertResponseCode(200).executeRequest();
      
      Document document = null;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(grouperHttpClient.getResponseBody().getBytes(StandardCharsets.UTF_8));
      
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Error parsing doc: " + grouperHttpClient.getResponseBody(), e);
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      
      XPath xPath = XPathFactory.newInstance().newXPath();
      try {
        wsAuthnToken = (String) xPath.evaluate("/string", document, XPathConstants.STRING);
      } catch (XPathExpressionException e) {
        // ignore
      }
      if (StringUtils.isBlank(this.wsAuthnToken)) {
        throw new RuntimeException("Did not get a token back from BPLogix: " + grouperHttpClient.getResponseBody());
      }
      wsAuthTokenRetrievedMillis = System.currentTimeMillis();
    }
    
  }
  
  public Set<String> groupHasPennids(String groupName, Collection<String> pennids) {
    int batchSize = 1000;
    List<String> pennidsList = new ArrayList<String>(pennids);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(pennidsList, batchSize, false);
    Set<String> pennIdsExistTotal = new HashSet<String>();
    
    // go through in batches
    for (int i=0;i<numberOfBatches;i++) {
      List<String> pennidsBatch = GrouperUtil.batchList(pennidsList, batchSize, i);
      List<String> pennIdsExist = new GcDbAccess().sql(
          "select subject_id from grouper_memberships_lw_v gmlv where " +
          " list_name = 'members' and subject_source = 'pennperson' " + 
          " and group_name = ? and subject_id in (" + 
          GrouperClientUtils.appendQuestions(pennidsBatch.size()) + ")").
          addBindVar(groupName).addBindVars(pennidsBatch).selectList(String.class);
      
      pennIdsExistTotal.addAll(pennIdsExist);
      
    }
  
    return pennIdsExistTotal;
  }

  
  
  @Override
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    super.setGrouperProvisioner(grouperProvisioner1);
    this.initDao();
  }



  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    
  }

  public TheState theState = null;
  
  public void initDao() {
    
    if (this.theState != null) {
      return;
    }
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    String provisionerConfigId = "bpogixStage";
    if (grouperProvisioner != null) {
      provisionerConfigId = grouperProvisioner.getConfigId();
    }
    this.theState = new TheState();
    
    // bplogixProd
    // bplogixStage
    // bplogixDev
    this.theState.initTheState(provisionerConfigId);

  }

  
  /**
   * there is no service to retrieve users in bulk, but there is a feed to sftp a user file and
   * it is loaded with grouper into a table, so just take the last load of users
   */
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = new TargetDaoRetrieveAllEntitiesResponse();
    List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
    
    List<String> eppns = new GcDbAccess().sql("select eppn from " + this.theState.userTableName).selectList(String.class);
    
    for (String eppn : eppns) {
      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
      provisioningEntity.setId(eppn);
      provisioningEntities.add(provisioningEntity);
    }
    
    targetDaoRetrieveAllEntitiesResponse.setTargetEntities(provisioningEntities);
    
    return targetDaoRetrieveAllEntitiesResponse;
  }

  /**
   * retrieve a single entity from bplogix
   */
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    initDao();
    GrouperUtil.assertion(StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute()), 
        "Search attribute must be 'id', but was: " + targetDaoRetrieveEntityRequest.getSearchAttribute());
    String eppn = (String)targetDaoRetrieveEntityRequest.getSearchAttributeValue();
    
    BplogixUser bplogixUser = this.theState.bplogixCommands.retrieveBplogixUser(eppn);
    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRetrieveUserCount", 1);

    TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse();

    if (bplogixUser != null ) {

      // keep this in our cache
      this.theState.eppnToBplogixUser.put(eppn, bplogixUser);
      
      // if they are not valid then it is like they arent there
      if (!bplogixUser.acctDisabled && !bplogixUser.acctLocked && StringUtils.equals(bplogixUser.authType, "SAML")) {
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(eppn);
        targetDaoRetrieveEntityResponse.setTargetEntity(provisioningEntity);
      }

    }
    return targetDaoRetrieveEntityResponse;
    
  }

  public BplogixUser retrieveEntityOrFromCache(String eppn) {
    BplogixUser bplogixUser = this.theState.eppnToBplogixUser.get(eppn);
    if (bplogixUser == null) {
      
      bplogixUser = this.theState.bplogixCommands.retrieveBplogixUser(eppn);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRetrieveUserCount", 1);

      this.theState.eppnToBplogixUser.put(eppn, bplogixUser);
    }
    return bplogixUser;
  }
  
  public String retrieveGidOrFromCache(String groupName) {
    String extension = GrouperUtil.extensionFromName(groupName);
    String gid = this.theState.groupExtensionToBplogixGid.get(extension);
    if (StringUtils.isBlank(gid)) {
      gid = this.theState.bplogixCommands.retrieveBplogixGroupGid(extension);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRetrieveGroupCount", 1);

      this.theState.groupExtensionToBplogixGid.put(extension, gid);
    }
    return gid;
  }
  
  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    initDao();
    
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    String eppn = targetEntity.getId();
    
    try {
      BplogixUser bplogixUser = this.retrieveEntityOrFromCache(eppn);
      
      // if its null then no problem
      if (bplogixUser != null) {
        
        //  If AcctDisabled = false then
        if (!bplogixUser.acctDisabled) {
  
          //  Check User Assigned Tasks 
          int taskCount = this.theState.bplogixCommands.retrieveBplogixTaskCount(bplogixUser.uid);
          GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRetrieveTasksCount", 1);

          
          //  If anything other than null returns, user cannot be disabled due to active Tasks
          if (taskCount == 0) {
          
            //  Remove User from Groups
            this.theState.bplogixCommands.removeBplogixUserFromAllGroups(bplogixUser.uid);
            GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRemoveMembershipsCount", 1);
            
            //
            //  Disable User
            bplogixUser.acctDisabled = this.theState.bplogixCommands.disableBplogixUser(bplogixUser.uid);  
            GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixDisableCount", 1);

           
            if (!bplogixUser.acctDisabled) {
              // add log row
              addLogRow(eppn, "DISABLE_FAIL", "Disable did not succeed");

              throw new RuntimeException("Error disabling user: " + eppn);
            }
            
            removeBplogixUserRow(eppn);
            
            //  add log row
            addLogRow(eppn, "DISABLE_SUCCESS", "Disable succeeded");
            
          } else {
            //  If AcctDisabled = true then nothing needed
            GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixDeleteUserHasTaskCount", 1);          }
  
        } else {
          removeBplogixUserRow(eppn);
          //  User not found, thats good
          GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixDeleteUserCouldNotFind", 1);
        }
        
      } else {
        removeBplogixUserRow(eppn);
      }
      
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
    } catch (Exception exception) {
      // exception.printStackTrace();
      // add log row
      addLogRow(eppn, "DISABLE_FAIL", GrouperUtil.getFullStackTrace(exception));
      
      targetEntity.setProvisioned(false);
      targetEntity.setException(exception);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
    }
    
    TargetDaoDeleteEntityResponse targetDaoDeleteEntityResponse = new TargetDaoDeleteEntityResponse();
    return targetDaoDeleteEntityResponse;
  }

  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(
      TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    // the provisioner will use valid emails
    return insertEntitiesHelper(targetDaoInsertEntitiesRequest, true);
  }
  
  /**
   * 
   * @param targetDaoInsertEntitiesRequest
   * @param validEmail if false then append .fake12 to the email address
   * @return
   */
  public TargetDaoInsertEntitiesResponse insertEntitiesHelper(
      TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest, boolean validEmail) {

    initDao();
    
    Set<String> eppns = new HashSet<String>();
    
    for (ProvisioningEntity targetEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) {
      eppns.add(targetEntity.getId());
    }

    Map<String, Subject> eppnToSubject = SubjectFinder.findByIdentifiers(eppns, "pennperson");

    Map<String, String> eppnToPennid = new HashMap<String, String>();
    for (String eppn : eppnToSubject.keySet()) {
      Subject subject = eppnToSubject.get(eppn);
      eppnToPennid.put(eppn, subject.getId());
    }

    // these are the roles they will get in bplogix, but not the group checked in grouper (which might be blank or not exist)
    String groupNameAllFac = "penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.FAC";
    String gidAllFac = this.retrieveGidOrFromCache(groupNameAllFac);
    
    String groupNameAllStf = "penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.STF";
    String gidAllStf = this.retrieveGidOrFromCache(groupNameAllStf);
    
    String groupNameAllStu = "penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.STU";    
    String gidAllStu = this.retrieveGidOrFromCache(groupNameAllStu);
    
    String groupNameAllAlum = "penn:isc:ait:apps:bplogix:service:policy:userFeedGroups:ALL.ALUM";
    String gidAllAlum = this.retrieveGidOrFromCache(groupNameAllAlum);
    
    Collection<String> pennids = eppnToPennid.values();
    
    // penn:isc:ait:apps:bplogix:service:ref:bplogixFaculty
    Set<String> facPennids = groupHasPennids("penn:isc:ait:apps:bplogix:service:ref:bplogixFaculty", pennids);
    
    // penn:isc:ait:apps:bplogix:service:ref:bplogixStaff
    Set<String> stfPennids = groupHasPennids("penn:isc:ait:apps:bplogix:service:ref:bplogixStaff", pennids);
    
    // penn:isc:ait:apps:bplogix:service:ref:bplogixStudent
    Set<String> stuPennids = groupHasPennids("penn:isc:ait:apps:bplogix:service:ref:bplogixStudent", pennids);
    
    // penn:isc:ait:apps:bplogix:service:ref:bplogixAlum
    Set<String> alumPennids = groupHasPennids("penn:isc:ait:apps:bplogix:service:ref:bplogixAlum", pennids);
    
    for (ProvisioningEntity targetEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) {
      
      try {
        String eppn = targetEntity.getId();
        String pennnid = eppnToPennid.get(eppn);
        Subject subject = eppnToSubject.get(eppn);
  
        if (StringUtils.isBlank(pennnid) || subject == null || StringUtils.isBlank(subject.getAttributeValue("eppn"))) {
          throw new RuntimeException("Error getting pennid for eppn: " + eppn);
        }
        
        //  Does User Exist? Call GetUserByUserID
        //
        //  pass in EPPN
        //  returns User Object.UID or null
        BplogixUser bplogixUser = this.retrieveEntityOrFromCache(eppn);
  
        //   If UserObject then
        if (bplogixUser != null) {
          
          if (bplogixUser.acctDisabled) {

            //  is AcctDisabled = true and AcctType = SAML?
            if (StringUtils.equals(bplogixUser.authType, "SAML")) {
              //  Enable the Account using wsUser.asmx?op=EnableUserAccount 
              //  pass in UID
              //  returns boolean
              bplogixUser.acctDisabled = ! this.theState.bplogixCommands.enableBplogixUser(bplogixUser.uid);
              GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixEnableCount", 1);
  
              //  If Enable Account return = true (success)
              if (!bplogixUser.acctDisabled) {
  
                addBplogixUserRow(eppn, false);
                
                //  Remove User from Groups
                this.theState.bplogixCommands.removeBplogixUserFromAllGroups(bplogixUser.uid);
                GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixRemoveMembershipsPreEnableCount", 1);

                //  Iterate over User Groups and check that they are in the core groups
                addUserToGroups(gidAllFac, gidAllStf, gidAllStu, gidAllAlum, facPennids,
                    stfPennids, stuPennids, alumPennids, pennnid, bplogixUser);
  
                // add log row
                addLogRow(eppn, "ENABLE_SUCCESS", "Enable succeeded");
                
              } else {
                
                // add log row
                addLogRow(eppn, "ENABLE_FAIL", "Enable did not succeed");
                
                //  If Enable Account return = false, error and notify someone
                throw new RuntimeException("Error enabling user: " + eppn);
              }
            } else {
              throw new RuntimeException("Error enabling user: " + eppn + ", not SAML");
            }
          } else {
            //
            //  is AcctDisabled = false and AcctType = SAML?
            if (StringUtils.equals(bplogixUser.authType, "SAML")) {

              //  Iterate over User Groups and check that they are in the core groups
              addUserToGroups(gidAllFac, gidAllStf, gidAllStu, gidAllAlum, facPennids,
                  stfPennids, stuPennids, alumPennids, pennnid, bplogixUser);
              
              // add log row
              addLogRow(eppn, "ENABLE_SAML_ADD_GROUPS", "Enable noop");

            } else {
              
              // add log row
              addLogRow(eppn, "ACCOUNT_ENABLED_BUT_NOT_SAML", "Enable did not succeed");
              
              //  is AcctDisabled = false and AcctType != SAML?
              //  Error
              throw new RuntimeException("Error enabling enabled user: " + eppn + ", not SAML");
            }
            
          }
          
        } else {
          //  if UserObject is Null 
          //  Add User using wsUser.asmx?op=CreateExternalUser which should return an enabled User Object with UID 
          //  UserID = EPPN
          //  Email = Current Email
          //  UserName = First Name<space>Last Name
          //  GUID = EPPN
          //  UserType = “SAML”
          String emailAddress = GrouperEmail.retrieveEmailAddress(subject);
          if (!validEmail && StringUtils.isNotBlank(emailAddress)) {
            emailAddress = emailAddress + ".fake12";
          }
          String uid = this.theState.bplogixCommands.createBplogixUser(eppn, emailAddress, subject.getName());

          this.theState.bplogixCommands.updateBplogixUserPennid(uid, pennnid);

          bplogixUser = this.retrieveEntityOrFromCache(eppn);
          
          GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "bplogixCreateCount", 1);
          
          addBplogixUserRow(eppn, false);

          // add log row
          addLogRow(eppn, "CREATE_SUCCESS", "Create succeeded");
          
          //  Add User’s Groups for core active affiliations (i.e. Faculty, Staff, Student, Alumni) 
          addUserToGroups(gidAllFac, gidAllStf, gidAllStu, gidAllAlum, facPennids,
              stfPennids, stuPennids, alumPennids, pennnid, bplogixUser);
  
        }
        
        targetEntity.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }

      } catch (Exception exception) {
        
        // add log row
        addLogRow(targetEntity.getId(), "CREATE_FAIL", GrouperUtil.getFullStackTrace(exception));
        
        targetEntity.setProvisioned(false);
        targetEntity.setException(exception);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }

      }

    }      
    return new TargetDaoInsertEntitiesResponse();
    
  }

  /**
   * add user to groups they are in
   * @param gidAllFac
   * @param gidAllStf
   * @param gidAllStu
   * @param gidAllAlum
   * @param facPennids
   * @param stfPennids
   * @param stuPennids
   * @param alumPennids
   * @param pennnid
   * @param bplogixUser
   */
  public void addUserToGroups(String gidAllFac, String gidAllStf, String gidAllStu,
      String gidAllAlum, Set<String> facPennids, Set<String> stfPennids,
      Set<String> stuPennids, Set<String> alumPennids, String pennnid,
      BplogixUser bplogixUser) {

    //  Add User’s core active affiliations (i.e. ALL.FAC, ALL.STF, ALL.STU, ALL.ALUM) to BPL Groups
    //  Get the Group ID (GID) using wsGroup.asmx?op=GetGroupByName 
    //  Iterate over User’s active affiliations and add to existing Groups using wsUser.asmx?op=AddUserToGroup 
    //  pass UID & GID returns boolean
    if (facPennids.contains(pennnid)) {
      this.theState.bplogixCommands.addUserToGroup(bplogixUser.uid, gidAllFac);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(),
          "bplogixAddMembershipsCount", 1);
    }
    if (stfPennids.contains(pennnid)) {
      this.theState.bplogixCommands.addUserToGroup(bplogixUser.uid, gidAllStf);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(),
          "bplogixAddMembershipsCount", 1);
    }
    if (stuPennids.contains(pennnid)) {
      this.theState.bplogixCommands.addUserToGroup(bplogixUser.uid, gidAllStu);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(),
          "bplogixAddMembershipsCount", 1);
    }
    if (alumPennids.contains(pennnid)) {
      this.theState.bplogixCommands.addUserToGroup(bplogixUser.uid, gidAllAlum);
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(),
          "bplogixAddMembershipsCount", 1);
    }
  }

  public void addLogRow(String eppn, String action, String description) {
    //  Insert into bplogix_provisioning_log_stage
    new GcDbAccess().sql(
        "insert into " + this.theState.logTableName + " (username,the_action,reason) values (?,?,?)").
        addBindVar(eppn).addBindVar(action).addBindVar(description).executeSql();

  }

  public void addBplogixUserRow(String eppn, boolean hasTask) {
    
    // see if there is a row already
    int count = new GcDbAccess().sql("select count(*) from " + this.theState.userTableName + " where eppn = ?").addBindVar(eppn).select(int.class);

    if (count == 0) {
      //  Insert into bplogix_provisioning_log_stage
      new GcDbAccess().sql(
          "insert into " + this.theState.userTableName + " (eppn, has_task) values (?,?)").
          addBindVar(eppn).addBindVar(hasTask ? "T" : "F").executeSql();
    }
  }

  public void removeBplogixUserRow(String eppn) {
    //  Insert into bplogix_provisioning_log_stage
    new GcDbAccess().sql(
        "delete from " + this.theState.userTableName + " where eppn = ?").
        addBindVar(eppn).executeSql();

  }

  public static void main(String[] args) {
    
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    
    //GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_provisioner_incremental_bplogixStage");

    
//    BplogixProvisionerDao bplogixProvisionerDao = new BplogixProvisionerDao();
//    
//    // we need this for a debug map
//    bplogixProvisionerDao.setGrouperProvisioner(new GrouperProvisioner() {
//      
//      @Override
//      protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
//        return null;
//      }
//      
//      @Override
//      protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
//        return null;
//      }
//    });
            
    // ed: 10026730
    // chris: 10021368
//    String pennid = "10021368";
//    bplogixProvisionerDao.theState.pennidToSubject = SubjectFinder.findByIds(GrouperUtil.toSet(pennid), "pennperson");
//
//    Subject subject = bplogixProvisionerDao.theState.pennidToSubject.get(pennid);
//    String eppn = subject.getAttributeValue("eppn");
//    BplogixUser bpLogixUser = bplogixProvisionerDao.theState.bplogixCommands.retrieveBplogixUser(eppn);
//    System.out.println("bpLogixUser: " + bpLogixUser);
//
//    String bpLogixUid = bpLogixUser.uid;
//
//    boolean disabled = bplogixProvisionerDao.theState.bplogixCommands.disableBplogixUser(bpLogixUid);
//
//    System.out.println("disabled: " + disabled);
//
//    boolean enabled = bplogixProvisionerDao.theState.bplogixCommands.enableBplogixUser(bpLogixUid);
//
//    System.out.println("enabled: " + enabled);
//
//    int taskCount = bplogixProvisionerDao.theState.bplogixCommands.retrieveBplogixTaskCount(bpLogixUid);
//    
//    System.out.println("taskCount: " + taskCount);
//
//    String gid = bplogixProvisionerDao.theState.bplogixCommands.retrieveBplogixGroupGid("ALL.FAC");
//    
//    System.out.println("gid: " + gid);
//  
//    bplogixProvisionerDao.theState.bplogixCommands.addUserToGroup(bpLogixUid, gid);
//  
//    bplogixProvisionerDao.theState.bplogixCommands.createBplogixUser("mchyzer@upenn.edu", "mchyzer@isc.upenn.edu", "Chris Hyzer");
//    
//    bplogixProvisionerDao.theState.bplogixCommands.removeBplogixUserFromAllGroups("055dc291-cdb6-4b1f-bb7e-d9458bcf6163");
    
//    for (String eppn : new String[] {}) {
//      
//      BplogixUser bplogixUser = bplogixProvisionerDao.retrieveEntityOrFromCache(eppn);
//      if (bplogixUser != null) {
//        System.out.println("bplogixUser: " + bplogixUser);
////        // enable user
////        if (bplogixUser.acctDisabled) {
////          bplogixProvisionerDao.theState.bplogixCommands.enableBplogixUser(bplogixUser.uid);
////        }
//      } else {
//        System.out.println("bplogixUser: null");
//      }
//      
//    }
    
    
    if (OtherJobScript.retrieveFromThreadLocal() == null) {
      System.exit(0);
    }

    
    //  // bulk lookup memberships for all appicable groups
    //  Set<String> automaticLicensePennids = groupHasPennids(theState.groupNameAutomaticLicense, pennids);
    //  Set<String> canClaimLicensePennids = groupHasPennids(theState.groupNameCanClaimLicense, pennids);
    //  Set<String> hasSelfClaimedLicensePennids = groupHasPennids(theState.groupNameHasSelfClaimedLicense, pennids);
    //  Set<String> recentBplogixPennids = groupHasPennids(theState.groupNameRecentBpLogixUser, pennids);
    //  Set<String> usersWithTasksPennids = groupHasPennids(theState.groupNameUsersWithTasks, pennids);
    //  Set<String> usersWithLicensePennids = groupHasPennids(theState.groupNameUsersWithLicense, pennids);
    //  Set<String> usersCanLoginPennids = groupHasPennids(theState.groupNameCanLogIn, pennids);
    //  
    //  theState.pennidToSubject = SubjectFinder.findByIds(pennids, "pennperson");
    //  String email = GrouperEmail.retrieveEmailAddress(subject);
    //  String name = subject.getName();
    //  String eppn = subject.getAttributeValue("eppn");

  }

}
