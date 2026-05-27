package com.hashdev;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Sounds
    Clip[] wingSounds = new Clip[3];
    int wingSoundIndex = 0;
    Clip hitSound;
    Clip pointSound;

    enum GameState {
        MENU, PLAYING, GAMEOVER
    }

    GameState currentState = GameState.MENU;
    /// I just don't understand why these are declared then redeclared in the bird class ?
    //Bird
    //position
    int birdX = boardWidth / 8; // ( at 1/8 of the board width)
    int birdY = boardHeight / 2; // at the half of the board height
    // bird's dimension
    int birdWidth = 34;
    int birdHeight = 24;


    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
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

        Pipe(Image img) {
            this.img = img;
        }
    }

    //score box dimension
    int boxX = (boardWidth / 2) - (260 / 2);
    int boxY = (boardHeight / 2) - (200 / 2);
    int boxWidth = 260;
    int boxHeight = 200;

    //game logic
    Bird bird;
    int velocityX = -4; //move the pipes to the left
    int velocityY = 0; //move bird up/down speed
    int gravity = 1;

    //bg
    int bgX = 0;
    int bgVelocity = -1;



    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;
    int highscore = 0;

    FlappyBird() throws Exception {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);   // 1. Je deviens capable d'écouter le clavier
        addKeyListener(this); // 2. Je connecte mes oreilles (les méthodes du clavier)

        //load Img
        backgroundImg = new ImageIcon(getClass().getResource("/img/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/img/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/img/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/img/bottompipe.png")).getImage();

        //load sounds
        URL wingURL = getClass().getResource("/audio/sfx_wing.wav");
        URL hitURL = getClass().getResource("/audio/vine-boom.wav");
        URL pointURL = getClass().getResource("/audio/sfx_point.wav");
        AudioInputStream hitStream = AudioSystem.getAudioInputStream(hitURL);
        AudioInputStream pointStream = AudioSystem.getAudioInputStream(pointURL);
        hitSound = AudioSystem.getClip();
        pointSound = AudioSystem.getClip();
        hitSound.open(hitStream);
        pointSound.open(pointStream);
        for (int i = 0; i < wingSounds.length; i++) {
            AudioInputStream wingStream = AudioSystem.getAudioInputStream(wingURL); // open audio stream
            wingSounds[i] = AudioSystem.getClip(); // init clips
            wingSounds[i].open(wingStream);


        }


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
//        if (currentState == GameState.PLAYING){
//            placePipesTimer.start();
//        }

        //game timer
        gameLoop = new Timer(1000 / 60, this); //60 frames each second
        gameLoop.start();

    }

    public void placePipes() {
        // the height of the pipe will be either /2, /4 or 3/4
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void paintComponent(Graphics g) { //" pinceau " pour dessiner
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {// methodes permettant de dessiner
        //bg
        g.drawImage(backgroundImg, bgX, 0, boardWidth, boardHeight, null);
        g.drawImage(backgroundImg, bgX + boardWidth, 0, boardWidth, boardHeight, null);

        //bird
//        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        /// Rotation of the bird
        //bird's rotation
        // 1. Calcul de l'angle de base (on multiplie par 0.04 pour adoucir la rotation)
        double angle = velocityY * 0.04;
        // 2. On bride l'angle pour que l'oiseau ne tourne pas à l'infini
        // Maximum 45 degrés en piquant du nez (0.78 radian)
        // Maximum 20 degrés en levant la tête (-0.35 radian)
        if (angle > 0.78) angle = 0.78;
        if (angle < -0.35) angle = -0.35;
        // 3. On crée une copie de notre pinceau pour pouvoir le manipuler librement
        Graphics2D g2d = (Graphics2D) g.create();

// 4. On déplace le point (0,0) de l'écran pile au centre de l'oiseau
        g2d.translate(bird.x + bird.width / 2, bird.y + bird.height / 2);

// 5. On fait pivoter toute la feuille de dessin selon notre angle
        g2d.rotate(angle);

// 6. On dessine l'oiseau centré sur ce nouveau point (0,0)
// On retire la moitié de la largeur et de la hauteur pour le recentrer parfaitement
        g2d.drawImage(birdImg, -bird.width / 2, -bird.height / 2, bird.width, bird.height, null);

// 7. TRÈS IMPORTANT : On détruit cette copie du pinceau pour ne pas déréglé le reste du jeu
        g2d.dispose();
        ///
        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        if (currentState == GameState.MENU) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("FLAPPY BIRD", boardWidth / 8, boardHeight / 4);
            g.setFont(new Font("Arial", Font.ITALIC, 15));
            g.drawString("Press SPACE to play", boardWidth / 4, boardHeight / 4 + 40);
        } else
            //Menu
            if (currentState == GameState.PLAYING) {
                g.setColor(Color.white);
                g.setFont(new Font("Consolas", Font.PLAIN, 32));
                g.drawString(String.valueOf((int) score), 10, 35);
            } else
                //score - GAME OVER
                if (currentState == GameState.GAMEOVER) {
                    if (highscore < (int) score) {
                        highscore = (int) score;
                    }
                    // gameOver dark layer
                    Color darkLayer = new Color(0, 0, 0, 125);
                    g.setColor(darkLayer);
                    g.fillRect(0, 0, boardWidth, boardHeight);

                    //score dialog box
                    g.setColor(new Color(222, 216, 149));
                    g.fillRect(boxX, boxY, boxWidth, boxHeight);
                    g.drawRect(boxX, boxY, boxWidth, boxHeight);

                    //box boarder
                    Graphics2D g2D = (Graphics2D) g;
                    g2D.setStroke(new BasicStroke(3));
                    g2D.setColor(new Color(171, 164, 47));
                    g2D.drawRect(boxX, boxY, boxWidth, boxHeight);
                    g2D.setStroke(new BasicStroke(3));
                    g2D.drawRect(boxX + 8, boxY + 8, boxWidth - 16, boxHeight - 16);

                    //dialog score text
                    g.setFont(new Font("Arial", Font.BOLD, 22));
                    g.setColor(Color.darkGray);
                    g.drawString("GAME OVER", boxX + 60, boxY + 50);

                    g.setFont(new Font("Arial", Font.PLAIN, 18));
                    g.drawString("Score: " + (int) score, boxX + 20, boxY + 100);
                    g.drawString("Highscore: " + highscore, boxX + 20, boxY + 150);

                    g.setFont(new Font("Arial", Font.ITALIC, 15));
                    g.setColor(Color.white);
                    g.drawString("Press SPACE to play again", boxX + 30, boxY + boxHeight + 50);

                }


    }

    public void move() {
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);
        //bg
        bgX += bgVelocity;
        if (bgX <= -boardWidth) {
            bgX = 0;
        }

        if (bird.y > boardHeight) {
            currentState = GameState.GAMEOVER;
        }
        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0.5 foreach pipe
                playSound(pointSound);
            }

            if (collision(bird, pipe)) {
                currentState = GameState.GAMEOVER;
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (currentState == GameState.PLAYING) {
            move();
        }
        repaint();
        if (currentState == GameState.GAMEOVER) {
            playSound(hitSound);
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x && //a's top right corner passes b's top left corner
                a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y; //a's bottom left corner passes b's top left corner
    }

    public void resetGame() {
        //restart by resetting the conditions
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameLoop.start();
        placePipesTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (currentState == GameState.MENU) {
                currentState = GameState.PLAYING;
                placePipesTimer.start();
            } else if (currentState == GameState.PLAYING) {
                velocityY = -10;
                playSound(wingSounds[wingSoundIndex]);
                wingSoundIndex = (wingSoundIndex + 1) % wingSounds.length;
            } else if (currentState == GameState.GAMEOVER) {
                resetGame();
                currentState = GameState.PLAYING;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyReleased(KeyEvent e) {
    }

}
