// Unified, clean, scalable version of the E-commerce system using best practices
// Combines the best of both previous codes: OOP structure, LocalDate expiration, interface-driven shipping

import java.time.LocalDate;
import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void decreaseQuantity(int q) { this.quantity -= q; }

    public abstract boolean isExpired();
    public boolean isShippable() { return this instanceof Shippable; }
}

class ExpirableProduct extends Product implements Shippable {
    private LocalDate expiryDate;
    private double weight;

    public ExpirableProduct(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
        this.weight = weight;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public double getWeight() {
        return weight;
    }
}

class NonExpirableProduct extends Product implements Shippable {
    private double weight;

    public NonExpirableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    public boolean isExpired() {
        return false;
    }

    public double getWeight() {
        return weight;
    }
}

class DigitalProduct extends Product {
    public DigitalProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    public boolean isExpired() {
        return false;
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void deduct(double amount) { balance -= amount; }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (product.isExpired()) throw new IllegalArgumentException(product.getName() + " is expired");
        if (quantity > product.getQuantity()) throw new IllegalArgumentException("Not enough stock for " + product.getName());
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}

class ShippingService {
    public static void ship(List<Shippable> items) {
        if (items.isEmpty()) return;
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        for (Shippable item : items) {
            System.out.printf("%s\t%.0fg\n", item.getName(), item.getWeight() * 1000);
            totalWeight += item.getWeight();
        }
        System.out.printf("Total package weight %.1fkg\n\n", totalWeight);
    }
}

class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) throw new RuntimeException("Cart is empty");

        double subtotal = cart.getSubtotal();
        double shipping = 0;
        List<Shippable> toShip = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.product;
            product.decreaseQuantity(item.quantity);

            if (product instanceof Shippable s) {
                for (int i = 0; i < item.quantity; i++) {
                    toShip.add(s);
                    shipping += 10.0; // per item shipping fee
                }
            }
        }

        double total = subtotal + shipping;
        if (customer.getBalance() < total) throw new RuntimeException("Insufficient balance");
        customer.deduct(total);

        if (!toShip.isEmpty()) ShippingService.ship(toShip);

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s\t%.0f\n", item.quantity, item.product.getName(), item.getTotalPrice());
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal\t%.0f\n", subtotal);
        System.out.printf("Shipping\t%.0f\n", shipping);
        System.out.printf("Amount\t\t%.0f\n", total);
        System.out.printf("Balance left\t%.0f\n", customer.getBalance());
        cart.clear();
    }
}

public class Main {
    public static void main(String[] args) {
        Product cheese = new ExpirableProduct("Cheese", 100, 5, LocalDate.now().plusDays(5), 0.2);
        Product biscuits = new ExpirableProduct("Biscuits", 150, 2, LocalDate.now().plusDays(2), 0.7);
        Product tv = new NonExpirableProduct("TV", 1000, 3, 10);
        Product scratchCard = new DigitalProduct("ScratchCard", 50, 10);

        Customer customer = new Customer("Ahmed", 1000);
        Cart cart = new Cart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        CheckoutService.checkout(customer, cart);
    }
}
