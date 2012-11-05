package dk.codeunited.kulturarv.mgr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.mgr.downloader.DownloadRequest;

/**
 * @author mixare
 */
public final class HttpTools {

	/**
	 * Prefered To use InputStream managed!
	 * 
	 * @param request
	 * @param cr
	 * @return
	 * @throws Exception
	 */
	public static String getPageContent(DownloadRequest request,
			ContentResolver cr) throws Exception {
		String pageContent;
		InputStream is = null;
		if (!request.getSource().getUrl().startsWith("file://")) {
			is = HttpTools.getHttpGETInputStream(request.getSource().getUrl()
					+ request.getParams(), cr);
		} else {
			is = HttpTools.getHttpGETInputStream(request.getSource().getUrl(),
					cr);
		}
		pageContent = HttpTools.getHttpInputString(is);
		HttpTools.returnHttpInputStream(is);
		return pageContent;
	}

	public static String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getHttpGETInputStream(String urlStr,
			ContentResolver cr) throws Exception {
		InputStream is = null;
		URLConnection conn = null;

		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}

		if (urlStr.startsWith("file://"))
			return new FileInputStream(urlStr.replace("file://", ""));

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null, cr);

		if (urlStr.startsWith("https://")) {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
					//
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
					//
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		}

		try {
			URL url = new URL(urlStr);
			conn = url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			is = conn.getInputStream();

			return is;
		} catch (Exception ex) {
			try {
				is.close();
			} catch (Exception ignore) {
				LogBridge.error("Error on url " + urlStr, ignore);
			}
			try {
				if (conn instanceof HttpURLConnection)
					((HttpURLConnection) conn).disconnect();
			} catch (Exception ignore) {
				//
			}
			throw ex;
		}
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getHttpPOSTInputStream(String urlStr,
			String params, ContentResolver cr) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, params, cr);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			if (params != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.write(params);
				wr.close();
			}

			is = conn.getInputStream();

			return is;
		} catch (Exception ex) {

			try {
				is.close();
			} catch (Exception ignore) {
				//
			}
			try {
				os.close();
			} catch (Exception ignore) {
				//
			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
				//
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr, cr);
			}

			throw ex;
		}
	}

	/**
	 * Input Stream with unsafe close
	 */
	@Deprecated
	public static InputStream getContentInputStream(String urlStr,
			String params, ContentResolver cr) throws Exception {
		// ContentResolver cr = mixView.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result.getBytes());
		}

		cur.deactivate();

		throw new Exception("Invalid content:// mode " + mode);
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public static void returnHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public InputStream getResourceInputStream(String name, AssetManager mgr)
			throws Exception {
		// AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}

	/**
	 * Input Stream management not safe
	 */
	@Deprecated
	public static void returnResourceInputStream(InputStream is)
			throws Exception {
		if (is != null)
			is.close();
	}
}