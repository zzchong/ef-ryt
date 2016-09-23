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

        StringBuffer sb = new StringBuffer();
        sb.append("select s from com.efeiyi.ec.art.model.Dictionary s where 1=1 ");

        if(type != null) {
            sb.append("and s.type = :type");
            queryParamMap.put("type", type);
        } else {
            sb.append("and s.type != 2");
        }

        return xdoDao.getObjectList(sb.toString(), queryParamMap);
    }
}
