---
title: "Grouper ABAC policy pattern GSH templates"
space: Grouper
pageId: 398393363
version: 1
lastUpdated: 2026-11-20T20:14:01.907Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/398393363/Grouper+ABAC+policy+pattern+GSH+templates
---

A policy pattern is a GSH template of type ABAC that builds the script for a [Grouper ABAC with scripted groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups) from a few user inputs, so users do not need to write JEXL by hand.

## Set up a policy pattern GSH template

Your institution can make a GSH template that will help users setup policies

Make a GSH template that is of type ABAC

Have inputs as you normally would to gather information from the user about the ABAC script to be generated. When you configure a scripted group, you will see all the GSH templates (you are allowed to see) when you select "pattern"

The script needs to take the inputs, construct an ABAC script, and return it to Grouper

```
    gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());
```

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.app.loader.*;
import edu.internet2.middleware.grouper.app.gsh.template.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouperClient.jdbc.*;
import org.apache.commons.lang3.*;
import java.util.*;

public class Test127abacAffiliations extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    
    
    
    Boolean gsh_input_hasPennkey = gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_hasPennkey");
    String gsh_input_affiliationCode = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_affiliationCode");
    boolean gsh_input_includeLeave = GrouperUtil.booleanValue(gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_includeLeave"), true);

    StringBuilder script = new StringBuilder("entity.hasRow('affiliation', \"");

    boolean hasColumnAlready = false;
    if (gsh_input_hasPennkey != null && gsh_input_hasPennkey) {
      if (hasColumnAlready) {
        script.append(" &&");
      }
      script.append(" affiliation_has_pennkey");
      hasColumnAlready = true;
    } else if (gsh_input_hasPennkey != null && !gsh_input_hasPennkey) {
      if (hasColumnAlready) {
        script.append(" &&");
      }
      script.append(" !affiliation_has_pennkey");
      hasColumnAlready = true;
    }

    if (!StringUtils.isBlank(gsh_input_affiliationCode)) {

      if (hasColumnAlready) {
          script.append(" &&");
      }
      script.append(" affiliation_code == '" + gsh_input_affiliationCode + "'");
      hasColumnAlready = true;
    }

    if (!gsh_input_includeLeave) {

      if (hasColumnAlready) {
          script.append(" &&");
      }
      script.append(" affiliation_banner_student_status == 'AS'");
      hasColumnAlready = true;
    }
    
    script.append(" \" )\n");
    gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());
    
  }
}
```

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.gsh.template.*;

public class SimpleReportstoAbac extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input input, GshTemplateV2output output) {

    // Get the built-in template output holder
    GshTemplateOutput builtInOutput = output.getGsh_builtin_gshTemplateOutput();

    // Read the input string from the template form
    String gsh_input_reportsto = input.getGsh_builtin_inputString("gsh_input_reportsto");

    // Construct the JEXL expression
    String jexl = "\${entity.hasAttribute('cs_reports_to', '" + gsh_input_reportsto + "')}";

    // Assign it as the ABAC script
    builtInOutput.assignAbacScript(jexl);
  }
}
```
