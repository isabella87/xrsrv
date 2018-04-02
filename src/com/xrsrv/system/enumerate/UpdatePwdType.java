package com.xrsrv.system.enumerate;

import org.json.ValueEnum;

public enum UpdatePwdType implements ValueEnum {
	AUTO(1), MANUAL(9);
    private int value;
    
	private UpdatePwdType(int value) {
		this.value = value;
	}

	@Override
	public long longValue() {
		return this.value;
	}

	@Override
	public int value() {
		return this.value;
	}

}
