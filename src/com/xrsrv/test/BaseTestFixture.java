/**
 * 
 */
package com.xrsrv.test;

import javax.sql.DataSource;

import org.xx.armory.db.impl.BasicDataSource;
import org.xx.armory.db.unittest.DbContextTestFixture;

/**
 * 
 * @author Isabella
 *
 */
public abstract class BaseTestFixture extends DbContextTestFixture {

	/**
	 * 
	 */
	protected BaseTestFixture() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xx.armory.db.unittest.DbContextTestFixture#initDataSource()
	 */
	@Override
	protected DataSource initDataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();

		dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource.setUrl("jdbc:oracle:thin:@localhost:1521/orcl");
		dataSource.setUsername("xinran");
		dataSource.setPassword("000000");

		return dataSource;
	}
}
