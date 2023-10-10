package edu.upc.epsevg.prop.robocode;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

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


/*package edu.upc.epsevg.prop.robocode;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

public class MyFirstRobot extends AdvancedRobot {
    private Point2D.Double closestEnemyPosition;

    
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
        double enemyBearing = getHeadingRadians() + event.getBearingRadians();
        double enemyX = getX() + event.getDistance() * Math.sin(enemyBearing);
        double enemyY = getY() + event.getDistance() * Math.cos(enemyBearing);
        Point2D.Double enemyPosition = new Point2D.Double(enemyX, enemyY);

        // If this enemy is closer than the previous closest, target it
        if (closestEnemyPosition == null || enemyPosition.distance(getX(), getY()) < closestEnemyPosition.distance(getX(), getY())) {
            closestEnemyPosition = enemyPosition;
            double enemyHeading = event.getHeadingRadians();
            double gunTurn = Utils.normalRelativeAngle(enemyBearing - getGunHeadingRadians());
            setTurnGunRightRadians(gunTurn);
            fire(Rules.MAX_BULLET_POWER);
        }
    }
}
*/