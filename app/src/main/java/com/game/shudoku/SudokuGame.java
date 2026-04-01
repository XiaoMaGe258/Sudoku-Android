package com.game.shudoku;

import java.util.Arrays;

/**
 * 数独游戏状态管理
 * 负责存储棋盘状态、选中格子、备注数字、高亮逻辑
 */
public class SudokuGame {

    public static final int SIZE = 9;

    private int[][] solution;       // 完整正确答案
    private int[][] userGrid;       // 用户当前填写状态（含预填数字）
    private boolean[][][] notes;    // 备注 notes[row][col][num-1]
    private boolean[][] isFixed;    // 是否为预填数字

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean noteMode = false;
    private int highlightNumber = 0;  // 当前高亮数字（0=不高亮）
    private int difficulty = 0;

    /**
     * 开始新游戏
     * @param difficulty 0=简单, 1=中等, 2=困难
     */
    public void newGame(int difficulty) {
        this.difficulty = difficulty;
        SudokuGenerator generator = new SudokuGenerator();
        solution = generator.generateSolution();
        int[][] puzzle = generator.generatePuzzle(solution, difficulty);

        userGrid = new int[SIZE][SIZE];
        notes = new boolean[SIZE][SIZE][SIZE];
        isFixed = new boolean[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                userGrid[i][j] = puzzle[i][j];
                isFixed[i][j] = (puzzle[i][j] != 0);
            }
        }

        selectedRow = -1;
        selectedCol = -1;
        noteMode = false;
        highlightNumber = 0;
    }

    /**
     * 选中一个格子
     */
    public void selectCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            selectedRow = -1;
            selectedCol = -1;
            return;
        }
        selectedRow = row;
        selectedCol = col;
        int val = userGrid[row][col];
        if (val != 0) {
            highlightNumber = val;
        } else {
            highlightNumber = 0;
        }
    }

    /**
     * 向选中格子输入数字
     * 普通模式：填写数字并清除该格备注
     * 备注模式：切换该数字的备注状态
     */
    public void inputNumber(int num) {
        if (selectedRow < 0 || selectedCol < 0) return;
        if (isFixed[selectedRow][selectedCol]) return;

        if (noteMode) {
            notes[selectedRow][selectedCol][num - 1] = !notes[selectedRow][selectedCol][num - 1];
        } else {
            userGrid[selectedRow][selectedCol] = num;
            Arrays.fill(notes[selectedRow][selectedCol], false);
            highlightNumber = num;
        }
    }

    /**
     * 清除选中格子内容（数字+备注）
     */
    public void erase() {
        if (selectedRow < 0 || selectedCol < 0) return;
        if (isFixed[selectedRow][selectedCol]) return;
        userGrid[selectedRow][selectedCol] = 0;
        Arrays.fill(notes[selectedRow][selectedCol], false);
        highlightNumber = 0;
    }

    /**
     * 切换备注模式
     */
    public void toggleNoteMode() {
        noteMode = !noteMode;
    }

    /**
     * 检查是否完成（所有格子填写正确）
     */
    public boolean isComplete() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (userGrid[i][j] != solution[i][j]) return false;
            }
        }
        return true;
    }

    /**
     * 检查某格子填写是否正确（空格视为正确）
     */
    public boolean isCorrect(int row, int col) {
        int val = userGrid[row][col];
        return val == 0 || val == solution[row][col];
    }

    /**
     * 获取某格子的备注状态
     */
    public boolean hasNote(int row, int col, int num) {
        return notes[row][col][num - 1];
    }

    // ---- Getters / Setters ----

    public int getCellValue(int row, int col) {
        return userGrid[row][col];
    }

    public boolean isFixed(int row, int col) {
        return isFixed[row][col];
    }

    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    public boolean isNoteMode() { return noteMode; }
    public int getHighlightNumber() { return highlightNumber; }
    public int getDifficulty() { return difficulty; }

    public void setHighlightNumber(int num) {
        highlightNumber = num;
    }
}
