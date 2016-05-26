package apps.staydecent.com.tabletoptally.views.mainscreen;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.GameModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends Activity {

    private Realm realm;
    private RealmResults<GameModel> games;
    private CoordinatorLayout.LayoutParams fabLayoutParams;
    private Bundle bundleReenterState;

    public boolean isGameActivityStarted;

    @Bind(R.id.games_recycler_view)
    RealmRecyclerView mRecyclerView;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @OnClick(R.id.fab)
    public void onFabClick() {
        buildAndShowInputDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setExitSharedElementCallback(mCallback);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();
        games = realm
                .where(GameModel.class)
                .findAllSorted("id", Sort.ASCENDING);

        GameRealmAdapter gameRealmAdapter = new GameRealmAdapter(this, games, true, true);
        mRecyclerView.setAdapter(gameRealmAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isGameActivityStarted = false;
        slideFabIn();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        Log.d("TTT", "MainActivity onActivityReenter");
        super.onActivityReenter(requestCode, data);
        bundleReenterState = new Bundle(data.getExtras());
        int startingPosition = bundleReenterState.getInt(getResources().getString(R.string.extra_starting_game_position));
        int currentPosition = bundleReenterState.getInt(getResources().getString(R.string.extra_current_game_position));
        if (startingPosition != currentPosition) {
            mRecyclerView.smoothScrollToPosition(currentPosition);
        }
        postponeEnterTransition();
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mRecyclerView.requestLayout();
                startPostponedEnterTransition();
                return true;
            }
        });
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
        GameModel game = realm.createObject(GameModel.class);
        game.setId(System.currentTimeMillis());
        game.setName(gameName);
        realm.commitTransaction();
        mRecyclerView.smoothScrollToPosition(games.size() - 1);
    }

    private void toast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    protected void slideFabOut() {
        if (fabLayoutParams == null) {
            fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        }
        int fabBottomMargin = fabLayoutParams.bottomMargin;
        fab.animate()
                .translationY(fabBottomMargin + fab.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(200);
    }

    protected void slideFabIn() {
        fab.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(200);
    }

    private final SharedElementCallback mCallback = new SharedElementCallback() {

        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (bundleReenterState != null) {
                int startingPosition = bundleReenterState.getInt(getResources().getString(R.string.extra_starting_game_position));
                int currentPosition = bundleReenterState.getInt(getResources().getString(R.string.extra_current_game_position));

                if (startingPosition != currentPosition) {
                    // If startingPosition != currentPosition the user must have swiped to a
                    // different page in the DetailsActivity. We must update the shared element
                    // so that the correct one falls into place.
                    String newTransitionName = String.format(
                            getResources().getString(R.string.tag_name_tpl),
                            currentPosition);
                    View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);
                    if (newSharedElement != null) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }

                bundleReenterState = null;
            } else {
                // the activity is exiting.
                View navigationBar = findViewById(android.R.id.navigationBarBackground);
                View statusBar = findViewById(android.R.id.statusBarBackground);
                if (navigationBar != null) {
                    names.add(navigationBar.getTransitionName());
                    sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                }
                if (statusBar != null) {
                    names.add(statusBar.getTransitionName());
                    sharedElements.put(statusBar.getTransitionName(), statusBar);
                }

                slideFabOut();
            }
        }
    };

}
