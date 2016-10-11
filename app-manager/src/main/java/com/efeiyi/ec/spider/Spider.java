package com.efeiyi.ec.spider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/6/22.
 */
public class Spider {

    private  static HttpClient httpClient = new HttpClient();

    public static boolean downLoadPage(String url) throws Exception{

        InputStream inputStream  = null;
        OutputStream outputStream = null;
        GetMethod getMethod = new GetMethod(url);
        int statusCode = httpClient.executeMethod(getMethod);
        if(statusCode== HttpStatus.SC_OK){
            inputStream = getMethod.getResponseBodyAsStream();
            String filename = url.substring(url.lastIndexOf('/')+1)+".html";
            outputStream = new FileOutputStream(filename);
            int temp = -1;
            while ((temp=inputStream.read(new byte[2000]))>0){
                outputStream.write(temp);
            }
            if(inputStream!=null){
                inputStream.close();
            }
            if(outputStream!=null){
                outputStream.close();
            }
            return  true;
        }
        return false;
    }

     public static void main(String[] args) {
        try {
            // 抓取百度首页，输出
            boolean v = Spider.downLoadPage("http://www.baidu.com");
            System.out.println(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
