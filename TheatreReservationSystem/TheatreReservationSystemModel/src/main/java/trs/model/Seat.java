package trs.model;

public class Seat implements IEntity<Long> {
    private long seatId;
    private int lodge;
    private int row;
    private int number;
    private float price;

    public Seat(long seatId, int lodge, int row, int number, float price) {
        this.seatId = seatId;
        this.lodge = lodge;
        this.row = row;
        this.number = number;
        this.price = price;
    }

    public Seat(int lodge, int row, int number, float price) {
        this.lodge = lodge;
        this.row = row;
        this.number = number;
        this.price = price;
    }

    public Seat() {

    }

    @Override
    public Long getId() {
        return seatId;
    }

    @Override
    public void setId(Long id) {
        this.seatId = id;
    }

    public int getLodge() {
        return lodge;
    }

    public void setLodge(int lodge) {
        this.lodge = lodge;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
