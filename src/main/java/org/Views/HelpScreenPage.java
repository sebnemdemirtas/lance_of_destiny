package org.Views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HelpScreenPage extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JButton btnNext, btnPrevious;
    private int currentPage = 0;
    private final String[] helpTexts = {
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Welcome to Lance of Destiny</h1><p>This game is about two warriors racing to obtain the Lance of Power. Use spells and skills to overcome barriers and defeat your opponent.<br>You can play singleplayer and multiplayer mode.</p></body></html>",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Gameplay</h1><p>Control the Magical Staff to direct the Fire Ball to destroy barriers and prevent it from falling.</p></html>",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Barriers</h1><p>There are several types of barriers like Simple Barrier, Reinforced Barrier, and Explosive Barrier, each with different properties.</p></html>",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Spells</h1><p>Collect spells to enhance your abilities or hinder your opponent. Spells like 'Hex' and 'Magical Staff Expansion' can be pivotal in gameplay.</p></html>",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Controls I</h1><p>Use arrow keys to move the Magical Staff.</p></html>",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Controls II</h1><p>Use 'A' and 'D' to rotate it.</p></html>",
            "<html><head><style>body { text-align: center; } li { text-align: left; }</style></head><body><h1>Singleplayer & Multiplayer</h1><p>The game can be played in single player and multiplayer modes.</p><ul><li><strong>Host a Game:</strong> Select this option to create a new game session that other players can join.</li><li><strong>Join a Game:</strong> If you want to join an existing game, select this option. You can select the game you want to join from the list.</li></ul></body></html>\n",
            "<html><head><style>body { text-align: center; }</style></head><body><h1>Build the Game</h1><p>In building mode, there are two options for placing barriers: <br> 1) Specify the quantity of each type of barrier and let the game automatically place them. <br> 2) Manually select the type of barrier and its location.</p></html>"
    };

    public HelpScreenPage() {
        setTitle("Help - Lance of Destiny");
        setSize(800, 500); // Square size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        for (String text : helpTexts) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(text);
            label.setHorizontalAlignment(JLabel.CENTER);

            if (text.contains("Use arrow keys to move the Magical Staff")) {
                ImageIcon gifIcon = new ImageIcon("assets/move.gif");
                JLabel gifLabel = new JLabel(gifIcon);
                panel.add(gifLabel, BorderLayout.NORTH);
            } else if (text.contains("Use 'A' and 'D' to rotate it")) {
                ImageIcon gifIcon = new ImageIcon("assets/rotate.gif");
                JLabel gifLabel = new JLabel(gifIcon);
                panel.add(gifLabel, BorderLayout.NORTH);
            } else if (text.contains("In building mode, there are two options for placing barriers:")) {
                ImageIcon gifIcon = new ImageIcon("assets/BuildingMode.gif");
                JLabel gifLabel = new JLabel(gifIcon);
                panel.add(gifLabel, BorderLayout.NORTH);
            }

            panel.add(label);
            mainPanel.add(panel);
        }

        btnNext = new JButton("Next");
        btnPrevious = new JButton("Previous");
        btnPrevious.setEnabled(false);

        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPage < helpTexts.length - 1) {
                    currentPage++;
                    cardLayout.next(mainPanel);
                    btnPrevious.setEnabled(true);
                }
                if (currentPage == helpTexts.length - 1) {
                    btnNext.setEnabled(false);
                }
            }
        });

        btnPrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPage > 0) {
                    currentPage--;
                    cardLayout.previous(mainPanel);
                    btnNext.setEnabled(true);
                }
                if (currentPage == 0) {
                    btnPrevious.setEnabled(false);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnPrevious);
        buttonPanel.add(btnNext);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

}