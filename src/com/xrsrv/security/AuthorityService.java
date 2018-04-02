/**
 * 
 */
package com.xrsrv.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.commons.CryptographicUtils;
import org.xx.armory.commons.MiscUtils;
import org.xx.armory.db.DbRow;
import org.xx.armory.db.ManyToOneMapper;
import org.xx.armory.db.MatchType;
import org.xx.armory.db.Parameter;
import org.xx.armory.db.ParameterType;
import org.xx.armory.db.Session;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.security.CaptchaImageProvider;
import org.xx.armory.security.Role;
import org.xx.armory.security.User;
import org.xx.armory.security.impl.OracleUserPermProvider;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;

import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xrsrv.msg.MsgCode;
import com.xrsrv.msg.MsgService;


public final class AuthorityService extends AggregatedService {
	private Log logger = LogFactory.getLog(AuthorityService.class);

	private static final String ADMIN_USER = "admin";

	private static final String ADMIN_ROLE = "administrators";

	private static final String ROLES_KEY = "roles";

	private static final String MOBILE_KEY = "mobile";

	private static final String EMAIL_KEY = "email";

	private static final String CREATE_DATE_KEY = "create_date";

	private static final Transformer<User, SysUser> SYS_USER_TRANSFORMER = new Transformer<User, SysUser>() {
		@Override
		public SysUser transform(final User u) {
			final SysUser ret = new SysUser(u.getName(), roles(u.getString(ROLES_KEY).trim()),
					u.isEnabled());

			ret.setMobile(u.getString(MOBILE_KEY));
			ret.setEmail(u.getString(EMAIL_KEY));
			ret.setCreateDate(u.getNumber(CREATE_DATE_KEY));

			return ret;
		}

	};

	private static class SysUserMapper extends ManyToOneMapper<SysUser> {
		@Override
		protected void executeMany(final DbRow row, final SysUser user) {
			final String key = row.getString(3);
			final String value = row.getString(4);
			if (MOBILE_KEY.equals(key)) {
				user.setMobile(value);
			} else if (EMAIL_KEY.equals(key)) {
				user.setEmail(value);
			} else if (CREATE_DATE_KEY.equals(key)) {
				final long cd = NumberUtils.toLong(value, 0L);
				user.setCreateDate(cd == 0 ? null : cd);
			}
		}

		@Override
		public SysUser executeOne(final DbRow row) {
			return new SysUser(row.getString(0), roles(row.getString(1)), row.getBoolean(2));
		}

		@Override
		protected boolean isSame(final SysUser u1, final SysUser u2) {
			return StringUtils.equalsIgnoreCase(u1.getUserName(), u2.getUserName());
		}

	};

	private static String[] roles(final String s) {
		if (s == null || s.isEmpty()) {
			return new String[0];
		}

		return StringUtils.split(s, ',');
	}

	/**
	 * 
	 */
	public AuthorityService() {
	}

