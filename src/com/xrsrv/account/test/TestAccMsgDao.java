package com.xrsrv.account.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import javax.sql.DataSource;

import org.junit.Test;
import org.xx.armory.commons.DateTimeUtils;
import org.xx.armory.db.unittest.DbUnitHelper;
import org.xx.armory.db.unittest.impl.OracleDbUnitHelper;

import com.xinran.xrsrv.persistence.AccMsgDao;
import com.xrsrv.test.BaseTestFixture;

public class TestAccMsgDao extends BaseTestFixture {

	private DbUnitHelper dbUnitHelper;

	@Override
	protected DataSource initDataSource() throws Exception {
		final DataSource dataSource = super.initDataSource();
		dbUnitHelper = new OracleDbUnitHelper(dataSource);
		dbUnitHelper.loadMetaData("ACC_USER_REG");	
		dbUnitHelper.loadMetaData("ACC_CORP_INFO");	
		dbUnitHelper.loadMetaData("ACC_PERSON_INFO");	
		dbUnitHelper.loadMetaData("MSG_REG_USER");	//注册激活码表
		dbUnitHelper.loadMetaData("MSG_LOST_PWD");	//找密码
		dbUnitHelper.loadMetaData("MSG_CHANGE_MOBILE");	//换手机 
		dbUnitHelper.readXml("ACC_USER_REG", getClass().getResourceAsStream("/test/acc_user_reg.xml"));
		dbUnitHelper.readXml("ACC_PERSON_INFO", getClass().getResourceAsStream("/test/acc_person_info.xml"));
		dbUnitHelper.readXml("ACC_CORP_INFO", getClass().getResourceAsStream("/test/acc_corp_info.xml"));
		dbUnitHelper.readXml("MSG_REG_USER", getClass().getResourceAsStream("/test/msg_reg_user.xml"));
		dbUnitHelper.readXml("MSG_LOST_PWD", getClass().getResourceAsStream("/test/msg_lost_pwd.xml"));
		dbUnitHelper.readXml("MSG_CHANGE_MOBILE", getClass().getResourceAsStream("/test/msg_change_mobile.xml"));
		dbUnitHelper.insertAll(true);

		return dataSource;
	}
	
	@Test
	public void testCreateLostPwdCode() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		
		Date date=DateTimeUtils.createLocalDate(2015,8,18,10,30,23).getTime();
		long c1= accMsgDao.createLostPwdCode( "13849987911", "111222", date);
		assertEquals(1L,c1);
		
