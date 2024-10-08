package org.example.cliente;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.Callback;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class NotificationUI extends JFrame {
    private JTextArea notificationDisplayArea;
    private JTextField messageInputField;
    private JRadioButton priorityHighRadio;
    private JRadioButton priorityLowRadio;
    private JButton sendButton;
    private JLabel statusLabel;
    private KafkaProducer<String, String> kafkaProducer;
    private List<String> lowPriorityBatch = new ArrayList<>();

    public NotificationUI() {
        setTitle("Sistema de Notificações Acadêmicas");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        configureKafkaProducer();

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = createInputPanel();
        JPanel notificationPanel = createNotificationPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, notificationPanel);
        splitPane.setDividerLocation(300); // Largura da barra lateral
        splitPane.setOneTouchExpandable(true);

        statusLabel = new JLabel("Pronto para enviar notificações.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(100, 100, 100));

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void configureKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<>(props);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel messageLabel = new JLabel("Mensagem: ");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(messageLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        messageInputField = new JTextField(20);
        messageInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        inputPanel.add(messageInputField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel priorityLabel = new JLabel("Prioridade: ");
        priorityLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(priorityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        ButtonGroup priorityGroup = new ButtonGroup();
        priorityHighRadio = new JRadioButton("Alta");
        priorityLowRadio = new JRadioButton("Baixa");
        priorityGroup.add(priorityHighRadio);
        priorityGroup.add(priorityLowRadio);
        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));


        priorityHighRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priorityLowRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));

        priorityPanel.add(priorityHighRadio);
        priorityPanel.add(priorityLowRadio);
        inputPanel.add(priorityPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel sendLabel = new JLabel("Enviar: ");
        sendLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(sendLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        inputPanel.add(sendButton, gbc);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        return inputPanel;
    }

    private JPanel createNotificationPanel() {
        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setBackground(new Color(240, 240, 240));
        notificationPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        notificationDisplayArea = new JTextArea();
        notificationDisplayArea.setEditable(false);
        notificationDisplayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notificationDisplayArea.setLineWrap(true);
        notificationDisplayArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notificationDisplayArea);

        TitledBorder border = BorderFactory.createTitledBorder("Notificações");
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 16));
        scrollPane.setBorder(border);

        notificationPanel.add(scrollPane, BorderLayout.CENTER);

        return notificationPanel;
    }

    public void sendMessage() {
        String message = messageInputField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A mensagem não pode estar vazia!", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendButton.setEnabled(false);
        String topic = "notifications";
        Integer priority = priorityHighRadio.isSelected() ? 1 : 2;
        String messageWithPriority = message + " (Prioridade: " + priority + ")";

        statusLabel.setText("Enviando...");

        if (priority == 1) {
            kafkaProducer.send(new ProducerRecord<>(topic, messageWithPriority), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        JOptionPane.showMessageDialog(NotificationUI.this,
                                "Erro ao enviar a notificação: " + exception.getMessage(),
                                "Erro",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        displayNotification("Mensagem de alta prioridade enviada: " + messageWithPriority);
                    }
                    sendButton.setEnabled(true);
                    statusLabel.setText("Pronto para enviar notificações.");
                }
            });
        } else if (priority == 2) {
            lowPriorityBatch.add(messageWithPriority);
            if (lowPriorityBatch.size() == 5) {
                String batchedMessages = String.join(", ", lowPriorityBatch);
                kafkaProducer.send(new ProducerRecord<>(topic, batchedMessages), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception != null) {
                            JOptionPane.showMessageDialog(NotificationUI.this,
                                    "Erro ao enviar o lote de notificações: " + exception.getMessage(),
                                    "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            displayNotification("Lote de 5 mensagens enviado: " + batchedMessages);
                        }
                        lowPriorityBatch.clear();
                        sendButton.setEnabled(true);
                        statusLabel.setText("Pronto para enviar notificações.");
                    }
                });
            } else {
                displayNotification("Mensagem de baixa prioridade armazenada! " +
                        "Aguardando mais " + (5 - lowPriorityBatch.size()) + " mensagem(s).");
                sendButton.setEnabled(true);
                statusLabel.setText("Pronto para enviar notificações.");
            }
        }
    }

    private void displayNotification(String notification) {
        notificationDisplayArea.append(notification + "\n");
    }

    @Override
    public void dispose() {
        kafkaProducer.close();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotificationUI::new);
    }
}
