package com.yxt.data.migration.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * JDBC封装类
 * 
 */
@Service
public class DbHelper {

	protected static final Log log = LogFactory.getLog(DbHelper.class);

	@Autowired
	private AppConfig config;

	private static DruidDataSource targetds = null;
	// 声明线程共享变量
	public static ThreadLocal<Connection> targetcontainer = new ThreadLocal<Connection>();

	private static DruidDataSource sourceds = null;
	// 声明线程共享变量
	public static ThreadLocal<Connection> sourcecontainer = new ThreadLocal<Connection>();

	private void init() {
		targetds = new DruidDataSource();
		targetds.setUrl(config.getTargetDbUrl());
		targetds.setUsername(config.getTargetDbUsername());// 用户名
		targetds.setPassword(config.getTargetDbPassword());// 密码
		targetds.setInitialSize(2);
		targetds.setMaxActive(20);
		targetds.setMinIdle(0);
		targetds.setMaxWait(60000);
		//targetds.setValidationQuery("SELECT 1");
		targetds.setTestOnBorrow(false);
		targetds.setTestWhileIdle(true);
		targetds.setPoolPreparedStatements(false);

		sourceds = new DruidDataSource();
		sourceds.setUrl(config.getSourceDbUrl());
		sourceds.setUsername(config.getSourceDbUsername());// 用户名
		sourceds.setPassword(config.getSourceDbPassword());// 密码
		sourceds.setInitialSize(2);
		sourceds.setMaxActive(20);
		sourceds.setMinIdle(0);
		sourceds.setMaxWait(60000);
		//sourceds.setValidationQuery("SELECT 1 FROM DUAL");
		sourceds.setTestOnBorrow(false);
		sourceds.setTestWhileIdle(true);
		sourceds.setPoolPreparedStatements(false);
	}

	/**
	 * 获取数据连接
	 * 
	 * @return
	 */
	public Connection getTargetConnection() {
		Connection conn = null;
		try {
			if (targetds == null) {
				init();
			}
			conn = targetds.getConnection();
			log.info(Thread.currentThread().getName() + " Target Connection started......");
			targetcontainer.set(conn);
		} catch (Exception e) {
			log.info(Thread.currentThread().getName() + " Get Target connection failed!");
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 获取数据连接
	 * 
	 * @return
	 */
	public Connection getSourceConnection() {
		Connection conn = null;
		try {
			if (sourceds == null) {
				init();
			}
			conn = sourceds.getConnection();
			log.info(Thread.currentThread().getName() + " Source Connection started......");
			sourcecontainer.set(conn);
		} catch (Exception e) {
			log.info(Thread.currentThread().getName() + " Get Source connection failed!");
			e.printStackTrace();
		}
		return conn;
	}

	/*** 关闭连接 */
	public void closeTargetConnection() {
		try {
			Connection conn = targetcontainer.get();
			if (conn != null) {
				conn.close();
				log.info(Thread.currentThread().getName() + " Target Connection closed.");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				targetcontainer.remove();// 从当前线程移除连接切记
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/*** 关闭连接 */
	public void closeSourceConnection() {
		try {
			Connection conn = sourcecontainer.get();
			if (conn != null) {
				conn.close();
				log.info(Thread.currentThread().getName() + " Source Connection closed.");
			}
		} catch (

		SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				sourcecontainer.remove();// 从当前线程移除连接切记
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

}