/**
 * 
 */
package com.xrsrv.worker;


/**
 * 
 * @author Administrator
 *
 */
public enum WorkerLevelType {
	A(1,"玉帝"), B(2,"太上老君"),C(3,"托塔天王"),D(4,"哪吒"),E(5,"虾米");

	private long value;
	private String strValue;

	WorkerLevelType(final long value,final String strValue) {
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
		for(WorkerLevelType pt:WorkerLevelType.values()){
			if(pt.longValue()==value){
				return pt.strValue();
			}
		}
		return "";
	}

	public final Long getLongValueByStrValue(String strValue) {
		for(WorkerLevelType pt:WorkerLevelType.values()){
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
