package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GameModel extends RealmObject {

    @PrimaryKey
    private long id;

    private String name;
    private RealmList<ScoreModel> scores;

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

    public RealmList<ScoreModel> getScores() {
        return scores;
    }

    public void setScores(RealmList<ScoreModel> scores) {
        this.scores = scores;
    }
}
