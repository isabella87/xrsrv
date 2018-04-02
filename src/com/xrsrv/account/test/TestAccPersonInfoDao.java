package com.xrsrv.account.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.xx.armory.commons.DateTimeUtils;
import org.xx.armory.db.unittest.DbUnitHelper;
import org.xx.armory.db.unittest.impl.OracleDbUnitHelper;

import com.xinran.xrsrv.persistence.AccPersonInfo;
import com.xinran.xrsrv.persistence.AccPersonInfoDao;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xrsrv.test.BaseTestFixture;

public class TestAccPersonInfoDao extends BaseTestFixture {
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
    public void testCreateAccPersonInfo() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 0L;
    	final String realName= "你好呀";
    	final String mobile = "13123123123";
    	final String idCard = "12345678901";
    	final String orgCode = "orgCode";
   
    	//data
    	final String loginName = "你好呀";
    	final String password = "123123";
    	final long userType = 1L;
    	final String recommendMobile = "12345678901";
    	final long status = 2L;
    	final Date regTime = new Date();
    	final String userCode = "1231312312312323";
    	final long recInfoModiCount = 0L;
    	
    	//create Data
    	long c = accUserRegDao.createAccUserReg(loginName, password, userType, mobile,"", recommendMobile, status, regTime, userCode, recInfoModiCount);
    	
    	//create Data
    	long c1 = accPersonInfoDao.createAccPersonInfo(auId, realName, idCard, orgCode);
    	
    	//compare
    	assertEquals(1, c);
    	assertEquals(1, c1);
    	
