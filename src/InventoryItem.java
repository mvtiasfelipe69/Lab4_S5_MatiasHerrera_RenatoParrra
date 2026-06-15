public class InventoryItem {
    private int id;
    private String name;
    private String category;
    private String location;
    private int stockTotal;
    private int stockAvailable;
    private int stockOnLoan;

    public InventoryItem(int id, String name, String category, String location, int stockTotal, int stockAvailable, int stockOnLoan) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.stockTotal = stockTotal;
        this.stockAvailable = stockAvailable;
        this.stockOnLoan = stockOnLoan;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public int getStockTotal() { return stockTotal; }
    public int getStockAvailable() { return stockAvailable; }
    public int getStockOnLoan() { return stockOnLoan; }

    public void addStock(int quantity) {
        if (quantity <= 0) return;
        stockTotal += quantity;
        stockAvailable += quantity;
    }

    public boolean lend(int quantity) {
        if (quantity <= 0 || quantity > stockAvailable) return false;
        stockAvailable -= quantity;
        stockOnLoan += quantity;
        return true;
    }

    public boolean receive(int quantity) {
        if (quantity <= 0 || quantity > stockOnLoan) return false;
        stockAvailable += quantity;
        stockOnLoan -= quantity;
        return true;
    }

    @Override
    public String toString() {
        return "InventoryItem{id=" + id + ", name='" + name + "', category='" + category + "', location='" + location + "', stockTotal=" + stockTotal + ", stockAvailable=" + stockAvailable + ", stockOnLoan=" + stockOnLoan + "}";
    }
}