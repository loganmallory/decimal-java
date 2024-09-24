# decimal-java

An object-free, highly efficient decimal number library with zero allocations* for comparisons and math operations.

Store your numbers in a `long` without worrying about accuracy!

\* _in progress_

---

## Features

- **Precision:**
You get up to **16** consecutive base 10 digits 

- **Range:**
Supports numbers between _± 9_999_999_999_999_999 * 10<sup>255</sup>_ and _± 1 * 10<sup>-255</sup>.

- **Representation:**
Numbers are represented as _mantissa * 10<sup>-exponent</sup>_, where:
  - Mantissa is a signed 55-bit integer.
  - Exponent is a signed 9-bit integer.
  - Both are stored compactly in a single 64-bit `long`.

- **Type safety**
The `decimal-annotations` package can be used to provide compile-time type checking

- **Non-finite Values:**
Supports special values like `NaN` and `+/- Infinity` by default.

- **Performance:**
Optimized for speed with **zero allocations*** for both comparisons and math operations.

\* In progress. Comparisons are allocation free, but `add` and `sub` are the only zero-alloc math ops currently.

---

## Correctness
Rigorously tested against the `BigDecimal` library with `MathContext.DECIMAL_64`

## Benchmarks
See the `decimal-benchmarks` module for source code.

Honestly, BigDecimal is pretty "fast". But it has a high std deviation in latencies 
and the GC pressure is awful. By comparison, Decimal64 has no GC pressure
and basically constant latencies.

| Method   | `a` n digits | `b` n digits | Decimal64       | BigDecimal        |
|----------|--------------|--------------|-----------------|-------------------|
| add(a,b) | 0            | 0            | 9.4 ns  ± 12 ns | 12.9 ns  ± 5 ns   |
| add(a,b) | 10           | 10           | 12.8 ns ± 3 ns  | 34.9 ns  ± 4 ns   |
| add(a,b) | 15           | 15           | 13.5 ns ± 6 ns  | 144.6 ns ± 444 ns |


TODO: more benchmarks

---

## Getting Started

To include this library in your project, add the following to your build system:

### Maven
```xml
<dependency>
  <groupId>io.github.loganmallory</groupId>
  <artifactId>decimal-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle
```

```

---

## Type safety
Using the checker-framework package, we can provide compile-time type checking on `long` values
annotated with `@Decimal`:
```java
public static void main(String[] args) {
    @Decimal long x = Decimal64.fromString("3.14");
    // bad! you can't add a plain integer to a decimal
    long y = x + 123; 
}
```

This will result in a compile time error of:
```
java: [binary] binary operation between incompatible fenums: @Decimal long and @FenumUnqualified int
```

```java
public static void main(String[] args) {
    @Decimal long x = Decimal64.fromString("3.14");
    // also bad! this will just print the raw bits as a long
    System.out.println("y = " + x); 
}
```

This will result in a compile time error of:
```
java: [argument] incompatible argument for parameter arg0 of PrintStream.println.
  found   : @FenumTop String
  required: @FenumUnqualified String
```

### Using compile-time type checks
Enabling type checks is a little bit of work, but definitely worth it.

#### Maven
To use the compile-time type safety checks, you'll need to modify your maven pom.xml 
to include the following:

```xml
<project>
  <dependencies>
    <dependency>
      <groupId>org.checkerframework</groupId>
      <artifactId>checker-qual</artifactId>
      <version>3.45.0</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.checkerframework</groupId>
      <artifactId>checker</artifactId>
      <version>3.45.0</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.github.loganmallory</groupId>
      <artifactId>decimal-annotations</artifactId>
      <version>1.0.0</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
  
  <build>
    <plugins>
      <!-- compiler -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
    
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
    
          <!-- must fork for <compilerArgs> -->
          <fork>true</fork>
    
          <annotationProcessorPaths>
            <!-- load checker so @Decimal works -->
            <path>
              <groupId>org.checkerframework</groupId>
              <artifactId>checker</artifactId>
              <version>3.45.0</version>
            </path>
            <!-- place @Decimal on the -processorpath -->
            <dependency>
              <groupId>io.github.loganmallory</groupId>
              <artifactId>decimal-annotations</artifactId>
              <version>1.0.0</version>
            </dependency>
          </annotationProcessorPaths>
          <compilerArgs>
            <!-- open for org.checkerframework -->
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
            <!-- apply the @Decimal type checking-->
            <arg>-processor</arg>
            <arg>org.checkerframework.checker.fenum.FenumChecker</arg>
            <arg>-Aquals=io.github.loganmallory.decimaljava.annotations.Decimal</arg>
          </compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```