package edu.susu.scrabble;

import android.graphics.Color;

// Класс Board создаёт игровую доску Scrabble и задаёт её функциональность.
public class Board {
    Cell[][] cellMatrix; // Матрица ячеек, представляющая доску.

    public Board() {
        createBoard(); // Создание матрицы ячеек.
        connectCells(); // Установка связей между ячейками.
        createBonus(); // Расстановка бонусных ячеек.
    }

    // Метод создаёт игровую доску в виде массива 15x15 ячеек.
    public void createBoard() {
        cellMatrix = new Cell[15][15]; // Инициализируем матрицу размером 15x15.

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                cellMatrix[i][j] = new Cell(); // Каждая ячейка создаётся как объект класса Cell.
            }
        }
    }

    // Метод устанавливает связи между ячейками (соседями).
    public void connectCells() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                connectTop(i, j, cellMatrix); // Связь с верхней ячейкой.
                connectLeft(i, j, cellMatrix); // Связь с левой ячейкой.
                connectRight(i, j, cellMatrix); // Связь с правой ячейкой.
                connectBottom(i, j, cellMatrix); // Связь с нижней ячейкой.
            }
        }
    }

    // Метод задаёт бонусные ячейки на доске.
    public void createBonus() {
        // Центр доски (стартовая ячейка).
        cellMatrix[7][7].setBonus("X");

        // Тройные слова (Triple Word).
        cellMatrix[0][0].setBonus("TW");
        cellMatrix[7][0].setBonus("TW");
        cellMatrix[14][0].setBonus("TW");
        cellMatrix[0][7].setBonus("TW");
        cellMatrix[0][14].setBonus("TW");
        cellMatrix[7][14].setBonus("TW");
        cellMatrix[14][7].setBonus("TW");
        cellMatrix[14][14].setBonus("TW");

        // Тройные буквы (Triple Letter).
        cellMatrix[5][1].setBonus("TL");
        cellMatrix[9][1].setBonus("TL");
        cellMatrix[1][5].setBonus("TL");
        cellMatrix[5][5].setBonus("TL");
        cellMatrix[9][5].setBonus("TL");
        cellMatrix[13][5].setBonus("TL");
        cellMatrix[1][9].setBonus("TL");
        cellMatrix[5][9].setBonus("TL");
        cellMatrix[9][9].setBonus("TL");
        cellMatrix[13][9].setBonus("TL");
        cellMatrix[5][13].setBonus("TL");
        cellMatrix[9][13].setBonus("TL");

        // Двойные слова (Double Word).
        cellMatrix[1][1].setBonus("DW");
        cellMatrix[2][2].setBonus("DW");
        cellMatrix[3][3].setBonus("DW");
        cellMatrix[4][4].setBonus("DW");
        cellMatrix[4][10].setBonus("DW");
        cellMatrix[3][11].setBonus("DW");
        cellMatrix[2][12].setBonus("DW");
        cellMatrix[1][13].setBonus("DW");
        cellMatrix[13][1].setBonus("DW");
        cellMatrix[12][2].setBonus("DW");
        cellMatrix[11][3].setBonus("DW");
        cellMatrix[10][4].setBonus("DW");
        cellMatrix[10][10].setBonus("DW");
        cellMatrix[11][11].setBonus("DW");
        cellMatrix[12][12].setBonus("DW");
        cellMatrix[13][13].setBonus("DW");

        // Двойные буквы (Double Letter).
        cellMatrix[3][0].setBonus("DL");
        cellMatrix[11][0].setBonus("DL");
        cellMatrix[0][3].setBonus("DL");
        cellMatrix[6][2].setBonus("DL");
        cellMatrix[7][3].setBonus("DL");
        cellMatrix[8][2].setBonus("DL");
        cellMatrix[14][3].setBonus("DL");
        cellMatrix[2][6].setBonus("DL");
        cellMatrix[6][6].setBonus("DL");
        cellMatrix[8][6].setBonus("DL");
        cellMatrix[12][6].setBonus("DL");
        cellMatrix[3][7].setBonus("DL");
        cellMatrix[11][7].setBonus("DL");
        cellMatrix[2][8].setBonus("DL");
        cellMatrix[6][8].setBonus("DL");
        cellMatrix[8][8].setBonus("DL");
        cellMatrix[12][8].setBonus("DL");
        cellMatrix[0][11].setBonus("DL");
        cellMatrix[7][11].setBonus("DL");
        cellMatrix[14][11].setBonus("DL");
        cellMatrix[6][12].setBonus("DL");
        cellMatrix[8][12].setBonus("DL");
        cellMatrix[3][14].setBonus("DL");
        cellMatrix[11][14].setBonus("DL");
    }

    // Связывает текущую ячейку с ячейкой сверху.
    public void connectTop(int row, int col, Cell[][] cellMatrix) {
        if ((row - 1) < 0) { // Если ячейка на первой строке, сверху нет связи.
            cellMatrix[row][col].setTop(null);
            return;
        }
        int newRow = row - 1;
        cellMatrix[row][col].setTop(cellMatrix[newRow][col]); // Связываем с ячейкой сверху.
    }

    // Связывает текущую ячейку с ячейкой снизу.
    public void connectBottom(int row, int col, Cell[][] cellMatrix) {
        if ((row + 1) > 14) { // Если ячейка на последней строке, снизу нет связи.
            cellMatrix[row][col].setBottom(null);
            return;
        }
        int newRow = row + 1;
        cellMatrix[row][col].setBottom(cellMatrix[newRow][col]); // Связываем с ячейкой снизу.
    }

    // Связывает текущую ячейку с ячейкой слева.
    public void connectLeft(int row, int col, Cell[][] cellMatrix) {
        if ((col - 1) < 0) { // Если ячейка на первой колонке, слева нет связи.
            cellMatrix[row][col].setLeft(null);
            return;
        }
        int newCol = col - 1;
        cellMatrix[row][col].setLeft(cellMatrix[row][newCol]); // Связываем с ячейкой слева.
    }

    // Связывает текущую ячейку с ячейкой справа.
    public void connectRight(int row, int col, Cell[][] cellMatrix) {
        if ((col + 1) > 14) { // Если ячейка на последней колонке, справа нет связи.
            cellMatrix[row][col].setRight(null);
            return;
        }
        int newCol = col + 1;
        cellMatrix[row][col].setRight(cellMatrix[row][newCol]); // Связываем с ячейкой справа.
    }
    public int getCellColor(Cell cell) {
        if (cell.getBonus() == null) {
            return Color.WHITE; // Белый цвет для обычных клеток
        }
        switch (cell.getBonus()) {
            case "DL":
                return Color.parseColor("#A5D6A7"); // Зеленый для Double Letter
            case "DW":
                return Color.parseColor("#FFF59D"); // Желтый для Double Word
            case "TW":
                return Color.parseColor("#EF9A9A");  // красный
            case "TL":
                return Color.parseColor("#ADD8E6"); // Светло-голубой для Triple Letter
            case "X":
                return Color.parseColor("#E57373");  // красный
            default:
                return Color.WHITE; // Белый для остальных
        }
    }
}
