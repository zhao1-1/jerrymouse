package cn.zhaobin.jerrymouse.util;

import cn.hutool.core.util.ArrayUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class CommonUtils {

    /**
     * 缓存读取输入流
     * @param fully 是否完全读取
     */
    public static byte[] readBytes(InputStream is, boolean fully) throws IOException {
        byte[] buffer = new byte[Constant.BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true) {
            int length = is.read(buffer);
            if(-1 == length)
                break;
            baos.write(buffer, 0, length);
            if(!fully && length != Constant.BUFFER_SIZE)
                break;
        }
        return baos.toByteArray();
    }

    public static String byte2String(byte[] input, String charset) {
        if(null == input)
            return null;
        try {
            return new String(input, charset).trim();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] glue2bytes(byte[] a, byte[] b) {
        byte[] resultByte = new byte[a.length + b.length];
        ArrayUtil.copy(a, 0, resultByte, 0, a.length);
        ArrayUtil.copy(b, 0, resultByte, a.length, b.length);
        return resultByte;
    }

    public static String convertStackTraceMsg(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString());
        sb.append("\n");
        StackTraceElement[] elements = e.getStackTrace();
        for (StackTraceElement ele : elements) {
            sb.append("\t");
            sb.append(ele.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String convertExceptionMsg(Throwable e) {
        String msg = e.getMessage();
        if (null != msg && msg.length() > 20)
            msg = msg.substring(0, 19);
        return msg;
    }

}
