package com.xrsrv.products;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xx.armory.bindings.ParamName;
import org.xx.armory.config.ConfigurationProviders;
import org.xx.armory.services.AggregatedService;
import org.xx.armory.services.ServiceContext;
import org.xx.armory.services.ServiceException;
import org.xx.armory.vfs.FileObject;

import com.xinran.xrsrv.persistence.FileInfo;
import com.xinran.xrsrv.persistence.MajorProductDao;
import com.xinran.xrsrv.persistence.ProductInfo;
import com.xinran.xrsrv.persistence.ProductInfoAndFileId;
import com.xrsrv.file.DefaultFileServiceImpl;
import com.xrsrv.file.FileService;

public class ProductFileService extends AggregatedService {
	private Log logger = LogFactory.getLog(ProductFileService.class);

	private static class ImageUrls {
		/**
		 * 商品图片
		 */
		static final String PRO_IMG_URL = getXinRanConfiguration("pro-image-url");
	}

	private static String getXinRanConfiguration(final String key) {
		return ConfigurationProviders.getInstance().getSection("xinran").getOrDefault(key, "")
				.trim();
	}

	/**
	 * path yyyymmdd+三位顺序数字 + 文件类型 + 项目id   eg.  20160803002_1_25.png
	 * 
	 * @param ctx
	 * @return
	 * @throws IOException
	 */
	public boolean writeProPicIntoDatabase(final ServiceContext ctx) throws IOException {
		final FileService fileService = getService(DefaultFileServiceImpl.class);
		String path = ImageUrls.PRO_IMG_URL + DateUtil.formatDate(new Date(), "yyyyMMdd");
		File dir = new File(path);
		File[] fs = dir.listFiles();
		int uploadResult = 0;
		for (int i = 0; i < fs.length; i++) {
			System.out.println(fs[i].getAbsolutePath());
			if (fs[i].isFile()) {
				String filePath = fs[i].getAbsolutePath();
				final BufferedImage img = ImageIO.read(new FileInputStream(filePath));
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					ImageIO.write(img, "PNG", os);
				} finally {
					IOUtils.closeQuietly(os);
				}
				String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
				String fileShortName = fileName.split("\\.")[0];
				long pId = NumberUtils.toLong(fileShortName.split("_")[2]);
				FileType fileType = null;
				for(FileType ft:FileType.values()){
					if(ft.longValue() == NumberUtils.toLong(fileShortName.split("_")[1])){
						fileType = ft;
					}
				}
				long fId = fileService.create(pId, fileType.longValue(), fileName, "",
						true, ctx);
				fileService.upload(fId, os.toByteArray(), ctx);
				uploadResult++;
			}
		}
		logger.debug("上传文件数：" + uploadResult + "个！");

		return true;
	}

	public boolean create(@ParamName("file-name") final String fileName,
			@ParamName("mp-id") final long mpId, final ServiceContext ctx) throws IOException {

		ProductInfoAndFileId productInfo = super.getDao(MajorProductDao.class).queryProductInfo(mpId);
		if (productInfo == null) {
			throw new ServiceException("商品不存在");
		}
		ProductType productType = null;
		for (ProductType pt : ProductType.values()) {
			if (pt.strValue().equals(productInfo.getType())) {
				productType = pt;
				break;
			}
		}
		final FileService fileService = getService(DefaultFileServiceImpl.class);
		String path = ImageUrls.PRO_IMG_URL + DateUtil.formatDate(new Date(), "yyyyMMdd");
		String filePath = path + File.separator + fileName;
		final BufferedImage img = ImageIO.read(new FileInputStream(filePath));
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "PNG", os);
		} finally {
			IOUtils.closeQuietly(os);
		}
		long fId = fileService.create(productInfo.getMpId(), productType.longValue(), fileName, "",
				true, ctx);
		int uploadResult = fileService.upload(fId, os.toByteArray(), ctx);
		logger.debug("上传" + productInfo.getMpId() + "商品的文件：" + uploadResult + "个！");

		return true;
	}

	/**
	 * 查询产品文件,用于产品详细页显示的图片
	 * 
	 * @param mpId
	 * @param type
	 * @param ctx
	 * @return
	 * @throws IOException
	 */
	public FileObject queryProPic(@ParamName("id") long mpId, @ParamName("type") String typeStr,
			@ParamName("parent-name") String parentName, final ServiceContext ctx)
			throws IOException {
		final FileService fileService = getService(DefaultFileServiceImpl.class);
		FileInfo fileInfo = super.getDao(MajorProductDao.class).queryFileInfoWithMaxId(mpId);
		return fileService.get(fileInfo.getFileId(), null, ctx);
	}

	/*public boolean writeProFileIntoDatabase(final ServiceContext ctx) throws FileNotFoundException {
		final FileService fileService = getService(DefaultFileServiceImpl.class);
		String path = "E:" + File.separator + "image" + File.separator + "20160726001_1_3.txt";
		File file = new File(path);
		final byte[] fBytes = getBytesFromFile(file);
		String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
		String fileShortName = fileName.split("\\.")[0];
		long pId = NumberUtils.toLong(fileShortName.split("_")[2]);
		long fId = fileService.create(pId, FileType.PRO_MOUSE.longValue(), fileName, "", true, ctx);
		int uploadResult = fileService.upload(fId, fBytes, ctx);
		return true;
	}
*/
	private byte[] getBytesFromFile(File dealedFile) {

		InputStream is = null;
		byte[] bytes = null;
		try {
			is = new FileInputStream(dealedFile);
			bytes = new byte[is.available()];
			is.read(bytes);
		} catch (Exception e) {
			logger.error("Get File Object Failed!", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					//
				}
			}
			// dealedFile.delete();
		}
		return bytes;
	}

	public static void main(String[] args) {
		String path = "20160726001_1_3.txt";
		System.out.println(path);
		System.out.println(path.replace("!", "\\."));
		String[] fileShortName = path.split("\\.");
		// long pId = NumberUtils.toLong(fileShortName.split("_")[2]);
		// file:/D:/workspace/banhui_system_2.0.0/xrsrv/bin/com/ ../../
		// file:/D:/workspace/banhui_system_2.0.0/xrsrv/bin/ /
		String pathRoot = ProductFileService.class.getResource("../").toString();
		System.out.println("pathRoot:    " + pathRoot);
		System.out.println(fileShortName);
		// System.out.println(pId);
	}
}
