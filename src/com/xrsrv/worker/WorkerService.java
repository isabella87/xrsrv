package com.xrsrv.worker;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.security.AuthenticationToken;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;

import com.xinran.xrsrv.persistence.AccUserReg;
import com.xinran.xrsrv.persistence.AccUserRegDao;
import com.xinran.xrsrv.persistence.BgWorkerInfo;
import com.xinran.xrsrv.persistence.FileInfo;
import com.xinran.xrsrv.persistence.MaintenanceManDao;
import com.xinran.xrsrv.persistence.WorkerInfoAndFileId;
import com.xrsrv.file.DefaultFileServiceImpl;
import com.xrsrv.file.FileService;

public class WorkerService extends AggregatedService {

	private final int PAGESIZE = 9;

	private final int MAINTENANCE_MAN_FILE_TYPE = 11;

	public boolean applyMaintenanceWorker(@ParamName("mm-name") String mmName,
			@ParamName("mm-level") long mmLevel,
			@ParamName("mm-mobile") @ParamDefaultValue("") String mobile,
			@ParamName("mm-intro") @ParamDefaultValue("") String intro,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		if (token.isGuest()) {
			throw new ServiceException("1*请先登录！");
		}
		long auId = NumberUtils.toLong(token.getUserId(), 0L);
		WorkerLevelType workerKevelType = null;
		for (WorkerLevelType wl : WorkerLevelType.values()) {
			if (wl.longValue() == mmLevel) {
				workerKevelType = wl;
				break;
			}
		}

		AccUserRegDao userDao = super.getDao(AccUserRegDao.class);
		AccUserReg userInfo = userDao.queryAccUserRegByAuId(auId);
		if (userInfo.getMmId() > 0) {
			return true;
		}
		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);

		Date date = new Date();
		String mmNo = "SJ"+getStrOfYear(date);
		long count = maintenanceManDao.createWorkerInfo(mmName, mmNo,
				workerKevelType.strValue(), mobile, intro, 0L,
				token.getUserId(), date, null, null);
		long mmId = maintenanceManDao.getLastRowLongId();
		maintenanceManDao.updateWorkerInfoMmNo(mmId, mmNo + mmId);
		long c1 = userDao.updateMmId(auId, mmId);

