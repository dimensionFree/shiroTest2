package com.shiroTest.common;

import com.shiroTest.enums.ResultCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.Serializable;


public class Result<T> extends ResponseEntity<T> implements Serializable {

    public Result(HttpStatus status) {
        super(status);
    }

    public Result(@Nullable T body, HttpStatus status) {
        super(body, null, status);
    }

    public static Result success(Object object){
        ResultData resultData=new ResultData();
        resultData.setCode("200");
        resultData.setMessage("操作成功");
        resultData.setDataContent(object);
        Result result = new Result(resultData,HttpStatus.OK);

        return  result;
    }

    public static Result fail(String message){
        ResultData resultData=new ResultData();

        resultData.setCode(ResultCodeEnum.PARAM_ERROR.getCode());
        resultData.setMessage(message);
        Result result = new Result(resultData,ResultCodeEnum.PARAM_ERROR.getStatus());

        return  result;
    }
    public static Result fail(Exception e){
        ResultData resultData=new ResultData();

        resultData.setCode(ResultCodeEnum.PARAM_ERROR.getCode());
        resultData.setMessage(e.getMessage());
        Result result = new Result(resultData,ResultCodeEnum.PARAM_ERROR.getStatus());

        return  result;
    }
    public static Result fail(MyException me) throws IOException {
        ResultData resultData=new ResultData();
        resultData.setCode(me.getErrorEnum().getCode());
        resultData.setMessage(me.getMessage());

        Result result = new Result(resultData,me.getErrorEnum().getStatus());
        return result;
    }

}
