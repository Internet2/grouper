package edu.internet2.middleware.grouper.app.provisioningExamples.exampleFileWriter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class MailListGroup {

  // /Users/mchyzer/Documents/23/2310/provisioningFiles
  private String fileName;
  
  
  public String getFileName() {
    return fileName;
  }


  public MailListGroup(String theFileName) {
    this.fileName = theFileName;
    
    File file = new File("/Users/mchyzer/Documents/23/2310/provisioningFiles/" + theFileName);
//    if (file.exists()) {
//      String contents = GrouperUtil.readFileIntoString(file);
//      String[] nameAndEmails = GrouperUtil.splitTrim(contents, "\n");
//      for (String nameAndEmail : GrouperUtil.nonNull(nameAndEmails, String.class) ) {
//        if (StringUtils.isBlank(nameAndEmail)) {
//          continue;
//        }
//        int spaceToSplit = nameAndEmail.lastIndexOf(" ");
//        String name = nameAndEmail.substring(0, spaceToSplit);
//        String email = nameAndEmail.substring(spaceToSplit + 1, nameAndEmail.length());
//        this.emailToName.put(email, name);
//      }
//    }
    
    
  }
  
  // display name space email
  private Map<String, String> emailToName = new LinkedHashMap<>();
  
  public void add(String email, String name) {
    emailToName.put(email, name);
  }
  
  
  public Map<String, String> getEmailToName() {
    return emailToName;
  }

  public void writeToDisk() {
    
    StringBuilder fileContents = new StringBuilder();
    for (String email : emailToName.keySet()) {
      String name = emailToName.get(email);
      fileContents.append(name).append(" ").append(email).append("\n");
    }
     
    GrouperUtil.saveStringIntoFile(new File("/Users/mchyzer/Documents/23/2310/provisioningFiles/" + fileName), 
        fileContents.toString());
  }
}