		return count > 0 && c1 > 0 ? true : false;
	}


	public String getStrOfYear(Date date){
		SimpleDateFormat df = new SimpleDateFormat("yyyy");
		return df.format(date);
	}

	public boolean uploadMaintenanceWorker(
			@ParamName("file-name") String fileName,
			@ParamName("mm-name") String mmName,
			@ParamName("mm-level") long mmLevel,
			@ParamName("mm-mobile") @ParamDefaultValue("") String mobile,
			@ParamName("mm-intro") @ParamDefaultValue("") String intro,
			@ParamName("file-content") @ParamDefaultValue("") String fileContent,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		WorkerLevelType workerKevelType = null;
		for (WorkerLevelType wl : WorkerLevelType.values()) {
			if (wl.longValue() == mmLevel) {
				workerKevelType = wl;
				break;
			}
		}

		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);

		Date date = new Date();
		String mmNo = "SJ"+getStrOfYear(date);
		long count = maintenanceManDao.createWorkerInfo(mmName, mmNo,
				workerKevelType.strValue(), mobile, intro, 1L,
				token.getUserId(), date, null, null);
		long mmId = maintenanceManDao.getLastRowLongId();
		maintenanceManDao.updateWorkerInfoMmNo(mmId, mmNo + mmId);
		final FileService fileService = getService(DefaultFileServiceImpl.class);

		long fId = fileService.create(mmId, MAINTENANCE_MAN_FILE_TYPE,
				fileName, "", true, ctx);
		int uploadResult = fileService.upload(fId,
				Base64.decodeBase64(fileContent), ctx);
		// TODO 创建图片并上传

		return count > 0 && uploadResult > 0 ? true : false;
	}

	public boolean uploadSinglemaintenanceManPic(
			@ParamName("file-name") String fileName,
			@ParamName("mm-id") String mmIdStr,
			@ParamName("file-content") @ParamDefaultValue("") String fileContent,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		// long mpId
		mmIdStr = mmIdStr.replace("，", ",");
		if (mmIdStr.contains(",")) {
			String[] mmIds = mmIdStr.split(",");
			int i = 0;
			for (i = 0; i < mmIds.length; i++) {
				long mmId = Long.valueOf(mmIds[i]);

				// TODO 创建图片并上传
				uploadMaintenanceManPic(mmId, fileName, fileContent, ctx);
			}
			if (i == mmIds.length) {
				return true;
			} else {
				return false;
			}
		} else {
			return uploadMaintenanceManPic(Long.valueOf(mmIdStr), fileName,
					fileContent, ctx);
		}
	}

	private boolean uploadMaintenanceManPic(long mmId, String fileName,
			String fileContent, final ServiceContext ctx) {
		FileInfo fi = super.getDao(MaintenanceManDao.class)
				.queryFileInfoWithMaxMmId(mmId);

		final FileService fileService = getService(DefaultFileServiceImpl.class);

		// 删除已有的文件
		if (fi != null && fi.getFileId() != null && fi.getFileId() != 0) {
			fileService.delete(fi.getFileId(), ctx);
		}

		long fId = fileService.create(mmId, MAINTENANCE_MAN_FILE_TYPE,
				fileName, "", true, ctx);
		int uploadResult = fileService.upload(fId,
				Base64.decodeBase64(fileContent), ctx);
		return uploadResult > 0 ? true : false;
	}

	public boolean updateMaintenanceMan(@ParamName("mm-id") final long mmId,
			@ParamName("file-path") String filePath,
			@ParamName("mm-name") String mmName,
			@ParamName("mm-level") long mmLevel,
			@ParamName("mm-mobile") @ParamDefaultValue("") String mobile,
			@ParamName("mm-intro") @ParamDefaultValue("") String intro,
			final ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		WorkerLevelType workerKevelType = null;
		for (WorkerLevelType wl : WorkerLevelType.values()) {
			if (wl.longValue() == mmLevel) {
				workerKevelType = wl;
				break;
			}
		}

		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);
		Date date = new Date();
		long count = maintenanceManDao.updateWorkerInfo(mmId, mmName, workerKevelType.strValue(), mobile, intro, 1L,
				token.get("loginName"), date);
		return count > 0 ? true : false;
	}

	public Map<String, Object> queryMaintenanceManList(
			@ParamName("pn") @ParamDefaultValue("1") int pn,
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate,
			@ParamName("isMainPage") @ParamDefaultValue("N") final Boolean isMainPage,
			@ParamName("search-key") final String searchKey, ServiceContext ctx) {
		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);

		Collection<WorkerInfoAndFileId> items = maintenanceManDao
				.queryWorkerInfoList(isMainPage ? 1L : null, startDate,
						endDate, searchKey, (pn - 1) * PAGESIZE, PAGESIZE);
		long count = maintenanceManDao.queryWorkerInfoListCount(isMainPage ? 1L
				: null, startDate, endDate, searchKey);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("items", items);
		map.put("pc", (int) Math.ceil((double) count / PAGESIZE));
		map.put("rc", count);
		return map;
	}

	public Collection<WorkerInfoAndFileId> queryTopMaintenanceManList(
			ServiceContext ctx) {
		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);

		return maintenanceManDao
				.queryWorkerInfoList(1L, null, null, null, 0, 3);
	}

	public List<BgWorkerInfo> queryMaintenanceManListForBg(
			@ParamName("start-date") final Date startDate,
			@ParamName("end-date") final Date endDate,
			@ParamName("status") final Long status,
			@ParamName("search-key") final String searchKey, ServiceContext ctx) {
		MaintenanceManDao maintenanceManDao = super
				.getDao(MaintenanceManDao.class);

		return maintenanceManDao.queryWorkerInfoListForBg(null, startDate,
				endDate, searchKey, 0, Integer.MAX_VALUE);
	}

	public WorkerInfoAndFileId queryMaintenanceManById(
			@ParamName("id") final long mmId, ServiceContext ctx) {
		return super.getDao(MaintenanceManDao.class).queryWorkerInfo(mmId);
	}

	public long deleteMaintenanceManById(@ParamName("id") final long mmId,
			ServiceContext ctx) {
		return super.getDao(MaintenanceManDao.class).deleteWorkerInfo(mmId);
	}

	/**
	 * 上架mpId该产品，使得在前台商品列表可见
	 * 
	 * @param mpId
	 * @param ctx
	 * @return
	 */
	public long upMaintenanceManById(@ParamName("id") final long mmId,
			ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		Date date = new Date();
		return super.getDao(MaintenanceManDao.class).updateWorkerInfoStatus(
				mmId, 1L, 0L, token.get("loginName"), date);
	}

	/**
	 * 产品下架
	 * 
	 * @param mpId
	 * @param ctx
	 * @return
	 */
	public long downMaintenanceManById(@ParamName("id") final long mmId,
			ServiceContext ctx) {
		final AuthenticationToken token = ctx.getToken();
		Date date = new Date();
		return super.getDao(MaintenanceManDao.class).updateWorkerInfoStatus(
				mmId, 0L, 1L, token.get("loginName"), date);
	}

}
