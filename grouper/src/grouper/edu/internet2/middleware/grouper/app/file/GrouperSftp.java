/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * <pre>
 * sftp files.  use the callback to do multiple operations, or a static call to do one operation
 * 
 * https://spaces.at.internet2.edu/display/Grouper/Grouper+Sftp+files
 * 
 * GrouperSftp.callback("depot", new GrouperSftpCallback() {
 *  
 *   public Object callback(GrouperSftpSession grouperSftpSession) {
 *     grouperSftpSession.sendFile(new File("d:/temp/temp/PennUsers.csv"), "/data01/isc/bplogix/PennUsers.csv");
 *     grouperSftpSession.deleteFile("/data01/isc/bplogix/whatever.txt");
 *     return null;
 *   }
 * });
 * </pre>
 */
public class GrouperSftp {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperSftp.class);


  /**
   * 
   */
  public GrouperSftp() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    // GrouperSftp.sendFile("depot", new File("d:/temp/temp/PennUsers.csv"), "/data01/isc/bplogix/PennUsers.csv");
    
    GrouperSftp.deleteFile("depot", "/data01/isc/bplogix/PennUsers3.csv");
    
    // GrouperSftp.receiveFile("depot", "/data01/isc/bplogix/PennUsers3.csv", new File("d:/temp/temp/PennUsers2.csv"));
    
    // System.out.println(GrouperUtil.toStringForLog(GrouperSftp.listFiles("depot", "/data01/isc/bplogix/")));

    // GrouperSftp.copyFile("depot", "/data01/isc/bplogix/PennUsers.csv", "/data01/isc/bplogix/PennUsers2.csv");
    
    // System.out.println(GrouperSftp.existsFile("depot", "/data01/isc/bplogix/PennUsers.csv"));
    
    //GrouperSftp.moveFile("depot", "/data01/isc/bplogix/PennUsers.csv", "/data01/isc/bplogix/PennUsers3.csv");
    
    //    GrouperSftp.callback("depot", new GrouperSftpCallback() {
    //      
    //      public Object callback(GrouperSftpSession grouperSftpSession) {
    //        grouperSftpSession.sendFile(new File("d:/temp/temp/PennUsers.csv"), "/data01/isc/bplogix/PennUsers.csv");
    //        grouperSftpSession.deleteFile("/data01/isc/bplogix/whatever.txt");
    //        return null;
    //      }
    //    });
  }

  /**
   * call this to do sftp stuff
   * @param configId from grouper.properties
   * @param grouperSftpCallback use anonymous inner class to tranfer files
   * @return object to caller (null if none)
   */
  public static Object callback(String configId, GrouperSftpCallback grouperSftpCallback) {
      
    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("Cant have null configId");
    }
    
    String grouperSftpDirName = GrouperConfig.retrieveConfig().propertyValueString("grouperSftpBaseDirName");
    if (StringUtils.isBlank(grouperSftpDirName)) {
      grouperSftpDirName = GrouperUtil.stripLastSlashIfExists(GrouperUtil.tmpDir()) + File.separator + "grouperSftp";
    }

    grouperSftpDirName = GrouperUtil.stripLastSlashIfExists(grouperSftpDirName);
    
    // add on a unique dir
    grouperSftpDirName = grouperSftpDirName + File.separator + "sftpSession_" 
        + GrouperUtil.timestampToFileString(new Date()) + "_" + GrouperUtil.uniqueId();

    File grouperSftpDir = new File(grouperSftpDirName);
    GrouperUtil.mkdirs(grouperSftpDir);
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long now = System.nanoTime();
    debugMap.put("configId", configId);

    debugMap.put("grouperSftpDirName", grouperSftpDirName);

    StandardFileSystemManager sysManager = new StandardFileSystemManager();

    try {
    
      sysManager.init();

      GrouperSftpSession grouperSftpSession = new GrouperSftpSession();
      grouperSftpSession.setConfigId(configId);
      grouperSftpSession.setDebugMap(debugMap);
      grouperSftpSession.setSysManager(sysManager);

      
      // handle private key
      String privateKeyFilePath = null;
      {
        StringBuilder keyFileContents = new StringBuilder();
        
        for (int i=0;i<10;i++) {

          // lines 1-19
          String grouperSftpPrivateKeyPart = GrouperConfig.retrieveConfig().propertyValueString("grouperSftp.site." + configId + ".secret.privateKey_" + i);
          if (!StringUtils.isBlank(grouperSftpPrivateKeyPart)) {
            if (keyFileContents.length() > 0) {
              keyFileContents.append("\n");
            }
            //use $newline$ in config overlays
            //grouperSftpPrivateKeyPart = StringUtils.replace(grouperSftpPrivateKeyPart, "NEWLINE", "\n");
            grouperSftpPrivateKeyPart = StringUtils.trim(grouperSftpPrivateKeyPart);
            keyFileContents.append(grouperSftpPrivateKeyPart);

          } else {
            break;
          }

        }

        if (keyFileContents.length() > 0) {
          String privateKeyFileString = keyFileContents.toString();
          debugMap.put("keyFileSize", privateKeyFileString.length());

          File privateKeyFile = new File(grouperSftpDirName + File.separator + configId + ".private.key");

          try {
            Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rw-------");
            FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
            Files.createFile(privateKeyFile.toPath(), permissions);
          } catch (Exception e) {
            //well, we tried
            
          }
          
          GrouperUtil.saveStringIntoFile(privateKeyFile, privateKeyFileString);
          privateKeyFilePath = privateKeyFile.getAbsolutePath();
        }

      }
      
      grouperSftpSession.setPrivateKeyFilePath(privateKeyFilePath);

      String host = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperSftp.site." + configId + ".host");
      grouperSftpSession.setHost(host);
      debugMap.put("host", host);


      File knownHostsFile = null;
      // known hosts
      {
      
        // e.g. "server.school.edu ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAIEA3B00cx5W9");
        String hostkey = GrouperConfig.retrieveConfig().propertyValueString("grouperSftp.site." + configId + ".knownHostsEntry");
        
        if (!GrouperUtil.isBlank(hostkey)) {
          debugMap.put("knownHost", StringUtils.abbreviate(hostkey, 50));
          knownHostsFile = new File(grouperSftpDirName + File.separator + configId + ".known_hosts.txt");
          GrouperUtil.saveStringIntoFile(knownHostsFile, hostkey);
          debugMap.put("knownHostsContainsHost", hostkey.contains(host));
        }
      }

      grouperSftpSession.setKnownHostsFile(knownHostsFile);

      String user = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperSftp.site." + configId + ".user");
      grouperSftpSession.setUser(user);
      debugMap.put("user", user);
//      if (filenameRemote.startsWith("/")) {
//        filenameRemote = filenameRemote.substring(1, filenameRemote.length());
//      }
      String passphrase = GrouperConfig.retrieveConfig().propertyValueString("grouperSftp.site." + configId + ".secret.privateKeyPassphrase");
      grouperSftpSession.setPassphrase(passphrase);
      debugMap.put("passphrase?", StringUtils.isBlank(passphrase) ? "<none>" : "yes");

      String password = GrouperConfig.retrieveConfig().propertyValueString(".password");
      grouperSftpSession.setPassword(password);
      debugMap.put("password?", StringUtils.isBlank(password) ? "<none>" : "yes");

      final FileSystemOptions fileSystemOptions = createDefaultOptions(debugMap, configId, privateKeyFilePath, passphrase, knownHostsFile);
      grouperSftpSession.setFileSystemOptions(fileSystemOptions);
      
      return grouperSftpCallback.callback(grouperSftpSession);

      
    } catch (Exception re) {
      
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      if (re instanceof RuntimeException) {
        throw (RuntimeException)re;
      }
      throw new RuntimeException("exception", re);
    } finally {
      if (sysManager !=  null) {
        try {
          sysManager.close();
        } catch (Exception e) {
          debugMap.put("sysManagerCloseException", GrouperUtil.getFullStackTrace(e));
          LOG.error("error", e);
        }
      }
      try {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperSftp.site." + configId + ".deleteTempFilesAfterSession", true)) {
          debugMap.put("deleteDir", true);
          FileUtils.deleteDirectory(grouperSftpDir);
        }
      } catch (Exception e) {
        // dont throw!
        debugMap.put("deleteDirException", GrouperUtil.getFullStackTrace(e));
        LOG.error("Cant delete dir: ", e);
      }
      debugMap.put("tookMillis", ((System.nanoTime() - now) / 1000000L));
      
      if (LOG.isDebugEnabled()) {
        String debugString = GrouperClientUtils.mapToString(debugMap);
        LOG.debug(debugString);
      }
    }
  }
  
  /**
   * 
   * @param debugMap 
   * @param configId 
   * @param keyPath
   * @param passPhrase
   * @param knownHostsFile
   * @return the file system options
   */
  private static FileSystemOptions createDefaultOptions(Map<String, Object> debugMap, String configId, final String keyPath, final String passPhrase, File knownHostsFile) {

    //create options for sftp
    FileSystemOptions options = new FileSystemOptions();
    //ssh key
    try {
      SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "yes");
    } catch (org.apache.commons.vfs2.FileSystemException fse) {
      throw new RuntimeException("error", fse);
    }
    try {
      SftpFileSystemConfigBuilder.getInstance().setKnownHosts(options, knownHostsFile);
    } catch (FileSystemException fse) {
      throw new RuntimeException("error", fse);
    }
    //set root directory to user home
    SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(options, false);
    //timeout
    int timeout = GrouperConfig.retrieveConfig().propertyValueInt("grouperSftp.site." + configId + ".timeoutMillis", 10000);
    debugMap.put("timeoutMillis", timeout);

    SftpFileSystemConfigBuilder.getInstance().setSessionTimeoutMillis(options, timeout);

    if (keyPath != null) {
      IdentityInfo identityInfo = null;
      if(passPhrase!=null){
        identityInfo = new IdentityInfo(new File(keyPath), passPhrase.getBytes());
      } else {
        identityInfo =  new IdentityInfo(new File(keyPath));
      }
      try {
        SftpFileSystemConfigBuilder.getInstance().setIdentityProvider(options, identityInfo);
      } catch (org.apache.commons.vfs2.FileSystemException fse) {
        throw new RuntimeException("error", fse);
      }
    }


    return options;
  }

  /**
   * copy file
   * @param configId 
   * @param filenameRemoteFrom
   * @param filenameRemoteTo
   */
  public static void copyFile(String configId, final String filenameRemoteFrom, final String filenameRemoteTo) {
    callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        grouperSftpSession.copyFile(filenameRemoteFrom, filenameRemoteTo);
        return null;
      }
    });
  }

  /**
   * delete file
   * @param configId 
   * @param filenameRemote 
   * @return true if object deleted
   */
  public static boolean deleteFile(String configId, final String filenameRemote) {
    
    return (Boolean)callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        return grouperSftpSession.deleteFile(filenameRemote);
      }
    });
  }

  /**
   * exists file
   * @param configId 
   * @param filenameRemote 
   * @return true if object exists
   */
  public static boolean existsFile(String configId, final String filenameRemote) {
    
    return (Boolean)callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        return grouperSftpSession.existsFile(filenameRemote);
      }
    });
  }

  /**
   * list files in directory
   * @param configId 
   * @param filenameRemote 
   * @return the list of paths doesnt return null;
   */
  public static List<String> listFiles(String configId, final String filenameRemote) {
    
    return (List<String>)callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        return grouperSftpSession.listFiles(filenameRemote);
      }
    });
  }

  /**
   * rename file
   * @param configId 
   * @param filenameRemoteFrom
   * @param filenameRemoteTo
   */
  public static void moveFile(String configId, final String filenameRemoteFrom, final String filenameRemoteTo) {
    callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        grouperSftpSession.moveFile(filenameRemoteFrom, filenameRemoteTo);
        return null;
      }
    });

  }

  /**
   * receive file
   * @param configId 
   * @param fileToReceive
   * @param filenameRemote 
   */
  public static void receiveFile(String configId, final String filenameRemote, final File fileToReceive) {
    callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        grouperSftpSession.receiveFile(filenameRemote, fileToReceive);
        return null;
      }
    });
    
  }

  /**
   * send file
   * @param configId 
   * @param fileToSend
   * @param filenameRemote 
   */
  public static void sendFile(String configId,  final File fileToSend, final String filenameRemote) {
    
    callback(configId, new GrouperSftpCallback() {
      
      public Object callback(GrouperSftpSession grouperSftpSession) {
        grouperSftpSession.sendFile(fileToSend, filenameRemote);
        return null;
      }
    });
  }

}
