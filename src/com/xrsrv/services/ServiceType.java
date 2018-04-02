/**
 * 
 */
package com.xrsrv.services;


/**
 * 
 * @author Administrator
 *
 */
public enum ServiceType {
	DESKTOP(1,"台式机维修"), 	LAPTOP(2,"笔记本维修"),AIR_CONDITION(3,"空调维修"),TV(4,"电视机维修"), USER_QA(5,"答用户问");

	private long value;
	private String strValue;

	ServiceType(final long value,final String strValue) {
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
		for(ServiceType pt:ServiceType.values()){
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
