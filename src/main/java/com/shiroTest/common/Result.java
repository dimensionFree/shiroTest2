package com.shiroTest.common;

import com.shiroTest.enums.ResultCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Desc: 处理统一返回结果格式
 * @Author: 知否技术
 * @date: 下午9:44 2022/10/24
 */
@Data
public class Result<T> implements Serializable {

    private String code;
    private String message;
    private T Data;

    public static Result success(Object object){
        Result result = new Result();
        result.setCode("200");
        result.setData(object);
        result.setMessage("操作成功");
        return  result;
    }

    public static Result fail(String message){
        Result result = new Result();
        result.setCode(ResultCodeEnum.PARAM_ERROR.getCode());
        result.setData(null);
        result.setMessage(message);
        return  result;
    }







}
