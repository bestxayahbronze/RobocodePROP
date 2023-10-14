// Import the necessary Robocode classes and utilities.
package edu.upc.epsevg.prop.robocode;
import robocode.*;
import static robocode.util.Utils.*;

// Define a class called "Droid" that extends the AdvancedRobot class.
public class Droid extends AdvancedRobot {

    // Initialize variables to be used throughout the robot's logic.
    double d = 150.0, dir = 1; // Distance and direction variables for movement.
    boolean sortintDeLaParet = false; // A flag to track if the robot is near a wall.

    String objectiu = "cap"; // The initial target is set to "none".
    double vidaObjectiu, distObjectiu; // Variables to store the target's health and distance.

    boolean escollintObjectiu = false; // Flag to determine if the robot is selecting a target.
    String tretAnterior = ""; // Keep track of the last robot that shot at this one.
    String altres[]; // An array to store names of other robots.
    double distAltres[]; // An array to store distances to other robots.

    // The main logic of the robot is implemented in the "run" method.
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        prepararPerEscollirObjectiu(); // Prepare to choose a target.

        while (true) {
            // Move the robot forward based on target distance and direction.
            setAhead((distObjectiu / 4 + 25) * dir);

            // Check if the robot is about to hit a wall and change direction.
            if (!sortintDeLaParet && aniraAImpactar(3)) {
                dir *= -1;
                sortintDeLaParet = true;
            } else if (!aniraAImpactar(3.2)) {
                sortintDeLaParet = false;
            }
            execute(); // Execute the movement and continue the loop.
        }
    }

    // The robot reacts to a scanned robot event, which is called when another robot is detected.
    public void onScannedRobot(ScannedRobotEvent e) {
        if (getOthers() == 1) {
            objectiu = e.getName(); // If there is only one other robot, set it as the target.
        } else {
            if (escollintObjectiu) {
                for (int i = 0; i < altres.length; i++) {
                    if (altres[i].equals(e.getName())) return;
                    if (altres[i].equals("")) {
                        altres[i] = e.getName();
                        distAltres[i] = e.getDistance();
                        if (i == altres.length - 1) {
                            escollirObjectiu(); // If all other robots are scanned, choose a target.
                        }
                        i = altres.length;
                    }
                }
                return;
            } else {
                if (!e.getName().equals(objectiu) && e.getEnergy() < vidaObjectiu &&
                    e.getDistance() <= distObjectiu) {
                    objectiu = e.getName(); // Change target based on certain conditions.
                }
            }
        }
        // Check if the target is not the wall, and adjust the robot's direction accordingly.
        if (!e.getName().equals(objectiu)) return;
        if (!sortintDeLaParet && vidaObjectiu -
            e.getEnergy() >= Rules.MIN_BULLET_POWER &&
            vidaObjectiu - e.getEnergy() <= Rules.MAX_BULLET_POWER &&
            !(vidaObjectiu - e.getEnergy() > 0.57 &&
                vidaObjectiu - e.getEnergy() < 0.63)) {
            dir *= -1;
        }
        // Adjust the robot's position and radar/gun direction based on the target.
        if (!aniraAImpactar(0.6)) {
            setTurnRight(e.getBearing() + 90 -
                (e.getDistance() > getHeight() * 3 ? 40 : 10) * dir);
        }
        double absBearing = e.getBearing() + getHeading();
        setTurnRadarRight(ajustarRadar(absBearing));
        setTurnGunRightRadians(ajustarArma(e) * 0.85);
        setFire(calcularPoderTret(e.getDistance())); // Fire a shot at the target.
        vidaObjectiu = e.getEnergy(); // Update target's health and distance.
        distObjectiu = e.getDistance();
    }

    // Handle the event when the robot is hit by a bullet.
    public void onHitByBullet(HitByBulletEvent e) {
        if (getOthers() > 1) {
            if ((distObjectiu > d || e.getName().equals(tretAnterior)) &&
                vidaObjectiu > 18) {
                objectiu = e.getName(); // Change target if certain conditions are met.
                double radarBearing = normalRelativeAngleDegrees((e.getBearing() +
                    getHeading()) - getRadarHeading());
                if (radarBearing > 0) {
                    setTurnRadarRight(Double.POSITIVE_INFINITY); // Rotate the radar right.
                } else {
                    setTurnRadarRight(Double.NEGATIVE_INFINITY); // Rotate the radar left.
                }
            }
            if (!e.getName().equals(objectiu)) tretAnterior = e.getName(); // Store the last shooter.
        }
    }

    // Handle the event when the robot hits another robot.
    public void onHitRobot(HitRobotEvent e) {
        dir *= -1; // Change direction after hitting another robot.
        if (!escollintObjectiu && !e.getName().equals(objectiu) &&
            e.getEnergy() < vidaObjectiu) {
            objectiu = e.getName(); // Change target if certain conditions are met.
            double radarBearing = normalRelativeAngleDegrees((e.getBearing() +
                getHeading()) - getRadarHeading());
            if (radarBearing > 0) {
                setTurnRadarRight(Double.POSITIVE_INFINITY);
            } else {
                setTurnRadarRight(Double.NEGATIVE_INFINITY);
            }
        }
    }

    // Handle the event when the robot hits a wall.
    public void onHitWall(HitWallEvent e) {
        sortintDeLaParet = true; // Set the wall flag to true.
        if (dir == -1 && Math.abs(e.getBearing()) >= 160.0) {
            dir = 1; // Change direction if close to the wall.
        } else if (dir == 1 && Math.abs(e.getBearing()) <= 20.0) {
            dir = -1; // Change direction if close to the wall.
        } else {
            if (dir == 1) {
                setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
                dir = -1; // Adjust direction to avoid the wall.
            } else {
                setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 180));
                dir = 1; // Adjust direction to avoid the wall.
            }
        }
    }

    // Handle the event when another robot is destroyed.
    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().equals(objectiu)) {
            if (getOthers() > 1) {
                prepararPerEscollirObjectiu(); // Prepare to choose a new target.
            } else {
                setTurnRadarRight(Double.POSITIVE_INFINITY); // Rotate the radar right.
            }
        } else if (escollintObjectiu) {
            prepararPerEscollirObjectiu(); // Prepare to choose a new target.
        }
    }

    // A helper method to initialize arrays and prepare to choose a target.
    void prepararPerEscollirObjectiu() {
        altres = new String[getOthers()];
        distAltres = new double[getOthers()];
        for (int i = 0; i < altres.length; i++) {
            altres[i] = ""; // Initialize the array with empty strings.
        }
        escollintObjectiu = true; // Set the target selection flag to true.
        setTurnRadarRight(Double.POSITIVE_INFINITY); // Rotate the radar right.
    }

    // A helper method to choose the best target from the available options.
    void escollirObjectiu() {
        escollintObjectiu = false; // Set the target selection flag to false.
        int robotEscollit = -1;
        double distanciaMesPetita = Double.MAX_VALUE;
        for (int i = 0; i < altres.length; i++) {
            if (distAltres[i] < distanciaMesPetita) {
                robotEscollit = i; // Choose the target with the shortest distance.
                distanciaMesPetita = distAltres[i];
            }
        }
        objectiu = altres[robotEscollit]; // Set the selected target.
    }

    // A helper method to adjust the radar direction.
    double ajustarRadar(double absBearing) {
        if (getOthers() > 1) {
            double correccioRadar;
            correccioRadar = normalRelativeAngleDegrees((absBearing - getRadarHeading()));
            correccioRadar += 22.5 * Math.signum(correccioRadar);

            if (correccioRadar > 45.0) {
                correccioRadar = 45.0;
            } else if (correccioRadar < -45.0) {
                correccioRadar = -45.0;
            } else if (correccioRadar > 0.0 && correccioRadar < 20.0) {
                correccioRadar = 20.0;
            } else if (correccioRadar > -20.0 && correccioRadar <= 0.0) {
                correccioRadar = -20.0;
            }
            return correccioRadar; // Adjust radar direction based on target's location.
        } else {
            return normalRelativeAngleDegrees((absBearing - getRadarHeading()) * 2);
        }
    }

    // A helper method to adjust the gun direction for targeting.
    double ajustarArma(ScannedRobotEvent e) {
        if (e.getEnergy() != 0.0) {
            double absBearingRad = getHeadingRadians() + e.getBearingRadians();
            double compensacioLineal = e.getVelocity() * Math.sin(e.getHeadingRadians() -
                absBearingRad) / Rules.getBulletSpeed(calcularPoderTret(e.getDistance()));
            if (e.getDistance() <= 12 * 10) {
                compensacioLineal *= 0.5;
            } else if (e.getDistance() <= 12 * 5) {
                compensacioLineal *= 0.3;
            }
            return normalRelativeAngle(absBearingRad -
                getGunHeadingRadians() + compensacioLineal); // Adjust gun direction.
        } else {
            return normalRelativeAngle(getHeadingRadians() +
                e.getBearingRadians() - getGunHeadingRadians()); // Adjust gun direction.
        }
    }

    // A helper method to calculate the power of the shot to be fired.
    double calcularPoderTret(double dObjectiu) {
        if (vidaObjectiu != 0.0) {
            if (dObjectiu < getHeight() * 1.5) {
                return Rules.MAX_BULLET_POWER;
            } else if (getEnergy() > 4 * Rules.MAX_BULLET_POWER + 2 * (Rules.MAX_BULLET_POWER - 1) ||
                (getOthers() == 1 && getEnergy() > 3 * Rules.MAX_BULLET_POWER)) {
                if (dObjectiu <= d * 2) {
                    return Rules.MAX_BULLET_POWER;
                } else {
                    return Math.min(1.1 + (d * 2) / dObjectiu, Rules.MAX_BULLET_POWER);
                }
            } else if (getEnergy() > 2.2) {
                return 1.1;
            } else {
                return Math.max(0.1, getEnergy() / 3);
            }
        } else {
            return 0.1;
        }
    }

    // A helper method to check if the robot will hit a wall.
    boolean aniraAImpactar(double marge) {
        return getX() + getHeight() * marge >= getBattleFieldWidth() ||
            getX() - getHeight() * marge <= 0.0 ||
            getY() + getHeight() * marge >= getBattleFieldHeight() ||
            getY() - getHeight() * marge <= 0.0; // Check if the robot is about to hit a wall.
    }
}