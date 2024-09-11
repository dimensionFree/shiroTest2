package com.shiroTest.enums;

import org.springframework.http.HttpStatus;


public enum ResultCodeEnum {
    SUCCESS("0000", "操作成功", HttpStatus.OK),
    SUCCESS_QUERY("0001", "查询成功", HttpStatus.OK),
    SUCCESS_ADD("0002", "添加成功", HttpStatus.OK),
    SUCCESS_UPDATE("0003", "更新成功", HttpStatus.OK),
    SUCCESS_DELETE("0004", "删除成功", HttpStatus.OK),


    TOKEN_ERROR("1000", "token错误", HttpStatus.UNAUTHORIZED),
    TOKEN_NULL("1001", "token为空", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("1002", "token过期", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("1003", "token无效", HttpStatus.UNAUTHORIZED),

    USER_ERROR("2000", "用户名密码错误", HttpStatus.UNAUTHORIZED),
    USER_NOT_EXISTS("2001", "用户不存在", HttpStatus.NOT_FOUND),
    USER_INVALID("2002", "用户无效", HttpStatus.UNAUTHORIZED),
    USER_EXPIRED("2003", "用户过期", HttpStatus.UNAUTHORIZED),
    USER_BLOCKED("2004", "用户封禁", HttpStatus.UNAUTHORIZED),
    USER_DUPLICATE("2005", "用户已存在", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_ERROR("2005", "密码错误", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_REGISTER("2006", "邮箱已注册", HttpStatus.BAD_REQUEST),
    VERIFICATION_NOT_MATCH("2007", "验证码与邮箱不匹配", HttpStatus.BAD_REQUEST),

    PARAM_ERROR("3000", "参数错误", HttpStatus.BAD_REQUEST),
    PARAM_NULL("3001", "参数为空", HttpStatus.BAD_REQUEST),
    PARAM_FORMAT_ERROR("3002", "参数格式不正确", HttpStatus.BAD_REQUEST),
    PARAM_VALUE_INCORRECT("3003", "参数值不正确", HttpStatus.BAD_REQUEST),
    PARAM_DUPLICATE("3004", "参数重复", HttpStatus.BAD_REQUEST),
    PARAM_CONVERT_ERROR("3005", "参数转化错误", HttpStatus.BAD_REQUEST),

    AUTHORITY_ERROR("4000", "权限错误", HttpStatus.BAD_REQUEST),
    AUTHORITY_UNAUTHORIZED("4001", "无权限", HttpStatus.BAD_REQUEST),

    SERVER_ERROR("5000", "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVER_UNAVAILABLE("5001", "服务器不可用", HttpStatus.INTERNAL_SERVER_ERROR),


    ;

    ResultCodeEnum(String code,  String message,HttpStatus status) {
        this.code = code;
        this.status = status;
        this.message = message;
    }


    private String code;
    private HttpStatus status;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
