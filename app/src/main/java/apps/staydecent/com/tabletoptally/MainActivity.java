package apps.staydecent.com.tabletoptally;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import apps.staydecent.com.tabletoptally.adapters.GameRealmAdapter;
import apps.staydecent.com.tabletoptally.models.Game;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends BaseActivity {

    private Realm realm;
    private RealmResults<Game> games;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @OnClick(R.id.fab)
    public void onFabClick() {
        buildAndShowInputDialog();
    }

    @Bind(R.id.games_recycler_view)
    RealmRecyclerView rvGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        realm = Realm.getInstance(this);
        games = realm
                .where(Game.class)
                .findAllSorted("id", Sort.ASCENDING);

        GameRealmAdapter gameRealmAdapter = new GameRealmAdapter(this, games, true, true);
        rvGames.setAdapter(gameRealmAdapter);
    }

    private void buildAndShowInputDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.title_dialog_add_game));

        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_game_add, null);
        final EditText input = (EditText) dialogView.findViewById(R.id.input);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addGame(input.getText().toString());
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
                            dialog.dismiss();
                            addGame(input.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void addGame(String gameName) {
        if (gameName == null || gameName.length() == 0) {
            toast("Empty games are no fun!");
            return;
        }

        realm.beginTransaction();
        Game game = realm.createObject(Game.class);
        game.setId(System.currentTimeMillis());
        game.setName(gameName);
        realm.commitTransaction();
        rvGames.smoothScrollToPosition(games.size() - 1);
    }

    private void toast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
