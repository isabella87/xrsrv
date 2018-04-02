/**
 * 
 */
package com.xrsrv.system.enumerate;

import org.json.ValueEnum;

/**
 * @author Haart
 *
 */
public enum AccountStatus implements ValueEnum {
	AUDITING(0), ORGAUDITED(1), AUDITED(2),JX_OK(3), REJECTED(-1), DISABLED(99);

	private int value;

	AccountStatus(final int value) {
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
