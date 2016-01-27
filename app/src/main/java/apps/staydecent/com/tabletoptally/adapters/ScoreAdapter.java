package apps.staydecent.com.tabletoptally.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.ButterKnife;
import butterknife.Bind;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.GameViewHolder> {

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
    public ScoreAdapter.GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.score_item_view, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder gameViewHolder, int position) {
        String winner = uniqueWinners.get(position);
        gameViewHolder.text.setText(winner);
        String total = String.format("%d/%d", getWinTotal(winner), getPlaysTotal(winner));
        gameViewHolder.total.setText(total);
    }

    private ArrayList<String> getUniqueWinners() {
        ArrayList<String> allWinners = new ArrayList<>(gameScores.size());
        for (Score score : gameScores) {
            allWinners.add(score.getWinner());
        }
        // remove duplicate names
        Set<String> winnersSet = new LinkedHashSet<>(allWinners);
        return new ArrayList<>(winnersSet);
    }

    private long getWinTotal(String playerName) {
        return gameScores.where().equalTo("winner", playerName).count();
    }

    private long getPlaysTotal(String playerName) {
        return gameScores.where().contains("players", playerName).count();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.score_text_view)
        TextView text;
        @Bind(R.id.score_total_view)
        TextView total;

        public GameViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
