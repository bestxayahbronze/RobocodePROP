/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.robocode;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

/**
 *
 * @author e9977173
 */
public class MyFirstRobot extends TeamRobot{
    
    @Override
    public void run() {
        //fem independents els diferents elements del tanc
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while(true) {
            setAhead(getBattleFieldWidth()*0.1);
            setTurnLeft(90);
            setTurnRadarRight(1000);
            execute();
            
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(Rules.MAX_BULLET_POWER);
    }
    //public void onScannedRobot ()
    
}
