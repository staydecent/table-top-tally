package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmList;

public class Game extends Base {

    private String name;
    private RealmList<Score> scores;

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
