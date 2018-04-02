package com.xrsrv.msg;

import java.util.Collection;
import java.util.Date;

import org.xx.armory.commons.MiscUtils;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;

import com.xinran.xrsrv.persistence.AccMsgDao;
import com.xinran.xrsrv.persistence.MyDict;
import com.xinran.xrsrv.persistence.MyDictDao;

public final class MsgService extends AggregatedService {

	public MsgService() {
	}

	public Collection<MyDict> queryMyDict(){
		return super.getDao(MyDictDao.class).getMyDict();
	}
	
	public boolean create(final ServiceContext ctx){
		
		super.getDao(MyDictDao.class).createMyDict(11L, "02", "value11", 1L, "", new Date());
		
		return true;
	}
	
	/**
	 * 
	 * @param mobile
	 * @param content
	 * @param code
	 * @param pid
	 * @param datepoint
	 * @return
	 */
	public final boolean sendMsg(final String mobile, final String content, final MsgCode code,
			final long pid, final Date datepoint) {
		return sendMsgYm(mobile, content, code, pid, datepoint) > 0L;
	}

	/**
	 * 
	 * @param mobile
	 * @param datepoint
	 * @return
	 */
	public static final String newCode(final String mobile, final Date datepoint) {
		final byte[] b = MiscUtils.uniqueHashBytes(new Object[] { mobile, datepoint });

		final StringBuilder sb = new StringBuilder();
		for (int i = 1; i < 7 && i < b.length; ++i) {
			final int bb = b[i] % 10;
			sb.append(bb < 0 ? -bb : bb);
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param mobile
	 * @param content
	 * @param code
	 * @param pid
	 * @param datepoint
	 * @return
	 */
	private long sendMsgYm(final String mobile, final String content, final MsgCode code,
			final long pid, final Date datepoint) { 
		return super.getDao(AccMsgDao.class).createSmsYmCode(mobile, content, Long.valueOf(code.value()), pid, datepoint);
	}

	public static void main(final String[] argv) {
		System.out.println(newCode("abce", new Date()));
		System.out.println(newCode("def", new Date()));
		System.out.println(newCode("gg234", new Date()));
	}
}
