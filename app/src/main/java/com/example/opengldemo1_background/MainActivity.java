package com.example.opengldemo1_background;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.opengldemo1_background.activity.SimpleRenderActivity;


public class MainActivity extends Activity implements View.OnClickListener {


    Button simpleRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    void initView(){
        simpleRender = findViewById(R.id.simple_render);
        simpleRender.setOnClickListener(this::onClick);

    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.simple_render:
                intent = new Intent(MainActivity.this, SimpleRenderActivity.class);
                break;
        }
        startActivity(intent);
    }
}