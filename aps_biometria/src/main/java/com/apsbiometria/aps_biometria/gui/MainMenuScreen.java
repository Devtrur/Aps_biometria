package com.apsbiometria.aps_biometria.gui;

import javax.swing.*;

import com.apsbiometria.aps_biometria.Util.Logger;

import java.awt.*;

public class MainMenuScreen extends JFrame {

    private JButton loginButton;
    private JButton registerButton;
    private JButton aboutButton;
    private JButton exitButton;

    public MainMenuScreen() {
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Sistema de Autenticação Biométrica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.warning("Erro ao definir look and feel");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Painel superior - Logo e título
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(44, 62, 80));
        topPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel logoLabel = new JLabel("🔐");
        logoLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Sistema Biométrico");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Ministério do Meio Ambiente");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(logoLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(subtitleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Painel central - Botões
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        centerPanel.setBackground(Color.WHITE);

        loginButton = createMenuButton("🔑 Fazer Login", new Color(52, 152, 219));
        registerButton = createMenuButton("👤 Cadastrar Usuário", new Color(46, 204, 113));
        aboutButton = createMenuButton("ℹ️ Sobre o Sistema", new Color(149, 165, 166));
        exitButton = createMenuButton("🚪 Sair", new Color(231, 76, 60));

        centerPanel.add(loginButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(registerButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(aboutButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(exitButton);

        add(centerPanel, BorderLayout.CENTER);

        // Painel inferior - Info
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(236, 240, 241));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Versão 1.0.0 | APS - Ciência da Computação");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoLabel.setForeground(Color.GRAY);

        bottomPanel.add(infoLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 50));
        button.setPreferredSize(new Dimension(300, 50));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void setupListeners() {
        loginButton.addActionListener(e -> openLoginScreen());
        registerButton.addActionListener(e -> openRegistrationScreen());
        aboutButton.addActionListener(e -> showAboutDialog());
        exitButton.addActionListener(e -> exitApplication());
    }

    private void openLoginScreen() {
        new LoginScreen().setVisible(true);
        dispose();
    }

    private void openRegistrationScreen() {
        new UserRegistrationScreen().setVisible(true);
    }

    private void showAboutDialog() {
        String message = """
                Sistema de Autenticação Biométrica

                Desenvolvido como Atividade Prática Supervisionada (APS)
                Curso: Ciência da Computação - 5º/6º Semestre
                Disciplina: Processamento de Imagem e Visão Computacional

                Funcionalidades:
                • Autenticação biométrica facial
                • Controle de acesso em 3 níveis
                • Cadastro de usuários
                • Logs de auditoria
                • Gerenciamento de sessões

                Tecnologias:
                • Java 11+
                • Swing (Interface Gráfica)
                • H2 Database / PostgreSQL
                • Processamento de Imagens

                © 2025 - Ministério do Meio Ambiente
                """;

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Sobre o Sistema",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair?",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Logger.logApplicationStop();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Logger.logApplicationStart();
            new MainMenuScreen().setVisible(true);
        });
    }
}