    	final AccPersonInfo accPersonInfo = accPersonInfoDao.queryAccPersonInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accPersonInfo);
    	
    	assertEquals(auId, accPersonInfo.getAuId().longValue());
    	assertEquals(realName, accPersonInfo.getRealName());
    	assertEquals(idCard, accPersonInfo.getIdCard());
    	assertEquals(orgCode, accPersonInfo.getOrgCode());
    }
    
    @Test
    public void createUpdateBasicInfoByAuId() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	final String address = "河南省信阳市";
	    final String homePhone = "464000";
	    final String postalCode = "23423423";
	    final String position = "sadfasdfasd";
	    final String company = "asdfasdfwerw";
	    final String companyType = "asdfasdfwq3rwer";
	    final String qqNumber = "131231312321";
	    
	    //update
	    assertEquals(1, accPersonInfoDao.updateBasicInfoByAuId(auId, address, homePhone, postalCode, position, company, companyType, qqNumber));
	    
	  //search result
    	final AccPersonInfo accPersonInfo = accPersonInfoDao.queryAccPersonInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accPersonInfo);
    	
    	assertEquals(auId, accPersonInfo.getAuId().longValue());
    	assertEquals(address, accPersonInfo.getAddress());
    	assertEquals(homePhone, accPersonInfo.getHomePhone());
    	assertEquals(position, accPersonInfo.getPosition());
    	assertEquals(company, accPersonInfo.getCompany());
    	assertEquals(companyType, accPersonInfo.getCompanyType());
    	assertEquals(qqNumber, accPersonInfo.getQqNumber());
    }
    
    @Test
    public void testExistAccPersonInfoByIdCard() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String idCard  = "2132451988012340987";
    	
    	//compare
    	assertEquals(true,accPersonInfoDao.existAccPersonInfoByIdCard(idCard));
    	
    	idCard  = "21323451988012340987";
    	assertEquals(false,accPersonInfoDao.existAccPersonInfoByIdCard(idCard));
    }
    
    @Test
    public void testQueryAccPersonInfoByAuId() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	// data
    	final long auId = 10L;
    	final String loginName = "雷公公";
    	final long status = 2L;
    	final long jxStatus = 0L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400001";
    	final String address = "上海市嘉定区同济大学";
    	final String homePhone = "12345678";
    	final String position = "教员";
    	final String company = "无";
    	final String companyType = "无";
    	final String qqNumber = "123456789";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 11, 11, 11).getTime();
    	final String realName = "张良";
    	final String idCard = "2132451988012340987";
    	final String mobile = "15123456789";
    	final String email = "123@163.com";
    	final long userType = 1L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	//search result
    	final AccPersonInfo accPersonInfo = accPersonInfoDao.queryAccPersonInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accPersonInfo);
    	
    	assertEquals(auId, accPersonInfo.getAuId().longValue());
    	assertEquals(loginName, accPersonInfo.getLoginName());
    	assertEquals(status, accPersonInfo.getStatus().longValue());
    	assertEquals(regTime, accPersonInfo.getRegTime());
    	assertEquals(userCode, accPersonInfo.getUserCode());
    	assertEquals(address, accPersonInfo.getAddress());
    	assertEquals(homePhone, accPersonInfo.getHomePhone());
    	assertEquals(position, accPersonInfo.getPosition());
    	assertEquals(company, accPersonInfo.getCompany());
    	assertEquals(companyType, accPersonInfo.getCompanyType());
    	assertEquals(qqNumber, accPersonInfo.getQqNumber());
    	assertEquals(updateTime, accPersonInfo.getUpdateTime());
    	assertEquals(createTime, accPersonInfo.getCreateTime());
    	assertEquals(realName, accPersonInfo.getRealName());
    	assertEquals(idCard, accPersonInfo.getIdCard());
    	assertEquals(mobile, accPersonInfo.getMobile());
    	assertEquals(email, accPersonInfo.getEmail());
    	assertEquals(userType, accPersonInfo.getUserType().longValue());
    }
    
    @Test
    public void testUpdateEmailByAuId() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 10L;
    	String email = "123@163.com";
    	
    	assertEquals(false,accPersonInfoDao.updateEmailByAuId(auId, email)>0);
    	
    	email = "122344@163.com";
    	assertEquals(true,accPersonInfoDao.updateEmailByAuId(auId, email)>0);
    	
    	email = "24234@qq.com";
    	assertEquals(false,accPersonInfoDao.updateEmailByAuId(auId, email)>0);
    }
    
    @Test
    public void testQueryAccPersonInfoList() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
   
		//search condition
		final Date startTime = DateTimeUtils.createLocalDate(2014, 1, 1, 1, 1, 1).getTime();
		final Date endTime = DateTimeUtils.createLocalDate(2017, 1, 1, 1, 1, 1).getTime();
		final long sstatus = 2L;
		final long sJxStatus = 0L;
		final String sloginName = "雷";
		final String srealName = "张";
		final String smobile = "15";
		final String srecommendMobile = "我";
		
		//search result
		List<AccPersonInfo> list  = accPersonInfoDao.queryAccPersonInfoList(startTime, endTime, sstatus,
				                                sloginName, srealName, smobile, srecommendMobile, 0, 2000);
		
		//compare
		assertEquals(1, list.size());
		
		AccPersonInfo accPersonInfo = list.get(0);
		
		final long auId = 10L;
    	final String loginName = "雷公公";
    	final long status = 2L;
    	final long jxStatus = 0L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400001";
    	final String address = "上海市嘉定区同济大学";
    	final String homePhone = "12345678";
    	final String position = "教员";
    	final String company = "无";
    	final String companyType = "无";
    	final String qqNumber = "123456789";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 11, 11, 11).getTime();
    	final String realName = "张良";
    	final String idCard = "2132451988012340987";
    	final String mobile = "15123456789";
    	final String email = "123@163.com";
    	final long userType = 1L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
		//assertEquals()
    	
    	assertEquals(auId, accPersonInfo.getAuId().longValue());
    	assertEquals(loginName, accPersonInfo.getLoginName());
    	assertEquals(status, accPersonInfo.getStatus().longValue());
    	assertEquals(regTime, accPersonInfo.getRegTime());
    	assertEquals(userCode, accPersonInfo.getUserCode());
    	assertEquals(address, accPersonInfo.getAddress());
    	assertEquals(homePhone, accPersonInfo.getHomePhone());
    	assertEquals(position, accPersonInfo.getPosition());
    	assertEquals(company, accPersonInfo.getCompany());
    	assertEquals(companyType, accPersonInfo.getCompanyType());
    	assertEquals(qqNumber, accPersonInfo.getQqNumber());
    	assertEquals(updateTime, accPersonInfo.getUpdateTime());
    	assertEquals(createTime, accPersonInfo.getCreateTime());
    	assertEquals(realName, accPersonInfo.getRealName());
    	assertEquals(idCard, accPersonInfo.getIdCard());
    	assertEquals(mobile, accPersonInfo.getMobile());
    	assertEquals(email, accPersonInfo.getEmail());
    	assertEquals(userType, accPersonInfo.getUserType().longValue());	
    }
    
    @Test
    public void testQueryAccPersonInfoByLoginNameOrMobile() throws Exception{
    	AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	
    	final String logNameOrMobile = "雷公公";
    	
    	AccPersonInfo accPersonInfo = accPersonInfoDao.queryAccPersonInfoByLoginNameOrMobile(logNameOrMobile);
    	
    	//compare
    	assertNotNull(accPersonInfo);
    	
    	//
		final long auId = 10L;
    	final String loginName = "雷公公";
    	final long status = 2L;
    	final long jxStatus = 0L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400001";
    	final String address = "上海市嘉定区同济大学";
    	final String homePhone = "12345678";
    	final String position = "教员";
    	final String company = "无";
    	final String companyType = "无";
    	final String qqNumber = "123456789";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 11, 11, 11).getTime();
    	final String realName = "张良";
    	final String idCard = "2132451988012340987";
    	final String mobile = "15123456789";
    	final String email = "123@163.com";
    	final long userType = 1L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	assertEquals(auId, accPersonInfo.getAuId().longValue());
    	assertEquals(loginName, accPersonInfo.getLoginName());
    	assertEquals(status, accPersonInfo.getStatus().longValue());
    	assertEquals(regTime, accPersonInfo.getRegTime());
    	assertEquals(userCode, accPersonInfo.getUserCode());
    	assertEquals(address, accPersonInfo.getAddress());
    	assertEquals(homePhone, accPersonInfo.getHomePhone());
    	assertEquals(position, accPersonInfo.getPosition());
    	assertEquals(company, accPersonInfo.getCompany());
    	assertEquals(companyType, accPersonInfo.getCompanyType());
    	assertEquals(qqNumber, accPersonInfo.getQqNumber());
    	assertEquals(updateTime, accPersonInfo.getUpdateTime());
    	assertEquals(createTime, accPersonInfo.getCreateTime());
    	assertEquals(realName, accPersonInfo.getRealName());
    	assertEquals(idCard, accPersonInfo.getIdCard());
    	assertEquals(mobile, accPersonInfo.getMobile());
    	assertEquals(email, accPersonInfo.getEmail());
    	assertEquals(userType, accPersonInfo.getUserType().longValue());	
    }
    
   @Test
   public void testUpdateNameAndIdCardByAuId(){
		AccPersonInfoDao accPersonInfoDao = new AccPersonInfoDao();
    	accPersonInfoDao.setSessionFactory(getSessionFactory());
    	final long auId = 10L;
    	final String name = "张三三";
    	final String id_card = "432503199901010101";
    	long count = accPersonInfoDao.updateNameAndIdCardByAuId(auId,name, id_card);
    	assertEquals(1, count);
   }
}
