package com.exfantasy.together.components.cardView;

/**
 * Created by User on 2015/12/11.
 */
public class MsgRecordItem {
    /**
     * 留言者名字
     */
    private String createName;
    /**
     * 留言內容
     */
    private String content;

    public MsgRecordItem(String createName, String content){
        this.createName = createName;
        this.content = content;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
