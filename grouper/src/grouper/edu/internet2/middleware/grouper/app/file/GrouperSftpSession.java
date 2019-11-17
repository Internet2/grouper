/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * object to do operations in sftp
 */
public class GrouperSftpSession {

  /**
   * 
   */
  public GrouperSftpSession() {
  }

  /**
   * config id of session
   */
  @SuppressWarnings("unused")
  private String configId;

  /**
   * config id of session
   * @param configId1 the configId to set
   */
  public void setConfigId(String configId1) {
    this.configId = configId1;
  }


  /**
   * debug map to put debug info
   */
  private Map<String, Object> debugMap;
  
  /**
   * debug map to put debug info
   * @param debugMap1 the debugMap to set
   */
  public void setDebugMap(Map<String, Object> debugMap1) {
    this.debugMap = debugMap1;
  }


  /**
   * sftp sys manager
   */
  private StandardFileSystemManager sysManager;
  
  /**
   * sftp sys manager
   * @param sysManager1 the sysManager to set
   */
  public void setSysManager(StandardFileSystemManager sysManager1) {
    this.sysManager = sysManager1;
  }

  /**
   * private key path
   */
  private String privateKeyFilePath;

  
  /**
   * @param privateKeyFilePath the privateKeyFilePath to set
   */
  public void setPrivateKeyFilePath(String privateKeyFilePath) {
    this.privateKeyFilePath = privateKeyFilePath;
  }

  /**
   * known hosts file
   */
  @SuppressWarnings("unused")
  private File knownHostsFile;

  
  /**
   * known hosts file
   * @param knownHostsFile1 the knownHostsFile to set
   */
  public void setKnownHostsFile(File knownHostsFile1) {
    this.knownHostsFile = knownHostsFile1;
  }

  
  /**
   * host to connect to
   */
  private String host;

  /**
   * host to connect to
   * @param host1 the host to set
   */
  public void setHost(String host1) {
    this.host = host1;
  }

  

  /**
   * user to connect as
   */
  private String user;
  
  
  /**
   * user to connect as
   * @param user1 the user to set
   */
  public void setUser(String user1) {
    this.user = user1;
  }

  

  /**
   * passphrase of private key (if not using password)
   */
  private String passphrase;

  /**
   * passphrase of private key (if not using password)
   * @param passphrase1 the passphrase to set
   */
  public void setPassphrase(String passphrase1) {
    this.passphrase = passphrase1;
  }

  /**
   * password of user
   */
  private String password;

  /**
   * password of user
   * @param password1 the password to set
   */
  public void setPassword(String password1) {
    this.password = password1;
  }


  /**
   * file system options of remote system
   */
  private FileSystemOptions fileSystemOptions;

  /**
   * file system options of remote system
   * @param fileSystemOptions1 the fileSystemOptions to set
   */
  public void setFileSystemOptions(FileSystemOptions fileSystemOptions1) {
    this.fileSystemOptions = fileSystemOptions1;
  }

  

