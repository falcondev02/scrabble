package edu.susu.scrabble;

import java.util.ArrayList;
import java.util.Collections;

// Класс Bag представляет мешок для хранения и работы с набором буквенных плиток (tiles).
public class Bag {
	private ArrayList<Tile> tiles; // Список плиток, доступных в мешке.
	public static Tile swappedBlankTile; // Статическая переменная для хранения замененной пустой плитки.

	// Конструктор: инициализирует мешок, наполняет его плитками и перемешивает.
	public Bag() {
		tiles = new ArrayList<>(); // Создаем новый список плиток.
		populateBag(); // Заполняем мешок плитками с учетом их количества и очков.
		shuffleBag(); // Перемешиваем плитки в случайном порядке.
	}

	// Метод для получения следующей плитки из мешка.
	public Tile getNextTile() {
		return tiles.remove(0); // Удаляет и возвращает первую плитку в списке.
	}

	// Метод для проверки, пуст ли мешок.
	public boolean bagIsEmpty() {
		return tiles.size() < 1; // Если в мешке меньше 1 плитки, возвращаем true.
	}

	/*
    Метод для заполнения мешка плитками с учетом правил Scrabble:
    - 0 очков: пустая плитка (Blank) x2
    - 1 очко: E ×12, A ×9, I ×9, O ×8, N ×6, R ×6, T ×6, L ×4, S ×4, U ×4
    - 2 очка: D ×4, G ×3
    - 3 очка: B ×2, C ×2, M ×2, P ×2
    - 4 очка: F ×2, H ×2, V ×2, W ×2, Y ×2
    - 5 очков: K ×1
    - 8 очков: J ×1, X ×1
    - 10 очков: Q ×1, Z ×1
    */
	public void populateBag() {
		createTile("-", 0, 2); // Пустые плитки.
		createTile("E", 1, 12);
		createTile("A", 1, 9);
		createTile("I", 1, 9);
		createTile("O", 1, 8);
		createTile("N", 1, 6);
		createTile("R", 1, 6);
		createTile("T", 1, 6);
		createTile("L", 1, 4);
		createTile("S", 1, 4);
		createTile("U", 1, 4);
		createTile("D", 2, 4);
		createTile("G", 2, 3);
		createTile("B", 3, 2);
		createTile("C", 3, 2);
		createTile("M", 3, 2);
		createTile("P", 3, 2);
		createTile("F", 4, 2);
		createTile("H", 4, 2);
		createTile("V", 4, 2);
		createTile("W", 4, 2);
		createTile("Y", 4, 2);
		createTile("K", 5, 1);
		createTile("J", 8, 1);
		createTile("X", 8, 1);
		createTile("Q", 10, 1);
		createTile("Z", 10, 1);
	}

	// Метод для создания заданного количества плиток с определенной буквой и очками.
	public void createTile(String letter, int points, int count) {
		for (int i = 0; i < count; i++) {
			tiles.add(new Tile(letter, points)); // Добавляем плитку в список.
		}
	}

	// Метод для случайного перемешивания плиток в мешке.
	public void shuffleBag() {
		Collections.shuffle(tiles); // Используем встроенный метод для перемешивания списка.
	}

	// Метод для создания плитки с выбранной буквой в случае замены пустой плитки.
	public static void swapBlankTile(char chosenLetter) {
		if (chosenLetter == 'E') {
			swappedBlankTile = new Tile("E", 1);
		} else if (chosenLetter == 'A') {
			swappedBlankTile = new Tile("A", 1);
		} else if (chosenLetter == 'I') {
			swappedBlankTile = new Tile("I", 1);
		} else if (chosenLetter == 'O') {
			swappedBlankTile = new Tile("O", 1);
		} else if (chosenLetter == 'N') {
			swappedBlankTile = new Tile("N", 1);
		} else if (chosenLetter == 'R') {
			swappedBlankTile = new Tile("R", 1);
		} else if (chosenLetter == 'T') {
			swappedBlankTile = new Tile("T", 1);
		} else if (chosenLetter == 'L') {
			swappedBlankTile = new Tile("L", 1);
		} else if (chosenLetter == 'S') {
			swappedBlankTile = new Tile("S", 1);
		} else if (chosenLetter == 'U') {
			swappedBlankTile = new Tile("U", 1);
		} else if (chosenLetter == 'D') {
			swappedBlankTile = new Tile("D", 2);
		} else if (chosenLetter == 'G') {
			swappedBlankTile = new Tile("G", 2);
		} else if (chosenLetter == 'B') {
			swappedBlankTile = new Tile("B", 3);
		} else if (chosenLetter == 'C') {
			swappedBlankTile = new Tile("C", 3);
		} else if (chosenLetter == 'M') {
			swappedBlankTile = new Tile("M", 3);
		} else if (chosenLetter == 'P') {
			swappedBlankTile = new Tile("P", 3);
		} else if (chosenLetter == 'F') {
			swappedBlankTile = new Tile("F", 4);
		} else if (chosenLetter == 'H') {
			swappedBlankTile = new Tile("H", 4);
		} else if (chosenLetter == 'V') {
			swappedBlankTile = new Tile("V", 4);
		} else if (chosenLetter == 'W') {
			swappedBlankTile = new Tile("W", 4);
		} else if (chosenLetter == 'Y') {
			swappedBlankTile = new Tile("Y", 4);
		} else if (chosenLetter == 'K') {
			swappedBlankTile = new Tile("K", 5);
		} else if (chosenLetter == 'J') {
			swappedBlankTile = new Tile("J", 8);
		} else if (chosenLetter == 'X') {
			swappedBlankTile = new Tile("X", 8);
		} else if (chosenLetter == 'Q') {
			swappedBlankTile = new Tile("Q", 10);
		} else if (chosenLetter == 'Z') {
			swappedBlankTile = new Tile("Z", 10);
		}
	}
}