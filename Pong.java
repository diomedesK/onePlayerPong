import java.awt.*;
import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class Pong{
    JFrame frame;
    GamePanel gameField;
    Racket racket = new Racket();

    List<Square> SquareList = new ArrayList<>();

    Color DEFAULT_BACKGROUND_COLOR = new Color(20, 20, 20);
    int SQUARE_SIDE = 10;

    double SquareXSpeed;
    double BaseRacketSpeed;
    int Score;

    boolean isGameOver;

    public static void main(String[] args){
        Pong gameInstance = new Pong();
        gameInstance.setGUI();
        gameInstance.setDefaultGameEnvironment();
        gameInstance.fillSquares(4);

        gameInstance.isGameOver = false;
        gameInstance.run();

    }

    public Pong(){
        frame = new JFrame("One Player Pong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
    }

    public void setGUI(){
        gameField = new GamePanel();

        gameField.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        gameField.setBackground(DEFAULT_BACKGROUND_COLOR);

        frame.add(gameField);
        frame.addKeyListener(new keyboardListener(racket));

        frame.setLocationRelativeTo(null);

        frame.pack();
        frame.setVisible(true);
    }

    public void setDefaultGameEnvironment(){
        Score = 0;
        BaseRacketSpeed = 7;
        SquareXSpeed = 2;
    }

    public void fillSquares(int squaresQuantity){

        for(int i = 0; i < squaresQuantity; i++){
            int startX, startY;

            startX = (int) (Math.random() * (frame.getWidth()/2) ) + 100;
            startY = (int) (Math.random() * (frame.getHeight()/2) ) + 100;

            Color color = new Color( (int) (Math.random() * 6777215) + 10000000);
            Square sq = new Square(startX, startY, SQUARE_SIDE, SQUARE_SIDE, color);

            SquareList.add(sq);
        }

    }

    public synchronized void run(){
        System.out.println("New game running");

        do{
            for(int i = 0; i < SquareList.size(); i++ ){
                Square sq = (Square) SquareList.get(i);
                blow(sq);
                if(sq.outOfScreen){
                    SquareList.remove(sq);
                    System.out.println(SquareList);
                }

            }

            if(SquareList.size() == 0){
                isGameOver = true;
            }

            try{
                Toolkit.getDefaultToolkit().sync(); //necessario para evitar lag
                Thread.sleep(10); //gargalo de velocidade
                frame.repaint();
            }catch(Exception e){
            }

        } while(true);


    }


    public class GamePanel extends JPanel{
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            if(isGameOver){
                gameOver(g);
            } else{

                g.setColor(Color.white);
                g.fillRect(racket.posX, racket.posY, racket.sizeX, racket.sizeY);

                for(Square sq : SquareList){
                    g.setColor(sq.color);
                    g.fillRect(sq.posX, sq.posY, sq.sizeX, sq.sizeY);
                    g.setColor(Color.white);
                }// end for

                g.setColor(Color.white);

                String MESSAGE = "Score: " + Score; 
                FontMetrics metrics = g.getFontMetrics(g.getFont());
                int x = (int) ((this.getWidth() - metrics.stringWidth(MESSAGE)) * 0.95);
                int y =  metrics.getAscent();
                g.drawString(MESSAGE, x, y);

            }

        }

        public void gameOver(Graphics g){
            String MESSAGE; 
            int x; int y;

            g.setColor(DEFAULT_BACKGROUND_COLOR);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            Font defaultFont = g.getFont();

            Map<TextAttribute, Object> attributes = new HashMap<>();

            attributes.put(TextAttribute.FAMILY, defaultFont.getFamily());
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            attributes.put(TextAttribute.SIZE, (int) (defaultFont.getSize() * 1.9));
            Font BoldCapitalFont = Font.getFont(attributes);

            MESSAGE = "GAME OVER";
            FontMetrics metrics = g.getFontMetrics(BoldCapitalFont);
            x = (this.getWidth() - metrics.stringWidth(MESSAGE)) / 2;
            y = ((this.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

            g.setFont(BoldCapitalFont);
            g.setColor(Color.white);
            g.drawString(MESSAGE, x, y);

            MESSAGE = "Sua pontuação: " + Score;

            x = (this.getWidth() - metrics.stringWidth(MESSAGE)) / 2;
            y = y + metrics.getHeight();
            g.drawString(MESSAGE, x, y);

            g.setColor(Color.white);
            g.drawString(MESSAGE, x, y);

            MESSAGE = "Pressione Enter para continuar";

            attributes.put(TextAttribute.FAMILY, defaultFont.getFamily());
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
            attributes.put(TextAttribute.SIZE, (int) (defaultFont.getSize() * 1.9));
            Font RegularFont = Font.getFont(attributes);


            metrics = g.getFontMetrics(RegularFont);
            x = (this.getWidth() - metrics.stringWidth(MESSAGE)) / 2;
            y = (int) (y*1.05) + metrics.getAscent();

            g.setFont(RegularFont);
            g.setColor(Color.white);
            g.drawString(MESSAGE, x, y);
        }


    }


    class keyboardListener implements KeyListener{

        Racket targetRacket;

        double ChangeFactor = 1.07;
        double racketSpeed = BaseRacketSpeed;

        int MaxRacketSpeed = 24;

        keyboardListener(Racket targetRacket){
            this.targetRacket = targetRacket;
        }

        public void keyPressed(KeyEvent e){
            int keyCode = e.getKeyCode();

            if(keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W){
                targetRacket.posY -= racketSpeed;
                if(racketSpeed < MaxRacketSpeed){ racketSpeed = racketSpeed * ChangeFactor; }
            }
            if(keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S){
                targetRacket.posY += racketSpeed;
                if(racketSpeed < MaxRacketSpeed){ racketSpeed = racketSpeed * ChangeFactor; }
            }

            if(isGameOver && keyCode == KeyEvent.VK_ENTER){
                fillSquares(2);
                setDefaultGameEnvironment();
                isGameOver = false;

            }

            int topThreshold = (0 - targetRacket.sizeY);
            int bottomThreshold = (gameField.getHeight() + targetRacket.sizeY);
            if(targetRacket.posY < topThreshold){
                System.out.println("Crossing topThreshold " + topThreshold);
                targetRacket.posY = bottomThreshold;
            }else if(targetRacket.posY > bottomThreshold){
                System.out.println("Crossing bottomThreshold " + bottomThreshold);
                targetRacket.posY = topThreshold;
            }

        }
        public void keyReleased(KeyEvent e){
            racketSpeed = BaseRacketSpeed;
        }
        public void keyTyped(KeyEvent e){
        }
    }



    public void blow(Square sq){

        if( (sq.posX + sq.sizeX) > gameField.getWidth()){
            sq.positiveX = false;
        }

        if( (sq.posX + sq.sizeX) < 0 ) {
            sq.outOfScreen = true;
        }

        if( (sq.posY + sq.sizeY) > gameField.getHeight()){
            sq.positiveY = false;
        }

        if( (sq.posY + sq.sizeY) < 0){
            sq.positiveY = true;
        }

        if((sq.posX > 0 ) & (0 + racket.posX + racket.sizeX) >= sq.posX  ){
            if((sq.posY >= (racket.posY - SQUARE_SIDE) ) & (sq.posY <= racket.posY + (racket.sizeY + SQUARE_SIDE) )){
                if(sq.positiveX == false){
                    Score++;
                }
                sq.positiveX = true;

                //Forma de aumentar a dificuldade do jogo de acordo com o grau de pontuação
                if(Score %2 == 0){
                    SquareXSpeed = SquareXSpeed * 1.1;
                    BaseRacketSpeed = BaseRacketSpeed * 1.1;
                    fillSquares(1);
                }

            }
        }

        if(sq.positiveX){
            sq.posX += SquareXSpeed;
        }
        else {
            sq.posX -=SquareXSpeed;
        }

        if(sq.positiveY){
            sq.posY += 3;
        }
        else{
            sq.posY -= 3;
        }

        SquareList.set(SquareList.indexOf(sq), sq);

    }

    class Square{
        int posX, posY, sizeX, sizeY;
        Color color = Color.white;

        boolean positiveX = true;
        boolean positiveY = true;
        boolean outOfScreen;

        Square(int posX, int posY, int sizeX, int sizeY, Color color){
            this.posX = posX;
            this.posY = posY;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.color = color;
        }//end constructor

        Square(int posX, int posY, int sizeX, int sizeY){
            this.posX = posX;
            this.posY = posY;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }//end constructor

        public void setX(int newX){
            this.posX = newX;
        }

        public void setY(int newY){
            this.posY = newY;
        }

    }// end square


    class Racket{
        public int posX, posY;

        Racket(){
            posX = 10;
        }

        public int sizeX = 5;
        public int sizeY = 80;
    }

}// end public class

