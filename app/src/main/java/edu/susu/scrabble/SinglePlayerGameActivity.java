package edu.susu.scrabble;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Пример SinglePlayerGameActivity для одиночной игры.
 * В layout activity_game_single.xml должны быть:
 * - GridLayout @+id/boardGrid (поле 15x15)
 * - LinearLayout @+id/rackLayout (стойка)
 * - TextView @+id/tvScore (отображение счёта)
 * - Buttons @+id/btnShuffle, @+id/btnUndo, @+id/btnSubmit, @+id/btnHelp
 */
public class SinglePlayerGameActivity extends AppCompatActivity {
    private GridLayout boardGrid;
    private LinearLayout rackLayout;
    private TextView tvScore;

    // Логика
    private Board board;
    private Bag bag;
    private Player player;
    private Engine engine;

    // Ссылки на динамически созданные кнопки
    private Button[][] cellButtons = new Button[15][15];
    private Button[] rackButtons = new Button[7];

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_single);

        boardGrid = findViewById(R.id.boardGrid);
        rackLayout = findViewById(R.id.rackLayout);
        tvScore = findViewById(R.id.tvScore);

        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnUndo = findViewById(R.id.btnUndo);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnHelp = findViewById(R.id.btnHelp);

        // 1) Создаём Board, Bag, Player
        board = new Board();
        bag = new Bag();
        player = new Player();

        // 2) Раздаём начальные 7 плиток (если есть)
        givePlayerStartingTiles();

        // 3) Получаем готовый словарь из DictionaryManager
        Dictionary dict = DictionaryManager.getInstance(this).getDictionary();

        // 4) Создаём движок
        engine = new Engine(player, board, dict);

        // 5) Создаём кнопки на поле и на стойке
        createBoardButtons();
        createRackButtons();

        // 6) Настраиваем кнопки
        btnShuffle.setOnClickListener(v -> {
            player.shuffleRack();
            player.organizeRack();
            updateRackGUI();
        });

        btnUndo.setOnClickListener(v -> {
            engine.undoLastMove();
            updateBoardGUI();
            updateRackGUI();
        });

        btnSubmit.setOnClickListener(v -> {
            if (engine.checkBoard()) {
                tvScore.setText("Score: " + player.getScore());
                // Добираем плитки из мешка
                while (givePlayerANewTile()) {}
                updateRackGUI();
                updateBoardGUI();
            }
        });

        btnHelp.setOnClickListener(v -> {
            HelpActivity.start(SinglePlayerGameActivity.this);
        });
    }

    /**
     * Создаём кнопки 15x15 для поля (boardGrid).
     */
    private void createBoardButtons() {
        boardGrid.removeAllViews();
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

                // Если есть бонус, показываем
                if (cell.getBonus() != null) {
                    button.setText(cell.getBonus());
                }

                button.setOnClickListener(v -> {
                    if (engine.rackTileSelected != null && cell.getTile() == null) {
                        if (engine.rackTileSelected.getLetter().equals("-")) {
                            // Blank — показываем диалог выбора буквы
                            ChooseTileDialogFragment dialog =
                                    ChooseTileDialogFragment.newInstance(chosen -> {
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
     * Создаём 7 кнопок для стойки (rackLayout).
     */
    private void createRackButtons() {
        rackLayout.removeAllViews();
        for (int i = 0; i < 7; i++) {
            Button btn = new Button(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ));
            btn.setTextSize(18f);

            final int index = i;
            Tile tile = player.getRack()[index];
            if (tile != null) {
                btn.setText(tile.getLetter());
            }

            btn.setOnClickListener(v -> {
                if (engine.rackTileSelected == null && tile != null) {
                    Tile selected = player.getAndRemoveFromRackAt(index);
                    engine.rackTileSelected = selected;
                    engine.recentlyPlayedTileStack.push(selected);
                    btn.setText("");
                }
            });

            rackButtons[i] = btn;
            rackLayout.addView(btn);
        }
    }

    /**
     * Обновляем кнопки поля (если плитки изменились).
     */
    private void updateBoardGUI() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Cell cell = board.cellMatrix[i][j];
                Button cellButton = cellButtons[i][j];
                if (cell.getTile() != null) {
                    cellButton.setText(cell.getTile().getLetter());
                    cellButton.setTextColor(getResources().getColor(R.color.black));
                } else {
                    cellButton.setText(cell.getBonus() != null ? cell.getBonus() : "");
                    cellButton.setBackgroundColor(board.getCellColor(cell));
                }
            }
        }
    }

    /**
     * Обновляем кнопки стойки.
     */
    private void updateRackGUI() {
        for (int i = 0; i < 7; i++) {
            Tile tile = player.getRack()[i];
            if (tile != null) {
                rackButtons[i].setText(tile.getLetter());
            } else {
                rackButtons[i].setText("");
            }
        }
        tvScore.setText("Score: " + player.getScore());
    }

    /**
     * Попробовать взять одну новую плитку из мешка, вернуть true/false получилось ли.
     */
    private boolean givePlayerANewTile() {
        if (bag.bagIsEmpty() || player.getRackSize() == 7) {
            return false;
        }
        Tile newTile = bag.getNextTile();
        player.addTileToRack(newTile);
        return true;
    }

    /**
     * В начале игры раздаём 7 плиток.
     */
    private void givePlayerStartingTiles() {
        for (int i = 0; i < 7; i++) {
            if (!bag.bagIsEmpty()) {
                player.addTileToRack(bag.getNextTile());
            }
        }
    }
}
