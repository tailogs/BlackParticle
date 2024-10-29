package com.tailogs;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JFrame {
    private final RSyntaxTextArea textArea;
    private final JTextArea consoleArea; // Поле для консоли
    private final JTextField commandInput; // Поле для ввода команд
    private final JFileChooser fileChooser;
    private final JTree projectTree;
    private final DefaultMutableTreeNode rootNode;
    private final JLabel statusLabel; // Поле состояния
    private final Timer blinkTimer;
    private File currentFile; // Текущий открытый файл
    private boolean darkTheme = true; // Переменная для хранения темы
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private final JLabel fileInfoLabel; // Поле для информации о файле
    private boolean fileSaved = true; // Статус сохранения файла

    public Main() {
        // Настройки основного окна
        setTitle("BlackParticle");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Панель состояния
        statusLabel = new JLabel("Lines: 0 | File not saved");
        statusLabel.setForeground(Color.LIGHT_GRAY);

        // Поле для информации о файле
        fileInfoLabel = new JLabel("Current File: None | Status: Unsaved | Encoding: UTF-8");
        fileInfoLabel.setForeground(Color.LIGHT_GRAY);

        // Настройка текстовой области с подсветкой синтаксиса
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setCodeFoldingEnabled(true);
        Font font = new Font("Monospaced", Font.PLAIN, 16); // Установка шрифта
        textArea.setFont(font);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        // Установить темный фон для нумерации строк
        sp.setLineNumbersEnabled(true);
        sp.getGutter().setBackground(new Color(30, 30, 30)); // Темный фон для нумерации строк
        sp.getGutter().setForeground(Color.LIGHT_GRAY); // Цвет текста для нумерации строк

        // Файловый выбор
        fileChooser = new JFileChooser();

        // Создание корневой директории для дерева проекта
        rootNode = new DefaultMutableTreeNode("Проект");
        projectTree = new JTree(rootNode);
        projectTree.setBackground(new Color(40, 40, 40)); // Темный фон для дерева
        projectTree.setForeground(Color.LIGHT_GRAY); // Цвет текста в дереве

        // Создание Renderer для узлов дерева
        DefaultTreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();
        treeCellRenderer.setBackground(new Color(40, 40, 40)); // Темный фон для узлов дерева
        treeCellRenderer.setTextNonSelectionColor(Color.LIGHT_GRAY); // Цвет текста для узлов дерева
        treeCellRenderer.setBackgroundNonSelectionColor(new Color(40, 40, 40)); // Фон для узлов, когда они не выбраны

        // Применение Renderer к дереву
        projectTree.setCellRenderer(treeCellRenderer);

        projectTree.addTreeSelectionListener(_ -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.isLeaf()) {
                File file = (File) selectedNode.getUserObject();
                openFile(file);
            }
        });

        projectTree.addTreeSelectionListener(_ -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.isLeaf()) {
                File file = (File) selectedNode.getUserObject();
                openFile(file);
            }
        });

        // Установка темы
        setTheme(darkTheme);

        // Создание меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open catalog");
        openItem.addActionListener(new OpenDirectoryAction());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new SaveAction());

        JMenuItem runItem = new JMenuItem("Run");
        runItem.addActionListener(new RunAction());

        JMenuItem themeToggleItem = new JMenuItem("Change theme");
        themeToggleItem.addActionListener(_ -> {
            darkTheme = !darkTheme; // Переключение темы
            setTheme(darkTheme);
            updateStatus(); // Обновляем статус после изменения темы
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(runItem);
        fileMenu.add(themeToggleItem); // Добавление переключателя темы

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Добавление компонентов в основное окно
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(projectTree), sp);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        add(statusLabel, BorderLayout.SOUTH); // Добавление панели состояния

        // Инициализация консоли
        consoleArea = new JTextArea(10, 60); // Указываем количество строк для консоли
        consoleArea.setEditable(false); // Запрещаем редактирование пользователю
        consoleArea.setBackground(new Color(30, 30, 30)); // Темный фон для консоли
        consoleArea.setForeground(Color.LIGHT_GRAY); // Цвет текста
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Шрифт консоли
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea); // Добавление прокрутки

        // Поле для ввода команд
        commandInput = new JTextField();
        commandInput.setBackground(new Color(50, 50, 50)); // Темный фон для ввода
        commandInput.setForeground(Color.LIGHT_GRAY);
        commandInput.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JTextField finalCommandInput = commandInput;
        commandInput.addActionListener(_ -> executeCommand(finalCommandInput.getText()));

        // Создание таймера для мигающего эффекта
        blinkTimer = new Timer(500, _ -> {
            // Переключаем цвет фона поля ввода между черным и темным серым
            if (commandInput.getBackground().equals(new Color(50, 50, 50))) {
                commandInput.setBackground(new Color(70, 70, 70)); // Светлее
            } else {
                commandInput.setBackground(new Color(50, 50, 50)); // Темнее
            }
        });
        blinkTimer.start(); // Запуск таймера

        // Добавление символов ввода
        commandInput.setText(">>> "); // Символы для ввода
        commandInput.setCaretPosition(commandInput.getText().length()); // Установка курсора в конец

        // Добавление консоли в нижнюю часть интерфейса
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, consoleScrollPane);
        mainSplitPane.setDividerLocation(400); // Положение разделителя
        add(mainSplitPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(commandInput, BorderLayout.NORTH); // Поле ввода команд
        bottomPanel.add(fileInfoLabel, BorderLayout.SOUTH); // Метка информации о файле
        add(bottomPanel, BorderLayout.SOUTH);

        // Добавление KeyBinding для сохранения файла при нажатии Ctrl+S
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveFile");
        textArea.getActionMap().put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
    }

    private void executeCommand(String command) {
        if (command.isBlank()) return;

        // Удаляем символы ">>>" перед выполнением команды
        command = command.replace(">>> ", "");

        // Проверка на команды cls или clear
        if (command.trim().equalsIgnoreCase("cls") || command.trim().equalsIgnoreCase("clear")) {
            consoleArea.setText(""); // Очищаем консоль
            commandInput.setText(">>> "); // Устанавливаем ">>>" для следующего ввода
            return; // Выходим из метода, не выполняя дальнейшие действия
        }

        consoleArea.append(">> " + command + "\n"); // Отображаем команду

        String finalCommand = command;
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Разделяем и запускаем процесс
                    String[] commandArray = finalCommand.split(" ");
                    ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("Windows-1251")))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Преобразуем строку в UTF-8 и публикуем результат
                            publish(new String(line.getBytes("Windows-1251"), StandardCharsets.UTF_8));
                        }
                    }
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    publish("Command execution failed: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String line : chunks) {
                    consoleArea.append(line + "\n");
                }
            }

            @Override
            protected void done() {
                commandInput.setText(">>> "); // Устанавливаем ">>>" для следующего ввода
                blinkTimer.stop(); // Останавливаем мигание после выполнения команды
            }
        };
        worker.execute(); // Запускаем фоновую задачу
    }

    private void saveFile() {
        if (currentFile == null) {
            // Если файл не выбран, вызываем диалог для сохранения
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
            } else {
                return; // Если пользователь отменил, выходим
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentFile), StandardCharsets.UTF_8))) {
            writer.write(textArea.getText());
            fileSaved = true; // Обновляем статус сохранения
            updateFileInfo();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    private void updateFileInfo() {
        if (currentFile != null) {
            fileInfoLabel.setText("Current File: " + currentFile.getName() + " | Status: " + (fileSaved ? "Saved" : "Unsaved") + " | Encoding: UTF-8");
        } else {
            fileInfoLabel.setText("Current File: None | Status: Unsaved | Encoding: UTF-8");
        }
    }

    private void setTheme(boolean dark) {
        if (dark) {
            textArea.setBackground(new Color(30, 30, 30));
            textArea.setForeground(Color.LIGHT_GRAY);
            statusLabel.setForeground(Color.LIGHT_GRAY);
            projectTree.setBackground(new Color(40, 40, 40));
            projectTree.setForeground(Color.LIGHT_GRAY);

            // Установка цветов для других компонентов
            UIManager.put("Panel.background", new Color(30, 30, 30)); // Темный фон для панели
            UIManager.put("Label.foreground", Color.LIGHT_GRAY); // Цвет текста для меток
            UIManager.put("TextField.background", new Color(50, 50, 50)); // Фон текстового поля
            UIManager.put("TextField.foreground", Color.LIGHT_GRAY); // Цвет текста в текстовом поле
            UIManager.put("Button.background", new Color(50, 50, 50)); // Фон кнопок
            UIManager.put("Button.foreground", Color.LIGHT_GRAY); // Цвет текста на кнопках
            UIManager.put("MenuBar.background", new Color(40, 40, 40)); // Фон меню
            UIManager.put("Menu.foreground", Color.LIGHT_GRAY); // Цвет текста в меню
            UIManager.put("MenuItem.background", new Color(50, 50, 50)); // Фон пунктов меню
            UIManager.put("MenuItem.foreground", Color.LIGHT_GRAY); // Цвет текста в пунктах меню

            SwingUtilities.updateComponentTreeUI(this); // Обновление интерфейса
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            statusLabel.setForeground(Color.BLACK);
            projectTree.setBackground(Color.WHITE);
            projectTree.setForeground(Color.BLACK);

            // Установка цветов для светлой темы
            UIManager.put("Panel.background", Color.WHITE); // Светлый фон для панели
            UIManager.put("Label.foreground", Color.BLACK); // Цвет текста для меток
            UIManager.put("TextField.background", Color.WHITE); // Фон текстового поля
            UIManager.put("TextField.foreground", Color.BLACK); // Цвет текста в текстовом поле
            UIManager.put("Button.background", Color.LIGHT_GRAY); // Светлый фон кнопок
            UIManager.put("Button.foreground", Color.BLACK); // Цвет текста на кнопках
            UIManager.put("MenuBar.background", Color.WHITE); // Фон меню
            UIManager.put("Menu.foreground", Color.BLACK); // Цвет текста в меню
            UIManager.put("MenuItem.background", Color.WHITE); // Фон пунктов меню
            UIManager.put("MenuItem.foreground", Color.BLACK); // Цвет текста в пунктах меню

            SwingUtilities.updateComponentTreeUI(this); // Обновление интерфейса
        }
    }

    private String getSyntaxStyle(String fileName) {
        String extension = getFileExtension(fileName);
        return switch (extension) {
            case "c", "h" -> SyntaxConstants.SYNTAX_STYLE_C;  // Для .c и .h файлов
            case "cpp", "hpp" -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;  // Для .cpp и .hpp файлов
            case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "py" -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "cs" -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "js" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "rb" -> SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "php" -> SyntaxConstants.SYNTAX_STYLE_PHP;
            case "go" -> SyntaxConstants.SYNTAX_STYLE_GO;
            case "scala" -> SyntaxConstants.SYNTAX_STYLE_SCALA;
            case "lua" -> SyntaxConstants.SYNTAX_STYLE_LUA;
            case "pl" -> SyntaxConstants.SYNTAX_STYLE_PERL;
            case "dart" -> SyntaxConstants.SYNTAX_STYLE_DART;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private class OpenDirectoryAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = directoryChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = directoryChooser.getSelectedFile();
                loadFilesIntoTree(selectedDirectory);
                updateFileListDisplay(); // Обновление отображения списка файлов
            }
        }
    }

    private void loadFilesIntoTree(File directory) {
        rootNode.removeAllChildren();
        addFilesToTree(directory, rootNode);
        ((DefaultTreeModel) projectTree.getModel()).reload();
    }

    private void addFilesToTree(File directory, DefaultMutableTreeNode node) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    // Используем только имя файла вместо полного пути
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file.getName());
                    // Сохраняем файл как объект, чтобы его можно было открыть
                    childNode.setUserObject(file);
                    node.add(childNode);
                    if (file.isDirectory()) {
                        addFilesToTree(file, childNode);
                    }
                }
            }
        }
    }

    private void openFile(File file) {
        if (file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                currentFile = file;
                textArea.setSyntaxEditingStyle(getSyntaxStyle(file.getName())); // Установка стиля синтаксиса
                updateStatus(); // Обновляем статус после открытия файла
            } catch (IOException e) {
                logger.log(Level.SEVERE, "An exception occurred", e);
            }
        }
    }

    private class SaveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentFile != null) {
                saveFile(currentFile);
            } else {
                int returnValue = fileChooser.showSaveDialog(Main.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    saveFile(selectedFile);
                }
            }
        }
    }

    private void saveFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
            currentFile = file;
            updateStatus(); // Обновляем статус после сохранения файла
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    private void updateStatus() {
        int lineCount = textArea.getLineCount();
        String fileStatus = (currentFile != null) ? currentFile.getName() : "File not saved";
        statusLabel.setText("Line: " + lineCount + " | " + fileStatus);
    }

    private class RunAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Реализация запуска кода (если необходимо)
            JOptionPane.showMessageDialog(Main.this, "Running code not implemented.");
        }
    }

    private void updateFileListDisplay() {
        // Обновляем дерево проекта, если оно уже загружено
        if (rootNode != null) {
            ((DefaultTreeModel) projectTree.getModel()).reload(rootNode);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main editor = new Main();
            editor.setVisible(true);
        });
    }
}
