package net.alexandroid.utils.exoplayerlibrary.list;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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

        ArrayList<String> list = new ArrayList<>(Arrays.asList(
                "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/071017_airbos_pjAO8E9r_800.mp4/master.m3u8",
                "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/arnav_fpo8g7XF_800.mp4/master.m3u8",
                "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/031017_yadiout_sodani_subs_fix_WATXm9aJ_800.mp4/master.m3u8",
                "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/061017_2225_Harrier_performs_Reverse_Landing_TWyVXwlk_800.mp4/master.m3u8",
                "http://ynethd-i.akamaihd.net/i/cdnwiz/0317/120317_fix_must_berlin_fix_yarden_ysIlzhaw_800.mp4/master.m3u8"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);

        recyclerView.setAdapter(adapter);
    }
}
