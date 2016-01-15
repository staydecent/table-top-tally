package apps.staydecent.com.tabletoptally;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private GameCollection mGameCollection;

    private GameAdapter mGameAdapter;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.content)
    RelativeLayout mContent;

    @Bind(R.id.list_of_games)
    RecyclerView rvGames;

    @OnClick(R.id.fab)
    public void addGame() {
        // TODO: Load up a fullscreen dialog for entering text
        Game newGame = new Game("Some New Game");
        mGameCollection.add(newGame);
        mGameAdapter.notifyItemInserted(mGameCollection.size() - 1);
        rvGames.scrollToPosition(mGameAdapter.getItemCount() - 1);
        toast(getString(R.string.toast_new_game));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        rvGames.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvGames.setLayoutManager(mLayoutManager);

        mGameCollection = new GameCollection();
        mGameAdapter = new GameAdapter(mGameCollection);
        rvGames.setAdapter(mGameAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rvGames.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new OnGameClickListener()));
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

    private void toast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private class OnGameClickListener extends RecyclerItemClickListener.SimpleOnItemClickListener {

        @Override
        public void onItemClick(View childView, int position) {
            Game mGame = mGameCollection.get(position);
            Resources res = getResources();
            String text = String.format(res.getString(R.string.game_click_tpl), mGame.getName());
            toast(text);
        }

    }
}
