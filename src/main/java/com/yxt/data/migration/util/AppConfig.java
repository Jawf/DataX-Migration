package com.yxt.data.migration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;

@Configuration
@PropertySource("classpath:config.properties")
@ComponentScan(basePackages = {"com.yxt",})

//@EnableAsync
@Service
public class AppConfig {

	@Value("${source.db.name}")
    private String sourceDbName;
	
	@Value("${source.db.url}")
	private String sourceDbUrl;
	
	@Value("${source.db.username}")
	private String sourceDbUsername;
	
	@Value("${source.db.password}")
	private String sourceDbPassword;
	
	@Value("${target.db.name}")
	private String targetDbName;
	
	@Value("${target.db.url}")
	private String targetDbUrl;
	
	@Value("${target.db.username}")
	private String targetDbUsername;
	
	@Value("${target.db.password}")
	private String targetDbPassword;
	
	@Value("${migration.datax.tool.folder}")
	private String dataxToolFolder;
	
	@Value("${migration.datax.channel.multiple}")
	private String dataxUseMultipleChannel;
	
	@Value("${migration.datax.channel.2channels.records.over}")
	private long dataxUse2ChannelRecordsOver;
	
	@Value("${migration.datax.channel.4channels.records.over}")
	private long dataxUse4ChannelRecordsOver;
	
	@Value("${migration.datax.channel.nchannels.number}")
	private int dataxUseNChannelNumber;
	
	@Value("${migration.datax.channel.nchannels.records.over}")
	private long dataxUseNChannelRecordsOver;

    @Value("${source.db.global.where.clause}")
    private String globalWhereClause;
    
    @Value("${source.db.global.where.second.clause}")
    private String globalWhere2Clause;
    
    @Value("${migration.error.continue}")
    private String errorContinue;
    
    @Value("${migration.ingore.tables}")
    private String ingoreTables;
    
    @Value("${migration.ingore.bigtables.size.than.mb}")
    private float ingoreBigTablesSizeThanMb;
    
    @Value("${migration.jobthread.split.type}")
    private String jobthreadSplitType;

    @Value("${migration.jobthread.split.maxcount}")
    private int jobthreadSplitMaxcount;
    
    @Value("${migration.jobthread.split.tablesize.mb}")
    private String jobthreadSplitTableSizeMb;
    
    @Value("${migration.jobthread.split.indexes}")
    private String jobthreadSplitTableIndexes;
    
    @Value("${migration.query.target.tables.sql}")
    private String migrationQueryTargetTablesSql;
    
    @Value("${migration.query.target.table.columns.sql}")
    private String migrationQueryTargetTableColumnsSql;
    
    @Value("${migration.query.target.table.primarykeys.sql}")
    private String migrationQueryTargetTablePrimaryKeysSql;
    
