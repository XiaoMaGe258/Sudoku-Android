package com.game.shudoku;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * 游戏界面：显示棋盘、数字按钮、备注/清除控件，处理交互逻辑
 */
public class GameActivity extends AppCompatActivity {

    private SudokuGame game;
    private SudokuBoard board;
    private Button btnNote;
    private Chronometer chronometer;
    private int difficulty;

    private static final int[] NUM_BUTTON_IDS = {
        R.id.btn_1, R.id.btn_2, R.id.btn_3,
        R.id.btn_4, R.id.btn_5, R.id.btn_6,
        R.id.btn_7, R.id.btn_8, R.id.btn_9
    };

    private static final String[] DIFFICULTY_NAMES = {"简单", "中等", "困难"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        difficulty = getIntent().getIntExtra("difficulty", 0);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("数独游戏");
        }

        // 难度标签
        TextView tvDifficulty = findViewById(R.id.tv_difficulty);
        tvDifficulty.setText(DIFFICULTY_NAMES[difficulty]);

        // 计时器
        chronometer = findViewById(R.id.chronometer);

        // 初始化游戏逻辑
        game = new SudokuGame();
        game.newGame(difficulty);

        // 棋盘
        board = findViewById(R.id.sudoku_board);
        board.setGame(game);
        board.setOnCellTouchedListener((row, col) -> {
            game.selectCell(row, col);
            board.invalidate();
        });

        // 数字按钮 1~9
        for (int i = 0; i < NUM_BUTTON_IDS.length; i++) {
            final int num = i + 1;
            Button btn = findViewById(NUM_BUTTON_IDS[i]);
            btn.setOnClickListener(v -> {
                // 先设置高亮数字（无论是否选中格子都高亮）
                game.setHighlightNumber(num);
                // 向选中格子填入数字（若无选中格子则忽略）
                game.inputNumber(num);
                board.invalidate();
                checkComplete();
            });
        }

        // 备注模式切换按钮
        btnNote = findViewById(R.id.btn_note);
        btnNote.setOnClickListener(v -> {
            game.toggleNoteMode();
            updateNoteModeButton();
        });

        // 清除按钮
        Button btnErase = findViewById(R.id.btn_erase);
        btnErase.setOnClickListener(v -> {
            game.erase();
            board.invalidate();
        });

        // 新游戏按钮
        Button btnNewGame = findViewById(R.id.btn_new_game);
        btnNewGame.setOnClickListener(v -> showNewGameDialog());

        // 启动计时
        startTimer();
    }

    // ---- 计时器 ----

    private void startTimer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    // ---- 备注按钮状态更新 ----

    private void updateNoteModeButton() {
        if (game.isNoteMode()) {
            btnNote.setText(getString(R.string.note_on));
            btnNote.setAlpha(1.0f);
        } else {
            btnNote.setText(getString(R.string.note_off));
            btnNote.setAlpha(0.6f);
        }
    }

    // ---- 完成检测 ----

    private void checkComplete() {
        if (game.isComplete()) {
            chronometer.stop();
            long elapsed  = SystemClock.elapsedRealtime() - chronometer.getBase();
            long minutes  = elapsed / 60000;
            long seconds  = (elapsed % 60000) / 1000;

            String msg = "恭喜你完成了数独！\n\n"
                    + "难度：" + DIFFICULTY_NAMES[difficulty] + "\n"
                    + String.format("用时：%02d 分 %02d 秒", minutes, seconds);

            new AlertDialog.Builder(this)
                    .setTitle("🎉 完成！")
                    .setMessage(msg)
                    .setPositiveButton("再来一局", (d, w) -> restartGame())
                    .setNegativeButton("返回主页",  (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    // ---- 新游戏对话框 ----

    private void showNewGameDialog() {
        chronometer.stop();
        new AlertDialog.Builder(this)
                .setTitle("新游戏")
                .setMessage("确定要开始新游戏吗？当前进度将丢失。")
                .setPositiveButton("确定", (d, w) -> restartGame())
                .setNegativeButton("取消",  (d, w) -> chronometer.start())
                .show();
    }

    // ---- 重新开始当前难度 ----

    private void restartGame() {
        game.newGame(difficulty);
        board.invalidate();
        if (game.isNoteMode()) game.toggleNoteMode();
        updateNoteModeButton();
        startTimer();
    }

    // ---- 返回键 ----

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
