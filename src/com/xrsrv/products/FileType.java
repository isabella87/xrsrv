/**
 * 
 */
package com.xrsrv.products;


/**
 * 
 * @author Administrator
 *
 */
public enum FileType {
	PRO_MOUSE(1,"鼠标"), PRO_KEYBOARD(2,"键盘"),PRO_DISPLAY(3,"显示器");

	private long value;
	private String strValue;

	FileType(final long value,final String strValue) {
		this.value = value;
		this.strValue = strValue;
	}

	/* (non-Javadoc)
	 * @see org.json.ValueEnum#value()
	 */
	public final String strValue() {
		return this.strValue;
	}
	
	public final String getStrValueByLongValue(long value) {
		for(FileType pt:FileType.values()){
			if(pt.longValue()==value){
				return pt.strValue();
			}
		}
		return "";
	}

	public final long longValue(){
		return this.value;
	}
}
