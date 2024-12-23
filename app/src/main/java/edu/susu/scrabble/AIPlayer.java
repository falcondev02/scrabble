package edu.susu.scrabble;

import java.util.ArrayList;
import java.util.List;

public class AIPlayer extends Player {

    public AIPlayer() {
        super();
    }

    /**
     * Описание лучшего хода
     */
    public static class BestMove {
        public String word;
        public int startRow;
        public int startCol;
        public boolean isHorizontal;
        public int score;
    }

    /**
     * Главный метод: пытается найти лучший ход
     */
    public BestMove findBestMove(Board board, Engine engine, Dictionary dictionary) {
        // 1) Собрать все слова из rack
        List<String> allWords = generatePossibleWords(dictionary);
        if (allWords.isEmpty()) {
            return null; // Нечего ставить
        }

        BestMove best = null;
        int bestScore = 0;

        // 2) Для каждого слова найти варианты размещения
        for (String word : allWords) {
            // Горизонтально
            BestMove bmH = findHorizontalPlacement(word, board, engine, dictionary);
            if (bmH != null && bmH.score > bestScore) {
                bestScore = bmH.score;
                best = bmH;
            }

            // Вертикально
            BestMove bmV = findVerticalPlacement(word, board, engine, dictionary);
            if (bmV != null && bmV.score > bestScore) {
                bestScore = bmV.score;
                best = bmV;
            }
        }
        return best;
    }

    // Генерация слов из букв стойки (упрощённо, brute force)
    private List<String> generatePossibleWords(Dictionary dictionary) {
        List<String> result = new ArrayList<>();
        String rackLetters = getRackLetters(); // Все буквы на стойке
        List<String> perms = new ArrayList<>();
        permutationsOf(rackLetters, "", perms);

        // Проверяем в словаре все префиксы
        for (String p : perms) {
            for (int end = 1; end <= p.length(); end++) {
                String cand = p.substring(0, end).toLowerCase();
                if (dictionary.verifyWord(cand) && !result.contains(cand)) {
                    result.add(cand);
                }
            }
        }
        return result;
    }

    private String getRackLetters() {
        StringBuilder sb = new StringBuilder();
        for (Tile t : getRack()) {
            if (t != null) {
                sb.append(t.getLetter());
            }
        }
        return sb.toString();
    }

    // Рекурсивная генерация перестановок
    private void permutationsOf(String remaining, String prefix, List<String> out) {
        if (remaining.length() == 0) {
            out.add(prefix);
        } else {
            for (int i = 0; i < remaining.length(); i++) {
                String newRem = remaining.substring(0, i) + remaining.substring(i + 1);
                permutationsOf(newRem, prefix + remaining.charAt(i), out);
            }
        }
    }

    private BestMove findHorizontalPlacement(String word, Board board, Engine engine, Dictionary dict) {
        int maxScore = -1;
        BestMove best = null;

        for (int row = 0; row < 15; row++) {
            for (int colStart = 0; colStart < 15; colStart++) {
                if (colStart + word.length() > 15) break;
                if (canPlaceHorizontal(row, colStart, word, board)) {
                    int sc = simulateScore(word, row, colStart, true, engine);
                    if (sc > maxScore) {
                        maxScore = sc;
                        best = new BestMove();
                        best.word = word;
                        best.startRow = row;
                        best.startCol = colStart;
                        best.isHorizontal = true;
                        best.score = sc;
                    }
                }
            }
        }

        return best;
    }

    private BestMove findVerticalPlacement(String word, Board board, Engine engine, Dictionary dict) {
        int maxScore = -1;
        BestMove best = null;

        for (int col = 0; col < 15; col++) {
            for (int rowStart = 0; rowStart < 15; rowStart++) {
                if (rowStart + word.length() > 15) break;
                if (canPlaceVertical(rowStart, col, word, board)) {
                    int sc = simulateScore(word, rowStart, col, false, engine);
                    if (sc > maxScore) {
                        maxScore = sc;
                        best = new BestMove();
                        best.word = word;
                        best.startRow = rowStart;
                        best.startCol = col;
                        best.isHorizontal = false;
                        best.score = sc;
                    }
                }
            }
        }

        return best;
    }

    // Проверяем, свободны ли нужные клетки (или совпадают по букве)
    private boolean canPlaceHorizontal(int row, int colStart, String word, Board board) {
        for (int i = 0; i < word.length(); i++) {
            Cell c = board.cellMatrix[row][colStart + i];
            if (c.getTile() != null) {
                // Если занято, проверяем совпадение букв
                if (!c.getTile().getLetter().equalsIgnoreCase(word.substring(i, i+1))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canPlaceVertical(int rowStart, int col, String word, Board board) {
        for (int i = 0; i < word.length(); i++) {
            Cell c = board.cellMatrix[rowStart + i][col];
            if (c.getTile() != null) {
                if (!c.getTile().getLetter().equalsIgnoreCase(word.substring(i, i+1))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Временный расчёт очков (через движок)
    private int simulateScore(String word, int row, int col, boolean horizontal, Engine engine) {
        // Сохраняем старое состояние
        Player oldPlayer = engine.player;
        int oldScore = Integer.parseInt(this.getScore());

        // Сбрасываем стеки
        engine.recentlyPlayedCellStack.clear();
        engine.recentlyPlayedTileStack.clear();
        // Меняем player на AI
        engine.player = this;

        // Временно ставим буквы
        ArrayList<Cell> placedCells = new ArrayList<>();
        if (horizontal) {
            for (int i = 0; i < word.length(); i++) {
                Cell c = engine.board.cellMatrix[row][col + i];
                if (c.getTile() == null) {
                    Tile t = new Tile(word.substring(i, i+1), 1);
                    c.setTile(t);
                    placedCells.add(c);
                    engine.recentlyPlayedCellStack.push(c);
                    engine.recentlyPlayedTileStack.push(t);
                }
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                Cell c = engine.board.cellMatrix[row + i][col];
                if (c.getTile() == null) {
                    Tile t = new Tile(word.substring(i, i+1), 1);
                    c.setTile(t);
                    placedCells.add(c);
                    engine.recentlyPlayedCellStack.push(c);
                    engine.recentlyPlayedTileStack.push(t);
                }
            }
        }

        // Вызываем checkBoard() — если валиден, будет начисление очков
        boolean ok = engine.checkBoard();
        int gained = 0;
        if (ok) {
            gained = Integer.parseInt(this.getScore()) - oldScore;
        }

        // Откат
        // Убираем временные плитки
        for (Cell cell : placedCells) {
            cell.setTile(null);
        }
        engine.recentlyPlayedCellStack.clear();
        engine.recentlyPlayedTileStack.clear();

        this.setScore(oldScore);
        engine.player = oldPlayer;

        return gained;
    }
}
