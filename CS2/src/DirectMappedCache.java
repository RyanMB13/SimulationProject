import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.util.*;
import java.util.Queue;
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
    private JPanel[] cacheBlockPanels;
    private JLabel[] cacheStatusLabels;
    private JLabel[] cacheMemoryLabels;
    private JLabel statusLabel;
    private Timer animationTimer;
    private JProgressBar simulationProgress;
    private int memoryBlocks = 1024; // Default number of memory blocks
    private final Queue<Integer> memoryQueue = new LinkedList<>(); // Queue for memory access sequence
    private JPanel chartPanel;
    private final Color CACHE_HIT_COLOR = new Color(76, 175, 80);
    private final Color CACHE_MISS_COLOR = new Color(244, 67, 54);
    private final Color EMPTY_CACHE_COLOR = new Color(224, 224, 224);

    public DirectMappedCache() {
        // Initialize the cache array with a fixed number of cache blocks
        cache = new CacheBlock[CACHE_BLOCKS];

        // Set up the graphical user interface (GUI) components
        setupGUI();

        // Reset the cache to clear any previous data and set initial values
        resetCache();

        // Prompt the user to enter the number of memory blocks
        promptForMemoryBlocks();

        // Set up the animation timer for GUI updates
        setupAnimationTimer();
    }

    private void promptForMemoryBlocks() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(10, 10));
        
        JLabel instructionLabel = new JLabel("Enter number of memory blocks (minimum 1024):");
        JTextField inputField = new JTextField("1024", 10);
        
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(instructionLabel);
        fieldPanel.add(inputField);
        
        inputPanel.add(fieldPanel, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                "Cache Configuration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int blocks = Integer.parseInt(inputField.getText().trim());
                if (blocks >= 1024) {
                    memoryBlocks = blocks;
                    statusLabel.setText("Memory configured with " + memoryBlocks + " blocks");
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Value must be at least 1024. Using default (1024 blocks).", 
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("Memory configured with default 1024 blocks");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                        "Please enter a valid number. Using default (1024 blocks).", 
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                statusLabel.setText("Memory configured with default 1024 blocks");
            }
        }
    }

    private void setupGUI() {
        setTitle("Direct Mapped Cache Simulation");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Direct Mapped Cache Simulator", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel controlPanel = createControlPanel();
        headerPanel.add(controlPanel, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Ready. Please select a test pattern.");
        statusLabel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 10, 5, 10)
        ));
        
        simulationProgress = new JProgressBar(0, 100);
        simulationProgress.setStringPainted(true);
        simulationProgress.setBorder(new EmptyBorder(5, 10, 5, 10));
        simulationProgress.setVisible(false);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Access Log"));
        
        JPanel cacheVisualizationPanel = createCacheVisualizationPanel();
        
        chartPanel = new JPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Cache Performance"));
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(300, 200));
        updateStatsChart(0, 0); 
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.add(cacheVisualizationPanel, BorderLayout.CENTER);
        rightPanel.add(chartPanel, BorderLayout.SOUTH);
        
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPane,
                rightPanel
        );
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(600);
        
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(simulationProgress, BorderLayout.NORTH);
        statusPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton sequentialButton = createStyledButton("Sequential Test", "Run test with sequential memory accesses");
        JButton randomButton = createStyledButton("Random Test", "Run test with random memory accesses");
        JButton midRepeatButton = createStyledButton("Mid-Repeat Test", "Run test with repeated mid-range accesses");
        JButton statsButton = createStyledButton("Show Statistics", "Display detailed cache performance statistics");
        
        sequentialButton.addActionListener(e -> prepareTest(1));
        randomButton.addActionListener(e -> prepareTest(2));
        midRepeatButton.addActionListener(e -> prepareTest(3));
        statsButton.addActionListener(e -> showStats());
        
        controlPanel.add(sequentialButton);
        controlPanel.add(randomButton);
        controlPanel.add(midRepeatButton);
        controlPanel.add(statsButton);
        
        return controlPanel;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(25, 103, 210));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(66, 133, 244));
            }
        });
        
        return button;
    }

    private JPanel createCacheVisualizationPanel() {
        JPanel cachePanel = new JPanel();
        cachePanel.setLayout(new GridLayout(CACHE_BLOCKS, 1, 0, 10));
        cachePanel.setBorder(BorderFactory.createTitledBorder("Cache Memory"));
        
        cacheBlockPanels = new JPanel[CACHE_BLOCKS];
        cacheStatusLabels = new JLabel[CACHE_BLOCKS];
        cacheMemoryLabels = new JLabel[CACHE_BLOCKS];
        
        for (int i = 0; i < CACHE_BLOCKS; i++) {
            cacheBlockPanels[i] = new JPanel(new BorderLayout());
            cacheBlockPanels[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            cacheBlockPanels[i].setBackground(EMPTY_CACHE_COLOR);
            
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(33, 150, 243));
            JLabel headerLabel = new JLabel("Cache Block " + i, JLabel.CENTER);
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            headerLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            headerPanel.add(headerLabel, BorderLayout.CENTER);
            
            JPanel contentPanel = new JPanel(new GridLayout(2, 1));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            cacheStatusLabels[i] = new JLabel("Status: Empty", JLabel.CENTER);
            cacheStatusLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            cacheMemoryLabels[i] = new JLabel("Memory Block: None", JLabel.CENTER);
            cacheMemoryLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            contentPanel.add(cacheStatusLabels[i]);
            contentPanel.add(cacheMemoryLabels[i]);
            
            cacheBlockPanels[i].add(headerPanel, BorderLayout.NORTH);
            cacheBlockPanels[i].add(contentPanel, BorderLayout.CENTER);
            
            cachePanel.add(cacheBlockPanels[i]);
        }
        
        return cachePanel;
    }

    private void resetCache() {
        for (int i = 0; i < CACHE_BLOCKS; i++) {
            cache[i] = new CacheBlock();
            cacheBlockPanels[i].setBackground(EMPTY_CACHE_COLOR);
            cacheStatusLabels[i].setText("Status: Empty");
            cacheMemoryLabels[i].setText("Memory Block: None");
        }
        
        memoryAccessCount = 0;
        cacheHitCount = 0;
        cacheMissCount = 0;
        logArea.setText("");
        statusLabel.setText("Cache reset. Ready for new test.");
        
        updateStatsChart(0, 0);
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(200, e -> {
            if (!memoryQueue.isEmpty()) {
                int address = memoryQueue.poll();
                processMemoryAccess(address);
                
                int progress = 100 - (int)((double)memoryQueue.size() / (double)memoryAccessCount * 100);
                simulationProgress.setValue(progress);
                simulationProgress.setString("Processing: " + progress + "%");
                
                updateStatsChart(cacheHitCount, cacheMissCount);
                
            } else {
                animationTimer.stop();
                simulationProgress.setVisible(false);
                statusLabel.setText("Test completed. " + memoryAccessCount + " accesses processed.");
                
                updateStatsChart(cacheHitCount, cacheMissCount);
            }
        });
    }

    private void processMemoryAccess(int blockNumber) {
        memoryAccessCount++;

        int index = blockNumber % CACHE_BLOCKS;

        if (cache[index].valid && cache[index].memoryBlockNumber == blockNumber) {
            cacheHitCount++; 
            logArea.append("[Hit] Memory Block " + blockNumber + " found in Cache Block " + index + "\n");
            
            flashCacheBlock(index, CACHE_HIT_COLOR);
            
        } else { 
            cacheMissCount++; 

            cache[index].valid = true; 
            cache[index].memoryBlockNumber = blockNumber; 

            logArea.append("[Miss] Memory Block " + blockNumber + " loaded into Cache Block " + index + "\n");
            
            
            flashCacheBlock(index, CACHE_MISS_COLOR);
        }

        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        updateCacheDisplay(index, cache[index].valid, cache[index].memoryBlockNumber);
    }

    private void flashCacheBlock(int index, Color highlightColor) {
        
        Color originalColor = cache[index].valid ? 
                new Color(240, 249, 255) : EMPTY_CACHE_COLOR;
        
        
        Timer flashTimer = new Timer(100, null);
        final int[] flashCount = {0};
        
        flashTimer.addActionListener(e -> {
            if (flashCount[0] % 2 == 0) {
                cacheBlockPanels[index].setBackground(highlightColor);
            } else {
                cacheBlockPanels[index].setBackground(originalColor);
            }
            
            flashCount[0]++;
            if (flashCount[0] >= 4) {
                flashTimer.stop();
                cacheBlockPanels[index].setBackground(originalColor);
            }
        });
        
        flashTimer.start();
    }

    private void updateCacheDisplay(int index, boolean valid, int memoryBlockNumber) {

        cacheStatusLabels[index].setText("Status: " + (valid ? "Valid" : "Empty"));
        cacheMemoryLabels[index].setText("Memory Block: " + (valid ? String.valueOf(memoryBlockNumber) : "None"));
        
        if (valid) {
            cacheBlockPanels[index].setBackground(new Color(240, 249, 255)); 
        } else {
            cacheBlockPanels[index].setBackground(EMPTY_CACHE_COLOR); 
        }
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
        String testName = "";

        // Determine the type of memory access pattern based on testType
        switch (testType) {
            case 1: // Sequential Access Test
                testName = "Sequential Test";
                // Access memory blocks in sequential order, repeating the sequence 4 times
                for (int repeat = 0; repeat < 4; repeat++) {
                    for (int i = 0; i < maxBlocks; i++) {
                        memoryQueue.add(i); // Add each memory block in order
                    }
                }
                break;

            case 2: // Random Access Test
                testName = "Random Test";
                // Access a random selection of memory blocks
                int totalBlocks = (4 * CACHE_BLOCKS); // Define total number of accesses
                for (int i = 0; i < totalBlocks; i++) {
                    memoryQueue.add(rand.nextInt(memoryBlocks)); // Add a randomly selected memory block
                }
                break;

            case 3: // Mid-Repeat Access Test
                testName = "Mid-Repeat Test";
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

        memoryAccessCount = memoryQueue.size();
        
        cacheHitCount = 0;
        cacheMissCount = 0;
        
      
        simulationProgress.setValue(0);
        simulationProgress.setString("Starting simulation...");
        simulationProgress.setVisible(true);
        
        statusLabel.setText("Running " + testName + " with " + memoryAccessCount + " memory accesses...");
        
        logArea.setText("=== " + testName + " ===\n");
        logArea.append("Cache Size: " + CACHE_BLOCKS + " blocks\n");
        logArea.append("Memory Size: " + memoryBlocks + " blocks\n");
        logArea.append("Total Memory Accesses: " + memoryAccessCount + "\n");
        logArea.append("---------------------------\n\n");

        animationTimer.start();
    }

    private void updateStatsChart(int hits, int misses) {
        chartPanel.removeAll();
        
        if (hits + misses == 0) {
            JLabel emptyLabel = new JLabel("No data available yet", JLabel.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            chartPanel.add(emptyLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            return;
        }
        
        JPanel barChart = new JPanel(new GridLayout(1, 2, 10, 0));
        barChart.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel hitPanel = createStatBar("Hits", hits, hits + misses, CACHE_HIT_COLOR);
        
        JPanel missPanel = createStatBar("Misses", misses, hits + misses, CACHE_MISS_COLOR);
        
        barChart.add(hitPanel);
        barChart.add(missPanel);
        
        DecimalFormat df = new DecimalFormat("0.00");
        double hitRate = ((double) hits / Math.max(hits + misses, 1)) * 100;
        double missRate = 100 - hitRate;
        
        JLabel summaryLabel = new JLabel(
            "<html><b>Summary:</b> " + 
            hits + " hits (" + df.format(hitRate) + "%), " +
            misses + " misses (" + df.format(missRate) + "%)" +
            "</html>", 
            JLabel.CENTER
        );
        summaryLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        chartPanel.add(barChart, BorderLayout.CENTER);
        chartPanel.add(summaryLabel, BorderLayout.SOUTH);
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private JPanel createStatBar(String label, int value, int total, Color color) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        
        JPanel barContainer = new JPanel(new BorderLayout());
        barContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JPanel bar = new JPanel();
        bar.setBackground(color);
        
        double percentage = (double) value / Math.max(total, 1);
        int height = (int) (100 * percentage);
        
        barContainer.setLayout(new BorderLayout());
        barContainer.add(bar, BorderLayout.SOUTH);
        bar.setPreferredSize(new Dimension(30, height));
        
        JLabel nameLabel = new JLabel(label, JLabel.CENTER);
        
        JLabel countLabel = new JLabel(String.valueOf(value), JLabel.CENTER);
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(barContainer, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);
        
        return panel;
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

        // Create custom styled dialog for statistics
        JDialog statsDialog = new JDialog(this, "Cache Performance Statistics", true);
        statsDialog.setSize(500, 400);
        statsDialog.setLocationRelativeTo(this);
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Cache Performance Statistics");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(20));
        
        addStatRow(statsPanel, "Number of Cache Blocks", String.valueOf(CACHE_BLOCKS));
        addStatRow(statsPanel, "Total Memory Blocks", String.valueOf(memoryBlocks));
        addStatRow(statsPanel, "Total Memory Accesses", String.valueOf(memoryAccessCount));
        statsPanel.add(Box.createVerticalStrut(10));
        
        addStatRow(statsPanel, "Cache Hits", String.valueOf(cacheHitCount));
        addStatRow(statsPanel, "Cache Misses", String.valueOf(cacheMissCount));
        statsPanel.add(Box.createVerticalStrut(10));
        
        addStatRow(statsPanel, "Cache Hit Rate", df.format(hitRate) + "%");
        addStatRow(statsPanel, "Cache Miss Rate", df.format(missRate) + "%");
        statsPanel.add(Box.createVerticalStrut(10));
        
        addStatRow(statsPanel, "Average Memory Access Time", df.format(avgMemoryAccessTime) + " ns");
        addStatRow(statsPanel, "Total Memory Access Time", df.format(totalMemoryAccessTime) + " ns");
        
        JPanel chartPanel = createPieChart(cacheHitCount, cacheMissCount);
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(chartPanel);
        
       
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> statsDialog.dispose());
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(closeButton);
        
       
        statsDialog.add(new JScrollPane(statsPanel));
        statsDialog.setVisible(true);
    }

    private void addStatRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 14));
        labelComponent.setPreferredSize(new Dimension(220, 25));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        rowPanel.add(labelComponent);
        rowPanel.add(Box.createHorizontalStrut(10));
        rowPanel.add(valueComponent);
        rowPanel.add(Box.createHorizontalGlue());
        
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    private JPanel createPieChart(int hits, int misses) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int total = hits + misses;
                if (total == 0) return;
                
                int width = getWidth();
                int height = getHeight();
                int diameter = Math.min(width, height) - 40;
                int x = (width - diameter) / 2;
                int y = (height - diameter) / 2;
                
                // Draw hit portion (green)
                double hitAngle = 360.0 * hits / total;
                g2d.setColor(CACHE_HIT_COLOR);
                g2d.fillArc(x, y, diameter, diameter, 0, (int) hitAngle);
                
                // Draw miss portion (red)
                g2d.setColor(CACHE_MISS_COLOR);
                g2d.fillArc(x, y, diameter, diameter, (int) hitAngle, 360 - (int) hitAngle);
                
                // Draw outline
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawOval(x, y, diameter, diameter);
                
                // Draw legend
                g2d.setColor(CACHE_HIT_COLOR);
                g2d.fillRect(width - 120, y, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Hits: " + hits, width - 100, y + 12);
                
                g2d.setColor(CACHE_MISS_COLOR);
                g2d.fillRect(width - 120, y + 25, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Misses: " + misses, width - 100, y + 37);
            }
        };
        
        chartPanel.setPreferredSize(new Dimension(300, 200));
        return chartPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DirectMappedCache().setVisible(true);
        });
    }
}
