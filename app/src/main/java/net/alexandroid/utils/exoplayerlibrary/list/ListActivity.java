package net.alexandroid.utils.exoplayerlibrary.list;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import net.alexandroid.utils.exoplayerlibrary.MainActivity;
import net.alexandroid.utils.exoplayerlibrary.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ListActivity extends AppCompatActivity {

    private RecyclerViewAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        serRecyclerView();
    }

    private void serRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new SliderLayoutManager(this));

        ArrayList<VideoItem> list = getVideoItemsList();

        mAdapter = new RecyclerViewAdapter(list, mToolbar);
        getLifecycle().addObserver(mAdapter);
        recyclerView.setAdapter(mAdapter);
    }

    @NonNull
    private ArrayList<VideoItem> getVideoItemsList() {
        return new ArrayList<>(Arrays.asList(
                new VideoItem(MainActivity.SAMPLE_1,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_2,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_4,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_5,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_6,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_7,
                        MainActivity.THUMB_IMG_URL),
                new VideoItem(MainActivity.SAMPLE_3,
                        MainActivity.THUMB_IMG_URL))
        );
    }


    @Override
    public void onBackPressed() {
        if (mAdapter.onBack()) {
            return;
        }
        super.onBackPressed();
    }
}
