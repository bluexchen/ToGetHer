package com.exfantasy.together.vo;

/**
 * Created by Tommy on 2015/11/4.
 */
public class Event {
    /**
     * 事件 ID
     */
    private long eventId;
    /**
     * 建立事件使用者 ID
     */
    private long createUserId;
    /**
     * 緯度
     */
    private double latitude;
    /**
     * 經度
     */
    private double longitude;
    /**
     * 事件名稱
     */
    private String name;
    /**
     * 事件內容
     */
    private String content;
    /**
     * 參加人數
     */
    private int attendeeNum;
    /**
     * 事件時間
     */
    private long time;

    public Event() {
    }

    public Event(double latitude, double longitude, String name, String content, int attendeeNum, long time) {
        this(0, 0, latitude, longitude, name, content, attendeeNum, time);
    }

    public Event(long eventId, long createUserId, double latitude, double longitude, String name, String content, int attendeeNum, long time) {
        this.eventId = eventId;
        this.createUserId = createUserId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.content = content;
        this.attendeeNum = attendeeNum;
        this.time = time;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(long createUserId) {
        this.createUserId = createUserId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAttendeeNum() {
        return attendeeNum;
    }

    public void setAttendeeNum(int attendeeNum) {
        this.attendeeNum = attendeeNum;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Event [eventId=" + eventId + ", createUserId=" + createUserId + ", latitude=" + latitude
                + ", longitude=" + longitude + ", name=" + name + ", content=" + content + ", attendeeNum="
                + attendeeNum + ", time=" + time + "]";
    }
}

