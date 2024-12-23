package edu.susu.scrabble;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Пример реализации GameActivity для игры на двоих игроков.
 * В layout activity_game.xml должны быть:
 * - GridLayout @+id/boardGrid (поле 15x15)
 * - LinearLayout @+id/rackLayoutPlayer1, @+id/rackLayoutPlayer2 (стойки)
 * - TextView @+id/tvScorePlayer1, @+id/tvScorePlayer2, @+id/tvCurrentTurn
 * - Buttons @+id/btnShuffle, @+id/btnUndo, @+id/btnSubmit, @+id/btnHelp, @+id/btnSkipTurn
 */
public class GameActivity extends AppCompatActivity {

    // Поле 15x15
    private GridLayout boardGrid;
    private Button[][] cellButtons = new Button[15][15];

    // Стойки игроков
    private LinearLayout rackLayoutPlayer1, rackLayoutPlayer2;
    private Button[] rackButtonsPlayer1 = new Button[7];
    private Button[] rackButtonsPlayer2 = new Button[7];

    // Текстовые поля для счёта и текущего хода
    private TextView tvScorePlayer1, tvScorePlayer2, tvCurrentTurn;

    // Кнопки управления
    private Button btnShuffle, btnUndo, btnSubmit, btnHelp, btnSkipTurn;

    // Логические объекты
    private Board board;
    private Bag bag;
    private Engine engine;

    // Два игрока + указатель на текущего
    private Player player1;
    private Player player2;
    private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Получаем из интента, какой режим игры (если нужно)
        String gameMode = getIntent().getStringExtra("GAME_MODE");
        if (gameMode == null) {
            gameMode = "SINGLE"; // по умолчанию
        }

        // Находим View-элементы
        boardGrid          = findViewById(R.id.boardGrid);
        rackLayoutPlayer1  = findViewById(R.id.rackLayoutPlayer1);
        rackLayoutPlayer2  = findViewById(R.id.rackLayoutPlayer2);
        tvScorePlayer1     = findViewById(R.id.tvScorePlayer1);
        tvScorePlayer2     = findViewById(R.id.tvScorePlayer2);
        tvCurrentTurn      = findViewById(R.id.tvCurrentTurn);

        btnShuffle   = findViewById(R.id.btnShuffle);
        btnUndo      = findViewById(R.id.btnUndo);
        btnSubmit    = findViewById(R.id.btnSubmit);
        btnHelp      = findViewById(R.id.btnHelp);
        btnSkipTurn  = findViewById(R.id.btnSkipTurn);

        // Создаём доску и мешок
        board = new Board();
        bag   = new Bag();

        // Инициализируем двух игроков
        player1 = new Player();
        player2 = new Player();

        // Для примера: раздаём каждому игроку по 7 плиток (если в мешке достаточно)
        for (int i = 0; i < 7; i++) {
            if (!bag.bagIsEmpty()) player1.addTileToRack(bag.getNextTile());
            if (!bag.bagIsEmpty()) player2.addTileToRack(bag.getNextTile());
        }

        // Текущий игрок — player1
        currentPlayer = player1;

        // Получаем словарь из DictionaryManager (уже загружен)
        Dictionary dict = DictionaryManager.getInstance(this).getDictionary();

        // Создаём движок
        engine = new Engine(currentPlayer, board, dict);

        // Создаём кнопки на поле
        createBoardButtons();

        // Создаём кнопки на стойках для обоих игроков
        createRackButtonsForPlayers();

        // Обновляем текст, чей ход
        updateCurrentTurnLabel();
        updateScoresOnScreen();

        // Привязываем логику к кнопкам
        btnShuffle.setOnClickListener(v -> {
            currentPlayer.shuffleRack();
            currentPlayer.organizeRack();
            updateRackGUI();
        });

        btnUndo.setOnClickListener(v -> {
            engine.player = currentPlayer;
            engine.undoLastMove();
            updateBoardGUI();
            updateRackGUI();
        });

        btnSubmit.setOnClickListener(v -> {
            engine.player = currentPlayer;
            if (engine.checkBoard()) {
                // Если ход валидный
                updateScoresOnScreen();
                fillRackForPlayer(currentPlayer); // добираем новые плитки
                switchTurn();
            } else {
                // Ход невалиден
            }
            updateBoardGUI();
            updateRackGUI();
        });

        btnHelp.setOnClickListener(v -> {
            HelpActivity.start(GameActivity.this);
        });

