package com.xrsrv.wap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xinran.xrsrv.persistence.ZfbOrderDao;
import com.xinran.xrsrv.persistence.ZfbOrderInfo;
import com.xinran.xrsrv.persistence.ZfbServiceDao;
import com.xinran.xrsrv.persistence.ZfbServiceInfo;
import com.xrsrv.system.enumerate.NoType;

public class WapBaseService extends AggregatedService {

	/**
	 * 
	 * @param id
	 * 			- 服务号或商品号
	 * @param subject
	 * 			-商品名
	 * @param type
	 * 			- 1：服务；2：商品；
	 * @param ctx
	 * @return
	 */
	public String execute(@ParamName("id") final long id,
			@ParamName("type")@ParamDefaultValue("1") final int type,
			final ServiceContext ctx) {

		BaseConfigurator.configure();
		
		
		String bizContent = null;
		switch(type){
		case 1:
			bizContent =createServiceBizContent(id);
			break;
		case 2:
			bizContent =createOrderBizContent(id);
			break;
			default:
				;
		}
		
		AlipayClient alipayClient = new DefaultAlipayClient(
				Configuration.getString(Configuration.SERVER_URL),
				Configuration.getString(Configuration.APP_ID),
				Configuration.getString(Configuration.APP_PRIVATE_KEY),
				Configuration.APPLICATION_JSON, Configuration.DEFAULT_CHARSET,
				Configuration.getString(Configuration.PALIPAY_PUBLIC_KEY),
				Configuration.SIGN_TYPE); // 获得初始化的AlipayClient
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
		alipayRequest.setReturnUrl(type==1?Configuration
				.getString(Configuration.SERVICE_RETURN_URL):Configuration
				.getString(Configuration.OREDER_RETURN_URL));
		alipayRequest.setNotifyUrl(Configuration
				.getString(Configuration.NOTIFY_URL));// 在公共参数中设置回跳和通知地址
		alipayRequest.setBizContent(bizContent);// 填充业务参数
		
		String form = "";
		try {
			form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
			return form;
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String createServiceBizContent(long id) {
		// 获取服务订单信息，创建指令数据
		ZfbServiceDao zfbServiceDao = super.getDao(ZfbServiceDao.class);
		String timeoutExpress = Configuration.getString(Configuration.TIMEOUT_EXPRESS);
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String timeExpire=dateFormat.format(now);
		String soutTranNo = generateOutTranNo(NoType.SERVICE_NO, 6);
		zfbServiceDao.createZfbServiceInfo(timeoutExpress, timeExpire,soutTranNo, id, 0L, now);
		long zsId = zfbServiceDao.getLastRowLongId();
		zfbServiceDao.updateXfbServicePassbackParams(zsId, "1:"+zsId);
		
		ZfbServiceInfo zfbServiceInfo = zfbServiceDao.queryZfbServiceInfo(zsId);
		String passbackParams ="";
		try {
			passbackParams = URLEncoder.encode(zfbServiceInfo.getPassbackParams(),   "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{")
		.append("\"out_trade_no\":\"").append(zfbServiceInfo.getOutTradeNo()).append("\",")
		.append("\"timeout_express\":\"").append(zfbServiceInfo.getTimeoutExpress()).append("\",")
		/*.append("\"time_expire\":\"").append(zfbServiceInfo.getTimeExpire()).append("\",")*/
		.append("\"total_amount\":\"").append(zfbServiceInfo.getTotalAmount().doubleValue()).append("\",")
		.append("\"subject\":\"").append(zfbServiceInfo.getSubject()).append("\",")
		.append("\"body\":\"").append(zfbServiceInfo.getBody()).append("\",")
		.append("\"goods_type\":\"").append(zfbServiceInfo.getGoodsType()).append("\",")
		.append("\"passback_params\":\"").append(passbackParams).append("\",")
		.append("\"product_code\":\"QUICK_WAP_PAY\"").append("}");
		return sb.toString();
	}
	private String createOrderBizContent(long id) {
		// 获取商品订单信息，创建指令数据
				ZfbOrderDao zfbOrderDao = super.getDao(ZfbOrderDao.class);
				String timeoutExpress = Configuration.getString(Configuration.TIMEOUT_EXPRESS);
				Date now = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String timeExpire=dateFormat.format(now);
				String soutTranNo = generateOutTranNo(NoType.ORDER_NO, 6);
				zfbOrderDao.createZfbOrderInfo(timeoutExpress, timeExpire,soutTranNo, id, 0L, now);
				long zoId = zfbOrderDao.getLastRowLongId();
				zfbOrderDao.updateXfbServicePassbackParams(zoId, "2:"+zoId);
				
				ZfbOrderInfo zfbOrderInfo = zfbOrderDao.queryZfbOrderInfo(zoId);
				String passbackParams ="";
				try {
					passbackParams = URLEncoder.encode(zfbOrderInfo.getPassbackParams(),   "utf-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				StringBuffer sb = new StringBuffer();
				sb.append("{")
				.append("\"out_trade_no\":\"").append(zfbOrderInfo.getOutTradeNo()).append("\",")
				.append("\"timeout_express\":\"").append(zfbOrderInfo.getTimeoutExpress()).append("\",")
				/*.append("\"time_expire\":\"").append(zfbOrderInfo.getTimeExpire()).append("\",")*/
				.append("\"total_amount\":\"").append(zfbOrderInfo.getTotalAmount().doubleValue()).append("\",")
				.append("\"subject\":\"").append(zfbOrderInfo.getSubject()).append("\",")
				.append("\"body\":\"").append(zfbOrderInfo.getBody()).append("\",")
				.append("\"goods_type\":\"").append(zfbOrderInfo.getGoodsType()).append("\",")
				.append("\"passback_params\":\"").append(passbackParams).append("\",")
				.append("\"product_code\":\"QUICK_WAP_PAY\"").append("}");
				return sb.toString();
	}
	private static String generateOutTranNo(NoType noType, int count) {
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
	public static void main(String[] args){
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeExpire=dateFormat.format(now);
		System.out.println(timeExpire);
	}
}
