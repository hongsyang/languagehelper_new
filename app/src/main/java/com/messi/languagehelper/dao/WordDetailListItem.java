package com.messi.languagehelper.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;


@Entity
public class WordDetailListItem {

    @Id(autoincrement = true)
    private Long id;
    private String item_id;
    private String class_id;
    private Integer course;
    private String class_title;
    private String desc;
    private String name;
    private String sound;
    private String symbol;
    private String examples;
    private String mp3_sdpath;
    private String img_url;
    private String new_words;
    private String is_study;
    private String backup1;
    private String backup2;
    private String backup3;
    @Transient
    private int select_time;

    public void setSelect_Time(){
        select_time++;
    }

    @Generated(hash = 279956360)
    public WordDetailListItem(Long id, String item_id, String class_id,
            Integer course, String class_title, String desc, String name,
            String sound, String symbol, String examples, String mp3_sdpath,
            String img_url, String new_words, String is_study, String backup1,
            String backup2, String backup3) {
        this.id = id;
        this.item_id = item_id;
        this.class_id = class_id;
        this.course = course;
        this.class_title = class_title;
        this.desc = desc;
        this.name = name;
        this.sound = sound;
        this.symbol = symbol;
        this.examples = examples;
        this.mp3_sdpath = mp3_sdpath;
        this.img_url = img_url;
        this.new_words = new_words;
        this.is_study = is_study;
        this.backup1 = backup1;
        this.backup2 = backup2;
        this.backup3 = backup3;
    }

    @Generated(hash = 1218086553)
    public WordDetailListItem() {
    }

    public int getSelect_time() {
        return select_time;
    }

    public void setSelect_time(int select_time) {
        this.select_time = select_time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }

    public Integer getCourse() {
        return course;
    }

    public void setCourse(Integer course) {
        this.course = course;
    }

    public String getClass_title() {
        return class_title;
    }

    public void setClass_title(String class_title) {
        this.class_title = class_title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getMp3_sdpath() {
        return mp3_sdpath;
    }

    public void setMp3_sdpath(String mp3_sdpath) {
        this.mp3_sdpath = mp3_sdpath;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getNew_words() {
        return new_words;
    }

    public void setNew_words(String new_words) {
        this.new_words = new_words;
    }

    public String getIs_study() {
        return is_study;
    }

    public void setIs_study(String is_study) {
        this.is_study = is_study;
    }

    public String getBackup1() {
        return backup1;
    }

    public void setBackup1(String backup1) {
        this.backup1 = backup1;
    }

    public String getBackup2() {
        return backup2;
    }

    public void setBackup2(String backup2) {
        this.backup2 = backup2;
    }

    public String getBackup3() {
        return backup3;
    }

    public void setBackup3(String backup3) {
        this.backup3 = backup3;
    }

}
