package edu.susu.scrabble;


// Класс Cell представляет ячейку на доске Scrabble, которая может содержать плитку (Tile).
public class Cell
{
    private Tile tile;                  // Плитка, размещённая в ячейке.
    private Cell top, left, right, bottom; // Соседние ячейки (сверху, слева, справа, снизу).


    /*
        Некоторые ячейки имеют бонусы, которые влияют на подсчёт очков:
        - DL: двойные очки за букву,
        - TL: тройные очки за букву,
        - DW: двойные очки за слово,
        - TW: тройные очки за слово.
        */
    private String bonus;

    // Конструктор создаёт пустую ячейку без плитки и соседей.
    public Cell()
    {
        tile = null;
        top = left = right = bottom = null;

    }
    
   public void setTile(Tile tile)
   {
       this.tile = tile;
   }
    
   public Tile getTile()
   {
       return this.tile;
   }
   
   public void setBonus(String bonus)
   {
       this.bonus = bonus;
   }
   
   public String getBonus()
   {
       return this.bonus;
   }
    
   public void setTop(Cell cell)
   {
       this.top = cell;
   }
   
   public void setLeft(Cell cell)
   {
       this.left = cell;
   }
   
   public void setRight(Cell cell)
   {
       this.right = cell;
   }
   
   public void setBottom(Cell cell)
   {
       this.bottom = cell;
   }
   
   public Cell getTop()
   {
       return this.top;
   }
   
   public Cell getLeft()
   {
       return this.left;
   }
   
   public Cell getRight()
   {
       return this.right;
   }
    
   public Cell getBottom()
   {
       return this.bottom;
   }
   
}
