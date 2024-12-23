package edu.susu.scrabble;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Игра против компьютера. Использует activity_game_ai.xml
 */
public class AIGameActivity extends AppCompatActivity {

    // UI элементы
    private GridLayout boardGrid;
    private Button[][] cellButtons = new Button[15][15];

    private TextView tvScorePlayer1;
    private TextView tvScorePlayer2;
    private TextView tvCurrentTurn;

    private Button btnShuffle, btnUndo, btnSubmit, btnHelp, btnSkipTurn;

    private LinearLayout rackLayoutPlayer1;
    private Button[] rackButtonsPlayer1 = new Button[7];

    // Игровая логика
    private Board board;
    private Bag bag;
    private Engine engine;

    // Два игрока: человек и AI
    private Player humanPlayer;
    private AIPlayer aiPlayer;

    // Кто ходит сейчас
    private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_ai);  // <-- наш новый макет

        // 1) Найти все View-элементы
        boardGrid = findViewById(R.id.boardGrid);
        tvScorePlayer1 = findViewById(R.id.tvScorePlayer1);
        tvScorePlayer2 = findViewById(R.id.tvScorePlayer2);
        tvCurrentTurn  = findViewById(R.id.tvCurrentTurn);

        btnShuffle   = findViewById(R.id.btnShuffle);
        btnUndo      = findViewById(R.id.btnUndo);
        btnSubmit    = findViewById(R.id.btnSubmit);
        btnHelp      = findViewById(R.id.btnHelp);
        btnSkipTurn  = findViewById(R.id.btnSkipTurn);

        rackLayoutPlayer1 = findViewById(R.id.rackLayoutPlayer1);
        // Если хотите отобразить стойку AI, найдите rackLayoutPlayer2:
        // rackLayoutPlayer2 = findViewById(R.id.rackLayoutPlayer2);

        // 2) Создаём Board, Bag, двух игроков
        board = new Board();
        bag   = new Bag();

        humanPlayer = new Player();
        aiPlayer    = new AIPlayer();

        // Раздать по 7 плиток (если мешок не пуст)
        for (int i = 0; i < 7; i++) {
            if (!bag.bagIsEmpty()) humanPlayer.addTileToRack(bag.getNextTile());
            if (!bag.bagIsEmpty()) aiPlayer.addTileToRack(bag.getNextTile());
        }

        // Текущий ход — человек
        currentPlayer = humanPlayer;

        // 3) Создаём движок (Engine) и передаём словарь
        Dictionary dict = DictionaryManager.getInstance(this).getDictionary();
        engine = new Engine(currentPlayer, board, dict);

        // 4) Генерация кнопок доски и стойки
        createBoardButtons();
        createRackButtons();

        // 5) Привязываем onClick на кнопки
        btnShuffle.setOnClickListener(v -> {
            if (currentPlayer == humanPlayer) {
                humanPlayer.shuffleRack();
                humanPlayer.organizeRack();
                updateRackGUI();
            }
        });

        btnUndo.setOnClickListener(v -> {
            if (currentPlayer == humanPlayer) {
                engine.player = humanPlayer;
                engine.undoLastMove();
                updateBoardGUI();
                updateRackGUI();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (currentPlayer == humanPlayer) {
                // Проверяем ход через движок
                engine.player = humanPlayer;
                boolean valid = engine.checkBoard();
                if (valid) {
                    updateScores();
                    fillRackForPlayer(humanPlayer);
                    switchToAI();  // Передаём ход AI
                } else {
                    // ход невалиден — ничего не меняем
                }
                updateBoardGUI();
                updateRackGUI();
            }
        });

        btnSkipTurn.setOnClickListener(v -> {
            // Если человек пропускает, просто ход переходит к AI
            if (currentPlayer == humanPlayer) {
                switchToAI();
            } else {
                // Если AI пропускает (редко), вернём ход человеку
                switchToHuman();
            }
        });

        btnHelp.setOnClickListener(v -> {
            HelpActivity.start(AIGameActivity.this);
        });

        // Обновим изначально
        updateScores();
        updateCurrentTurnLabel();
        updateBoardGUI();
        updateRackGUI();
    }

    // Создаём кнопки для каждой ячейки 15x15
    private void createBoardButtons() {
        boardGrid.removeAllViews();

        // Подбираем размер кнопок (примерный)
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = screenWidth / 16;  // 15 клеток + промежутки

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Button btn = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(1,1,1,1);
                btn.setLayoutParams(params);
                btn.setTextSize(8f);
                btn.setPadding(0,0,0,0);

                Cell cell = board.cellMatrix[i][j];
                btn.setBackgroundColor(board.getCellColor(cell));

                if (cell.getBonus() != null) {
                    btn.setText(cell.getBonus());
                }

                // Сохраняем ссылку
                cellButtons[i][j] = btn;
                boardGrid.addView(btn);

                // Логика клика для человека
                final int row = i;
                final int col = j;
                btn.setOnClickListener(v -> {
                    if (currentPlayer == humanPlayer) {
                        // Если у нас выбрана плитка
                        if (engine.rackTileSelected != null && cell.getTile() == null) {
                            // Проверяем blank
                            if (engine.rackTileSelected.getLetter().equals("-")) {
                                ChooseTileDialogFragment dialog =
                                        ChooseTileDialogFragment.newInstance(chosen -> {
                                            Bag.swapBlankTile(chosen.charAt(0));
                                            cell.setTile(Bag.swappedBlankTile);
                                            btn.setText(Bag.swappedBlankTile.getLetter());
                                            btn.setTextColor(getResources().getColor(R.color.black));
                                            engine.rackTileSelected = null;
                                            engine.recentlyPlayedCellStack.push(cell);
                                        });
                                dialog.show(getSupportFragmentManager(), "chooseTile");
                            } else {
                                cell.setTile(engine.rackTileSelected);
                                btn.setText(engine.rackTileSelected.getLetter());
                                btn.setTextColor(getResources().getColor(R.color.black));
                                engine.rackTileSelected = null;
                                engine.recentlyPlayedCellStack.push(cell);
                            }
                        }
                    }
                });
            }
        }
    }

    // Создаём кнопки стойки игрока (человека)
    private void createRackButtons() {
        rackLayoutPlayer1.removeAllViews();

        for (int i = 0; i < 7; i++) {
            Button btn = new Button(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            btn.setLayoutParams(lp);
            btn.setTextSize(18f);

            final int index = i;
            Tile tile = humanPlayer.getRack()[i];
            if (tile != null) {
                btn.setText(tile.getLetter());
            }

            btn.setOnClickListener(v -> {
                if (currentPlayer == humanPlayer && engine.rackTileSelected == null && tile != null) {
                    Tile selected = humanPlayer.getAndRemoveFromRackAt(index);
                    engine.rackTileSelected = selected;
                    engine.recentlyPlayedTileStack.push(selected);
                    btn.setText("");
                }
            });

            rackButtonsPlayer1[i] = btn;
            rackLayoutPlayer1.addView(btn);
        }
    }

    // Обновляем кнопки на доске (надписи)
    private void updateBoardGUI() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Cell cell = board.cellMatrix[i][j];
                Button btn = cellButtons[i][j];
                if (cell.getTile() != null) {
                    btn.setText(cell.getTile().getLetter());
                    btn.setTextColor(getResources().getColor(R.color.black));
                } else {
                    // Пустая ячейка => показываем либо бонус, либо пусто
                    btn.setText(cell.getBonus() != null ? cell.getBonus() : "");
                    btn.setBackgroundColor(board.getCellColor(cell));
                }
            }
        }
    }

    // Обновляем кнопки стойки
    private void updateRackGUI() {
        for (int i = 0; i < 7; i++) {
            Tile tile = humanPlayer.getRack()[i];
            if (rackButtonsPlayer1[i] != null) {
                rackButtonsPlayer1[i].setText(tile == null ? "" : tile.getLetter());
            }
        }
    }

    private void updateScores() {
        tvScorePlayer1.setText(humanPlayer.getScore());
        tvScorePlayer2.setText(aiPlayer.getScore());
    }

    private void updateCurrentTurnLabel() {
        if (currentPlayer == humanPlayer) {
            tvCurrentTurn.setText("Your move");
        } else {
            tvCurrentTurn.setText("AI is thinking...");
        }
    }

    // Ход переходит к AI
    private void switchToAI() {
        currentPlayer = aiPlayer;
        engine.player = aiPlayer;
        updateCurrentTurnLabel();

        // Запускаем ход AI автоматически
        doAIturn();

        // После хода AI возвращаем ход человеку (если игра не закончилась)
        switchToHuman();
    }

    // Логика хода AI
    private void doAIturn() {
        // Ищем лучшее слово
        AIPlayer.BestMove bestMove = aiPlayer.findBestMove(board, engine,
                DictionaryManager.getInstance(this).getDictionary());

        if (bestMove == null) {
            // AI пропускает, если не находит хода
            return;
        }

        // Расставляем слово
        placeBestMoveOnBoard(bestMove);

        // Проверяем и начисляем очки
        boolean ok = engine.checkBoard();
        if (ok) {
            fillRackForPlayer(aiPlayer);
            updateScores();
            updateBoardGUI();
        }
    }

    // Ставим слово (horizontal/vertical) на доску
    private void placeBestMoveOnBoard(AIPlayer.BestMove move) {
        engine.recentlyPlayedCellStack.clear();
        engine.recentlyPlayedTileStack.clear();

        if (move.isHorizontal) {
            for (int i = 0; i < move.word.length(); i++) {
                Cell c = board.cellMatrix[move.startRow][move.startCol + i];
                if (c.getTile() == null) {
                    Tile t = new Tile(String.valueOf(move.word.charAt(i)), 1);
                    c.setTile(t);
                    engine.recentlyPlayedCellStack.push(c);
                    engine.recentlyPlayedTileStack.push(t);
                }
            }
        } else {
            for (int i = 0; i < move.word.length(); i++) {
                Cell c = board.cellMatrix[move.startRow + i][move.startCol];
                if (c.getTile() == null) {
                    Tile t = new Tile(String.valueOf(move.word.charAt(i)), 1);
                    c.setTile(t);
                    engine.recentlyPlayedCellStack.push(c);
                    engine.recentlyPlayedTileStack.push(t);
                }
            }
        }
    }

    // Возвращаем ход человеку
    private void switchToHuman() {
        currentPlayer = humanPlayer;
        engine.player = humanPlayer;
        updateCurrentTurnLabel();
        updateBoardGUI();
        updateRackGUI();
        checkEndGame();
    }

    private void fillRackForPlayer(Player p) {
        while (!bag.bagIsEmpty() && p.getRackSize() < 7) {
            p.addTileToRack(bag.getNextTile());
        }
    }

    private void checkEndGame() {
        // Пример: если мешок пуст и у обоих игроков пустые стойки
        if (bag.bagIsEmpty() && humanPlayer.getRackSize() == 0 && aiPlayer.getRackSize() == 0) {
            showWinner();
        }
    }

    private void showWinner() {
        int p1Score = Integer.parseInt(humanPlayer.getScore());
        int p2Score = Integer.parseInt(aiPlayer.getScore());
        String msg;
        if (p1Score > p2Score) {
            msg = "Вы победили! " + p1Score + " : " + p2Score;
        } else if (p2Score > p1Score) {
            msg = "Компьютер победил! " + p2Score + " : " + p1Score;
        } else {
            msg = "Ничья! " + p1Score + ":" + p2Score;
        }

        new AlertDialog.Builder(this)
                .setTitle("Игра окончена")
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }
}
