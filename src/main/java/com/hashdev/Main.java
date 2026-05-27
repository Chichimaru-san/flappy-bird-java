package com.hashdev;
import javax.swing.*;


public class Main {
    static void main() throws Exception{
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("src/main/resources/img/flappybird.png");
        frame.setIconImage(icon.getImage());

        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        flappyBird.requestFocus(); // 3. Je prends l'attention du clavier tout de suite !
        frame.setVisible(true);

        }
    }
