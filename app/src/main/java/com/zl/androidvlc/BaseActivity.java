package com.zl.androidvlc;

import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;

/**
 * 项目名称：AndroidVLC
 * 类描述：
 * 创建人：zhanglin
 * 创建时间：2019/1/16 17:25
 * 修改人：Administrator
 * 修改时间：2019/1/16 17:25
 * 修改备注：
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(setLayoutView());
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public abstract int setLayoutView();
    public abstract void initView();
}
