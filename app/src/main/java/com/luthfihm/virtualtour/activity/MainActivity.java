package com.luthfihm.virtualtour.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.luthfihm.virtualtour.R;

public class MainActivity extends AppCompatActivity {

    private Button ARViewButton;
    private Button trainModelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ARViewButton = (Button) findViewById(R.id.ARViewButton);
        trainModelButton = (Button) findViewById(R.id.trainModelButton);

        ARViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ARViewActivity.class);
                startActivity(intent);
            }
        });

        trainModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrainModelActivity.class);
                startActivity(intent);
            }
        });
    }
}
