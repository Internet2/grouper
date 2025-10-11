import java.io.File;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;
 
 
  public class MyGshTemplate extends GshTemplateV2 {
     
    public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
        GshTemplateV2output gshTemplateV2output) {
       
      GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
      String gsh_builtin_ownerStemName = gshTemplateV2input.getGsh_builtin_ownerStemName();
//      File file = gshTemplateV2input.getGsh_builtin_inputFile("gsh_input_prefix");
//      String fileContents = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_myExtension");
//      String nonFileString = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_string");
//      if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
//        gsh_builtin_gshTemplateOutput.assignIsError(true);
//        return;
//      }
 
//      gsh_builtin_gshTemplateOutput.addOutputLine("Got file: "+file.getName());
//      gsh_builtin_gshTemplateOutput.addOutputLine("File contents are "+fileContents);
//      gsh_builtin_gshTemplateOutput.addOutputLine("Non file string "+nonFileString);
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Success ");
             
    }
 
  }