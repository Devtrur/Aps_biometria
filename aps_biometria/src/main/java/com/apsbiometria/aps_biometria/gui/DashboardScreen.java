package com.apsbiometria.aps_biometria.gui;

import javax.swing.*;
import java.awt.*;

import com.apsbiometria.aps_biometria.Util.DateFormatter;
import com.apsbiometria.aps_biometria.Util.Logger;
import com.apsbiometria.aps_biometria.authentication.AuthenticationService;
import com.apsbiometria.aps_biometria.authentication.Session;
import com.apsbiometria.aps_biometria.model.AccessLevel;

public class DashboardScreen extends JFrame {

    private Session session;
    private AuthenticationService authService;

    private JLabel welcomeLabel;
    private JLabel accessLevelLabel;
    private JTextArea dataArea;
    private JButton level1Button;
    private JButton level2Button;
    private JButton level3Button;
    private JButton logoutButton;
    private JButton refreshButton;

    public DashboardScreen(Session session) {
        this.session = session;
        this.authService = new AuthenticationService();

        initComponents();
        setupLayout();
        setupListeners();
        updateWelcomeInfo();
    }

    private void initComponents() {
        setTitle("Sistema Biométrico - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.warning("Erro ao definir look and feel");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Painel superior - Informações do usuário
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(41, 128, 185));

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        accessLevelLabel = new JLabel();
        accessLevelLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        accessLevelLabel.setForeground(Color.WHITE);

        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(accessLevelLabel);

        topPanel.add(userInfoPanel, BorderLayout.WEST);

        // Botões de ação no topo
        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtonsPanel.setBackground(new Color(41, 128, 185));

        refreshButton = new JButton("🔄 Atualizar");
        refreshButton.setFocusPainted(false);

        logoutButton = new JButton("🚪 Sair");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);

        topButtonsPanel.add(refreshButton);
        topButtonsPanel.add(logoutButton);

