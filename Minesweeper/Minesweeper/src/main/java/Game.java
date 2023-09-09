import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Game {

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;//num of columns * the size of the tile to get pixel width
    int boardHeight = numRows * tileSize;//num of columns * the size of the tile to get pixel width

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int mineCount = 10;
    Random random = new Random();

    MineTile[][]board = new MineTile[numRows][numCols];//2D array to store where the mine tiles are
    ArrayList<MineTile>mineList;

    int tilesClicked = 0;//The goal of the game is to click all tiles except the ones containing the mines and this is the counter for it
    boolean gameOver = false;
    Game(){
        //frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);//set location to the center of the screen
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial",Font.BOLD,25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: There are " + Integer.toString(mineCount) + " mines, find them!");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel,BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows,numCols));//make a new grid layout for our board
        //boardPanel.setBackground(Color.green);
        frame.add(boardPanel);

        //fill the grid with mine tiles
        for (int row = 0; row < numRows; row++){
            for (int column = 0; column < numCols; column++){
                MineTile tile = new MineTile(row,column);
                board[row][column] = tile;//store the location of the mine tiles into our board 2d array

            tile.setFocusable(false);
            tile.setMargin(new Insets(0,0,0,0));
            tile.setFont(new Font("Serif", Font.PLAIN,45));
            //tile.setText(new String(Character.toChars(0x1F4A3)));//we input the emoji code
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(gameOver==true){//if game is already over then avoid this whole function
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();//we cast the get source in MineTile

                        //left click (check tiles)
                        if(e.getButton()== MouseEvent.BUTTON1){
                            if(tile.getText() == ""){
                                if(mineList.contains(tile)){//if user clicked on a mine then reveal all mines because it is game over
                                    revealMines();
                                }
                                else{
                                    checkMine(tile.row,tile.column);
                                }
                            }
                        }
                        //right click (place or remove flag)
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if(tile.getText() == "" && tile.isEnabled()){//we only want to allow the user to place a flag if the tile is empty or if the tile is enabled(so not revealed)
                                tile.setText(new String(Character.toChars(0x1F6A9)));//we input the emoji code for flag
                            }
                            else if (tile.getText().equals(new String(Character.toChars(0x1F6A9)))) {//if the tile already has a flag then remove it after right clicking again
                                tile.setText("");
                            }
                        }
                    }
                });
            boardPanel.add(tile);//finally add the tile to our board panel
            }
        }
        frame.setVisible(true);//we are putting this line of code at the end because sometimes all the tiles fail to load if this code is placed at the start of the program

        setMines();
    }
    private void setMines(){
        mineList = new ArrayList<MineTile>();

        int mineLeft = mineCount;
        while (mineLeft > 0){
            int row = random.nextInt(numRows);
            int column = random.nextInt(numCols);

            MineTile tile = board[row][column];//add the randomly generated coordinate to the mine tiles
            if(!mineList.contains(tile)){//this is to check if the tile already has a mine
                mineList.add(tile);
                mineLeft -=1;
            }
        }
    }
    private void revealMines(){
        for(int i = 0; i < mineList.size(); i++){
            MineTile tile = mineList.get(i);
            tile.setText(new String(Character.toChars(0x1F4A3)));//we input the emoji code for mine
        }

        gameOver = true;
        textLabel.setText("GAME OVER");
    }

    private void checkMine(int row, int column){
        if(row < 0 || row >= numRows || column <  0 || column >= numCols){//check to see if neighbouring tiles that we are trying to check are out of bounds
            return;
        }

        MineTile tile = board[row][column];

        if(!tile.isEnabled()){//if a tile has alredy been clicked on then return(this is when user opens a empty tile with no surrounding mines and program automatically starts to check other tiles around it)
            return;
        }

        tile.setEnabled(false);//when a user clicks on the empty tile disable the tile to be clicked again
        tilesClicked+=1;//when a empty tile is clicked add it to the tiles clicked counter

        int minesFound = 0;

        //check neighbouring tiles for bombs

        minesFound += countMine(row - 1,column - 1);//top left tile
        minesFound += countMine(row - 1,column);//top tile
        minesFound += countMine(row - 1,column + 1);//top right tile
        minesFound += countMine(row,column - 1);//left tile
        minesFound += countMine(row,column + 1);//right tile
        minesFound += countMine(row + 1,column - 1);//bottom left tile
        minesFound += countMine(row + 1,column);//bottom tile
        minesFound += countMine(row + 1,column + 1);//bottom right tile

        if(minesFound > 0){//if mines are found then set the text of the tile to the number of mines found
            tile.setText(Integer.toString(minesFound));
        }else{
            tile.setText("");//if no mines are found then put empty strings

            //then continue checking other tiles around it if they are also neighbours to mines

            checkMine(row - 1,column - 1);//top left tile
            checkMine(row - 1,column);//top tile
            checkMine(row - 1,column + 1);//top right tile
            checkMine(row,column - 1);//left tile
            checkMine(row,column + 1);//right tile
            checkMine(row + 1,column - 1);//bottom left tile
            checkMine(row + 1,column);//bottom tile
            checkMine(row + 1,column + 1);//bottom right tile
        }

        if(tilesClicked == numRows * numCols - mineList.size()){//if all tiles are opened or revealed(except for mines)
            gameOver = true;
            textLabel.setText("Mines Cleared! You Win!");
        }
    }

    private int countMine(int row, int column){
        if(row < 0 || row >= numRows || column <  0 || column >= numCols){//check to see if neighbouring tiles that we are trying to check are out of bounds
            return 0;
        }
        if(mineList.contains(board[row][column])){//if a neighbouring tile contains a mine return 1
            return  1;
        }
        return  0;//if not return 0
    }

    private class MineTile extends JButton{//fill the grid with clickable buttons class
        int row;
        int column;

        public MineTile(int row, int column){
            this.row = row;
            this.column = column;
        }
    }


}

