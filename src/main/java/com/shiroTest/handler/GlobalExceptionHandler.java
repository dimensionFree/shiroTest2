package com.shiroTest.handler;

import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public Result handler(AuthenticationException e) {
        log.info("权限不足："+e.getMessage(),e);
        return Result.fail(e);
    }



    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = ShiroException.class)
    public Result handler(ShiroException e) {
        log.error("运行时shiro异常：----------------{}", e);
        return Result.fail( e.getMessage());
    }

    @ExceptionHandler(MyException.class)
    public Result handler(MyException e) throws IOException {
        log.info("运行时my异常："+e.getMessage(),e);
        return Result.fail(e);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handler(MethodArgumentNotValidException e) throws IOException {
        log.info("运行时validate exception："+e.getMessage(),e);
        return Result.fail(e);
    }
    @ExceptionHandler(RuntimeException.class)
    public Result handler(RuntimeException e){
        log.info("运行时异常：",e.getMessage());
        return Result.fail(e.getMessage());
    }


}
