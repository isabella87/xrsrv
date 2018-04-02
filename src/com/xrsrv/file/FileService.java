package com.xrsrv.file;

import java.util.Collection;

import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;
import org.xx.armory.vfs.FileObject;

/**
 * 文件服务，提供在虚拟文件系统中对文件对象的常用操作，包括查询、创建、上传、下载和删除等。
 * 
 * @author Haart
 *
 */
public interface FileService {
	/**
	 * 获取符合条件的文件对象列表。
	 * 
	 * @param objectId
	 *            关联的对象的ID。
	 * @param fileType
	 *            文件对象的类型。
	 * @param parentName
	 *            上级文件对象的名称。
	 * @param ctx
	 *            服务上下文。
	 * @return
	 *         符合条件的文件对象列表。
	 */
	Collection<File> list(final long objectId, final long fileType, final String parentName,
			final ServiceContext ctx);

	/**
	 * 获取具有指定ID的文件对象。
	 * 
	 * <p>
	 * 此方法需要检测文件对象的hash值，如果与参数不符则返回<code>null</code>。
	 * </p>
	 * 
	 * @param fileId
	 *            文件对象ID。
	 * @param hash
	 *            文件对象的hash值。
	 * @param ctx
	 *            服务上下文。
	 * @return
	 *         具有指定ID的文件对象。如果具有指定ID的文件对象不存在则返回<code>null</code>。如果该对象的hash值与参数<code>hash</code>
	 *         不同，则返回<code>null</code>。
	 */
	FileObject get(final long fileId, final String hash, final ServiceContext ctx);

	/**
	 * 创建新的文件对象，并返回成功创建的文件对象的ID。
	 * 
	 * @param objectId
	 *            关联的对象的ID。
	 * @param fileType
	 *            文件类型。
	 * @param fileName
	 *            文件对象的名称。
	 * @param parentName
	 *            上级文件对象的名称。
	 * @param force
	 *            是否覆盖已存在的文件对象。
	 * @param ctx
	 *            服务上下文。
	 * @return
	 *         创建的文件对象的ID，如果创建失败则返回<code>0</code>。
	 */
	long create(final long objectId, final long fileType, final String fileName,
			final String parentName, final boolean force, final ServiceContext ctx);

	/**
	 * 上传文件对象的内容。
	 * 
	 * @param fileId
	 *            文件对象的ID。
	 * @param content
	 *            文件对象的内容。如果此参数的值是<code>null</code>，那么不写入任何内容。
	 * @param ctx
	 *            服务上下文。
	 * 
	 * @return
	 *         成功上传的文件对象数量。
	 */
	int upload(final long fileId, final byte[] content, final ServiceContext ctx);

	/**
	 * 删除文件对象。
	 * 
	 * @param fileId
	 *            文件对象的ID。
	 * @param ctx
	 *            服务上下文。
	 * 
	 * @return
	 *         成功删除的文件对象数量。
	 */
	int delete(final long fileId, final ServiceContext ctx);

	/**
	 * 文件对象改名。
	 * 
	 * @param fileId
	 *            文件对象的ID。
	 * @param fileName
	 *            该文件对象的新名称，如果和同一个目录下的其它文件对象同名则会抛出异常。
	 * @param ctx
	 *            服务上下文。
	 * 
	 * @return
	 *         成功改名的文件对象数量。
	 * 
	 * @throws NullPointerException
	 *             如果参数<code>fileName</code>是<code>null</code>。
	 * @throws ServiceException
	 *             如果具有指定ID的文件对象不存在、或者参数<code>fileName</code>只包含空白字符，或者同一个目录下的其它文件对象已有同样的名称。
	 */
	int rename(final long fileId, final String fileName, final ServiceContext ctx);
}
