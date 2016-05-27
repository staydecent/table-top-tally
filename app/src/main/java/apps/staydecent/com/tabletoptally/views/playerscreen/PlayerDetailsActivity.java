package apps.staydecent.com.tabletoptally.views.playerscreen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.GameModel;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PlayerDetailsActivity extends Activity {

    private Realm realm;
    private String playerName;
    private long gameId;
    private RealmResults<ScoreModel> scores; // scores where playerName is present

    @Bind(R.id.player_text_view)
    TextView playerNameTextView;

    @Bind(R.id.game_text_view)
    TextView gameNameTextView;

    @Bind(R.id.player_scores_recycler_view)
    RealmRecyclerView playerScoresRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        playerName = getIntent().getStringExtra("playerName");
        playerNameTextView.setText(playerName);

        int color = getIntent().getIntExtra("color", 0);
        if (color != 0) {
            gameNameTextView.setTextColor(color);
        }

        gameId = getIntent().getLongExtra("gameId", 0);
        if (gameId != 0) {
            GameModel game = realm.where(GameModel.class).equalTo("id", gameId).findFirst();
            gameNameTextView.setText(String.format(
                    getResources().getString(R.string.player_details_subheading_tpl), game.getName()));
        }

        scores = realm
                .where(ScoreModel.class)
                .equalTo("game.id", gameId)
                .contains("players", playerName)
                .findAllSorted("id", Sort.ASCENDING);

        PlayerScoresRealmAdapter playerScoresAdapter = new PlayerScoresRealmAdapter(this, scores, playerName, true, true);
        playerScoresRecyclerView.setAdapter(playerScoresAdapter);
    }
}
