package com.efeiyi.ec.art.base.service;

import com.efeiyi.ec.art.model.Dictionary;

import java.util.List;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
public interface DictionaryManager {
    List<Dictionary> getDictionaryByType(Integer type);
}
