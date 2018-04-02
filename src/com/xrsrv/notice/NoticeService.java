package com.xrsrv.notice;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.commons.Converter;
import org.xx.armory.commons.MiscUtils;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;
import org.xx.armory.vfs.FileObject;
import org.xx.armory.vfs.FileSystemProvider;

import com.xinran.xrsrv.persistence.CmNoticeDao;
import com.xinran.xrsrv.persistence.NoticeInfo;
import com.xinran.xrsrv.persistence.NoticeInfoDetail;

public class NoticeService extends AggregatedService {

	private static final int PORTAL_TOP_INFO_COUNT = 6;
	private static final int PORTAL_BOTTOM_INFO_COUNT = 5;
	private static final int PORTAL_BOTTOM_MEDIA_INFO_COUNT = 3;
	private static final int INFO_PAGESIZE = 5;

	/**
	 * 创建公告
	 * 
	 * @param cnId
	 * @param type
	 *            类型。1，还款公告；2，平台公告；3，媒体报道；4，平台动态；5，互联网金融；6，工程金融；7，政策法规；8，投资策略；
	 *            9，工程贷；10，债权转让；11班汇宝；12，班汇通高手；13社会责任；14，投资人说。
	 * @param title
	 *            公告标题
	 * @param key
	 *            关键字
	 * @param content
	 *            公告内容
	 * @param status
	 *            公告状态。0，内部审核。1，正式发布。
	 * @param recommend
	 *            是否推荐。0，不推荐。1，推荐。
	 * @param abstractContent
	 *            内容摘要
	 * @param priority
	 *            优先级，用于设定上下移动
	 * @param creator
	 *            创建者
	 * @return
	 * @throws ServiceException
	 */
	public long createNotice(@ParamName("noticeType") String type,
			@ParamName("noticeTitle") String title,
			@ParamName("notice-key") String key,
			@ParamName("noticeContent") String content,
			@ParamName("notice-status")@ParamDefaultValue("0") int status,
			@ParamName("notice-recommend") @ParamDefaultValue("0") int recommend,
			@ParamName("notice-abstract-content") String abstractContent,
			int priority, ServiceContext ctx) throws ServiceException {

		NoticeType noticeType = NoticeType.DEFAULT;
		for (NoticeType nt : NoticeType.values()) {
			if (type.equals(nt.getStrValueByLongValue(nt.longValue()))) {
				noticeType = nt;
				break;
			}
		}
		CmNoticeDao NoticeManagementDao = super.getDao(CmNoticeDao.class);
		return NoticeManagementDao.createNotice(noticeType.longValue(), title,
				key, content, (long) status, (long) recommend, abstractContent,
				ctx.getToken().get("loginName"));
	}

	/**
	 * 更新公告
	 * 
	 * @param type
	 *            类型。1，还款公告；2，平台公告；3，媒体报道；4，平台动态；5，互联网金融；6，工程金融；7，政策法规；8，投资策略；
	 *            9，工程贷；10，债权转让；11班汇宝；12，班汇通高手；13社会责任；14，投资人说。
	 * @param title
	 *            公告标题
	 * @param key
	 *            关键字
	 * @param content
	 *            公告内容
	 * @param status
	 *            公告状态。0，内部审核。1，正式发布。
	 * @param recommend
	 *            是否推荐。0，不推荐。1，推荐。
	 * @param abstractContent
	 *            内容摘要
	 * @param priority
	 *            优先级，用于设定上下移动
	 * @param updater
	 *            更新者
	 * @return
	 * @throws ServiceException
	 */
	public long updateNotice(int cnId, int type, String title, String key,
			String content, int status, int recommend, String abstractContent,
			int priority, String updater) throws ServiceException {
		CmNoticeDao NoticeManagementDao = super.getDao(CmNoticeDao.class);
		return NoticeManagementDao
				.updateNotice((long) cnId, (long) type, title, key, content,
						(long) recommend, abstractContent, updater);
	}

