package edu.upc.epsevg.prop.robocode;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot; 
import java.awt.Color;
import java.awt.Graphics2D;

public class Aim extends TeamRobot{
    
    @Override
    public void run() {
        //fem independents els diferents elements del tanc
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while(true) {
            setTurnRadarRight(5000);
            execute();
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double finalRotate = aimCalc(event);
        if ((finalRotate > 5 || finalRotate < -5)) {
            setTurnGunRight(finalRotate);
        }
        //fire(Rules.MAX_BULLET_POWER);
    }
    
    double _X;
    double _Y;
    double _B;
    double X;
    double Y;
    public double aimCalc(ScannedRobotEvent event) {
       /* double Rotate = event.getBearing();
        double gunDir = getGunHeading();
        double Dir = getHeading();
        double a = Math.sqrt(64);
        a++;
        
        
        double finalRotate = Dir - gunDir + Rotate;
        
        
        
        if (finalRotate < -180) finalRotate = 360.0 - finalRotate;
        if (finalRotate > 180) finalRotate = -360.0 + finalRotate;
        return finalRotate;*/
        _X = Math.sin(Math.toRadians(event.getBearing() + getHeading())) * event.getDistance() + getX();
        _Y = Math.cos(Math.toRadians(event.getBearing() + getHeading())) * event.getDistance() + getY();
        double _A = Math.cos(Math.toRadians(event.getHeading())) / Math.sin(Math.toRadians(event.getHeading()));
        _B =  _Y-_X*_A;
        X = _X;
        Y = _X*_A + _B;
        double d = aprox(X, Y, event.getVelocity()), minX = X, minD = d, maxX = Double.MAX_VALUE, maxD = Double.MAX_VALUE;
        boolean goUp = false;
        X = X + 10 * Math.sin(Math.toRadians(event.getHeading()));
        Y = X*_A + _B;
        System.out.println(Math.sin(Math.toRadians(event.getHeading())));
        for (int i = 0; i < 20; i++) {
            d = aprox (X, Y, event.getVelocity());
            /*System.out.println("d: " + d);
            System.out.println("X: " + X);
            System.out.println("minX: " + minX);*/
            System.out.println("maxX: " + maxX);
            if (d > minD && d < 0) {
                minD = d;
                minX = X;
                goUp = false;
            }
            else if (d < maxD && d > 0) {
                maxD = d;
                maxX = X;
                goUp = true;
            }
            if (goUp == true){
                X = (minX + maxX) / 2;
                Y = X*_A + _B;
            }
            else {
                X = X + 200 * Math.sin(Math.toRadians(event.getHeading()));
                Y = Y + 200 * Math.cos(Math.toRadians(event.getHeading()));
            }
        }
        if (Math.abs(minD) < Math.abs(maxD)) X = minX;
        else X = maxX;
        Y = _A*X + _B;
        return 1;
    }
    
// Paint a transparent square on top of the last scanned robot
public void onPaint(Graphics2D g) {
    // Set the paint color to a red half transparent color
    g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
    int _drawX = (int) _X;
    int _drawY = (int) _Y;
    int drawX = (int) X;
    int drawY = (int) Y;
    // Draw a line from our robot to the scanned robot
    g.drawLine( _drawX,  _drawY, drawX, drawY);
    g.drawLine( -1000,(int)_B, 1000, (int)_B);
    // Draw a filled square on top of the scanned robot that covers it
    g.fillRect( drawX - 20,  drawY - 20, 40, 40);
}
    
   public double PowTwo(double a){
        return a*a;
    }
    
    public double aprox(double X, double Y, double speed) {
        
        return Math.sqrt( PowTwo((X - _X)) + PowTwo((Y - _Y))) - (speed * Math.sqrt( PowTwo((X)) + PowTwo((Y))) / Rules.getBulletSpeed(Rules.MIN_BULLET_POWER));    
    }
}

