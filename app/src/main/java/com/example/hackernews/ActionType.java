package com.example.hackernews;

import android.util.Log;

import java.util.Date;

class ExemplaryDateMilliseconds {
    static final long millisecond = 1;
    static final long second      = 1000 * millisecond;
    static final long minute      = 60   * second;
    static final long hour        = 60   * minute;
    static final long day         = 24   * hour;
    static final long week        = 7    * day;
    static final long mount       = 31   * day;
    static final long year        = 12   * mount;
}

class ConvertDateToAgoString {
    final String name;
    final long milliseconds;

    ConvertDateToAgoString(String name, long milliseconds) {
        this.name = name;
        this.milliseconds = milliseconds;
    }

    String isConverted(long milliseconds) {
        long whole = milliseconds / this.milliseconds;
        if(whole > 1) {
            return whole + " " + name + "s ago";
        } else if(whole == 1) {
            return whole + " " + name + " ago";
        } else {
            return null;
        }
    }
}

class ConverterDateToAgoString {
    static final ConvertDateToAgoString[] converters = {
        new ConvertDateToAgoString("year",        ExemplaryDateMilliseconds.year),
        new ConvertDateToAgoString("mount",       ExemplaryDateMilliseconds.mount),
        new ConvertDateToAgoString("week",        ExemplaryDateMilliseconds.week),
        new ConvertDateToAgoString("day",         ExemplaryDateMilliseconds.day),
        new ConvertDateToAgoString("hour",        ExemplaryDateMilliseconds.hour),
        new ConvertDateToAgoString("minute",      ExemplaryDateMilliseconds.minute),
        new ConvertDateToAgoString("second",      ExemplaryDateMilliseconds.second),
        new ConvertDateToAgoString("millisecond", ExemplaryDateMilliseconds.millisecond),
    };

    static String convert(Long diff) {
        for(int t=0; t<converters.length; t++) {
            String ago = converters[t].isConverted(diff);
            if(ago != null)
                return ago;
        }

        return "now";
    }
}

public abstract class ActionType {
    public Integer id;
    public Long time;
    public String by;
    public Boolean deleted;

    public String getTimeAgo(long currentDate) {
        long different = currentDate - (time * 1000);

        return ConverterDateToAgoString.convert(different);
    }
    public String getTimeAgo() {
        long currentDate = new Date().getTime();

        return getTimeAgo(currentDate);
    }
}






