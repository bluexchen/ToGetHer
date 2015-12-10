package com.exfantasy.together.vo;

import java.util.HashSet;
import java.util.Set;

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
     * 事件日期
     */
    private int date;
    /**
     * 事件時間
     */
    private int time;
    /**
     * 參與活動的使用者
     */
    private Set<User> users;
    /**
     * 活動的留言
     */
    private Set<Message> messages;

    public Event() {
    }

    public Event(double latitude, double longitude, String name, String content, int attendeeNum, int date, int time) {
        this(0, 0, latitude, longitude, name, content, attendeeNum, date, time);
    }

    public Event(long eventId, long createUserId, double latitude, double longitude, String name, String content, int attendeeNum, int date, int time) {
        this.eventId = eventId;
        this.createUserId = createUserId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.content = content;
        this.attendeeNum = attendeeNum;
        this.date = date;
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


    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(user);
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        if (messages == null) {
            messages = new HashSet<>();
        }
        messages.add(message);
    }

    @Override
    public String toString() {
        return "Event [eventId=" + eventId + ", createUserId=" + createUserId + ", latitude=" + latitude
                + ", longitude=" + longitude + ", name=" + name + ", content=" + content + ", attendeeNum="
                + attendeeNum + ", date=" + date + ", time=" + time + ", users=" + users + ", messages=" + messages
                + "]";
    }
}