  /**
   * keep track of operations in logs
   */
  private int operationIndex = 0;

  
  /**
   * send file
   * @param fileToSend
   * @param filenameRemote 
   */
  public void sendFile(File fileToSend, String filenameRemote) {
    
    if (fileToSend == null) {
      throw new RuntimeException("fileToSend cant be null!");
    }
    if (StringUtils.isBlank(filenameRemote)) {
      throw new RuntimeException("filenameRemote cant be blank!");
    }
    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("sendFileLocal_" + this.operationIndex, fileToSend.getAbsoluteFile());
        this.debugMap.put("sendFileRemote_" + this.operationIndex, filenameRemote);

      }
      final String connectionString = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemote);
  
      FileObject localFile = this.sysManager.resolveFile(fileToSend.getAbsolutePath());
  
      FileObject remoteFile = this.sysManager.resolveFile(connectionString,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error sending file '" + fileToSend.getAbsolutePath()  + "' to file: '" + filenameRemote + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * receive file
   * @param fileToReceive
   * @param filenameRemote 
   */
  public void receiveFile(String filenameRemote, File fileToReceive) {
    
    if (fileToReceive == null) {
      throw new RuntimeException("fileToReceive cant be null!");
    }
    if (StringUtils.isBlank(filenameRemote)) {
      throw new RuntimeException("filenameRemote cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("receiveFileRemote_" + this.operationIndex, filenameRemote);
        this.debugMap.put("receiveFileLocal_" + this.operationIndex, fileToReceive.getAbsoluteFile());

      }
      final String connectionString = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemote);
  
      FileObject localFile = this.sysManager.resolveFile(fileToReceive.getAbsolutePath());
  
      FileObject remoteFile = this.sysManager.resolveFile(connectionString,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error receiving file '" + filenameRemote + "' to file: '" + fileToReceive.getAbsolutePath() + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * delete file
   * @param filenameRemote 
   * @return true if object deleted
   */
  public boolean deleteFile(String filenameRemote) {
    
    if (StringUtils.isBlank(filenameRemote)) {
      throw new RuntimeException("filenameRemote cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("deleteFileRemote_" + this.operationIndex, filenameRemote);

      }
      final String connectionString = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemote);
  
      FileObject remoteFile = this.sysManager.resolveFile(connectionString,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      return remoteFile.delete();
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error deleting file '" + filenameRemote + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * exists file
   * @param filenameRemote 
   * @return true if object exists
   */
  public boolean existsFile(String filenameRemote) {
    
    if (StringUtils.isBlank(filenameRemote)) {
      throw new RuntimeException("filenameRemote cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("existsFileRemote_" + this.operationIndex, filenameRemote);

      }
      final String connectionString = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemote);
  
      FileObject remoteFile = this.sysManager.resolveFile(connectionString,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      return remoteFile.exists();
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error isExists file '" + filenameRemote + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * list files in directory
   * @param filenameRemote 
   * @return the list of paths doesnt return null;
   */
  public List<String> listFiles(String filenameRemote) {
    
    if (StringUtils.isBlank(filenameRemote)) {
      throw new RuntimeException("filenameRemote cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("listFilesRemote_" + this.operationIndex, filenameRemote);

      }
      final String connectionString = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemote);
  
      FileObject remoteFile = this.sysManager.resolveFile(connectionString,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      List<String> paths = new ArrayList<String>();
      FileObject[] fileObjects = remoteFile.findFiles(Selectors.SELECT_CHILDREN);
      for (FileObject fileObject : GrouperUtil.nonNull(fileObjects, FileObject.class)) {
        paths.add(fileObject.getName().getPath());
      }
      return paths;
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error list files '" + filenameRemote + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * rename file
   * @param filenameRemoteFrom
   * @param filenameRemoteTo
   */
  public void moveFile(String filenameRemoteFrom, String filenameRemoteTo) {
    
    if (StringUtils.isBlank(filenameRemoteFrom)) {
      throw new RuntimeException("filenameRemoteFrom cant be blank!");
    }

    if (StringUtils.isBlank(filenameRemoteTo)) {
      throw new RuntimeException("filenameRemoteTo cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("renameFileRemoteFrom_" + this.operationIndex, filenameRemoteFrom);

      }
      final String connectionStringFrom = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemoteFrom);
      final String connectionStringTo = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemoteTo);
  
      FileObject remoteFileFrom = this.sysManager.resolveFile(connectionStringFrom,  this.fileSystemOptions);
      FileObject remoteFileTo = this.sysManager.resolveFile(connectionStringTo,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      remoteFileFrom.moveTo(remoteFileTo);
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error move file '" + filenameRemoteFrom + "' to '" + filenameRemoteTo + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * copy file
   * @param filenameRemoteFrom
   * @param filenameRemoteTo
   */
  public void copyFile(String filenameRemoteFrom, String filenameRemoteTo) {
    
    if (StringUtils.isBlank(filenameRemoteFrom)) {
      throw new RuntimeException("filenameRemoteFrom cant be blank!");
    }

    if (StringUtils.isBlank(filenameRemoteTo)) {
      throw new RuntimeException("filenameRemoteTo cant be blank!");
    }

    try {
      if (this.operationIndex < 20) {
        
        this.debugMap.put("copyFileRemoteFrom_" + this.operationIndex, filenameRemoteFrom);

      }
      final String connectionStringFrom = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemoteFrom);
      final String connectionStringTo = createConnectionString(this.host, this.user, this.password, this.privateKeyFilePath, this.passphrase, filenameRemoteTo);
  
      FileObject remoteFileFrom = this.sysManager.resolveFile(connectionStringFrom,  this.fileSystemOptions);
      FileObject remoteFileTo = this.sysManager.resolveFile(connectionStringTo,  this.fileSystemOptions);
  
      //Selectors.SELECT_FILES --> A FileSelector that selects only the base file/folder.
      remoteFileTo.copyFrom(remoteFileFrom, Selectors.SELECT_SELF);
    } catch (FileSystemException fse) {
      throw new RuntimeException("Error copy file '" + filenameRemoteFrom + "' to '" + filenameRemoteTo + "'", fse);
    } finally {
      this.operationIndex++;
    }
  }

  /**
   * 
   * @param hostName
   * @param username
   * @param password
   * @param keyPath
   * @param passphrase
   * @param remoteFilePath
   * @return connect string
   */
  private static String createConnectionString(String hostName, String username, String password, String keyPath, 
      String passphrase, String remoteFilePath) {

    if (remoteFilePath.startsWith("/")) {
      remoteFilePath = remoteFilePath.substring(1, remoteFilePath.length());
    }
    
    if (keyPath != null) {
      return "sftp://" + username + "@" + hostName  + "/" + remoteFilePath;
    }
    return "sftp://" + username + ":" + password + "@" + hostName  + "/" + "/" + remoteFilePath;
  }



}
