package cn.zhaobin.jerrymouse.util;

public enum StatusCodeEnum {

    STATUS_CODE_200(200, "HTTP/1.1 200 OK{}{}\n\n", ""),
    STATUS_CODE_302(302, "HTTP/1.1 302 Found\nLocation: {}\n\n", ""),
    STATUS_CODE_404(404, "HTTP/1.1 404 Not Found\nContent-Type: text/html\n\n", Constant.TEXT_FORMAT_404),
    STATUS_CODE_500(500, "HTTP/1.1 500 Internal Server Error\nContent-Type: text/html\n\n", Constant.TEXT_FORMAT_500);

    private final int code;
    private final String head;
    private final String content;

    StatusCodeEnum(int code, String head, String content) {
        this.code = code;
        this.head = head;
        this.content = content;
    }

    public int getCode() { return this.code; }
    public String getHead() { return this.head; }
    public String getContent() { return this.content; }

    public static StatusCodeEnum valueOf(int code) throws Exception {
        for (StatusCodeEnum type : StatusCodeEnum.values()) {
            if(code == type.code) return type;
        }
        throw new Exception("undefined status code");
    }

}
