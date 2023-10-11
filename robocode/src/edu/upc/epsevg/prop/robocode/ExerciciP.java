package edu.upc.epsevg.prop.robocode;

import robocode.*;
import static robocode.util.Utils.*;

public class ExerciciP extends AdvancedRobot {
  
  double d = 150.0, dir = 1;
  boolean saindoDaParede = false;
  
  String alvo = "nenhum";
  double vidaAlvo, distAlvo;
  
  boolean escolhendoAlvo = false;
  String tiroAnterior = "";
  String outros[];
  double disOutros[];
  
  public void run() {
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    prepararParaEscolherAlvo();
    while (true) {
      setAhead((distAlvo / 4 + 25) * dir);
      if (!saindoDaParede && vaiBater(3)) {
        dir *= -1;
        saindoDaParede = true;
      } else if (!vaiBater(3.2)) {
        saindoDaParede = false;
      }
      execute();
    }
  }
  
  public void onScannedRobot(ScannedRobotEvent e) {
    if (getOthers() == 1) {
      alvo = e.getName();
    } else {
      if (escolhendoAlvo) {
        for (int i = 0; i < outros.length; i++) {
          if (outros[i].equals(e.getName())) return;
          if (outros[i].equals("")) {
            outros[i] = e.getName();
            disOutros[i] = e.getDistance();
            if (i == outros.length-1) {
              escolherAlvo();
            }
            i = outros.length;
          }
        }
        return;
      } else {
        if (!e.getName().equals(alvo) && e.getEnergy() < vidaAlvo && e.getDistance() <= distAlvo) {
          alvo = e.getName();
        }
      }
    }
    if (!e.getName().equals(alvo)) return;
    if (!saindoDaParede && vidaAlvo - e.getEnergy() >= Rules.MIN_BULLET_POWER && vidaAlvo - e.getEnergy() <= Rules.MAX_BULLET_POWER && !(vidaAlvo - e.getEnergy() > 0.57 && vidaAlvo - e.getEnergy() < 0.63)) {
      dir *= -1;
    }
    if (!vaiBater(0.6)) {
      setTurnRight(e.getBearing() + 90 - (e.getDistance() > getHeight()*3 ? 40 : 10) * dir);
    }
    double absBearing = e.getBearing() + getHeading();
    setTurnRadarRight(ajustarRadar(absBearing));
    setTurnGunRightRadians(ajustarArma(e)*0.85);
    setFire(calcularPoderTiro(e.getDistance()));
    vidaAlvo = e.getEnergy();
    distAlvo = e.getDistance();
  }
  
  public void onHitByBullet(HitByBulletEvent e) {
    if (getOthers() > 1) {   
      if ((distAlvo > d || e.getName().equals(tiroAnterior)) && vidaAlvo > 18) {
        alvo = e.getName();
        double radarBearing = normalRelativeAngleDegrees((e.getBearing() + getHeading()) - getRadarHeading());
        if (radarBearing > 0) {
          setTurnRadarRight(Double.POSITIVE_INFINITY);
        } else {
          setTurnRadarRight(Double.NEGATIVE_INFINITY);
        }
      }
      if (!e.getName().equals(alvo)) tiroAnterior = e.getName();
    }
  }
  
  public void onHitRobot(HitRobotEvent e) {
    dir *= -1;    
    if (!escolhendoAlvo && !e.getName().equals(alvo) && e.getEnergy() < vidaAlvo) {
      alvo = e.getName();
      double radarBearing = normalRelativeAngleDegrees((e.getBearing() + getHeading()) - getRadarHeading());
      if (radarBearing > 0) {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      } else {
        setTurnRadarRight(Double.NEGATIVE_INFINITY);
      }
    }
  }
  
  public void onHitWall(HitWallEvent e) {
    saindoDaParede = true;
    if (dir == -1 && Math.abs(e.getBearing()) >= 160.0) {
      dir = 1;
    } else if (dir == 1 && Math.abs(e.getBearing()) <= 20.0) {
      dir = -1;
    } else {
      if (dir == 1) {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
        dir = -1;
      } else {
        setTurnRight(normalRelativeAngleDegrees(e.getBearing()+180));
        dir = 1;
      }
    }
  }
  
