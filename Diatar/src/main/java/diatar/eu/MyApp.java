package diatar.eu;

import android.app.Application;

import diatar.eu.net.TcpClient;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TcpClient.get(null);
    }

    @Override
    public void onTerminate() {
        TcpClient t = TcpClient.getMe();
        if (t!=null) t.Stop();
        super.onTerminate();
    }
}