        btnSkipTurn.setOnClickListener(v -> {
            switchTurn();
        });
    }

    /**
     * Создаём кнопки 15x15 для поля. Каждая кнопка привязана к ячейке (Cell).
     */
    private void createBoardButtons() {
        boardGrid.removeAllViews();

        // Рассчитываем размер кнопок по ширине экрана
        int screenSize = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = screenSize / 16;

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Button button = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(1, 1, 1, 1);

                button.setLayoutParams(params);
                button.setTextSize(8f);
                button.setPadding(0, 0, 0, 0);

                Cell cell = board.cellMatrix[i][j];
                button.setBackgroundColor(board.getCellColor(cell));

                // Если есть бонус в Cell, показываем
                if (cell.getBonus() != null) {
                    button.setText(cell.getBonus());
                }

                button.setOnClickListener(v -> {
                    // Если у текущего игрока выбрана плитка, и клетка пуста — ставим
                    if (engine.rackTileSelected != null && cell.getTile() == null) {
                        if (engine.rackTileSelected.getLetter().equals("-")) {
                            // Если это blank, спрашиваем букву
                            ChooseTileDialogFragment dialog = ChooseTileDialogFragment.newInstance(chosen -> {
                                Bag.swapBlankTile(chosen.charAt(0));
                                cell.setTile(Bag.swappedBlankTile);
                                button.setText(Bag.swappedBlankTile.getLetter());
                                button.setTextColor(getResources().getColor(R.color.black));

                                engine.rackTileSelected = null;
                                engine.recentlyPlayedCellStack.push(cell);
                            });
                            dialog.show(getSupportFragmentManager(), "chooseTile");
                        } else {
                            // Обычная плитка
                            cell.setTile(engine.rackTileSelected);
                            button.setText(engine.rackTileSelected.getLetter());
                            button.setTextColor(getResources().getColor(R.color.black));

                            engine.rackTileSelected = null;
                            engine.recentlyPlayedCellStack.push(cell);
                        }
                    }
                });

                cellButtons[i][j] = button;
                boardGrid.addView(button);
            }
        }
    }

    /**
     * Создаём кнопки на стойках для игрока 1 и 2.
     */
    private void createRackButtonsForPlayers() {
        // Стойка Player1
        rackLayoutPlayer1.removeAllViews();
        for (int i = 0; i < 7; i++) {
            Button btn = new Button(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ));
            btn.setTextSize(18f);

            final int index = i;
            Tile tile = player1.getRack()[index];
            if (tile != null) {
                btn.setText(tile.getLetter());
            }

            btn.setOnClickListener(v -> {
                if (currentPlayer == player1 && engine.rackTileSelected == null && tile != null) {
                    Tile selected = player1.getAndRemoveFromRackAt(index);
                    engine.rackTileSelected = selected;
                    engine.recentlyPlayedTileStack.push(selected);
                    btn.setText("");
                }
            });

            rackButtonsPlayer1[i] = btn;
            rackLayoutPlayer1.addView(btn);
        }

        // Стойка Player2
        rackLayoutPlayer2.removeAllViews();
        for (int i = 0; i < 7; i++) {
            Button btn = new Button(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ));
            btn.setTextSize(18f);

            final int index = i;
            Tile tile = player2.getRack()[index];
            if (tile != null) {
                btn.setText(tile.getLetter());
            }

            btn.setOnClickListener(v -> {
                if (currentPlayer == player2 && engine.rackTileSelected == null && tile != null) {
                    Tile selected = player2.getAndRemoveFromRackAt(index);
                    engine.rackTileSelected = selected;
                    engine.recentlyPlayedTileStack.push(selected);
                    btn.setText("");
                }
            });

            rackButtonsPlayer2[i] = btn;
            rackLayoutPlayer2.addView(btn);
        }
    }

    /**
     * Обновляем доску после изменений.
     */
    private void updateBoardGUI() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Cell cell = board.cellMatrix[i][j];
                Button btn = cellButtons[i][j];
                if (cell.getTile() != null) {
                    btn.setText(cell.getTile().getLetter());
                    btn.setTextColor(getResources().getColor(R.color.black));
                } else {
                    btn.setText(cell.getBonus() != null ? cell.getBonus() : "");
                    btn.setBackgroundColor(board.getCellColor(cell));
                }
            }
        }
    }

    /**
     * Обновляем стойки обоих игроков.
     */
    private void updateRackGUI() {
        // Player1
        for (int i = 0; i < 7; i++) {
            Tile tile = player1.getRack()[i];
            rackButtonsPlayer1[i].setText(tile == null ? "" : tile.getLetter());
        }
        // Player2
        for (int i = 0; i < 7; i++) {
            Tile tile = player2.getRack()[i];
            rackButtonsPlayer2[i].setText(tile == null ? "" : tile.getLetter());
        }
    }

    /**
     * Заполняем стойку игрока новыми плитками из мешка, если есть свободные слоты.
     */
    private void fillRackForPlayer(Player p) {
        while (!bag.bagIsEmpty() && p.getRackSize() < 7) {
            p.addTileToRack(bag.getNextTile());
        }
    }

    /**
     * Переключаем ход между player1 и player2.
     */
    private void switchTurn() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
        engine.player = currentPlayer;
        updateCurrentTurnLabel();
        checkEndOfGame();
        updateBoardGUI();
        updateRackGUI();
    }

    /**
     * Обновляем текст: "Ход игрока 1" или "Ход игрока 2".
     */
    private void updateCurrentTurnLabel() {
        if (currentPlayer == player1) {
            tvCurrentTurn.setText("PLAYER 1's MOVE");
        } else {
            tvCurrentTurn.setText("PLAYER 2's MOVE");
        }
    }

    /**
     * Проверяем, не окончилась ли игра (например, мешок пуст и у обоих игроков пустые стойки).
     */
    private void checkEndOfGame() {
        if (bag.bagIsEmpty() && player1.getRackSize() == 0 && player2.getRackSize() == 0) {
            showWinner();
        }
    }

    /**
     * Показываем диалог с результатами.
     */
    private void showWinner() {
        int p1Score = Integer.parseInt(player1.getScore());
        int p2Score = Integer.parseInt(player2.getScore());
        String msg;
        if (p1Score > p2Score) {
            msg = "Победил игрок 1! Счёт " + p1Score + " : " + p2Score;
        } else if (p2Score > p1Score) {
            msg = "Победил игрок 2! Счёт " + p2Score + " : " + p1Score;
        } else {
            msg = "Ничья! Оба набрали " + p1Score;
        }

        new AlertDialog.Builder(this)
                .setTitle("Игра окончена")
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    /**
     * Обновляем счёт на экране.
     */
    private void updateScoresOnScreen() {
        tvScorePlayer1.setText(player1.getScore());
        tvScorePlayer2.setText(player2.getScore());
    }
}
