/**
 * 
 */
package com.xrsrv.security;

import java.util.Date;

/**
 * 
 * @author Isabella
 *
 */
public final class SysUser {
	private final String userName;
	private final String[] roles;
	private String mobile;
	private String email;
	private Date createDate;
	private final boolean enabled;

	/**
	 * 
	 * @param userName
	 * @param roles
	 * @param enabled
	 */
	public SysUser(final String userName, final String[] roles, final boolean enabled) {
		this.userName = userName == null ? "" : userName.trim();
		this.roles = roles == null ? new String[0] : roles;
		this.enabled = enabled;
	}

	/**
	 * @return the userName
	 */
	public final String getUserName() {
		return this.userName;
	}

	/**
	 * @return the roles
	 */
	public final String[] getRoles() {
		return this.roles;
	}

	/**
	 * @return the mobile
	 */
	public final String getMobile() {
		return this.mobile;
	}

	/**
	 * 
	 * @param mobile
	 */
	public final void setMobile(final String mobile) {
		this.mobile = mobile == null ? "" : mobile.trim();
	}

	/**
	 * @return the email
	 */
	public final String getEmail() {
		return this.email;
	}

	/**
	 * 
	 * @param email
	 */
	public final void setEmail(final String email) {
		this.email = email == null ? "" : email.trim();
	}

	/**
	 * @return the createDate
	 */
	public final Date getCreateDate() {
		return this.createDate;
	}

	/**
	 * 
	 * @param date
	 */
	public final void setCreateDate(final Date date) {
		this.createDate = date;
	}

	/**
	 * 
	 * @param n
	 */
	public final void setCreateDate(final Number n) {
		this.createDate = n == null ? null : new Date(n.longValue());
	}

	/**
	 * @return the enabled
	 */
	public final boolean isEnabled() {
		return this.enabled;
	}

}
