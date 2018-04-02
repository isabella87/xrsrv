/**
 * 
 */
package com.xrsrv.system.enumerate;

import org.json.ValueEnum;

/**
 * @author Haart
 *
 */
public enum AccountType implements ValueEnum{
	PERSON(1), ORG(2);

	private int value;

	AccountType(final int value) {
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
