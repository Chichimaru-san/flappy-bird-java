package com.hashdev;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    enum GameState {
        MENU, PLAYING, GAMEOVER
    }
    /// I just don't understand why these are declared then redeclared in the bird class ?
    //Bird
    //position
    int birdX = boardWidth/8; // ( at 1/8 of the board width)
    int birdY = boardHeight/2; // at the half of the board height
    // bird's dimension
    int birdWidth = 34;
    int birdHeight = 24;



    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    //Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false; // to see if the bird collapsed the pipe
        Pipe(Image img){
            this.img = img;
        }
    }
    //game logic
    Bird bird;
    int velocityX = -4; //move the pipes to the left
    int velocityY = 0; //move bird up/down speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setFocusable(true);   // 1. Je deviens capable d'écouter le clavier
        addKeyListener(this); // 2. Je connecte mes oreilles (les méthodes du clavier)

        //load Img
        backgroundImg = new ImageIcon(getClass().getResource("/img/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/img/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/img/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/img/bottompipe.png")).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        //game timer
        gameLoop = new Timer(1000/60,this); //60 frames each second
        gameLoop.start();

    }

    public void placePipes(){
        // the height of the pipe will be either /2, /4 or 3/4
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g){ //" pinceau " pour dessiner
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){// methodes permettant de dessiner
        //bg
        g.drawImage(backgroundImg,0,0,boardWidth,boardHeight,null);

        //bird
        g.drawImage(bird.img,bird.x,bird.y,bird.width,bird.height,null);

        //pipes
        for(int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img,pipe.x,pipe.y,pipe.width,pipe.height,null);
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Consolas", Font.PLAIN, 32));
        if (gameOver){
            g.drawString("Game Over: " + String.valueOf((int)score),10,35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move(){
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y,0);

        if (bird.y > boardHeight){
            gameOver = true;
        }
        //pipes
        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width){
                pipe.passed = true;
                score += 0.5; // 0.5 foreach pipe
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver == true){
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    public boolean collision(Bird a, Pipe b){
        return a.x < b.x + b.width && //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x && //a's top right corner passes b's top left corner
                a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y; //a's bottom left corner passes b's top left corner
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE){
            velocityY = -9 ;
            if(gameOver) {
                //restart by resetting the conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}



    @Override
    public void keyReleased(KeyEvent e) {}

}
