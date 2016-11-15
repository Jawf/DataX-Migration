# DataX-Migration
DataX-Migration is Yxt (Yunxuetang) Full Database Migration Tool based on Alibaba DataX 3.0.
Datax-Migration 是云学堂开源的基于阿里巴巴的DataX 3.0的数据库迁移工具。

## DataX是什么?

DataX 是阿里巴巴集团内被广泛使用的离线数据同步工具/平台，实现包括 MySQL、Oracle、SqlServer、Postgre、HDFS、Hive、ADS、HBase、OTS、ODPS 等各种异构数据源之间高效的数据同步功能。

![DataX-Framework](https://cloud.githubusercontent.com/assets/1067175/17879841/93b7fc1c-6927-11e6-8cda-7cf8420fc65f.png)
关于更详细的介绍请看这里：https://github.com/alibaba/DataX/wiki/DataX-Introduction

## 为什么还需要DataX-Migration
DataX专注于对数据的同步，它使用脚本以及可配置的方式，以一个个独立的脚本任务，非常方便地对单表的数据进行同步操作。但我们需要更加智能或自动的方式同步整个数据库，所以我们对DataX进行了包装，以更方便地进行整个数据库的迁移工作。




## Support Data Channels

DataX目前已经有了比较全面的插件体系，主流的RDBMS数据库、NOSQL、大数据计算系统都已经接入，目前支持数据如下图，详情请点击：[DataX数据源参考指南](https://github.com/alibaba/DataX/wiki/DataX-all-data-channels)

| 类型           | 数据源        | Reader(读) | Writer(写) |
| ------------ | ---------- | :-------: | :-------: |
| RDBMS 关系型数据库 | Mysql      |     √     |     √     |
|              | Oracle     |     √     |     √     |
|              | SqlServer  |     √     |     √     |
|              | Postgresql |     √     |     √     |
|              | 达梦         |     √     |     √     |
| 阿里云数仓数据存储    | ODPS       |     √     |     √     |
|              | ADS        |           |     √     |
|              | OSS        |     √     |     √     |
|              | OCS        |     √     |     √     |
| NoSQL数据存储    | OTS        |     √     |     √     |
|              | Hbase0.94  |     √     |     √     |
|              | Hbase1.1   |     √     |     √     |
|              | MongoDB    |     √     |     √     |
| 无结构化数据存储     | TxtFile    |     √     |     √     |
|              | FTP        |     √     |     √     |
|              | HDFS       |     √     |     √     |

Datax-Migration目前对关系型数据库的直接使用是基本没有问题，至于非关系型数据库还需验证。

## Quick Start

* git clone https://github.com/Jawf/datax-migration.git
* mvn clean install
* download [DataX可以运行的bin下载地址](http://datax-opensource.oss-cn-hangzhou.aliyuncs.com/datax.tar.gz)
* copy target/datax-migration.jar and target/datax-migration_lib to datax home directory.
* open the target/datax-migration.jar, edit the config.properties, config migration db information source/target url, dbname, user, password, etc.
* java -jar datax-migration.jar

## Config.properties Detail Properties
** source.db.url=jdbc:mysql://xxx.xxx.xxx.xxx:3306/sourcedbname?useUnicode=true&characterEncoding=UTF-8
** source.db.name=sourcedbname
** source.db.username=username
** source.db.password=password
** target.db.url=jdbc:mysql://xxx.xxx.xxx.xxx:3306/targetdbname?useUnicode=true&characterEncoding=UTF-8
** target.db.name=targetdbname
** target.db.username=username
** target.db.password=password



## 欢迎使用，或加入我们使其变得更加完善。
