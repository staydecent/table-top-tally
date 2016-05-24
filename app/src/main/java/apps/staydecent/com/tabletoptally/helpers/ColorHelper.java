package apps.staydecent.com.tabletoptally.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

import apps.staydecent.com.tabletoptally.R;

/**
 * Created by aunger on 2016-05-24.
 */
public class ColorHelper {

    public List<Integer> colors = Arrays.asList(
            R.color.colorPrimaryDark,
            R.color.colorPrimaryAlt1,
            R.color.colorPrimaryAlt2,
            R.color.colorPrimaryAlt3,
            R.color.colorPrimaryAlt4);

    private Context context;

    public ColorHelper(Context context) {
        this.context = context;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public int getColorFromPosition(int position) {
        int index = 0;
        int max = colors.size(); // 5

        if (position < max) {
            index = position;
        } else {
            index = position - max;

            // if position is >= max*2
            if (index >= max) {
                int factor = index / max;
                index = index - (factor * max);
            }
        }

        return ContextCompat.getColor(context, colors.get(index));
    }
}
