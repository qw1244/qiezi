package com.cms.video.common;

import lombok.Data;

import java.util.List;

@Data
public class PageVo<T> {

    private long total;
    private List<T> list;

    public static <T> PageVo<T> of(long total, List<T> list) {
        PageVo<T> vo = new PageVo<>();
        vo.total = total;
        vo.list = list;
        return vo;
    }
}
