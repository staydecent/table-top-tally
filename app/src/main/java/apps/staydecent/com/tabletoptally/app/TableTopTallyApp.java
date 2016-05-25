package apps.staydecent.com.tabletoptally.app;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by input-unger on 2016-05-25.
 */
public class TableTopTallyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration
                .Builder(this)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
