package com.example.hackernews;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class ActionsActivity extends AppCompatActivity {
    ActionsApi  actionsInfo = new ActionsApi(new OkHttpClient(), new Gson());
    ProgressBar vLoadingActions;
    TextView vQuantityActions;
    CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        vLoadingActions = findViewById(R.id.loadingActionsBar);
        vQuantityActions = findViewById(R.id.quantityActions);

        disposables.add(
            actionsInfo.updateQuantity().subscribeWith(new DisposableObserver<Integer>() {
                @Override
                public void onNext(@NonNull Integer integer) {

                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {
                    Log.v("tag",  actionsInfo.quantity.toString());
                    vLoadingActions.setVisibility(View.GONE);
                    vQuantityActions.append(actionsInfo.quantity.toString());

                    actionsInfo.liveQuantity()
                    .observeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<Integer>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onNext(@NonNull Integer integer) {
                            Log.v("tag", actionsInfo.quantity.toString());
                            vQuantityActions.setText("Actions " + actionsInfo.quantity.toString());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }
            })
        );
    }
}