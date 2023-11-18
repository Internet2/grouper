/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.visualization;

public enum StyleObjectType {
  DEFAULT("default"), GRAPH("graph"),
  STEM("stem"), START_STEM("start_stem"), SKIP_STEM("skip_stem"),
  GROUP("group"), START_GROUP("start_group"), SKIP_GROUP("skip_group"),
  LOADER_GROUP("loader_group"), START_LOADER_GROUP("start_loader_group"),
  SIMPLE_LOADER_GROUP("simple_loader_group"), START_SIMPLE_LOADER_GROUP("start_simple_loader_group"),
  COMPLEMENT_GROUP("complement_group"), INTERSECT_GROUP("intersect_group"),
  SUBJECT("subject"), START_SUBJECT("start_subject"),
  PROVISIONER("provisioner"),
  EDGE("edge"), EDGE_FROM_LOADER("edge_loader"), EDGE_TO_PROVISIONER("edge_provisioner"),
  EDGE_FROM_STEM("edge_stem"), EDGE_MEMBERSHIP("edge_membership"),
  EDGE_COMPLEMENT_LEFT("edge_complement_left"), EDGE_COMPLEMENT_RIGHT("edge_complement_right"),
  EDGE_INTERSECT_LEFT("edge_intersect_left"), EDGE_INTERSECT_RIGHT("edge_intersect_right");

  private String name;

  private StyleObjectType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
