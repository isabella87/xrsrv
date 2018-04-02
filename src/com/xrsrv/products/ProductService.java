package com.xrsrv.products;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;

import com.xinran.xrsrv.persistence.BgProductInfo;
import com.xinran.xrsrv.persistence.MajorProductDao;
import com.xinran.xrsrv.persistence.ProductInfo;
import com.xinran.xrsrv.persistence.ProductInfoAndFileId;
import com.xrsrv.file.DefaultFileServiceImpl;
import com.xrsrv.file.FileService;
import com.xrsrv.system.enumerate.NoType;

public class ProductService extends AggregatedService {

	private final int PAGESIZE = 9;

	public boolean createMajorProduct(@ParamName("file-path") String filePath,
			@ParamName("pro-name") String proName, @ParamName("price") BigDecimal price,
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

		MajorProductDao majorProductDao = super.getDao(MajorProductDao.class);
		proNo = generateProNo(NoType.PRODUCT_NO, 6);
		while (majorProductDao.existProductOfProNo(proNo)) {
			proNo = generateProNo(NoType.PRODUCT_NO, 6);
		}

		Date date = new Date();
		long count = majorProductDao.createProductInfo(proName, proNo, price, proType.strValue(),
				0L, visible, token.get("loginName"), date, null, null);

		// TODO 创建图片并上传

		return count > 0 ? true : false;
	}

	public boolean uploadMajorProduct(@ParamName("file-name") String fileName,
			@ParamName("product-name") String proName,
			@ParamName("product-price") BigDecimal price,
			@ParamName("visible") @ParamDefaultValue("1") long visible,
			@ParamName("product-type") @ParamDefaultValue("") long productType,
			@ParamName("file-content") @ParamDefaultValue("") String fileContent,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		ProductType proType = null;
		String proNo = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.longValue() == productType) {
				proType = pt;
				break;
			}
		}

		MajorProductDao majorProductDao = super.getDao(MajorProductDao.class);
		proNo = generateProNo(NoType.PRODUCT_NO, 6);
		while (majorProductDao.existProductOfProNo(proNo)) {
			proNo = generateProNo(NoType.PRODUCT_NO, 6);
		}

		Date date = new Date();
		long count = majorProductDao.createProductInfo(proName, proNo, price, proType.strValue(),
				0L, visible, token.getUserId(), date, null, null);

		long mpId = majorProductDao.getLastRowLongId();
		final FileService fileService = getService(DefaultFileServiceImpl.class);

		ProductType pType = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.strValue().equals(productType)) {
				pType = pt;
				break;
			}
		}

		long fId = fileService.create(mpId, productType, fileName, "", true, ctx);
		int uploadResult = fileService.upload(fId, Base64.decodeBase64(fileContent), ctx);
		// TODO 创建图片并上传

		return count > 0 ? true : false;
	}

	public boolean uploadSingleMajorProduct(@ParamName("file-name") String fileName,
			@ParamName("mp-id") String mpIdStr,
			@ParamName("file-content") @ParamDefaultValue("") String fileContent,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		// long mpId
		mpIdStr = mpIdStr.replace("，", ",");
		if (mpIdStr.contains(",")) {
			String[] mpIds = mpIdStr.split(",");
			int i =0;
			for (i=0; i < mpIds.length; i++) {
				long mpId = Long.valueOf(mpIds[i]);

				// TODO 创建图片并上传
				uploadProductPic(mpId, fileName, fileContent, ctx);
			}
			if(i==mpIds.length){
				return true;
			}else{
				return false;
			}
		} else {
			return uploadProductPic(Long.valueOf(mpIdStr), fileName, fileContent, ctx);
		}
	}

	private boolean uploadProductPic(long mpId, String fileName, String fileContent,
			final ServiceContext ctx) {
		ProductInfoAndFileId pi = super.getDao(MajorProductDao.class).queryProductInfo(mpId);

		ProductType pType = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.strValue().equals(pi.getType())) {
				pType = pt;
				break;
			}
		}

		final FileService fileService = getService(DefaultFileServiceImpl.class);

		// 删除已有的文件
		if (pi!=null&&pi.getFileId() != null && pi.getFileId() != 0) {
			fileService.delete(pi.getFileId(), ctx);
		}

		long fId = fileService.create(mpId, pType.longValue(), fileName, "", true, ctx);
		int uploadResult = fileService.upload(fId, Base64.decodeBase64(fileContent), ctx);
		return uploadResult > 0 ? true : false;
	}

	public boolean updateMajorProduct(@ParamName("mp-id") final long mpId,
			@ParamName("file-path") String filePath, @ParamName("pro-name") String proName,
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
		long count = majorProductDao.updateProductInfo(mpId, proName, price, proType.strValue(),
				0L, 1L, token.get("loginName"), date);
		return count > 0 ? true : false;
	}

	private static String generateProNo(NoType noType, int count) {
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

	public Map<String, Object> queryMajorProductList(
			@ParamName("pn") @ParamDefaultValue("1") int pn,
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate,
			@ParamName("isMainPage") @ParamDefaultValue("N") final Boolean isMainPage,
			@ParamName("search-key") final String searchKey,
			@ParamName("product-type") final String productType, ServiceContext ctx) {
		MajorProductDao majorProductDao = super.getDao(MajorProductDao.class);

		Collection<ProductInfo> items = majorProductDao.queryProductInfoList(productType,
				isMainPage ? 1L : null, startDate, endDate, searchKey,(pn - 1) * PAGESIZE,
				PAGESIZE);
		long count = majorProductDao.queryProductInfoListCount(productType, isMainPage ? 1L : null,
				startDate, endDate, searchKey);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("items", items);
		map.put("pc", (int) Math.ceil((double) count / PAGESIZE));
		map.put("rc", count);
		return map;
	}

	public Collection<BgProductInfo> queryMajorProductListForBg(
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate, @ParamName("status") final Long status,
			@ParamName("search-key") final String searchKey,
			@ParamName("product-type") final String productType, ServiceContext ctx) {
		MajorProductDao majorProductDao = super.getDao(MajorProductDao.class);

		return majorProductDao.queryProductInfoListForBg(productType, status, startDate, endDate,
				searchKey, 0, Integer.MAX_VALUE);
	}

	public ProductInfoAndFileId queryMajorProductById(@ParamName("id") final long mpId,
			ServiceContext ctx) {
		ProductInfoAndFileId productInfo = super.getDao(MajorProductDao.class).queryProductInfo(
				mpId);
		return productInfo;
	}

	public long deleteMajorProductById(@ParamName("id") final long mpId, ServiceContext ctx) {
		return super.getDao(MajorProductDao.class).deleteProductInfo(mpId);
	}

	/**
	 * 上架mpId该产品，使得在前台商品列表可见
	 * 
	 * @param mpId
	 * @param ctx
	 * @return
	 */
	public long upMajorProductById(@ParamName("id") final long mpId, ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		Date date = new Date();
		return super.getDao(MajorProductDao.class).updateProductInfoStatus(mpId, 1L, 0L,
				token.get("loginName"), date);
	}

	/**
	 * 产品下架
	 * 
	 * @param mpId
	 * @param ctx
	 * @return
	 */
	public long downMajorProductById(@ParamName("id") final long mpId, ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		Date date = new Date();
		return super.getDao(MajorProductDao.class).updateProductInfoStatus(mpId, -1L, 1L,
				token.get("loginName"), date);
	}

}
