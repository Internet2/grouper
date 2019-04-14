/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 *
 */
public class GrouperReportLogic {

  private static final Log LOG = GrouperUtil.getLog(GrouperReportLogic.class);

  /**
   * run report
   * @param grouperReportConfigurationBean
   */
  public static GrouperReportInstance runReport(GrouperReportConfigurationBean grouperReportConfigurationBean) {
    
    long startTime = System.currentTimeMillis();
    GrouperReportInstance grouperReportInstance = new GrouperReportInstance();

    try {
      // get report data
      GrouperReportData grouperReportData = grouperReportConfigurationBean.getReportConfigType().retrieveReportDataByConfig(grouperReportConfigurationBean);

      grouperReportInstance.setGrouperReportConfigurationBean(grouperReportConfigurationBean);
      grouperReportInstance.setReportInstanceRows(grouperReportData.getData().size());

      // now the file is in the report instance
      grouperReportConfigurationBean.getReportConfigFormat().formatReport(grouperReportData, grouperReportInstance);
      
      long endTime = System.currentTimeMillis();
      grouperReportInstance.setReportElapsedMillis(endTime - startTime);
      grouperReportInstance.setReportInstanceConfigMarkerAssignmentId(grouperReportConfigurationBean.getAttributeAssignmentMarkerId());

      String randomEncryptionKey = RandomStringUtils.random(16, true, true);
      grouperReportInstance.setReportInstanceEncryptionKey(randomEncryptionKey);
      
      String reportDestination = GrouperConfig.retrieveConfig().propertyValueString("reporting.storage.option");
      if (StringUtils.isBlank(reportDestination)) {
        throw new RuntimeException("reporting.storage.option cannot be blank. Valid values are S3 and fileSystem");
      }
      
      if (!reportDestination.equals("S3") || !reportDestination.equals("fileSystem")) {
        throw new RuntimeException("reporting.storage.option cannot be blank. Valid values are S3 and fileSystem");
      }
      
      if (reportDestination.equals("S3")) {
        String s3Url = uploadFileToS3(grouperReportInstance.getReportFileUnencrypted(), randomEncryptionKey);
        grouperReportInstance.setReportInstanceFilePointer(s3Url);
      } else {
        String reportOutputDirectory = GrouperConfig.retrieveConfig().propertyValueString("reporting.file.system.path");
        if (StringUtils.isBlank(reportOutputDirectory)) {          
          throw new RuntimeException("reporting.file.system.path cannot be blank");
        }
        String filePath = saveFileToFileSystem(grouperReportInstance.getReportFileUnencrypted(), randomEncryptionKey, reportOutputDirectory);
        grouperReportInstance.setReportInstanceFilePointer(filePath);
      }
      
      sendReportLinkViaEmail(grouperReportInstance);
      //TODO need to email this out
      //grouperReportInstance.setReportInstanceEmailToSubjects(reportInstanceEmailToSubjects);
      //grouperReportInstance.setReportInstanceEmailToSubjectsError(reportInstanceEmailToSubjectsError);
      
      grouperReportInstance.setReportInstanceMillisSince1970(System.currentTimeMillis());
      grouperReportInstance.setReportInstanceStatus(GrouperReportInstance.STATUS_SUCCESS);
    } catch(Exception e) {
      grouperReportInstance.setReportInstanceStatus(GrouperReportInstance.STATUS_ERROR);
    } finally {
      
      File tmpReportsPath = new File(GrouperUtil.tmpDir() + "reports");
      if (tmpReportsPath.exists()) {        
        FileUtils.deleteQuietly(tmpReportsPath);
      }
    }
    
    return grouperReportInstance;
    
  }
  
