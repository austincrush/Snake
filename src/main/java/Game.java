import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Game extends JPanel implements ActionListener, KeyListener {

    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private final int frameWidth = 500;
    private final int frameHeight = 600;
    private final int gameBoardWidth = 500;
    private final int gameBoardHeight = 500;
    private final int tileSize = 25;

    private Tile snakeHead;
    private ArrayList<Tile> snakeBody;
    private Tile fruit;
    private Random random = new Random();
    private Timer timer;
    private Label highScore;
    private Label score;
    private JButton replay;
    private int xVelocity;
    private int yVelocity;
    private int currentScore;
    private int currentHighScore;
    private boolean gameOver = false;
    private Scanner scanner;

    public Game() throws FileNotFoundException {

        JFrame frame = new JFrame("SNAKE");
        frame.setVisible(true);
        frame.setSize(frameWidth, frameHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);

        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setBackground(Color.decode("#303030"));
        addKeyListener(this);
        setFocusable(true);
        setLayout(null);

        snakeHead = new Tile(2, 10);
        snakeBody = new ArrayList<>();
        fruit = new Tile(0, 0);
        generateFruit();

        xVelocity = 0;
        yVelocity = 0;

        currentScore = 1;
        currentHighScore = Integer.parseInt(readHighScore());

        highScore = new Label();
        this.add(highScore);
        highScore.setText("High Score: " + readHighScore());
        highScore.setLocation(50, 525);
        highScore.setSize(200, 50);
        highScore.setFont(new Font("SansSerif", Font.BOLD , 20));

        score = new Label();
        this.add(score);
        score.setText("Score: " + currentScore);
        score.setLocation(350, 525);
        score.setSize(200, 50);
        score.setFont(new Font("SansSerif", Font.BOLD, 20));

        timer = new Timer(100, this);
        timer.start();

    }

    public void paint(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void generateFruit() {
        fruit.x = random.nextInt(gameBoardWidth / tileSize);
        fruit.y = random.nextInt(gameBoardHeight / tileSize);
    }

    public void draw(Graphics g) {
        for(int i = 0; i < gameBoardWidth/tileSize; i++) {
            g.setColor(Color.DARK_GRAY);
            g.drawLine(i * tileSize, 0, i * tileSize, gameBoardHeight);
            g.drawLine(0, i * tileSize, gameBoardWidth, i * tileSize);
        }
        g.drawLine(0, 500, 500, 500);

        g.setColor(Color.GREEN);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, false);

        g.setColor(Color.RED);
        g.fillOval(fruit.x * tileSize, fruit.y * tileSize, tileSize, tileSize);

        for (int i = 0; i < snakeBody.size(); i++) {
            Tile part = snakeBody.get(i);
            g.setColor(Color.GREEN);
            g.fill3DRect(part.x*tileSize, part.y*tileSize, tileSize, tileSize, false);
        }

    }

    public void move() {

        if(collision(snakeHead, fruit)) {
            snakeBody.add(new Tile(fruit.x, fruit.y));
            generateFruit();
            currentScore++;
            updateCurrentScore();
        }

        for(int i = snakeBody.size()-1; i >= 0; i--) {
            Tile part = snakeBody.get(i);
            if(i == 0) {
                part.x = snakeHead.x;
                part.y = snakeHead.y;
            }
            else {
                Tile prevPart = snakeBody.get(i-1);
                part.x = prevPart.x;
                part.y = prevPart.y;
            }
        }

        snakeHead.x += xVelocity;
        snakeHead.y += yVelocity;

        for(int i = 0; i < snakeBody.size(); i++) {
            Tile part = snakeBody.get(i);
            if(collision(snakeHead, part)) {
                gameOver = true;
            }
        }

        if (snakeHead.y * tileSize < 0 || snakeHead.y * tileSize > gameBoardHeight - tileSize
                || snakeHead.x * tileSize < 0 || snakeHead.x * tileSize > gameBoardWidth - tileSize){
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return (tile1.x == tile2.x && tile1.y == tile2.y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver) {
            timer.stop();
            endGame();
            updateHighScore();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_UP && yVelocity != 1) {
            xVelocity = 0;
            yVelocity = -1;
        }
        else if(key == KeyEvent.VK_DOWN && yVelocity != -1) {
            xVelocity = 0;
            yVelocity = 1;
        }
        else if(key == KeyEvent.VK_RIGHT && xVelocity != -1) {
            xVelocity = 1;
            yVelocity = 0;
        }
        else if(key == KeyEvent.VK_LEFT && xVelocity != 1) {
            xVelocity = -1;
            yVelocity = 0;
        }
    }

    public void endGame() {

    }

//    public void replay() throws FileNotFoundException {
//        Game game = new Game();
//    }

    public void updateHighScore() {
        if(currentScore > currentHighScore) {
            try {
                FileWriter writer = new FileWriter("src/main/resources/HighScore.txt", false);
                writer.write(String.valueOf(currentScore));
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readHighScore() {
        String line;
        try {
            File file = new File("src/main/resources/HighScore.txt");
            scanner = new Scanner(file);
            line = scanner.nextLine();
        } catch(FileNotFoundException e ) {
            return "";
        }
        return line;
    }

    public void updateCurrentScore() {
        this.add(score);
        score.setText("Score: " + currentScore);
        score.setLocation(350, 525);
        score.setSize(200, 50);
        score.setFont(new Font("SansSerif", Font.BOLD, 20));
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
