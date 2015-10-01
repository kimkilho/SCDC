package kr.ac.snu.imlab.scdc.util;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;

/**
 * Created by kilho on 15. 10. 1.
 */
public class HttpUtil {

  /**
   * HTTP GET request
   * @reference http://hayageek.com/android-http-post-get/
   * @reference http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
   */
  public static String sendGet(String destUrl) throws Exception {
    String responseBody = null;
    HttpClient client = new DefaultHttpClient();
    HttpGet request = new HttpGet(destUrl);
    HttpResponse response;

    try {
      response = client.execute(request);
      responseBody = EntityUtils.toString(response.getEntity());
    } catch (ClientProtocolException e) {
      Log.e(LogKeys.DEBUG, "HttpUtil.sendGet(): error: ", e);
      responseBody = null;
    } catch (IOException e) {
      Log.e(LogKeys.DEBUG, "HttpUtil.sendGet(): error: ", e);
      responseBody = null;
    } finally {
      client.getConnectionManager().shutdown();
    }

    return responseBody;


    /*
    URL url = new URL(destUrl);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    InputStream is = null;
    int len = 500;

    // add request header
    // conn.setRequestProperty("User-Agent", "Mozilla/5.0");
    conn.setRequestMethod("GET");
    conn.setReadTimeout(10000);
    conn.setConnectTimeout(15000);
    conn.setDoInput(true);
    // starts the query
    conn.connect();

    int response = conn.getResponseCode();
    Log.d(LogKeys.DEBUG, "SharedPrefsHandler.sendGet(): response=" + response);
    is = conn.getInputStream();
    String content = readIt(is, len);
    Log.d(LogKeys.DEBUG, "SharedPrefsHandler.sendGet(): content=" + content);

    if (is != null) {
      is.close();
    }

    return content;
    */
  }

  /**
   * HTTP POST request
   * @reference http://hayageek.com/android-http-post-get/
   */
  public static String sendPost(String destUrl, List<NameValuePair> nameValuePairs) throws Exception {
    String responseBody = null;
    HttpClient client = new DefaultHttpClient();
    HttpPost request = new HttpPost(destUrl);

    // encoding POST data
    try {
      request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    } catch (UnsupportedEncodingException e) {
      // log exception
      Log.e(LogKeys.DEBUG, "HttpUtil.sendPost(): error: ", e);
    }

    // make POST reqeust
    try {
      HttpResponse response = client.execute(request);
      responseBody = EntityUtils.toString(response.getEntity());
    } catch (ClientProtocolException e) {
      Log.e(LogKeys.DEBUG, "HttpUtil.sendPost(): error: ", e);
      responseBody = null;
    } catch (IOException e) {
      Log.e(LogKeys.DEBUG, "HttpUtil.sendPost(): error: ", e);
      responseBody = null;
    }

    return responseBody;
  }

  /*
  /**
   * Reads an InputStream and converts it to a String
   * @reference http://developer.android.com/training/basics/
   *                    network-ops/connecting.html#download
  private String readIt(InputStream stream, int len) throws IOException,
          UnsupportedEncodingException {
    Reader reader = null;
    reader = new InputStreamReader(stream, "UTF-8");
    char[] buffer = new char[len];
    reader.read(buffer);
    return new String(buffer);
  }
  */
}
