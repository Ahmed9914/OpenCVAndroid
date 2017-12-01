package au.edu.itc539.opencvandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duane on 30/11/2017.
 */

public class ProgramActivity extends Activity {

    ListView lv;
    Context context;
    private static final String TAG = "ProgramActivity";

    static Map<String, Integer> data;

    static {
        data = new HashMap<>();
        data.put("Avocado", R.drawable.unidentified);
        data.put("Banana", R.drawable.unidentified);
        data.put("Orange", R.drawable.unidentified);
        data.put("Strawberry", R.drawable.unidentified);
    }

    static Map<String, Integer> info;

    static {
        info = new HashMap<>();
        info.put("Avocado", R.drawable.avocado);
        info.put("Banana", R.drawable.banana);
        info.put("Orange", R.drawable.orange);
        info.put("Strawberry", R.drawable.strawberry);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "Called onCreate of ProgramActivity...");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.show_program);

        context = this;

        lv = findViewById(R.id.listView);

        lv.setAdapter(new CustomAdapter(data));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

}
