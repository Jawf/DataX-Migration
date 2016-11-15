package com.yxt.data.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yxt.data.migration.DbViewer;
import com.yxt.data.migration.bean.DataTable;
import com.yxt.data.migration.bean.DataTableStatus;
import com.yxt.data.migration.util.AppConfig;
import com.yxt.data.migration.util.DbHelper;

@Service
public class DbViewerImpl implements DbViewer {
	protected static final Log log = LogFactory.getLog(DbViewerImpl.class);

	@Autowired
	private DbHelper dbHelper;
	
	@Autowired
	private AppConfig config;
	
	/* (non-Javadoc)
	 * @see com.yxt.data.migration.DbViewer#getTransfterTables()
	 */
	public List<DataTable> getTargetTransfterTables() throws SQLException {
		String sql = config.getMigrationQueryTargetTablesSql();
		ResultSet rs = null;
		List<DataTable> result = null;
		try {
			rs = dbHelper.getTargetConnection().prepareStatement(sql).executeQuery();

			if (rs != null) {
				result = new ArrayList<DataTable>();
				while (rs.next()) {
					DataTable ta = new DataTable();
					ta.setName(rs.getString(1));
					result.add(ta);
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeTargetConnection();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.yxt.data.migration.DbViewer#getTransfterTableColumns(java.lang.String)
	 */
	public List<String> getTargetTransfterTableColumns(String tableName) throws SQLException {
		String sql = config.getMigrationQueryTargetTableColumnsSql();
		sql = sql.replace("{0}", tableName);
		List<String> result = null;

		try {
			ResultSet rs = dbHelper.getTargetConnection().prepareStatement(sql).executeQuery();
			if (rs != null) {
				result = new ArrayList<String>();
				while (rs.next()) {
					result.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeTargetConnection();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.yxt.data.migration.DbViewer#getTransfterTablePrimaryKey(java.lang.String)
	 */
	public List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException {
		String sql = config.getMigrationQueryTargetTablePrimaryKeysSql();
		sql = sql.replace("{0}", tableName);
		List<String> result = null;
		try {
			ResultSet rs = dbHelper.getTargetConnection().prepareStatement(sql).executeQuery();
			
			if (rs != null) {
				result = new ArrayList<String>();
				while (rs.next()) {
					result.add(rs.getString(1));
				}
			} 
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeTargetConnection();
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see com.yxt.data.migration.DbViewer#getTransfterTablesSize()
	 */
	public List<DataTableStatus> getSourceTransfterTablesStatus() throws SQLException {
		String sql = config.getMigrationQuerySourceTablesStatusSql();

		List<DataTableStatus> result = null;

		try {
			ResultSet rs = dbHelper.getSourceConnection().prepareStatement(sql).executeQuery();
			if (rs != null) {
				result = new ArrayList<DataTableStatus>();
				while (rs.next()) {
					DataTableStatus ta = new DataTableStatus();
					ta.setName(rs.getString(1));
					ta.setSize(rs.getFloat(2));
					ta.setCount(rs.getLong(3));
					result.add(ta);
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeSourceConnection();
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see com.yxt.data.migration.DbViewer#getTransfterTableMigrationCount(java.lang.String, java.lang.String)
	 */
	public long getSourceTransfterTableMigrationCount(String tableName, String whereClause) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from " + tableName + "  ");
		if (whereClause != null && !"".equals(whereClause)) {
			whereClause = whereClause.replace("\"", "");
			sql.append(" where " + whereClause);
		}
		long result = 0;

		try {
			ResultSet rs = dbHelper.getSourceConnection().prepareStatement(sql.toString()).executeQuery();
			if (rs != null) {
				rs.next();
				result = rs.getLong(1);
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeSourceConnection();
		return result;
	}

	public long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from " + tableName + "  ");
		if (whereClause != null && !"".equals(whereClause)) {
			whereClause = whereClause.replace("\"", "");
			sql.append(" where " + whereClause);
		}
		long result = 0;

		try {
			ResultSet rs = dbHelper.getTargetConnection().prepareStatement(sql.toString()).executeQuery();
			if (rs != null) {
				rs.next();
				result = rs.getLong(1);
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		dbHelper.closeTargetConnection();
		return result;
	}

}
