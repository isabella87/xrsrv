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

import com.xinran.xrsrv.persistence.AccCorpInfo;
import com.xinran.xrsrv.persistence.AccCorpInfoDao;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xrsrv.test.BaseTestFixture;

public class TestAccCorpInfoDao extends BaseTestFixture {
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
    public void testCreateAccCorpInfo() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	AccUserRegDao accUserRegDao = new AccUserRegDao();
    	accUserRegDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 0L;
    	final String realName= "你好呀";
    	final String mobile = "13123123123";
    	final String idCard = "12345678901";
   
    	//data
    	final String loginName = "你好呀";
    	final String password = "123123";
    	final long userType = 1L;
    	final String recommendMobile = "12345678901";
    	final long status = 2L;
    	final Date regTime = new Date();
    	final String userCode = "1231312312312323";
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	final long recInfoModiCount = 0L;
    	
    	//create Data
    	long c = accUserRegDao.createAccUserReg( loginName, password, userType, mobile,"", recommendMobile, status, regTime, userCode, recInfoModiCount);
    	
    	//create Data
    	long c1 = accCorpInfoDao.createAccCorpInfo(auId, realName, idCard);
    	
    	//compare
    	assertEquals(1, c);
    	assertEquals(1, c1);
    	
    	final AccCorpInfo accCorpInfo = accCorpInfoDao.queryAccCorpInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accCorpInfo);
    	
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(realName, accCorpInfo.getRealName());
    	assertEquals(idCard, accCorpInfo.getIdCard());
    }
    
    @Test
    public void createUpdateCorpAccountInfoByAuId() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 20L;
    	final String lawName = "王天华";
	    final String lawIdCard = "46400012312312312";
	    final String bassLic = "23423423";
	    final String taxLic = "sadfasdfasd";
	    final String orgCodeNo = "asdfasdfwerw";
	    final String accUserName = "asdfasdfwq3rwer";
	    final String account = "131231312321";
	    final String accBank = "交通银行五角场支行";
	    final String corpName = "werwer";
	    
	    //update
	    assertEquals(1, accCorpInfoDao.updateCorpAccountInfoByAuId(auId, lawName, lawIdCard, bassLic, taxLic, orgCodeNo, accUserName, account, accBank, corpName));
	    
	  //search result
    	final AccCorpInfo accCorpInfo = accCorpInfoDao.queryAccCorpInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accCorpInfo);
    	
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(lawName, accCorpInfo.getLawName());
    	assertEquals(lawIdCard, accCorpInfo.getLawIdCard());
    	assertEquals(bassLic, accCorpInfo.getBussLic());
    	assertEquals(taxLic, accCorpInfo.getTaxLic());
    	assertEquals(orgCodeNo, accCorpInfo.getOrgCodeNo());
    	assertEquals(accUserName, accCorpInfo.getAccUserName());
    	assertEquals(account, accCorpInfo.getAccount());
    	assertEquals(accBank, accCorpInfo.getAccBank());
    	assertEquals(corpName, accCorpInfo.getOrgName());
    }
    
    @Test
    public void createUpdateBasicInfoByAuId() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 20L;
    	final String address = "河南省信阳市";
	    final String homePhone = "464000";
	    final String postalCode = "23423423";
	    final String position = "sadfasdfasd";
	    final String company = "asdfasdfwerw";
	    final String companyType = "asdfasdfwq3rwer";
	    final String qqNumber = "131231312321";
	    
	    //update
	    assertEquals(1, accCorpInfoDao.updateBasicInfoByAuId(auId, address, homePhone, postalCode, position, company, companyType, qqNumber));
	    
	  //search result
    	final AccCorpInfo accCorpInfo = accCorpInfoDao.queryAccCorpInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accCorpInfo);
    	
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(address, accCorpInfo.getAddress());
    	assertEquals(homePhone, accCorpInfo.getHomePhone());
    	assertEquals(position, accCorpInfo.getPosition());
    	assertEquals(company, accCorpInfo.getCompany());
    	assertEquals(companyType, accCorpInfo.getCompanyType());
    	assertEquals(qqNumber, accCorpInfo.getQqNumber());
    }
    
    @Test
    public void testExistAccCorpInfoByIdCard() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	String idCard  = "213456196812120123";
    	
    	//compare
    	assertEquals(true,accCorpInfoDao.existAccCorpInfoByIdCard(idCard));
    	
    	idCard  = "21323451988012340987";
    	assertEquals(false,accCorpInfoDao.existAccCorpInfoByIdCard(idCard));
    }
    
    @Test
    public void testQueryAccCorpInfoByAuId() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    			
    	// data
    	final long auId = 20L;
    	final String loginName = "神战之太阳神他妹";
    	final long status = 2L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400005";
    	final String address = "上海市杨浦区大连路888号";
    	final String homePhone = "";
    	final String position = "副经理";
    	final String company = "太阳神科技公司";
    	final String companyType = "电脑科技";
    	final String qqNumber = "";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 13, 13, 13).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final String realName = "李东";
    	final String idCard = "213456196812120123";
    	final String mobile = "13812345678";
    	final String email = "";
    	final String orgName = "太阳神科技公司";
    	final String bussLic = "64843278";
    	final String taxLic = "64843278";
    	final String orgCodeNo = "64843278";
    	final String lawName = "刘欢";
    	final String lawIdCard = "123456198101240931";
    	final String accUserName = "太阳神科技公司杨浦支行";
    	final String account = "19011234";
    	final String accBank = "交通银行杨浦支行";
    	final long userType = 2L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	//search result
    	final AccCorpInfo accCorpInfo = accCorpInfoDao.queryAccCorpInfoByAuId(auId);
    	
    	//compare
    	assertNotNull(accCorpInfo);
    	
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(loginName, accCorpInfo.getLoginName());
    	assertEquals(status, accCorpInfo.getStatus().longValue());
    	assertEquals(regTime, accCorpInfo.getRegTime());
    	assertEquals(userCode, accCorpInfo.getUserCode());
    	assertEquals(address, accCorpInfo.getAddress());
    	assertEquals(homePhone, accCorpInfo.getHomePhone());
    	assertEquals(position, accCorpInfo.getPosition());
    	assertEquals(company, accCorpInfo.getCompany());
    	assertEquals(companyType, accCorpInfo.getCompanyType());
    	assertEquals(qqNumber, accCorpInfo.getQqNumber());
    	assertEquals(updateTime, accCorpInfo.getUpdateTime());
    	assertEquals(createTime, accCorpInfo.getCreateTime());
    	assertEquals(realName, accCorpInfo.getRealName());
    	assertEquals(idCard, accCorpInfo.getIdCard());
    	assertEquals(mobile, accCorpInfo.getMobile());
    	assertEquals(email, accCorpInfo.getEmail());
    	assertEquals(orgName, accCorpInfo.getOrgName());
    	assertEquals(bussLic, accCorpInfo.getBussLic());
    	assertEquals(taxLic, accCorpInfo.getTaxLic());
    	assertEquals(orgCodeNo, accCorpInfo.getOrgCodeNo());
    	assertEquals(lawName, accCorpInfo.getLawName());
    	assertEquals(lawIdCard, accCorpInfo.getLawIdCard());
    	assertEquals(accUserName, accCorpInfo.getAccUserName());
    	assertEquals(account, accCorpInfo.getAccount());
    	assertEquals(accBank, accCorpInfo.getAccBank());
    	assertEquals(userType, accCorpInfo.getUserType().longValue());
    	assertEquals(allowInvest, accCorpInfo.getAllowInvest().longValue());
    	assertEquals(allowBorrow, accCorpInfo.getAllowBorrow().longValue());
    }
    
    @Test
    public void testUpdateEmailByAuId() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//data
    	final long auId = 23L;
    	String email = "24234@qq.com";
    	
    	assertEquals(false,accCorpInfoDao.updateEmailByAuId(auId, email)>0);
    	
    	email = "122344@163.com";
    	assertEquals(true,accCorpInfoDao.updateEmailByAuId(auId, email)>0);
    	
    	email = "123@163.com";
    	assertEquals(false,accCorpInfoDao.updateEmailByAuId(auId, email)>0);
    }
    
    @Test
    public void testQueryAccCorpInfoList() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//search condition
		final Date startRegTime = DateTimeUtils.createLocalDate(2014, 1, 1).getTime();
		final Date endRegTime = DateTimeUtils.createLocalDate(2017, 1, 1).getTime();
		final Date startAuditTime = DateTimeUtils.createLocalDate(2014, 1, 1).getTime();
		final Date endAuditTime = DateTimeUtils.createLocalDate(2017, 1, 1).getTime();
		final long sstatus = 2L;
		final long sjxstatus = 1L;
		final String sloginName = "神";
		final String srealName = "李";
		final String smobile = "1";
		final String sauditor = "2";
		
		//search result
		List<AccCorpInfo> list = accCorpInfoDao.queryAccCorpInfoList(startRegTime, endRegTime, 
				 startAuditTime, endAuditTime, sstatus, sjxstatus, sloginName, srealName, smobile, sauditor, 0, 2000);
		
		//compare
		assertEquals(1, list.size());
		
		// data
    	final long auId = 20L;
    	final String loginName = "神战之太阳神他妹";
    	final long status = 2L;
    	final long jxStatus = 1L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400005";
    	final String address = "上海市杨浦区大连路888号";
    	final String homePhone = "";
    	final String position = "副经理";
    	final String company = "太阳神科技公司";
    	final String companyType = "电脑科技";
    	final String qqNumber = "";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 13, 13, 13).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final String realName = "李东";
    	final String idCard = "213456196812120123";
    	final String mobile = "13812345678";
    	final String email = "";
    	final String orgName = "太阳神科技公司";
    	final String bussLic = "64843278";
    	final String taxLic = "64843278";
    	final String orgCodeNo = "64843278";
    	final String lawName = "刘欢";
    	final String lawIdCard = "123456198101240931";
    	final String accUserName = "太阳神科技公司杨浦支行";
    	final String account = "19011234";
    	final String accBank = "交通银行杨浦支行";
    	final long userType = 2L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	//search result
    	final AccCorpInfo accCorpInfo = list.get(0);
    	
    	//compare
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(loginName, accCorpInfo.getLoginName());
    	assertEquals(status, accCorpInfo.getStatus().longValue());
    	assertEquals(jxStatus, accCorpInfo.getJxStatus().longValue());
    	assertEquals(regTime, accCorpInfo.getRegTime());
    	assertEquals(userCode, accCorpInfo.getUserCode());
    	assertEquals(address, accCorpInfo.getAddress());
    	assertEquals(homePhone, accCorpInfo.getHomePhone());
    	assertEquals(position, accCorpInfo.getPosition());
    	assertEquals(company, accCorpInfo.getCompany());
    	assertEquals(companyType, accCorpInfo.getCompanyType());
    	assertEquals(qqNumber, accCorpInfo.getQqNumber());
    	assertEquals(updateTime, accCorpInfo.getUpdateTime());
    	assertEquals(createTime, accCorpInfo.getCreateTime());
    	assertEquals(realName, accCorpInfo.getRealName());
    	assertEquals(idCard, accCorpInfo.getIdCard());
    	assertEquals(mobile, accCorpInfo.getMobile());
    	assertEquals(email, accCorpInfo.getEmail());
    	assertEquals(orgName, accCorpInfo.getOrgName());
    	assertEquals(bussLic, accCorpInfo.getBussLic());
    	assertEquals(taxLic, accCorpInfo.getTaxLic());
    	assertEquals(orgCodeNo, accCorpInfo.getOrgCodeNo());
    	assertEquals(lawName, accCorpInfo.getLawName());
    	assertEquals(lawIdCard, accCorpInfo.getLawIdCard());
    	assertEquals(accUserName, accCorpInfo.getAccUserName());
    	assertEquals(account, accCorpInfo.getAccount());
    	assertEquals(accBank, accCorpInfo.getAccBank());
    	assertEquals(userType, accCorpInfo.getUserType().longValue());
    	assertEquals(allowInvest, accCorpInfo.getAllowInvest().longValue());
    	assertEquals(allowBorrow, accCorpInfo.getAllowBorrow().longValue());
    }
    
    
    @Test
    public void testQueryAccCorpInfoByLoginNameOrMobile() throws Exception{
    	AccCorpInfoDao accCorpInfoDao = new AccCorpInfoDao();
    	accCorpInfoDao.setSessionFactory(getSessionFactory());
    	
    	//search condition
    	final String loginNameOrMobile  = "13812345678";
    	
    	final AccCorpInfo accCorpInfo = accCorpInfoDao.queryAccCorpInfoByLoginNameOrMobile(loginNameOrMobile);
    	
    	// data
    	final long auId = 20L;
    	final String loginName = "神战之太阳神他妹";
    	final long status = 2L;
    	final long jxStatus = 1L;
    	final Date regTime = DateTimeUtils.createLocalDate(2014, 8, 18, 10, 30, 23).getTime();
    	final String userCode = "2220160400005";
    	final String address = "上海市杨浦区大连路888号";
    	final String homePhone = "";
    	final String position = "副经理";
    	final String company = "太阳神科技公司";
    	final String companyType = "电脑科技";
    	final String qqNumber = "";
    	final Date updateTime = DateTimeUtils.createLocalDate(2016, 3, 1, 13, 13, 13).getTime();
    	final Date createTime = DateTimeUtils.createLocalDate(2016, 3, 1, 12, 12, 12).getTime();
    	final String realName = "李东";
    	final String idCard = "213456196812120123";
    	final String mobile = "13812345678";
    	final String email = "";
    	final String orgName = "太阳神科技公司";
    	final String bussLic = "64843278";
    	final String taxLic = "64843278";
    	final String orgCodeNo = "64843278";
    	final String lawName = "刘欢";
    	final String lawIdCard = "123456198101240931";
    	final String accUserName = "太阳神科技公司杨浦支行";
    	final String account = "19011234";
    	final String accBank = "交通银行杨浦支行";
    	final long userType = 2L;
    	final long allowInvest = 1L;
    	final long allowBorrow = 0L;
    	
    	//compare
    	assertEquals(auId, accCorpInfo.getAuId().longValue());
    	assertEquals(loginName, accCorpInfo.getLoginName());
    	assertEquals(status, accCorpInfo.getStatus().longValue());
    	assertEquals(jxStatus, accCorpInfo.getJxStatus().longValue());
    	assertEquals(regTime, accCorpInfo.getRegTime());
    	assertEquals(userCode, accCorpInfo.getUserCode());
    	assertEquals(address, accCorpInfo.getAddress());
    	assertEquals(homePhone, accCorpInfo.getHomePhone());
    	assertEquals(position, accCorpInfo.getPosition());
    	assertEquals(company, accCorpInfo.getCompany());
    	assertEquals(companyType, accCorpInfo.getCompanyType());
    	assertEquals(qqNumber, accCorpInfo.getQqNumber());
    	assertEquals(updateTime, accCorpInfo.getUpdateTime());
    	assertEquals(createTime, accCorpInfo.getCreateTime());
    	assertEquals(realName, accCorpInfo.getRealName());
    	assertEquals(idCard, accCorpInfo.getIdCard());
    	assertEquals(mobile, accCorpInfo.getMobile());
    	assertEquals(email, accCorpInfo.getEmail());
    	assertEquals(orgName, accCorpInfo.getOrgName());
    	assertEquals(bussLic, accCorpInfo.getBussLic());
    	assertEquals(taxLic, accCorpInfo.getTaxLic());
    	assertEquals(orgCodeNo, accCorpInfo.getOrgCodeNo());
    	assertEquals(lawName, accCorpInfo.getLawName());
    	assertEquals(lawIdCard, accCorpInfo.getLawIdCard());
    	assertEquals(accUserName, accCorpInfo.getAccUserName());
    	assertEquals(account, accCorpInfo.getAccount());
    	assertEquals(accBank, accCorpInfo.getAccBank());
    	assertEquals(userType, accCorpInfo.getUserType().longValue());
    	assertEquals(allowInvest, accCorpInfo.getAllowInvest().longValue());
    	assertEquals(allowBorrow, accCorpInfo.getAllowBorrow().longValue());
    	
    }
    
   
}
