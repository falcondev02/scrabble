package edu.susu.scrabble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnSingleGame;
    private Button btnTwoPlayers;
    private Button btnComputer;
    private Button btnAddWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -- ВАЖНО: инициализируем (подгружаем) словарь, чтобы сделать это один раз при запуске приложения.
        DictionaryManager.getInstance(this);

        btnSingleGame = findViewById(R.id.btnSingleGame);
        btnTwoPlayers = findViewById(R.id.btnTwoPlayers);
        btnComputer   = findViewById(R.id.btnComputer);
        btnAddWord    = findViewById(R.id.btnAddWord);

        btnSingleGame.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SinglePlayerGameActivity.class));
        });

        btnTwoPlayers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("GAME_MODE", "TWO_PLAYERS");
            startActivity(intent);
        });

        btnComputer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("GAME_MODE", "VS_COMPUTER");
            startActivity(intent);
        });
        btnComputer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AIGameActivity.class);
            // Можно передавать putExtra, если нужно
            startActivity(intent);
        });

        // Переход в AddWordActivity
        btnAddWord.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddWordActivity.class));
        });
    }
}
