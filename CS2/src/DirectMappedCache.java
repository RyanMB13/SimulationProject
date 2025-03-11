import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.text.DecimalFormat;

class CacheBlock {
    boolean valid;
    int memoryBlockNumber;

    public CacheBlock() {
        this.valid = false;
        this.memoryBlockNumber = -1; // Indicates an empty block
    }
}

public class DirectMappedCache extends JFrame {
    private static final int CACHE_BLOCKS = 4;
    private static final double CACHE_HIT_TIME_NS = 1.0;
    private static final double MEMORY_ACCESS_TIME_NS = 100.0;

    private final CacheBlock[] cache;
    private int memoryAccessCount = 0;
    private int cacheHitCount = 0;
    private int cacheMissCount = 0;
    private JTextArea logArea;
    private JLabel[] cacheLabels;
    private int memoryBlocks = 1024;
    private final Queue<Integer> memoryQueue = new LinkedList<>();
    private javax.swing.Timer animationTimer;

    public DirectMappedCache() {
        cache = new CacheBlock[CACHE_BLOCKS];
        setupGUI();
        resetCache();

        String input = JOptionPane.showInputDialog(this, "Enter number of memory blocks (minimum 1024):");
        try {
            int blocks = Integer.parseInt(input);
            if (blocks >= 1024) {
                memoryBlocks = blocks;
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input! Using default (1024 blocks).", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input! Using default (1024 blocks).", "Warning", JOptionPane.WARNING_MESSAGE);
        }
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
        memoryAccessCount++;
        int index = blockNumber % CACHE_BLOCKS;

        if (cache[index].valid && cache[index].memoryBlockNumber == blockNumber) {
            cacheHitCount++;
            logArea.append("Hit: Memory Block " + blockNumber + " found in Cache Block " + index + "\n");
        } else {
            cacheMissCount++;
            cache[index].valid = true;
            cache[index].memoryBlockNumber = blockNumber;
            logArea.append("Miss: Memory Block " + blockNumber + " loaded into Cache Block " + index + "\n");
        }
        updateCacheDisplay(index, cache[index].valid, cache[index].memoryBlockNumber);
    }

    private void updateCacheDisplay(int index, boolean valid, int memoryBlockNumber) {
        cacheLabels[index].setText("Cache Block " + index + " | Main Memory Block: " + memoryBlockNumber);
        cacheLabels[index].setBackground(valid ? Color.GREEN : Color.RED);
    }

    private void prepareTest(int testType) {
        resetCache();
        memoryQueue.clear();
        Random rand = new Random();
        int maxBlocks = (2 * CACHE_BLOCKS);

        switch (testType) {
            case 1:
                for (int repeat = 0; repeat < 4; repeat++) {
                    for (int i = 0; i < maxBlocks; i++) {
                        memoryQueue.add(i);
                    }
                }
                break;
            case 2:
                int totalBlocks = (4 * CACHE_BLOCKS);
                for (int i = 0; i < totalBlocks; i++) {
                    memoryQueue.add(rand.nextInt(memoryBlocks));
                }
                break;
            case 3:
                for (int repeat = 0; repeat < 4; repeat++) {
                    for (int i = 0; i < CACHE_BLOCKS; i++) {
                        memoryQueue.add(i);
                    }
                    for (int midRepeat = 0; midRepeat < 2; midRepeat++) {
                        for (int i = 1; i < CACHE_BLOCKS; i++) {
                            memoryQueue.add(i);
                        }
                    }
                    for (int i = CACHE_BLOCKS; i < maxBlocks; i++) {
                        memoryQueue.add(i);
                    }
                }
                break;
        }
        animationTimer.start();
    }

    private void showStats() {
        DecimalFormat df = new DecimalFormat("0.00");
        double hitRate = ((double) cacheHitCount / Math.max(memoryAccessCount, 1)) * 100;
        double missRate = ((double) cacheMissCount / Math.max(memoryAccessCount, 1)) * 100;
        double avgMemoryAccessTime = ((cacheHitCount * CACHE_HIT_TIME_NS + cacheMissCount * MEMORY_ACCESS_TIME_NS) / Math.max(memoryAccessCount, 1));
        double totalMemoryAccessTime = cacheHitCount * CACHE_HIT_TIME_NS + cacheMissCount * MEMORY_ACCESS_TIME_NS;

        JOptionPane.showMessageDialog(this,
                "Number of Cache Blocks (N): " + CACHE_BLOCKS +
                        "\nTotal Memory Accesses: " + memoryAccessCount +
                        "\nCache Hits: " + cacheHitCount +
                        "\nCache Misses: " + cacheMissCount +
                        "\nCache Hit Rate: " + df.format(hitRate) + "%" +
                        "\nCache Miss Rate: " + df.format(missRate) + "%" +
                        "\nAverage Memory Access Time: " + df.format(avgMemoryAccessTime) + " ns" +
                        "\nTotal Memory Access Time: " + df.format(totalMemoryAccessTime) + " ns");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DirectMappedCache().setVisible(true));
    }
}
