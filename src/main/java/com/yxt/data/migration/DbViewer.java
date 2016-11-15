package com.yxt.data.migration;

import java.sql.SQLException;
import java.util.List;

import com.yxt.data.migration.bean.DataTable;
import com.yxt.data.migration.bean.DataTableStatus;

public interface DbViewer {

	public List<DataTable> getTargetTransfterTables() throws SQLException;

	public List<String> getTargetTransfterTableColumns(String tableName) throws SQLException;

	public List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException;

	public List<DataTableStatus> getSourceTransfterTablesStatus() throws SQLException;

	public long getSourceTransfterTableMigrationCount(String tableName, String whereClause) throws SQLException;

	public long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException;

}