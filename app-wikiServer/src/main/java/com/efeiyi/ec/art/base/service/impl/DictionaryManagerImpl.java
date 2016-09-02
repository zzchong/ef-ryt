package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.DictionaryManager;
import com.efeiyi.ec.art.model.Dictionary;
import com.ming800.core.base.dao.XdoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
@Service
public class DictionaryManagerImpl implements DictionaryManager {

    @Autowired
    private XdoDao xdoDao;

    @Override
    public List<Dictionary> getDictionaryByType(Integer type) {
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<String, Object>();
        queryParamMap.put("type", type);
        String hql = "select s from com.efeiyi.ec.art.model.Dictionary s where s.type = :type";
        return xdoDao.getObjectList(hql, queryParamMap);
    }

}
