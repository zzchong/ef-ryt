package com.efeiyi.ec.system.service;


/**
 * Created with IntelliJ IDEA.
 * User: ming
 * Date: 12-12-11
 * Time: 下午12:19
 * To change this template use File | Settings | File Templates.
 */
public interface SmsCheckManager {

    String createCheckCode();

    String send(String phone, String content, String tpl_id, Integer company);

    String send(String mobile, String tpl_id, String tpl_value);

}
