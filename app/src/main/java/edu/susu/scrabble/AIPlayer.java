package edu.susu.scrabble;

import java.util.*;

public class AIPlayer extends Player {

    private static final Set<String> usedWords = new HashSet<>();

    public AIPlayer() {
        super();
    }

    public static class BestMove {
        public String word;
        public int startRow;
        public int startCol;
        public boolean isHorizontal;
        public int score;
    }

    /**
     * Главный метод: находим лучший ход среди всех слов,
     * которые можно составить из букв на стойке.
     * Для первого хода (engine.initialMove), старайся покрыть клетку (7,7).
     */
    public BestMove findBestMove(Board board, Engine engine, Dictionary dictionary) {

        List<String> allWords = generatePossibleWords(dictionary);
        if (allWords.isEmpty()) {
            return null;
        }

        BestMove best = null;
        int bestScore = 0;

        // Множество «якорных» клеток - все пустые, но рядом с занятыми (включая диагонали)
        Set<Cell> anchorCells = findAnchorCells(board);

        for (String word : allWords) {

            // Чтобы не повторять одно и то же слово
            if (usedWords.contains(word)) {
                continue;
            }

            // Горизонтальное
            BestMove bmH = tryHorizontalPlacement(word, board, engine, anchorCells);
            if (bmH != null && bmH.score > bestScore) {
                bestScore = bmH.score;
                best = bmH;
            }

            // Вертикальное
            BestMove bmV = tryVerticalPlacement(word, board, engine, anchorCells);
            if (bmV != null && bmV.score > bestScore) {
                bestScore = bmV.score;
                best = bmV;
            }
        }

        if (best != null && best.score > 0) {
            usedWords.add(best.word);
        }

        return best;
    }

