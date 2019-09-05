 package com.bdaim.common.entity;


 import com.bdaim.common.dto.Page;

 import javax.persistence.*;

 @Entity
 @Table(name = "config")
 public class Config extends Page {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id;
     @Column
     private Integer lid;
     @Column(name = "project_name")
     private String projectName;
     @Column(name = "flow_code")
     private String flowCode;
     @Column(name = "node_id")
     private String nodeId;
     @Column(name = "task_id")
     private String taskId;
     @Column(name = "first_level_name")
     private String firstLevelName;//
     @Column(name="second_level_name")
     private String secondLevelName;
     @Column(name="second_level_field")
     private String secondLevelField;
     @Column(name="third_level_name")
     private String thirdLevelName;
     @Column(name="first_level_id")
     private String firstLevelId;
     @Column(name="second_level_id")
     private String secondLevelId;
     @Column(name="third_level_id")
     private String thirdLevelId;
     @Column(name="hdfs_path")
     private String hdfsPath;
     @Column(name="hive_table")
     private String hiveTable;
     @Column(name="hbase_table")
     private String hbaseTable;
     @Column(name="hbase_column")
     private String hbaseColumn;
     @Column(name="merge_hbase_table")
     private String mergeHbaseTable;
     @Column(name="merge_hbase_column")
     private String mergeHbaseColumn;
     @Column
     private Integer type;
     @Column
     private Integer mutex;
     public String getFirstLevelName() {
         return firstLevelName;
     }

     public void setFirstLevelName(String firstLevelName) {
         this.firstLevelName = firstLevelName;
     }

     public String getSecondLevelName() {
         return secondLevelName;
     }

     public void setSecondLevelName(String secondLevelName) {
         this.secondLevelName = secondLevelName;
     }

     public String getSecondLevelField() {
         return secondLevelField;
     }

     public void setSecondLevelField(String secondLevelField) {
         this.secondLevelField = secondLevelField;
     }

     public String getThirdLevelName() {
         return thirdLevelName;
     }

     public void setThirdLevelName(String thirdLevelName) {
         this.thirdLevelName = thirdLevelName;
     }

     public String getFirstLevelId() {
         return firstLevelId;
     }

     public void setFirstLevelId(String firstLevelId) {
         this.firstLevelId = firstLevelId;
     }

     public String getSecondLevelId() {
         return secondLevelId;
     }

     public void setSecondLevelId(String secondLevelId) {
         this.secondLevelId = secondLevelId;
     }

     public String getThirdLevelId() {
         return thirdLevelId;
     }

     public void setThirdLevelId(String thirdLevelId) {
         this.thirdLevelId = thirdLevelId;
     }

     public String getHdfsPath() {
         return hdfsPath;
     }

     public void setHdfsPath(String hdfsPath) {
         this.hdfsPath = hdfsPath;
     }

     public String getHiveTable() {
         return hiveTable;
     }

     public void setHiveTable(String hiveTable) {
         this.hiveTable = hiveTable;
     }

     public String getHbaseTable() {
         return hbaseTable;
     }

     public void setHbaseTable(String hbaseTable) {
         this.hbaseTable = hbaseTable;
     }

     public String getHbaseColumn() {
         return hbaseColumn;
     }

     public void setHbaseColumn(String hbaseColumn) {
         this.hbaseColumn = hbaseColumn;
     }

     public String getMergeHbaseTable() {
         return mergeHbaseTable;
     }

     public void setMergeHbaseTable(String mergeHbaseTable) {
         this.mergeHbaseTable = mergeHbaseTable;
     }

     public String getMergeHbaseColumn() {
         return mergeHbaseColumn;
     }

     public void setMergeHbaseColumn(String mergeHbaseColumn) {
         this.mergeHbaseColumn = mergeHbaseColumn;
     }

     public Integer getType() {
         return type;
     }

     public void setType(Integer type) {
         this.type = type;
     }

     public Integer getMutex() {
         return mutex;
     }

     public void setMutex(Integer mutex) {
         this.mutex = mutex;
     }

     public String getTaskId() {
         return taskId;
     }

     public void setTaskId(String taskId) {
         this.taskId = taskId;
     }
     public Integer getId() {
         return id;
     }
     public void setId(Integer id) {
         this.id = id;
     }
     public String getProjectName() {
         return projectName;
     }
     public void setProjectName(String projectName) {
         this.projectName = projectName;
     }
     public String getFlowCode() {
         return flowCode;
     }
     public void setFlowCode(String flowCode) {
         this.flowCode = flowCode;
     }
     public String getNodeId() {
         return nodeId;
     }
     public void setNodeId(String nodeId) {
         this.nodeId = nodeId;
     }
     public Integer getLid() {
         return lid;
     }
     public void setLid(Integer lid) {
         this.lid = lid;
     }

 }