    @Value("${migration.query.source.tables.status.sql}")
    private String migrationQuerySourceTablesStatusSql;
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
        
    }
	
	public String getSourceDbName() {
		return sourceDbName;
	}

	public void setSourceDbName(String sourceDbName) {
		this.sourceDbName = sourceDbName;
	}

	public String getSourceDbUrl() {
		return sourceDbUrl;
	}

	public void setSourceDbUrl(String sourceDbUrl) {
		this.sourceDbUrl = sourceDbUrl;
	}

	public String getSourceDbUsername() {
		return sourceDbUsername;
	}

	public void setSourceDbUsername(String sourceDbUsername) {
		this.sourceDbUsername = sourceDbUsername;
	}

	public String getSourceDbPassword() {
		return sourceDbPassword;
	}

	public void setSourceDbPassword(String sourceDbPassword) {
		this.sourceDbPassword = sourceDbPassword;
	}

	public String getTargetDbName() {
		return targetDbName;
	}

	public void setTargetDbName(String targetDbName) {
		this.targetDbName = targetDbName;
	}

	public String getTargetDbUrl() {
		return targetDbUrl;
	}

	public void setTargetDbUrl(String targetDbUrl) {
		this.targetDbUrl = targetDbUrl;
	}

	public String getTargetDbUsername() {
		return targetDbUsername;
	}

	public void setTargetDbUsername(String targetDbUsername) {
		this.targetDbUsername = targetDbUsername;
	}

	public String getTargetDbPassword() {
		return targetDbPassword;
	}

	public void setTargetDbPassword(String targetDbPassword) {
		this.targetDbPassword = targetDbPassword;
	}

	public String getDataxToolFolder() {
		return dataxToolFolder;
	}

	public void setDataxToolFolder(String dataxToolFolder) {
		this.dataxToolFolder = dataxToolFolder;
	}

	public String getGlobalWhereClause() {
		return globalWhereClause;
	}

	public void setGlobalWhereClause(String globalWhereClause) {
		this.globalWhereClause = globalWhereClause;
	}

	public String getGlobalWhere2Clause() {
		return globalWhere2Clause;
	}

	public void setGlobalWhere2Clause(String globalWhere2Clause) {
		this.globalWhere2Clause = globalWhere2Clause;
	}

	public String getErrorContinue() {
		return errorContinue;
	}

	public void setErrorContinue(String errorContinue) {
		this.errorContinue = errorContinue;
	}

	public String getIngoreTables() {
		return ingoreTables;
	}

	public void setIngoreTables(String ingoreTables) {
		this.ingoreTables = ingoreTables;
	}

	public float getIngoreBigTablesSizeThanMb() {
		return ingoreBigTablesSizeThanMb;
	}

	public void setIngoreBigTablesSizeThanMb(float ingoreBigTablesSizeThanMb) {
		this.ingoreBigTablesSizeThanMb = ingoreBigTablesSizeThanMb;
	}

	public String getJobthreadSplitTableSizeMb() {
		return jobthreadSplitTableSizeMb;
	}

	public void setJobthreadSplitTableSizeMb(String jobthreadSplitTableSizeMb) {
		this.jobthreadSplitTableSizeMb = jobthreadSplitTableSizeMb;
	}

	public String getDataxUseMultipleChannel() {
		return dataxUseMultipleChannel;
	}

	public void setDataxUseMultipleChannel(String dataxUseMultipleChannel) {
		this.dataxUseMultipleChannel = dataxUseMultipleChannel;
	}

	public long getDataxUse2ChannelRecordsOver() {
		return dataxUse2ChannelRecordsOver;
	}

	public void setDataxUse2ChannelRecordsOver(long dataxUse2ChannelRecordsOver) {
		this.dataxUse2ChannelRecordsOver = dataxUse2ChannelRecordsOver;
	}

	public long getDataxUse4ChannelRecordsOver() {
		return dataxUse4ChannelRecordsOver;
	}

	public void setDataxUse4ChannelRecordsOver(long dataxUse4ChannelRecordsOver) {
		this.dataxUse4ChannelRecordsOver = dataxUse4ChannelRecordsOver;
	}

	public int getDataxUseNChannelNumber() {
		return dataxUseNChannelNumber;
	}

	public void setDataxUseNChannelNumber(int dataxUseNChannelNumber) {
		this.dataxUseNChannelNumber = dataxUseNChannelNumber;
	}

	public long getDataxUseNChannelRecordsOver() {
		return dataxUseNChannelRecordsOver;
	}

	public void setDataxUseNChannelRecordsOver(long dataxUseNChannelRecordsOver) {
		this.dataxUseNChannelRecordsOver = dataxUseNChannelRecordsOver;
	}

	public String getMigrationQueryTargetTablesSql() {
		return migrationQueryTargetTablesSql;
	}

	public void setMigrationQueryTargetTablesSql(String migrationQueryTargetTablesSql) {
		this.migrationQueryTargetTablesSql = migrationQueryTargetTablesSql;
	}

	public String getMigrationQueryTargetTableColumnsSql() {
		return migrationQueryTargetTableColumnsSql;
	}

	public void setMigrationQueryTargetTableColumnsSql(String migrationQueryTargetTableColumnsSql) {
		this.migrationQueryTargetTableColumnsSql = migrationQueryTargetTableColumnsSql;
	}

	public String getMigrationQueryTargetTablePrimaryKeysSql() {
		return migrationQueryTargetTablePrimaryKeysSql;
	}

	public void setMigrationQueryTargetTablePrimaryKeysSql(String migrationQueryTargetTablePrimaryKeysSql) {
		this.migrationQueryTargetTablePrimaryKeysSql = migrationQueryTargetTablePrimaryKeysSql;
	}

	public String getMigrationQuerySourceTablesStatusSql() {
		return migrationQuerySourceTablesStatusSql;
	}

	public void setMigrationQuerySourceTablesStatusSql(String migrationQuerySourceTablesStatusSql) {
		this.migrationQuerySourceTablesStatusSql = migrationQuerySourceTablesStatusSql;
	}

	public String getJobthreadSplitType() {
		return jobthreadSplitType;
	}

	public void setJobthreadSplitType(String jobthreadSplitType) {
		this.jobthreadSplitType = jobthreadSplitType;
	}

	public int getJobthreadSplitMaxcount() {
		return jobthreadSplitMaxcount;
	}

	public void setJobthreadSplitMaxcount(int jobthreadSplitMaxcount) {
		this.jobthreadSplitMaxcount = jobthreadSplitMaxcount;
	}

	public String getJobthreadSplitTableIndexes() {
		return jobthreadSplitTableIndexes;
	}

	public void setJobthreadSplitTableIndexes(String jobthreadSplitTableIndexes) {
		this.jobthreadSplitTableIndexes = jobthreadSplitTableIndexes;
	}
	
}
