package com.efeiyi.ec.art.organization.model;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-5-24
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */

public class YunPianSmsProvider implements SmsProvider {

    /**
     * 服务http地址
     */
    private static String BASE_URI = "http://yunpian.com";
    /**
     * 服务版本号
     */
    private static String VERSION = "v1";
    /**
     * 编码格式
     */
    private static String ENCODING = "UTF-8";
    /**
     * 查账户信息的http地址
     */
    public static String URI_GET_USER_INFO = BASE_URI + "/" + VERSION + "/user/get.json";
    /**
     * 通用发送接口的http地址
     */
    public static String URI_SEND_SMS = BASE_URI + "/" + VERSION + "/sms/send.json";
    /**
     * 模板发送接口的http地址
     */
    public static String URI_TPL_SEND_SMS = BASE_URI + "/" + VERSION + "/sms/tpl_send.json";

    public final static String apikey = "b802cb40c7a0db20e787884bf29f1e6d";

    /**
     * 基于HttpClient 3.1的通用POST方法
     *
     * @param url       提交的URL
     * @param paramsMap 提交<参数，值>Map
     * @return 提交响应
     */
    @Override
    public String post(String url, Map<String, String> paramsMap) {
        HttpClient client = new HttpClient();
        try {
            PostMethod method = new PostMethod(url);
            if (paramsMap != null) {
                NameValuePair[] namePairs = new NameValuePair[paramsMap.size()];
                int i = 0;
                for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                    NameValuePair pair = new NameValuePair(param.getKey(), param.getValue());
                    namePairs[i++] = pair;
                }
                method.setRequestBody(namePairs);
                HttpMethodParams param = method.getParams();
                param.setContentCharset(ENCODING);
            }
            client.executeMethod(method);
            return method.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 发送, 并返回结果
     *
     * @param phone
     * @param content
     * @return
     */
    @Override
    public SendCode post(String phone, String content, String tpl_id) {

        try {
            content = URLEncoder.encode("#code#=" + content , ENCODING);//+ "&#company#=e飞蚁", ENCODING)
        } catch (Exception e) {
            e.printStackTrace();
        }
        String postData = "apikey=" + apikey + "&mobile=" + phone + "&tpl_id=" + tpl_id + "&tpl_value=" + content + "";

        String data = null;
        try {
            URL dataUrl = new URL(URI_TPL_SEND_SMS);
            HttpURLConnection con = (HttpURLConnection) dataUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Proxy-Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(), ENCODING);
            out.write(postData);
            out.flush();
            out.close();
            InputStream is = con.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            byte[] d = new byte[dis.available()];
            dis.read(d);
            data = new String(d, ENCODING);
            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conversion(data);
    }

    /**
     * 返回结果
     * 通过JSON解析
     *
     * @param
     * @return
     */
    public SendCode conversion(String data) {
        SendCode postResult = JSON.parseObject(data, SendCode.class);
        return postResult;
    }

    /**
     * 查询余额
     *
     * @return
     */
    @Override
    public String checkAmount() throws IOException {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(URI_GET_USER_INFO + "?apikey=" + apikey);
        HttpMethodParams param = method.getParams();
        param.setContentCharset(ENCODING);
        client.executeMethod(method);
        String jsonBody = method.getResponseBodyAsString();
        return jsonBody;
    }

}