/**
 * 
 */
package com.xrsrv.file;

/**
 * @author Haart
 *
 */
public final class File {
	private final long id;
	private final String name;
	private final long size;
	private final String description;
	private final String hash;

	/**
	 * 
	 * @param fileId
	 * @param fileName
	 * @param size
	 * @param description
	 * @param hash
	 */
	public File(final long id, final String name, final long size,
			final String description, final String hash) {
		this.id = id;
		this.name = name == null ? "" : name.trim();
		this.size = size;
		this.description = description == null ? "" : description.trim();
		this.hash = hash == null ? "" : hash.trim();
	}

	/**
	 * 
	 * @return
	 */
	public final long getId() {
		return this.id;
	}

	/**
	 * 
	 * @return
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return
	 */
	public final long getSize() {
		return this.size;
	}

	/**
	 * 
	 * @return
	 */
	public final String getDescription() {
		return this.description;
	}

	/**
	 * 
	 * @return
	 */
	public final String getHash() {
		return this.hash;
	}

}
