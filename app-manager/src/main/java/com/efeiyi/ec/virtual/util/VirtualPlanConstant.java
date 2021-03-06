package com.efeiyi.ec.virtual.util;

//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2015/12/10.
 * 虚拟计划常量
 */
public class VirtualPlanConstant {

    public static final String PLAN_TYPE_USER = "user";//虚拟计划对象--用户
    public static final String PLAN_TYPE_ORDER = "order";//虚拟计划对象--订单
    public static final String PLAN_TYPE_PRAISE = "praise";//虚拟计划对象--点赞
    public static final String PLAN_TYPE_PRODUCT = "product";//虚拟计划对象--商品
    public static final String PLAN_TYPE_COLLECT = "collect";//虚拟计划对象--收藏
    public static final String PLAN_TYPE_POPULARITY = "popularity";//虚拟计划对象--人气
    public static final String PLAN_TYPE_INVESTMENT = "investment";//虚拟计划对象--融资

    final public static String planStatusDeleted = "0";
    final public static String planStatusNormal = "1";
    final public static String planStatusInit = "2";
    final public static String planStatusStarted = "3";
    final public static String planStatusStopped = "5";
    final public static String planStatusFinished = "9";
    final public static String virtualUserIdentifier = "2";

//    final public static ExecutorService threadPool = Executors.newCachedThreadPool();
}
