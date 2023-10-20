package edu.upc.epsevg.prop.robocode;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class Move extends TeamRobot{
    
    @Override
    public void run() {
        //fem independents els diferents elements del tanc
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while(true) {
            setAhead(getBattleFieldWidth()*0.05);
            //setTurnLeft(90);
            setTurnRadarRight(1000);
            execute();
            
        }
    }
}