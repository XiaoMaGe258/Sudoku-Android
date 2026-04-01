package com.game.shudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 数独棋盘自定义视图
 * 负责绘制 9x9 格子、高亮、数字、备注，处理触摸事件
 */
public class SudokuBoard extends View {

    private SudokuGame game;

    // 背景与高亮画笔
    private final Paint paintBackground    = new Paint();
    private final Paint paintSelected      = new Paint();  // 选中格
    private final Paint paintRegion        = new Paint();  // 同行/列/宫
    private final Paint paintSameNum       = new Paint();  // 同数字
    private final Paint paintBoth          = new Paint();  // 同数字且同区域

    // 格线画笔
    private final Paint paintThickLine     = new Paint();
    private final Paint paintThinLine      = new Paint();

    // 数字画笔
    private final Paint paintFixed         = new Paint();  // 预填数字
    private final Paint paintUser          = new Paint();  // 用户输入
    private final Paint paintWrong         = new Paint();  // 错误数字（红色）
    private final Paint paintNote          = new Paint();  // 备注数字

    private float cellSize;

    // 触摸回调
    private OnCellTouchedListener listener;

    public interface OnCellTouchedListener {
        void onCellTouched(int row, int col);
    }

    // ---- 构造函数 ----

    public SudokuBoard(Context context) {
        super(context);
        init();
    }

    public SudokuBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SudokuBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintBackground.setColor(Color.WHITE);
        paintBackground.setStyle(Paint.Style.FILL);

        // 选中格：蓝色
        paintSelected.setColor(0xFFBBDEFB);
        paintSelected.setStyle(Paint.Style.FILL);

        // 同行/列/宫：浅蓝
        paintRegion.setColor(0xFFE3F2FD);
        paintRegion.setStyle(Paint.Style.FILL);

        // 同数字：浅绿
        paintSameNum.setColor(0xFFC8E6C9);
        paintSameNum.setStyle(Paint.Style.FILL);

        // 同数字且在区域内：中蓝
        paintBoth.setColor(0xFF90CAF9);
        paintBoth.setStyle(Paint.Style.FILL);

        // 宫格粗线
        paintThickLine.setColor(0xFF37474F);
        paintThickLine.setStyle(Paint.Style.STROKE);
        paintThickLine.setStrokeWidth(4f);
        paintThickLine.setAntiAlias(true);

        // 单元格细线
        paintThinLine.setColor(0xFFB0BEC5);
        paintThinLine.setStyle(Paint.Style.STROKE);
        paintThinLine.setStrokeWidth(1f);
        paintThinLine.setAntiAlias(true);

        // 预填数字：深黑粗体
        paintFixed.setColor(0xFF212121);
        paintFixed.setStyle(Paint.Style.FILL);
        paintFixed.setTextAlign(Paint.Align.CENTER);
        paintFixed.setFakeBoldText(true);
        paintFixed.setAntiAlias(true);

        // 用户输入：蓝色
        paintUser.setColor(0xFF1565C0);
        paintUser.setStyle(Paint.Style.FILL);
        paintUser.setTextAlign(Paint.Align.CENTER);
        paintUser.setAntiAlias(true);

        // 错误数字：红色
        paintWrong.setColor(0xFFB71C1C);
        paintWrong.setStyle(Paint.Style.FILL);
        paintWrong.setTextAlign(Paint.Align.CENTER);
        paintWrong.setAntiAlias(true);