	/**
	 * 分页展示公告列表信息
	 * 
	 * @param startTime
	 *            创建时间
	 * @param endTime
	 *            创建时间
	 * @param type
	 *            类型。1，还款公告；2，平台公告；3，媒体报道；4，平台动态；5，互联网金融；6，工程金融；7，政策法规；8，投资策略；
	 *            9，工程贷；10，债权转让；11班汇宝；12，班汇通高手；13社会责任；14，投资人说。
	 * @param status
	 *            0，内部审核。1，正式发布。
	 * @param searchKey
	 *            公告内容
	 * @param page
	 * @param pageCount
	 * @return
	 * @throws ServiceException
	 */
	public List<NoticeInfo> queryNoticeInfoList(Date startTime, Date endTime,
			int type, int status, String searchKey, int page, int pageSize)
			throws ServiceException {
		CmNoticeDao NoticeManagementDao = super.getDao(CmNoticeDao.class);
		return NoticeManagementDao.queryNoticeInfo(startTime, endTime,
				(long) type, (long) status, searchKey, (page - 1) * pageSize,
				pageSize);
	}

	/**
	 * @Title: queryNoticeInfoDetail
	 * @Description: 获取某条公共信息的详细内容
	 * @param cnId
	 * 
	 * @param @throws ServiceException
	 * @return NoticeInfoDetail 返回类型
	 * @throws
	 */
	public NoticeInfoDetail queryNoticeInfoDetail(
			@ParamName("id") final long cnId, ServiceContext ctx)
			throws ServiceException {
		CmNoticeDao NoticeManagementDao = super.getDao(CmNoticeDao.class);
		return NoticeManagementDao.queryNoticeInfoDetail(cnId);
	}

	/**
	 * @Title: deleteNoticeInfo
	 * @Description: 删除公告
	 * @param cnId
	 * 
	 * @param @throws ServiceException
	 * @return long 返回类型
	 * @throws
	 */
	public long deleteNoticeInfoById(@ParamName("id") final long cnId,
			ServiceContext ctx) throws ServiceException {
		CmNoticeDao NoticeManagementDao = super.getDao(CmNoticeDao.class);
		return NoticeManagementDao.deleteNoticeInfo(cnId);
	}

	/**
	 * 查询最新count条不同类型公告 type值传null时，不作类型限定（用于查询最新count条公告）
	 * 
	 * @throws SQLException
	 */
	public Collection<Map<String, Object>> getTopInfoList(
			@ParamName("type") @ParamDefaultValue("0") int type,
			final ServiceContext ctx) throws ServiceException {
		CmNoticeDao dao = super.getDao(CmNoticeDao.class);
		List<NoticeInfo> noticeInfoList = null;
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		if (type == 0) {
			noticeInfoList = dao.queryNoticeInfo(null, null, null, null, null,
					0, PORTAL_TOP_INFO_COUNT);
		} else if (type == 3) {
			noticeInfoList = dao.queryNoticeInfo(null, null, (long) type, null,
					null, 0, PORTAL_BOTTOM_MEDIA_INFO_COUNT);
		} else if (type == 8) {
			noticeInfoList = dao.queryStratageInfo(0, PORTAL_BOTTOM_INFO_COUNT);
		} else {
			noticeInfoList = dao.queryNoticeInfo(null, null, (long) type, null,
					null, 0, PORTAL_BOTTOM_INFO_COUNT);
		}

		return noticeInfoList.parallelStream().map(ni -> {
			Map<String, Object> item = Converter.toMap(ni);
			item.put("hash", hashNoticeInfo(ni.getCnId(), ni.getTitle()));
			List<FileObject> fList = vfs.load(ni.getCnId(), 9, 0, null);
			if (fList != null && fList.size() > 0) {
				item.put("fileId", fList.get(0).getId());
				item.put("filedHash", uniqueHash(fList.get(0)));
			}
			return item;
		}).collect(Collectors.toList());
	}

