import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.text.DecimalFormat;

// Class representing a single cache block
class CacheBlock {
    boolean valid; // Valid bit indicating if block contains valid data
    int memoryBlockNumber; // Stores the memory block number currently mapped to this cache block

    public CacheBlock() {
        this.valid = false;
        this.memoryBlockNumber = -1; // -1 indicates an empty block
    }
}

public class DirectMappedCache extends JFrame {
    private static final int CACHE_BLOCKS = 4; // Number of cache blocks
    private static final double CACHE_HIT_TIME_NS = 1.0; // Time taken for a cache hit (nanoseconds)
    private static final double MEMORY_ACCESS_TIME_NS = 100.0; // Time taken for a memory access (nanoseconds)

    private final CacheBlock[] cache; // Array representing the cache
    private int memoryAccessCount = 0;
    private int cacheHitCount = 0;
    private int cacheMissCount = 0;
    private JTextArea logArea;
    private JLabel[] cacheLabels;
    private int memoryBlocks = 1024; // Default number of memory blocks
    private final Queue<Integer> memoryQueue = new LinkedList<>(); // Queue for memory access sequence
    private javax.swing.Timer animationTimer;

    public DirectMappedCache() {
        // Initialize the cache array with a fixed number of cache blocks
        cache = new CacheBlock[CACHE_BLOCKS];

        // Set up the graphical user interface (GUI) components
        setupGUI();

        // Reset the cache to clear any previous data and set initial values
        resetCache();

        // Prompt the user to enter the number of memory blocks (default is 1024)
        String input = JOptionPane.showInputDialog(this, "Enter number of memory blocks (minimum 1024):");

        try {
            // Attempt to parse the user input into an integer
            int blocks = Integer.parseInt(input);

            // Validate the user input: ensure it is at least 1024
            if (blocks >= 1024) {
                memoryBlocks = blocks; // Set memoryBlocks to user-defined value
            } else {
                // Show a warning and use the default value if input is invalid
                JOptionPane.showMessageDialog(this, "Invalid input! Using default (1024 blocks).", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            // Handle invalid (non-numeric) input by displaying a warning and using the default value
            JOptionPane.showMessageDialog(this, "Invalid input! Using default (1024 blocks).", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        // Set up the animation timer for GUI updates
        setupAnimationTimer();
    }

    private void setupGUI() {
        setTitle("Direct Mapped Cache Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = getjPanel();
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel cachePanel = new JPanel();
        cachePanel.setLayout(new GridLayout(CACHE_BLOCKS, 1));
        cacheLabels = new JLabel[CACHE_BLOCKS];
        for (int i = 0; i < CACHE_BLOCKS; i++) {
            cacheLabels[i] = new JLabel("Cache Block " + i + " | Main Memory Block: None");
            cacheLabels[i].setOpaque(true);
            cacheLabels[i].setBackground(Color.LIGHT_GRAY);
            cachePanel.add(cacheLabels[i]);
        }

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(cachePanel, BorderLayout.EAST);
    }

    private JPanel getjPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        JButton sequentialButton = new JButton("Sequential Test");
        JButton randomButton = new JButton("Random Test");
        JButton midRepeatButton = new JButton("Mid-Repeat Test");
        JButton statsButton = new JButton("Show Stats");

        sequentialButton.addActionListener(e -> prepareTest(1));
        randomButton.addActionListener(e -> prepareTest(2));
        midRepeatButton.addActionListener(e -> prepareTest(3));
        statsButton.addActionListener(e -> showStats());

        panel.add(sequentialButton);
        panel.add(randomButton);
        panel.add(midRepeatButton);
        panel.add(statsButton);
        return panel;
    }

    private void resetCache() {
        for (int i = 0; i < CACHE_BLOCKS; i++) {
            cache[i] = new CacheBlock();
            cacheLabels[i].setText("Cache Block " + i + " | Main Memory Block: None");
            cacheLabels[i].setBackground(Color.LIGHT_GRAY);
        }
        memoryAccessCount = 0;
        cacheHitCount = 0;
        cacheMissCount = 0;
        logArea.setText("");
    }

    private void setupAnimationTimer() {
        animationTimer = new javax.swing.Timer(200, e -> {
            if (!memoryQueue.isEmpty()) {
                int address = memoryQueue.poll();
                processMemoryAccess(address);
            } else {
                animationTimer.stop();
            }
        });
    }

    private void processMemoryAccess(int blockNumber) {
        // Increment the total memory access count
        memoryAccessCount++;

        // Compute the cache index using modulo operation
        int index = blockNumber % CACHE_BLOCKS;

        // Check if the requested memory block is already in cache (cache hit)
        if (cache[index].valid && cache[index].memoryBlockNumber == blockNumber) {
            cacheHitCount++; // Increment cache hit count
            logArea.append("Hit: Memory Block " + blockNumber + " found in Cache Block " + index + "\n");
        } else { // Cache miss
            cacheMissCount++; // Increment cache miss count

            // Load the requested memory block into the cache
            cache[index].valid = true; // Mark the cache block as valid
            cache[index].memoryBlockNumber = blockNumber; // Store the memory block number in cache

            logArea.append("Miss: Memory Block " + blockNumber + " loaded into Cache Block " + index + "\n");
        }

        // Update the cache display to reflect the changes
        updateCacheDisplay(index, cache[index].valid, cache[index].memoryBlockNumber);
    }

    private void updateCacheDisplay(int index, boolean valid, int memoryBlockNumber) {
        cacheLabels[index].setText("Cache Block " + index + " | Main Memory Block: " + memoryBlockNumber);
        cacheLabels[index].setBackground(valid ? Color.GREEN : Color.RED);
    }

    private void prepareTest(int testType) {
        // Reset the cache to its initial empty state
        resetCache();

        // Clear the memory queue before starting the new test
        memoryQueue.clear();

        // Random number generator for the random test case
        Random rand = new Random();

        // Define the maximum number of memory blocks to be accessed in sequential and mid-repeat tests
        int maxBlocks = (2 * CACHE_BLOCKS);

        // Determine the type of memory access pattern based on testType
        switch (testType) {
            case 1: // Sequential Access Test
                // Access memory blocks in sequential order, repeating the sequence 4 times
                for (int repeat = 0; repeat < 4; repeat++) {
                    for (int i = 0; i < maxBlocks; i++) {
                        memoryQueue.add(i); // Add each memory block in order
                    }
                }
                break;

            case 2: // Random Access Test
                // Access a random selection of memory blocks
                int totalBlocks = (4 * CACHE_BLOCKS); // Define total number of accesses
                for (int i = 0; i < totalBlocks; i++) {
                    memoryQueue.add(rand.nextInt(memoryBlocks)); // Add a randomly selected memory block
                }
                break;

            case 3: // Mid-Repeat Access Test
                // Simulates a mix of repeated and unique memory accesses
                for (int repeat = 0; repeat < 4; repeat++) {
                    // First phase: Access blocks sequentially within the cache size
                    for (int i = 0; i < CACHE_BLOCKS; i++) {
                        memoryQueue.add(i);
                    }

                    // Second phase: Repeat access to mid-range blocks twice
                    for (int midRepeat = 0; midRepeat < 2; midRepeat++) {
                        for (int i = 1; i < CACHE_BLOCKS; i++) {
                            memoryQueue.add(i);
                        }
                    }

                    // Third phase: Access remaining blocks beyond cache size
                    for (int i = CACHE_BLOCKS; i < maxBlocks; i++) {
                        memoryQueue.add(i);
                    }
                }
                break;
        }

        // Start the animation timer to process memory accesses
        animationTimer.start();
    }

    private void showStats() {
        // Create a decimal format to display numbers with two decimal places
        DecimalFormat df = new DecimalFormat("0.00");

        // Calculate the cache hit rate as a percentage
        double hitRate = ((double) cacheHitCount / Math.max(memoryAccessCount, 1)) * 100;

        // Calculate the cache miss rate as a percentage
        double missRate = ((double) cacheMissCount / Math.max(memoryAccessCount, 1)) * 100;

        // Compute the average memory access time (considering both hits and misses)
        double avgMemoryAccessTime = ((cacheHitCount * CACHE_HIT_TIME_NS + cacheMissCount * MEMORY_ACCESS_TIME_NS)
                / Math.max(memoryAccessCount, 1));

        // Compute the total memory access time for all accesses
        double totalMemoryAccessTime = cacheHitCount * CACHE_HIT_TIME_NS + cacheMissCount * MEMORY_ACCESS_TIME_NS;

        // Display cache performance statistics in a message dialog
        JOptionPane.showMessageDialog(this,
                "Number of Cache Blocks (N): " + CACHE_BLOCKS +    // Show total cache blocks
                        "\nTotal Memory Accesses: " + memoryAccessCount +  // Show total memory accesses
                        "\nCache Hits: " + cacheHitCount +                 // Show total cache hits
                        "\nCache Misses: " + cacheMissCount +              // Show total cache misses
                        "\nCache Hit Rate: " + df.format(hitRate) + "%" +  // Show hit rate as percentage
                        "\nCache Miss Rate: " + df.format(missRate) + "%" +// Show miss rate as percentage
                        "\nAverage Memory Access Time: " + df.format(avgMemoryAccessTime) + " ns" +  // Show average access time
                        "\nTotal Memory Access Time: " + df.format(totalMemoryAccessTime) + " ns"); // Show total access time
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DirectMappedCache().setVisible(true));
    }
}
