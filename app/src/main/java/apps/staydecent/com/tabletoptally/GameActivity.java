package apps.staydecent.com.tabletoptally;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;

import apps.staydecent.com.tabletoptally.models.Game;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;

public class GameActivity extends AppCompatActivity {

    private Realm realm;
    private Game game;
    private Score score;
    private RealmResults<Score> scores;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.scores_recycler_view)
    RealmRecyclerView rvScores;

    @OnClick(R.id.fab)
    public void onFabClick() {
        buildAndShowPlayersDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        long gameId = getIntent().getLongExtra("game_id", 0);

        resetRealm();
        realm = Realm.getInstance(this);
        game = realm
                .where(Game.class)
                .equalTo("id", gameId)
                .findFirst();

        scores = realm
                .where(Score.class)
                .findAllSorted("id", Sort.ASCENDING);

        getSupportActionBar().setTitle(game.getName());

        ScoreRealmAdapter scoreRealmAdapter = new ScoreRealmAdapter(this, scores, true, true);
        rvScores.setAdapter(scoreRealmAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private void buildAndShowPlayersDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle("Who played this game?");

        // Views
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.score_players_dialog_view, null);
        final AutoCompleteTextView input = (AutoCompleteTextView) dialogView.findViewById(R.id.input);

        // Get AutoComplete options from Realm
        RealmResults<Score> scores = realm
                .where(Score.class)
                .findAllSorted("id", Sort.ASCENDING);
        ArrayList<String> playerNames = new ArrayList<>(0);
        for (Score score : scores) {
            playerNames.addAll(splitPlayersFromScore(score));
        }

        // Create the AutoCompleteTextView adapter
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_list_item_1, playerNames);
        input.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.show();
        input.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE ||
                                (event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            Log.d("TTT", input.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    private ArrayList<String> splitPlayersFromScore(Score score) {
        Iterable<String> namesIterable = Splitter.on("; ")
                .trimResults()
                .omitEmptyStrings()
                .split(score.getPlayers());
        return Lists.newArrayList(namesIterable);
    }

    public class ScoreRealmAdapter
            extends RealmBasedRecyclerViewAdapter<Score, ScoreRealmAdapter.ViewHolder> {

        public ScoreRealmAdapter(
                Context context,
                RealmResults<Score> realmResults,
                boolean automaticUpdate,
                boolean animateResults) {
            super(context, realmResults, automaticUpdate, animateResults);
        }

        public class ViewHolder extends RealmViewHolder {
            public Score score;

            @Bind(R.id.score_text_view)
            TextView scoreTextView;

            @Bind(R.id.score_list_item)
            CardView scoreCardView;

            public ViewHolder(FrameLayout container) {
                super(container);
                ButterKnife.bind(this, container);

                final ViewHolder vh = this;

                scoreCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        score = realmResults.get(vh.getAdapterPosition());
                        if (score == null || !score.isValid()) {
                            return;
                        }
                    }
                });

                scoreCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        score = realmResults.get(vh.getAdapterPosition());
                        if (score == null || !score.isValid()) {
                            return false;
                        }
                        return true;
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            View v = inflater.inflate(R.layout.score_item_view, viewGroup, false);
            return new ViewHolder((FrameLayout) v);
        }

        @Override
        public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
            final Score score = realmResults.get(position);
            viewHolder.scoreTextView.setText(score.getGame().getName());
        }
    }

}
