package com.xrsrv.msg;

public enum MsgCode {
	REG_ACCOUNT(1), LOST_PWD(2), CHANGE_MOBILE(3), REG_THIRD_ACCOUNT(4), CHANGE_THIRD_PWD(5), BIND_THIRD_CARD(
			6), CREDIT_ASSIGN(11), PAY_PAYMENT(61), FEE_PAYMENT(62), CONFIRM_PAY_PAYMENT(63), PAY_REFUNDMENT(
			64), REG_MGR_ACCOUNT(101), MGR_RESET_PWD(104);

	private int value;

	MsgCode(final int value) {
		this.value = value;
	}

	public final int value() {
		return this.value;
	}
}
