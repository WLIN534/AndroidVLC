package com.zl.androidvlc;

import android.content.Intent;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.to_player)
    Button toPlayer;

    @Override
    public int setLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {

    }

   @OnClick(R.id.to_player)
    public void readyGo(){
        Intent intent = new Intent(this,PlayerActivity.class);
        startActivity(intent);
   }
}
