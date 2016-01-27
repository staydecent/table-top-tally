package apps.staydecent.com.tabletoptally.models;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Score extends RealmObject {

    private static final String DELIM = "; ";

    @PrimaryKey
    private long id;

    private String players;
    private String winner;
    private Game game;

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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}