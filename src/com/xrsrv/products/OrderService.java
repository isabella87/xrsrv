package com.xrsrv.products;

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
import com.xinran.xrsrv.persistence.MajorProductDao;
import com.xinran.xrsrv.persistence.ProOrderDao;
import com.xinran.xrsrv.persistence.ProOrderDetailInfo;
import com.xrsrv.system.enumerate.NoType;

public class OrderService extends AggregatedService {

	private final int PAGESIZE = 6;
	
	/**
	 * 订购详细页订购商品生成对应订单
	 * @param mpId
	 * @param despatchFee
	 * @param payType
	 * @param ctx
	 * @return
	 */
	public boolean createProductOrder(@ParamName("mpId") long mpId,
			@ParamName("fee")@ParamDefaultValue("6.0") BigDecimal despatchFee,
			@ParamName("pay-type") @ParamDefaultValue("1") long payType, final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if(token.isGuest()){
			throw new ServiceException("1*请登录后，再订购！");
		}
		String orderNo = null;

		ProOrderDao proOrderDao = super.getDao(ProOrderDao.class);
		orderNo = generateOrderNo(NoType.ORDER_NO, 6);
		while (proOrderDao.existOrderOfOrderNo(orderNo)) {
			orderNo = generateOrderNo(NoType.ORDER_NO, 6);
		}

		Date date = new Date();
		long count = proOrderDao.createProOrderByMpId(mpId,
				Long.valueOf(token.getUserId()), orderNo, despatchFee, null,
				payType, token.get("LoginName"), date);
		return count > 0 ? true : false;
	}
	
	

	public boolean createProOrderInfo(
			@ParamName("file-path") String filePath,
			@ParamName("pro-name") String proName,
			@ParamName("price") BigDecimal price,
			@ParamName("visible") @ParamDefaultValue("1") long visible,
			@ParamName("product-type") @ParamDefaultValue("") String productType,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		ProductType proType = null;
		String proNo = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.strValue().equals(productType)) {
				proType = pt;
				break;
			}
		}

		ProOrderDao proOrderDao = super.getDao(ProOrderDao.class);
		proNo = generateOrderNo(NoType.ORDER_NO, 6);
		while (proOrderDao.existOrderOfOrderNo(proNo)) {
			proNo = generateOrderNo(NoType.ORDER_NO, 6);
		}

		Date date = new Date();
		long count = 0l;
		// long count = proOrderDao.createProOrderInfo(arg0, arg1, arg2, arg3,
		// arg4, arg5, arg6, arg7, arg8, arg9, arg10)
		return count > 0 ? true : false;
	}

	public boolean updateProOrderInfo(
			@ParamName("id") final long mpId,
			@ParamName("file-path") String filePath,
			@ParamName("pro-name") String proName,
			@ParamName("price") BigDecimal price,
			@ParamName("visible") @ParamDefaultValue("1") long visible,
			@ParamName("product-type") @ParamDefaultValue("") String productType,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		ProductType proType = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.strValue().equals(productType)) {
				proType = pt;
				break;
			}
		}

		MajorProductDao majorProductDao = super.getDao(MajorProductDao.class);
		Date date = new Date();
		long count = majorProductDao.updateProductInfo(mpId, proName, price,
				proType.strValue(), 0L, 1L, token.get("loginName"), date);
		return count > 0 ? true : false;
	}

	private static String generateOrderNo(NoType noType, int count) {
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

	public Map<String, Object> queryProductOrderList(
			@ParamName("pn") @ParamDefaultValue("1") int pn,
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate,
			@ParamName("status") final long status,
			@ParamName("key") final String searchKey,
			@ParamName("pay-type") final long payType, ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		long auId = NumberUtils.toLong(token.getUserId(), 0L);
		AuthInfo authInfo = super.getDao(AccUserRegDao.class)
				.authenticate(auId);
		boolean isBg = authInfo.getBgPrivilege() == 1 ? true : false;
		ProOrderDao proOrderDao = super.getDao(ProOrderDao.class);

		Collection<ProOrderDetailInfo> items = proOrderDao
				.queryProOrderInfoList(startDate, endDate, isBg ? null : auId,
						status == 65535 ? null : status,
						payType == 65535 ? null : payType,(pn - 1) * PAGESIZE,
						PAGESIZE);
		long count = proOrderDao.queryProOrderInfoListCount(startDate, endDate,
				isBg ? null : auId, status == 65535 ? null : status,
				payType == 65535 ? null : payType);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("items", items);
		map.put("pc", (int) Math.ceil((double) count / PAGESIZE));
		return map;
	}

	public ProOrderDetailInfo queryProductOrderInfoById(
			@ParamName("id") final long poId, ServiceContext ctx) {
		return super.getDao(ProOrderDao.class).queryProOrderInfo(poId);
	}

	public long deleteProOrderInfoById(@ParamName("id") final long poId,
			ServiceContext ctx) {
		return super.getDao(ProOrderDao.class).deleteProOrderInfo(poId);
	}

	public long downProOrderInfoById(@ParamName("id") final long poId,
			ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(ProOrderDao.class).updateProOrderInfoStatus(poId,
				0L, 1L, token.get("loginName"), new Date());
	}

	public long submitProOrderInfoById(@ParamName("id") final long poId,
			ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(ProOrderDao.class).updateProOrderInfoStatus(poId,
				1L, 0L, token.get("loginName"), new Date());
	}

	/**
	 * 支付订单
	 * 
	 * @param poId
	 * @param ctx
	 * @return
	 */
	public long payProOrderInfoById(@ParamName("id") final long poId,
			ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		return super.getDao(ProOrderDao.class).updateProOrderInfoStatus(poId,
				2L, 1L, token.get("loginName"), new Date());
	}

}
