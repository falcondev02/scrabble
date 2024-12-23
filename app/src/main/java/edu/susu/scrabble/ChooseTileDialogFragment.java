package edu.susu.scrabble;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// Диалоговое окно для выбора буквы
public class ChooseTileDialogFragment extends DialogFragment {

    // Интерфейс для передачи выбранной буквы
    public interface OnLetterChosenListener {
        void onLetterChosen(String letter);
    }

    private OnLetterChosenListener listener;
    private String chosenLetter = null;

    // Метод для создания нового экземпляра диалогового окна
    public static ChooseTileDialogFragment newInstance(OnLetterChosenListener l) {
        ChooseTileDialogFragment fragment = new ChooseTileDialogFragment();
        fragment.listener = l;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Создаём представление для диалогового окна
        View view = inflater.inflate(R.layout.dialog_choose_tile, container, false);

        GridLayout grid = view.findViewById(R.id.gridLetters);
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // Для каждой буквы создаём кнопку
        for (char c : letters.toCharArray()) {
            Button btn = new Button(requireContext());
            btn.setText(String.valueOf(c));
            btn.setOnClickListener(v -> {
                // Подсвечиваем
                chosenLetter = String.valueOf(c);
            });
            grid.addView(btn);
        }

        Button btnSwap = view.findViewById(R.id.btnSwap);  // Кнопка для подтверждения выбора
        btnSwap.setOnClickListener(v -> {
            if(chosenLetter != null && listener != null) {
                listener.onLetterChosen(chosenLetter);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

    }
}
