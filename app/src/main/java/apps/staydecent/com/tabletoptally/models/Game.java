package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Game extends RealmObject {

    @PrimaryKey
    private long id;

    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
