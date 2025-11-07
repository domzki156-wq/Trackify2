package oop.barcelo.trackify27.models;

public class PaymentMethod {
    private String id;
    private String methodName;

    public PaymentMethod() {}

    public PaymentMethod(String id, String methodName) {
        this.id = id;
        this.methodName = methodName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
}
