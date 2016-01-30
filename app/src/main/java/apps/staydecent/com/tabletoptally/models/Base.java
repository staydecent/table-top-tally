package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Base extends RealmObject {

    @PrimaryKey
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
