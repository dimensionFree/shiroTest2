package com.shiroTest.enums;

import org.springframework.http.HttpStatus;


public enum ResultCodeEnum {
    SUCCESS("0000", "操作成功", HttpStatus.OK),
    SUCCESS_QUERY("0001", "クエリ成功", HttpStatus.OK),
    SUCCESS_ADD("0002", "追加成功", HttpStatus.OK),
    SUCCESS_UPDATE("0003", "更新成功", HttpStatus.OK),
    SUCCESS_DELETE("0004", "削除成功", HttpStatus.OK),

    TOKEN_ERROR("1000", "トークンエラー", HttpStatus.UNAUTHORIZED),
    TOKEN_NULL("1001", "トークンが空です", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("1002", "トークンの有効期限が切れています", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("1003", "トークンが無効です", HttpStatus.UNAUTHORIZED),

    USER_ERROR("2000", "ユーザー名またはパスワードが間違っています", HttpStatus.UNAUTHORIZED),
    USER_NOT_EXISTS("2001", "ユーザーが存在しません", HttpStatus.NOT_FOUND),
    USER_INVALID("2002", "ユーザーが無効です", HttpStatus.UNAUTHORIZED),
    USER_EXPIRED("2003", "ユーザーが期限切れです", HttpStatus.UNAUTHORIZED),
    USER_BLOCKED("2004", "ユーザーがブロックされています", HttpStatus.UNAUTHORIZED),
    USER_DUPLICATE("2005", "ユーザーはすでに存在しています", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_ERROR("2005", "パスワードが間違っています", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_REGISTER("2006", "メールアドレスは既に登録されています", HttpStatus.BAD_REQUEST),
    VERIFICATION_NOT_MATCH("2007", "確認コードがメールアドレスと一致しません", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXIST("2007", "有効な確認コードが既に存在します", HttpStatus.BAD_REQUEST),

    PARAM_ERROR("3000", "パラメータエラー", HttpStatus.BAD_REQUEST),
    PARAM_NULL("3001", "パラメータが空です", HttpStatus.BAD_REQUEST),
    PARAM_FORMAT_ERROR("3002", "パラメータ形式が正しくありません", HttpStatus.BAD_REQUEST),
    PARAM_VALUE_INCORRECT("3003", "パラメータの値が正しくありません", HttpStatus.BAD_REQUEST),
    PARAM_DUPLICATE("3004", "パラメータが重複しています", HttpStatus.BAD_REQUEST),
    PARAM_CONVERT_ERROR("3005", "パラメータの変換エラー", HttpStatus.BAD_REQUEST),

    AUTHORITY_ERROR("4000", "権限エラー", HttpStatus.BAD_REQUEST),
    AUTHORITY_UNAUTHORIZED("4001", "権限がありません", HttpStatus.BAD_REQUEST),

    SERVER_ERROR("5000", "サーバー内部エラー", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVER_UNAVAILABLE("5001", "サーバーが利用できません", HttpStatus.INTERNAL_SERVER_ERROR),



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
