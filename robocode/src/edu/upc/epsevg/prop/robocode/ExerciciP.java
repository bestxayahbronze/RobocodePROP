package edu.upc.epsevg.prop.robocode;

import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import java.util.Random;

public class ExerciciP extends TeamRobot {
    private Random random = new Random();

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            // Random movement
            setAhead(100 + random.nextInt(50)); // Move forward randomly, s ha de modificar i posar que sigui el target
            setTurnRight(90 - random.nextInt(180)); // Turn randomly
            setTurnRadarRight(1000);
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double finalRotate = aimCalc(event);
        if (Math.abs(finalRotate) > 5) {
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
