package com.adgvcxz.path;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.adgvcxz.path.view.PathGroup;

public class MyActivity extends Activity {
    private static final String[] CONTENT = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n"};
    private static final String[] ADAPTER = {"item1", "item2", "item3", "item4", "item5"};
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, CONTENT));
        final PathGroup pathGroup = (PathGroup) findViewById(R.id.path_group);
        pathGroup.setAdapter(new ArrayAdapter<String>(this, R.layout.item, ADAPTER));
        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return pathGroup.getIsShow();
            }
        });
        pathGroup.setOnPathGroupListener(new PathGroup.OnPathGroupListener() {
            @Override
            public void onItemClick(int index) {
                Toast.makeText(MyActivity.this, ADAPTER[index], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPathHide(int index) {

            }
        });
    }
}
