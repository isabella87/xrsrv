package com.xrsrv.account;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.xx.armory.services.AggregatedService;

public class MidHttpService extends AggregatedService {

	private Logger logger = Logger.getLogger(MidHttpService.class);

	public Map<String, String> httpClientRequest(String url, List<BasicNameValuePair> params,
			String method) {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
	    Map<String, String> map = new HashMap<String, String>();
		
		if (method == HttpGet.METHOD_NAME) {
			try {
				URIBuilder uriBuilder = new URIBuilder(url);
				if (params != null) {
					uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
				}

				HttpGet httpGet = new HttpGet(uriBuilder.build());
				httpGet.addHeader("Accept", "application/json;charset=UTF-8");

				response = httpclient.execute(httpGet);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					map.put("success", EntityUtils.toString(response.getEntity(), "UTF-8"));
				} else {
					map.put("error", EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
			} catch (IOException e) {
				map.put("error", "IO异常!");
			} catch (URISyntaxException e) {
				map.put("error", "URL语法错误!");
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						map.put("error", "IO异常!");
					}
				}
			}
			return map;
		}

		if (method == HttpPut.METHOD_NAME) {
			try {
				HttpPut httpPut = new HttpPut(url);
				httpPut.addHeader("Accept", "application/json;charset=UTF-8");
				if (params != null) {
					httpPut.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
				}

				response = httpclient.execute(httpPut);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					map.put("success", EntityUtils.toString(response.getEntity(), "UTF-8"));
				} else {
					map.put("error", EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
			} catch (ClientProtocolException e) {
				map.put("error", "客户端协议异常!");
			} catch (IOException e) {
				map.put("error", "IO异常!");
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						map.put("error", "IO异常!");
					}
				}
			}
			return map;
		}

		if (method == HttpPost.METHOD_NAME) {
			try {
				HttpPost httpPost = new HttpPost(url);
				httpPost.addHeader("Accept", "application/json;charset=UTF-8");
				if (params != null) {
					httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
				}

				response = httpclient.execute(httpPost);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					map.put("success", EntityUtils.toString(response.getEntity(), "UTF-8"));
				} else {
					map.put("error", EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
				
			} catch (ClientProtocolException e) {
				map.put("error", "客户端协议异常!");
			} catch (IOException e) {
				map.put("error", "IO异常!");
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						map.put("error", "IO异常!");
					}
				}
			}
			return map;
		}

		if (method == HttpDelete.METHOD_NAME) {
			try {
				URIBuilder uriBuilder = new URIBuilder(url);
				if (params != null) {
					if (params != null) {
						uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
					}
				}

				HttpDelete httpDelete = new HttpDelete(uriBuilder.build());
				httpDelete.addHeader("Accept", "application/json;charset=UTF-8");

				response = httpclient.execute(httpDelete);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					map.put("success", EntityUtils.toString(response.getEntity(), "UTF-8"));
				} else {
					map.put("error", EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
				
			} catch (URISyntaxException e) {
				map.put("error", "URL语法错误!");
			} catch (ClientProtocolException e) {
				map.put("error", "客户端协议异常!");
			} catch (IOException e) {
				map.put("error", "IO异常!");
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						map.put("error", "IO异常!");
					}
				}
			}
			return map;
		}
		return map;
	}
}
