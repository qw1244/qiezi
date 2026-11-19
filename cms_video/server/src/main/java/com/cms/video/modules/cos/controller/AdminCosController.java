package com.cms.video.modules.cos.controller;

import com.cms.video.common.ApiRes;
import com.cms.video.common.AuthCtx;
import com.cms.video.modules.cos.dto.CosCfgSaveReq;
import com.cms.video.modules.cos.entity.CosCfg;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.oplog.service.OpLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cos")
@RequiredArgsConstructor
public class AdminCosController {

    private final CosService cosService;
    private final OpLogService opLogService;

    @GetMapping("/cfg")
    public ApiRes<CosCfg> cfg() {
        CosCfg cfg = cosService.getCfg();
        CosCfg safe = new CosCfg();
        safe.setId(cfg.getId());
        safe.setSecretId(mask(cfg.getSecretId()));
        safe.setSecretKey(StringUtilsEmpty(cfg.getSecretKey()) ? "" : "********");
        safe.setRegion(cfg.getRegion());
        safe.setBucket(cfg.getBucket());
        safe.setCdnHost(cfg.getCdnHost());
        safe.setPathPrefix(cfg.getPathPrefix());
        safe.setEnabled(cfg.getEnabled());
        safe.setUpdatedAt(cfg.getUpdatedAt());
        return ApiRes.ok(safe);
    }

    @PostMapping("/cfg")
    public ApiRes<Void> save(@RequestBody CosCfgSaveReq req, HttpServletRequest request) {
        cosService.saveCfg(req);
        opLogService.record(AuthCtx.requireId(), "cos.save", "更新R2配置", request.getRemoteAddr());
        return ApiRes.ok();
    }

    @PostMapping("/cred")
    public ApiRes<CosCredVo> cred(@RequestBody Map<String, Object> body) {
        String fileName = String.valueOf(body.get("fileName"));
        String contentType = body.get("contentType") == null ? null : String.valueOf(body.get("contentType"));
        if (body.get("seriesId") != null && !String.valueOf(body.get("seriesId")).isBlank()) {
            Long seriesId = Long.valueOf(String.valueOf(body.get("seriesId")));
            return ApiRes.ok(cosService.createPutCred(seriesId, fileName));
        }
        String folder = body.get("folder") == null ? "cover" : String.valueOf(body.get("folder"));
        return ApiRes.ok(cosService.createPutCred(folder, fileName, contentType));
    }

    @PostMapping("/cred/batch")
    public ApiRes<List<CosCredVo>> credBatch(@RequestBody Map<String, Object> body) {
        Long seriesId = Long.valueOf(String.valueOf(body.get("seriesId")));
        @SuppressWarnings("unchecked")
        List<String> fileNames = (List<String>) body.get("fileNames");
        return ApiRes.ok(cosService.createPutCredBatch(seriesId, fileNames));
    }

    @GetMapping("/objects")
    public ApiRes<Map<String, Object>> objects(@RequestParam(required = false) String prefix,
                                               @RequestParam(required = false) String marker,
                                               @RequestParam(defaultValue = "50") int limit) {
        return ApiRes.ok(cosService.listPage(prefix, marker, limit));
    }

    @PostMapping("/objects/delete")
    public ApiRes<Void> delete(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String key = body.get("key");
        cosService.deleteObject(key);
        opLogService.record(AuthCtx.requireId(), "cos.delete", key, request.getRemoteAddr());
        return ApiRes.ok();
    }

    private String mask(String v) {
        if (!StringUtilsEmpty(v) && v.length() > 8) {
            return v.substring(0, 4) + "****" + v.substring(v.length() - 4);
        }
        return v == null ? "" : v;
    }

    private boolean StringUtilsEmpty(String v) {
        return v == null || v.isEmpty();
    }
}
