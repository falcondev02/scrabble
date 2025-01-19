package edu.susu.scrabble;

import android.content.Context;

import java.util.ArrayList;
import java.util.Stack;

// Основной класс для проверки ходов на доске и подсчёта очков.
public class Engine {
	Player player; // Игрок, делающий ход.
	Board board; // Игровая доска.
	Dictionary dict; // Словарь для проверки легальности слов.

	// Стеки для хранения последовательности последних сыгранных плиток и клеток.
	Stack<Tile> recentlyPlayedTileStack = new Stack<>();
	Stack<Cell> recentlyPlayedCellStack = new Stack<>();
	ArrayList<Cell> occupiedCells = new ArrayList<>(); // Хранит клетки, занятые ранее, для проверки связности хода.

	Tile rackTileSelected; // Плитка, выбранная игроком из стойки.
	boolean initialMove = true; // Флаг для проверки, является ли ход первым.

	// Старый конструктор, который принимал context:
	// public Engine(Player player, Board board, Context context) {
	//     this.player = player;
	//     this.board = board;
	//     dict = new Dictionary(context);
	// }

	// Новый конструктор:
	public Engine(Player player, Board board, Dictionary dictionary) {
		this.player = player;
		this.board = board;
		this.dict = dictionary;
	}


	// Метод для проверки валидности хода.
	public boolean checkBoard() {
		// Проверка первого хода.
		if (initialMove && recentlyPlayedCellStack.size() == 1) {
			return false;
		}

		// Если не было сделано ни одного хода, ход невалиден.
		if (recentlyPlayedCellStack.isEmpty()) {
			return false;
		}

		// Проверяем, что центральная клетка занята (требование Scrabble).
		if (board.cellMatrix[7][7].getTile() == null) {
			return false;
		}

		// Проверяем связность хода по горизонтали или вертикали.
		if (!checkIfMoveIsConnectedHorizontally() && !checkIfMoveIsConnectedVertically()) {
			return false;
		}

		// Проверяем связь текущего хода с уже существующими ходами.
		if (!checkIfMoveIsConnectedToPastMoves()) {
			return false;
		}

		// Проверяем, являются ли все слова на доске валидными.
		if (!verifyHorizontalWords() || !verifyVerticalWords()) {
			return false;
		}

		// Подсчёт очков за текущий ход.
		if (checkIfMoveIsConnectedHorizontally() && !checkIfMoveIsConnectedVertically()) {
			Cell leftMost = getLeftMostOccupiedCellFrom(recentlyPlayedCellStack.peek());
			player.addScore(scoreHorizontalWord(leftMost) + scoreVerticalWordsConnectedToHorizontalMove());
		} else if (checkIfMoveIsConnectedVertically() && !checkIfMoveIsConnectedHorizontally()) {
			Cell topMost = getTopMostOccupiedCellFrom(recentlyPlayedCellStack.peek());
			player.addScore(scoreVerticalWord(topMost) + scoreHorizontalWordsConnectedToVerticalMove());
		} else if (checkIfMoveIsConnectedVertically() && checkIfMoveIsConnectedHorizontally()) {
			Cell leftMost = getLeftMostOccupiedCellFrom(recentlyPlayedCellStack.peek());
			Cell topMost = getTopMostOccupiedCellFrom(recentlyPlayedCellStack.peek());
			player.addScore(scoreHorizontalWord(leftMost) + scoreVerticalWord(topMost));
		}

		// Перемещаем клетки текущего хода в список занятых клеток.
		transferRecentlyPlayedCellStackToOccupiedCellsArrayList();
		clearStacks();
		initialMove = false;
		return true;
	}

	// Подсчитывает очки за горизонтальные слова, связанные с вертикальным ходом.
	public int scoreHorizontalWordsConnectedToVerticalMove()
	{
		Cell[] cellArray = copyRecentlyPlayedCellStackToArray();
		int score = 0;

		for(int i = 0; i < cellArray.length; i++)
		{
			//Get left most occupied cell of potential horizontal word connect to vertical move
			Cell leftMost = getLeftMostOccupiedCellFrom(cellArray[i]);
			score = score + scoreHorizontalWord(leftMost);
		}
		return score;
	}

