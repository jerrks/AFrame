package com.xo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import com.xo.Log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkUtils {

	final static String TAG = "NetworkUtils";

	final static int CONNECT_TIMEOUT = 15 * 1000;
	final static int IO_TIMEOUT = 10 * 1000;

	public static boolean isWifi(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo == null || !netInfo.isAvailable()) {
			return false;
		}

		if (ConnectivityManager.TYPE_WIFI == manager.getActiveNetworkInfo()
				.getType()) {
			return true;
		}

		return false;
	}
	
	public static String getPhoneType(Context context) {
		TelephonyManager tpm = (TelephonyManager) 
				context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneType = "";
		int type = tpm.getPhoneType();
		switch (type) {
		case TelephonyManager.PHONE_TYPE_GSM:
			phoneType = "GSM";
			break;
		case TelephonyManager.PHONE_TYPE_CDMA:
			phoneType = "CDMA";
			break;
		default:
			break;
		}
		return phoneType;
	}
	
	public static String getOperater(Context context) {
		TelephonyManager tpm = (TelephonyManager) 
				context.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = "";
		if(tpm.getSimState() == TelephonyManager.SIM_STATE_READY)
			operator = tpm.getSimOperator();
		return operator;
	}
	
	/**
     * 获取网络连接类型,MOBILE or WIFI
     *
     * @param context
     * @return
     */
    public static final String getNetType(Context context) {
        String networkType = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();// NULL

            if (networkInfo != null && networkInfo.isAvailable()) {
            	networkType = networkInfo.getTypeName(); // MOBILE/WIFI
            }
        } catch (Exception e) {
            Log.e(TAG, "getNetType:"+ e);
        }
        return networkType;
    }
	
	/**
     * 获取网络连接类型
     *
     * @param context
     * @return
     */
    public static final String getMobileNetWorkType(Context context) {
        String networkType = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();// NULL

            if (networkInfo != null && networkInfo.isAvailable()) {
                String typeName = networkInfo.getTypeName(); // MOBILE/WIFI
                if (!"MOBILE".equalsIgnoreCase(typeName)) {
                    networkType = typeName;
                } else {
                    networkType = networkInfo.getExtraInfo(); // cmwap/cmnet/wifi/uniwap/uninet
                    if (networkType == null) {
                        networkType = typeName + "#[]";
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "getNetType:"+ e);
        }
        Log.d("TAG", "networkType:"+networkType);
        return networkType;
    }


	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if (network != null) {
			return network.isAvailable();
		}
		return false;
	}

	/**
	 * 下载图片,以字节数组返囄1�7
	 */
	public static byte[] downLoadImage(String path) throws Exception {
		
		byte[] result = null;
		try {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			InputStream inStream = conn.getInputStream();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[8 * 1024];
			int len = -1;
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			result = outStream.toByteArray();
			outStream.close();
			inStream.close();
		} catch (ConnectionPoolTimeoutException e) {
			result = null;
		} catch (MalformedURLException e) {
			result = null;
		} catch (ProtocolException e) {
			result = null;
		}
		return result;
	}
	
	/**
	 * execute a HTTP GET request and get response as String. Redirection is
	 * disabled and default charset is utf-8.
	 * 
	 * @param maxLength
	 *            max length of returned string
	 * @param url
	 *            target url
	 */
	public static String executeGet(int maxLength, String url) throws Exception {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(params, IO_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
		HttpClientParams.setRedirecting(params, true);

		DefaultHttpClient client = new DefaultHttpClient(params);
		try {
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null)
					return entity2String(maxLength, entity, HTTP.UTF_8);
			} else {
				Log.d(TAG, "get error: " + status + " " + url);
			}
		} finally {
			try {
				client.getConnectionManager().shutdown();
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * execute a HTTP POST request and get response as String. Redirection is
	 * disabled and default charset is utf-8.
	 * 
	 * @param maxLength
	 *            max length of returned string
	 * @param url
	 *            target url
	 * @param params
	 */
	public static String executePost(int maxLength, String url,
			List<BasicNameValuePair> postParams) throws Exception {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(params, IO_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
		HttpClientParams.setRedirecting(params, true);

		DefaultHttpClient client = new DefaultHttpClient(params);
		try {
			HttpPost request = new HttpPost(url);
			request.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
			HttpResponse response = client.execute(request);
			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null)
					return entity2String(maxLength, entity, HTTP.UTF_8);
			} else {
				Log.d(TAG, "post error: " + status + " " + url);
			}
		} finally {
			try {
				client.getConnectionManager().shutdown();
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	private static String entity2String(int maxLength, final HttpEntity entity,
			final String defaultCharset) throws IOException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		try {
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				Log.w(TAG, "HTTP entity too large to be buffered in memory");
				return null;
			}
			int i = (int) entity.getContentLength();
			if (i < 0) {
				i = 4096;
			} else {
				// take each char as 3 byte in average
				if (maxLength > 0 && i > maxLength * 3) {
					Log.w(TAG, "entity length exceed given maxLength");
					return null;
				}
			}
			String charset = EntityUtils.getContentCharSet(entity);
			if (charset == null) {
				charset = defaultCharset;
			}
			if (charset == null) {
				charset = HTTP.UTF_8;
			}
			Reader reader = new InputStreamReader(instream, charset);
			CharArrayBuffer buffer = new CharArrayBuffer(i);
			char[] tmp = new char[1024];
			int l;
			int len = 0;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
				len += l;
				if (maxLength > 0 && len > maxLength * 3) {
					Log.w(TAG, "entity length did exceed given maxLength");
					return null;
				}
			}
			return buffer.toString();
		} finally {
			instream.close();
		}
	}

}
