package apps.staydecent.com.tabletoptally;

import android.app.Activity;
import android.os.Bundle;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseActivity extends Activity {

    public Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetRealm();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

}
