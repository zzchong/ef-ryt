package com.efeiyi.ec.art.base.service;

import java.util.concurrent.ExecutorService;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
public interface ThreadManager {
    void startWork(ExecutorService pool) throws Exception;
}
