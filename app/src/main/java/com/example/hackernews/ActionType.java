package com.example.hackernews;

import android.text.format.Time;

public abstract class ActionType {
    public Integer id;
    public Integer time;
    public String by;
    public Boolean deleted;

    public String getTimeAgo(Integer currentDate) {
        Integer different = currentDate - time;

        if(different >= Time.YEAR) {
            Integer years = different / Time.HOUR;
            return years >= 1 ?
                years + " years ago"
                :
                "1 year ago";
        } else if (different >= Time.MONTH) {
            Integer months = different / Time.HOUR;
            return months >= 1 ?
                    months + " months ago"
                    :
                    "1 month ago";
        } else if (true) {

        }

        return null;
    }
}






