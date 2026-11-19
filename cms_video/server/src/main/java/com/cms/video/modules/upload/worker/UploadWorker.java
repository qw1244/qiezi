package com.cms.video.modules.upload.worker;

import com.cms.video.common.RedisKeys;
import com.cms.video.modules.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UploadWorker {

    private final StringRedisTemplate redis;
    private final UploadService uploadService;

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        String jobId = redis.opsForList().rightPop(RedisKeys.UP_QUEUE);
        if (jobId == null || jobId.isEmpty()) {
            return;
        }
        try {
            uploadService.finalizeJob(Long.valueOf(jobId));
        } catch (Exception e) {
            e.printStackTrace();
            redis.opsForList().leftPush(RedisKeys.UP_QUEUE, jobId);
        }
    }
}
