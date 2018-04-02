package com.xrsrv.account.main;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;

import com.xrsrv.account.MidHttpService;

public class Client {

	private static Log getLog() {
		return LogFactory.getLog(Client.class);
	}

	public static void main(String[] args) throws Exception {

		long source = new Date().getTime();
		final String url = "http://localhost/xrsrv/reg/register-person";

		BasicNameValuePair[] parms = { new BasicNameValuePair("real-name", "刘红"),
				new BasicNameValuePair("id-card", "340826********8728"),
				new BasicNameValuePair("login-name", "xiaoshiliu"),
				new BasicNameValuePair("pwd", "woshimima"),
				new BasicNameValuePair("mobile", "15878787854") };
		Map<String, String> map = new MidHttpService().httpClientRequest(url, Arrays.asList(parms),
				HttpPut.METHOD_NAME);
		System.out.println("perfect");
	}
}