        // 备注数字：灰色小字
        paintNote.setColor(0xFF757575);
        paintNote.setStyle(Paint.Style.FILL);
        paintNote.setTextAlign(Paint.Align.CENTER);
        paintNote.setAntiAlias(true);
    }

    // ---- 公开方法 ----

    public void setGame(SudokuGame game) {
        this.game = game;
        invalidate();
    }

    public void setOnCellTouchedListener(OnCellTouchedListener l) {
        this.listener = l;
    }

    // ---- 测量：保持正方形 ----

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 取宽高中较小值，使棋盘始终为正方形
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        // 如果高度未指定，则用宽度
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            h = w;
        }
        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    // ---- 绘制 ----

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) return;

        int boardSize = getWidth();
        cellSize = (float) boardSize / SudokuGame.SIZE;

        // 动态调整字体大小
        paintFixed.setTextSize(cellSize * 0.58f);
        paintUser.setTextSize(cellSize * 0.58f);
        paintWrong.setTextSize(cellSize * 0.58f);
        paintNote.setTextSize(cellSize * 0.27f);

        // 1. 绘制白色背景
        canvas.drawRect(0, 0, boardSize, boardSize, paintBackground);

        // 2. 绘制高亮背景
        drawHighlights(canvas);

        // 3. 绘制格线
        drawGrid(canvas);

        // 4. 绘制数字与备注
        drawNumbers(canvas);
    }

    /**
     * 绘制格子高亮：选中格、同行/列/宫、同数字
     */
    private void drawHighlights(Canvas canvas) {
        int selRow = game.getSelectedRow();
        int selCol = game.getSelectedCol();
        int hl = game.getHighlightNumber();

        for (int r = 0; r < SudokuGame.SIZE; r++) {
            for (int c = 0; c < SudokuGame.SIZE; c++) {
                boolean isSelected  = (r == selRow && c == selCol);
                boolean inRegion    = selRow >= 0 && (
                        r == selRow || c == selCol ||
                        (r / 3 == selRow / 3 && c / 3 == selCol / 3)
                );
                boolean sameNumber  = hl > 0 && game.getCellValue(r, c) == hl;

                Paint bg = null;
                if (isSelected) {
                    bg = paintSelected;
                } else if (sameNumber && inRegion) {
                    bg = paintBoth;
                } else if (sameNumber) {
                    bg = paintSameNum;
                } else if (inRegion) {
                    bg = paintRegion;
                }

                if (bg != null) {
                    float left = c * cellSize;
                    float top  = r * cellSize;
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, bg);
                }
            }
        }
    }

    /**
     * 绘制格线：3x3 宫边界用粗线，单元格用细线
     */
    private void drawGrid(Canvas canvas) {
        float size = getWidth();
        for (int i = 0; i <= SudokuGame.SIZE; i++) {
            Paint p = (i % 3 == 0) ? paintThickLine : paintThinLine;
            float pos = i * cellSize;
            canvas.drawLine(0,    pos,  size, pos,  p);  // 横线
            canvas.drawLine(pos,  0,    pos,  size, p);  // 竖线
        }
    }

    /**
     * 绘制所有格子的数字和备注
     */
    private void drawNumbers(Canvas canvas) {
        for (int r = 0; r < SudokuGame.SIZE; r++) {
            for (int c = 0; c < SudokuGame.SIZE; c++) {
                int val = game.getCellValue(r, c);
                float cx = c * cellSize + cellSize / 2f;
                float cy = r * cellSize + cellSize / 2f;

                if (val != 0) {
                    // 绘制数字
                    Paint p;
                    if (game.isFixed(r, c)) {
                        p = paintFixed;
                    } else if (!game.isCorrect(r, c)) {
                        p = paintWrong;
                    } else {
                        p = paintUser;
                    }
                    // 垂直居中补偿
                    Paint.FontMetrics fm = p.getFontMetrics();
                    float offset = -(fm.ascent + fm.descent) / 2f;
                    canvas.drawText(String.valueOf(val), cx, cy + offset, p);
                } else {
                    // 绘制备注小数字（1-9 排成 3x3）
                    drawNotes(canvas, r, c);
                }
            }
        }
    }

    /**
     * 在单元格内绘制 3x3 备注数字网格
     */
    private void drawNotes(Canvas canvas, int row, int col) {
        float noteCell = cellSize / 3f;
        Paint.FontMetrics fm = paintNote.getFontMetrics();
        float offset = -(fm.ascent + fm.descent) / 2f;

        for (int n = 1; n <= 9; n++) {
            if (game.hasNote(row, col, n)) {
                int nr = (n - 1) / 3;
                int nc = (n - 1) % 3;
                float x = col * cellSize + nc * noteCell + noteCell / 2f;
                float y = row * cellSize + nr * noteCell + noteCell / 2f;
                canvas.drawText(String.valueOf(n), x, y + offset, paintNote);
            }
        }
    }

    // ---- 触摸事件 ----

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (cellSize <= 0) return false;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (row >= 0 && row < SudokuGame.SIZE && col >= 0 && col < SudokuGame.SIZE) {
                if (listener != null) {
                    listener.onCellTouched(row, col);
                }
                return true;
            }
        }
        return false;
    }
}
