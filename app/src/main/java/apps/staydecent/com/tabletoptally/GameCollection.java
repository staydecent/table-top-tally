package apps.staydecent.com.tabletoptally;

import java.util.ArrayList;
import java.util.List;

public class GameCollection {

    private List<Game> mGameList;

    public GameCollection() {
        super();
        mGameList = load();
    }

    public Game get(int position) {
        return mGameList.get(position);
    }

    public List<Game> all() {
        return mGameList;
    }

    // TODO: Eventually this needs to be loaded from Realm
    public List<Game> load() {
        List<Game> GameList = new ArrayList<Game>();

        Game Game1 = new Game("Carcassonne");
        GameList.add(Game1);

        Game Game2 = new Game("Settlers of Catan");
        GameList.add(Game2);

        Game Game3 = new Game("Ticket to Ride");
        GameList.add(Game3);

        Game Game4 = new Game("Tokaido");
        GameList.add(Game4);

        Game Game5 = new Game("Exploding Kittens");
        GameList.add(Game5);

        Game Game6 = new Game("Chess");
        GameList.add(Game6);

        Game Game7 = new Game("Boss Monstor");
        GameList.add(Game7);

        Game Game8 = new Game("Bang");
        GameList.add(Game8);

        Game Game9 = new Game("Bananagrams");
        GameList.add(Game9);

        Game Game10 = new Game("Risk");
        GameList.add(Game10);

        return GameList;
    }
}
