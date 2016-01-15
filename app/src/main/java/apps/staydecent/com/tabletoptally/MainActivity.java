package apps.staydecent.com.tabletoptally;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private GameCollection mGameCollection;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.content)
    RelativeLayout mContent;

    @Bind(R.id.list_of_games)
    RecyclerView listOfGames;

    @OnClick(R.id.fab)
    public void sayHello() {
        String text = getString(R.string.default_toast);
        toast(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        listOfGames.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listOfGames.setLayoutManager(mLayoutManager);

        mGameCollection = new GameCollection();
        GameAdapter mGameAdapter = new GameAdapter(mGameCollection.all());
        listOfGames.setAdapter(mGameAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listOfGames.addOnItemTouchListener(new RecyclerItemClickListener(this,
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

    private void toast(String text) {
        Snackbar.make(mContent, text, Snackbar.LENGTH_LONG).show();
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
