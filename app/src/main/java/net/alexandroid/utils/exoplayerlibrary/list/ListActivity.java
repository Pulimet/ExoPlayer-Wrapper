package net.alexandroid.utils.exoplayerlibrary.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.alexandroid.utils.exoplayerlibrary.MainActivity;
import net.alexandroid.utils.exoplayerlibrary.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        serRecyclerView();
    }

    private void serRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<VideoItem> list = getVideoItemsList();

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);
        getLifecycle().addObserver(adapter);
        recyclerView.setAdapter(adapter);
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
}
