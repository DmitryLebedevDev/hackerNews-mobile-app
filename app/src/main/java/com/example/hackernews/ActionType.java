package com.example.hackernews;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    public String type;
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
    public static Request createRequestForGet(Integer id) {
        return new
        Request.Builder()
               .url("https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty")
               .build();
    }

    public static void getInstanceOfApiInObservableEmit(
        Integer id,
        ObservableEmitter<? extends ActionType> emitter,
        Class<? extends ActionType> formatTo,
        OkHttpClient http,
        Gson gson
    ) {

        Pair<Call, Runnable> req = sendRequestForGetAction(id, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException err) {
                    emitter.onError(err);
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    emitter.onNext(
                            gson.fromJson(response.body().string(), (Type) formatTo)
                    );
                    emitter.onComplete();
                }
            },
            http
        );

        emitter.setCancellable(req.first::cancel);
        req.second.run();
    }

    public static void getInstanceOfApiInObservableEmit(
            Integer id,
            SingleEmitter<? extends ActionType> emitter,
            Class<? extends ActionType> formatTo,
            OkHttpClient http,
            Gson gson
    ) {
        Pair<Call, Runnable> req = sendRequestForGetAction(id, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.v("test", "err");
                emitter.onError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                emitter.onSuccess(
                    gson.fromJson(response.body().string(), (Type) formatTo)
                );
            }
        }, http);

        emitter.setCancellable(req.first::cancel);
        req.second.run();
    }

    private static Pair<Call, Runnable> sendRequestForGetAction(
        Integer id,
        Callback callback,
        OkHttpClient http
    ) {
        Call req = http.newCall(
            Comment.createRequestForGet(id)
        );

        return new Pair<>(
            req,
            () -> req.enqueue(callback)
        );
    }
}






