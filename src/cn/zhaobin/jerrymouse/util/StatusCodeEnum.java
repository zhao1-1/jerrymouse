package cn.zhaobin.jerrymouse.util;

import java.io.IOException;

public enum StatusCodeEnum {

    STATUS_CODE_200(200, Constant.RESPONSE_HEAD_200, ""),
    STATUS_CODE_302(302, "", ""),
    STATUS_CODE_404(404, Constant.RESPONSE_HEAD_404, Constant.TEXT_FORMAT_404),
    STATUS_CODE_500(500, Constant.RESPONSE_HEAD_500, Constant.RESPONSE_HEAD_500);

    private int code;
    private String head;
    private String content;

    StatusCodeEnum(int code, String head, String content) {
        this.code = code;
        this.head = head;
        this.content = content;
    }

    public String getHead() { return this.head; }

    public static StatusCodeEnum valueOf(int code) throws Exception {
        for (StatusCodeEnum type : StatusCodeEnum.values()) {
            if(code == type.code) return type;
        }
        throw new Exception("undefined status code");
    }

}
