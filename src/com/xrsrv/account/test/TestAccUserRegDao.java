package com.xrsrv.account.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Test;
import org.xx.armory.commons.DateTimeUtils;
import org.xx.armory.db.unittest.DbUnitHelper;
import org.xx.armory.db.unittest.impl.OracleDbUnitHelper;

import com.xinran.xrsrv.persistence.AccUserInfoOfPrj;
import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xinran.xrsrv.persistence.AuthInfo;
import com.xrsrv.system.enumerate.AccountStatus;
import com.xrsrv.test.BaseTestFixture;

public class TestAccUserRegDao extends BaseTestFixture {
	private DbUnitHelper dbUnitHelper;

	@Override
	protected DataSource initDataSource() throws Exception {
		final DataSource dataSource = super.initDataSource();
		dbUnitHelper = new OracleDbUnitHelper(dataSource);
		
		dbUnitHelper.loadMetaData("ACC_USER_REG");	
		dbUnitHelper.loadMetaData("ACC_PERSON_INFO");
		dbUnitHelper.loadMetaData("ACC_CORP_INFO");
		
		
		dbUnitHelper.readXml("ACC_USER_REG", getClass().getResourceAsStream("/test/acc_user_reg.xml"));
		dbUnitHelper.readXml("ACC_PERSON_INFO", getClass().getResourceAsStream("/test/acc_person_info.xml"));
		dbUnitHelper.readXml("ACC_CORP_INFO", getClass().getResourceAsStream("/test/acc_corp_info.xml"));
		
		
		dbUnitHelper.insertAll(true);

		return dataSource;
	}
	
    @Test
    public void testCreateAccUserReg() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	//data
    	final long auId = 0L;
    	final String loginName = "你好呀";
    	final String password = "123123";
    	final long userType = 1L;
    	final String recommendMobile = "12345678901";
    	final long status = 2L;
    	final Date regTime = new Date();
    	final String userCode = "1231312312312323";
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	final String mobile = "12345678910";
    	final long recInfoModiCount = 0L;
    	
    	//create Data
    	long c = accUserRegDao.createAccUserReg(loginName, password, userType, mobile, "",recommendMobile, status, regTime, userCode, recInfoModiCount);
    	
    	//compare
    	assertEquals(1, c);
    	
    	final AccUserReg accUserReg = accUserRegDao.queryAccUserRegByAuId(auId);
    	
    	//compare
    	assertNotNull(accUserReg);
    	
