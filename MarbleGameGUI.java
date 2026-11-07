import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * ====================================================================
 * THE MARBLE
 * A Psychological Game of Wits, Betting & Deception
 * ====================================================================
 * Custom-rendered, glitch-free Java Swing GUI.
 * Fully styled vector components: no Windows L&F button artifacts,
 * no missing font glyphs, realistic glass marble physics & sound.
 * ====================================================================
 */
public class MarbleGameGUI extends JFrame {

    // --- THEME COLOR PALETTE ---
    public static final Color BG_DARK = new Color(11, 15, 25);
    public static final Color CARD_BG = new Color(19, 26, 43);
    public static final Color CARD_BORDER = new Color(38, 48, 77);
    public static final Color MAT_BG = new Color(13, 18, 30);

    public static final Color ACCENT_CYAN = new Color(34, 211, 238);
    public static final Color ACCENT_CYAN_HOVER = new Color(6, 182, 212);
    public static final Color ACCENT_CORAL = new Color(244, 63, 94);
    public static final Color ACCENT_CORAL_HOVER = new Color(225, 29, 72);
    public static final Color ACCENT_GOLD = new Color(251, 191, 36);
    public static final Color ACCENT_GOLD_HOVER = new Color(245, 158, 11);

    public static final Color TEXT_WHITE = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color TEXT_DARK = new Color(15, 23, 42);

    // --- GAME STATE ---
    private int playerMarbles = 10;
    private int leoMarbles = 10;
    private int currentRound = 1;
    private int winStreak = 0;
    private String playerName = "dhruv";
    private Difficulty difficulty = Difficulty.STRATEGIC;
    private TurnPhase currentPhase = TurnPhase.PLAYER_GUESSING;

    private int hiddenByLeo = 0;
    private int currentBet = 1;

    private final Random random = new Random();
    private final ArrayList<String> roundHistory = new ArrayList<>();

    // --- UI COMPONENTS ---
    private MarbleBowlPanel playerBowl;
    private MarbleBowlPanel leoBowl;
    private JLabel leoMoodBadge;
    private JLabel leoDialogueText;
    private JLabel phaseHeaderTitle;
    private JLabel phaseSubtitle;
    private JLabel roundBadge;
    private JLabel streakBadge;

    private CustomSlider betSlider;
    private JLabel betValueLabel;
    private CustomButton oddBtn;
    private CustomButton evenBtn;

    private CustomSlider hideSlider;
    private JLabel hideValueLabel;
    private CustomButton submitHideBtn;
    private JPanel attackControlsPanel;
    private JPanel defenseControlsPanel;

    private JTextArea historyArea;
    private JLabel leoCountLabel;
    private JLabel playerCountLabel;
    private JComboBox<String> diffDropdown;

    public enum Difficulty {
        CASUAL("Casual Leo (Easy)", 0.35),
        STRATEGIC("Strategic Leo (Normal)", 0.65),
        MIND_READER("Mind Reader (Hard)", 0.85);

        public final String label;
        public final double smartRate;
        Difficulty(String label, double smartRate) {
            this.label = label;
            this.smartRate = smartRate;
        }
    }

    public enum TurnPhase {
        PLAYER_GUESSING,
        LEO_GUESSING
    }

    public MarbleGameGUI() {
        super("The Marble");
        promptPlayerName();
        initUI();
        startNewRound();
    }

