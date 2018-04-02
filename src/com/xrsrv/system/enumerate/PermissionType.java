package com.xrsrv.system.enumerate;

import org.json.ValueEnum;

public enum PermissionType implements ValueEnum{
	UNPERMIT(0,"不允许"), 
	PERMIT(1,"允许");
	
	private int value;
	private String desc;
	
	PermissionType(final int value, final String desc){
		this.value = value;
		this.desc = desc;
	}
	
	@Override
	public long longValue() {
		return value();
	}

	@Override
	public int value() {
		return value;
	}

	public String getDesc() {
		return desc;
	}
	
}
