package apps.staydecent.com.tabletoptally.models;

public class Score extends Base {

    private static final String DELIM = "; ";

    private String players;
    private String winner;
    private Game game;

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
