package edu.susu.scrabble;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Класс Dictionary читает и хранит слова из файла (уже скопированного во внутреннюю память)
 * и использует Trie для быстрой проверки.
 */
public class Dictionary {
	Trie trie; // Структура данных Trie для хранения слов

	/**
	 * При создании Dictionary указываем:
	 * @param context - нужен, но фактически для совместимости
	 * @param internalFilePath - путь к файлу file.txt во внутреннем хранилище
	 */
	public Dictionary(Context context, String internalFilePath) {
		trie = new Trie();
		addFileToDictionary(internalFilePath);
	}

	/**
	 * Читаем словарь из файла (внутреннее хранилище).
	 */
	private void addFileToDictionary(String filePath) {
		try (FileInputStream fis = new FileInputStream(filePath);
			 Scanner sc = new Scanner(fis)) {
			while (sc.hasNextLine()) {
				String word = sc.nextLine().trim().toLowerCase();
				if (!word.isEmpty()) {
					trie.insertWord(word);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Проверка, есть ли слово в словаре.
	 */
	public boolean verifyWord(String word) {
		if (word == null) {
			return false;
		}
		return trie.searchWord(word.toLowerCase());
	}

	/**
	 * Доступ к Trie (для возможности insertWord снаружи, если надо).
	 */
	public Trie getTrie() {
		return trie;
	}
}
