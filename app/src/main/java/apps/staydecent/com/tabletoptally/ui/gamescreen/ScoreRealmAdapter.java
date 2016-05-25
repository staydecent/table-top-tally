package apps.staydecent.com.tabletoptally.ui.gamescreen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import apps.staydecent.com.tabletoptally.ui.playerscreen.PlayerDetailsActivity;
import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.ButterKnife;
import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;

public class ScoreRealmAdapter
        extends RealmBasedRecyclerViewAdapter<ScoreModel, ScoreRealmAdapter.ViewHolder> {

    private Context context;
    private Realm realm;
    private long gameId;
    private RealmResults<ScoreModel> gameScores;
    private ArrayList<String> uniqueWinners; // each of these represents a Score Card

    public ScoreRealmAdapter(
            Context context,
            RealmResults<ScoreModel> realmResults,
            long gameId,
            boolean automaticUpdates,
            boolean animateResults) {
        super(context, realmResults, automaticUpdates, animateResults);
        this.context = context;
        this.realm = Realm.getDefaultInstance();
        this.gameId = gameId;
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.game_item_view, viewGroup, false);
        return new ViewHolder((FrameLayout) v);
    }


    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
//        final ScoreModel score = realmResults.get(position);
        viewHolder.text.setText("FUCK");
        viewHolder.total.setText("TOTAL");
//        viewHolder.gameTextView.setBackgroundColor(mColorHelper.getColorFromPosition(position));
    }

//    private void loadData() {
//        gameScores = realm
//                .where(ScoreModel.class)
//                .equalTo("game.id", gameId)
//                .findAllSorted("id", Sort.ASCENDING);
//        uniqueWinners = getUniqueWinners();
//    }

    // --- Helpers

    private ArrayList<String> getUniqueWinners() {
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

    private long getWinTotal(String playerName) {
        return gameScores.where().equalTo("winner", playerName).count();
    }

    private long getPlaysTotal(String playerName) {
        return gameScores.where().contains("players", playerName).count();
    }

    // --- Child Classes

    public class ViewHolder extends RealmViewHolder {
        public ScoreModel score;

        @Bind(R.id.score_list_item)
        CardView cardView;

        @Bind(R.id.score_player_name)
        TextView text;

        @Bind(R.id.score_total_view)
        TextView total;

        public ViewHolder(FrameLayout container) {
            super(container);
            ButterKnife.bind(this, container);

            final ViewHolder vh = this;
        }
    }

}
