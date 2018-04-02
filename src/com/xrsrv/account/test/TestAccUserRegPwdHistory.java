package com.xrsrv.account.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Test;
import org.xx.armory.commons.Converter;
import org.xx.armory.commons.DateTimeUtils;
import org.xx.armory.db.unittest.DbUnitHelper;
import org.xx.armory.db.unittest.impl.OracleDbUnitHelper;

import com.xinran.xrsrv.persistence.AccUserRegPwdHistory;
import com.xinran.xrsrv.persistence.AccUserRegPwdHistoryDao;
import com.xrsrv.test.BaseTestFixture;

public class TestAccUserRegPwdHistory extends BaseTestFixture {

	private DbUnitHelper dbUnitHelper;

	@Override
	protected DataSource initDataSource() throws Exception {
		final DataSource dataSource = super.initDataSource();
		dbUnitHelper = new OracleDbUnitHelper(dataSource);
		dbUnitHelper.loadMetaData("ACC_USER_REG_PWD_HISTORY");
		dbUnitHelper.readXml("ACC_USER_REG_PWD_HISTORY", getClass().getResourceAsStream("/test/acc_user_reg_pwd_history.xml"));
		dbUnitHelper.insertAll(true);
		return dataSource;
	}
	
	@Test
	public void testCreateAccUserRegPwdHistory() {
		AccUserRegPwdHistoryDao accUserRegPwdHistroyDao = new AccUserRegPwdHistoryDao();
		accUserRegPwdHistroyDao.setSessionFactory(getSessionFactory());
		//data
		final long operator = 11L;
		final long source = 1L;
		final String vcode = "654321";
		
		//create data
		final long  c = accUserRegPwdHistroyDao.createAccUserRegPwdHistory(operator, source, vcode);
		
	    //search result
		final long id = Converter.toLong(accUserRegPwdHistroyDao.getLastRowId(),0);
		final AccUserRegPwdHistory p = accUserRegPwdHistroyDao.queryAccUserRegPwdHistoryById(id);
		
		//compare
		assertEquals(1L, c);
		assertNotNull(p);
		
		assertEquals(operator, p.getOperator().longValue());
		assertEquals(source, p.getSource().longValue());
		assertEquals(vcode, p.getVcode());
	}

	@Test
	public void testQueryAccUserRegPwdHistoryById() {
		AccUserRegPwdHistoryDao accUserRegPwdHistroyDao = new AccUserRegPwdHistoryDao();
		accUserRegPwdHistroyDao.setSessionFactory(getSessionFactory());
		//data
		final long id = 0L;
		final long operator = 10L;
		final Date operateTime = DateTimeUtils.createLocalDate(2015, 6, 26, 10, 30, 23).getTime();
		final long source = 1L;
		final String vcode = "123456";	
		
		//query result
		final AccUserRegPwdHistory p = accUserRegPwdHistroyDao.queryAccUserRegPwdHistoryById(id);
		
		//compare
        assertNotNull(p);
		
		assertEquals(operator, p.getOperator().longValue());
		assertEquals(operateTime, p.getOperateTime());
		assertEquals(source, p.getSource().longValue());
		assertEquals(vcode, p.getVcode());
	}

}
