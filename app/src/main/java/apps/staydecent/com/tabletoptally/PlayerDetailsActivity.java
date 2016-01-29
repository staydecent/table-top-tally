package apps.staydecent.com.tabletoptally;

import android.os.Bundle;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class PlayerDetailsActivity extends BaseActivity {

    private String playerName;
    private RealmResults<Score> scores; // scores where playerName is present

    @Bind(R.id.details_player_name)
    TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        ButterKnife.bind(this);

        playerName = getIntent().getStringExtra("playerName");
        realm = Realm.getInstance(this);
        scores = realm
                .where(Score.class)
                .contains("players", playerName)
                .findAll();

        mName.setText(playerName);
    }
}
