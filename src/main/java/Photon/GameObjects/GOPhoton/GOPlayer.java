package Photon.GameObjects.GOPhoton;

import Photon.*;
import Photon.Enums.DrawFigure;
import Photon.GameObjects.Bonus.GOPrism;
import Photon.GameObjects.Enemy.GOObstacle;

/**
 * Created by Serega on 03.03.2015.
 */
public class GOPlayer extends Photon {

    public float score = 0;
    public float scoreBonusBySecond = 1;
    public float scoreBonusByPrism = Game.gameConfiguration.scoreBonusByPrism;
    public float scoreBonusByObstacle = Game.gameConfiguration.scoreBonusByObstacle;
    public float factor = 1;
    public boolean superBonus = false;
    private float prismGravitationParameter = Game.gameConfiguration.prismGravitationParameter;
    private float obstacleGravitationParameter =  Game.gameConfiguration.obstacleGravitationParameter;
    public int prism = 0;
    public int obstacles = 0;
    public float score2 = 0;
    public int hitPoints  = 0;


    public boolean danger = false;
    private float penalty = 4;
    public String name;

    public boolean chosen = false;
    public boolean up;
    public boolean isBot = false;
    public boolean needToUpFreak = true;
    public int botLevel = 6;
    public int dodgedObstacle = 0;
    public int comboBonus = 0;
    public float funX = 0;
    public static float beginX = 50;
    public int counter;
    public static GOObstacle tempObstacle;



    private GOPlayer(float x, float y, float sx, DrawFigure figure, String name, int color, boolean isBot) {
        this.x = x;
        this.funX = x;
        this.y = y;
        this.defaultY = y;
        this.playerYShift = y;
        this.sx = sx;
        this.sy = sx;
        defaultSx = sx;
        this.figure = figure;
        this.name = name;
        this.color = color;
        this.defaultColor = color;
        this.isBot = isBot;
        this.t = 0;

        float temp = lengthTrajectory / Game.moveOnStep -1;
        for(int i = 0; ; i++) {
            path.add(new GOPoint(i * Game.moveOnStep*1.5f, defaultY, this));
            if(i * Game.moveOnStep > Main.dWidth)
                break;
        }
    }

    private void gameOver() {
        Game.gameOver();
    }

    private GOPlayer() {
    }

    public float myFunction(float tempT) {
        float tempY = (float) Math.sin(tempT);
        tempY *= amplitude;
        tempY += playerYShift;
        return tempY;
    }



    public void update() {
        updateLvlConfiguration();
        if(this.getX() < Main.game.blackHole.sx - 5) {

            gameOver();
            return;
        }
        if(immortalityDie > 0) {
            immortalityDie -= Main.delay;
        }
        else {
            die = false;
            color = defaultColor;
            opacity = 1f;
        }
        if(die) {
            opacity = 0.5f;
                if(immortalityDie > timeToRecovery/2)
                    sx = defaultSx + timeToRecovery/500f - immortalityDie /500f * (1);
                else {
                    sx = defaultSx + immortalityDie /500f;
                }
            sy = sx;
        }
        else {
            setScore(scoreBonusBySecond);
            score2 = dodgedObstacle + hitPoints;


        }

        if(Game.controlMode == 2 && freak >= minFreak)
            freak -= 0.01;




        checkCollisions();
        move();
        for (GOPoint p : path) {
            p.move();
        }
    }
    public void updateLvlConfiguration() {
        minFreak = Main.game.gameConfiguration.minFreak;
        maxFreak = Main.game.gameConfiguration.maxFreak;
        prismGravitationParameter = Game.gameConfiguration.prismGravitationParameter;
        obstacleGravitationParameter =  Game.gameConfiguration.obstacleGravitationParameter;
        Main.game.blackHole.gravitationParameter = Game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(this)];
    }

    @Override
    public void move() {
        if (isBot) {



            for (GOObstacle obstacle : Game.obstacles) {
                if (obstacle.x > this.x) {
                    tempObstacle = obstacle;
                    break;
                }
            }
            if (tempObstacle != null) {
                for( int iteration = 0; iteration <3; iteration++) {
                    danger = isClashWith(tempObstacle);
                    // System.out.println(tempObstacle.x + "X");
                    if ((!danger) && (!chosen)) {
                        if (Math.random() > 0.5) {
                            up = true;
                        } else {
                            up = false;
                        }
                        chosen = true;
                    }

                    if (danger) {
                        if (!up) {
                            if (freak > (minFreak+0.08)) {
                                freak -= Game.freakChanger;
                            } else {
                                up = true;
                            }
                        }
                        if (up) {
                            if (freak < (maxFreak - 0.08)) {
                                freak += Game.freakChanger;
                            } else {
                                up = false;
                            }
                        }

                    }

                }
            }

        }

        if(Game.players.size() <= 5) {
            setX(funX - (Main.game.blackHole.gravitationPower) / Main.fps/* * (1 + 1/this.x)*/);

        }
        t += freak;
        y = myFunction(t);
        if (Math.abs(shiftObAlongX) > 0) {
            if (Math.abs(shiftObAlongX) < 0.5)
                shiftObAlongX = 0;
            shiftObAlongX -= 0.2 * Math.signum(shiftObAlongX);
        }

    }
    public void setX(float newX) {
        funX = newX;
        if(newX >= beginX) {
            float deltaX = -20 + 0;
            newX -= beginX;
            x = (float) (1f - 1f / (0.04f*newX + 1f)) * deltaX + beginX;
        }
        else
            x = newX;
    }
    public float getX() {
        return x+Draw.xshift;
    }
    public float getXForPoint() {
        return x;
    }
