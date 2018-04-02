package com.xrsrv.account;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.commons.CryptographicUtils;
import org.xx.armory.commons.MapBuilder;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;

import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AuthInfo;
import com.xrsrv.system.enumerate.AccountStatus;
import com.xrsrv.system.enumerate.AccountType;

public abstract class AuthorityService extends AggregatedService {
	private Log logger = LogFactory.getLog(AuthorityService.class);

	/**
	 * 最大重试次数。
	 */
	public static final int MAX_RETRY_COUNT = 2;

	private static enum SignResult {
		/**
		 * 身份验证通过。
		 */
		OK(true, false),
		/**
		 * 身份验证不通过。
		 */
		WRONG(false, false),
		/**
		 * 身份验证不通过，并且已重试多次。
		 */
		WRONG_WRONG(false, true);

		private final boolean valid;
		private final boolean extraInput;

		/**
		 * 
		 * @param valid
		 * @param extraInput
		 */
		SignResult(final boolean valid, final boolean extraInput) {
			this.valid = valid;
			this.extraInput = extraInput;
		}

		@SuppressWarnings("unused")
		public final boolean isValid() {
			return this.valid;
		}

		@SuppressWarnings("unused")
		public final boolean isExtraInput() {
			return this.extraInput;
		}
	}

	public static class UserAccount extends Account {
		public UserAccount(final String loginName, final String realName, final String mobile,
				final AccountStatus status, final AccountType type, final long recInfoModCount,long bgPrivilege) {
			super(loginName, realName, mobile, status, type, recInfoModCount,bgPrivilege);
		}
	}

	public AuthorityService() {
	}

	/**
	 * 执行登录操作。
	 * 
	 * @param login
	 *            -name-or-mobile 登录名或者手机号
	 * @param password
	 *            密码
	 * @param captcha
	 *            -code 验证码
	 * @param ctx
	 *            服务上下文
	 * @return 身份认证结果。
	 */
	public Map<String, Object> signIn(
			@ParamName("login-name-or-mobile") final String loginNameOrMobile,
			@ParamName("pwd") final String password,
			@ParamName("captcha-code") final String captchaCode, final ServiceContext ctx) {

		final AuthenticationToken token = ctx.getToken();
		if (token == null) {
			throw new NullPointerException("token is null");
		}

		// 0.登录信息验证
		final long auId = validate(loginNameOrMobile, hash(password));
		final boolean valid = auId == 0 ? false : true;

		// 1.设置已重试次数
		final int retryCount = token.getInt("retryCount", 0);
		token.putInt("retryCount", valid ? 0 : retryCount > 100 ? 100 : retryCount + 1);
		
		// 若重试次数>最大重试次数:检查验证码
		// if(valid && retryCount > MAX_RETRY_COUNT){
		// final String tokenCaptchaCode =
		// token.get(CaptchaImageProvider.CAPTCHA_KEY, "");
		// valid = StringUtils.isNotBlank(captchaCode) &&
		// StringUtils.equalsIgnoreCase(captchaCode.trim(), tokenCaptchaCode);
		// }

		// 2.构建返回结果
		final MapBuilder<String, Object> rb = new MapBuilder<String, Object>();
		if (valid) {
			final AuthenticationToken newToken = new AuthenticationToken(String.valueOf(auId));
			
		
			rb.put("signResult", SignResult.OK);
			AccUserReg accUser = (AccUserReg) getAccUserRegInfo(auId);
			rb.put("status", accUser.getStatus());
			rb.put("userType", accUser.getUserType());
			
			newToken.putLong("userType",  accUser.getUserType());
			newToken.put("loginName", accUser.getLoginName());
			ctx.setToken(newToken);
		} else {
			rb.put("signResult", retryCount <= MAX_RETRY_COUNT ? SignResult.WRONG
					: SignResult.WRONG_WRONG);
		}

		return rb.buildHashMap();
	}

	public Account account(final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		logger.info(token.isGuest());
		if (!isSignIn(token)) {
			return Account.NONE;
		}

		final AuthInfo authInfo = authenticate(Long.parseLong(token.getUserId()));
		if (authInfo == null) {
			return Account.NONE;
		} else {
				if (StringUtils.isEmpty(authInfo.getLoginName())) {
					return Account.NONE;
				}
				return getAccount(authInfo.getLoginName(),  authInfo.getRealName(), authInfo.getMobile(),
						authInfo.getStatus()!=null? status(authInfo.getStatus().intValue()):null,
						authInfo.getUserType()!=null?	type(authInfo.getUserType().intValue()):null,
												authInfo.getRecInfoModiCount(),authInfo.getBgPrivilege());
		}
	}

	public abstract Account getAccount(String loginName, String realName, String mobile,
			AccountStatus status, AccountType type,  long recInfoModCount,long bgPrivilege);

	protected abstract AuthInfo authenticate(final long userId);

	public abstract AccUserReg getAccUserRegInfo(long auId);

	public abstract long validate(String loginNameOrMobile, String hash);

	/**
	 * 
	 * @param ctx
	 */
	public void signOut(final ServiceContext ctx) {
		ctx.setToken(null);
	}

	/**
	 * 判断指定的令牌是否表示已登录。
	 * 
	 * @param token
	 *            令牌
	 * @return
	 */
	public final boolean isSignIn(final AuthenticationToken token) {
		return token != null && !token.isGuest();
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String hash(final String s) {
		final String salt = "abc";

		return Base64.encodeBase64String(CryptographicUtils.hash(CryptographicUtils
				.disturb(s, salt)));
	}

	private static AccountStatus status(final int value) {
		for (final AccountStatus s : AccountStatus.values()) {
			if (s.value() == value) {
				return s;
			}
		}

		return null;
	}

	private static AccountType type(final int type) {
		for (final AccountType t : AccountType.values()) {
			if (t.value() == type) {
				return t;
			}
		}

		return null;
	}

	private static Log getLog() {
		return LogFactory.getLog(AuthorityService.class);
	}
}
