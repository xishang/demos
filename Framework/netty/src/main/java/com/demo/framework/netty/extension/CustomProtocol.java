package com.demo.framework.netty.extension;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/4
 * <p>
 * 扩展协议
 */
public class CustomProtocol {

    // 协议版本
    private int version;
    // 消息长度
    private int contentLength;
    // 消息内容
    private String content;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "custom protocol: version = " + version + ", contentLength = " + contentLength + ", content = [" + content + "]";
    }

}
