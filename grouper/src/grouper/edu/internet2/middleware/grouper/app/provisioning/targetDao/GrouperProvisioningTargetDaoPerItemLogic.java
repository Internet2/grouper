/**
 * Copyright 2024 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.provisioning.targetDao;

/**
 * Logic to run for a single item of independent, parallelizable work in a target DAO retrieve
 * (e.g. fetch the members of one group). Invoked by
 * {@link GrouperProvisionerTargetDaoBase#retrievePerItemInParallel(java.util.List, String, GrouperProvisioningTargetDaoPerItemLogic)},
 * once per item, potentially concurrently across the provisioner thread pool. Implementations must
 * be safe to call concurrently for different items: synchronize any shared collections they write
 * to (the per-item inputs themselves are not shared).
 *
 * @param <T> the type of item processed (e.g. a target group)
 */
public interface GrouperProvisioningTargetDaoPerItemLogic<T> {

  /**
   * Process one item of work. May run on a pool thread.
   * @param item the item to process
   */
  void processItem(T item);

}
