package apps.staydecent.com.tabletoptally.views.gamescreen;

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
import android.widget.TextView;

import java.util.ArrayList;

import apps.staydecent.com.tabletoptally.helpers.ScoreHelper;
import apps.staydecent.com.tabletoptally.views.playerscreen.PlayerDetailsActivity;
import apps.staydecent.com.tabletoptally.R;
import butterknife.ButterKnife;
import butterknife.Bind;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    public long mGameId;
    public int mColor;

    private Context context;
    private ScoreHelper scoreHelper;
    private ArrayList<String> uniqueWinners; // each of these represents a Score Card

    public ScoreAdapter(Context c, long gameId, int color) {
        mGameId = gameId;
        mColor = color;
        context = c;
        scoreHelper = new ScoreHelper(gameId);
        updateScores();
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
        scoreViewHolder.playerNameText.setText(winner);
        String total = String.format("%d/%d",
                scoreHelper.getWinTotal(winner),
                scoreHelper.getPlaysTotal(winner));
        scoreViewHolder.totalText.setText(total);
    }

    public void updateScores() {
        updateScores(false);
    }

    public void updateScores(boolean notify) {
        scoreHelper.loadScores();
        ArrayList<String> newUniqueWinners = scoreHelper.getUniqueWinners();

        if (uniqueWinners != null) {
            uniqueWinners.clear();
            uniqueWinners.addAll(newUniqueWinners);
        } else {
            uniqueWinners = newUniqueWinners;
        }

        if (notify) {
            notifyDataSetChanged();
        }
    }


    public class ScoreViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.score_list_item)
        CardView cardView;

        @Bind(R.id.score_player_name)
        TextView playerNameText;

        @Bind(R.id.score_total_view)
        TextView totalText;

        public ScoreViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            final RecyclerView.ViewHolder vh = this;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getAdapterPosition();
                    String playerName = uniqueWinners.get(position);

                    Intent intent = new Intent(context, PlayerDetailsActivity.class);
                    intent.putExtra("playerName", playerName);
                    intent.putExtra("gameId", mGameId);
                    intent.putExtra("color", mColor);

                    GameActivity gameActivity = (GameActivity) context;
                    if (!gameActivity.isPlayerDetailsActivityStarted) {
                        gameActivity.isPlayerDetailsActivityStarted = true;
                    }

                    gameActivity.startActivity(intent);
                }
            });
        }
    }
}
