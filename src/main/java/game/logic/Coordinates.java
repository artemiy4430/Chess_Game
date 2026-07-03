package game.logic;

public class Coordinates {

    private int coordinateX;
    private int coordinateY;
    private boolean isAttackCoordinate;

    public Coordinates(int coordinateX, int coordinateY) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        isAttackCoordinate = false;
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
}
