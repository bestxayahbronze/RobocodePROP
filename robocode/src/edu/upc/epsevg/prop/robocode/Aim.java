package edu.upc.epsevg.prop.robocode;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class Aim extends TeamRobot{
    
    @Override
    public void run() {
        //fem independents els diferents elements del tanc
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while(true) {
            setTurnRadarRight(1000);
            execute();
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double finalRotate = aimCalc(event);
        if ((finalRotate > 5 || finalRotate < -5)) {
            setTurnGunRight(finalRotate);
        }
        fire(Rules.MAX_BULLET_POWER);
    }
    
    public double aimCalc(ScannedRobotEvent event) {
        double Rotate = event.getBearing();
        double gunDir = getGunHeading();
        double Dir = getHeading();
        
        
        double finalRotate = Dir - gunDir + Rotate;
        
        
        
        if (finalRotate < -180) finalRotate = 360.0 - finalRotate;
        if (finalRotate > 180) finalRotate = -360.0 + finalRotate;
        return finalRotate;
    }
}