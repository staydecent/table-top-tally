package apps.staydecent.com.tabletoptally.helpers;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import apps.staydecent.com.tabletoptally.models.ScoreModel;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by aunger on 2016-05-25.
 */
public class ScoreHelper {

    private long gameId;
    private Realm realm;
    private RealmResults<ScoreModel> gameScores;

    public ScoreHelper(long gameId) {
        this.gameId = gameId;
        realm = Realm.getDefaultInstance();
        loadScores();
    }

    public void loadScores() {
        gameScores = realm
                .where(ScoreModel.class)
                .equalTo("game.id", gameId)
                .findAllSorted("id", Sort.ASCENDING);
    }

    public ArrayList<String> getUniqueWinners() {
        ArrayList<String> allWinners = new ArrayList<>(gameScores.size());
        for (ScoreModel score : gameScores) {
            allWinners.add(score.getWinner());
        }

        // Sort by frequency of occurrence
        final Multiset<String> winnerCounts = HashMultiset.create(allWinners);
        Ordering<String> byFrequency = new Ordering<String>() {
            public int compare(String left, String right) {
                return Ints.compare(winnerCounts.count(left), winnerCounts.count(right));
            }
        };
        Collections.sort(allWinners, byFrequency.reverse());

        // remove duplicate names (preserved ordering)
        Set<String> winnersSet = new LinkedHashSet<>(allWinners);

        return new ArrayList<>(winnersSet);
    }

    public long getWinTotal(String playerName) {
        return gameScores.where().equalTo("winner", playerName).count();
    }

    public long getPlaysTotal(String playerName) {
        return gameScores.where().contains("players", playerName).count();
    }
}