  public void onRobotDeath(RobotDeathEvent e) {
    if (e.getName().equals(alvo)) {
      if (getOthers() > 1) {
        prepararParaEscolherAlvo();
      } else {
        setTurnRadarRight(Double.POSITIVE_INFINITY);
      }
    } else if (escolhendoAlvo) {
      prepararParaEscolherAlvo();
    }
  }
  
  void prepararParaEscolherAlvo() {
    outros = new String[getOthers()];
    disOutros = new double[getOthers()];
    for (int i = 0; i < outros.length; i++) {
      outros[i] = "";
    }
    escolhendoAlvo = true;
    setTurnRadarRight(Double.POSITIVE_INFINITY);
  }

  void escolherAlvo() {
    escolhendoAlvo = false;
    int roboEscolhido = -1;
    double menorDis = Double.MAX_VALUE;
    for (int i = 0; i < outros.length; i++) {
      if (disOutros[i] < menorDis) {
        roboEscolhido = i;
        menorDis = disOutros[i];
      }
    }
    alvo = outros[roboEscolhido];
  }
  
  double ajustarRadar(double absBearing) {
    if (getOthers() > 1) {
      double correcaoRadar;
      correcaoRadar = normalRelativeAngleDegrees((absBearing - getRadarHeading()));
      correcaoRadar += 22.5*Math.signum(correcaoRadar);
      if (correcaoRadar > 45.0) {
        correcaoRadar = 45.0;
      } else if (correcaoRadar < -45.0) {
        correcaoRadar = -45.0;
      } else if (correcaoRadar > 0.0 && correcaoRadar < 20.0) {
        correcaoRadar = 20.0;
      } else if (correcaoRadar > -20.0 && correcaoRadar <= 0.0) {
        correcaoRadar = -20.0;
      }
      return correcaoRadar;
    } else {
      return normalRelativeAngleDegrees((absBearing - getRadarHeading())*2);
    }
  }
  
  double ajustarArma(ScannedRobotEvent e) {
    if (e.getEnergy() != 0.0) {
      double absBearingRad = getHeadingRadians() + e.getBearingRadians();
      double compensacaoLinear = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearingRad) / Rules.getBulletSpeed(calcularPoderTiro(e.getDistance()));
      if (e.getDistance() <= 12*10) {
        compensacaoLinear *= 0.5;
      } else if (e.getDistance() <= 12*5) {
        compensacaoLinear *= 0.3;
      }
      return normalRelativeAngle(absBearingRad - getGunHeadingRadians() + compensacaoLinear);
    } else {
      return normalRelativeAngle(getHeadingRadians()+ e.getBearingRadians()- getGunHeadingRadians());
    }
  }
  
  double calcularPoderTiro(double dAlvo) {
    if (vidaAlvo != 0.0) {
      if (dAlvo < getHeight()*1.5) {
        return Rules.MAX_BULLET_POWER;
      } else if (getEnergy() > 4*Rules.MAX_BULLET_POWER + 2*(Rules.MAX_BULLET_POWER - 1) || (getOthers() == 1 && getEnergy() > 3*Rules.MAX_BULLET_POWER)) {
        if (dAlvo <= d*2) {
          return Rules.MAX_BULLET_POWER;
        } else {
          return Math.min(1.1 + (d*2) / dAlvo, Rules.MAX_BULLET_POWER);
        }
      } else if (getEnergy() > 2.2) {
        return 1.1;
      } else {
        return Math.max(0.1, getEnergy()/3);
      }
    } else {
      return 0.1;
    }
  }
  
  boolean vaiBater(double margem) {
    return getX() + getHeight()*margem >= getBattleFieldWidth() || getX() - getHeight()*margem <= 0.0 || getY() + getHeight()*margem >= getBattleFieldHeight() || getY() - getHeight()*margem <= 0.0;
  }
}

