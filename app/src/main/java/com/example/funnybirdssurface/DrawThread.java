package com.example.funnybirdssurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class DrawThread extends Thread{
    private SurfaceHolder surfaceHolder;
    private int viewWidth;
    private int viewHeight;
    private int to_x;
    private int to_y;
    private int points = 0;
    private Sprite playerBird;
    private int lvl=1;
    private final int timerInterval = 30;
    private Sprite enemyBird;
    private Sprite coin;
    private String gameover="";
    public static boolean paused = false;
    public static boolean resumed = false;
    private double velPlayer;
    private Sprite boom;
    boolean action=false;
    Timer timer=new Timer();
    private volatile boolean running = true;//флаг для остановки потока

    public DrawThread(Context context, SurfaceHolder surfaceHolder) {
        Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        Bitmap c = BitmapFactory.decodeResource(context.getResources(),R.drawable.enemy);
        Bitmap d=BitmapFactory.decodeResource(context.getResources(),R.drawable.coin);
        Bitmap e=BitmapFactory.decodeResource(context.getResources(),R.drawable.bomb1);
        int w = b.getWidth()/5;
        int h = b.getHeight()/3;
        int w1=d.getWidth()/10;
        int h1=d.getHeight();
        int w2=e.getWidth()/5;
        int h2=e.getHeight()/2;

        Rect firstFrame = new Rect(0, 0, w, h);
        Rect secondFrame=new Rect(0,0,w1,h1);
        Rect thirdFrame=new Rect(0,0,w2,h2);
        playerBird = new Sprite(10, 0, 0, 200, firstFrame, b);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j * w, i * h, j * w + w, i * w + w));
            }
        }
        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, c);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue;
                }
                if (i ==2 && j == 0) {
                    continue;
                }
                enemyBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }
        coin=new Sprite(2000, 100, -300, 0, secondFrame, d);
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 10; j++) {
                coin.addFrame(new Rect(j * w1, i * h1, j * w1 + w1, i * w1 + w1));

            }
        }
        boom=new Sprite(2000,1000,-300,0,thirdFrame,e);
        for (int i = 0; i < 2; i++) {
            for (int j = 4; j>=0; j--) {
                boom.addFrame(new Rect(j*w2,i*h2,j*w2+w2,i*w2+w2));
            }
        }
        timer.start();
        this.surfaceHolder = surfaceHolder;
    }
    public void setView(int w,int h){
        viewWidth=w;
        viewHeight=h;
    }
    public void setXY(int x,int y,boolean act) {
        to_x=x;
        to_y=y;
        action=act;
    }
    public void requestStop() {
        running = false;
    }
    protected void update () {
        playerBird.update(timerInterval);
        enemyBird.update(timerInterval);
        coin.update(timerInterval);
        boom.update(timerInterval);
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVelocityY(-playerBird.getVelocityY());
        }
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVelocityY(-playerBird.getVelocityY());
        }
        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportEnemy (enemyBird);
            points +=10;
        }
        if (enemyBird.intersect(playerBird)) {
            teleportEnemy (enemyBird);
            points -= 40;
        }
        if (coin.getX() < - coin.getFrameWidth()) {
            teleportEnemy(coin);
        }
        if (coin.intersect(playerBird)) {
            teleportEnemy(coin);
            points += 10;
        }
        if (points >= 30 && points<60 && lvl!=2 && lvl!=3) {
            lvl = 2;
            uplvl(lvl);
            points=0;
        }
        if(points>=60 && points<90 && lvl!=3){
            lvl=3;
            uplvl(lvl);
            points=0;
        }
        if(points<=-60){
            GameOver();
        }
        if(paused && !gameover.equals("Game Over")){
            gameover="PAUSE";
            velPlayer=playerBird.getVelocityY();
            enemyBird.setVelocityX(0);
            coin.setVelocityX(0);
            playerBird.setVelocityY(0);
            boom.setVelocityX(0);
            resumed=false;
            paused=false;

        }
        if(resumed && !gameover.equals("Game Over")){
            gameover="";
            uplvl(lvl);
            playerBird.setVelocityY(velPlayer);
            paused=false;
            resumed=false;

        }
        if (boom.getX() < - boom.getFrameWidth()) {
            teleportEnemy(boom);
        }
        if(boom.intersect(playerBird)){
            teleportEnemy(boom);
            teleportEnemy(playerBird);
            GameOver();
        }
    }
    private void uplvl(int lvl){
        if(lvl!=1){
            enemyBird.setVelocityX(-300-100*lvl);
            boom.setVelocityX(-300-100*lvl);
        }
        else{
            enemyBird.setVelocityX(-300);
            boom.setVelocityX(-300);
        }
        coin.setVelocityX(-300);

    }
    private void GameOver(){
        gameover="Game Over";
        enemyBird.setVelocityX(0);
        coin.setVelocityX(0);
        playerBird.setVelocityY(0);
        boom.setVelocityX(0);
    }
    private void teleportEnemy (Sprite s) {
        s.setX(viewWidth + Math.random() * 500);
        s.setY(Math.random() * (viewHeight - s.getFrameHeight()));
    }
    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update ();

        }

        @Override
        public void onFinish() {
        }
    }
    @Override
    public void run() {
        while (running) {
            Canvas canvas = surfaceHolder.lockCanvas();
            viewWidth=canvas.getWidth();
            viewHeight=canvas.getHeight();
            if (canvas != null) {
                try {
                    synchronized (surfaceHolder) {
                        canvas.drawARGB(250, 127, 199, 255); // заливаем цветом
                        playerBird.draw(canvas);
                        enemyBird.draw(canvas);
                        coin.draw(canvas);
                        boom.draw(canvas);
                        Paint p = new Paint();
                        p.setAntiAlias(true);
                        p.setTextSize(55.0f);
                        p.setColor(Color.WHITE);
                        canvas.drawText(points+"", viewWidth - 100, 70, p);
                        canvas.drawText("lvl: "+lvl,viewWidth-300,70,p);
                        canvas.drawText(gameover,viewWidth/2,viewHeight/2,p);
                        if(action){
                            if (to_y < playerBird.getBoundingBoxRect().top) {
                                playerBird.setVelocityY(-200);
                                points--;
                                action=false;
                            }
                            else if (to_y> (playerBird.getBoundingBoxRect().bottom)) {
                                playerBird.setVelocityY(200);
                                points--;
                                action=false;
                            }
                            if ((to_y>=boom.getBoundingBoxRect().top&&to_y<=boom.getBoundingBoxRect().bottom)&&(to_y>=boom.getBoundingBoxRect().left&&to_x<=boom.getBoundingBoxRect().right)){
                                teleportEnemy(boom);
                                points+=5;
                                action=false;
                            }
                        }

                    }


                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

}