    	assertEquals(auId, accUserReg.getAuId().longValue());
    	assertEquals(loginName, accUserReg.getLoginName());
    	assertEquals(userType, accUserReg.getUserType().longValue());
    	assertEquals(status, accUserReg.getStatus().longValue());
    	assertEquals(regTime, accUserReg.getRegTime());
    	assertEquals(userCode, accUserReg.getUserCode());
    	assertEquals(mobile, accUserReg.getMobile());
    }
    
    @Test
    public void testQueryAccUserRegByAuId() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	// data
    	final long auId = 10L;
    	final String loginName = "雷公公";
    	final long userType = 1L;
    	final String recommendMobile = "我可以是推荐信息啦啦啦";
    	final long status = 3L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String auditor  = "1";
    	final Date auditTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400001";
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	//search result
    	final AccUserReg accUserReg = accUserRegDao.queryAccUserRegByAuId(auId);
    	
    	//compare
    	assertNotNull(accUserReg);
    	
    	assertEquals(auId, accUserReg.getAuId().longValue());
    	assertEquals(loginName, accUserReg.getLoginName());
    	assertEquals(userType, accUserReg.getUserType().longValue());
    	assertEquals(status, accUserReg.getStatus().longValue());
    	assertEquals(regTime, accUserReg.getRegTime());
    	assertEquals(auditor, accUserReg.getAuditor());
    	assertEquals(auditTime, accUserReg.getAuditTime());
    	assertEquals(userCode, accUserReg.getUserCode());
    }
    
    @Test
    public void testQueryAccUserRegByLoginName() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	// data
    	final long auId = 10L;
    	final String loginName = "雷公公";
    	final long userType = 1L;
    	final long status = 3L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String auditor  = "1";
    	final Date auditTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400001";
    	
    	//search result
    	final AccUserReg accUserReg = accUserRegDao.queryAccUserRegByLoginName(loginName);
    	
    	//compare
    	assertNotNull(accUserReg);
    	
    	assertEquals(auId, accUserReg.getAuId().longValue());
    	assertEquals(loginName, accUserReg.getLoginName());
    	assertEquals(userType, accUserReg.getUserType().longValue());
    	assertEquals(status, accUserReg.getStatus().longValue());
    	assertEquals(regTime, accUserReg.getRegTime());
    	assertEquals(auditor, accUserReg.getAuditor());
    	assertEquals(auditTime, accUserReg.getAuditTime());
    	assertEquals(userCode, accUserReg.getUserCode());
    }
    
    @Test
    public void testUpdateAuditorInfo() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	final String auditor = "管理员";
    	final Date auditTime = new Date();
    	
    	//update data
    	assertEquals(1, accUserRegDao.updateAuditorInfo(auId, auditor, auditTime));
    }
    
    @Test
    public void testQueryAuIdByUserCode() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String userCode = "2220160400001";
    	long auId = 10L;
    	
    	assertEquals(auId, accUserRegDao.queryAuIdByUserCode(userCode).longValue());
    	
    	userCode = "22342220160400001";
    	auId = 0L;
    	assertEquals(auId, accUserRegDao.queryAuIdByUserCode(userCode).longValue());
    }
    
    @Test
    public void testQueryUserInfoByUserCode() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String userCode = "2220160400001";
    	long auId = 10L;
    	String realName = "张良";
    	String mobile = "15123456789";
    	
    	AccUserInfoOfPrj a = accUserRegDao.queryUserInfoByUserCode(userCode);
    	
    	//compare
    	assertNotNull(a);
    	
    	assertEquals(auId, a.getAuId().longValue());
    	assertEquals(realName, a.getRealName());
    	assertEquals(mobile, a.getMobile());
    }
    
    @Test
    public void testQueryAccUserInfoByPrj() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String userCode = "2220160400001";
    	long auId = 10L;
    	String realName = "张良";
    	String mobile = "15123456789";
    	
    	AccUserInfoOfPrj auip = accUserRegDao.queryUserInfoByUserCode(userCode);
    	
    	//compare
    	assertNotNull(auip);
    	
    	assertEquals(auId, auip.getAuId().longValue());
    	assertEquals(realName, auip.getRealName());
    	assertEquals(mobile, auip.getMobile());
    	
    	userCode = "2220160400005";
    	auId = 20L;
    	realName = "太阳神科技公司";
    	mobile = "13812345678";
    	
    	auip = accUserRegDao.queryUserInfoByUserCode(userCode);
    	
    	//compare
    	assertNotNull(auip);
    	
    	assertEquals(auId, auip.getAuId().longValue());
    	assertEquals(realName, auip.getRealName());
    	assertEquals(mobile, auip.getMobile());
    	
    	userCode = "22342220160400001";
    	auip = accUserRegDao.queryUserInfoByUserCode(userCode);
    	//compare
    	assertNull(auip);
    }
    
    @Test
    public void testExistAccUserInfoByLoginNameOrMobile() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//
    	String loginName  = "雷公公";
    	String mobile = "13812345678";
    	
    	assertEquals(true, accUserRegDao.existAccUserInfoByLoginNameOrMobile(loginName));
    	assertEquals(true, accUserRegDao.existAccUserInfoByLoginNameOrMobile(mobile));
    	
    	loginName  = "雷公公1";
    	mobile = "18712345678";
    	assertEquals(false, accUserRegDao.existAccUserInfoByLoginNameOrMobile(loginName));
    	assertEquals(false, accUserRegDao.existAccUserInfoByLoginNameOrMobile(mobile));
    }
    
    @Test
    public void testExistLoginName() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String loginName = "雷公公";
    	
    	//compare
    	assertEquals(true, accUserRegDao.existLoginName(loginName));
    	
    	loginName = "雷公公1";
    	assertEquals(false, accUserRegDao.existLoginName(loginName));
    }
    
    @Test
    public void testUpdatePwd() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	final String newPassword = "132123";
    	
    	//update data
    	assertEquals(1, accUserRegDao.updatePwd(auId, newPassword));
    }
    
    @Test
    public void testExistAccountOfMobile() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	//person
    	String mobile = "15123456789";
    	assertEquals(true,accUserRegDao.existAccountOfMobile(mobile));
    	
    	//corp
    	mobile = "13812345678";
    	assertEquals(true,accUserRegDao.existAccountOfMobile(mobile));
    	
    	mobile = "11111111111";
    	assertEquals(false,accUserRegDao.existAccountOfMobile(mobile));
    }
    
    @Test
    public void testExistAccountOfUserCode() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String userCode = "2220160400001";
    	
    	//compare
    	assertEquals(true,accUserRegDao.existAccountOfUserCode(userCode));
    	
    	userCode = "123123123123";
    	assertEquals(false,accUserRegDao.existAccountOfUserCode(userCode));
    }
    
    @Test
    public void testValidate() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String userName = "雷公公";
    	String password = "Q+ONbhWjj3TExVkKsSv2/kyCuLE=";
    	String mobile = "15123456789";
    	
    	//compare
    	assertEquals(true, accUserRegDao.validate(userName, password)>0);
    	assertEquals(true, accUserRegDao.validate(mobile, password)>0);
    	
    	userName = "雷公公1";
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	
    	mobile = "12123456789";
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	
    	
    	mobile = "15123456789";
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	
    	userName = "雷公公";
    	password = "Q+ONbhWjj3TExVkKsSv2/kyCuLE1";
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	assertEquals(false, accUserRegDao.validate(userName, password)>0);
    	
    }
    
    @Test
    public void testQueryMobileByAuId() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	
    	//person
    	long auId = 10L;
    	String mobile = "15123456789";
    	assertEquals(mobile,accUserRegDao.queryMobileByAuId(auId));
    	//org
    	auId = 20L;
    	mobile = "13812345678";
    	assertEquals(mobile,accUserRegDao.queryMobileByAuId(auId));
    }
    
    @Test
    public void testValidate2() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	
    	final long auId = 10L;
    	String password = "Q+ONbhWjj3TExVkKsSv2/kyCuLE=";
    	
    	assertEquals(true, accUserRegDao.validate2(auId, password));
    	
    	password = "Q+ONbhWjj3TExVkKsSv2/kyCuLE1";
    	assertEquals(false, accUserRegDao.validate2(auId, password));
    }
    
    @Test
    public void testUpdateStatusByAuId() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	final long auId = 22L;
    	
    	final long c = accUserRegDao.updateStatusByAuId(auId, AccountStatus.AUDITED.longValue(), AccountStatus.AUDITING.longValue());
    	
    	//compare
    	assertEquals(1,c);
    	
    	//search result
    	final AccUserReg accUserReg = accUserRegDao.queryAccUserRegByAuId(auId);
    	
    	//compare
    	assertNotNull(accUserReg);
    	
    	assertEquals(AccountStatus.AUDITED.longValue(), accUserReg.getStatus().longValue());
    }
    
    @Test
    public void testQueryAuIdByLoginNameOrMobile() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	final String loginName = "雷公公";
    	final String mobile = "15123456789";
    	final long auId  = 10L;
    	
    	assertEquals(auId, accUserRegDao.queryAuIdByLoginNameOrMobile(loginName).longValue());
    	assertEquals(auId, accUserRegDao.queryAuIdByLoginNameOrMobile(mobile).longValue());
    }
    
    @Test
    public void testAuthenticate() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final String loginName = "雷公公";
    	final String realName = "张良";
    	final String mobile = "15123456789";
    	final long status = 3L;
    	final long type = 1L;
    	final long auId = 10L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	final long recInfoModCount = 0;
    	
    	//search result
    	final AuthInfo authInfo = accUserRegDao.authenticate(auId);
    	
    	//compare
    	assertNotNull(authInfo);
    	
        assertEquals(loginName, authInfo.getLoginName());
        assertEquals(realName, authInfo.getRealName());
        assertEquals(mobile, authInfo.getMobile());
        assertEquals(status, authInfo.getStatus().longValue());
        assertEquals(type, authInfo.getUserType().longValue());
        assertEquals(recInfoModCount, authInfo.getRecInfoModiCount().longValue());
    }
    
    @Test
    public void testQueryStatusByAuId() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	final long status = 2L;
    	
    	//compare
    	assertEquals(status, accUserRegDao.queryStatusByAuId(auId).longValue());
    	
    }
    
    @Test
    public void testUpdateMobileOfAuId() throws Exception{
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	final String mobile = "1234556666666";
    	
    	//update action
    	assertEquals(1, accUserRegDao.updateMobileOfAuId(auId, mobile));
    	
    	final AccUserReg accUserReg = accUserRegDao.queryAccUserRegByAuId(auId);
    	
    	//compare
    	assertNotNull(accUserReg);
    	
    	assertEquals(mobile, accUserReg.getMobile());
    }
}
