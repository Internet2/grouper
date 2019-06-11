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
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 *
 */
public class GrouperReportLogic {

  private static final Log LOG = GrouperUtil.getLog(GrouperReportLogic.class);

  /**
   * run report
   * @param reportConfigBean
   * @param reportInstance
   * @param owner
   */
  public static void runReport(GrouperReportConfigurationBean reportConfigBean,
      GrouperReportInstance reportInstance, GrouperObject owner) {
    
    long startTime = System.currentTimeMillis();
    File fileToDelete = null;
    try {
      // get report data
      GrouperReportData grouperReportData = reportConfigBean.getReportConfigType().retrieveReportDataByConfig(reportConfigBean);
  
      reportInstance.setGrouperReportConfigurationBean(reportConfigBean);
      reportInstance.setReportInstanceRows(Long.valueOf(grouperReportData.getData().size()));
  
      // now the file is in the report instance
      reportConfigBean.getReportConfigFormat().formatReport(grouperReportData, reportInstance);
        
      long endTime = System.currentTimeMillis();
      reportInstance.setReportElapsedMillis(endTime - startTime);
  
      String randomEncryptionKey = RandomStringUtils.random(16, true, true);
      
      reportInstance.setReportInstanceEncryptionKey(Morph.encrypt(randomEncryptionKey));

      fileToDelete = reportInstance.getReportFileUnencrypted();
      
      String reportDestination = GrouperConfig.retrieveConfig().propertyValueString("reporting.storage.option");
      if (StringUtils.isBlank(reportDestination)) {
        throw new RuntimeException("reporting.storage.option cannot be blank. Valid values are S3 and fileSystem");
      }
      
      if (!reportDestination.equals("S3") && !reportDestination.equals("fileSystem")) {
        throw new RuntimeException("reporting.storage.option is not valid. Use S3 or fileSystem");
      }
        
      if (reportDestination.equals("S3")) {
        String s3Url = uploadFileToS3(reportInstance.getReportFileUnencrypted(), randomEncryptionKey);
        reportInstance.setReportInstanceFilePointer(s3Url);
      } else {
        String reportOutputDirectory = GrouperConfig.retrieveConfig().propertyValueString("reporting.file.system.path");
        if (StringUtils.isBlank(reportOutputDirectory)) {          
          throw new RuntimeException("reporting.file.system.path cannot be blank");
        }
        String filePath = saveFileToFileSystem(reportInstance.getReportFileUnencrypted(), randomEncryptionKey, reportOutputDirectory);
        reportInstance.setReportInstanceFilePointer(filePath);
      }
      
      reportInstance.setReportInstanceFileName(reportInstance.getReportFileUnencrypted().getName());
      reportInstance.setReportInstanceStatus(GrouperReportInstance.STATUS_SUCCESS);
      
    } catch(Exception e) {
      LOG.error("Error occurred generating report for config name "+reportConfigBean.getReportConfigName(), e);
      reportInstance.setReportInstanceStatus(GrouperReportInstance.STATUS_ERROR);
    } finally {
      if (fileToDelete != null && fileToDelete.exists()) {        
        FileUtils.deleteQuietly(fileToDelete);
      }
    }
    
    try {
      GrouperReportInstanceService.saveReportInstanceAttributes(reportInstance, owner);
    } catch(Exception e) {
      LOG.error("Error saving report instance. Config name is "+reportConfigBean.getReportConfigName());
      return;
    }
      
    if (reportInstance.getReportInstanceStatus().equals(GrouperReportInstance.STATUS_SUCCESS)) {
      try {
        //now the attribute assign id on report instance is populated and we can use it to generate link in the email
        sendReportLinkViaEmail(reportInstance);
      } catch(Exception e) {
        LOG.error("Error sending report email. Config name is "+reportConfigBean.getReportConfigName());
        return;
      }
      
      // now the email attributes are populated on the report instance, let's save them as well.
      try {
        GrouperReportInstanceService.saveReportInstanceAttributes(reportInstance, owner);
      } catch(Exception e) {
        LOG.error("Error saving report instance. Config name is "+reportConfigBean.getReportConfigName());
        return;
      }
    }
    
  }
  
