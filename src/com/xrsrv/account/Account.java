package com.xrsrv.account;

import org.apache.commons.lang3.StringUtils;

import com.xrsrv.system.enumerate.AccountStatus;
import com.xrsrv.system.enumerate.AccountType;

public abstract class Account {

	private final String loginName;

	private final String realName;

	private final String mobile;

	private final AccountStatus status;

	private final AccountType userType;
	
	private final long recInfoModCount;
	
	private final long bgPrivilege;

	public static final Account NONE = new NullAccount();

	/**
	 * 
	 * @param loginName
	 * @param realName
	 * @param mobile
	 * @param status
	 */
	protected Account(final String loginName, final String realName, final String mobile,
			final AccountStatus status, final AccountType userType,  final long recInfoModCount,final long bgPrivilege) {
		this.loginName = loginName == null ? "" : loginName.trim();
		this.realName = realName == null ? "" : realName.trim();
		this.mobile = mobile == null ? "" : mobile.trim();
		this.status = status;
		this.userType = userType;
		this.recInfoModCount = recInfoModCount;
		this.bgPrivilege = bgPrivilege;
	}

	public AccountType getUserType() {
		return userType;
	}

	/**
	 * 
	 * @return
	 */
	public final boolean isValid() {
		return !this.loginName.isEmpty();
	}

	/**
	 * @return the loginName
	 */
	public final String getLoginName() {
		return this.loginName;
	}

	/**
	 * @return the realName
	 */
	public final String getRealName() {
		return this.realName;
	}

	/**
	 * @return the mobile
	 */
	public final String getMobile() {
		return this.mobile;
	}
	
	/**
	 * @return showName 
	 */
	public final String getShowMobile() {
		if(StringUtils.isEmpty(this.mobile)) {
			return "";
		}
		return StringUtils.substring(this.mobile, 0,3)+"****"+StringUtils.substring(this.mobile, 7, 11);
	}

	/**
	 * 
	 * @return
	 */
	public final AccountStatus getStatus() {
		return this.status;
	}

	public long getRecInfoModCount() {
		return recInfoModCount;
	}

	public long getBgPrivilege() {
		return bgPrivilege;
	}

}

class NullAccount extends Account {

	/**
	 * 
	 */
	protected NullAccount() {
		super("", "", "", null, null, 0,0);
	}

}