  private static void sendReportLinkViaEmail(GrouperReportInstance reportInstance) {
    GrouperReportConfigurationBean configBean = reportInstance.getGrouperReportConfigurationBean();
    
    if (!configBean.isReportConfigSendEmail()) {
      LOG.info("Config send email is set to false. not going to send any emails");
      return;
    }
    
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    
    if (StringUtils.isBlank(uiUrl)) {
      LOG.error("grouper.properties grouper.ui.url is blank/null. Please fix that first. No emails have been sent.");
      return;
    }
    
    boolean sendToViewers = configBean.isReportConfigSendEmailToViewers();
    
    Group emailGroup = null;
    
    if (sendToViewers) {
      String groupId = configBean.getReportConfigViewersGroupId();
      emailGroup = GroupFinder.findByUuid(GrouperSession.startRootSession(), groupId, false);
    } else {
      emailGroup = GroupFinder.findByUuid(GrouperSession.startRootSession(), configBean.getReportConfigSendEmailToGroupId(), false);
    }
    
    if (emailGroup == null) {
      LOG.error("group to send email to for config: "+configBean.getReportConfigName()+ " is null. not sending any emails");
      return;
    }
    
    String subject = reportInstance.getGrouperReportConfigurationBean().getReportConfigEmailSubject();
    if (StringUtils.isBlank(subject)) {
      subject = GrouperConfig.retrieveConfig().getProperty("grouper.report.email.subject", "Subject for report email");
    }
    String templateBody = reportInstance.getGrouperReportConfigurationBean().getReportConfigEmailBody();
    if (StringUtils.isBlank(templateBody)) {
      templateBody = GrouperConfig.retrieveConfig().getProperty("grouper.report.email.body", "Email body for report email");
    }
    
    List<String> emailSuccessSubjects = new ArrayList<String>();
    List<String> emailFailureSubjects = new ArrayList<String>();
    
    Set<Member> members = emailGroup.getMembers();
    for (Member member: members) {
      String emailAddress = GrouperEmailUtils.getEmail(member.getSubject());
      if (StringUtils.isBlank(emailAddress)) {
        LOG.info("For subject: "+member.getSubjectId()+" no email address found.");
        emailFailureSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
        continue;
      }

      try {   
        String emailBody = buildEmailBody(member.getSubject(), templateBody, reportInstance, uiUrl);
        new GrouperEmail().setBody(emailBody).setSubject(subject).setTo(emailAddress).send();
        emailSuccessSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
      } catch (Exception e) {
        emailFailureSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
      }
    }
    
    reportInstance.setReportInstanceEmailToSubjects(StringUtils.join(emailSuccessSubjects, ","));
    reportInstance.setReportInstanceEmailToSubjectsError(StringUtils.join(emailFailureSubjects, ","));
    
  }
  
  private static String buildEmailBody(Subject recipient, String templatedBody, 
      GrouperReportInstance reportInstance, String uiUrl) {
    
    String link = "grouperUi/app/UiV2Main.index?operation=UiV2GrouperReport.viewReportInstanceDetails&attributeAssignId="+reportInstance.getAttributeAssignId();
    
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(reportInstance.getAttributeAssignId(), true);
    Stem stem = attributeAssign.getOwnerStem();
    if (stem == null) {
      stem = attributeAssign.getOwnerAttributeAssign().getOwnerStem(); 
    }
    
    if (stem != null) {
      link = link + "&stemId="+stem.getId();
    } else {
      //must be group
      Group group = attributeAssign.getOwnerGroup();
      group  = group == null ? attributeAssign.getOwnerAttributeAssign().getOwnerGroup(): group;
      link = link + "&groupId="+group.getId();
    }
        
    GrouperReportConfigurationBean reportConfigBean = reportInstance.getGrouperReportConfigurationBean();
    String emailBody = templatedBody.replaceAll("$$reportConfigName$$", reportConfigBean.getReportConfigName());
    emailBody = emailBody.replaceAll("$$reportConfigDescription$$", reportConfigBean.getReportConfigDescription());
    emailBody = emailBody.replaceAll("$$reportLink$$", uiUrl+link);    
    emailBody = emailBody.replaceAll("$$subjectName$$", recipient.getName());
    
    return emailBody;
  }
  
  
  
  public static String getReportContent(GrouperReportInstance reportInstance) {
    
    if (reportInstance.isReportStoredInS3()) {
      //TODO refactor to read only once
      
      String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
      String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
      
      try {
        String encryptionKey = reportInstance.getReportInstanceEncryptionKey();
        SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
        
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        
        AmazonS3URI s3Uri = new AmazonS3URI(reportInstance.getReportInstanceFilePointer());
        AmazonS3 s3Client = AmazonS3EncryptionClientBuilder.standard()
                .withRegion(s3Uri.getRegion())
                .withCredentials(credentialsProvider)
                .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(encryptionMaterials))
                .build();
        
        // Download and decrypt the object.
        S3Object downloadedObject = s3Client.getObject(s3Uri.getBucket(), s3Uri.getKey());     
        byte[] decrypted = com.amazonaws.util.IOUtils.toByteArray(downloadedObject.getObjectContent());
        String reportContent = new String(decrypted);
        return reportContent;
        
      } catch (Exception e) {
        // TODO: handle exception
      }
    } else {
      return getReportContentFromFileSystem(reportInstance);
    }
    
