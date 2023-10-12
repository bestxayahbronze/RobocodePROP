package edu.upc.epsevg.prop.robocode;

import robocode.*;
import static robocode.util.Utils.*;

public class Droid extends AdvancedRobot {

  double d = 150.0, dir = 1;
  boolean sortintDeLaParet = false;

  String objectiu = "cap";
  double vidaObjectiu, distObjectiu;

  boolean escollintObjectiu = false;
  String tretAnterior = "";
  String altres[];
  double distAltres[];

  public void run() {
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    prepararPerEscollirObjectiu();
    while (true) {
      setAhead((distObjectiu / 4 + 25) * dir);
      if (!sortintDeLaParet && aniraAImpactar(3)) {
        dir *= -1;
        sortintDeLaParet = true;
      } else if (!aniraAImpactar(3.2)) {
        sortintDeLaParet = false;
      }
      execute();
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    if (getOthers() == 1) {
      objectiu = e.getName();
    } else {
      if (escollintObjectiu) {
        for (int i = 0; i < altres.length; i++) {
          if (altres[i].equals(e.getName())) return;
          if (altres[i].equals("")) {
            altres[i] = e.getName();
            distAltres[i] = e.getDistance();
            if (i == altres.length - 1) {
              escollirObjectiu();
            }
            i = altres.length;
          }
        }
        return;
      } else {
        if (!e.getName().equals(objectiu) && e.getEnergy() < vidaObjectiu 
          && e.getDistance() <= distObjectiu) {
          objectiu = e.getName();
        }
      }
    }
    if (!e.getName().equals(objectiu)) return;
    if (!sortintDeLaParet && vidaObjectiu 
        - e.getEnergy() >= Rules.MIN_BULLET_POWER 
        && vidaObjectiu - e.getEnergy() <= Rules.MAX_BULLET_POWER 
        && !(vidaObjectiu - e.getEnergy() > 0.57 
        && vidaObjectiu - e.getEnergy() < 0.63)) {
      dir *= -1;
    }
    if (!aniraAImpactar(0.6)) {
      setTurnRight(e.getBearing() + 90 
        - (e.getDistance() > getHeight() * 3 ? 40 : 10) * dir);
    }
    double absBearing = e.getBearing() + getHeading();
    setTurnRadarRight(ajustarRadar(absBearing));
    setTurnGunRightRadians(ajustarArma(e) * 0.85);
    setFire(calcularPoderTret(e.getDistance()));
    vidaObjectiu = e.getEnergy();
    distObjectiu = e.getDistance();
  }

  public void onHitByBullet(HitByBulletEvent e) {
    if (getOthers() > 1) {
      if ((distObjectiu > d || e.getName().equals(tretAnterior)) 
        && vidaObjectiu > 18) {
        objectiu = e.getName();
        double radarBearing = normalRelativeAngleDegrees((e.getBearing() 
          + getHeading()) - getRadarHeading());
        if (radarBearing > 0) {
          setTurnRadarRight(Double.POSITIVE_INFINITY);
        } else {
          setTurnRadarRight(Double.NEGATIVE_INFINITY);
        }
      }
      if (!e.getName().equals(objectiu)) tretAnterior = e.getName();
    }
  }

  public void onHitRobot(HitRobotEvent e) {
    dir *= -1;
    if (!escollintObjectiu && !e.getName().equals(objectiu) 
      && e.getEnergy() < vidaObjectiu) {
      objectiu = e.getName();
      double radarBearing = normalRelativeAngleDegrees((e.getBearing() 
        + getHeading()) - getRadarHeading());
      if (radarBearing > 0) {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      } else {
        setTurnRadarRight(Double.NEGATIVE_INFINITY);
      }
    }
  }

  public void onHitWall(HitWallEvent e) {
    sortintDeLaParet = true;
    if (dir == -1 && Math.abs(e.getBearing()) >= 160.0) {
      dir = 1;
    } else if (dir == 1 && Math.abs(e.getBearing()) <= 20.0) {
      dir = -1;
    } else {
      if (dir == 1) {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
        dir = -1;
      } else {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 180));
        dir = 1;
      }
    }
  }

  public void onRobotDeath(RobotDeathEvent e) {
    if (e.getName().equals(objectiu)) {
      if (getOthers() > 1) {
        prepararPerEscollirObjectiu();
      } else {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      }
    } else if (escollintObjectiu) {
      prepararPerEscollirObjectiu();
    }
  }

  void prepararPerEscollirObjectiu() {
    altres = new String[getOthers()];
    distAltres = new double[getOthers()];
    for (int i = 0; i < altres.length; i++) {
      altres[i] = "";
    }
    escollintObjectiu = true;
    setTurnRadarRight(Double.POSITIVE_INFINITY);
  }

  void escollirObjectiu() {
    escollintObjectiu = false;
    int robotEscollit = -1;
    double distanciaMesPetita = Double.MAX_VALUE;
    for (int i = 0; i < altres.length; i++) {
      if (distAltres[i] < distanciaMesPetita) {
        robotEscollit = i;
        distanciaMesPetita = distAltres[i];
      }
    }
    objectiu = altres[robotEscollit];
  }

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
      return correccioRadar;
    } else {
      return normalRelativeAngleDegrees((absBearing - getRadarHeading()) * 2);
    }
  }

  double ajustarArma(ScannedRobotEvent e) {
    if (e.getEnergy() != 0.0) {
      double absBearingRad = getHeadingRadians() + e.getBearingRadians();
      double compensacioLineal = e.getVelocity() * Math.sin(e.getHeadingRadians() 
        - absBearingRad) / Rules.getBulletSpeed(calcularPoderTret(e.getDistance()));
      if (e.getDistance() <= 12 * 10) {
        compensacioLineal *= 0.5;
      } else if (e.getDistance() <= 12 * 5) {
        compensacioLineal *= 0.3;
      }
      return normalRelativeAngle(absBearingRad 
        - getGunHeadingRadians() + compensacioLineal);
    } else {
      return normalRelativeAngle(getHeadingRadians() 
        + e.getBearingRadians() - getGunHeadingRadians());
    }
  }

  double calcularPoderTret(double dObjectiu) {
    if (vidaObjectiu != 0.0) {
      if (dObjectiu < getHeight() * 1.5) {
        return Rules.MAX_BULLET_POWER;
      } else if (getEnergy() > 4 * Rules.MAX_BULLET_POWER + 2 * (Rules.MAX_BULLET_POWER - 1) 
      || (getOthers() == 1 && getEnergy() > 3 * Rules.MAX_BULLET_POWER)) {
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

  boolean aniraAImpactar(double marge) {
    return getX() + getHeight() * marge >= getBattleFieldWidth() 
    || getX() - getHeight() * marge <= 0.0 
    || getY() + getHeight() * marge >= getBattleFieldHeight() 
    || getY() - getHeight() * marge <= 0.0;
  }
}