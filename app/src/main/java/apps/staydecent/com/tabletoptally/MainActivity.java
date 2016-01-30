package apps.staydecent.com.tabletoptally;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import apps.staydecent.com.tabletoptally.models.Game;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
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

    private void buildAndShowDeleteDialog(final Game game) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        String title = getResources().getString(R.string.confirm_delete_game_tpl);
        builder.setTitle(String.format(title, game.getName()));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toast(String.format("OK, deleting %s!", game.getName()));
                asyncRemoveGame(game.getId());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.show();
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

    public class GameRealmAdapter
            extends RealmBasedRecyclerViewAdapter<Game, GameRealmAdapter.ViewHolder> {

        public int colorIndex = 0;
        public List<Integer> colors = Arrays.asList(
                R.color.colorPrimaryDark,
                R.color.colorPrimaryAlt1,
                R.color.colorPrimaryAlt2,
                R.color.colorPrimaryAlt3,
                R.color.colorPrimaryAlt4);

        public GameRealmAdapter(
                Context context,
                RealmResults<Game> realmResults,
                boolean automaticUpdate,
                boolean animateResults) {
            super(context, realmResults, automaticUpdate, animateResults);
        }

        public class ViewHolder extends RealmViewHolder {
            public Game game;

            @Bind(R.id.game_text_view)
            TextView gameTextView;

            @Bind(R.id.game_list_item)
            CardView cardView;

            public ViewHolder(FrameLayout container) {
                super(container);
                ButterKnife.bind(this, container);

                final ViewHolder vh = this;

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        game = realmResults.get(vh.getAdapterPosition());
                        if (game == null || !game.isValid()) {
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra("game_id", game.getId());
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    }
                });

                cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        game = realmResults.get(vh.getAdapterPosition());
                        if (game == null || !game.isValid()) {
                            return false;
                        }
                        buildAndShowDeleteDialog(game);
                        return true;
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            View v = inflater.inflate(R.layout.game_item_view, viewGroup, false);
            return new ViewHolder((FrameLayout) v);
        }

        @Override
        public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
            final Game game = realmResults.get(position);
            viewHolder.gameTextView.setText(game.getName());
            viewHolder.cardView.setCardBackgroundColor(getNextColor());
        }

        private int getNextColor() {
            if (colorIndex >= colors.size() - 1) {
                colorIndex = 0;
            } else {
                colorIndex = colorIndex + 1;
            }

            return ContextCompat.getColor(getBaseContext(), colors.get(colorIndex));
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
                    RealmResults<Score> scores =
                        instance.where(Score.class).equalTo("game.id", game.getId()).findAll();
                    instance.beginTransaction();
                    scores.clear();
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
