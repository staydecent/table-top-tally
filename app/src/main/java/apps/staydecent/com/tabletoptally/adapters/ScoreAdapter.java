package apps.staydecent.com.tabletoptally.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import apps.staydecent.com.tabletoptally.GameActivity;
import apps.staydecent.com.tabletoptally.PlayerDetailsActivity;
import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.ButterKnife;
import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private Context context;
    private Realm realm;
    private long gameId;
    private RealmResults<Score> gameScores;
    private ArrayList<String> uniqueWinners; // each of these represents a Score Card

    public ScoreAdapter(Context context, Realm realm, long gameId) {
        this.context = context;
        this.realm = realm;
        this.gameId = gameId;
        loadData();
    }

    @Override
    public int getItemCount() {
        return uniqueWinners.size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.score_item_view, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScoreViewHolder scoreViewHolder, int position) {
        String winner = uniqueWinners.get(position);
        scoreViewHolder.text.setText(winner);
        String total = String.format("%d/%d", getWinTotal(winner), getPlaysTotal(winner));
        scoreViewHolder.total.setText(total);
    }

    public void loadDataAndNotifyAdapter() {
        loadData();
        notifyDataSetChanged();
    }

    private void loadData() {
        gameScores = realm
                .where(Score.class)
                .equalTo("game.id", gameId)
                .findAllSorted("id", Sort.ASCENDING);
        uniqueWinners = getUniqueWinners();
    }

    private ArrayList<String> getUniqueWinners() {
        ArrayList<String> allWinners = new ArrayList<>(gameScores.size());
        for (Score score : gameScores) {
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

    public class ScoreViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.score_list_item)
        CardView cardView;

        @Bind(R.id.score_player_name)
        TextView text;

        @Bind(R.id.score_total_view)
        TextView total;

        public ScoreViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            final RecyclerView.ViewHolder vh = this;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String playerName = uniqueWinners.get(vh.getAdapterPosition());
                    Log.d("TTT", String.format("HIYA %s", playerName));
                    Intent intent = new Intent(context, PlayerDetailsActivity.class);
                    intent.putExtra("playerName", playerName);

                    GameActivity gameActivity = (GameActivity) context;

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            gameActivity,
                            new Pair<View, String>(view.findViewById(R.id.score_player_name),
                                    context.getResources().getString(R.string.transition_name_player_name))
                    );

                    gameActivity.startActivity(intent, options.toBundle());
                }
            });
        }
    }
}
