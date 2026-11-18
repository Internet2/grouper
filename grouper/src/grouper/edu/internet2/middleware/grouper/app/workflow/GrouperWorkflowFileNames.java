package edu.internet2.middleware.grouper.app.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * <pre>
 * Builds the storage name of a workflow instance snapshot (grouper_file path, S3 key or file
 * system file name).  The name used to be config id + state + time to the second, so two
 * instances of the same workflow saved in the same state in the same second got the same name:
 * with database storage one save failed on the unique file_path index, with S3 or the file
 * system the later save overwrote the earlier instance's (differently encrypted) file.
 *
 * The name now holds the instance uuid (unique per instance) and the time to the millisecond
 * (distinct for repeated transitions of one instance).  Readers use the stored file pointer,
 * never re-derive the name, so existing instances with old names keep working.
 *
 * In its own class so one version ships to every release and can be unit tested.
 * </pre>
 */
public class GrouperWorkflowFileNames {

  /**
   * no instances
   */
  private GrouperWorkflowFileNames() {
  }

  /**
   * storage name for the current snapshot of this instance
   * @param workflowConfig
   * @param instance
   * @return the name
   */
  public static String fileName(GrouperWorkflowConfig workflowConfig, GrouperWorkflowInstance instance) {
    return fileName(workflowConfig.getWorkflowConfigId(), instance.getWorkflowInstanceUuid(),
        instance.getWorkflowInstanceState(), new Date());
  }

  /**
   * storage name: configId_instanceUuid_state_yyyyMMdd_HH_mm_ss_SSS
   * @param workflowConfigId
   * @param instanceUuid if blank (should not happen) a random uuid is used so the name is still unique
   * @param state
   * @param date
   * @return the name
   */
  public static String fileName(String workflowConfigId, String instanceUuid, String state, Date date) {
    String uuid = StringUtils.isBlank(instanceUuid) ? GrouperUuid.getUuid() : instanceUuid;
    return workflowConfigId + "_" + uuid + "_" + state + "_"
        + new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS").format(date);
  }
}
