package com.xrsrv.account;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.commons.Converter;
import org.xx.armory.commons.DateTimeUtils;
import org.xx.armory.config.ConfigurationProviders;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.security.CaptchaImageProvider;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;
import org.xx.armory.web.TempFileObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.xinran.xrsrv.persistence.AccCorpInfoDao;
import com.xinran.xrsrv.persistence.AccMsgDao;
import com.xinran.xrsrv.persistence.AccPersonInfoDao;
import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xinran.xrsrv.persistence.AccUserRegPwdHistoryDao;
import com.xinran.xrsrv.persistence.AccountSignInfo;
import com.xinran.xrsrv.persistence.AppInfo;
import com.xinran.xrsrv.persistence.AppInfoDao;
import com.xrsrv.msg.MsgCode;
import com.xrsrv.msg.MsgService;
import com.xrsrv.system.enumerate.AccountStatus;
import com.xrsrv.system.enumerate.AccountType;
import com.xrsrv.system.enumerate.UpdatePwdType;

/**
 * 
 * @author Isabella
 *
 */
public class AccountService extends AggregatedService {
	static final Pattern LOGIN_NAME_PATTERN = Pattern.compile("^[\u4e00-\u9fa5a-z0-9_]{4,15}$",
			Pattern.CASE_INSENSITIVE);
	static final Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Za-z0-9_]{6,18}$",
			Pattern.CASE_INSENSITIVE);
	static final Pattern ID_CARD_PATTERN = Pattern.compile("^[0-9]{15}|[0-9]{17}[0-9X]$",
			Pattern.CASE_INSENSITIVE);
	static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-8][0-9]{9}$",
			Pattern.CASE_INSENSITIVE);

	private static class InitSmsInfo {
		
		/**
		 * 二维码地址(即m注册地址)
		 */
		static String RCODE_URL = ConfigurationProviders.getInstance()
				.getSection("rcode").get("url").trim();
		/**
		 * 激活码短信信息
		 */
		static String ACTIVE_MOBILE_INFO = ConfigurationProviders.getInstance()
				.getSection("active").get("mobile").trim();
		/**
		 * 修改手机号短信信息
		 */
		static String MODIFY_MOBILE_INFO = ConfigurationProviders.getInstance()
				.getSection("modify").get("mobile").trim();
		/**
		 * 重置密码短信信息
		 */
		static String RESET_CODE_INFO = ConfigurationProviders.getInstance().getSection("reset")
				.get("code").trim();
		/**
		 * 密码短信信息
		 */
		static String RESET_PASSWORD_INFO = ConfigurationProviders.getInstance()
				.getSection("reset").get("password").trim();
	}
	
	static String ICON_PATH = "/logo.png";

	private static Map<String, Map<String, Integer>> mapIdCheck = new ConcurrentHashMap<String, Map<String, Integer>>();
	private static Map<String, AbstractQueue<Date>> phoneCountMap = new HashMap<String, AbstractQueue<Date>>();

	private static Map<String, AbstractQueue<Date>> phoneCountMap2 = new HashMap<String, AbstractQueue<Date>>();
	private static Map<String, AbstractQueue<Date>> ipIntervalMap2 = new HashMap<String, AbstractQueue<Date>>();
	private static Map<String, AbstractQueue<Date>> ipCountMap2 = new HashMap<String, AbstractQueue<Date>>();

	public static int RANDOM_COUNT = 6;

	// 校验码
	private static int VALIDE_CODE[] = { 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

	// 权重
	private static int[] WEIGHT = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 };

	public Logger logger = Logger.getLogger(AccountService.class);

	// 校验身份证号码
	public boolean isValidateCodeBy18IdCard(String idCard) {
		if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
			return false;
		}

		int sum = 0;
		int validCode = 0;
		if (idCard == null || idCard.length() != 18) {
			return false;
		}
		String validLoc = idCard.substring(17);
		if (validLoc.equalsIgnoreCase("X")) {
			validCode = 10;
		} else {
			validCode = Integer.valueOf(validLoc);
		}

		for (int i = 0; i < 17; i++) {
			sum += WEIGHT[i] * Integer.valueOf(idCard.substring(i, i + 1));
		}

		return VALIDE_CODE[sum % 11] == validCode;
	}

	// 验证身份证生日是否合法
	public boolean isValidateBirthBy18IdCard(String idCard) {
		if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
			return false;
		}

		if (idCard == null || idCard.length() != 18) {
			return false;
		}

		try {
			final int year = Integer.valueOf(idCard.substring(6, 10)); // 年
			final int month = Integer.valueOf(idCard.substring(10, 12)) - 1; // 月
			final int day = Integer.valueOf(idCard.substring(12, 14)); // 日
			final Calendar calendar = DateTimeUtils.createLocalDate(year, month, day);
			if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH)
					&& day == calendar.get(Calendar.DAY_OF_MONTH)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	// 通过身份证号来检查身份证已经验证的次数
	public int checkNumByIdCard(@ParamName("idCard") String idCard) {
		Map<String, Integer> map = getContainer();
		if (map == null) {
			return 0;
		}
		final int i;
		if (map.containsKey(idCard)) {
			i = map.get(idCard);
		} else {
			i = 0;
		}
		return i;
	}

	private Map<String, Integer> getContainer() {
		String key = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		removeYesterdayContainer();
		if (mapIdCheck.containsKey(key)) {
			return mapIdCheck.get(key);
		} else {
			mapIdCheck.put(key, new ConcurrentHashMap<String, Integer>());
			return mapIdCheck.get(key);
		}
	}

	public void removeYesterdayContainer() {
		Date yesterday = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(yesterday);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		yesterday = calendar.getTime();
		String key = DateFormatUtils.format(yesterday, "yyyy-MM-dd");
		mapIdCheck.remove(key);
	}

	public boolean additionCheck(String mobile, String ipAddr) {

		if (StringUtils.isEmpty(mobile)) {
			throw new ServiceException("10*无法获取手机号码，发送短信失败！");
		}

		if (StringUtils.isEmpty(ipAddr)) {
			throw new ServiceException("11*无法获取IP地址，发送短信失败！");
		}

		AbstractQueue<Date> phoneCountQueue = phoneCountMap.get(mobile);
		if (phoneCountQueue == null) {
			phoneCountQueue = new ArrayBlockingQueue<Date>(5);
			phoneCountQueue.add(new Date());
			phoneCountMap.put(mobile, phoneCountQueue);
		} else {
			// 删除超过10分钟的对象
			Date now = new Date();
			Iterator<Date> it = phoneCountQueue.iterator();
			while (it.hasNext()) {
				Date one = it.next();
				if (now.getTime() - one.getTime() > 10 * 60 * 1000L) {
					it.remove();
				}
			}

			// 判断队列长度
			if (phoneCountQueue.size() < 10) {
				phoneCountQueue.add(now);
			} else {
				throw new ServiceException("12*相同号码10分钟内不能发送超过10条短信！");
			}
		}
		return true;
	}

	/**
	 * 向个人用户发送手机激活码
	 * 
	 * @param mobile
	 *            手机号
	 * @param captchaCode
	 *            验证码
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean sendUserMobileCode(@ParamName("mobile") final String mobile,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			final ServiceContext ctx) {

		captchaCode = captchaCode.replace(" ", "");
		final AuthenticationToken token = ctx.getToken();
		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}

		if (getDao(AccUserRegDao.class).existAccountOfMobile(mobile) != null) {
			throw new ServiceException("56*该手机号已注册!");
		}

		// 10分钟最多发10条
		String ipAddr = ctx.getRemoteAddr().toString();
		additionCheck(mobile, ipAddr);
		return sendCodeToMobile(mobile);
	}

	private boolean sendCodeToMobile(final String mobile) {
		final String code = MsgService.newCode(mobile, new Date());
		final Date now = new Date();
		AccMsgDao amDao =  getDao(AccMsgDao.class);
		amDao.createRegAccountCode(mobile, code, now);
		long mId =amDao.getLastRowLongId();
		final String content = String.format(InitSmsInfo.ACTIVE_MOBILE_INFO, code);
		if (!getService(MsgService.class).sendMsg(mobile, content, MsgCode.REG_ACCOUNT, mId, now)) {
			throw new ServiceException("54*发送短信失败!");
		}
		return true;
	}

	/**
	 * 向机构用户发送手机激活码
	 * 
	 * @param mobile
	 *            手机号
	 * @param captchaCode
	 *            激活码
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean sendCorpMobileCode(@ParamName("mobile") final String mobile,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			final ServiceContext ctx) {
		captchaCode = captchaCode.replace(" ", "");
		final AuthenticationToken token = ctx.getToken();
		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}

		if (getDao(AccUserRegDao.class).existAccountOfMobile(mobile) != null) {
			throw new ServiceException("56*该手机号已注册!");
		}

		// 10分钟最多发10条
		String ipAddr = ctx.getRemoteAddr().toString();
		additionCheck(mobile, ipAddr);
		return sendCodeToMobile(mobile);
	}

	/**
	 * 验证手机激活码的有效性
	 * 
	 * @param mobile
	 *            手机号
	 * @param captchaCode
	 *            验证码
	 * @param mobileCode
	 *            手机激活码
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean validateMobileCode(@ParamName("mobile") final String mobile,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			@ParamName("mobile-code") String mobileCode, final ServiceContext ctx) {
		captchaCode = captchaCode.replace(" ", "");
		final AuthenticationToken token = ctx.getToken();
		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}

		/*mobileCode = mobileCode.trim();
		// Check code
		final Date now = new Date();
		if (!getDao(AccMsgDao.class).checkRegAccountCode(mobile, mobileCode,
				DateUtils.addMinutes(now, -30))) {
			throw new ServiceException("39*激活码无效!");
		}

		getDao(AccMsgDao.class).updateAcviceCodeStatus(mobile, mobileCode);*/
		return true;
	}

	/**
	 * 注册个人用户
	 * 
	 * @param realName
	 *            姓名
	 * @param idCard
	 *            身份证
	 * @param loginName
	 *            登录名
	 * @param pwd
	 *            密码
	 * @param mobile
	 *            手机号
	 * @param orgCode
	 *            所属企业编号
	 * @param recommendMobile
	 *            推荐人手机号
	 * @param ctx
	 * 
	 * @return 成功返回true,失败返回false
	 */
	public boolean regPerson(@ParamName("real-name") final String realName,
			@ParamName("id-card") final String idCard,
			@ParamName("login-name") final String loginName, @ParamName("pwd") final String pwd,
			@ParamName("mobile") final String mobile, @ParamName("org-code") final String orgCode,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			@ParamName("recommend-mobile") String recommendMobile,@ParamName("pwd-security") final String pwdSecurity, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		captchaCode = captchaCode.replace(" ", "");
		
		final AccUserRegDao accUserRegDao = super.getDao(AccUserRegDao.class);

		// 判断用户名的格式
		if (!LOGIN_NAME_PATTERN.matcher(loginName).matches()) {
			throw new ServiceException("50*用户名只能由数字、字符、下划线组成");
		}
		// 判断密码的格式
		/*if (!PASSWORD_PATTERN.matcher(pwd).matches()) {
			throw new ServiceException("57*密码只能由字母、数字 或下划线组成且不能含有空格！");
		}*/

		// 判断登录名是否存在
		if (accUserRegDao.existLoginName(loginName)) {
			throw new ServiceException("58*用户名已存在！");
		}

		// 判断手机号是否存在
		if (accUserRegDao.existAccountOfMobile(mobile) != null) {
			throw new ServiceException("51*手机号已存在!");
		}

		if(recommendMobile.startsWith("_")&& recommendMobile.endsWith("_")){
			recommendMobile = recommendMobile.replace("+", "=");
			recommendMobile = recommendMobile.substring(1, recommendMobile.length()-1);
			recommendMobile= new String(Base64.decodeBase64(recommendMobile));
			recommendMobile = recommendMobile.substring(3, recommendMobile.length()-3);
			
		}
		
		if (mobile.equals(recommendMobile)) {
			throw new ServiceException("52*推荐人手机号不能和自己的手机号相同!");
		}

		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}
		
		final AccPersonInfoDao accPersonInfoDao = super.getDao(AccPersonInfoDao.class);

		// 生成userCode
		String userCode = generateUserCode(1, AccountType.PERSON, RANDOM_COUNT);
		while (accUserRegDao.existAccountOfUserCode(userCode)) {
			userCode = generateUserCode(1, AccountType.PERSON, RANDOM_COUNT);
		}

		long auId = 0;
		getSessionFactory().beginTransaction(false);
		try {
			final long c1 = accUserRegDao.createAccUserReg( loginName,
					AuthorityService.hash(pwd), AccountType.PERSON.longValue(), mobile,pwdSecurity,
					recommendMobile, AccountStatus.AUDITED.longValue(), new Date(), userCode,
					StringUtils.isEmpty(recommendMobile) ? 0L : 1L);
			long c2 =0;
			if(c1 > 0){
				auId = accUserRegDao.getLastRowLongId();
				c2 = accPersonInfoDao.createAccPersonInfo(auId, realName, idCard, orgCode);
			}
			if (c1 > 0 && c2 > 0) {
				getSessionFactory().commitTransaction();
				AuthenticationToken token1 = new AuthenticationToken(String.valueOf(auId));
				token1.putInt("userType", AccountType.PERSON.value());
				ctx.setToken(token1);
				return true;
			}
		} finally {
			getSessionFactory().endTransaction();
		}

		return false;
	}

	public static void main(String[] args){
		String recommendMobile ="ccc15800838512ddd";
		recommendMobile ="_" +Base64.encodeBase64URLSafeString(recommendMobile.getBytes())+"_";
		recommendMobile = recommendMobile.substring(1, recommendMobile.length()-1);
		byte[] bytes= Base64.decodeBase64(recommendMobile);
		recommendMobile =new String(bytes);
		System.out.println(recommendMobile);
		recommendMobile =recommendMobile.substring(3, recommendMobile.length()-3);
		System.out.println(recommendMobile);
	}
	/**
	 * 注册机构账户
	 * 
	 * @param realName
	 *            姓名
	 * @param idCard
	 *            身份证号
	 * @param loginName
	 *            登录名
	 * @param pwd
	 *            密码
	 * @param mobile
	 *            手机号
	 * @param ctx
	 * 
	 * @return 成功返回true,失败返回false
	 */
	public boolean regCorp(@ParamName("real-name") final String realName,
			@ParamName("id-card") final String idCard,
			@ParamName("login-name") final String loginName, @ParamName("pwd") final String pwd,
			@ParamName("mobile") final String mobile,
			@ParamName("recommend-mobile") final String recommendMobile,@ParamName("pwd-security") final String pwdSecurity,  final ServiceContext ctx) {
		final AccUserRegDao accUserRegDao = super.getDao(AccUserRegDao.class);

		long auId = 0;

		// 判断用户名的格式
		if (!LOGIN_NAME_PATTERN.matcher(loginName).matches()) {
			throw new ServiceException("50*用户名只能由数字、字符、下划线组成");
		}
		// 判断密码的格式
		if (!PASSWORD_PATTERN.matcher(pwd).matches()) {
			throw new ServiceException("57*密码只能由字母、数字 或下划线组成且不能含有空格！");
		}

		// 判断登录名是否存在
		if (accUserRegDao.existLoginName(loginName)) {
			throw new ServiceException("58*用户名已存在！");
		}

		// 判断手机号是否存在
		if (accUserRegDao.existAccountOfMobile(mobile) != null) {
			throw new ServiceException("51*手机号已存在!");
		}

		if (mobile.equals(recommendMobile)) {
			throw new ServiceException("52*推荐人手机号不能和自己的手机号相同!");
		}

		final AccCorpInfoDao accCorpInfoDao = super.getDao(AccCorpInfoDao.class);

		// 生成userCode
		String userCode = generateUserCode(1, AccountType.ORG, RANDOM_COUNT);
		while (accUserRegDao.existAccountOfUserCode(userCode)) {
			userCode = generateUserCode(1, AccountType.ORG, RANDOM_COUNT);
		}

		getSessionFactory().beginTransaction(false);
		try {
			final long c1 = accUserRegDao.createAccUserReg( loginName,
					AuthorityService.hash(pwd), AccountType.ORG.longValue(), mobile,pwdSecurity,
					recommendMobile, AccountStatus.AUDITING.longValue(), new Date(), userCode,
					StringUtils.isEmpty(recommendMobile) ? 0L : 1L);
			long c2 = 0;
			if(c1>0){
				auId = accUserRegDao.getLastRowLongId();
				c2 = accCorpInfoDao.createAccCorpInfo(auId, realName, idCard);
			}
			if (c1 > 0 && c2 > 0) {
				getSessionFactory().commitTransaction();
				AuthenticationToken token = new AuthenticationToken(String.valueOf(auId));
				token.putInt("userType", AccountType.ORG.value());
				ctx.setToken(token);
				return true;
			}
		} finally {
			getSessionFactory().endTransaction();
		}

		return false;
	}

	/**
	 * 更新用户信息
	 * 
	 * @param position
	 *            职务
	 * @param company
	 *            工作单位
	 * @param companyType
	 *            单位类型
	 * @param address
	 *            通讯地址
	 * @param postalCode
	 *            邮政编码
	 * @param homephone
	 *            家庭电话
	 * @param qqNumber
	 *            qq号码
	 * @param legalPersonName
	 *            法人姓名
	 * @param legalPersonIdCard
	 *            法人身份证号
	 * @param corpName
	 *            机构名称
	 * @param bussLic
	 *            营业执照号
	 * @param taxLic
	 *            税务登记证号
	 * @param corpOrgCode
	 *            组织机构代码证号
	 * @param accUserName
	 *            银行账户户名
	 * @param account
	 *            银行账号
	 * @param accountBank
	 *            银行账户开户行
	 * @return 成功返回true,失败返回false
	 */
	public boolean updateUserInfo(@ParamName("position") String position,
			@ParamName("company") String company, @ParamName("company-type") String companyType,
			@ParamName("address") String address, @ParamName("postal-code") String postalCode,
			@ParamName("home-phone") String homePhone, @ParamName("qq-number") String qqNumber,
			@ParamName("legal-person-name") String legalPersonName,
			@ParamName("legal-person-id-card") String legalPersonIdCard,
			@ParamName("corp-name") String corpName, @ParamName("buss-lic") String bussLic,
			@ParamName("tax-lic") String taxLic, @ParamName("corp-org-code") String corpOrgCode,
			@ParamName("acc-user-name") String accUserName, @ParamName("account") String account,
			@ParamName("account-bank") String accountBank, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long auId = NumberUtils.toLong(token.getUserId(), 0L);
		final long userType = NumberUtils.toLong(token.get("userType"), 0L);
		if (userType == AccountType.PERSON.longValue()) {
			return updatePersonInfo(position, company, companyType, address, postalCode, homePhone,
					qqNumber, auId);
		}

		if (userType == AccountType.ORG.longValue()) {
			return updateCorpAccountInfo(legalPersonName, legalPersonIdCard, corpName, bussLic,
					taxLic, corpOrgCode, accUserName, account, accountBank, auId);
		}

		return false;
	}

	/**
	 * 更新个人用户信息
	 * 
	 * @param position
	 *            职务
	 * @param company
	 *            工作单位
	 * @param companyType
	 *            单位类型
	 * @param address
	 *            通讯地址
	 * @param postalCode
	 *            邮政编码
	 * @param homePhone
	 *            家庭电话
	 * @param qqNumber
	 *            qq号码
	 * @param auId
	 *            用户Id
	 * @return
	 * 
	 */
	public boolean updatePersonInfo(@ParamName("position") String position,
			@ParamName("company") String company, @ParamName("company-type") String companyType,
			@ParamName("address") String address, @ParamName("postal-code") String postalCode,
			@ParamName("home-phone") String homePhone, @ParamName("qq-number") String qqNumber,
			Long auId) {
		return super.getDao(AccPersonInfoDao.class).updateBasicInfoByAuId(auId, address, homePhone,
				postalCode, position, company, companyType, qqNumber) > 0;
	}

	/**
	 * 更新机构账户信息
	 * 
	 * @param legalPersonName
	 *            法人姓名
	 * @param legalPersonIdCard
	 *            法人身份证号
	 * @param corpName
	 *            机构名称
	 * @param bussLic
	 *            营业执照号
	 * @param taxLic
	 *            税务登记证号
	 * @param corpOrgCode
	 *            组织机构代码证号
	 * @param accUserName
	 *            银行账户户名
	 * @param account
	 *            银行账号
	 * @param accountBank
	 *            银行账户开户行
	 * @param auId
	 *            用户Id
	 * @return 更新成功返回true,更新失败返回false
	 */
	public boolean updateCorpAccountInfo(@ParamName("legal-person-name") String legalPersonName,
			@ParamName("legal-person-id-card") String legalPersonIdCard,
			@ParamName("corp-name") String corpName, @ParamName("buss-lic") String bussLic,
			@ParamName("tax-lic") String taxLic, @ParamName("corp-org-code") String corpOrgCode,
			@ParamName("acc-user-name") String accUserName, @ParamName("account") String account,
			@ParamName("account-bank") String accountBank, Long auId) {
		legalPersonName = legalPersonName.trim();
		/*
		 * if(legalPersonName.isEmpty()){ throw new
		 * ServiceException("法人姓名不能为空!"); }
		 */

		legalPersonIdCard = legalPersonIdCard.trim();
		/*
		 * if(legalPersonIdCard.isEmpty()){ throw new
		 * ServiceException("法人身份证号不能为空!"); }
		 */

		corpName = corpName.trim();
		if (corpName.isEmpty()) {
			throw new ServiceException("10*机构名称不能为空!");
		}

		bussLic = bussLic.trim();
		/*
		 * if(bussLic.isEmpty()){ throw new ServiceException("营业执照号不能为空!"); }
		 */

		taxLic = taxLic.trim();
		/*
		 * if(taxLic.isEmpty()){ throw new ServiceException("税务登记证号不能为空!"); }
		 */

		corpOrgCode = corpOrgCode.trim();
		if (corpOrgCode.isEmpty()) {
			throw new ServiceException("11*组织机构代码证号不能为空!");
		}

		accUserName = accUserName.trim();
		/*
		 * if(accUserName.isEmpty()){ throw new
		 * ServiceException("银行账户户名证号不能为空!"); }
		 */

		account = account.trim();
		if (account.isEmpty()) {
			throw new ServiceException("12*银行账号不能为空!");
		}

		accountBank = accountBank.trim();
		/*
		 * if(accountBank.isEmpty()){ throw new
		 * ServiceException("银行账户开户行不能为空!"); }
		 */

		// 判断身份证的格式
		/*
		 * if(!ID_CARD_PATTERN.matcher(legalPersonIdCard).matches()){ throw new
		 * ServiceException("法人身份证的格式不正确!"); }
		 */

		/*
		 * if(legalPersonIdCard.length()==18 &&
		 * (!isValidateCodeBy18IdCard(legalPersonIdCard)) ||
		 * !isValidateBirthBy18IdCard(legalPersonIdCard)){ throw new
		 * ServiceException("法人身份证号码的格式不正确！"); }
		 */

		// 法人身份实名认证

		final AccCorpInfoDao accCorpInfoDao = super.getDao(AccCorpInfoDao.class);
		final boolean isSuccess = accCorpInfoDao.updateCorpAccountInfoByAuId(auId, legalPersonName,
				legalPersonIdCard, bussLic, taxLic, corpOrgCode, accUserName, account, accountBank,
				corpName) > 0 ? true : false;
		if (isSuccess) {
			super.getDao(AccUserRegDao.class).updateStatusByAuId(auId,
					AccountStatus.AUDITED.longValue(), AccountStatus.AUDITING.longValue());
		}
		return isSuccess;

	}

	/**
	 * 查询用户信息
	 * 
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public Map<String, Object> queryUserInfo(final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long auId = NumberUtils.toLong(token.getUserId(), 0L);
		long userType = NumberUtils.toLong(token.get("userType"), 0L);
		if (userType == 0) {
			final AccUserReg aur = super.getDao(AccUserRegDao.class).queryAccUserRegByAuId(auId);
			if (aur != null) {
				userType = aur.getUserType();
			}
		}
		
		if (userType == AccountType.PERSON.longValue()) {
			return Converter.toMap(super.getDao(AccPersonInfoDao.class).queryAccPersonInfoByAuId(
					auId));
		}

		if (userType == AccountType.ORG.longValue()) {
			return Converter.toMap(super.getDao(AccCorpInfoDao.class).queryAccCorpInfoByAuId(auId));
		}

		return new HashMap<String, Object>();
	}

	/**
	 * 更新用户密码
	 * 
	 * @param oldPwd
	 *            旧密码
	 * @param newPwd
	 *            新密码
	 * @param ctx
	 * 
	 * @return 更新成功返回true,更新失败返回false
	 */
	public boolean updatePassword(@ParamName("old-pwd") final String oldPwd,
			@ParamName("new-pwd") final String newPwd, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long auId = NumberUtils.toLong(token.getUserId(), 0L);

		final AccUserRegDao accUserRegDao = super.getDao(AccUserRegDao.class);

		if (oldPwd == null || !accUserRegDao.validate2(auId, AuthorityService.hash(oldPwd))) {
			throw new ServiceException("50*原密码错误!");
		}

		getDao(AccUserRegPwdHistoryDao.class).createAccUserRegPwdHistory(auId,
				UpdatePwdType.AUTO.longValue(), "");
		return super.getDao(AccUserRegDao.class).updatePwd(auId, AuthorityService.hash(newPwd)) > 0 ? true
				: false;
	}

	/**
	 * 更新手机号码中，发送原手机号验证码
	 * 
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean sendCodeOfOldMobile(final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long userId = NumberUtils.toLong(token.getUserId(), 0);
		final Date now = new Date();
		String mobile = getDao(AccUserRegDao.class).queryMobileByAuId(userId);
		if (mobile == null || mobile.isEmpty()) {
			throw new ServiceException("53*手机号不存在！");
		}

		String code = MsgService.newCode(mobile, new Date());
		AccMsgDao amDao = getDao(AccMsgDao.class);
		amDao.createChangeMobileCode(mobile, code, now);
		final long mId = amDao.getLastRowLongId();
		final String content = String.format(InitSmsInfo.MODIFY_MOBILE_INFO, code);
		if (!getService(MsgService.class).sendMsg(mobile, content, MsgCode.CHANGE_MOBILE, mId, now)) {
			throw new ServiceException("54*发送短信失败!");
		}
		return true;
	}

	/**
	 * 更新手机号码中，发送新手机号验证码
	 * 
	 * @param newMobile
	 *            新手机号
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean sendCodeWithNewMobile(@ParamName("new-mobile") final String newMobile,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final AccUserRegDao dao = super.getDao(AccUserRegDao.class);
		if (dao.existAccountOfMobile(newMobile) != null) {
			throw new ServiceException("56*该手机号已注册!");
		}

		final Date now = new Date();
		String code = MsgService.newCode(newMobile, new Date());
		AccMsgDao amDao = getDao(AccMsgDao.class);
		amDao.createChangeMobileCode( newMobile, code, now);
		final long mId =amDao.getLastRowLongId();

		final String content = String.format(InitSmsInfo.MODIFY_MOBILE_INFO, code);
		if (!getService(MsgService.class).sendMsg(newMobile, content, MsgCode.CHANGE_MOBILE, mId,
				now)) {
			throw new ServiceException("46*发送短信失败!");
		}
		return true;
	}

	/**
	 * 更新用户手机号
	 * 
	 * @param oldMobileCode
	 *            发送到旧手机的验证码
	 * @param newMobileCode
	 *            发送到新手机的验证码
	 * @param newMobile
	 *            新手机号
	 * @param pwd
	 *            密码
	 * @param ctx
	 * 
	 * @return 更新成功返回true,更新失败返回false
	 */
	public boolean updateMobile(@ParamName("old-mobile-code") String oldMobileCode,
			@ParamName("new-mobile-code") String newMobileCode,
			@ParamName("new-mobile") final String newMobile, @ParamName("pwd") final String pwd,
			final ServiceContext ctx) {

		oldMobileCode = oldMobileCode.trim();
		newMobileCode = newMobileCode.trim();
		// 验证原手机验证码是否正确
		// 验证新手机验证码是否正确
		// 判断新手机号是否已注册
		// 验证密码是否正确
		// 更新手机号
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long userId = NumberUtils.toLong(ctx.getToken().getUserId(), 0);
		final AccUserRegDao accUserRegDao = getDao(AccUserRegDao.class);
		final String mobile = accUserRegDao.queryMobileByAuId(userId);
		if (mobile == null || mobile.isEmpty()) {
			throw new ServiceException("53*手机号不存在！");
		}

		final Date now = new Date();
		/*if (!getDao(AccMsgDao.class).checkChangeMobileCode(mobile, oldMobileCode,
				DateUtils.addMinutes(now, -30))) {
			throw new ServiceException("62*原手机短信验证码无效!");
		}*/

		/*if (!getDao(AccMsgDao.class).checkChangeMobileCode(newMobile, newMobileCode,
				DateUtils.addMinutes(now, -30))) {
			throw new ServiceException("63*新手机短信注册码无效!");
		}*/

		if (accUserRegDao.existAccountOfMobile(newMobile) != null) {
			throw new ServiceException("56*该手机号已注册!");
		}

		if (pwd == null || !accUserRegDao.validate2(userId, AuthorityService.hash(pwd))) {
			throw new ServiceException("46*密码错误!");
		}

		if (accUserRegDao.updateMobileOfAuId(userId, newMobile) <= 0) {
			throw new ServiceException("64*未成功更改手机号！");
		}
		return true;
	}

	/**
	 * 更新用户的email
	 * 
	 * @param newEmail
	 *            邮箱账号
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean updateEmail(@ParamName("new-email") final String newEmail,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long auId = NumberUtils.toLong(token.getUserId(), 0L);
		final long userType = NumberUtils.toLong(token.get("userType"), 0L);
		if (userType == AccountType.PERSON.longValue()) {
			if (getDao(AccPersonInfoDao.class).updateEmailByAuId(auId, newEmail) <= 0) {
				throw new ServiceException("60*该邮箱已存在!");
			}
		}

		if (userType == AccountType.ORG.longValue()) {
			if (getDao(AccCorpInfoDao.class).updateEmailByAuId(auId, newEmail) <= 0) {
				throw new ServiceException("60*该邮箱已存在!");
			}
		}

		return true;
	}

	/**
	 * 生成userCode
	 * 
	 * @param resouce
	 *            来源
	 * @param type
	 *            类型(1(个人),2(机构)
	 * @return
	 * 
	 */
	private static String generateUserCode(long resouce, AccountType userType, int count) {
		if (count < 1) {
			throw new ServiceException("21*count不能小于1!");
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String date = df.format(new Date());
		return resouce + "" + userType.longValue() + date + generateRandom(count);
	}

	/**
	 * 生成count长度的字符串
	 * 
	 * @param count
	 * 
	 * @return count长度的字符串
	 */
	private static String generateRandom(long count) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append((int) (Math.random() * 10));
		}
		return sb.toString();
	}

	/**
	 * 发送手机激活码（忘记密码申请中）
	 * 
	 * @param loginNameOrMobie
	 *            登录名或手机号
	 * @param captchaCode
	 *            验证码
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public String sendLostPwdActiveCode(
			@ParamName("login-name-or-mobile") final String loginNameOrMobile,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			final ServiceContext ctx) {
		// 判断验证码是否有效
		captchaCode = captchaCode.replace(" ", "");
		final AuthenticationToken token = ctx.getToken();
		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}
		// 判断是否存在有效的注册账户用户名或手机号
		// 发送手机激活码
		AccUserRegDao regDao = getDao(AccUserRegDao.class);
		Long auId = regDao.queryAuIdByLoginNameOrMobile(loginNameOrMobile);
		if (auId == null || auId == 0) {
			throw new ServiceException("52*用户名或手机号不存在！");
		}
		String mobile = regDao.queryMobileByAuId(auId);
		if (mobile == null || mobile.isEmpty()) {
			throw new ServiceException("53*手机号不存在！");
		}
		AccUserReg accUserReg = regDao.queryAccUserRegByAuId(auId);
		if (accUserReg.getStatus().longValue() == AccountStatus.DISABLED.longValue()) {
			throw new ServiceException("54*该用户已停用，不能找回密码！");
		}

		// 单IP触发短信间隔，不小于60秒或者120秒。
		// 单IP触发次数限制，24小时不超过20条。
		// 单号码24小时不超过5条。
		String ipAddr = ctx.getRemoteAddr().toString();
		additionCheck2(mobile, ipAddr);

		final Date now = new Date();
		String code = MsgService.newCode(mobile, new Date());
		AccMsgDao amDao  = getDao(AccMsgDao.class);
		getDao(AccMsgDao.class).createLostPwdCode(mobile, code, now);
		long mId = amDao.getLastRowLongId();
		final String content = String.format(InitSmsInfo.RESET_CODE_INFO, code);
		if (!getService(MsgService.class).sendMsg(mobile, content, MsgCode.MGR_RESET_PWD, mId, now)) {
			throw new ServiceException("54*发送短信失败!");
		}
		return mobile;
	}

	/**
	 * 验证手机激活码
	 * 
	 * @param loginNameOrMobie
	 *            登录名或手机号
	 * @param activeCode
	 *            激活码
	 * @param ctx
	 * 
	 * @return
	 * 
	 */
	public boolean resetPwd(@ParamName("login-name-or-mobile") final String loginNameOrMobile,
			@ParamName("captcha-code") @ParamDefaultValue("") String captchaCode,
			@ParamName("pwd-question-answer") String pwdQuestionAnswer,
			@ParamName("pwd") String pwd,final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		// 判断激活码是否有效
		// 创建一个新密码
		// 将新密码发送给用户
		pwdQuestionAnswer = pwdQuestionAnswer.trim();

		AccUserRegDao regDao = getDao(AccUserRegDao.class);
		Long auId = regDao.queryAuIdByLoginNameOrMobile(loginNameOrMobile);
		if (auId == null || auId == 0) {
			throw new ServiceException("52*用户名或手机号不存在！");
		}
		String pwdSecurity = regDao.queryAccUserRegByAuId(auId).getPwdSecurity();
		if (pwdSecurity == null || pwdSecurity.isEmpty()||(!pwdSecurity.equals(pwdQuestionAnswer))) {
			throw new ServiceException("53*密保答案错误！");
		}
		final String tokenCaptchaCode = token != null ? token.get(CaptchaImageProvider.CAPTCHA_KEY,
				"") : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			throw new ServiceException("57*验证码错误!");
		}
		regDao.updatePwd(auId, AuthorityService.hash(pwd));
		return true;
	}

	public boolean additionCheck2(String mobile, String ipAddr) {

		if (mobile == null || "".equals(mobile)) {
			throw new ServiceException("32*无法获取手机号码，发送短信失败！");
		}

		if (ipAddr == null || "".equals(ipAddr)) {
			throw new ServiceException("33*无法获取IP地址，发送短信失败！");
		}

		AbstractQueue<Date> ipIntervalQueue = ipIntervalMap2.get(ipAddr);
		if (ipIntervalQueue == null) {
			ipIntervalQueue = new ArrayBlockingQueue<Date>(1);
			ipIntervalQueue.add(new Date());
			ipIntervalMap2.put(ipAddr, ipIntervalQueue);
		} else {
			// 删除超过60秒的对象
			Date now = new Date();
			Iterator<Date> it = ipIntervalQueue.iterator();
			while (it.hasNext()) {
				Date one = it.next();
				if (now.getTime() - one.getTime() > 60000) {
					it.remove();
				}
			}

			// 判断队列长度
			if (ipIntervalQueue.size() < 1) {
				ipIntervalQueue.add(now);
			} else {
				throw new ServiceException("相同IP短信间隔必须超过60秒！");
			}
		}

		AbstractQueue<Date> phoneCountQueue = phoneCountMap2.get(mobile);
		if (phoneCountQueue == null) {
			phoneCountQueue = new ArrayBlockingQueue<Date>(5);
			phoneCountQueue.add(new Date());
			phoneCountMap2.put(mobile, phoneCountQueue);
		} else {
			// 删除超过24小时的对象
			Date now = new Date();
			Iterator<Date> it = phoneCountQueue.iterator();
			while (it.hasNext()) {
				Date one = it.next();
				if (now.getTime() - one.getTime() > 86400000) {
					it.remove();
				}
			}

			// 判断队列长度
			if (phoneCountQueue.size() < 5) {
				phoneCountQueue.add(now);
			} else {
				throw new ServiceException("34*相同号码24小时内不能超过5条！");
			}
		}

		AbstractQueue<Date> ipCountQueue = ipCountMap2.get(ipAddr);
		if (ipCountQueue == null) {
			ipCountQueue = new ArrayBlockingQueue<Date>(20);
			ipCountQueue.add(new Date());
			ipCountMap2.put(ipAddr, ipCountQueue);
		} else {
			// 删除超过60秒的对象
			Date now = new Date();
			Iterator<Date> it = ipCountQueue.iterator();
			while (it.hasNext()) {
				Date one = it.next();
				if (now.getTime() - one.getTime() > 86400000) {
					it.remove();
				}
			}

			// 判断队列长度
			if (ipCountQueue.size() < 20) {
				ipCountQueue.add(now);
			} else {
				throw new ServiceException("35*相同地址24小时内不能超过20条！");
			}
		}
		return true;
	}

	/**
	 * 判断REC_INFO_MODI_COUNT是否大于0，如果大于0则不修改，并抛出异常“推荐码不可修改”；
	 * 判断recommInfo是否为空，不为空则写入数据库，并将REC_INFO_MODI_COUNT字段值加1
	 * 
	 * @param recommInfo
	 * @param ctx
	 * @return
	 */
	public boolean updateRecommInfo(@ParamName("recommend-mobile") String recommInfo,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}

		final long auId = NumberUtils.toLong(ctx.getToken().getUserId());
		if (StringUtils.isNotBlank(recommInfo)) {
			final long count = getDao(AccUserRegDao.class).updateRecommInfo(auId, recommInfo);
			if (count == 0) {
				throw new ServiceException("推荐码不可修改！");
			}
			return true;
		}
		return false;
	}
	
	public boolean setBgPrivilege(final ServiceContext ctx){
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}
		final long auId = NumberUtils.toLong(ctx.getToken().getUserId());
		AccUserRegDao aurDao = getDao(AccUserRegDao.class);
		AccUserReg aur = aurDao.queryAccUserRegByAuId(auId);
		if(aur.getLoginName().equals("admin")){
			final long count = getDao(AccUserRegDao.class).updateBgPrivilege(auId, 1L);
			if (count == 0) {
				throw new ServiceException("后台权限设置失败！");
			}
			return true;
		}
		
		return false;
	}
	
	public boolean cancelBgPrivilege(final ServiceContext ctx){
		final AuthenticationToken token = ctx.getToken();
		if (token == null || token.isGuest()) {
			throw new ServiceException("1*您还没有登录，请先登录!");
		}
		final long auId = NumberUtils.toLong(ctx.getToken().getUserId());
		AccUserRegDao aurDao = getDao(AccUserRegDao.class);
		AccUserReg aur = aurDao.queryAccUserRegByAuId(auId);
		if(aur.getLoginName().equals("admin")){
			final long count = getDao(AccUserRegDao.class).updateBgPrivilege(auId, 0L);
			if (count == 0) {
				throw new ServiceException("后台权限取消失败！");
			}
			return true;
		}
		return false;
	}
	
	public Map<String, String> getApkInfo() {
		Map<String, String> map = new HashMap<String, String>();

		// String apkPath =
		// ConfigurationProviders.getInstance().getSection("apk").get("path");
		// String apkVersion =
		// ConfigurationProviders.getInstance().getSection("apk").get("version");

		final AppInfoDao dao = super.getDao(AppInfoDao.class);
		List<AppInfo> infos = dao.getAppInfo();

		for (AppInfo info : infos) {
			if ("APK_PATH".equalsIgnoreCase(info.getName())) {
				map.put("apkPath", info.getContent());
				continue;
			}

			if ("APK_VERSON".equalsIgnoreCase(info.getName())) {
				map.put("apkVersion", info.getContent());
				continue;
			}
		}

		return map;
	}
	
	/**
	 * 生成并获取二维码图片
	 * 
	 * @param userId
	 *            用户Id
	 * 
	 * @param oriRCode
	 *            原始推荐码
	 * 
	 * @param ctx
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public TempFileObject getRqCodePicture(
			@ParamName("disp-name") @ParamDefaultValue("true") boolean dispName,
			@ParamName("userId") @ParamDefaultValue("0") long userId,
			@ParamName("ori-rcode") final String oriRCode,
			final ServiceContext ctx) throws Exception {
		if (ctx.getToken().isGuest()) {
			return null;
		}
		if (userId == 0) {
			userId = NumberUtils.toLong(ctx.getToken().getUserId(), 0);
		}

		// 生成二维码
		final int width = 300; // 图像宽度
		final int height = 300; // 图像高度
		final int textHeight = dispName ? 40 : 0;// 文字高度
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.MARGIN, 1);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix bitMatrix = new MultiFormatWriter().encode(
				getUrlWithRcode(userId, oriRCode, ctx), BarcodeFormat.QR_CODE,
				width, height, hints);// 生成矩阵

		// 创建一张原始图片
		final BufferedImage img = new BufferedImage(width, height + textHeight,
				BufferedImage.TYPE_INT_RGB);// 创建图片

		final BufferedImage bg = toBufferedImage(bitMatrix);// 二维码图片

		// 读logo
		final BufferedImage iconBi = ImageIO.read(AccountService.class
				.getResourceAsStream(ICON_PATH));

		final Graphics2D g = (Graphics2D) img.getGraphics();// 开启画图
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height + textHeight);

		final int iconWidth = iconBi.getWidth();// icon的宽度
		final int iconHeight = iconBi.getHeight();// icon的高度

		g.drawImage(bg.getScaledInstance(width, height, Image.SCALE_DEFAULT),
				0, 0, null);
		g.drawImage(iconBi.getScaledInstance(iconWidth, iconHeight,
				Image.SCALE_DEFAULT), (width - iconWidth) / 2,
				(height - iconHeight) / 2, Color.WHITE, null);

		final Font font = new Font("Microsoft Yahei", Font.PLAIN, 18);

		g.setColor(Color.BLACK);
		g.setFont(font);
		// 在图上写字
		if (dispName) {
			// 查询登录名 姓名
			final AccountSignInfo asi = getDao(AccUserRegDao.class)
					.queryAcountSignInfoByAuId(userId);
			if (asi != null) {
				final String realName = asi.getRealName(); // 姓名
				final String loginName = asi.getLoginName(); // 登录名
				final FontRenderContext context = g.getFontRenderContext();
				Rectangle2D stringBounds = font.getStringBounds(realName,
						context);
				final double realNameWidth = stringBounds.getWidth();// 姓名的宽度
				final double realNameHight = stringBounds.getHeight();// 姓名的高度

				stringBounds = font.getStringBounds(loginName, context);
				final double loginNameWidth = stringBounds.getWidth();// 登录名的宽度
				final double loginNameHeight = stringBounds.getHeight();// 登录名的高度
				g.drawString(realName, (int) ((width - realNameWidth) / 2),
						(int) (height + realNameHight / 2));
				g.drawString(loginName, (int) ((width - loginNameWidth) / 2),
						(int) (height + realNameHight + loginNameHeight / 2));
			}
		}
		g.dispose();

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "PNG", os);
		} finally {
			IOUtils.closeQuietly(os);
		}

		return new TempFileObject("qrcode.png", os.toByteArray());
	}
	
	/**
	 * 获取URL
	 * 
	 * @param userId
	 *            用户Id
	 * 
	 * @param oriRCode
	 *            原始推荐码
	 * 
	 * @param ctx
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public String getUrlWithRcode(
			@ParamName("userId") @ParamDefaultValue("0") long userId,
			@ParamName("ori-rcode") final String oriRCode,
			final ServiceContext ctx) throws Exception {
		String encodedCode;

		if (oriRCode.isEmpty()) {
			if (userId == 0) {
				userId = NumberUtils.toLong(ctx.getToken().getUserId(), 0);
			}

			final String mobile = getDao(AccUserRegDao.class)
					.queryMobileByAuId(userId);
			encodedCode = mobile==null ||mobile.equals("null")? "":Base64.encodeBase64URLSafeString(("ccc" + mobile + "ddd").getBytes());
		} else {
			encodedCode = Base64.encodeBase64String(("ccc" + oriRCode + "ddd").getBytes());
		}
		encodedCode = encodedCode.replace("=", "+");
		String rcodeEndStr = encodedCode.length()>6 ? "_"+encodedCode+"_" :"";
		return InitSmsInfo.RCODE_URL + rcodeEndStr;
	}

	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static BufferedImage toBufferedImage(final BitMatrix matrix) {
		final int BLACK = 0x000000;
		final int WHITE = 0xFFFFFF;

		int width = matrix.getWidth();
		int height = matrix.getHeight();
		final BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}
}
