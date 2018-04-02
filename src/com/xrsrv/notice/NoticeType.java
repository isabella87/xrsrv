/**
 * 
 */
package com.xrsrv.notice;


/**
 * 
 * @author Administrator
 *
 */
public enum NoticeType {
	DEFAULT(0,"未知"),TYPE1(1,"维修资讯"), TYPE2(2,"常见故障"),TYPE3(3,"维护保养"),TYPE4(4,"电脑问题");

	private long value;
	private String strValue;

	NoticeType(final long value,final String strValue) {
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
		for(NoticeType pt:NoticeType.values()){
			if(pt.longValue()==value){
				return pt.strValue();
			}
		}
		return "";
	}

	public final Long getLongValueByStrValue(String strValue) {
		for(NoticeType pt:NoticeType.values()){
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
