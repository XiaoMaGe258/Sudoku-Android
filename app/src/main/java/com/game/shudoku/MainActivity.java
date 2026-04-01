package com.game.shudoku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 主界面：选择难度并开始游戏
 */
public class MainActivity extends AppCompatActivity {

    private RadioGroup difficultyGroup;
    private TextView tvDifficultyDesc;

    private static final String[] HINTS = {
        "简单：46个提示数字",
        "中等：36个提示数字",
        "困难：27个提示数字"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultyGroup  = findViewById(R.id.difficulty_group);
        tvDifficultyDesc = findViewById(R.id.tv_difficulty_desc);
        Button btnStart  = findViewById(R.id.btn_start);

        // 难度选择变化时更新说明文字
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int difficulty = getDifficulty(checkedId);
            tvDifficultyDesc.setText(HINTS[difficulty]);
        });

        // 初始显示
        tvDifficultyDesc.setText(HINTS[0]);

        // 开始游戏
        btnStart.setOnClickListener(v -> {
            int checkedId  = difficultyGroup.getCheckedRadioButtonId();
            int difficulty = getDifficulty(checkedId);
            Intent intent  = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        });
    }

    private int getDifficulty(int checkedId) {
        if (checkedId == R.id.rb_easy)   return 0;
        if (checkedId == R.id.rb_medium) return 1;
        if (checkedId == R.id.rb_hard)   return 2;
        return 0;
    }
}
