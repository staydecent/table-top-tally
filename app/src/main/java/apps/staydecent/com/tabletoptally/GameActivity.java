package apps.staydecent.com.tabletoptally;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import apps.staydecent.com.tabletoptally.adapters.ScoreAdapter;
import apps.staydecent.com.tabletoptally.models.Game;
import apps.staydecent.com.tabletoptally.models.Score;
import apps.staydecent.com.tabletoptally.views.LineDividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GameActivity extends BaseActivity {

    private Realm realm;
    private Game game;
    private ScoreAdapter scoreAdapter;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.scores_recycler_view)
    RecyclerView rvScores;

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

        // Load data from Realm
        long gameId = getIntent().getLongExtra("game_id", 0);
        realm = Realm.getInstance(this);
        game = realm
                .where(Game.class)
                .equalTo("id", gameId)
                .findFirst();

        // Set toolbar title
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(game.getName());

        // Setup RecyclerView
        rvScores.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvScores.setLayoutManager(mLayoutManager);
        rvScores.addItemDecoration(new LineDividerItemDecoration(this));
        scoreAdapter = new ScoreAdapter(this, realm, gameId);
        rvScores.setAdapter(scoreAdapter);
    }

    private void buildAndShowPlayersDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getResources().getString(R.string.title_dialog_players));

        // Views
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.score_players_dialog_view, null);
        final AutoCompleteTextView input = (AutoCompleteTextView) dialogView.findViewById(R.id.input);
        final TextView tvNames = (TextView) dialogView.findViewById(R.id.names);
        final Button btnAdd = (Button) dialogView.findViewById(R.id.btn_add);

        // Get AutoComplete options from Realm
        RealmResults<Score> scores = realm
                .where(Score.class)
                .findAllSorted("id", Sort.ASCENDING);
        ArrayList<String> existingNames = new ArrayList<>(0);
        for (Score score : scores) {
            existingNames.addAll(splitPlayersFromScore(score));
        }
        // remove duplicate names
        Set<String> namesSet = new LinkedHashSet<>(existingNames);
        existingNames = new ArrayList<>(namesSet);

        // Store entered player names in list
        final ArrayList<String> playerNames = new ArrayList<>(0);

        // Create the AutoCompleteTextView adapter
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_list_item_1, existingNames);
        input.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If text is currently entered (not submitted) save it
                String newName = input.getText().toString();
                if (!Strings.isNullOrEmpty(newName)) {
                    playerNames.add(input.getText().toString());
                }
                // ... Then dismiss
                dialog.dismiss();
                buildAndShowWinnerDialog(playerNames);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String newName = input.getText().toString();
                playerNames.add(newName);
                joinTextViewString(tvNames, newName);
                input.setText("");
            }
        });

        final AlertDialog dialog = builder.show();
        input.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (isInputDone(actionId, event)) {
                            String newName = input.getText().toString();
                            playerNames.add(newName);
                            joinTextViewString(tvNames, newName);
                            input.setText("");
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void buildAndShowWinnerDialog(final ArrayList<String> playerNames) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getResources().getString(R.string.title_dialog_winner));

        // Views
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.scores_winner_dialog_view, null);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.winner_spinner);

        // Setup Spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                GameActivity.this,
                android.R.layout.simple_spinner_item,
                playerNames);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Input events
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                addScore(game, playerNames, spinner.getSelectedItem().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private ArrayList<String> splitPlayersFromScore(Score score) {
        Iterable<String> namesIterable = Splitter.on(", ")
                .trimResults()
                .omitEmptyStrings()
                .split(score.getPlayers());
        return Lists.newArrayList(namesIterable);
    }

    private void joinTextViewString(TextView tv, String str) {
        String prevStr = tv.getText().toString();
        if (Strings.isNullOrEmpty(prevStr)) {
            tv.setText(str);
        } else {
            String newStr = prevStr + ", " + str;
            tv.setText(newStr);
        }
    }

    private boolean isInputDone(int actionId, KeyEvent event) {
        return (actionId == EditorInfo.IME_ACTION_DONE ||
                (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER));
    }

    private void addScore(Game game, ArrayList<String> players, String winner) {
        realm.beginTransaction();
        Score score = realm.createObject(Score.class);
        score.setId(System.currentTimeMillis());
        score.setWinner(winner);
        score.setPlayers(Joiner.on(", ").join(players));
        score.setGame(game);
        realm.commitTransaction();
        scoreAdapter.loadDataAndNotifyAdapter();
    }

}
