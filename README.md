# 🚀 Create Your First Java Program

Java has evolved to become more beginner-friendly. This guide walks you through creating a simple program that prints “Hello World,” using both the classic syntax and the new streamlined approach introduced in Java 21.

---

## ✨ Classic Java Approach

Traditionally, Java requires a class with a `main` method as the entry point:

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

This works across all Java versions and forms the foundation of most Java programs.

---

## 🆕 Java 25: Unnamed Class with Instance Main Method

In newer versions like **Java 25**, you can use **Unnamed Classes** and an **Instance Main Method**, which allows for a much cleaner syntax:

```java
void main() {
    System.out.println("Hello World");
}
```

### 💡 Why is this cool?

- ✅ No need for a `public class` declaration  
- ✅ No `static` keyword required  
- ✅ Great for quick scripts and learning  

To compile and run this, use:

```bash
java --source 25 HelloWorld.java
```

---

## 📚 Learn More

This feature is part of Java’s ongoing effort to streamline syntax. You can explore deeper in [Baeldung’s guide to Unnamed Classes and Instance Main Methods](https://www.baeldung.com/java-21-unnamed-class-instance-main).
