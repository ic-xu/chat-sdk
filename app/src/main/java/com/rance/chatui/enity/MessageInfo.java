package com.rance.chatui.enity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 作者：Rance on 2016/12/14 14:13
 * 邮箱：rance935@163.com
 */
@Entity
public class MessageInfo {

    @Id
    private Long msgId;
    private int type;
    private String content;
    private String filepath;
    private int sendState;
    private Long time;
    private String header;
    private long voiceTime;
    private String fileType;
    private String mimeType;

    String name, phonenumber, surname;

    String subject, text, stream, url;



    @Generated(hash = 2047931031)
    public MessageInfo(Long msgId, int type, String content, String filepath,
            int sendState, Long time, String header, long voiceTime,
            String fileType, String mimeType, String name, String phonenumber,
            String surname, String subject, String text, String stream,
            String url) {
        this.msgId = msgId;
        this.type = type;
        this.content = content;
        this.filepath = filepath;
        this.sendState = sendState;
        this.time = time;
        this.header = header;
        this.voiceTime = voiceTime;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.name = name;
        this.phonenumber = phonenumber;
        this.surname = surname;
        this.subject = subject;
        this.text = text;
        this.stream = stream;
        this.url = url;
    }

    @Generated(hash = 1292770546)
    public MessageInfo() {
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public long getVoiceTime() {
        return voiceTime;
    }

    public void setVoiceTime(long voiceTime) {
        this.voiceTime = voiceTime;
    }

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "msgId=" + msgId +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", filepath='" + filepath + '\'' +
                ", sendState=" + sendState +
                ", time='" + time + '\'' +
                ", header='" + header + '\'' +
                ", voiceTime=" + voiceTime +
                ", fileType='" + fileType + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", name='" + name + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                ", surname='" + surname + '\'' +
                ", subject='" + subject + '\'' +
                ", text='" + text + '\'' +
                ", stream='" + stream + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
