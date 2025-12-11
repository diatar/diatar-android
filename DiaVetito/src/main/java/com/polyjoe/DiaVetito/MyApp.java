package com.polyjoe.DiaVetito;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TcpServer.get(null);
    }

    @Override
    public void onTerminate() {
        TcpServer t = TcpServer.getMe();
        if (t!=null) t.Stop();
        super.onTerminate();
    }
}