    return "";
    
  }
  
  private static String getReportContentFromFileSystem(GrouperReportInstance reportInstance) {
    
    SecretKey encryptionKeySecret = new SecretKeySpec(reportInstance.getReportInstanceEncryptionKey().getBytes(), "AES");
    
    Cipher cipher;
    try {
      cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, encryptionKeySecret);
      
      FileInputStream inputStream = new FileInputStream(reportInstance.getReportInstanceFilePointer());
      File file = new File(reportInstance.getReportInstanceFilePointer());
      byte[] inputBytes = new byte[(int) file.length()];
      inputStream.read(inputBytes);
       
      byte[] outputBytes = cipher.doFinal(inputBytes);
      inputStream.close();
      return new String(outputBytes);
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return "";
  }
  
  public static void main(String[] args) {
    
    File file = new File("/Users/vsachdeva/Downloads/one_rel_history.csv");
    String encryptionKey = RandomStringUtils.random(16, true, true);
    System.out.println(encryptionKey);
    String url = uploadFileToS3(file, encryptionKey);
    System.out.println(url);
    
  }
  
  private static String saveFileToFileSystem(File file, String encryptionKey, String reportOutputDirectory) {
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    String baseDirectory = reportOutputDirectory.endsWith(File.separator) ? reportOutputDirectory: reportOutputDirectory + File.separator;
    
    String reportDirectoryPath = baseDirectory + "reports" + File.separator + calendar.get(Calendar.YEAR) + File.separator 
        + StringUtils.leftPad(""+(calendar.get(Calendar.MONTH)+1), 2, '0') + File.separator + StringUtils.leftPad(""+calendar.get(Calendar.DAY_OF_MONTH), 2, '0')
        + File.separator + GrouperUtil.uniqueId();
    
    try {
      
      SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
      
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, encryptionKeySecret);
      
      FileInputStream inputStream = new FileInputStream(file);
      byte[] inputBytes = new byte[(int) file.length()];
      inputStream.read(inputBytes);
       
      byte[] outputBytes = cipher.doFinal(inputBytes);
       
      FileOutputStream outputStream = new FileOutputStream(reportDirectoryPath+File.separator+file.getName());
      outputStream.write(outputBytes);
       
      inputStream.close();
      outputStream.close();
      
    } catch (Exception e) {
      // TODO: handle exception
    }
    
    return reportDirectoryPath+File.separator+file.getName();
  }
  
  public static void deleteFromFileSystem(GrouperReportInstance reportInstance) {
    
    String filePath = reportInstance.getReportInstanceFilePointer();
    File file = new File(filePath);
    if (file.exists()) {
      file.delete();
    }
  }
  
  public static void deleteFileFromS3(GrouperReportInstance reportInstance) {
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
    
    try {
      String encryptionKey = reportInstance.getReportInstanceEncryptionKey();
      SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
      EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
      
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
      
      AmazonS3URI s3Uri = new AmazonS3URI(reportInstance.getReportInstanceFilePointer());
      AmazonS3 s3Client = AmazonS3EncryptionClientBuilder.standard()
              .withRegion(s3Uri.getRegion())
              .withCredentials(credentialsProvider)
              .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(encryptionMaterials))
              .build();
      
      s3Client.deleteObject(s3Uri.getBucket(), s3Uri.getKey());
      
    } catch (Exception e) {
      // TODO: handle exception
    }
    
  }
  
  private static String uploadFileToS3(File file, String encryptionKey) {
    
    String bucketName = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.bucket.name");
    String region = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.region");
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
    
    String fileObjKeyName = file.getName();
    
    try {
      
      SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
      EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
      
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
      
      AmazonS3 s3Client = AmazonS3EncryptionClientBuilder.standard()
              .withRegion(region)
              .withCredentials(credentialsProvider)
              .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(encryptionMaterials))
              .build();
      
      // Upload a file as a new object with ContentType and title specified.
      PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, file);
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType("plain/text");
      request.setMetadata(metadata);
      s3Client.putObject(request);
      
      return s3Client.getUrl(bucketName, fileObjKeyName).toString();
    //TODO handle exceptions properly
    } catch(AmazonServiceException e) {
        // The call was transmitted successfully, but Amazon S3 couldn't process 
        // it, so it returned an error response.
        e.printStackTrace();
    } catch(SdkClientException e) {
        // Amazon S3 couldn't be contacted for a response, or the client
        // couldn't parse the response from Amazon S3.
        e.printStackTrace();
    }
    
    return null;
    
    
  }
  
}
