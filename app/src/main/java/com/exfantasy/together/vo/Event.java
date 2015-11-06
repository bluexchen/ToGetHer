package com.exfantasy.together.vo;

/**
 * Created by Tommy on 2015/11/4.
 */
public class Event {
    /**
     * 事件 ID
     */
    private long id;
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
        this(0, latitude, longitude, name, content, attendeeNum, time);
    }

    public Event(long id, double latitude, double longitude, String name, String content, int attendeeNum, long time) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.content = content;
        this.attendeeNum = attendeeNum;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return "Event [id=" + id + ", latitude=" + latitude + ", longitude=" + longitude + ", name=" + name
                + ", content=" + content + ", attendeeNum=" + attendeeNum + ", time=" + time + "]";
    }
}
