package oop.barcelo.trackify27.models;

public class TransactionDetail {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double priceAtSale;
    private final double subtotal;
    public TransactionDetail(String productId, String productName, int quantity, double priceAtSale) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
        this.subtotal = quantity * priceAtSale;
    }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPriceAtSale() { return priceAtSale; }
    public double getSubtotal() { return subtotal; }
}
