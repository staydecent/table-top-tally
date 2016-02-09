package apps.staydecent.com.tabletoptally.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ScoreModel extends RealmObject {

    @PrimaryKey
    private long id;

    private static final String DELIM = "; ";

    private String players;
    private String winner;
    private GameModel game;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public GameModel getGame() {
        return game;
    }

    public void setGame(GameModel game) {
        this.game = game;
    }
}
