package com.photor.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TrashBinRealmModel extends RealmObject {

    @PrimaryKey
    private String trashbinpath;
    private String oldpath;
    private String datetime;
    private String timeperiod;

    public TrashBinRealmModel() { }

    public TrashBinRealmModel(String trashbinpath, String oldpath, String datetime, String timeperiod) {
        this.trashbinpath = trashbinpath;
        this.oldpath = oldpath;
        this.datetime = datetime;
        this.timeperiod = timeperiod;
    }

    public String getTrashbinpath() {
        return trashbinpath;
    }

    public void setTrashbinpath(String trashbinpath) {
        this.trashbinpath = trashbinpath;
    }

    public String getOldpath() {
        return oldpath;
    }

    public void setOldpath(String oldpath) {
        this.oldpath = oldpath;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getTimeperiod() {
        return timeperiod;
    }

    public void setTimeperiod(String timeperiod) {
        this.timeperiod = timeperiod;
    }
}
