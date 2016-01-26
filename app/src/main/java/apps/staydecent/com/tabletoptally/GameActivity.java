package apps.staydecent.com.tabletoptally;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.models.Game;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.Sort;

public class GameActivity extends AppCompatActivity {

    private Realm realm;
    private Game game;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @OnClick(R.id.fab)
    public void onFabClick() {
        Log.d("TTT", "FAB");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        long gameId = getIntent().getLongExtra("game_id", 0);

        realm = Realm.getInstance(this);
        game = realm
                .where(Game.class)
                .equalTo("id", gameId)
                .findFirst();

        getSupportActionBar().setTitle(game.getName());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

}