//    public float getX() {
//        return funX;
//    }
    public void checkCollisions() {
        for(GOObstacle ob : Game.obstacles) {
            if(Physics.checkCollisions(this, ob) && !die && !ob.die && !immortal) {
                ob.collision();
                if (isBot) {
                    System.out.println("BOTCOLISION"+counter);
                    counter++;
                }
                collisionWithObstacle();
                break;
            }
            if(Physics.checkCollisions(this, ob)) {
            }
        }
        for(GOPrism bonus : Game.bonuses) {
            if(Physics.checkCollisions(this, bonus) && !die && !bonus.die && !immortal) {
                bonus.collision();
                collisionWithPrism();
                break;
            }
        }
    }
    @Override
    public void collision() {

    }
    public void collisionWithObstacle() {
        if((Main.game.gameConfiguration.playersAmount + Main.game.gameConfiguration.isBot) <= 1) {
            obstacles++;
            immortalityDie = timeToRecovery;
            die = true;
            color = 1;
            Main.game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(this)] += obstacleGravitationParameter;
//        setScore(scoreBonusByObstacle);
            comboBonus = 0;
        } else {
            for(GOPlayer curPlayer : Main.game.players) {
                float parFactorT = 0.2f;
                if(curPlayer != this) {
                    Main.game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(curPlayer)]
                            -= obstacleGravitationParameter / (Main.game.gameConfiguration.playersAmount + Main.game.gameConfiguration.isBot - 1) *parFactorT;
                }
                else {
                    Main.game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(curPlayer)]
                            += obstacleGravitationParameter *parFactorT;
                }
            }
        }
    }
    public void collisionWithPrism() {
        if((Main.game.gameConfiguration.playersAmount + Main.game.gameConfiguration.isBot) <= 1) {
            prism++;
            comboBonus++;
            setScore(scoreBonusByPrism);
            Game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(this)] += prismGravitationParameter;
        } else {
            for (GOPlayer curPlayer : Main.game.players) {
                if(curPlayer != this)
                    Main.game.gameConfiguration.gravitationParameter[Main.game.players.indexOf(curPlayer)] -= prismGravitationParameter / (Main.game.gameConfiguration.playersAmount + Main.game.gameConfiguration.isBot- 1) / 3;
            }
        }
    }


    public void render() {
        Draw.draw(figure, x, y, sx, sy, 0, color, opacity);
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setColor(int color) {
        this.color = color;
        this.defaultColor = color;
    }
    public boolean isClashWith(GOObstacle obstacle) {
        for (GOPoint point : this.path) {
            if (Physics.checkCollisions(obstacle, new GOPoint(point.getX(), point.getY(), this.getSx()*1.5f, this.getSy()*1.5f, this))) {
                return true;
            }
        }
        return false;
    }

    public void setScore(float a) {
        float delta = Main.game.blackHole.x + Main.game.blackHole.sx/2;
//        factor = (float) (1 + (x-delta) / (100-delta) * 2 * (superBonus?2:1) * (Math.pow(Game.level, 0.3f)/3)); //
        factor = 0.1f;
        score+=a*factor*(comboBonus+1)*(int)(0.3f+Main.game.level * 0.7f);
    }
    public void setDodgedObstacle() {
        if(immortalityDie <= 0) {
            dodgedObstacle++;
//            score += 50;
        }
    }


    public static PlayerBuilder newBuilder() {
        return new GOPlayer().new PlayerBuilder();
    }

    public void iAdded()  {
            System.out.println("Photon was added!");

    }

    public class PlayerBuilder {

        private PlayerBuilder() {
        }
        public GOPlayer build() {
            GOPlayer.this.funX = x;
            GOPlayer.this.defaultY = y;
            GOPlayer.this.defaultSx = sx;
            GOPlayer.this.defaultColor = color;
            GOPlayer.this.t = 0;
            for(int i = 0; ; i++) {
                path.add(new GOPoint(i * Game.moveOnStep*1.5f, defaultY, GOPlayer.this));
//            path.add(new GOPoint(i * 0.75f, defaultY, this));
                if(i * Game.moveOnStep > Main.dWidth)
                    break;
            }
            return GOPlayer.this;
        }
        public PlayerBuilder setXStart(float x) {
            GOPlayer.this.x = x;
            return this;
        }
        public PlayerBuilder setYStart(float y) {
            GOPlayer.this.y = y;
            return this;
        }
        public PlayerBuilder setSize(float sx) {
            GOPlayer.this.sx = sx;
            GOPlayer.this.sy = sx;
            return this;
        }
        public PlayerBuilder setFigure(DrawFigure figure) {
            GOPlayer.this.figure = figure;
            return this;
        }
        public PlayerBuilder setName(String name) {
            GOPlayer.this.name = Game.playerName.toString();
            return this;
        }
        public PlayerBuilder setColor(int color) {
            GOPlayer.this.color = color;
            return this;
        }
        public PlayerBuilder isBot(boolean isBot) {
            GOPlayer.this.isBot = isBot;
            return this;
        }

    }
}
