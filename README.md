# SimulationProject
CSARCH2 Simulation Project Grouo 5 (Direct Mapping)

**Cache Simulation System Specifications**
The direct-mapped cache simulator is designed as a Java-based stand-alone application with a graphical user interface (GUI). It implements a direct-mapped caching strategy where each main memory block maps to a unique cache block. The cache consists of 32 blocks, with each cache line storing 16 words. The system follows a non-load-through read policy, meaning data is fetched from memory only on a cache miss. The number of main memory blocks is user-defined, with a minimum of 1024 blocks.
The simulator provides three test cases—sequential access, random access, and mid-repeat access—to analyze cache performance under different memory access patterns. System outputs include a step-by-step animation toggle, a final cache memory snapshot, and a text log of cache memory trace. Performance metrics such as memory access count, cache hit count, cache miss count, cache hit rate, cache miss rate, average memory access time, and total memory access time are displayed after execution.

**Test Case Analysis**
The sequential access test examines cache performance when memory blocks are accessed in ascending order. This test accesses 2N (64) memory blocks and repeats the sequence four times. Initially, cache misses occur as new memory blocks are loaded, but as the sequence repeats, the cache hit rate improves due to temporal locality. This pattern demonstrates how direct-mapped caching efficiently handles sequential data access, leading to higher hit rates in later iterations.

The random access test evaluates how the cache handles unpredictable workloads by accessing 4N (128) memory blocks in a random sequence. Due to the lack of locality in memory references, this test results in a high miss rate as randomly selected blocks frequently replace existing ones. The absence of repeated accesses significantly impacts cache performance, showing that direct-mapped caching is less effective for workloads without predictable memory access patterns.

The mid-repeat access test introduces a mix of sequential and repetitive accesses to analyze the impact of cache reuse. The sequence starts at block 0, progresses up to N-1 (31), repeats the middle section twice, then continues up to 2N (64), repeating the full sequence four times. The test demonstrates that cache hit rates improve in the repeated middle segment, where frequently accessed blocks remain in cache, but performance declines when accessing blocks beyond the cache capacity. This case highlights the trade-off between cache reuse and eviction, making it useful for analyzing workloads with both locality and new data introduction.

**Conclusion**
The direct-mapped cache simulation provides insights into cache behavior under different access patterns. Sequential access benefits from locality, random access suffers from high misses, and mid-repeat access demonstrates cache reuse limitations. By analyzing hit rates, miss rates, and access times, this simulation illustrates the efficiency and constraints of direct-mapped caching in modern computing environments.

