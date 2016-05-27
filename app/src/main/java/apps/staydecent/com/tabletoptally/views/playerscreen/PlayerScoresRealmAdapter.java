package apps.staydecent.com.tabletoptally.views.playerscreen;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Objects;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.helpers.ScoreHelper;
import apps.staydecent.com.tabletoptally.models.GameModel;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by aunger on 2016-05-27.
 */
public class PlayerScoresRealmAdapter
        extends RealmBasedRecyclerViewAdapter<ScoreModel, PlayerScoresRealmAdapter.ViewHolder> {

    Context mContext;
    String mWinnerName;

    public PlayerScoresRealmAdapter(
            Context context,
            RealmResults<ScoreModel> realmResults,
            String winnerName,
            boolean automaticUpdate,
            boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
        mContext = context;
        mWinnerName = winnerName;
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.player_score_item_view, viewGroup, false);
        return new ViewHolder((FrameLayout) v);
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final ScoreModel score = realmResults.get(position);
        String winnerName = score.getWinner();

        if (Objects.equals(mWinnerName, winnerName)) {
            viewHolder.scoreWinnerTextView.setText(
                    String.format("Won against %s", getOtherPlayers(score, winnerName)));
            viewHolder.scoreWinnerTextView.setTextColor(
                    ContextCompat.getColor(mContext, R.color.colorPrimaryAlt1));
        } else {
            viewHolder.scoreWinnerTextView.setText(String.format("Lost to %s", winnerName));
            viewHolder.scoreWinnerTextView.setTextColor(
                    ContextCompat.getColor(mContext, R.color.colorPrimaryAlt4));
        }
    }

    private String getOtherPlayers(ScoreModel score, String winner) {
        ArrayList<String> allPlayers = ScoreHelper.splitPlayersFromScore(score);
        ArrayList<String> otherPlayers = Lists.newArrayList(
                Collections2.filter(allPlayers, Predicates.not(Predicates.containsPattern(winner))));
        return Joiner.on(", ").join(otherPlayers);
    }


    public class ViewHolder extends RealmViewHolder {
        public GameModel game;

        @Bind(R.id.player_score_winner)
        TextView scoreWinnerTextView;

        public ViewHolder(FrameLayout container) {
            super(container);
            ButterKnife.bind(this, container);
        }
    }

}
