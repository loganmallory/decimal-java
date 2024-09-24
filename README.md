# decimal-java

An object-free, highly efficient decimal number library with zero allocations* for comparisons and math operations.

\* _in progress_

---

## Features

- **Precision:**
You get up to **16** consecutive base 10 digits 

- **Range:**
Supports numbers between _± 9_999_999_999_999_999 * 10<sup>255</sup>_ and _± 1 * 10<sup>-255</sup>.

  - Expanded out, that is:
    _0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000**1**_ <= _x_ <= _**9999999999999999**000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_

- **Representation:**
Numbers are represented as _mantissa * 10<sup>-exponent</sup>_, where:
  - Mantissa is a signed 55-bit integer.
  - Exponent is a signed 9-bit integer.
  - Both are stored compactly in a single 64-bit `long`.

- **Non-finite Values:**
Supports special values like `NaN` and `+/- Infinity` by default.

- **Performance:**
Optimized for speed with **zero allocation** for both comparisons and math operations.
  - TODO: Decimal64.add(a, b) is the only zero-alloc math op currently.

---

## Correctness
Rigorously tested against the `BigDecimal` library with `MathContext.DECIMAL_64`

## Benchmarks

*TODO: Add benchmark comparisons against BigDecimal*

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