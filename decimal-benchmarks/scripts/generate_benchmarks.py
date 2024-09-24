def create_benchmark_d64(method: str, a_n_digits: int, b_n_digits: int):
    return f"""
@Benchmark
public long decimal64_{method}_{a_n_digits}_{b_n_digits}(JmhState s) {{
    @Decimal long a = s.decimalSamples[{a_n_digits}][s.idx++];
    @Decimal long b = s.decimalSamples[{b_n_digits}][s.idx++];
    
    if (s.idx == (s.N - 2)) s.idx = 0;
    
    return Decimal64.add(a, b);
}}
"""


def create_benchmark_bd(method: str, a_n_digits: int, b_n_digits: int):
    return f"""
@Benchmark
public BigDecimal bigDecimal_{method}_{a_n_digits}_{b_n_digits}(JmhState s) {{
    BigDecimal a = s.bigDecimalSamples[{a_n_digits}][s.idx++];
    BigDecimal b = s.bigDecimalSamples[{b_n_digits}][s.idx++];
    
    if (s.idx == (s.N - 2)) s.idx = 0;

    return a.add(b, MathContext.DECIMAL64);
}}
"""


benchmarks = []

for i in range(17):
    for j in range(i, 17):
        benchmarks += [
            create_benchmark_d64('add', i, j),
            create_benchmark_bd('add', i, j)
        ]


print(''.join(benchmarks))