        topPanel.add(topButtonsPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Painel esquerdo - Níveis de acesso
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        leftPanel.setPreferredSize(new Dimension(200, 0));

        JLabel menuLabel = new JLabel("Níveis de Acesso");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(menuLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        level1Button = createMenuButton("📊 Nível 1", "Informações Públicas", new Color(46, 204, 113));
        level2Button = createMenuButton("📋 Nível 2", "Dados de Diretores", new Color(241, 196, 15));
        level3Button = createMenuButton("🔒 Nível 3", "Dados Confidenciais", new Color(231, 76, 60));

        leftPanel.add(level1Button);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(level2Button);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(level3Button);
        leftPanel.add(Box.createVerticalGlue());

        add(leftPanel, BorderLayout.WEST);

        // Painel central - Área de dados
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel dataLabel = new JLabel("Informações do Sistema");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(dataLabel, BorderLayout.NORTH);

        dataArea = new JTextArea();
        dataArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dataArea.setEditable(false);
        dataArea.setLineWrap(true);
        dataArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(dataArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Painel inferior - Status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel statusLabel = new JLabel("Sistema operacional");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);

        JLabel timeLabel = new JLabel(DateFormatter.now());
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        timeLabel.setForeground(Color.GRAY);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(timeLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text, String tooltip, Color color) {
        JButton button = new JButton("<html><center>" + text + "</center></html>");
        button.setToolTipText(tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 60));
        button.setPreferredSize(new Dimension(180, 60));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    private void setupListeners() {
        level1Button.addActionListener(e -> loadLevel1Data());
        level2Button.addActionListener(e -> loadLevel2Data());
        level3Button.addActionListener(e -> loadLevel3Data());
        refreshButton.addActionListener(e -> updateWelcomeInfo());
        logoutButton.addActionListener(e -> performLogout());
    }

    private void updateWelcomeInfo() {
        welcomeLabel.setText("👤 Bem-vindo(a), " + session.getUser().getName());
        accessLevelLabel.setText("Nível de Acesso: " + session.getUser().getAccessLevel().toDisplayString());

        AccessLevel userLevel = session.getUser().getAccessLevel();

        level1Button.setEnabled(userLevel.getLevel() >= 1);
        level2Button.setEnabled(userLevel.getLevel() >= 2);
        level3Button.setEnabled(userLevel.getLevel() >= 3);

        StringBuilder info = new StringBuilder();
        info.append("========== INFORMAÇÕES DA SESSÃO ==========\n\n");
        info.append("Usuário: ").append(session.getUser().getName()).append("\n");
        info.append("Email: ").append(session.getUser().getEmail()).append("\n");
        info.append("CPF: ").append(session.getUser().getCpf()).append("\n");
        info.append("Departamento: ").append(session.getUser().getDepartment()).append("\n");
        info.append("Nível de Acesso: ").append(session.getUser().getAccessLevel()).append("\n\n");
        info.append("Login: ").append(DateFormatter.formatDateTime(session.getLoginTime())).append("\n");
        info.append("Duração da Sessão: ").append(session.getSessionDurationMinutes()).append(" minutos\n");
        info.append("Score de Autenticação: ").append(String.format("%.2f%%", session.getAuthenticationScore() * 100))
                .append("\n");
        info.append("IP: ").append(session.getIpAddress()).append("\n\n");
        info.append("==========================================\n\n");
        info.append("Selecione um nível de acesso no menu lateral para visualizar os dados.");

        dataArea.setText(info.toString());
        dataArea.setCaretPosition(0);
    }

    private void loadLevel1Data() {
        dataArea.setText("Carregando dados públicos...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return authService.getPublicData(session.getSessionId());
            }

            @Override
            protected void done() {
                try {
                    String data = get();
                    dataArea.setText(data);
                    dataArea.setCaretPosition(0);
                    Logger.logDataAccess(session.getUser().getId(), "NIVEL_1", true);
                } catch (Exception ex) {
                    dataArea.setText("Erro ao carregar dados: " + ex.getMessage());
                    Logger.error("Erro ao carregar nível 1", ex);
                }
            }
        }.execute();
    }

    private void loadLevel2Data() {
        dataArea.setText("Carregando dados de diretores...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return authService.getDirectorData(session.getSessionId());
            }

            @Override
            protected void done() {
                try {
                    String data = get();

                    if (data.contains("ACESSO NEGADO")) {
                        JOptionPane.showMessageDialog(
                                DashboardScreen.this,
                                "Você não tem permissão para acessar estes dados.\n" +
                                        "Nível necessário: Diretor (Nível 2)",
                                "Acesso Negado",
                                JOptionPane.WARNING_MESSAGE);
                    }

                    dataArea.setText(data);
                    dataArea.setCaretPosition(0);
                    Logger.logDataAccess(session.getUser().getId(), "NIVEL_2",
                            !data.contains("ACESSO NEGADO"));
                } catch (Exception ex) {
                    dataArea.setText("Erro ao carregar dados: " + ex.getMessage());
                    Logger.error("Erro ao carregar nível 2", ex);
                }
            }
        }.execute();
    }

    private void loadLevel3Data() {
        dataArea.setText("Carregando dados confidenciais...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return authService.getMinisterData(session.getSessionId());
            }

            @Override
            protected void done() {
                try {
                    String data = get();

                    if (data.contains("ACESSO NEGADO")) {
                        JOptionPane.showMessageDialog(
                                DashboardScreen.this,
                                "Você não tem permissão para acessar estes dados.\n" +
                                        "Nível necessário: Ministro (Nível 3)",
                                "Acesso Negado",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    dataArea.setText(data);
                    dataArea.setCaretPosition(0);
                    Logger.logDataAccess(session.getUser().getId(), "NIVEL_3",
                            !data.contains("ACESSO NEGADO"));
                } catch (Exception ex) {
                    dataArea.setText("Erro ao carregar dados: " + ex.getMessage());
                    Logger.error("Erro ao carregar nível 3", ex);
                }
            }
        }.execute();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair do sistema?",
                "Confirmar Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout(session.getSessionId());
            Logger.logSessionEnd(session.getUser().getId(), session.getSessionId());

            // Voltar para tela de login
            SwingUtilities.invokeLater(() -> {
                new LoginScreen().setVisible(true);
                dispose();
            });
        }
    }
}
