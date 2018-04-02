/**
 * 
 */
package com.xrsrv.account;

import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xinran.xrsrv.persistence.AuthInfo;
import com.xrsrv.system.enumerate.AccountStatus;
import com.xrsrv.system.enumerate.AccountType;

/**
 * 
 * 
 * @author Isabella
 *
 */
public class UserAuthorityService extends AuthorityService {

	@Override
	public long validate(String loginNameOrMobile, String hash) {
		return super.getDao(AccUserRegDao.class).validate(loginNameOrMobile, hash);
	}

	@Override
	public AccUserReg getAccUserRegInfo(long auId) {
		return super.getDao(AccUserRegDao.class).queryAccUserRegByAuId(auId);
	}

	@Override
	public Account getAccount(String loginName, String realName, String mobile,
			AccountStatus status, AccountType type,  long recInfoModCount,long bgPrivilege) {
		return new UserAccount(loginName, realName, mobile, status, type,  recInfoModCount,bgPrivilege);
	}

	@Override
	protected AuthInfo authenticate(long userId) {
		return super.getDao(AccUserRegDao.class).authenticate(userId);
	}

}