		final String sql="select m_id ,mobile ,v_code ,datepoint,status from msg_lost_pwd where M_ID=?";
		final Object[] row1 = this.dbUnitHelper.executeQuery(sql,7L);
		assertEquals(BigDecimal.valueOf(7), row1[0]);
		assertEquals("13849987911", row1[1]);
		assertEquals("111222", row1[2]);
		assertEquals(date, row1[3]);
		assertEquals(BigDecimal.ZERO, row1[4]);
		
	}
	
	@Test
	public void testCheckLostPwdCode() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		
		Date date=DateTimeUtils.createLocalDate(2015,8,17,10,30,23).getTime();
		Boolean b = accMsgDao.checkLostPwdCode("13862334710", "123456", date);
		assertEquals(true,b );
		//手机不存在
		b = accMsgDao.checkLostPwdCode("138623399999797", "123456", date);
		assertEquals(false,b );
		//状态不为0
		b = accMsgDao.checkLostPwdCode("13862334722", "123456", date);
		assertEquals(false,b );
	}
	
	@Test
	public void testUpdateLostPwdCode() throws Exception{
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//set status=1 where mobile=#mobile# and v_code=#v_code# and status=0
		assertEquals(1L,accMsgDao.updateLostPwdCode("13862334710","123456"));
		
		final String sql = "select status from msg_lost_pwd where mobile=? and v_code=?";
		Object[] objs = this.dbUnitHelper.executeQuery(sql, "13862334710","123456");
		assertEquals(BigDecimal.ONE,objs[0]);
				
	}
	
	@Test
	public void testClearMsgLostPwd() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//update msg_lost_pwd set status = 1 where mobile = #mobile#
		long c1= accMsgDao.clearMsgLostPwd("13862334710"); 
		assertEquals(1L,c1);
		
		final String sql1="select status from msg_lost_pwd where mobile=?";
		final Object[] row1 = this.dbUnitHelper.executeQuery(sql1,"13862334710");
		assertEquals(BigDecimal.ONE, row1[0]);
		
	}
	
	@Test
	public void testClearMsgRegAccount() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//delete from msg_reg_user where mobile=#mobile#
		long c1= accMsgDao.clearMsgRegAccount("13862334720");
		assertEquals(1L,c1);
		
		final String sql = "select m_id from msg_reg_user where mobile=?";
		Object[] objs = this.dbUnitHelper.executeQuery(sql, "13862334720");
		assertArrayEquals(null, objs);
		
	}
	
	public void testCreateRegAccountCode() throws Exception{
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		Date date = new Date();
		long lineCount = accMsgDao.createRegAccountCode("13849987911", "111222", date);
		assertEquals(1L,lineCount);
		
		final String sql="select m_id ,mobile ,v_code ,datepoint,status from msg_reg_user where m_id=?";
		final Object[] row = this.dbUnitHelper.executeQuery(sql,7L);
		assertEquals(BigDecimal.valueOf(7), row[0]);
		assertEquals("13849987911", row[1]);
		assertEquals("111222", row[2]);
		assertEquals(date, row[3]);
		assertEquals(BigDecimal.ZERO, row[4]);
	}
	
	public void testCheckRegAccountCode(){
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//where mobile=#mobile#  and status=0 and v_code=#v_code# and datepoint>=#date#
		Date date=DateTimeUtils.createLocalDate(2015,8,18,10,30,23).getTime();
		Boolean b = accMsgDao.checkRegAccountCode("13862334712", "123456",date);
		assertEquals(true,b);
		
		b = accMsgDao.checkRegAccountCode("13849987911", "111222",date);
		assertEquals(false,b);
	}
	
	@Test
	public void testUpdateAcviceCodeStatus() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//update msg_reg_user set status=1 where mobile=#mobile# and v_code=#v_code# and status = 0
		long lc = accMsgDao.updateAcviceCodeStatus("13862334712", "123456");
		assertEquals(1L,lc);
		
		final String sql1="select status from msg_reg_user where mobile=? and v_code=?";
		final Object[] row1 = this.dbUnitHelper.executeQuery(sql1,"13862334712", "123456");
		assertEquals(BigDecimal.ONE, row1[0]);
		
	}
	
	@Test
	public void testCreateChangeMobileCode() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		
		Date date=DateTimeUtils.createLocalDate(2015,8,18,10,30,23).getTime();
		long c1= accMsgDao.createChangeMobileCode("13849987911", "123456", date);
		assertEquals(1L,c1);
		
		final String sql="select m_id ,mobile ,v_code ,datepoint,status from msg_change_mobile where m_id=?";
		final Object[] row = this.dbUnitHelper.executeQuery(sql,7L);
		assertEquals(BigDecimal.valueOf(7), row[0]);
		assertEquals("13849987911", row[1]);
		assertEquals("123456", row[2]);
		assertEquals(date, row[3]);
		assertEquals(BigDecimal.ZERO, row[4]);
		
    }
	
	@Test
	public void testUpdateChangeMobileStatus() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		//set status=1 where mobile=#mobile# and v_code=#v_code# and status=0
		long lc = accMsgDao.updateChangeMobileStatus("13862334710", "123456");
		assertEquals(1L,lc);
		
		final String sql1="select status from msg_change_mobile where mobile=? and v_code=?";
		final Object[] row1 = this.dbUnitHelper.executeQuery(sql1,"13862334710", "123456");
		assertEquals(BigDecimal.ONE, row1[0]);
		
    }
	
	@Test
	public void testCheckChangeMobileCode() throws Exception {
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		
		Date date=DateTimeUtils.createLocalDate(2014,10,11,10,30,23).getTime();
		boolean b = accMsgDao.checkChangeMobileCode("13862334710", "123456", date);
		assertEquals(true,b);
		b = accMsgDao.checkChangeMobileCode("13862339999", "123456", date);
		assertEquals(false,b);
		
    }
	
	@Test
	public void testCreateSmsYmCode() throws Exception{
		AccMsgDao accMsgDao=new AccMsgDao();
		accMsgDao.setSessionFactory(getSessionFactory());
		Date date = DateTimeUtils.createLocalDate(2014,10,11,10,30,23).getTime();
		long lc = accMsgDao.createSmsYmCode("13512345678", "abcode", 1L, 2L, date);
		assertEquals(1, lc);
		
		final String sql = "select mobile,content,t_code,p_id,datepoint from msg_sms_ym where ms_id = ?";
		Object[] objs = this.dbUnitHelper.executeQuery(sql, accMsgDao.getLastRowLongId());
		assertArrayEquals(new Object[]{"13512345678","abcode",BigDecimal.valueOf(1), BigDecimal.valueOf(2), date}, objs); 
	}
	
}
