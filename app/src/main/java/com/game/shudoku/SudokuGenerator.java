package com.game.shudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 数独题目生成器
 * 使用回溯法生成完整解，然后按难度挖空
 */
public class SudokuGenerator {

    private static final int SIZE = 9;
    private int[][] board;
    private final Random random;

    public SudokuGenerator() {
        random = new Random();
    }

    /**
     * 生成完整的数独解
     */
    public int[][] generateSolution() {
        board = new int[SIZE][SIZE];
        fillBoard(0, 0);
        return board;
    }

    /**
     * 根据难度从完整解生成题目（挖空）
     * @param solution 完整解
     * @param difficulty 0=简单, 1=中等, 2=困难
     * @return 题目（0 表示空格）
     */
    public int[][] generatePuzzle(int[][] solution, int difficulty) {
        int[][] puzzle = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, SIZE);
        }

        // 简单:挖30个格(留51个) / 中等:挖45个格(留36个) / 困难:挖54个格(留27个)
        int cellsToRemove;
        switch (difficulty) {
            case 0:  cellsToRemove = 30; break;
            case 1:  cellsToRemove = 45; break;
            case 2:  cellsToRemove = 54; break;
            default: cellsToRemove = 30;
        }

        removeNumbers(puzzle, cellsToRemove);
        return puzzle;
    }

    // ---- 内部方法 ----

    private boolean fillBoard(int row, int col) {
        if (row == SIZE) return true;

        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SIZE - 1) ? 0 : col + 1;

        if (board[row][col] != 0) {
            return fillBoard(nextRow, nextCol);
        }

        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) nums.add(i);
        Collections.shuffle(nums, random);

        for (int num : nums) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoard(nextRow, nextCol)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int[][] grid, int row, int col, int num) {
        // 检查行
        for (int j = 0; j < SIZE; j++) {
            if (grid[row][j] == num) return false;
        }
        // 检查列
        for (int i = 0; i < SIZE; i++) {
            if (grid[i][col] == num) return false;
        }
        // 检查 3x3 方块
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[boxRow + i][boxCol + j] == num) return false;
            }
        }
        return true;
    }

    private void removeNumbers(int[][] puzzle, int count) {
        // 随机打乱所有格子索引，顺序挖空
        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < SIZE * SIZE; i++) cells.add(i);
        Collections.shuffle(cells, random);

        int removed = 0;
        for (int cell : cells) {
            if (removed >= count) break;
            int row = cell / SIZE;
            int col = cell % SIZE;
            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                removed++;
            }
        }
    }
}