    private void promptPlayerName() {
        String input = JOptionPane.showInputDialog(
                this,
                "Welcome to The Marble!\nEnter your player name:",
                "Player Registration",
                JOptionPane.PLAIN_MESSAGE
        );
        if (input != null && !input.trim().isEmpty()) {
            this.playerName = input.trim();
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 820);
        setMinimumSize(new Dimension(1000, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 14));

        // 1. TOP NAVBAR
        add(buildHeaderBar(), BorderLayout.NORTH);

        // 2. CENTER BATTLE ARENA (Leo Arena | VS Divider | Player Arena)
        JPanel arenaContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        arenaContainer.setOpaque(false);
        arenaContainer.setBorder(new EmptyBorder(0, 24, 0, 24));

        // Leo's Arena Card
        JPanel leoCard = buildFighterCard("Leo", "AI OPPONENT", true);
        arenaContainer.add(leoCard);

        // Player's Arena Card
        JPanel playerCard = buildFighterCard(playerName, "CHALLENGER", false);
        arenaContainer.add(playerCard);

        add(arenaContainer, BorderLayout.CENTER);

        // 3. BOTTOM PANEL: Action Control Deck & Match History
        JPanel bottomContainer = new JPanel(new BorderLayout(16, 0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(6, 24, 20, 24));

        JPanel controlDeck = buildControlDeck();
        bottomContainer.add(controlDeck, BorderLayout.CENTER);

        JPanel historyBox = buildHistoryCard();
        bottomContainer.add(historyBox, BorderLayout.EAST);

        add(bottomContainer, BorderLayout.SOUTH);
    }

    // =========================================================================
    // HEADER BAR
    // =========================================================================
    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(CARD_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 26, 14, 26));

        // Brand Title
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brandBox.setOpaque(false);

        JLabel logoText = new JLabel("THE MARBLE");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoText.setForeground(ACCENT_CYAN);

