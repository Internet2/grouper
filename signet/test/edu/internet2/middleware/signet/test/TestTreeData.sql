INSERT INTO Tree VALUES
  ('testTreeId',
   'testTreeName',
   'edu.internet2.middleware.signet.TreeAdapterImpl',
   getDate());
INSERT INTO TreeNode
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',
  'L_0_PID_[NOPARENTID]_S_0_ID',
  getDate(),
  'active',
  'L_0_PNAME_[NOPARENTNAME]_S_0_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID',
  getDate(),
  'active',
  'L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_0_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID',
  getDate(),
  'active',
  'L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_1_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID',
  getDate(),
  'active',
  'L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_2_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_0_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_0_NAME]_S_0_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_1_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_0_NAME]_S_1_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_2_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_0_NAME]_S_2_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_0_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_1_NAME]_S_0_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_1_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_1_NAME]_S_1_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_2_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_1_NAME]_S_2_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_0_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_2_NAME]_S_0_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_1_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_2_NAME]_S_1_NAME',
  'testTreeNodeType');
INSERT INTO TreeNode 
 ("treeID", "nodeID", "modifyDatetime", "status", "name", "nodeType")
VALUES
 ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_2_ID',
  getDate(),
  'active',
  'L_2_PNAME_[L_1_PNAME_[L_0_PNAME_[NOPARENTNAME]_S_0_NAME]_S_2_NAME]_S_2_NAME',
  'testTreeNodeType');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID',
  'L_0_PID_[NOPARENTID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID',
  'L_0_PID_[NOPARENTID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID',
  'L_0_PID_[NOPARENTID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_0_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_1_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID]_S_2_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_0_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_0_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_1_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID]_S_2_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_1_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_0_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_1_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID');
INSERT INTO TreeNodeRelationship
  ("treeID", "nodeID", "parentNodeID")
VALUES
  ('testTreeId',	
  'L_2_PID_[L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID]_S_2_ID',
  'L_1_PID_[L_0_PID_[NOPARENTID]_S_0_ID]_S_2_ID');