	// Подсчитывает очки за вертикальные слова, связанные с горизонтальным ходом.
	public int scoreVerticalWordsConnectedToHorizontalMove()
	{
		Cell[] cellArray = copyRecentlyPlayedCellStackToArray();
		int score = 0;

		for(int i = 0; i < cellArray.length; i++)
		{
			//Gets top most occupied cell of potential vertical words connected to played tiles of horizontal move
			Cell topMost = getTopMostOccupiedCellFrom(cellArray[i]);
			score = score + scoreVerticalWord(topMost);
		}
		return score;
	}

	// Подсчитывает очки за слово, составленное по горизонтали.
	public int scoreHorizontalWord(Cell leftMost)
	{
		Cell current = leftMost;
		int wordMultiply = 1;
		int wordScore = 0;
		boolean hasOneTile = true; // Проверка, что слово состоит более чем из одной плитки.

		while(current.getTile() != null)
		{
			wordMultiply = wordMultiply + getWordBonus(current);
			wordScore = wordScore + (current.getTile().getPoints() * getLetterBonus(current));

			if (current.getRight() != null && current.getRight().getTile() != null) {
				current = current.getRight();
				hasOneTile = false; // У слова больше одной плитки.
			}
			else
			{
				break;
			}
		}

		if(hasOneTile)
		{
			return 0;
		}

		return wordScore * wordMultiply;
	}

	// Подсчитывает очки за слово, составленное по вертикали.
	public int scoreVerticalWord(Cell topMost)
	{
		Cell current = topMost;
		int wordMultiply = 1;
		int wordScore = 0;
		boolean hasOneTile = true;

		while(current.getTile() != null)
		{
			wordMultiply = wordMultiply + getWordBonus(current);
			wordScore = wordScore + (current.getTile().getPoints() * getLetterBonus(current));

			if(current.getBottom() != null)
			{
				if(current.getBottom().getTile() != null)
				{
					current = current.getBottom();
					hasOneTile = false;
				}
				else
				{
					break;
				}
			}
			else
			{
				break;
			}
		}

		if(hasOneTile == true)
		{
			return 0;
		}

		return wordScore * wordMultiply;
	}

	//In scoring method, we will update the total multiplication word bonus
	public int getWordBonus(Cell cell)
	{
		if(occupiedCells.contains(cell))
		{
			return 0;
		}
		else if(cell.getBonus() == "TW")
		{
			return 3;
		}
		else if(cell.getBonus() == "DW")
		{
			return 2;
		}

		return 0;
	}

	//In scoring method, we will update only letter on current cell with letter bonus
	public int getLetterBonus(Cell cell)
	{
		if(occupiedCells.contains(cell))
		{
			return 1;
		}
		else if(cell.getBonus() == "TL")
		{
			return 3;
		}
		else if(cell.getBonus() == "DL")
		{
			return 2;
		}

		return 1;
	}

	public void clearStacks()
	{
		recentlyPlayedTileStack.clear();
	}

	public void transferRecentlyPlayedCellStackToOccupiedCellsArrayList()
	{
		while(!recentlyPlayedCellStack.isEmpty())
		{
			occupiedCells.add(recentlyPlayedCellStack.pop());
		}
	}

	public Cell[] copyRecentlyPlayedCellStackToArray()
	{
		Cell[] cellArray = new Cell[recentlyPlayedCellStack.size()];
		recentlyPlayedCellStack.toArray(cellArray);

		return cellArray;
	}

	public boolean checkIfMoveIsConnectedToPastMoves()
	{
		//No past moves to be connected to
		if(occupiedCells.size() == 0)
		{
			return true;
		}

		Cell[] recentlyPlayedCellArray = convertRecentlyPlayedCellStackToArray();

		for(Cell currentCell : recentlyPlayedCellArray)
		{
			//FREEZES HERE
			if(traverseLeftUntilPastMove(currentCell) == true || traverseRightUntilPastMove(currentCell) == true || traverseUpUntilPastMove(currentCell) == true || traverseDownUntilPastMove(currentCell) == true )
			{
				return true;
			}
		}

		return false;
	}