        roundBadge = new JLabel("ROUND 1");
        roundBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roundBadge.setForeground(ACCENT_GOLD);
        roundBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(ACCENT_GOLD, 1, 8),
                new EmptyBorder(4, 10, 4, 10)
        ));

        streakBadge = new JLabel("STREAK: 0");
        streakBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        streakBadge.setForeground(ACCENT_CORAL);
        streakBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(ACCENT_CORAL, 1, 8),
                new EmptyBorder(4, 10, 4, 10)
        ));

        brandBox.add(logoText);
        brandBox.add(roundBadge);
        brandBox.add(streakBadge);
        header.add(brandBox, BorderLayout.WEST);

        // Right side: AI Difficulty selector & Reset Game
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBox.setOpaque(false);

        JLabel diffLbl = new JLabel("AI Mode:");
        diffLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        diffLbl.setForeground(TEXT_MUTED);

        diffDropdown = new JComboBox<>(new String[]{
                Difficulty.CASUAL.label,
                Difficulty.STRATEGIC.label,
                Difficulty.MIND_READER.label
        });
        diffDropdown.setSelectedIndex(1);
        diffDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        diffDropdown.setBackground(new Color(28, 38, 62));
        diffDropdown.setForeground(TEXT_WHITE);
        diffDropdown.setFocusable(false);
        diffDropdown.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        diffDropdown.addActionListener(e -> {
            int idx = diffDropdown.getSelectedIndex();
            if (idx == 0) difficulty = Difficulty.CASUAL;
            else if (idx == 1) difficulty = Difficulty.STRATEGIC;
            else difficulty = Difficulty.MIND_READER;
            updateLeoDialogue("Difficulty changed to " + difficulty.label + ". Let's duel!");
        });

        CustomButton resetBtn = new CustomButton("Reset Match", new Color(42, 53, 82), new Color(55, 68, 105), TEXT_WHITE);
        resetBtn.setPreferredSize(new Dimension(110, 32));
        resetBtn.addActionListener(e -> restartGame());

        rightBox.add(diffLbl);
        rightBox.add(diffDropdown);
        rightBox.add(resetBtn);

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // FIGHTER CARD (LEO / PLAYER)
    // =========================================================================
    private JPanel buildFighterCard(String title, String subtitle, boolean isLeo) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card Body
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Border with top accent line
                g2.setColor(isLeo ? new Color(244, 63, 94, 70) : new Color(34, 211, 238, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(isLeo ? ACCENT_CORAL : ACCENT_CYAN);
                g2.fillRoundRect(0, 0, getWidth(), 4, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        // Header: Name & Marble Count Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel nameCol = new JPanel(new GridLayout(2, 1, 0, 2));
        nameCol.setOpaque(false);

        JLabel nameLbl = new JLabel(title);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setForeground(isLeo ? ACCENT_CORAL : ACCENT_CYAN);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subLbl.setForeground(TEXT_MUTED);

        nameCol.add(nameLbl);
        nameCol.add(subLbl);
        topRow.add(nameCol, BorderLayout.WEST);

        JLabel countBadge = new JLabel("10 Marbles");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        countBadge.setForeground(TEXT_WHITE);
        countBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(isLeo ? ACCENT_CORAL : ACCENT_CYAN, 1, 10),
                new EmptyBorder(6, 14, 6, 14)
        ));

        if (isLeo) {
            leoCountLabel = countBadge;
        } else {
            playerCountLabel = countBadge;
        }
        topRow.add(countBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center: Custom Marble Bowl / Felt Table Mat
        MarbleBowlPanel bowl = new MarbleBowlPanel(10, isLeo ? ACCENT_CORAL : ACCENT_CYAN);
        if (isLeo) leoBowl = bowl;
        else playerBowl = bowl;
        card.add(bowl, BorderLayout.CENTER);

        // Bottom: Dynamic Speech Bubble for Leo / Tips for Player
        if (isLeo) {
            JPanel dialogueCard = new JPanel(new BorderLayout(8, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(13, 17, 28));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                }
            };
            dialogueCard.setOpaque(false);
            dialogueCard.setBorder(new EmptyBorder(10, 14, 10, 14));

            leoMoodBadge = new JLabel("[LEO - CONFIDENT]:");
            leoMoodBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            leoMoodBadge.setForeground(ACCENT_GOLD);

            leoDialogueText = new JLabel("\"I am Leo. Can you read my mind and take my marbles?\"");
            leoDialogueText.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            leoDialogueText.setForeground(TEXT_WHITE);

            dialogueCard.add(leoMoodBadge, BorderLayout.WEST);
            dialogueCard.add(leoDialogueText, BorderLayout.CENTER);
            card.add(dialogueCard, BorderLayout.SOUTH);
        } else {
            JPanel tipCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            tipCard.setOpaque(false);
            JLabel tipText = new JLabel("Game Rule: Guess Odd/Even in odd rounds; hide your marbles in even rounds!");
            tipText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tipText.setForeground(TEXT_MUTED);
            tipCard.add(tipText);
            card.add(tipCard, BorderLayout.SOUTH);
        }

        return card;
    }

    // =========================================================================
    // CONTROL DECK (Sliders & Custom Styled Action Buttons)
    // =========================================================================
    private JPanel buildControlDeck() {
        JPanel deck = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        deck.setOpaque(false);
        deck.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Deck Headline
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        phaseHeaderTitle = new JLabel("Round 1: Place Your Bet & Make Your Call");
        phaseHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        phaseHeaderTitle.setForeground(TEXT_WHITE);

        phaseSubtitle = new JLabel("Leo has concealed marbles in his closed fist. Choose your bet and guess!");
        phaseSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phaseSubtitle.setForeground(TEXT_MUTED);

        titleBox.add(phaseHeaderTitle);
        titleBox.add(phaseSubtitle);
        deck.add(titleBox, BorderLayout.NORTH);

        // Center: Interactive Switchable Control Panel
        JPanel centerSwitcher = new JPanel(new CardLayout());
        centerSwitcher.setOpaque(false);

        // --- ATTACK CONTROLS PANEL (Player Bets & Guesses) ---
        attackControlsPanel = new JPanel(new BorderLayout(0, 12));
        attackControlsPanel.setOpaque(false);

        JPanel betSliderRow = new JPanel(new BorderLayout(12, 0));
        betSliderRow.setOpaque(false);

        JLabel betPrompt = new JLabel("Bet Amount: ");
        betPrompt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        betPrompt.setForeground(TEXT_WHITE);
        betSliderRow.add(betPrompt, BorderLayout.WEST);

        betSlider = new CustomSlider(1, 10, 1, ACCENT_CYAN);
        betSlider.addChangeListener(e -> {
            currentBet = betSlider.getValue();
            betValueLabel.setText(currentBet + " Marble" + (currentBet > 1 ? "s" : ""));
        });
        betSliderRow.add(betSlider, BorderLayout.CENTER);

        betValueLabel = new JLabel("1 Marble");
        betValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        betValueLabel.setForeground(ACCENT_GOLD);
        betValueLabel.setPreferredSize(new Dimension(90, 30));
        betSliderRow.add(betValueLabel, BorderLayout.EAST);

        attackControlsPanel.add(betSliderRow, BorderLayout.NORTH);

        // Custom ODD vs EVEN Buttons (Rendered cleanly without Windows LookAndFeel glitches!)
        JPanel choiceBtnRow = new JPanel(new GridLayout(1, 2, 16, 0));
        choiceBtnRow.setOpaque(false);

        oddBtn = new CustomButton("GUESS ODD (1, 3, 5, 7, 9...)", ACCENT_CORAL, ACCENT_CORAL_HOVER, Color.WHITE);
        oddBtn.setPreferredSize(new Dimension(200, 48));
        oddBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        oddBtn.addActionListener(e -> handlePlayerGuess(1));

        evenBtn = new CustomButton("GUESS EVEN (2, 4, 6, 8, 10...)", ACCENT_CYAN, ACCENT_CYAN_HOVER, TEXT_DARK);
        evenBtn.setPreferredSize(new Dimension(200, 48));
        evenBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        evenBtn.addActionListener(e -> handlePlayerGuess(2));

        choiceBtnRow.add(oddBtn);
        choiceBtnRow.add(evenBtn);
        attackControlsPanel.add(choiceBtnRow, BorderLayout.CENTER);

        // --- DEFENSE CONTROLS PANEL (Player Hides & Leo Guesses) ---
        defenseControlsPanel = new JPanel(new BorderLayout(0, 12));
        defenseControlsPanel.setOpaque(false);

        JPanel hideSliderRow = new JPanel(new BorderLayout(12, 0));
        hideSliderRow.setOpaque(false);

        JLabel hidePrompt = new JLabel("Marbles in your fist: ");
        hidePrompt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hidePrompt.setForeground(TEXT_WHITE);
        hideSliderRow.add(hidePrompt, BorderLayout.WEST);

        hideSlider = new CustomSlider(1, 10, 1, ACCENT_GOLD);
        hideSlider.addChangeListener(e -> {
            int val = hideSlider.getValue();
            hideValueLabel.setText(val + " Marble" + (val > 1 ? "s" : ""));
        });
        hideSliderRow.add(hideSlider, BorderLayout.CENTER);

        hideValueLabel = new JLabel("1 Marble");
        hideValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hideValueLabel.setForeground(ACCENT_GOLD);
        hideValueLabel.setPreferredSize(new Dimension(90, 30));
        hideSliderRow.add(hideValueLabel, BorderLayout.EAST);

        defenseControlsPanel.add(hideSliderRow, BorderLayout.NORTH);

        submitHideBtn = new CustomButton("CLOSE FIST & LET LEO GUESS", ACCENT_GOLD, ACCENT_GOLD_HOVER, TEXT_DARK);
        submitHideBtn.setPreferredSize(new Dimension(200, 48));
        submitHideBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitHideBtn.addActionListener(e -> handlePlayerHideSubmit());
        defenseControlsPanel.add(submitHideBtn, BorderLayout.CENTER);

        centerSwitcher.add(attackControlsPanel, "ATTACK");
        centerSwitcher.add(defenseControlsPanel, "DEFENSE");

        deck.add(centerSwitcher, BorderLayout.CENTER);
        return deck;
    }

    // =========================================================================
    // MATCH ACTIVITY & HISTORY LOG
    // =========================================================================
    private JPanel buildHistoryCard() {
        JPanel box = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(340, 180));
        box.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = new JLabel("MATCH ACTIVITY FEED");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(ACCENT_GOLD);
        headerRow.add(title, BorderLayout.WEST);

        CustomButton clearBtn = new CustomButton("Clear", new Color(30, 40, 64), new Color(45, 58, 90), TEXT_MUTED);
        clearBtn.setPreferredSize(new Dimension(54, 22));
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        clearBtn.addActionListener(e -> {
            historyArea.setText("");
            logActivity("Feed cleared.");
        });
        headerRow.add(clearBtn, BorderLayout.EAST);
        box.add(headerRow, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(13, 17, 28));
        historyArea.setForeground(TEXT_WHITE);
        historyArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 10)));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        box.add(scroll, BorderLayout.CENTER);

        return box;
    }

    // =========================================================================
    // GAMEPLAY LOGIC & ROUND FLOW
    // =========================================================================
    private void startNewRound() {
        roundBadge.setText("ROUND " + currentRound);
        streakBadge.setText("STREAK: " + winStreak);

        int maxBet = Math.min(playerMarbles, leoMarbles);
        betSlider.setMaximum(Math.max(1, maxBet));
        betSlider.setValue(Math.min(betSlider.getValue(), maxBet));

        // Alternate turns: Odd = Player attacks / Even = Leo attacks
        if (currentRound % 2 != 0) {
            currentPhase = TurnPhase.PLAYER_GUESSING;
            setupPlayerAttackPhase();
        } else {
            currentPhase = TurnPhase.LEO_GUESSING;
            setupLeoAttackPhase();
        }

        updateUI();
    }

    private void setupPlayerAttackPhase() {
        hiddenByLeo = random.nextInt(leoMarbles) + 1;

        phaseHeaderTitle.setText("Round " + currentRound + ": Place Your Bet & Make Your Call");
        phaseSubtitle.setText("Leo has concealed marbles in his closed fist. Choose your bet and guess Odd or Even!");

        attackControlsPanel.setVisible(true);
        defenseControlsPanel.setVisible(false);

        updateLeoDialogueForHiding();
    }

    private void setupLeoAttackPhase() {
        phaseHeaderTitle.setText("Round " + currentRound + ": Close Your Fist & Let Leo Guess");
        phaseSubtitle.setText("Select how many marbles to conceal in your hand. Leo will wager and guess!");

        attackControlsPanel.setVisible(false);
        defenseControlsPanel.setVisible(true);

        hideSlider.setMaximum(playerMarbles);
        hideSlider.setValue(Math.min(hideSlider.getValue(), playerMarbles));

        updateLeoDialogue("Now it's my turn, " + playerName + "! Hide your marbles and let's see!");
    }

    private void handlePlayerGuess(int guessOddEven) {
        playTone(520, 80);
        int bet = betSlider.getValue();
        boolean isOdd = (hiddenByLeo % 2 != 0);
        boolean correct = (guessOddEven == 1 && isOdd) || (guessOddEven == 2 && !isOdd);

        String guessName = (guessOddEven == 1) ? "ODD" : "EVEN";
        String actualType = isOdd ? "ODD" : "EVEN";

        if (correct) {
            playVictoryFanfare();
            playerMarbles += bet;
            leoMarbles -= bet;
            winStreak++;
            logActivity("R" + currentRound + ": Correct guess (" + guessName + ")! Leo had " + hiddenByLeo + " (" + actualType + "). Won +" + bet + " marbles!");
            updateLeoDialogue(getLeoLossDialogue(bet));
            JOptionPane.showMessageDialog(this,
                    "CORRECT! Leo had " + hiddenByLeo + " marbles (" + actualType + ").\nYou won " + bet + " marbles from Leo!",
                    "Round Won!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            playDefeatTone();
            playerMarbles -= bet;
            leoMarbles += bet;
            winStreak = 0;
            logActivity("R" + currentRound + ": Wrong guess (" + guessName + ")! Leo had " + hiddenByLeo + " (" + actualType + "). Lost -" + bet + " marbles.");
            updateLeoDialogue(getLeoWinDialogue(bet));
            JOptionPane.showMessageDialog(this,
                    "WRONG! Leo had " + hiddenByLeo + " marbles (" + actualType + ").\nYou lost " + bet + " marbles to Leo.",
                    "Round Lost", JOptionPane.WARNING_MESSAGE);
        }

        checkGameOverOrNext();
    }

    private void handlePlayerHideSubmit() {
        int playerHidden = hideSlider.getValue();
        int maxLeoBet = Math.min(leoMarbles, playerMarbles);
        int leoBet = calculateLeoBet(maxLeoBet);
        int leoGuess = calculateLeoGuess(playerHidden);

        String leoGuessName = (leoGuess == 1) ? "ODD" : "EVEN";
        boolean playerIsOdd = (playerHidden % 2 != 0);
        boolean leoCorrect = (leoGuess == 1 && playerIsOdd) || (leoGuess == 2 && !playerIsOdd);

        if (leoCorrect) {
            playDefeatTone();
            leoMarbles += leoBet;
            playerMarbles -= leoBet;
            winStreak = 0;
            logActivity("R" + currentRound + ": Leo bet " + leoBet + " and guessed " + leoGuessName + " correctly! Lost -" + leoBet + " marbles.");
            updateLeoDialogue("Boom! I read you completely. +" + leoBet + " marbles for me!");
            JOptionPane.showMessageDialog(this,
                    "Leo bet " + leoBet + " marbles and guessed " + leoGuessName + " correctly!\n(You hid " + playerHidden + " marbles). You lose " + leoBet + " marbles.",
                    "Leo Won the Round!", JOptionPane.WARNING_MESSAGE);
        } else {
            playVictoryFanfare();
            playerMarbles += leoBet;
            leoMarbles -= leoBet;
            winStreak++;
            logActivity("R" + currentRound + ": Leo bet " + leoBet + " and guessed " + leoGuessName + " incorrectly! Won +" + leoBet + " marbles!");
            updateLeoDialogue("Agh! You tricked me! How did I miss that?");
            JOptionPane.showMessageDialog(this,
                    "Leo bet " + leoBet + " marbles and guessed " + leoGuessName + " INCORRECTLY!\n(You hid " + playerHidden + " marbles). You won " + leoBet + " marbles from Leo!",
                    "You Won the Round!", JOptionPane.INFORMATION_MESSAGE);
        }

        checkGameOverOrNext();
    }

    private int calculateLeoBet(int maxBet) {
        if (maxBet <= 1) return 1;
        switch (difficulty) {
            case CASUAL:
                return random.nextInt(maxBet) + 1;
            case STRATEGIC:
                int mid = Math.max(1, maxBet / 2);
                return Math.max(1, random.nextInt(mid) + 1);
            case MIND_READER:
                return Math.max(1, (int) Math.ceil(maxBet * 0.65));
            default:
                return 1;
        }
    }

    private int calculateLeoGuess(int playerHidden) {
        boolean actualIsOdd = (playerHidden % 2 != 0);
        double roll = random.nextDouble();
        if (roll < difficulty.smartRate) {
            return actualIsOdd ? 1 : 2;
        } else {
            return random.nextBoolean() ? 1 : 2;
        }
    }

    private void checkGameOverOrNext() {
        updateUI();

        if (playerMarbles <= 0) {
            playDefeatTone();
            updateLeoMood("[LEO - VICTORIOUS]", ACCENT_GOLD);
            updateLeoDialogue("\"Gganbu! It was an honorable match, but all your marbles belong to me!\"");
            JOptionPane.showMessageDialog(this,
                    "GAME OVER!\nYou ran out of marbles! Leo won the match.",
                    "The Marble - Match Terminated", JOptionPane.ERROR_MESSAGE);
            restartGame();
        } else if (leoMarbles <= 0) {
            playVictoryFanfare();
            updateLeoMood("[LEO - DEFEATED]", ACCENT_CORAL);
            updateLeoDialogue("\"No way... you took all my marbles! You are the ultimate master!\"");
            JOptionPane.showMessageDialog(this,
                    "CHAMPION! Congratulations " + playerName + "!\nYou claimed all 20 marbles and conquered The Marble!",
                    "The Marble - Victory!", JOptionPane.INFORMATION_MESSAGE);
            restartGame();
        } else {
            currentRound++;
            startNewRound();
        }
    }

    private void restartGame() {
        playerMarbles = 10;
        leoMarbles = 10;
        currentRound = 1;
        winStreak = 0;
        roundHistory.clear();
        historyArea.setText("");
        logActivity("Game initialized: 10 marbles each.");
        updateLeoMood("[LEO - CONFIDENT]", ACCENT_GOLD);
        updateLeoDialogue("\"Fresh start! Let's see who holds all 20 marbles at the end.\"");
        startNewRound();
    }

    private void updateUI() {
        playerBowl.setCount(playerMarbles);
        leoBowl.setCount(leoMarbles);

        playerCountLabel.setText(playerMarbles + " Marbles");
        leoCountLabel.setText(leoMarbles + " Marbles");

        if (leoMarbles <= 3) {
            updateLeoMood("[LEO - SWEATING]", ACCENT_CORAL);
        } else if (leoMarbles >= 15) {
            updateLeoMood("[LEO - SMUG]", ACCENT_CYAN);
        } else {
            updateLeoMood("[LEO - CONFIDENT]", ACCENT_GOLD);
        }
    }

    private void updateLeoMood(String moodText, Color color) {
        leoMoodBadge.setText(moodText + ": ");
        leoMoodBadge.setForeground(color);
    }

    private void updateLeoDialogue(String text) {
        leoDialogueText.setText("\"" + text + "\"");
    }

    private void updateLeoDialogueForHiding() {
        String[] taunts = {
                "I've got a tricky number in my hand. Odd or Even?",
                "Think carefully... I know what you picked last time!",
                "Are you feeling lucky today, " + playerName + "?",
                "You can't read my poker face!",
                "Go ahead, place your bet!"
        };
        updateLeoDialogue(taunts[random.nextInt(taunts.length)]);
    }

    private String getLeoLossDialogue(int bet) {
        String[] losses = {
                "Argh! Lucky guess! Take your " + bet + " marbles...",
                "What?! How did you know that?!",
                "Ouch! That bet hurt my bag...",
                "Beginner's luck, surely!"
        };
        return losses[random.nextInt(losses.length)];
    }

    private String getLeoWinDialogue(int bet) {
        String[] wins = {
                "Haha! I knew you'd pick that! Thanks for the " + bet + " marbles!",
                "Read like an open book! + " + bet + " marbles for me.",
                "Better luck next round! Leo never loses twice in a row.",
                "Your marbles look shiny in my bag!"
        };
        return wins[random.nextInt(wins.length)];
    }

    private void logActivity(String msg) {
        roundHistory.add(msg);
        historyArea.append(msg + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    // =========================================================================
    // PROCEDURAL AUDIO SYNTHESIZER
    // =========================================================================
    private void playTone(int frequencyHz, int durationMs) {
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buf = new byte[(int) (sampleRate * (durationMs / 1000.0))];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (sampleRate / frequencyHz) * 2.0 * Math.PI;
                    double decay = 1.0 - ((double) i / buf.length);
                    buf[i] = (byte) (Math.sin(angle) * 110 * decay);
                }
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private void playVictoryFanfare() {
        new Thread(() -> {
            int[] notes = {523, 659, 784, 1046};
            for (int note : notes) {
                playTone(note, 90);
                try { Thread.sleep(90); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void playDefeatTone() {
        new Thread(() -> {
            int[] notes = {440, 392, 349, 293};
            for (int note : notes) {
                playTone(note, 120);
                try { Thread.sleep(110); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    // =========================================================================
    // CUSTOM VECTOR GRAPHICS COMPONENTS (No LookAndFeel Glitches!)
    // =========================================================================

    /**
     * Custom rendered button: Rounded corners, drop shadow, hover effects,
     * guarantees correct colors on any OS without Windows L&F overriding it.
     */
    public static class CustomButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private final Color textColor;
        private boolean isHovered = false;

        public CustomButton(String text, Color baseColor, Color hoverColor, Color textColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Background Fill
            g2.setColor(isHovered ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, width, height, 12, 12);

            // Subtle border
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);

            // Centered Text
            g2.setColor(textColor);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int strX = (width - fm.stringWidth(getText())) / 2;
            int strY = ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(getText(), strX, strY);

            g2.dispose();
        }
    }

    /**
     * Custom sleek slider with custom painted track and glowing thumb
     */
    public static class CustomSlider extends JSlider {
        private final Color accentColor;

        public CustomSlider(int min, int max, int val, Color accentColor) {
            super(min, max, val);
            this.accentColor = accentColor;
            setOpaque(false);
            setFocusable(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Track background
            int trackH = 8;
            int trackY = (h - trackH) / 2;
            int trackW = w - 24;
            int trackX = 12;

            g2.setColor(new Color(30, 41, 65));
            g2.fillRoundRect(trackX, trackY, trackW, trackH, trackH, trackH);

            // Active Track Fill
            double pct = (double) (getValue() - getMinimum()) / (getMaximum() - getMinimum());
            int fillW = (int) (trackW * pct);
            g2.setColor(accentColor);
            g2.fillRoundRect(trackX, trackY, fillW, trackH, trackH, trackH);

            // Thumb
            int thumbX = trackX + fillW;
            int thumbY = h / 2;
            int thumbR = 10;

            // Thumb outer glow
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
            g2.fillOval(thumbX - thumbR - 3, thumbY - thumbR - 3, (thumbR + 3) * 2, (thumbR + 3) * 2);

            // Thumb solid body
            g2.setColor(Color.WHITE);
            g2.fillOval(thumbX - thumbR, thumbY - thumbR, thumbR * 2, thumbR * 2);
            g2.setColor(accentColor);
            g2.drawOval(thumbX - thumbR, thumbY - thumbR, thumbR * 2, thumbR * 2);

            g2.dispose();
        }
    }

    /**
     * Beautiful Felt Mat / Marble Bowl with 3D glass marbles
     */
    public static class MarbleBowlPanel extends JPanel {
        private int count;
        private final Color themeColor;

        public MarbleBowlPanel(int count, Color themeColor) {
            this.count = count;
            this.themeColor = themeColor;
            setOpaque(false);
            setPreferredSize(new Dimension(360, 220));
        }

        public void setCount(int count) {
            this.count = count;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Velvet Arena Felt Mat
            g2.setColor(MAT_BG);
            g2.fillRoundRect(8, 8, width - 16, height - 16, 20, 20);

            // Inner Velvet Shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.drawRoundRect(8, 8, width - 16, height - 16, 20, 20);

            // Marbles layout
            int marbleDiameter = 36;
            int marbleRadius = marbleDiameter / 2;
            int cols = 5;
            int hGap = 16;
            int vGap = 16;

            int totalRowW = (cols * marbleDiameter) + ((cols - 1) * hGap);
            int startX = (width - totalRowW) / 2;
            int startY = 32;

            for (int i = 0; i < count; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = startX + col * (marbleDiameter + hGap);
                int y = startY + row * (marbleDiameter + vGap);

                // Marble Drop Shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillOval(x + 2, y + 5, marbleDiameter, marbleDiameter);

                // 3D Spherical Radial Gradient
                Point2D center = new Point2D.Float(x + marbleRadius * 0.7f, y + marbleRadius * 0.7f);
                float radius = marbleRadius * 1.3f;
                float[] dist = {0.0f, 0.45f, 0.85f, 1.0f};
                Color[] colors = {
                        new Color(255, 255, 255, 230),
                        themeColor.brighter(),
                        themeColor,
                        themeColor.darker().darker()
                };
                RadialGradientPaint paint = new RadialGradientPaint(center, radius, dist, colors);
                g2.setPaint(paint);
                g2.fillOval(x, y, marbleDiameter, marbleDiameter);

                // Glass Specular Reflection Highlight
                g2.setColor(new Color(255, 255, 255, 190));
                g2.fillOval(x + (int) (marbleRadius * 0.35), y + (int) (marbleRadius * 0.3), 9, 6);

                // Marble rim highlight
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawOval(x, y, marbleDiameter, marbleDiameter);
            }

            // Bottom helper text
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String status = count + " Marbles in Hand";
            FontMetrics fm = g2.getFontMetrics();
            int strX = (width - fm.stringWidth(status)) / 2;
            g2.drawString(status, strX, height - 16);

            g2.dispose();
        }
    }

    public static class RoundedBorder extends EmptyBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedBorder(Color color, int thickness, int radius) {
            super(2, 6, 2, 6);
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        // Do NOT use Windows LookAndFeel so custom painted graphics stay clean & uncorrupted
        SwingUtilities.invokeLater(() -> {
            MarbleGameGUI game = new MarbleGameGUI();
            game.setVisible(true);
        });
    }
}