    /**
     * Метод для генерации слов из букв на стойке.
     * (упрощённо: все перестановки + подстроки, которые есть в словаре)
     */
    private List<String> generatePossibleWords(Dictionary dictionary) {
        List<String> result = new ArrayList<>();
        String rackLetters = getRackLetters();

        List<String> perms = new ArrayList<>();
        permutationsOf(rackLetters, "", perms);

        for (String p : perms) {
            for (int end = 2; end <= p.length(); end++) {
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

    /**
     * Проверяем горизонтальные варианты
     */
    private BestMove tryHorizontalPlacement(String word, Board board, Engine engine, Set<Cell> anchorCells) {
        int maxScore = 0;
        BestMove best = null;

        // Если это первый ход, нужно, чтобы слово покрывало (7,7)
        boolean mustCoverCenter = engine.initialMove;

        for (int row = 0; row < 15; row++) {
            for (int colStart = 0; colStart < 15; colStart++) {
                int endCol = colStart + word.length() - 1;
                if (endCol >= 15) break;

                // Если это первый ход, проверяем, попадает ли (7,7) в интервал colStart..endCol при row=7
                if (mustCoverCenter) {
                    // Нужно row == 7 и 7 между colStart..endCol
                    if (row != 7) continue;
                    if (7 < colStart || 7 > endCol) continue;
                }

                // Проверяем, можно ли наложить слово
                if (!canPlaceHorizontal(row, colStart, word, board)) {
                    continue;
                }
                // Проверяем якорные клетки (касается ли уже занятых)
                if (!touchesAnyAnchorHorizontal(row, colStart, word.length(), board, anchorCells)
                        && !mustCoverCenter) {
                    // Если это не первый ход, нужно касаться уже выложенных букв
                    continue;
                }

                int sc = simulateScore(word, row, colStart, true, engine);
                if (sc > maxScore) {
                    maxScore = sc;
                    BestMove bm = new BestMove();
                    bm.word = word;
                    bm.startRow = row;
                    bm.startCol = colStart;
                    bm.isHorizontal = true;
                    bm.score = sc;
                    best = bm;
                }
            }
        }

        return best;
    }

    /**
     * Проверяем вертикальные варианты
     */
    private BestMove tryVerticalPlacement(String word, Board board, Engine engine, Set<Cell> anchorCells) {
        int maxScore = 0;
        BestMove best = null;

        boolean mustCoverCenter = engine.initialMove;

        for (int col = 0; col < 15; col++) {
            for (int rowStart = 0; rowStart < 15; rowStart++) {
                int endRow = rowStart + word.length() - 1;
                if (endRow >= 15) break;

                // Если это первый ход, проверяем, покрывает ли (7,7)
                if (mustCoverCenter) {
                    // нужно col == 7 и 7 между rowStart..endRow
                    if (col != 7) continue;
                    if (7 < rowStart || 7 > endRow) continue;
                }

                if (!canPlaceVertical(rowStart, col, word, board)) {
                    continue;
                }
                if (!touchesAnyAnchorVertical(rowStart, col, word.length(), board, anchorCells)
                        && !mustCoverCenter) {
                    continue;
                }

                int sc = simulateScore(word, rowStart, col, false, engine);
                if (sc > maxScore) {
                    maxScore = sc;
                    BestMove bm = new BestMove();
                    bm.word = word;
                    bm.startRow = rowStart;
                    bm.startCol = col;
                    bm.isHorizontal = false;
                    bm.score = sc;
                    best = bm;
                }
            }
        }

        return best;
    }

    private boolean canPlaceHorizontal(int row, int colStart, String word, Board board) {
        for (int i = 0; i < word.length(); i++) {
            Cell c = board.cellMatrix[row][colStart + i];
            if (c.getTile() != null) {
                // Если занято - проверим совпадение
                char existing = c.getTile().getLetter().charAt(0);
                char needed = word.charAt(i);
                if (Character.toLowerCase(existing) != Character.toLowerCase(needed)) {
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
                char existing = c.getTile().getLetter().charAt(0);
                char needed = word.charAt(i);
                if (Character.toLowerCase(existing) != Character.toLowerCase(needed)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean touchesAnyAnchorHorizontal(int row, int colStart, int length,
                                               Board board, Set<Cell> anchorCells) {
        for (int i = 0; i < length; i++) {
            Cell c = board.cellMatrix[row][colStart + i];
            if (anchorCells.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean touchesAnyAnchorVertical(int rowStart, int col, int length,
                                             Board board, Set<Cell> anchorCells) {
        for (int i = 0; i < length; i++) {
            Cell c = board.cellMatrix[rowStart + i][col];
            if (anchorCells.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private Set<Cell> findAnchorCells(Board board) {
        Set<Cell> anchors = new HashSet<>();
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 15; c++) {
                Cell cell = board.cellMatrix[r][c];
                if (cell.getTile() != null) {
                    // Смотрим всех соседей (8 направлений)
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int rr = r + dr;
                            int cc = c + dc;
                            if (rr >= 0 && rr < 15 && cc >= 0 && cc < 15) {
                                Cell neighbor = board.cellMatrix[rr][cc];
                                if (neighbor.getTile() == null) {
                                    anchors.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }
        return anchors;
    }

    /**
     * Временный расчёт очков: вызываем checkBoard() после «пробной» укладки.
     * Если checkBoard()==false, значит 0 очков.
     */
    private int simulateScore(String word, int row, int col, boolean horizontal, Engine engine) {
        Player oldPlayer = engine.player;
        int oldScore = Integer.parseInt(this.getScore());

        engine.recentlyPlayedCellStack.clear();
        engine.recentlyPlayedTileStack.clear();
        engine.player = this;

        List<Cell> placedCells = new ArrayList<>();

        if (horizontal) {
            for (int i = 0; i < word.length(); i++) {
                Cell c = engine.board.cellMatrix[row][col + i];
                if (c.getTile() == null) {
                    Tile t = new Tile(String.valueOf(word.charAt(i)), 1);
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
                    Tile t = new Tile(String.valueOf(word.charAt(i)), 1);
                    c.setTile(t);
                    placedCells.add(c);
                    engine.recentlyPlayedCellStack.push(c);
                    engine.recentlyPlayedTileStack.push(t);
                }
            }
        }

        boolean ok = engine.checkBoard();
        int gained = 0;
        if (ok) {
            gained = Integer.parseInt(this.getScore()) - oldScore;
        }

        // Откат
        for (Cell cell : placedCells) {
            cell.setTile(null);
        }
        engine.recentlyPlayedCellStack.clear();
        engine.recentlyPlayedTileStack.clear();
        this.setScore(oldScore);
        engine.player = oldPlayer;

        return gained;
    }

    /**
     * Вспомогательный метод: найти индекс плитки c (буква) в rack
     * (если в rack есть Tile с такой буквой).
     */
    public int findTileInRack(char letter) {
        for (int i = 0; i < 7; i++) {
            Tile t = getRack()[i];
            if (t != null && Character.toUpperCase(t.getLetter().charAt(0)) == Character.toUpperCase(letter)) {
                return i;
            }
        }
        return -1;
    }
}
