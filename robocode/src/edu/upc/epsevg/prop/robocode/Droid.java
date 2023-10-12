package edu.upc.epsevg.prop.robocode;

import robocode.*;
import static robocode.util.Utils.*;

public class Droid extends AdvancedRobot{
    double d = 150.0, dir = 1;
    boolean tocaParet = false;
    String enemic = "drpau";
    double distanciaEnemic, vidaEnemic;
    boolean escollintEnemic = false;
    String tirAnterior = "";
    String outros[];
    double disOutros[];
    
    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        escollintEnemicFuncio();
        while(true){
            setAhead(distanciaEnemic / 4 + 25 * dir);
            if(!tocaParet && orbita(3)){
                dir *= -1;
                tocaParet = true;
            } else if(!orbita(3.2)){
                tocaParet = false;
            }
            execute();
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent e){
        if(getOthers() == 1) {
            enemic = e.getName();
        } else {
            if(escollintEnemic){
                for (int i = 0; i < outros.length; i++) {
                    if (outros[i].equals(e.getName())) return;
                    if (outros[i].equals("")) {
                      outros[i] = e.getName();
                      disOutros[i] = e.getDistance();
                      if (i == outros.length-1) {
                          a_quien_pillo();
                      }
                      i = outros.length;
                    }
                  }
                 return;
            } else {
                if (!e.getName().equals(enemic) && e.getEnergy() < vidaEnemic 
                        && e.getDistance() <= distanciaEnemic) {
                    enemic = e.getName();
                  }
            }
        }
        if(!e.getName().equals(enemic)) return;
        if (!tocaParet && vidaEnemic - e.getEnergy() >= Rules.MIN_BULLET_POWER 
                && vidaEnemic - e.getEnergy() <= Rules.MAX_BULLET_POWER 
                && !(vidaEnemic - e.getEnergy() > 0.57 
                && vidaEnemic - e.getEnergy() < 0.63)) {
                dir *= -1;
        }
        if(!orbita(0.6)) {
            setTurnRight(e.getBearing() + 90 - (e.getDistance() > getHeight()*3 ? 40 : 10) * dir);
        }
        double absBearing = e.getBearing() + getHeading();
        setTurnRadarRight(ajustarRadar(absBearing));
        setTurnGunRightRadians(ajustarArma(e)*0.85);
        setFire(calcularPotencia(e.getDistance()));
        vidaEnemic = e.getEnergy();
        distanciaEnemic = e.getDistance();
    }
    
    public void onHitByBullet(HitByBulletEvent e){
        if(getOthers() > 1){
            if((distanciaEnemic > d 
                    || e.getName().equals(tirAnterior)) && vidaEnemic > 18){
                enemic = e.getName();
                double radarBearing = normalRelativeAngleDegrees((e.getBearing() + getHeading()) - getRadarHeading());
                if(radarBearing > 0){
                    setTurnRadarRight(Double.POSITIVE_INFINITY);
                } else {
                    setTurnRadarLeft(Double.NEGATIVE_INFINITY);
                }
            }
            if (!e.getName().equals(enemic)) tirAnterior = e.getName();
        }
    }
    
    public void onHitRobot(HitRobotEvent e){
        dir *= -1;
        if(!escollintEnemic 
                && !e.getName().equals(enemic) 
                && e.getEnergy() < vidaEnemic) {
            enemic = e.getName();
            double radarBearing = normalRelativeAngleDegrees((e.getBearing() + 
                        getHeading()) - getRadarHeading());
            if(radarBearing > 0){
                    setTurnRadarRight(Double.POSITIVE_INFINITY);
                } else {
                    setTurnRadarLeft(Double.NEGATIVE_INFINITY);
            }
        }
    }
    
