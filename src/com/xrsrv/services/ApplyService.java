package com.xrsrv.services;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;

import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xinran.xrsrv.persistence.AuthInfo;
import com.xinran.xrsrv.persistence.MajorServiceDao;
import com.xinran.xrsrv.persistence.ServiceInfo;
import com.xrsrv.system.enumerate.NoType;

public class ApplyService extends AggregatedService {

	private final int PAGESIZE = 6;

	public boolean createApplyService(@ParamName("name") String name,
			@ParamName("service-name") String serviceName, @ParamName("mobile") String mobile,
			@ParamName("id-card") String idCard, @ParamName("address") String address,
			@ParamName("content") String content, @ParamName("due-time") Date dueTime,
			@ParamName("service-type") String type, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		long userType = 1;
		long auId = NumberUtils.toLong(token.getUserId(), 0L);
		Long gender = null;
		if(idCard!=null&&!idCard.isEmpty()&&(idCard.length()==15||idCard.length()==18)){
			gender = NumberUtils.toLong(idCard.length() == 15 ? idCard.substring(14, 15) : idCard
				.substring(16, 17)) % 2;
		}

		Date date = new Date();
		if (token.isGuest()) {
			userType = 0;
		}
		ServiceType serviceType = null;
		for (ServiceType st : ServiceType.values()) {
			if (st.strValue().equals(type)) {
				serviceType = st;
				break;
			}
		}
		MajorServiceDao majorServiceDao = super.getDao(MajorServiceDao.class);
		String serviceNo = generateServiceNo(NoType.SERVICE_NO, 6);
		while (majorServiceDao.existServiceOfServiceNo(serviceNo)) {
			serviceNo = generateServiceNo(NoType.SERVICE_NO, 6);
		}
		long count = majorServiceDao.createMajorServiceInfo(serviceName, serviceNo, content,
				dueTime, mobile, address, serviceType.strValue(), name, idCard, gender, auId,
				userType, null, new BigDecimal(0), 0L, date, null);
		return count > 0 ? true : false;
	}

	public boolean updateApplyService(@ParamName("ms-id") long msId,
			@ParamName("name") String name, @ParamName("mobile") String mobile,
			@ParamName("service-name") String serviceName, @ParamName("id-card") String idCard,
			@ParamName("address") String address, @ParamName("content") String content,
			@ParamName("due-time") Date dueTime, @ParamName("type") String type,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		long gender = NumberUtils.toLong(idCard.length() == 15 ? idCard.substring(14, 15) : idCard
				.substring(16, 17)) % 2;
		long auId = NumberUtils.toLong(token.getUserId(), 0L);
		long userType = 1;
		Date date = new Date();
		if (token.isGuest()) {
			userType = 0;
		}
		return super.getDao(MajorServiceDao.class).updateMajorServiceInfo(msId, serviceName,
				content, dueTime, mobile, address, type, name, idCard, gender, auId, userType,
				null, new BigDecimal(0), 1L, date) > 0 ? true : false;
	}
	
	/**
	 * 结清服务
	 * @param msId
	 * @param ctx
	 * @return
	 */
	public long completedService(@ParamName("id") long msId,@ParamName("dueDate") Date dueDate,@ParamName("tracker") String tracker,@ParamName("amt") BigDecimal amt,final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(MajorServiceDao.class).acceptMajorService(msId, amt,99L, 1L,dueDate, new Date(),tracker);
		
	}
	
	public long acceptService(@ParamName("id") long msId,@ParamName("dueDate") Date dueDate,@ParamName("tracker") String tracker,@ParamName("amt") BigDecimal amt,final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(MajorServiceDao.class).acceptMajorService(msId, amt,1L, 0L,dueDate, new Date(),tracker);
		
	}
	
	/**
	 * 
	 * @param msId
	 * @param ctx
	 * @return
	 */
	public long submitService(@ParamName("id") long msId,final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(MajorServiceDao.class).updateMajorServiceInfoStatus(msId, 0L, -1L, new Date());
	}

	public Collection<ServiceInfo> queryTopTenList(){
		return super.getDao(MajorServiceDao.class).queryServiceInfoList(null, null,null, null, null , null, 0,10);
	}
	
	public Map<String, Object> queryApplyServiceList(
			@ParamName("pn") @ParamDefaultValue("1") int pn,
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate, @ParamName("status") final long status,
			@ParamName("search-key") final String searchKey, ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		long auId = NumberUtils.toLong(token.getUserId(), 0L);
		AuthInfo authInfo = super.getDao(AccUserRegDao.class).authenticate(auId);
		boolean isBg = authInfo.getBgPrivilege() == 1 ? true : false;
		MajorServiceDao majorServiceDao = super.getDao(MajorServiceDao.class);
		Collection<ServiceInfo> items = majorServiceDao.queryServiceInfoList(startDate, endDate,
				status == 65535 ? null : status, null, isBg ? null : auId, searchKey, (pn - 1)
						* PAGESIZE, PAGESIZE);
		long count = majorServiceDao.queryServiceInfoListCount(startDate, endDate,
				status == 65535 ? null : status, null, isBg ? null : auId, searchKey);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("items", items);
		map.put("pc", (int) Math.ceil((double) count / PAGESIZE));

		return map;
	}

	public ServiceInfo queryApplyServiceById(@ParamName("id") final long msId) {
		return super.getDao(MajorServiceDao.class).queryServiceInfo(msId);
	}

	public static void main(String[] args) {
		String idCard = "340825197707078278";
		// System.out.println(idCard.substring(14, 15));
		System.out.println(idCard.substring(17, 18));
		long gender = NumberUtils.toLong(idCard.length() == 15 ? idCard.substring(14, 15) : idCard
				.substring(16, 17)) % 2;
		System.out.println(gender);
	}

	private static String generateServiceNo(NoType noType, int count) {
		if (count < 1) {
			throw new ServiceException("21*count不能小于1!");
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String date = df.format(new Date());
		return noType.longValue() + date + generateRandom(count);
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
}
