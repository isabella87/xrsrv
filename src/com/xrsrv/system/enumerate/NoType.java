/**
 * 
 */
package com.xrsrv.system.enumerate;

import org.json.ValueEnum;

/**
 * @author Haart
 *
 */
public enum NoType implements ValueEnum {
	 PRODUCT_NO(1),ORDER_NO(0), SERVICE_NO(2);

	private int value;

	NoType(final int value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.json.ValueEnum#value()
	 */
	@Override
	public final int value() {
		return this.value;
	}

	/* (non-Javadoc)
	 * @see org.json.ValueEnum#longValue()
	 */
	public final long longValue() {
		return value();
	}
}
