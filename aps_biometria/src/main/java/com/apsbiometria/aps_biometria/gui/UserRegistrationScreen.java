package com.apsbiometria.aps_biometria.gui;

import javax.swing.*;

import com.apsbiometria.aps_biometria.Util.CPFValidator;
import com.apsbiometria.aps_biometria.Util.EmailValidator;
import com.apsbiometria.aps_biometria.Util.Logger;
import com.apsbiometria.aps_biometria.authentication.BiometricAuthenticator;
import com.apsbiometria.aps_biometria.biometric.BiometricCapture;
import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.BiometricData;
import com.apsbiometria.aps_biometria.model.User;
import com.apsbiometria.aps_biometria.repository.UserRepository;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class UserRegistrationScreen extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField cpfField;
    private JComboBox<String> accessLevelCombo;
    private JTextField departmentField;
    private JLabel imageLabel;
    private JButton selectImageButton;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private UserRepository userRepo;
    private BiometricAuthenticator bioAuth;
    private BiometricCapture capture;
    private BufferedImage selectedImage;

    public UserRegistrationScreen() {
        this.userRepo = new UserRepository();
        this.bioAuth = new BiometricAuthenticator();
        this.capture = new BiometricCapture();

        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Cadastro de Novo Usuário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Título
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(52, 152, 219));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("👤 Cadastro de Usuário");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Formulário
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Nome
        formPanel.add(createFieldPanel("Nome Completo:", nameField = new JTextField(25)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Email
        formPanel.add(createFieldPanel("Email:", emailField = new JTextField(25)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // CPF
        formPanel.add(createFieldPanel("CPF:", cpfField = new JTextField(25)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Nível de Acesso
        String[] levels = { "Nível 1 - Público", "Nível 2 - Diretor", "Nível 3 - Ministro" };
        accessLevelCombo = new JComboBox<>(levels);
        formPanel.add(createFieldPanel("Nível de Acesso:", accessLevelCombo));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Departamento
        formPanel.add(createFieldPanel("Departamento:", departmentField = new JTextField(25)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Imagem biométrica
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Foto Biométrica"));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setText("Sem foto");
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectImageButton = new JButton("📷 Selecionar Foto");
        selectImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        imagePanel.add(imageLabel);
        imagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        imagePanel.add(selectImageButton);

        formPanel.add(imagePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));

        registerButton = new JButton("✓ Cadastrar");
        registerButton.setPreferredSize(new Dimension(120, 35));
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        cancelButton = new JButton("✗ Cancelar");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(140, 25));

        panel.add(jLabel);
        panel.add(field);

        return panel;
    }

    private void setupListeners() {
        selectImageButton.addActionListener(e -> selectImage());
        registerButton.addActionListener(e -> performRegistration());
        cancelButton.addActionListener(e -> dispose());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$");
            }

            public String getDescription() {
                return "Imagens (*.jpg, *.jpeg, *.png)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                selectedImage = capture.captureFromFile(fileChooser.getSelectedFile().getAbsolutePath());

                ImageIcon icon = new ImageIcon(selectedImage);
                Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText("");

                setStatus("Foto carregada com sucesso", Color.GREEN);
            } catch (Exception ex) {
                setStatus("Erro ao carregar foto", Color.RED);
                Logger.error("Erro ao carregar imagem", ex);
            }
        }
    }

    private void performRegistration() {
        // Validações
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String cpf = cpfField.getText().trim();
        String department = departmentField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || cpf.isEmpty() || department.isEmpty()) {
            setStatus("Preencha todos os campos obrigatórios", Color.RED);
            return;
        }

        if (!EmailValidator.isValid(email)) {
            setStatus("Email inválido", Color.RED);
            return;
        }

        if (!CPFValidator.isValid(cpf)) {
            setStatus("CPF inválido", Color.RED);
            return;
        }

        if (selectedImage == null) {
            setStatus("Selecione uma foto biométrica", Color.RED);
            return;
        }

        setStatus("Cadastrando usuário...", Color.BLUE);
        registerButton.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Determinar nível de acesso
                int selectedIndex = accessLevelCombo.getSelectedIndex();
                AccessLevel level = AccessLevel.fromLevel(selectedIndex + 1);

                // Criar usuário
                User user = new User(name, email, CPFValidator.removeFormatting(cpf), level);
                user.setDepartment(department);
                user = userRepo.create(user);

                // Cadastrar biometria
                BiometricData bioData = bioAuth.enrollBiometric(user.getId(), selectedImage, "FACIAL");

                Logger.info("Usuário cadastrado: %s", user.getName());
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                UserRegistrationScreen.this,
                                "Usuário cadastrado com sucesso!",
                                "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                    }
                } catch (Exception ex) {
                    setStatus("Erro ao cadastrar: " + ex.getMessage(), Color.RED);
                    Logger.error("Erro no cadastro", ex);
                } finally {
                    registerButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        cpfField.setText("");
        departmentField.setText("");
        accessLevelCombo.setSelectedIndex(0);
        imageLabel.setIcon(null);
        imageLabel.setText("Sem foto");
        selectedImage = null;
        setStatus(" ", Color.BLACK);
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserRegistrationScreen().setVisible(true));
    }
}