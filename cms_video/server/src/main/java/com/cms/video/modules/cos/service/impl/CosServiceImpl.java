package com.cms.video.modules.cos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.video.common.BizException;
import com.cms.video.modules.cos.dto.CosCfgSaveReq;
import com.cms.video.modules.cos.entity.CosCfg;
import com.cms.video.modules.cos.mapper.CosCfgMapper;
import com.cms.video.modules.cos.service.CosService;
import com.cms.video.modules.cos.vo.CosCredVo;
import com.cms.video.modules.cos.vo.CosObjVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosServiceImpl implements CosService {

    private final CosCfgMapper cosCfgMapper;

    @Override
    public CosCfg getCfg() {
        CosCfg cfg = cosCfgMapper.selectOne(new LambdaQueryWrapper<CosCfg>().last("limit 1"));
        if (cfg == null) {
            cfg = new CosCfg();
            cfg.setSecretId("");
            cfg.setSecretKey("");
            cfg.setRegion("");
            cfg.setBucket("");
            cfg.setCdnHost("");
            cfg.setPathPrefix("video/");
            cfg.setEnabled(0);
            cosCfgMapper.insert(cfg);
        }
        return cfg;
    }

    @Override
    public void saveCfg(CosCfgSaveReq req) {
        CosCfg cfg = getCfg();
        if (StringUtils.hasText(req.getSecretId())) {
            String accessKey = req.getSecretId().trim();
            if (accessKey.startsWith("http://") || accessKey.startsWith("https://") || accessKey.startsWith("cfat_")) {
                throw new BizException("Access Key ID 应填令牌里的「访问密钥 ID」，不要填网址或 cfat_ 令牌值");
            }
            cfg.setSecretId(accessKey);
        }
        if (StringUtils.hasText(req.getSecretKey())) {
            String secret = req.getSecretKey().trim();
            if (secret.startsWith("http://") || secret.startsWith("https://") || secret.startsWith("cfat_")) {
                throw new BizException("Secret Access Key 应填令牌里的「机密访问密钥」");
            }
            cfg.setSecretKey(secret);
        }
        if (StringUtils.hasText(req.getRegion())) {
            String accountId = req.getRegion().trim();
            
            if (accountId.contains(".r2.cloudflarestorage.com")) {
                accountId = accountId.replace("https://", "").replace("http://", "");
                accountId = accountId.substring(0, accountId.indexOf('.'));
            }
            cfg.setRegion(accountId);
        }
        if (StringUtils.hasText(req.getBucket())) {
            cfg.setBucket(req.getBucket().trim());
        }
        if (req.getCdnHost() != null) {
            cfg.setCdnHost(req.getCdnHost().trim());
        }
        if (StringUtils.hasText(req.getPathPrefix())) {
            String prefix = req.getPathPrefix().trim();
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
            cfg.setPathPrefix(prefix);
        }
        if (req.getEnabled() != null) {
            cfg.setEnabled(req.getEnabled());
        }
        cosCfgMapper.updateById(cfg);
    }

    @Override
    public boolean ready() {
        CosCfg cfg = getCfg();
        return cfg.getEnabled() != null && cfg.getEnabled() == 1
                && StringUtils.hasText(cfg.getSecretId())
                && StringUtils.hasText(cfg.getSecretKey())
                && StringUtils.hasText(cfg.getBucket())
                && StringUtils.hasText(cfg.getRegion())
                && StringUtils.hasText(cfg.getCdnHost());
    }

    @Override
    public CosCredVo createPutCred(Long seriesId, String fileName) {
        CosCfg cfg = getCfg();
        String key = buildKey(cfg, seriesId, fileName);
        return signPut(key, "application/octet-stream");
    }

    @Override
    public CosCredVo createPutCred(String folder, String fileName, String contentType) {
        String type = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        String key = buildFolderKey(folder, fileName);
        return signPut(key, type);
    }

    private CosCredVo signPut(String key, String contentType) {
        if (!ready()) {
            throw new BizException("请先配置并启用 R2 存储（含公开访问域名）");
        }
        CosCfg cfg = getCfg();
        Duration ttl = Duration.ofHours(2);
        try (S3Presigner presigner = buildPresigner(cfg)) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(cfg.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            PresignedPutObjectRequest presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .putObjectRequest(objectRequest)
                    .build());
            CosCredVo vo = new CosCredVo();
            vo.setBucket(cfg.getBucket());
            vo.setRegion(cfg.getRegion());
            vo.setKey(key);
            vo.setPutUrl(presigned.url().toString());
            vo.setUrl(buildPlayUrl(key));
            vo.setContentType(contentType);
            vo.setExpireAt(System.currentTimeMillis() + ttl.toMillis());
            return vo;
        }
    }

    @Override
    public List<CosCredVo> createPutCredBatch(Long seriesId, List<String> fileNames) {
        List<CosCredVo> list = new ArrayList<>();
        for (String name : fileNames) {
            list.add(createPutCred(seriesId, name));
        }
        return list;
    }

    @Override
    public List<CosObjVo> listObjects(String prefix, String marker, int limit) {
        Map<String, Object> page = listPage(prefix, marker, limit);
        @SuppressWarnings("unchecked")
        List<CosObjVo> list = (List<CosObjVo>) page.get("list");
        return list;
    }

    @Override
    public Map<String, Object> listPage(String prefix, String marker, int limit) {
        if (!ready()) {
            throw new BizException("请先配置并启用 R2 存储（含公开访问域名）");
        }
        CosCfg cfg = getCfg();
        String usePrefix = StringUtils.hasText(prefix) ? prefix : cfg.getPathPrefix();
        try (S3Client client = buildClient(cfg)) {
            ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                    .bucket(cfg.getBucket())
                    .prefix(usePrefix)
                    .maxKeys(Math.min(Math.max(limit, 1), 100));
            if (StringUtils.hasText(marker)) {
                builder.continuationToken(marker);
            }
            ListObjectsV2Response listing = client.listObjectsV2(builder.build());
            List<CosObjVo> list = new ArrayList<>();
            for (S3Object s : listing.contents()) {
                CosObjVo vo = new CosObjVo();
                vo.setKey(s.key());
                vo.setSize(s.size() == null ? 0L : s.size());
                vo.setLastModified(s.lastModified() == null ? "" : s.lastModified().toString());
                list.add(vo);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("list", list);
            map.put("nextMarker", listing.nextContinuationToken());
            map.put("truncated", Boolean.TRUE.equals(listing.isTruncated()));
            return map;
        }
    }

    @Override
    public void deleteObject(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (!ready()) {
            throw new BizException("请先配置并启用 R2 存储（含公开访问域名）");
        }
        CosCfg cfg = getCfg();
        try (S3Client client = buildClient(cfg)) {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(cfg.getBucket())
                    .key(key)
                    .build());
        }
    }

    @Override
    public String buildPlayUrl(String cosKey) {
        if (!StringUtils.hasText(cosKey)) {
            return "";
        }
        CosCfg cfg = getCfg();
        if (!StringUtils.hasText(cfg.getCdnHost())) {
            return "";
        }
        String host = cfg.getCdnHost().trim();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        String key = cosKey.startsWith("/") ? cosKey.substring(1) : cosKey;
        return host + "/" + key;
    }

    private URI endpoint(CosCfg cfg) {
        String accountId = cfg.getRegion().trim();
        
        if (accountId.startsWith("http://") || accountId.startsWith("https://")) {
            return URI.create(accountId);
        }
        return URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
    }

    private StaticCredentialsProvider credentials(CosCfg cfg) {
        String accessKey = cfg.getSecretId() == null ? "" : cfg.getSecretId().trim();
        String secretKey = cfg.getSecretKey() == null ? "" : cfg.getSecretKey().trim();
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    

    private S3Client buildClient(CosCfg cfg) {
        return S3Client.builder()
                .endpointOverride(endpoint(cfg))
                .credentialsProvider(credentials(cfg))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .requestChecksumCalculation(software.amazon.awssdk.core.checksums.RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(software.amazon.awssdk.core.checksums.ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    private S3Presigner buildPresigner(CosCfg cfg) {
        return S3Presigner.builder()
                .endpointOverride(endpoint(cfg))
                .credentialsProvider(credentials(cfg))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }

    private String buildKey(CosCfg cfg, Long seriesId, String fileName) {
        String prefix = StringUtils.hasText(cfg.getPathPrefix()) ? cfg.getPathPrefix() : "video/";
        return prefix + seriesId + "/" + dayPart() + "/" + uuidPart() + "_" + safeName(fileName);
    }

    private String buildFolderKey(String folder, String fileName) {
        String base = StringUtils.hasText(folder) ? folder.trim() : "file";
        base = base.replaceAll("^/+|/+$", "");
        return base + "/" + dayPart() + "/" + uuidPart() + "_" + safeName(fileName);
    }

    private String safeName(String fileName) {
        return fileName == null ? "file.bin" : fileName.replaceAll("[\\\\/\\s]+", "_");
    }

    private String dayPart() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String uuidPart() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
