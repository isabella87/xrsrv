/**
 * 
 */
package com.xrsrv.products;


/**
 * 
 * @author Administrator
 *
 */
public enum ProductType {
	MOUSE(1,"鼠标"), KEYBOARD(2,"键盘"),DISPLAY(3,"显示器");

	private long value;
	private String strValue;

	ProductType(final long value,final String strValue) {
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
		for(ProductType pt:ProductType.values()){
			if(pt.longValue()==value){
				return pt.strValue();
			}
		}
		return "";
	}

	public final Long getLongValueByStrValue(String strValue) {
		for(ProductType pt:ProductType.values()){
			if(pt.strValue==strValue){
				return pt.value;
			}
		}
		return 0L;
	}
	
	public final long longValue(){
		return this.value;
	}
}