    public void onHitWall(HitWallEvent e){
        tocaParet = true;
        if(dir == -1 && Math.abs(e.getBearing()) >= 160.0) {
            dir = 1;
        } else if (dir == 1 && Math.abs(e.getBearing()) <= 20.0){
            dir = -1;
        } else {
            if (dir == 1){
                setTurnRight(normalRelativeAngleDegrees(e.getBearing()));
                dir = -1;
            } else {
                setTurnRight(normalRelativeAngleDegrees(e.getBearing()+180));
                dir = 1;
            }
        }
    }
    
    public void onRobotDeath(RobotDeathEvent e){
        if(e.getName().equals(enemic)){
            if(getOthers() > 1){
                escollintEnemicFuncio();
            } else {
                setTurnRadarRight(Double.POSITIVE_INFINITY);
            }
        } else if(escollintEnemic){
            escollintEnemicFuncio();
        }
    }
    
    void escollintEnemicFuncio(){
        outros = new String[getOthers()];
        disOutros = new double[getOthers()];
        for(int i = 0; i < outros.length; i++){
            outros[i] = "";
        }
        escollintEnemic = true;
        setTurnRadarRight(Double.POSITIVE_INFINITY);
    }
    
    void a_quien_pillo() {
        escollintEnemic = false;
        int robotEscollit = -1;
        double menorDis = Double.MAX_VALUE;
        for(int i = 0; i < outros.length; i++){
            if(disOutros[i] < menorDis){
                robotEscollit = i;
                menorDis = disOutros[i];
            }
        }
        enemic = outros[robotEscollit];
    }
    
    //funcio que crec que no es necessaria
    double ajustarRadar(double absBearing){
        if(getOthers() > 1){
            double modRadar;
            modRadar = normalRelativeAngleDegrees((absBearing - getRadarHeading()));
            modRadar += 22.5*Math.signum(modRadar);
            if(modRadar > 45.0){
                modRadar = 45.0;
            } else if (modRadar < -45.0){
                modRadar = -45.0;
            } else if (modRadar > 0.0 && modRadar < 20.0){
                modRadar = 20.0;
            } else if (modRadar > -20.0 && modRadar <= 0.0){
                modRadar = -20.0;
            }
            return modRadar;
        } else {
            return normalRelativeAngleDegrees((absBearing - getRadarHeading())*2);
        }
    }
    
    double ajustarArma(ScannedRobotEvent e){
        if(e.getEnergy() != 0.0){
            double absBearingRad = getHeadingRadians() + e.getBearingRadians();
            double apuntar = e.getVelocity() * Math.sin(e.getHeadingRadians() 
                - absBearingRad) / Rules.getBulletSpeed(calcularPotencia(e.getDistance()));
            if(e.getDistance() <= 120){
                apuntar *= 0.5;
            } else if (e.getDistance() <= 60){
                apuntar *= 0.3;
            }
              return normalRelativeAngle(absBearingRad - getGunHeadingRadians() 
                        + apuntar);
        } else {
            return normalRelativeAngle(getHeadingRadians()+ e.getBearingRadians() - 
                        getGunHeadingRadians());
        }
    }
    
    //funcio que no es necessaria
    double calcularPotencia(double enemigo){
        if(vidaEnemic != 0.0){
            if(enemigo < getHeight()*1.5){
                return Rules.MAX_BULLET_POWER;
            } else if (getEnergy() > 4*Rules.MAX_BULLET_POWER 
                    + 2*(Rules.MAX_BULLET_POWER - 1) || (getOthers() == 1 
                    && getEnergy() > 3*Rules.MAX_BULLET_POWER)) {
                if(enemigo <= d*2){
                    return Rules.MAX_BULLET_POWER;
                } else {
                      return Math.min(1.1 + (d*2) / enemigo, Rules.MAX_BULLET_POWER);
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
    
    boolean orbita(double radi){
            return getX() + getHeight()*radi >= getBattleFieldWidth() 
                    || getX() - getHeight()*radi <= 0.0 
                    || getY() + getHeight()*radi >= getBattleFieldHeight() 
                    || getY() - getHeight()*radi <= 0.0;
    }
}
