---
title: "Grouper provisioning concurrency"
space: Grouper
pageId: 28554512
version: 3
lastUpdated: 2026-07-01T05:40:17.728Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554512/Grouper+provisioning+concurrency
---

## Description

- The provisioning framework will run actions in threads
- The target by default is assumed to be able to process concurrent operations
- These operations will be threaded by type
  
  - e.g. all group inserts will run in threads, and when all complete, do all the entity inserts
- The default number of threads is 5
  
  - This can be configured in the provisioner
  - The same number of threads is used for full and incremental
- The thread pool is cached statically for each provisioner
  
  - Its ok if the next job runs on another node
  - If the thread size configuration is changed then the thread pool will be replaced dynamically

## Programming a DAO and specifying batch size

- The batch size per thread should match how much work an operation can do
- The default batch size is 20
  
  - e.g. if there are 85 groups to delete, with a batch size of 20, then 5 threads will run at the same time to delete 20, 20, 20, 20, and 5 groups at the same time
- Specify the batch size for each operation in the DAO. E.g. for Azure, all operations can happen 20 at a time, but adding memberships can happen 400 at a time...
  
  
  ```
    @Override
    public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
  
      grouperProvisionerDaoCapabilities.setDefaultBatchSize(20);
      grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);
  ```