	//If we verified the move as horizontal, we can traverse left of recently played cell to look for left-most occupied cell in order to build the word played to score it
	public Cell getLeftMostOccupiedCellFrom(Cell cell)
	{
		Cell current = cell;

		while(current.getLeft() != null)
		{
			if(current.getLeft().getTile() != null)
			{
				current = current.getLeft();
			}
			else
			{
				break;
			}
		}

		return current;
	}

	public Cell getTopMostOccupiedCellFrom(Cell cell)
	{
		Cell current = cell;

		while(current.getTop() != null)
		{
			if(current.getTop().getTile() != null)
			{
				current = current.getTop();
			}
			else
			{
				break;
			}
		}

		return current;
	}

	//traversing left of cell to see if connected to past moves
	public boolean traverseLeftUntilPastMove(Cell cell)
	{
		Cell current = cell;

		while(current.getLeft() != null)
		{
			if(current.getLeft().getTile() != null)
			{
				current = current.getLeft();

				if(occupiedCells.contains(current))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	//traversing right of cell to see if connected to past moves
	public boolean traverseRightUntilPastMove(Cell cell)
	{
		Cell current = cell;

		while(current.getRight() != null)
		{
			if(current.getRight().getTile() != null)
			{
				current = current.getRight();

				if(occupiedCells.contains(current))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	//traversing up of cell to see if connected to past moves
	public boolean traverseUpUntilPastMove(Cell cell)
	{
		Cell current = cell;

		while(current.getTop() != null)
		{
			if(current.getTop().getTile() != null)
			{
				current = current.getTop();

				if(occupiedCells.contains(current))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	//traversing down of cell to see if connected to past moves
	public boolean traverseDownUntilPastMove(Cell cell)
	{
		Cell current = cell;

		while(current.getBottom() != null)
		{
			if(current.getBottom().getTile() != null)
			{
				current = current.getBottom();

				if(occupiedCells.contains(current))
				{
					return true;
				}
			}
			else
			{
				break;
			}
		}
		return false;
	}

	//Checks if move is only horizontally connected by traversing left/right while counting number of matched played cells
	public boolean checkIfMoveIsConnectedHorizontally()
	{
		Cell currentCell = recentlyPlayedCellStack.peek();

		while(currentCell.getLeft() != null)
		{
			if(currentCell.getLeft().getTile() != null)
			{
				currentCell = currentCell.getLeft();
			}
			else
			{
				break;
			}
		}

		Cell[] recentlyPlayedCellArray = convertRecentlyPlayedCellStackToArray();
		int count = 0;

		if(isCellRecentlyPlayed(currentCell, recentlyPlayedCellArray) == true)
		{
			count++;
		}

		while(currentCell.getRight() != null)
		{
			if(currentCell.getRight().getTile() != null)
			{
				currentCell = currentCell.getRight();

				if(isCellRecentlyPlayed(currentCell, recentlyPlayedCellArray) == true)
				{
					count++;
				}
			}
			else
			{
				break;
			}
		}

		if(count == recentlyPlayedCellArray.length)
		{
			return true;
		}

		return false;
	}

	//Checks if move is only vertically connected by traversing up/down while counting number of matched played cells
	public boolean checkIfMoveIsConnectedVertically()
	{
		Cell currentCell = recentlyPlayedCellStack.peek();

		while(currentCell.getTop() != null)
		{
			if(currentCell.getTop().getTile() != null)
			{
				currentCell = currentCell.getTop();
			}
			else
			{
				break;
			}
		}

		Cell[] recentlyPlayedCellArray = convertRecentlyPlayedCellStackToArray();
		int count = 0;

		if(isCellRecentlyPlayed(currentCell, recentlyPlayedCellArray) == true)
		{
			count++;
		}

		while(currentCell.getBottom() != null)
		{
			if(currentCell.getBottom().getTile() != null)
			{
				currentCell = currentCell.getBottom();

				if(isCellRecentlyPlayed(currentCell, recentlyPlayedCellArray) == true)
				{
					count++;
				}
			}
			else
			{
				break;
			}
		}

		if(count == recentlyPlayedCellArray.length)
		{
			return true;
		}

		return false;
	}
	public boolean verifyHorizontalWords() {
		for (int row = 0; row < 15; row++) {
			StringBuilder sb = new StringBuilder();
			for (int col = 0; col < 15; col++) {
				Cell cell = board.cellMatrix[row][col];
				if (cell.getTile() != null) {
					// добавляем букву к sb
					sb.append(cell.getTile().getLetter());
				} else {
					// дошли до пустой клетки - проверяем, не получилась ли у нас подряд строка >=2 букв
					if (sb.length() > 1) {
						String word = sb.toString().toLowerCase();
						if (!dict.verifyWord(word)) {
							return false; // если такого слова нет в словаре, ход невалиден
						}
					}
					sb.setLength(0); // сбрасываем
				}
			}
			// в конце ряда проверяем, вдруг последнее слово тянулось до конца
			if (sb.length() > 1) {
				String word = sb.toString().toLowerCase();
				if (!dict.verifyWord(word)) {
					return false;
				}
			}
		}
		return true;
	}

	//Only check horizontally words on board if legal, despite being legally connected or not
	/*public boolean verifyHorizontalWords()
	{
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < 15; i++)
		{
			for(int j = 0; j < 15; j++)
			{
				Cell currentCell = board.cellMatrix[i][j];

				if(currentCell.getTile() != null)
				{
					sb.append(currentCell.getTile().getLetter());
				}
				else if((currentCell.getTile() == null && sb.length() > 1) || (j == 14 && sb.length() > 1))
				{
					String word = sb.toString();

					if(dict.verifyWord(word) == false)
					{
						System.out.println("Failed: " + word);
						sb.setLength(0);
						return false;
					}
					else
					{
						System.out.println("Passed: " + word);
						sb.setLength(0);
					}
				}
				else if((currentCell.getTile() == null && sb.length() == 1) || (j == 14 && sb.length() == 1))
				{
					sb.setLength(0);
				}
			}

			sb.setLength(0);
		}
		return true;
	}*/

	//Only check vertical words on board if legal, despite being legally connected or not
	public boolean verifyVerticalWords()
	{
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < 15; i++)
		{
			for(int j = 0; j < 15; j++)
			{
				Cell currentCell = board.cellMatrix[j][i];

				if(currentCell.getTile()!= null)
				{
					sb.append(currentCell.getTile().getLetter());
				}
				else if((currentCell.getTile() == null && sb.length() > 1) || (j == 14 && sb.length() > 1))
				{
					String word = sb.toString();

					if(dict.verifyWord(word) == false)
					{
						System.out.println("Failed: " + word);
						sb.setLength(0);
						return false;
					}
					else
					{
						System.out.println("Passed: " + word);
						sb.setLength(0);
					}
				}
				else if((currentCell.getTile() == null && sb.length() == 1) || (j == 14 && sb.length() == 1))
				{
					sb.setLength(0);
				}
			}

			sb.setLength(0);
		}
		return true;
	}

	public Cell[] convertRecentlyPlayedCellStackToArray()
	{
		int size = recentlyPlayedCellStack.size();
		Cell[] cellStack = new Cell[size];
		recentlyPlayedCellStack.toArray(cellStack);
		return cellStack;
	}

	public boolean isCellRecentlyPlayed(Cell currentCell, Cell[] cellArray)
	{
		if(currentCell == null)
		{
			return false;
		}

		for(Cell i : cellArray)
		{
			if(i == currentCell)
			{
				return true;
			}
		}

		return false;
	}

	public void undoLastMove() {
		// Проверяем, есть ли ходы для отмены
		if (recentlyPlayedTileStack.isEmpty() || recentlyPlayedCellStack.isEmpty()) {
			System.out.println("Nothing to undo.");
			return; // Если нечего отменять, просто выходим
		}

		// Восстанавливаем плитку в стойке игрока
		Tile lastTile = recentlyPlayedTileStack.pop();
		player.addTileToRack(lastTile);

		// Очищаем клетку, на которой была размещена плитка
		Cell lastCell = recentlyPlayedCellStack.pop();
		lastCell.setTile(null);

		System.out.println("Undo successful: Tile " + lastTile.getLetter() + " removed from board.");
	}

}