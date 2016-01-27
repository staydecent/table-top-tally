package apps.staydecent.com.tabletoptally.adapters;

import android.support.v7.widget.RecyclerView;
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

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.ButterKnife;
import butterknife.Bind;
import io.realm.RealmResults;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private RealmResults<Score> gameScores;
    private ArrayList<String> uniqueWinners;

    public ScoreAdapter(RealmResults<Score> scores) {
        this.gameScores = scores;
        this.uniqueWinners = getUniqueWinners();
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

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.score_text_view)
        TextView text;
        @Bind(R.id.score_total_view)
        TextView total;

        public ScoreViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
