package edu.susu.scrabble;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Singleton-класс для управления словарем.
 * 1) Копирует file.txt из assets во внутреннее хранилище (при первом запуске).
 * 2) Загружает слова в Trie (использует класс Dictionary).
 * 3) Позволяет добавлять новые слова в файл и обновлять Trie.
 */
public class DictionaryManager {
    private static DictionaryManager instance;
    private Dictionary dictionary;  // наш уже существующий класс с Trie внутри
    private File internalDictionaryFile; // путь к скопированному file.txt во внутреннем хранилище

    private static final String DICT_FILE_NAME = "file.txt"; // имя словарного файла

    // Приватный конструктор (Singleton)
    private DictionaryManager(Context context) {
        // 1) Копируем file.txt, если он ещё не скопирован
        copyFileFromAssetsIfNeeded(context);

        // 2) Создаем Dictionary, передавая ему Context,
        //    но теперь он читает уже из внутреннего файла, а не из assets
        dictionary = new Dictionary(context, getInternalDictionaryFilePath(context));
    }

    /**
     * Инициализация/получение экземпляра DictionaryManager.
     */
    public static DictionaryManager getInstance(Context context) {
        if (instance == null) {
            instance = new DictionaryManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Возвращает доступ к объекту Dictionary (где хранится Trie и метод verifyWord).
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Проверяем, лежит ли уже file.txt во внутренней памяти. Если нет — копируем из assets.
     */
    private void copyFileFromAssetsIfNeeded(Context context) {
        internalDictionaryFile = new File(context.getFilesDir(), DICT_FILE_NAME);
        if (!internalDictionaryFile.exists()) {
            // если файл не существует, копируем
            AssetUtils.copyAsset(context, DICT_FILE_NAME, internalDictionaryFile.getAbsolutePath());
        }
    }

    /**
     * Полный путь к словарному файлу во внутреннем хранилище.
     */
    private String getInternalDictionaryFilePath(Context context) {
        return new File(context.getFilesDir(), DICT_FILE_NAME).getAbsolutePath();
    }

    /**
     * Добавление нового слова в словарь:
     * 1) Проверяем дубли в Trie;
     * 2) Если нет — добавляем в файл + Trie;
     * 3) Возвращаем результат строки для UI.
     */
    public String addNewWord(String newWord) {
        if (newWord == null || newWord.trim().isEmpty()) {
            return "Введите слово!";
        }
        newWord = newWord.toLowerCase();

        // Проверим, есть ли уже слово в Trie
        if (dictionary.verifyWord(newWord)) {
            return "Слово уже есть в словаре";
        }

        // 1) Добавляем слово в файл
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(internalDictionaryFile, true))) {
            bw.write(newWord);
            bw.newLine();
        } catch (IOException e) {
            Log.e("DictionaryManager", "Ошибка при записи в файл: " + e.getMessage());
            return "Ошибка при сохранении слова";
        }

        // 2) Добавляем слово в Trie
        dictionary.getTrie().insertWord(newWord);

        return "Слово успешно добавлено";
    }
}
