package com.cms.video.modules.upload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpCreateReq {

    @NotNull(message = "缺少合集")
    private Long seriesId;

    @NotEmpty(message = "请选择文件")
    private List<UpFileReq> files;
}
