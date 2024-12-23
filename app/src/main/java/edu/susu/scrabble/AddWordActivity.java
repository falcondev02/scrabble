package edu.susu.scrabble;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AddWordActivity extends AppCompatActivity {

    private EditText etWord;
    private Button btnAdd;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        etWord = findViewById(R.id.etWord);
        btnAdd = findViewById(R.id.btnAdd);
        tvResult = findViewById(R.id.tvResult);

        // При нажатии на "Добавить" берём введённое слово и отдаём в DictionaryManager
        btnAdd.setOnClickListener(v -> {
            String wordToAdd = etWord.getText().toString().trim();
            DictionaryManager dictManager = DictionaryManager.getInstance(AddWordActivity.this);
            String result = dictManager.addNewWord(wordToAdd);
            tvResult.setText(result);
        });
    }
}
