package com.example.hackernews;

class ExemplaryDateMilliseconds {
    static final Integer second = 1000;
    static final Integer minute = 60 * second;
    static final Integer hour   = 60 * minute;
    static final Integer day    = 24 * hour;
    static final Integer mounts = 31 * day;
    static final Integer year   = 12 * mounts;
}

public abstract class ActionType {
    public Integer id;
    public Integer time;
    public String by;
    public Boolean deleted;

    public String getTimeAgo(Integer currentDate) {
        Integer different = currentDate - time;

        if(different >= ExemplaryDateMilliseconds.year) {
            Integer years = different / ExemplaryDateMilliseconds.year;
            return years >= 1 ?
                years + " years ago"
                :
                "1 year ago";
        } else if (different >= ExemplaryDateMilliseconds.mounts) {
            Integer months = different / ExemplaryDateMilliseconds.mounts;
            return months >= 1 ?
                    months + " months ago"
                    :
                    "1 month ago";
        } else if (different >= ExemplaryDateMilliseconds.day) {
            Integer days = different / ExemplaryDateMilliseconds.day;
            return days >= 1 ?
                    days + " days ago"
                    :
                    "1 day ago";
        } else if (different >= ExemplaryDateMilliseconds.hour) {
            Integer hours = different / ExemplaryDateMilliseconds.hour;
            return hours >= 1 ?
                    hours + " hours ago"
                    :
                    "1 hour ago";
        } else if (different >= ExemplaryDateMilliseconds.minute) {
            Integer minutes = different / ExemplaryDateMilliseconds.minute;
            return minutes >= 1 ?
                    minutes + " minutes ago"
                    :
                    "1 minute ago";
        } else if (different >= ExemplaryDateMilliseconds.second) {
            Integer seconds = different / ExemplaryDateMilliseconds.second;
            return seconds >= 1 ?
                    seconds + " seconds ago"
                    :
                    "1 second ago";
        } else {
            Integer Milliseconds = different / ExemplaryDateMilliseconds.second;
            return Milliseconds >= 1 ?
                    Milliseconds + " seconds ago"
                    :
                    "1 milliseconds ago";
        }
    }
}






