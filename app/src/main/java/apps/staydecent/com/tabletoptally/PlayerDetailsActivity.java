package apps.staydecent.com.tabletoptally;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewTreeObserver;
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

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }

        playerName = getIntent().getStringExtra("playerName");
        realm = Realm.getInstance(this);
        scores = realm
                .where(Score.class)
                .contains("players", playerName)
                .findAll();

        mName.setText(playerName);
    }
}
