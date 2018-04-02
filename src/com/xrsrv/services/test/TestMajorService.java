package com.xrsrv.services.test;

import java.math.BigDecimal;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;
import org.xx.armory.db.unittest.DbUnitHelper;
import org.xx.armory.db.unittest.impl.OracleDbUnitHelper;

import com.xinran.xrsrv.persistence.MajorServiceDao;
import com.xrsrv.test.BaseTestFixture;

public class TestMajorService extends BaseTestFixture {

	private DbUnitHelper dbUnitHelper;

	@Override
	protected DataSource initDataSource() throws Exception {
		final DataSource dataSource = super.initDataSource();
		dbUnitHelper = new OracleDbUnitHelper(dataSource);

		return dataSource;
	}
	
	@Test
	public void testMajorService() throws Exception {
		MajorServiceDao majorServiceDao = new MajorServiceDao();
		majorServiceDao.setSessionFactory(getSessionFactory());
		final String name = "石刘红";
		final String address="洪城滚滚路";
		final String  mobile="15800838516";
		final String  idCard="340826198707078728";
		final String content="去随意";
		final Date dueTime = new Date();
		final Date date = new Date();
		final long type = 0;
		long userType = 1;
		long auId = 0L;
		long gender = NumberUtils.toLong(idCard.length() == 15 ?idCard.substring(14, 15):idCard.substring(16, 17))%2;
		
//		majorServiceDao.createMajorServiceInfo(content, dueTime,mobile, address, type, name, idCard, gender, auId, userType, "", new BigDecimal(0), 0L, date, null);
	}
}
