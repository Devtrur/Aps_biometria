package com.apsbiometria.aps_biometria.gui;

import javax.swing.*;

import com.apsbiometria.aps_biometria.Util.CPFValidator;
import com.apsbiometria.aps_biometria.Util.Logger;
import com.apsbiometria.aps_biometria.authentication.AuthenticationService;
import com.apsbiometria.aps_biometria.authentication.Session;
import com.apsbiometria.aps_biometria.biometric.BiometricCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class LoginScreen extends JFrame {

    private JTextField cpfField;
    private JLabel imageLabel;
    private JButton selectImageButton;
    private JButton loginButton;
    private JButton identifyButton;
    private JLabel statusLabel;

    private AuthenticationService authService;
    private BiometricCapture capture;
    private BufferedImage selectedImage;
    private String selectedImagePath;

    public LoginScreen() {
        this.authService = new AuthenticationService();
        this.capture = new BiometricCapture();

        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Sistema de Autenticação Biométrica - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.warning("Não foi possível definir look and feel");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Painel superior - Título
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 102, 204));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🔐 Autenticação Biométrica");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Painel central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // CPF
        JPanel cpfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel cpfLabel = new JLabel("CPF:");
        cpfLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        cpfField = new JTextField(20);
        cpfField.setFont(new Font("Arial", Font.PLAIN, 14));
        cpfPanel.add(cpfLabel);
        cpfPanel.add(cpfField);
        centerPanel.add(cpfPanel);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Imagem biométrica
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Imagem Biométrica"));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(200, 200));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setText("Nenhuma imagem selecionada");
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectImageButton = new JButton("📁 Selecionar Imagem");
        selectImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectImageButton.setFont(new Font("Arial", Font.PLAIN, 14));

        imagePanel.add(imageLabel);
        imagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        imagePanel.add(selectImageButton);

        centerPanel.add(imagePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botões de ação
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        loginButton = new JButton("🔑 Login (1:1)");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        identifyButton = new JButton("🔍 Identificar (1:N)");
        identifyButton.setFont(new Font("Arial", Font.BOLD, 14));
        identifyButton.setPreferredSize(new Dimension(170, 40));
        identifyButton.setBackground(new Color(52, 152, 219));
        identifyButton.setForeground(Color.WHITE);
        identifyButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(identifyButton);

        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(statusLabel);

        add(centerPanel, BorderLayout.CENTER);

        // Painel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel infoLabel = new JLabel("Ministério do Meio Ambiente - Sistema de Controle de Acesso");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoLabel.setForeground(Color.GRAY);
        bottomPanel.add(infoLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // Selecionar imagem
        selectImageButton.addActionListener(e -> selectImage());

        // Login (1:1 - Verificação)
        loginButton.addActionListener(e -> performLogin());

        // Identificação (1:N)
        identifyButton.addActionListener(e -> performIdentification());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".jpg") ||
                        f.getName().toLowerCase().endsWith(".jpeg") ||
                        f.getName().toLowerCase().endsWith(".png");
            }

            public String getDescription() {
                return "Imagens (*.jpg, *.jpeg, *.png)";
            }
        });

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
                selectedImage = capture.captureFromFile(selectedImagePath);

                // Exibir imagem redimensionada
                ImageIcon icon = new ImageIcon(selectedImage);
                Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText("");

                setStatus("Imagem carregada com sucesso", Color.GREEN);
                Logger.info("Imagem selecionada: %s", selectedImagePath);

            } catch (Exception ex) {
                setStatus("Erro ao carregar imagem: " + ex.getMessage(), Color.RED);
                Logger.error("Erro ao carregar imagem", ex);
            }
        }
    }

    private void performLogin() {
        String cpf = cpfField.getText().trim();

        // Validações
        if (cpf.isEmpty()) {
            setStatus("Por favor, informe o CPF", Color.RED);
            return;
        }

        if (!CPFValidator.isValid(cpf)) {
            setStatus("CPF inválido", Color.RED);
            return;
        }

        if (selectedImage == null) {
            setStatus("Por favor, selecione uma imagem biométrica", Color.RED);
            return;
        }

        String cpfClean = CPFValidator.removeFormatting(cpf);

        setStatus("Autenticando...", Color.BLUE);
        loginButton.setEnabled(false);
        identifyButton.setEnabled(false);

        new SwingWorker<Session, Void>() {
            @Override
            protected Session doInBackground() throws Exception {
                return authService.login(cpfClean, selectedImage, "127.0.0.1");
            }

            @Override
            protected void done() {
                try {
                    Session session = get();

                    if (session != null) {
                        setStatus("Login bem-sucedido!", Color.GREEN);
                        Logger.logSessionStart(session.getUser().getId(), session.getSessionId());

                        // Abrir dashboard
                        SwingUtilities.invokeLater(() -> {
                            new DashboardScreen(session).setVisible(true);
                            dispose();
                        });
                    } else {
                        setStatus("Falha na autenticação. Verifique seus dados.", Color.RED);
                    }

                } catch (Exception ex) {
                    setStatus("Erro: " + ex.getMessage(), Color.RED);
                    Logger.error("Erro no login", ex);
                } finally {
                    loginButton.setEnabled(true);
                    identifyButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void performIdentification() {
        if (selectedImage == null) {
            setStatus("Por favor, selecione uma imagem biométrica", Color.RED);
            return;
        }

        setStatus("Identificando usuário...", Color.BLUE);
        loginButton.setEnabled(false);
        identifyButton.setEnabled(false);

        // Executar em thread separada
        new SwingWorker<Session, Void>() {
            @Override
            protected Session doInBackground() throws Exception {
                return authService.loginByIdentification(selectedImage, "127.0.0.1");
            }

            @Override
            protected void done() {
                try {
                    Session session = get();

                    if (session != null) {
                        setStatus("Usuário identificado: " + session.getUser().getName(), Color.GREEN);
                        Logger.logSessionStart(session.getUser().getId(), session.getSessionId());

                        SwingUtilities.invokeLater(() -> {
                            new DashboardScreen(session).setVisible(true);
                            dispose();
                        });
                    } else {
                        setStatus("Não foi possível identificar o usuário", Color.RED);
                    }

                } catch (Exception ex) {
                    setStatus("Erro: " + ex.getMessage(), Color.RED);
                    Logger.error("Erro na identificação", ex);
                } finally {
                    loginButton.setEnabled(true);
                    identifyButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Logger.logApplicationStart();
            new LoginScreen().setVisible(true);
        });
    }
}
