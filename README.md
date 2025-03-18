CSARCH2 Simulation Project Group 5
=======
# Direct Mapped Cache Simulator

This is a Java application that simulates a direct-mapped cache, allowing you to visualize and understand how memory accesses are handled in a cache system. The simulator provides a graphical user interface (GUI) to demonstrate cache hits, misses, and overall cache performance.

## Table of Contents
- [System Requirements](#system-requirements)
- [Running the Program](#running-the-program)
- [Using the Simulator](#using-the-simulator)
  - [Initial Configuration](#initial-configuration)
  - [Running Tests](#running-tests)
  - [Understanding the Interface](#understanding-the-interface)
  - [Viewing Statistics](#viewing-statistics)
- [Cache Simulation System Specifications](#cache-simulation-system-specifications)
- [Test Case Analysis](#test-case-analysis)
- [Understanding the Cache Simulation](#understanding-the-cache-simulation)
- [Conclusion](#conclusion)

## System Requirements
- Java Development Kit (JDK) 8 or later
- Java Runtime Environment (JRE)
- Visual Studio Code (or any Java IDE)

## Running the Program

### Using VS Code:

1. **Open the project** in VS Code

2. **Build the project**:
   - Open the Command Palette (Ctrl+Shift+P or Cmd+Shift+P on Mac)
   - Type "Java: Build Workspace" and press Enter

3. **Run the program**:
   - Right-click on `DirectMappedCache.java` in the Explorer panel
   - Select "Run Java" or use the play button that appears in the top-right corner of the editor

   Alternatively, you can use the terminal:
   ```
   javac DirectMappedCache.java
   java DirectMappedCache
   ```

## Using the Simulator

### Initial Configuration

1. **When the program starts**, a dialog will appear asking for the number of memory blocks:
   - Enter a value of at least 1024 (the default)
   - Click "OK" to continue

2. **Main Window**: After configuration, the main simulator window will appear with the following components:
   - Control buttons at the top
   - Access log on the left
   - Cache visualization on the right
   - Statistics chart at the bottom right
   - Status bar at the bottom

### Running Tests

The simulator offers three different test patterns to demonstrate cache behavior:

1. **Sequential Test**:
   - Click the "Sequential Test" button
   - This test accesses memory blocks in sequential order
   - Good for demonstrating how direct-mapped caches handle sequential access patterns

2. **Random Test**:
   - Click the "Random Test" button
   - This test accesses memory blocks randomly
   - Demonstrates how the cache handles unpredictable memory access patterns

3. **Mid-Repeat Test**:
   - Click the "Mid-Repeat Test" button
   - This test uses a mix of sequential and repeated access patterns
   - Good for demonstrating cache hits when accessing the same data multiple times

### Understanding the Interface

During test execution, you'll see:

1. **Access Log** (left panel):
   - Shows each memory access and whether it resulted in a hit or miss
   - Indicates which memory block was accessed and which cache block was used

2. **Cache Visualization** (right panel):
   - Displays the state of each cache block
   - Shows which memory block is currently mapped to each cache block
   - Highlights in green for cache hits and red for cache misses

3. **Statistics Chart** (bottom right):
   - Shows the current hit and miss rates as they occur
   - Updates in real-time as the test progresses

4. **Status Bar** (bottom):
   - Shows the current state of the simulation
   - Displays a progress bar during test execution

### Viewing Statistics

After running a test, you can view detailed statistics:

1. **Click the "Show Statistics" button**

2. **Statistics Dialog**:
   - Shows detailed information about the cache performance
   - Includes metrics such as:
     - Number of cache blocks
     - Total memory blocks
     - Total memory accesses
     - Cache hits and misses
     - Hit and miss rates
     - Average memory access time
     - Total memory access time
   - Includes a pie chart showing the proportion of hits and misses

## Cache Simulation System Specifications

The direct-mapped cache simulator is designed as a Java-based stand-alone application with a graphical user interface (GUI). It implements a direct-mapped caching strategy where each main memory block maps to a unique cache block. The cache consists of 32 blocks, with each cache line storing 16 words. The system follows a non-load-through read policy, meaning data is fetched from memory only on a cache miss. The number of main memory blocks is user-defined, with a minimum of 1024 blocks.

The simulator provides three test cases—sequential access, random access, and mid-repeat access—to analyze cache performance under different memory access patterns. System outputs include a step-by-step animation toggle, a final cache memory snapshot, and a text log of cache memory trace. Performance metrics such as memory access count, cache hit count, cache miss count, cache hit rate, cache miss rate, average memory access time, and total memory access time are displayed after execution.

## Test Case Analysis

### Sequential Access Test
The sequential access test examines cache performance when memory blocks are accessed in ascending order. This test accesses 2N (64) memory blocks and repeats the sequence four times. Initially, cache misses occur as new memory blocks are loaded, but as the sequence repeats, the cache hit rate improves due to temporal locality. This pattern demonstrates how direct-mapped caching efficiently handles sequential data access, leading to higher hit rates in later iterations.

### Random Access Test
The random access test evaluates how the cache handles unpredictable workloads by accessing 4N (128) memory blocks in a random sequence. Due to the lack of locality in memory references, this test results in a high miss rate as randomly selected blocks frequently replace existing ones. The absence of repeated accesses significantly impacts cache performance, showing that direct-mapped caching is less effective for workloads without predictable memory access patterns.

### Mid-Repeat Access Test
The mid-repeat access test introduces a mix of sequential and repetitive accesses to analyze the impact of cache reuse. The sequence starts at block 0, progresses up to N-1 (31), repeats the middle section twice, then continues up to 2N (64), repeating the full sequence four times. The test demonstrates that cache hit rates improve in the repeated middle segment, where frequently accessed blocks remain in cache, but performance declines when accessing blocks beyond the cache capacity. This case highlights the trade-off between cache reuse and eviction, making it useful for analyzing workloads with both locality and new data introduction.

## Understanding the Cache Simulation

The simulator implements a direct-mapped cache with the following characteristics:

- **Cache Size**: 4 blocks by default (can be configured as 32 blocks as per specifications)
- **Memory Size**: User-configurable (minimum 1024 blocks)
- **Mapping**: Direct-mapped (memory block address mod cache size)
- **Replacement Policy**: Automatic replacement when a new block maps to the same cache location
- **Access Times**: 
  - Cache Hit: 1 nanosecond (simulated)
  - Cache Miss: 100 nanoseconds (simulated)

Each memory access results in one of two outcomes:

1. **Cache Hit**: The requested memory block is already in the cache
   - Visually represented by a green flash
   - Faster access time

2. **Cache Miss**: The requested memory block is not in the cache
   - Visually represented by a red flash
   - The block is loaded from memory
   - Slower access time

## Conclusion

The direct-mapped cache simulation provides insights into cache behavior under different access patterns. Sequential access benefits from locality, random access suffers from high misses, and mid-repeat access demonstrates cache reuse limitations. By analyzing hit rates, miss rates, and access times, this simulation illustrates the efficiency and constraints of direct-mapped caching in modern computing environments.
>>>>>>> Stashed changes

## walkthrough
https://drive.google.com/drive/folders/1pgrg4XeChwLtZQyniQE_Eo9k-HqiDW5I?usp=sharing