	/**
	 * 检查当前登录用户的口令是否匹配
	 * 
	 * @param password
	 *            口令
	 * @param ctx
	 *            当前服务上下文
	 * @return
	 *         登录名和口令是否匹配, 如果不存在当前登录用户那么也不匹配
	 */
	public boolean validateUser(@ParamName("password") String password, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token.isGuest()) {
			return false;
		}
		final User u = getService(OracleUserPermProvider.class).loadUserByName(token.getUserId());
		return getService(OracleUserPermProvider.class).validateUser(u.getName(), password);
	}

	/**
	 * 登录
	 * 
	 * <p>
	 * 如果登录成功，那么设置有效令牌，并且{@code getUserId() == userName}。
	 * </p>
	 * 
	 * @param userName
	 *            登录名
	 * @param password
	 *            口令
	 * @param captchaCode
	 *            验证码
	 * @return
	 *         是否成功登录, 如果具有此登录名的帐户不存在也看作登录失败
	 */
	public boolean signin(
			@ParamName("user-name") String userName, @ParamName("password") String password,
			@ParamName("captcha-code") String captchaCode, final ServiceContext ctx) {
		if (ADMIN_USER.equalsIgnoreCase(userName)) {
			ensureAdministrator();
		}

		boolean valid = true;
		//
		long auId = super.getDao(AccUserRegDao.class).validate(userName, hash(password));
		if (auId == 0) {
			valid = false;
		}

		final AuthenticationToken token = ctx.getToken();
		final String tokenCaptchaCode = token != null ?
				token.get(CaptchaImageProvider.CAPTCHA_KEY) : "";
		if (!captchaCode.equalsIgnoreCase(tokenCaptchaCode)) {
			valid = false;
		}

		if (valid) {
			ctx.setToken(new AuthenticationToken(userName));

			if (logger.isDebugEnabled()) {
				logger.debug("[" + userName + "] logged from [" + ctx.getRemoteAddr() + "]");
			}
		}

		return valid;
	}

	
	public static String hash(final String s) {
		final String salt = "abc";

		return Base64.encodeBase64String(CryptographicUtils.hash(CryptographicUtils
				.disturb(s, salt)));
	}
	
	/**
	 * 查看当前登录的帐户。
	 * 
	 * @param ctx
	 *            服务上下文。
	 * @return
	 *         当前登录的帐户，如果不存在则返回{@code null}。
	 * 
	 * @throws ServiceException
	 *             如果当前未登录。
	 */
	public AccUserReg currentUser(final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token.isGuest()) {
			throw new ServiceException("Not log in");
		}

		return super.getDao(AccUserRegDao.class).queryAccUserRegByLoginName(token.get("loginName"));
	}

	/**
	 * 查看帐户信息
	 * 
	 * @param userName
	 *            帐户登录名
	 * @return
	 *         具有指定登录名的帐户信息，如果不存在则返回{@code null}。
	 */
	public SysUser user(@ParamName("user-name") @ParamDefaultValue("") final String userName) {
		final User u = getService(OracleUserPermProvider.class).loadUserByName(userName);
		return SYS_USER_TRANSFORMER.transform(u);
	}

	/**
	 * 
	 * @param userName
	 * @param mobile
	 * @param email
	 * @param enabled
	 * @param roles
	 * @return
	 */
	public int addUser(@ParamName("user-name") final String userName,
			@ParamName("mobile") final String mobile, @ParamName("email") final String email,
			@ParamName("enabled") @ParamDefaultValue("false") final Boolean enabled,
			@ParamName("roles") final String roles) {
		final User user = new User(userName, enabled, null);
		if (mobile.isEmpty()) {
			throw new ServiceException("Mobile cannot be empty");
		}
		user.put(MOBILE_KEY, mobile);
		user.put(EMAIL_KEY, email);
		user.put(CREATE_DATE_KEY, new Date().getTime());

		getService(OracleUserPermProvider.class).addUser(user);
		getService(OracleUserPermProvider.class)
				.assignRoles(userName, StringUtils.split(roles, ','));

		return 1;
	}

	/**
	 * 更新帐户
	 * 
	 * @param userName
	 * @param mobile
	 * @param email
	 * @param enabled
	 * @param roles
	 * @return
	 */
	public int updateUser(@ParamName("user-name") final String userName,
			@ParamName("mobile") final String mobile, @ParamName("email") final String email,
			@ParamName("enabled") @ParamDefaultValue("false") final Boolean enabled,
			@ParamName("roles") @ParamDefaultValue("") final String roles) {
		final User user = new User(userName, enabled, null);
		if (mobile.isEmpty()) {
			throw new ServiceException("Mobile cannot be empty");
		}
		user.put(MOBILE_KEY, mobile);
		user.put(EMAIL_KEY, email);
		user.put(CREATE_DATE_KEY, new Date().getTime());

		final List<String> p = new ArrayList<String>();
		for (final String s : roles.split(",")) {
			if (s.isEmpty()) {
				continue;
			}

			p.add(s.trim());
		}

		final int c = getService(OracleUserPermProvider.class).updateUser(user);
		getService(OracleUserPermProvider.class).assignRoles(userName, p.toArray(new String[0]));

		return c;
	}

	/**
	 * 删除帐户
	 * 
	 * @param userName
	 *            帐户的登录名
	 * @return
	 *         成功删除的帐户记录数
	 */
	public int deleteUser(@ParamName("user-name") final String userName) {
		return getService(OracleUserPermProvider.class).removeUser(userName);
	}

	/**
	 * 修改密码
	 * 
	 * @param oldPassword
	 *            原密码
	 * @param newPassword
	 *            新密码
	 * @param ctx
	 *            服务上下文
	 * @return
	 *         成功修改密码的帐户数
	 */
	public int changePassword(@ParamName("old-password") final String oldPassword,
			@ParamName("new-password") final String newPassword, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token.isGuest()) {
			throw new ServiceException("Not log in!");
		}

		final String userName = token.getUserId();
		if (!getService(OracleUserPermProvider.class).validateUser(userName, oldPassword)) {
			return 0;
		} else {
			return getService(OracleUserPermProvider.class).updatePassword(userName, newPassword);
		}
	}

	/**
	 * 重置密码
	 * 
	 * @param userName
	 *            登录名
	 * @return
	 *         成功重置密码的帐户数
	 */
	public int resetPassword(@ParamName("user-name") final String userName) {
		final Date now = new Date();
		final User user = getService(OracleUserPermProvider.class).loadUserByName(userName);
		if (user == null) {
			throw new ServiceException("Invalid user");
		}

		final String mobile = user.getString(MOBILE_KEY);
		if (mobile == null || mobile.isEmpty()) {
			throw new ServiceException("No mobile");
		}

		final String newPassword = MsgService.newCode(mobile, new Date());
		final int c = getService(OracleUserPermProvider.class)
				.updatePassword(userName, newPassword);
		getService(MsgService.class)
				.sendMsg(mobile, "您的运维帐号的新密码是 " + newPassword, MsgCode.MGR_RESET_PWD, 0L, now);
		return c;
	}

	/**
	 * 查看帐户列表
	 * 
	 * @param roleName
	 * @param enabled
	 * @param keyword
	 * @return
	 */
	public Collection<SysUser> findAllUsers(
			@ParamName("role-name") @ParamDefaultValue("") final String roleName,
			@ParamName("enabled") final Boolean enabled,
			@ParamName("keyword") @ParamDefaultValue("") final String keyword) {

		final StringBuilder sql = new StringBuilder(
				"SELECT U.U_NAME, VARCHAR2_NTT_TO_STRING(CAST(COLLECT(DISTINCT  R1.R_TITLE) AS VARCHAR2_NTT)) R_NAMES, MIN(U.U_ENABLED), UP.P_KEY, MIN(UP.P_VALUE)\n"
						+ "FROM MY_USER U\n"
						+ "LEFT JOIN MY_USER_ROLE UR1 ON UR1.U_NAME = U.U_NAME\n"
						+ "LEFT JOIN MY_ROLE R1 ON R1.R_NAME = UR1.R_NAME\n"
						+ "LEFT JOIN MY_USER_PROP UP ON UP.U_NAME = U.U_NAME\n"
						+ "LEFT JOIN MY_USER_ROLE UR ON UR.U_NAME = U.U_NAME\n"
						+ "LEFT JOIN MY_ROLE R ON R.R_NAME = UR.R_NAME\n");
		final StringBuilder sqlCrit = new StringBuilder();
		if (keyword != null && keyword.length() != 0) {
			sqlCrit.append("U.U_NAME LIKE #keyword#\n");
		}
		if (roleName != null && roleName.length() != 0) {
			if (sqlCrit.length() != 0) {
				sqlCrit.append(" AND ");
			}
			sqlCrit.append("R.R_NAME = #r_name#\n");
		}
		if (enabled != null) {
			if (sqlCrit.length() != 0) {
				sqlCrit.append(" AND ");
			}
			sqlCrit.append("U.U_ENABLED = #uenabled#\n");
		}
		if (sqlCrit.length() != 0) {
			sql.append(" WHERE ");
		}
		sql.append(sqlCrit.toString());
		sql.append("GROUP BY U.U_NAME, UP.P_KEY\n");
		sql.append("ORDER BY U.U_NAME, UP.P_KEY");

		final Parameter paramRoleName = getSessionFactory().newParameter("r_name",
				ParameterType.STRING, roleName).build();
		final Parameter paramEnabled = getSessionFactory().newParameter("uenabled",
				ParameterType.BOOLEAN, enabled).build();
		final Parameter paramKeyword = getSessionFactory().newParameter("keyword",
				ParameterType.STRING, keyword).setMatch(MatchType.CONTAINS).build();

		final Session session = getSessionFactory().newReadonlySession();
		try {
			return session.query(sql.toString(), new SysUserMapper(), paramRoleName, paramEnabled,
					paramKeyword);
		} finally {
			MiscUtils.closeQuietly(session);
		}
	}

	/**
	 * 查看角色列表
	 * 
	 * @param enabled
	 * @param keyword
	 * @return
	 */
	public Collection<Role> findAllRoles(@ParamName("enabled") final Boolean enabled,
			@ParamName("keyword") @ParamDefaultValue("") final String keyword) {
		return getService(OracleUserPermProvider.class).findRoles(keyword, enabled, 0, 0);
	}

	/**
	 * 查看角色信息
	 * 
	 * @param roleName
	 * @return
	 */
	public Role role(@ParamName("role-name") final String roleName) {
		return getService(OracleUserPermProvider.class).loadRoleByName(roleName);
	}

	/**
	 * 创建新角色
	 * 
	 * @param name
	 * @param title
	 * @param description
	 * @param enabled
	 * 
	 * @return
	 */
	public int addRole(@ParamName("role-name") final String name,
			@ParamName("title") final String title,
			@ParamName("description") @ParamDefaultValue("") final String description,
			@ParamName("enabled") @ParamDefaultValue("false") final Boolean enabled) {
		// if (getService(OracleUserPermProvider.class))
		final Role role = new Role(name, title, description, enabled);
		return getService(OracleUserPermProvider.class).addRole(role);
	}

	/**
	 * 更新角色
	 * 
	 * @param name
	 * @param title
	 * @param description
	 * @param enabled
	 * 
	 * @return
	 *         成功更新的角色记录数。
	 */
	public int updateRole(@ParamName("role-name") final String name,
			@ParamName("title") final String title,
			@ParamName("description") @ParamDefaultValue("") final String description,
			@ParamName("enabled") @ParamDefaultValue("false") final Boolean enabled) {
		final Role role = new Role(name, title, description, enabled);
		return getService(OracleUserPermProvider.class).updateRole(role);
	}

	/**
	 * 删除角色
	 * 
	 * @param name
	 *            角色名
	 * 
	 * @return 成功删除的角色记录数。
	 */
	public int deleteRole(@ParamName("role-name") final String name) {
		return getService(OracleUserPermProvider.class).removeRole(name);
	}

	/**
	 * 
	 * @param userName
	 * @return
	 */
	public Collection<String> findRolesByUser(@ParamName("user-name") final String userName) {
		return getService(OracleUserPermProvider.class).findRolesByUser(userName);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public Collection<Long> findPermsByRole(@ParamName("role-name") final String name) {
		return getService(OracleUserPermProvider.class).findPermsByRole(name);
	}

	/**
	 * 为角色分配权限
	 * 
	 * @param name
	 * @param perms
	 * @return
	 */
	public int assignPerms(@ParamName("role-name") final String name,
			@ParamName("perms") @ParamDefaultValue("") final String perms) {
		List<Long> p = new ArrayList<Long>();
		for (final String s : perms.split(",")) {
			if (s.isEmpty()) {
				continue;
			}

			final long l = NumberUtils.toLong(s.trim(), 0);
			if (l > 0) {
				p.add(l);
			}
		}

		return getService(OracleUserPermProvider.class)
				.assignPerms(name, ArrayUtils.toPrimitive(p.toArray(new Long[0])));
	}

	/**
	 * 
	 */
	private void ensureAdministrator() {
		getSessionFactory().beginTransaction(false);

		try {
			if (!getService(OracleUserPermProvider.class).isRoleExists(ADMIN_ROLE)) {
				final Role adminRole = new Role(ADMIN_ROLE, "administrators",
						"All administrators group", true);
				getService(OracleUserPermProvider.class).addRole(adminRole);
			}
			if (!getService(OracleUserPermProvider.class).isUserExists(ADMIN_USER)) {
				final User adminUser = new User(ADMIN_USER, true, null);
				adminUser.put(MOBILE_KEY, "");
				adminUser.put(EMAIL_KEY, "");
				adminUser.put(CREATE_DATE_KEY, new Date().getTime());
				getService(OracleUserPermProvider.class).addUser(adminUser);
				getService(OracleUserPermProvider.class).assignRoles(ADMIN_USER, ADMIN_ROLE);
			}
			getSessionFactory().commitTransaction();
		} finally {
			getSessionFactory().endTransaction();
		}
	}
}
