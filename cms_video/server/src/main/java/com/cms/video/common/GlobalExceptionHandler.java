package com.cms.video.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiRes<Void> handleBiz(BizException e) {
        return ApiRes.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiRes<Void> handleValid(Exception e) {
        String msg = "参数错误";
        if (e instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            msg = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            msg = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return ApiRes.fail(msg);
    }

    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    public ApiRes<Void> handleDuplicate(Exception e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        if (raw.contains("uk_username") || raw.contains("username")) {
            return ApiRes.fail("账号已存在");
        }
        return ApiRes.fail("数据冲突，请重试");
    }

    @ExceptionHandler(Exception.class)
    public ApiRes<Void> handleOther(Exception e) {
        e.printStackTrace();
        return ApiRes.fail("系统繁忙");
    }
}
