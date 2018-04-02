/**
 *
 */
package com.xrsrv.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xx.armory.bindings.ParamDefaultValue;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.bindings.RawContent;
import org.xx.armory.commons.MiscUtils;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;
import org.xx.armory.vfs.FileObject;
import org.xx.armory.vfs.FileObjectAttr;
import org.xx.armory.vfs.FileSystemProvider;
import org.xx.armory.vfs.impl.NullFileObject;

/**
 * 文件服务的缺省实现。
 *
 * @author Haart
 *
 */
public final class DefaultFileServiceImpl extends AggregatedService implements FileService {
	@SuppressWarnings("unused")
	private Log logger = LogFactory.getLog(DefaultFileServiceImpl.class);

	private static final long DEFAULT_PARENT_ID = 0L;

	private static final Set<FileObjectAttr> DEFAULT_FILE_ATTRS = Collections
			.unmodifiableSet(EnumSet.noneOf(FileObjectAttr.class));
	private static final Set<FileObjectAttr> DEFAULT_DIR_ATTRS = Collections
			.unmodifiableSet(EnumSet.of(FileObjectAttr.SUB_DIRECTORY));

	/**
	 *
	 */
	public DefaultFileServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.service.FileService#list(long, long, java.lang.String, org.xx.armory.services.ServiceContext)
	 */
	@Override
	public Collection<File> list(@ParamName("objectId") final long objectId,
			@ParamName("fileType") final long fileType,
			@ParamName("parentName") @ParamDefaultValue("") final String parentName,
			final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		long parentId;
		if (parentName != null && !parentName.isEmpty()) {
			final FileObject po = vfs.load(objectId, fileType, parentName, DEFAULT_PARENT_ID);
			if (po == null) {
				return new ArrayList<File>();
			} else {
				parentId = po.getId();
			}
		} else {
			parentId = DEFAULT_PARENT_ID;
		}

		return new java.util.ArrayList<File>(CollectionUtils.collect(
				vfs.load(objectId, fileType, parentId, DEFAULT_FILE_ATTRS),
				new Transformer<FileObject, File>() {
					@Override
					public File transform(final FileObject fo) {
						return new File(fo.getId(), fo.getName(), fo.getSize(),
								fo.getDescription(), uniqueHash(fo));
					}
				}));
	}

	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.service.FileService#get(long, java.lang.String, org.xx.armory.services.ServiceContext)
	 */
	@Override
	public FileObject get(@ParamName("fileId") final long fileId,
			@ParamName("hash") final String hash, final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		final FileObject fo = vfs.load(fileId);

		if (fo == null) {
			return NullFileObject.INSTANCE;
		}

		return fo;
	}

	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.service.FileService#create(long, long, java.lang.String, org.xx.armory.services.ServiceContext)
	 */
	@Override
	public long create(@ParamName("objectId") final long objectId,
			@ParamName("fileType") final long fileType,
			@ParamName("fileName") final String fileName,
			@ParamName("parentName") @ParamDefaultValue("") final String parentName,
			@ParamName("force") @ParamDefaultValue("false") final boolean force,
			final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		final MutableLong newFileId = new MutableLong(0);
		final Date now = new Date();

		getSessionFactory().beginTransaction(false);
		try {
			long parentId;
			if (parentName != null && !parentName.isEmpty()) {
				final FileObject po = vfs.load(objectId, fileType, parentName, DEFAULT_PARENT_ID);
				if (po == null) {
					// 如果上级目录不存在则创建上级目录
					if (vfs.create(newFileId, objectId, fileType, parentName, now,
							"", "", DEFAULT_PARENT_ID, DEFAULT_DIR_ATTRS) > 0) {
						parentId = newFileId.longValue();
					} else {
						throw new ServiceException("创建目录失败！");
					}
				} else {
					parentId = po.getId();
				}
			} else {
				parentId = DEFAULT_PARENT_ID;
			}

			if (force) {
				// 删除已存在的文件。
				vfs.delete(objectId, fileType, fileName, parentId);
			}

			// 创建新文件。
			vfs.create(newFileId, objectId, fileType, fileName, now, "", "", parentId,
					DEFAULT_FILE_ATTRS);

			getSessionFactory().commitTransaction();
		} finally {
			getSessionFactory().endTransaction();
		}

		return newFileId.getValue();
	}


	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.FileService#upload(long, byte[], org.xx.armory.services.ServiceContext)
	 */
	@Override
	public int upload(@ParamName("fileId") final long fileId, @RawContent final byte[] content,
			final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		try {
			vfs.writeContent(fileId, content);
			return 1;
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.service.FileService#remove(long, org.xx.armory.services.ServiceContext)
	 */
	@Override
	public int delete(@ParamName("fileId") final long fileId, final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		return vfs.delete(fileId);
	}

	/* (non-Javadoc)
	 * @see com.banhui.midsrv.file.service.FileService#rename(long, java.lang.String, org.xx.armory.services.ServiceContext)
	 */
	@Override
	public int rename(@ParamName("fileId") final long fileId,
			@ParamName("fileName") final String fileName, final ServiceContext ctx) {
		final FileSystemProvider vfs = ctx.find(FileSystemProvider.class);
		final Date now = new Date();
		return vfs.update(fileId, fileName, now, "", "", DEFAULT_FILE_ATTRS);
	}

	/**
	 *
	 * @param fo
	 * @return
	 */
	private static String uniqueHash(final FileObject fo) {
		if (fo == null) {
			return "";
		}
		return MiscUtils.uniqueHash(new Object[] { fo.getId(), fo.getLastWriteTime() });
	}

}
