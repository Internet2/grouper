/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller;

import java.io.File;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;


/**
 * when indexing a file, keep track of the name, file, type, etc
 */
public class GrouperInstallerIndexFile {

  /**
   * type of patch file
   */
  public static enum PatchFileType {
    
    /**
     * normal file
     */
    file("files"),
    
    /**
     * goes on classpath
     */
    clazz("classes"),
    
    /**
     * library
     */
    lib("lib");
    
    /**
     * dirname in patch
     */
    private String dirName;
    
    /**
     * dirname in patch
     * @return the dirName
     */
    public String getDirName() {
      return this.dirName;
    }
    
    /**
     * dirname in patch
     * @param dirName1 the dirName to set
     */
    public void setDirName(String dirName1) {
      this.dirName = dirName1;
    }

    /**
     * construct with dir name
     * @param theDirName
     */
    private PatchFileType(String theDirName) {
      this.dirName = theDirName;
    }
    
  }
  
  /**
   * 
   */
  public GrouperInstallerIndexFile() {
  }
  
  /**
   * if there are multiple files by this simplename
   */
  private boolean hasMultipleFilesBySimpleName;
  
  /**
   * if there are multiple files by path from start of project
   */
  private boolean hasMultipleFilesByPath;
  
  /**
   * if there are multiple files by relative path (in the patch)
   */
  private boolean hasMultipleFilesByRelativePath;
  
  /**
   * if there are multiple files by relative path (in the patch)
   * @return the hasMultipleFilesByRelativePath
   */
  public boolean isHasMultipleFilesByRelativePath() {
    return this.hasMultipleFilesByRelativePath;
  }
  
  /**
   * if there are multiple files by relative path (in the patch)
   * @param hasMultipleFilesByRelativePath1 the hasMultipleFilesByRelativePath to set
   */
  public void setHasMultipleFilesByRelativePath(boolean hasMultipleFilesByRelativePath1) {
    this.hasMultipleFilesByRelativePath = hasMultipleFilesByRelativePath1;
  }

  /**
   * if there are multiple files by this simplename
   * @return the hasMultipleFilesBysimpleName
   */
  public boolean isHasMultipleFilesBySimpleName() {
    return this.hasMultipleFilesBySimpleName;
  }


  
  /**
   * if there are multiple files by this simplename
   * @param hasMultipleFilesBysimpleName1 the hasMultipleFilesBysimpleName to set
   */
  public void setHasMultipleFilesBySimpleName(boolean hasMultipleFilesBysimpleName1) {
    this.hasMultipleFilesBySimpleName = hasMultipleFilesBysimpleName1;
  }

  /**
   * relative path is path from patch dir (e.g. inside files or classes)
   */
  private String relativePath;
  
  /**
   * relative path is path from patch dir (e.g. inside files or classes)
   * @return the relativePath
   */
  public String getRelativePath() {
    return this.relativePath;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.simpleName + ", relPath: " + this.relativePath + ", type: " + this.patchFileType
        + ", hasMult: " + this.hasMultipleFilesBySimpleName + ", " + this.hasMultipleFilesByRelativePath + ", "
        + this.hasMultipleFilesByPath;
        
  }

  /**
   * relative path is path from patch dir (e.g. inside files or classes)
   * @param relativePath1 the relativePath to set
   */
  public void setRelativePath(String relativePath1) {
    this.relativePath = relativePath1;
  }

  /**
   * if there are multiple files by path from start of project
   * @return the hasMultipleFilesByPath
   */
  public boolean isHasMultipleFilesByPath() {
    return this.hasMultipleFilesByPath;
  }


  
  /**
   * if there are multiple files by path from start of project
   * @param hasMultipleFilesByPath1 the hasMultipleFilesByPath to set
   */
  public void setHasMultipleFilesByPath(boolean hasMultipleFilesByPath1) {
    this.hasMultipleFilesByPath = hasMultipleFilesByPath1;
  }

  /**
   * name and extension of file
   */
  private String simpleName;
  
  
  /**
   * name and extension of file
   * @return the simpleName
   */
  public String getSimpleName() {
    return this.simpleName;
  }

  
  /**
   * name and extension of file
   * @param simpleName1 the simpleName to set
   */
  public void setSimpleName(String simpleName1) {
    this.simpleName = simpleName1;
  }

  
  /**
   * type of file
   * @return the patchFileType
   */
  public PatchFileType getPatchFileType() {
    return this.patchFileType;
  }

  
  /**
   * type of file
   * @param patchFileType1 the patchFileType to set
   */
  public void setPatchFileType(PatchFileType patchFileType1) {
    this.patchFileType = patchFileType1;
  }

  
  /**
   * path from the project directory for the file
   * @return the path
   */
  public String getPath() {
    return this.path;
  }

  
  /**
   * path from the project directory for the file
   * @param path1 the path to set
   */
  public void setPath(String path1) {
    this.path = path1;
  }

  
  /**
   * reference to the file object itself
   * @return the file
   */
  public File getFile() {
    return this.file;
  }

  
  /**
   * reference to the file object itself
   * @param file1 the file to set
   */
  public void setFile(File file1) {
    this.file = file1;
    this.sha1 = null;
  }

  /**
   * null if not computed yet, or the sha1 on file
   */
  private String sha1;

  /**
   * compute sha1
   * @return the hex sha1
   */
  public String computeSha1() {
    if (this.sha1 == null) {
      if (this.file == null){
        throw new RuntimeException("Need a file");
      }
      if (this.file.isDirectory()) {
        throw new RuntimeException("Cant do sha1 on directory: " + this.file.getAbsolutePath());
      }
      this.sha1 = GrouperInstallerUtils.fileSha1(this.file);
    }
    return this.sha1;
  }
  
  /**
   * type of file
   */
  private PatchFileType patchFileType;

  /**
   * path from the project directory for the file
   */
  private String path;
  
  /**
   * reference to the file object itself
   */
  private File file;
  
}
