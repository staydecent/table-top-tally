package apps.staydecent.com.tabletoptally.views.playerscreen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class PlayerDetailsActivity extends Activity {

    private Realm realm;
    private String playerName;
    private RealmResults<ScoreModel> scores; // scores where playerName is present

    @Bind(R.id.details_player_name)
    TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        ButterKnife.bind(this);

        playerName = getIntent().getStringExtra("playerName");
        realm = Realm.getDefaultInstance();
        scores = realm
                .where(ScoreModel.class)
                .contains("players", playerName)
                .findAll();

        mName.setText(playerName);
    }
}
