package game.logic;

import java.util.Objects;

public class Coordinates {

    private int coordinateX;
    private int coordinateY;
    private boolean isAttackCoordinate;
    private boolean isEnpassant;

    public Coordinates(int coordinateX, int coordinateY) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        isAttackCoordinate = false;
        isEnpassant = false;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public boolean isAttackCoordinate() {
        return isAttackCoordinate;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }

    public void setAttackCoordinate(boolean attackCoordinate) {
        isAttackCoordinate = attackCoordinate;
    }

    public boolean isEnpassant() {
        return isEnpassant;
    }

    public void setEnpassant(boolean enpassant) {
        isEnpassant = enpassant;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return coordinateX == that.coordinateX && coordinateY == that.coordinateY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinateX, coordinateY);
    }

    @Override
    public String toString() {
        return  "coordinateX: " + coordinateX +
                "\ncoordinateY " + coordinateY;
    }
}
