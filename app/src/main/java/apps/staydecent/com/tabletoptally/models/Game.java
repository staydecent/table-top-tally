package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Game extends RealmObject {

    @PrimaryKey
    private long id;

    private String name;
    private RealmList<Score> scores;

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

    public RealmList<Score> getScores() {
        return scores;
    }

    public void setScores(RealmList<Score> scores) {
        this.scores = scores;
    }
}