  /**
   * send report link in email
   * @param reportInstance
   */
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
    
    String templateSubject = configBean.getReportConfigEmailSubject();
    if (StringUtils.isBlank(templateSubject)) {
      templateSubject = GrouperConfig.retrieveConfig().propertyValueString("reporting.email.subject");
    }
    
    if (StringUtils.isBlank(templateSubject)) {
      templateSubject = "Report $$reportConfigName$$ generated";
    }
    
    String subject = StringUtils.replace(templateSubject, "$$reportConfigName$$", configBean.getReportConfigName());
    
    String templateBody = configBean.getReportConfigEmailBody();
    if (StringUtils.isBlank(templateBody)) {
      templateBody = GrouperConfig.retrieveConfig().propertyValueString("reporting.email.body");
    }
    if (StringUtils.isBlank(templateBody)) {
      templateBody = "Hello $$subjectName$$, \n\n Report $$reportConfigName$$ has been generated. Download the report: $$reportLink$$ \n\n Thanks";
    }
    
    List<String> emailSuccessSubjects = new ArrayList<String>();
    List<String> emailFailureSubjects = new ArrayList<String>();
    
    Set<Member> members = emailGroup.getMembers();
    for (Member member: members) {
      if (StringUtils.equals(member.getSubject().getType().getName(), SubjectTypeEnum.PERSON.getName())) {
        String emailAddress = GrouperEmailUtils.getEmail(member.getSubject());
        if (StringUtils.isBlank(emailAddress)) {
          LOG.info("For subject: "+member.getSubjectId()+" no email address found.");
          emailFailureSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
          continue;
        }

        try {
          String emailBody = buildEmailBody(member.getSubject(), templateBody, reportInstance, uiUrl, configBean);
          new GrouperEmail().setBody(emailBody).setSubject(subject).setTo(emailAddress).send();
          emailSuccessSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
        } catch (Exception e) {
          emailFailureSubjects.add(member.getSubject().getSourceId()+"::::"+member.getSubjectId());
          LOG.error("Error sending report email to "+emailAddress+ " Config name is "+configBean.getReportConfigName());
        }
      }
    }
    
