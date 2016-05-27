package apps.staydecent.com.tabletoptally.views.playerscreen;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.helpers.ColorHelper;
import apps.staydecent.com.tabletoptally.models.GameModel;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class PlayerDetailsActivity extends Activity {

    private Realm realm;
    private String playerName;
    private long gameId;
    private RealmResults<ScoreModel> scores; // scores where playerName is present

    @Bind(R.id.player_details_container)
    LinearLayout mContainer;

    @Bind(R.id.player_text_view)
    TextView mPlayerName;

    @Bind(R.id.game_text_view)
    TextView mGameName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        playerName = getIntent().getStringExtra("playerName");
        mPlayerName.setText(playerName);

        int color = getIntent().getIntExtra("color", 0);
        if (color != 0) {
            mGameName.setBackgroundColor(color);
            mContainer.setBackgroundColor(color);
        }

        gameId = getIntent().getLongExtra("gameId", 0);
        if (gameId != 0) {
            GameModel game = realm.where(GameModel.class).equalTo("id", gameId).findFirst();
            mGameName.setText(game.getName());
        }

        scores = realm
                .where(ScoreModel.class)
                .contains("players", playerName)
                .findAll();
    }
}
