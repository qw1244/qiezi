package com.cms.video.modules.cos.service;

import com.cms.video.modules.cos.dto.CosCfgSaveReq;
import com.cms.video.modules.cos.entity.CosCfg;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.cos.vo.CosObjVo;

import java.util.List;
import java.util.Map;

public interface CosService {

    CosCfg getCfg();

    void saveCfg(CosCfgSaveReq req);

    CosCredVo createPutCred(Long seriesId, String fileName);

    CosCredVo createPutCred(String folder, String fileName, String contentType);

    List<CosCredVo> createPutCredBatch(Long seriesId, List<String> fileNames);

    List<CosObjVo> listObjects(String prefix, String marker, int limit);

    Map<String, Object> listPage(String prefix, String marker, int limit);

    void deleteObject(String key);

    String buildPlayUrl(String cosKey);

    boolean ready();
}