    reportInstance.setReportInstanceEmailToSubjects(StringUtils.join(emailSuccessSubjects, ","));
    reportInstance.setReportInstanceEmailToSubjectsError(StringUtils.join(emailFailureSubjects, ","));
    
  }
  
  /**
   * build personalized email content for recipient
   * @param recipient
   * @param templateBody
   * @param reportInstance
   * @param uiUrl
   * @param configBean
   * @return
   */
  private static String buildEmailBody(Subject recipient, String templateBody, 
      GrouperReportInstance reportInstance, String uiUrl, GrouperReportConfigurationBean configBean) {
    
    String link = null;
    
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(configBean.getAttributeAssignmentMarkerId(), true);
    Stem stem = attributeAssign.getOwnerStem();
    if (stem != null) {
      link = "grouperUi/app/UiV2Main.index?operation=UiV2GrouperReport.viewReportInstanceDetailsForFolder&attributeAssignId="+reportInstance.getAttributeAssignId();
      link = link + "&stemId="+stem.getId();
    } else {
      Group group = attributeAssign.getOwnerGroup();
      link = "grouperUi/app/UiV2Main.index?operation=UiV2GrouperReport.viewReportInstanceDetailsForGroup&attributeAssignId="+reportInstance.getAttributeAssignId();
      link = link + "&groupId="+group.getId();
    }
    
    GrouperReportConfigurationBean reportConfigBean = reportInstance.getGrouperReportConfigurationBean();
    
    String emailBody = StringUtils.replace(templateBody, "$$reportConfigName$$", reportConfigBean.getReportConfigName());
    
    emailBody = StringUtils.replace(emailBody, "$$reportConfigDescription$$", reportConfigBean.getReportConfigDescription());
    
    emailBody = StringUtils.replace(emailBody, "$$reportLink$$", uiUrl+link);
    
    emailBody = StringUtils.replace(emailBody, "$$subjectName$$", recipient.getName());
    
    return emailBody;
  }
  
  
  /**
   * retrieves report content from given report instance
   * @param reportInstance
   * @return
   */
  public static String getReportContent(GrouperReportInstance reportInstance) {
    
    return reportInstance.isReportStoredInS3() ? 
        getReportContentFromS3(reportInstance): getReportContentFromFileSystem(reportInstance);
  }
  
  /**
   * @param reportInstance
   * @return report content from s3 for a given report instance
   */
  private static String getReportContentFromS3(GrouperReportInstance reportInstance) {
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
    
    String encryptionKey = Morph.decrypt(reportInstance.getReportInstanceEncryptionKey());
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
    
    try {
      
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
      return new String(decrypted);
    } catch (Exception e) {
      throw new RuntimeException("Error getting report content from S3", e);
    }
  }
  
  /**
   * @param reportInstance
   * @return report content from file system for a given report instance
   */
  private static String getReportContentFromFileSystem(GrouperReportInstance reportInstance) {
    
    String encryptionKey = Morph.decrypt(reportInstance.getReportInstanceEncryptionKey());
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    
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
      throw new RuntimeException("Error in getting report content from file system", e);
    }
    
  }
  
  /**
   * save report file to file system
   * @param file
   * @param encryptionKey
   * @param reportOutputDirectory
   * @return pointer to the file location
   */
  private static String saveFileToFileSystem(File file, String encryptionKey, String reportOutputDirectory) {
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    String baseDirectory = reportOutputDirectory.endsWith(File.separator) ? reportOutputDirectory: reportOutputDirectory + File.separator;
    
    String reportDirectoryPath = baseDirectory + "reports" + File.separator + calendar.get(Calendar.YEAR) + File.separator 
        + StringUtils.leftPad(""+(calendar.get(Calendar.MONTH)+1), 2, '0') + File.separator + StringUtils.leftPad(""+calendar.get(Calendar.DAY_OF_MONTH), 2, '0')
        + File.separator + GrouperUtil.uniqueId();
    
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    
    try {
      
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, encryptionKeySecret);
      
      FileInputStream inputStream = new FileInputStream(file);
      byte[] inputBytes = new byte[(int) file.length()];
      inputStream.read(inputBytes);
       
      byte[] outputBytes = cipher.doFinal(inputBytes);
      
      GrouperUtil.mkdirs(new File(reportDirectoryPath));
       
      File outputFile = new File(reportDirectoryPath+File.separator+file.getName());
      GrouperUtil.fileCreateNewFile(outputFile);
       
      FileOutputStream outputStream = new FileOutputStream(outputFile);
      outputStream.write(outputBytes);
       
      inputStream.close();
      outputStream.close();
      
    } catch (Exception e) {
      throw new RuntimeException("Error in saving report to file system", e);
    }
    
    return reportDirectoryPath+File.separator+file.getName();
  }
  
  /**
   * delete file from file system for a given report instance
   * @param reportInstance
   */
  public static void deleteFromFileSystem(GrouperReportInstance reportInstance) {
    
    String filePath = reportInstance.getReportInstanceFilePointer();
    File file = new File(filePath);
    if (file.exists()) {
      file.delete();
    }
  }
  
  /**
   * delete file from S3 for a given report instance
   * @param reportInstance
   */
  public static void deleteFileFromS3(GrouperReportInstance reportInstance) {
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
    
    String encryptionKey = Morph.decrypt(reportInstance.getReportInstanceEncryptionKey());
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
    
    try {
      
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
      throw new RuntimeException("Error deleting report file from S3");
    }
    
  }
  
  /**
   * upload given file to file system
   * @param file
   * @param encryptionKey
   * @return s3 url of file 
   */
  private static String uploadFileToS3(File file, String encryptionKey) {
    
    String bucketName = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.bucket.name");
    String region = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.region");
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("reporting.s3.secret.key");
    
    String fileObjKeyName = file.getName();
    
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
    
    try {
      
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
    } catch(AmazonServiceException e) {
      LOG.error("Error uploading report file to S3. file name: "+file.getName());
      throw new RuntimeException("Error uploading file to S3");
    } catch(SdkClientException e) {
      LOG.error("Error uploading report file to S3. file name: "+file.getName());
      throw new RuntimeException("Error uploading file to S3");
    }
    
  }
  
}
