package net.alexandroid.utils.exoplayerlibrary.list;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.alexandroid.utils.exoplayerlibrary.R;

import java.lang.reflect.Array;
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

        ArrayList<String> list = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(list);

        recyclerView.setAdapter(adapter);
    }
}