	/**
	 * @Title: getInfoList
	 * @Description: 分页展示公告列表信息
	 * @param type
	 *            类型。1，还款公告；2，平台公告；3，媒体报道；4，平台动态；5，互联网金融；6，工程金融；7，政策法规；8，投资策略；
	 *            9，工程贷；10，债权转让；11班汇宝；12，班汇通高手；13社会责任；14，投资人说。
	 * @param pn
	 * 
	 * @param @throws ServiceException
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> getInfoList(
			@ParamName("type") @ParamDefaultValue("") long type,
			@ParamName("isMainPage") @ParamDefaultValue("N") final Boolean isMainPage,
			@ParamName("pn") @ParamDefaultValue("1") int pn)
			throws ServiceException {
		final CmNoticeDao dao = super.getDao(CmNoticeDao.class);
		Map<String, Object> result = new HashMap<String, Object>();
		final Long count = dao.queryNoticeInfoCount(null, null,type == 0 ? null : type,
				isMainPage ? 2L:null, null);
		final List<NoticeInfo> noticeInfoList = dao.queryNoticeInfo(null, null,
				type == 0 ? null : type,isMainPage ? 2L:null, null, (pn - 1) * INFO_PAGESIZE,
				INFO_PAGESIZE);

		final Collection<Map<String, Object>> items = noticeInfoList
				.parallelStream()
				.map(ni -> {
					Map<String, Object> item = Converter.toMap(ni);
					item.put("hash",
							hashNoticeInfo(ni.getCnId(), ni.getTitle()));
					return item;
				}).collect(Collectors.toList());

		result.put("items", items);
		result.put("pc", (int) Math.ceil((double) count / INFO_PAGESIZE));
		return result;
	}

	public List<NoticeInfo> getInfoListForBg(
			@ParamName("start-date") Date startDate,
			@ParamName("end-date") Date endDate, @ParamName("type") long type,
			@ParamName("search-key") String searchKey) throws ServiceException {
		final CmNoticeDao dao = super.getDao(CmNoticeDao.class);
		return dao.queryNoticeInfo(startDate, endDate, type == 0 ? null : type,
				null, searchKey, 0, Integer.MAX_VALUE);

	}

	/**
	 * 获取公司简介等只有一条数据的类型公告记录
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
	public NoticeInfoDetail getInfoByIdAndType(
			@ParamName("type") final long type, @ParamName("id") final long id) {
		return super.getDao(CmNoticeDao.class).queryNoticeInfoDetailByType(id,
				type);
	}

	/***
	 * 退回重新修改
	 * 
	 * @param cnId
	 * @param ctx
	 * @return
	 */
	public long returnCmNoticeById(@ParamName("id") final long cnId,
			ServiceContext ctx) {

		return super.getDao(CmNoticeDao.class).returnNotice(cnId);

	}

	/**
	 * 从发布状态永久的撤下
	 * 
	 * @param cnId
	 * @param ctx
	 * @return
	 */
	public long revokeCmNoticeById(@ParamName("id") final long cnId,
			ServiceContext ctx) {

		return super.getDao(CmNoticeDao.class).revokeNotice(cnId,
				ctx.getToken().get("loginName"));

	}

	/**
	 * 提交审核
	 * 
	 * @param cnId
	 * @param ctx
	 * @return
	 */
	public long submitCmNoticeById(@ParamName("id") final long cnId,
			ServiceContext ctx) {
		return super.getDao(CmNoticeDao.class).submitNotice(cnId,
				ctx.getToken().get("loginName"));

	}

	/**    
	 * 发布公告到前台
	 * 
	 * @param cnId
	 * @param ctx
	 * @return
	 */
	public long publishCmNoticeById(@ParamName("id") final long cnId,
			ServiceContext ctx) {
		return super.getDao(CmNoticeDao.class).publishNotice(cnId,
				ctx.getToken().get("loginName"));

	}

	/**
	 * @Title: getInfo
	 * @Description: 获取某条公共信息的详细内容
	 * @param id
	 * @param hash
	 * 
	 * @param @throws ServiceException
	 * @return NoticeInfoDetail 返回类型
	 * @throws
	 */
	public NoticeInfoDetail getInfoByIdAndHash(@ParamName("id") int id,
			@ParamName("hash") final String hash) throws ServiceException {
		CmNoticeDao dao = super.getDao(CmNoticeDao.class);
		NoticeInfoDetail info = dao.queryNoticeInfoDetail(Long.valueOf(id));
		if (null == info) {
			return null;
		}

		if (StringUtils.equals(hashNoticeInfo(info.getCnId(), info.getTitle()),
				hash)) {
			return info;
		} else {
			return null;
		}
	}

	private static String uniqueHash(final FileObject fileObj) {
		if (fileObj == null) {
			return "";
		}
		return MiscUtils.uniqueHash(new Object[] { fileObj.getId(),
				fileObj.getName() });
	}

	// hash(cnId, title)
	private static String hashNoticeInfo(final Object... objects) {
		return MiscUtils.uniqueHash(objects);
	}
}
