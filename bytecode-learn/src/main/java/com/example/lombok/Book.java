package com.example.lombok;

@Data
public class Book {

    private String id;
    private int price;

    public static void main(String[] args) {
        Book book = new Book();
        // book.setId("aBook");
        // book.setPrice(12);
        System.out.println(book);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", price=" + price +
                '}';
    }
}
