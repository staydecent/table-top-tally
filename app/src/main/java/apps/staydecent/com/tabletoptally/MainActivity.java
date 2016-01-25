package apps.staydecent.com.tabletoptally;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import apps.staydecent.com.tabletoptally.models.Game;
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

public class MainActivity extends AppCompatActivity {

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

        resetRealm();
        realm = Realm.getInstance(this);
        games = realm
                .where(Game.class)
                .findAllSorted("id", Sort.ASCENDING);

        GameRealmAdapter gameRealmAdapter = new GameRealmAdapter(this, games, true, true);
        rvGames.setAdapter(gameRealmAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void buildAndShowInputDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add A Game");

        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.game_dialog_view, null);
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

    private void resetRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    public class GameRealmAdapter
            extends RealmBasedRecyclerViewAdapter<Game, GameRealmAdapter.ViewHolder> {

        public GameRealmAdapter(
                Context context,
                RealmResults<Game> realmResults,
                boolean automaticUpdate,
                boolean animateResults) {
            super(context, realmResults, automaticUpdate, animateResults);
        }

        public class ViewHolder extends RealmViewHolder {
            public TextView gameTextView;
            public CardView cardView;
            public Game game;

            public ViewHolder(FrameLayout container) {
                super(container);

                final ViewHolder vh = this;

                this.gameTextView = (TextView) container.findViewById(R.id.game_text_view);
                this.cardView = (CardView) container.findViewById(R.id.game_list_item);

                this.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        game = realmResults.get(vh.getAdapterPosition());
                        if (game == null || !game.isValid()) {
                            Log.d("WTF", String.format("WHATT %d", vh.getAdapterPosition()));
                            return;
                        }

                        toast(game.getName());
                        //asyncRemoveGame(game.getId());
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            View v = inflater.inflate(R.layout.game_item_view, viewGroup, false);
            ViewHolder vh = new ViewHolder((FrameLayout) v);
            return vh;
        }

        @Override
        public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
            final Game game = realmResults.get(position);
            viewHolder.gameTextView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!game.isValid()) {
                                return;
                            }
                            toast(game.getName());
                            //asyncRemoveGame(game.getId());
                        }
                    }
            );
            viewHolder.gameTextView.setText(game.getName());
        }
    }

    private void toast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void asyncRemoveGame(final long id) {
        AsyncTask<Void, Void, Void> remoteItem = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Realm instance = Realm.getInstance(MainActivity.this);
                Game game =
                        instance.where(Game.class).equalTo("id", id).findFirst();
                if (game != null) {
                    instance.beginTransaction();
                    game.removeFromRealm();
                    instance.commitTransaction();
                }
                instance.close();
                return null;
            }
        };
        remoteItem.execute();
    }
}
