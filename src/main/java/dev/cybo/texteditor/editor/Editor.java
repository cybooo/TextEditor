package dev.cybo.texteditor.editor;

import dev.cybo.texteditor.Main;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Editor {

    private static final JFrame FRAME = new JFrame();
    private static final AtomicReference<JTextArea> SELECTED_TEXT_AREA = new AtomicReference<>();

    public Editor() {

        try {
            URL url = Main.class.getClassLoader().getResource("icon.png");
            File imageFile = new File(Objects.requireNonNull(url).toURI());
            FRAME.setIconImage(ImageIO.read(imageFile));
        } catch (URISyntaxException | IOException exception) {
            exception.printStackTrace();
        }

        FRAME.setTitle("Text Editor");
        FRAME.setSize(1200, 800);
        FRAME.setLocationRelativeTo(null);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.setFocusable(false);
        jTabbedPane.setSize(1000, 20);

        jTabbedPane.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent event) {
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isMiddleMouseButton(event) && jTabbedPane.getSelectedIndex() != 0) {
                    jTabbedPane.remove(jTabbedPane.getSelectedIndex());
                }

                if (SwingUtilities.isRightMouseButton(event) && jTabbedPane.getSelectedIndex() != 0) {

                    JPopupMenu jPopupMenu = new JPopupMenu();
                    JMenuItem delete = new JMenuItem("Close");

                    delete.addActionListener(actionEvent -> {
                        if (jTabbedPane.getSelectedIndex() == 0) {
                            showNoFileSelectedWarning();
                            return;
                        }
                        closeSelectedFile(jTabbedPane);
                    });

                    JMenuItem save = new JMenuItem("Save");
                    save.addActionListener(actionEvent -> saveFile(jTabbedPane));
                    jPopupMenu.add(delete);
                    jPopupMenu.add(save);
                    jPopupMenu.show(FRAME, event.getX(), event.getY() + 50);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
            }

            @Override
            public void mouseEntered(MouseEvent event) {
            }

            @Override
            public void mouseExited(MouseEvent event) {
            }
        });

        addDefaultButtons(jTabbedPane);

        FRAME.add(jTabbedPane);
        FRAME.setVisible(true);
    }

    private static void addDefaultButtons(JTabbedPane jTabbedPane) {

        JMenuBar jMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem newFileItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem fontSizeItem = new JMenuItem("Font Size");

        newFileItem.addActionListener(event -> createNewFile(jTabbedPane));
        openItem.addActionListener(event -> openFile(jTabbedPane));
        exitItem.addActionListener(event -> System.exit(0));
        saveItem.addActionListener(event -> saveFile(jTabbedPane));
        fontSizeItem.addActionListener(event -> {
            if (jTabbedPane.getSelectedIndex() == 0) {
                showNoFileSelectedWarning();
                return;
            }

            try {
                int userInput = Integer.parseInt(JOptionPane.showInputDialog(null, "Font size:",
                        "Change Font Size", JOptionPane.INFORMATION_MESSAGE));
                if (userInput > 150) {
                    JOptionPane.showMessageDialog(FRAME, "Font size can't be higher than 150!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Font font = SELECTED_TEXT_AREA.get().getFont();
                SELECTED_TEXT_AREA.get().setFont(new Font(font.getFontName(), font.getStyle(), userInput));
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(FRAME, "Font size needs to be a number!", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        fileMenu.add(newFileItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        editMenu.add(fontSizeItem);

        jMenuBar.add(fileMenu);
        jMenuBar.add(editMenu);

        Editor.FRAME.setJMenuBar(jMenuBar);

        JLabel textEditor = new JLabel("TEXT EDITOR");
        textEditor.setFont(new Font("Arial", Font.BOLD, 25));


        JButton newFile = new JButton("New File");
        newFile.setBorder(null);
        newFile.setFont(new Font("Arial", Font.PLAIN, 14));
        newFile.setFocusPainted(false);
        newFile.setContentAreaFilled(false);
        newFile.setPreferredSize(new Dimension(64, 32));
        newFile.addActionListener(event -> createNewFile(jTabbedPane));

        JButton openFile = new JButton("Open File");
        openFile.setBorder(null);
        openFile.setFont(new Font("Arial", Font.PLAIN, 14));
        openFile.setFocusPainted(false);
        openFile.setContentAreaFilled(false);
        openFile.setPreferredSize(new Dimension(64, 32));
        openFile.addActionListener(event -> openFile(jTabbedPane));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        jPanel.add(textEditor);
        jPanel.add(new JLabel(""), gridBagConstraints);
        jPanel.add(newFile);
        jPanel.add(new JLabel(""), gridBagConstraints);
        jPanel.add(openFile);

        jTabbedPane.addTab("Home", jPanel);
        jTabbedPane.setTabComponentAt(0, null);

        jTabbedPane.setUI(new BasicTabbedPaneUI() {
            private final Insets borderInsets = new Insets(2, 0, 0, 0);

            @Override
            protected Insets getContentBorderInsets(int tabPlacement) {
                return borderInsets;
            }

            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int runCount, int maxTabHeight) {
                return jTabbedPane.getTabCount() > 1 ?
                        super.calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) : -1;
            }
        });

    }

    private static void saveFile(JTabbedPane jTabbedPane) {
        if (jTabbedPane.getSelectedIndex() == 0) {
            showNoFileSelectedWarning();
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int userResponse = fileChooser.showSaveDialog(null);

        if (userResponse == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
            try (PrintWriter fileOut = new PrintWriter(file)) {
                fileOut.println(SELECTED_TEXT_AREA.get().getText());
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(
                        FRAME, "Something went wrong while saving the file!", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static void openFile(JTabbedPane jTabbedPane) {
        JFileChooser jFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files",
                "txt", "java", "kt", "js", "csv", "css", "cs", "cpp", "c", "go",
                "html", "json", "lua", "php");
        jFileChooser.setFileFilter(filter);

        int userResponse = jFileChooser.showOpenDialog(null);

        if (userResponse == JFileChooser.APPROVE_OPTION) {

            File selectedFile = new File(jFileChooser.getSelectedFile().getAbsolutePath());
            RSyntaxTextArea jTextArea = constructNewArea(jTabbedPane, selectedFile.getName());

            if (jTextArea == null) {
                return;
            }

            try (Scanner fileIn = new Scanner(selectedFile)) {
                if (selectedFile.isFile()) {
                    while (fileIn.hasNextLine()) {
                        String line = fileIn.nextLine() + "\n";
                        jTextArea.append(line);
                    }
                }
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(FRAME,
                        "Something went wrong while reading the file's contents!", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void closeSelectedFile(JTabbedPane jTabbedPane) {
        jTabbedPane.remove(jTabbedPane.getSelectedIndex());
    }

    private static RSyntaxTextArea constructNewArea(JTabbedPane jTabbedPane, String name) {

        for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
            Component component = jTabbedPane.getComponentAt(i);
            if (component.getName() != null && component.getName().equals(name)) {
                JOptionPane.showMessageDialog(FRAME, "File with this name is already opened!", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        JLabel jLabel = new JLabel(name);
        RSyntaxTextArea syntaxTextArea = new RSyntaxTextArea();
        RTextScrollPane scrollPane = new RTextScrollPane(syntaxTextArea);

        syntaxTextArea.getDocument().addUndoableEditListener(new UndoManager());
        syntaxTextArea.setBorder(null);
        syntaxTextArea.setCodeFoldingEnabled(true);

        try {
            Theme.load(Main.class.getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml")).apply(syntaxTextArea);
        } catch (IOException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(FRAME,
                    "Something went wrong while updating the theme!", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

        setSyntaxEditingStyleByExtension(syntaxTextArea, name);

        scrollPane.setPreferredSize(new Dimension(450, 450));
        scrollPane.setName(name);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(450, 450));
        scrollPane.setBorder(null);
        scrollPane.setLineNumbersEnabled(true);

        jTabbedPane.addTab(name, scrollPane);
        jTabbedPane.setTabComponentAt(jTabbedPane.getTabCount() - 1, jLabel);
        jTabbedPane.setSelectedIndex(jTabbedPane.getTabCount() - 1);

        Color color = new Color(60, 63, 65, 255);
        syntaxTextArea.setBackground(color);

        SELECTED_TEXT_AREA.set(syntaxTextArea);
        return syntaxTextArea;
    }

    private static void createNewFile(JTabbedPane jTabbedPane) {
        String name = JOptionPane.showInputDialog(null, "File name:",
                "Create New File", JOptionPane.INFORMATION_MESSAGE);

        if (name != null && name.contains(".")) {
            constructNewArea(jTabbedPane, name);
        } else {
            JOptionPane.showMessageDialog(FRAME, "You need to specify a file extension!", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void showNoFileSelectedWarning() {
        JOptionPane.showMessageDialog(FRAME, "You need to have a text file selected!", "Warning",
                JOptionPane.WARNING_MESSAGE);
    }

    private static void setSyntaxEditingStyleByExtension(RSyntaxTextArea syntaxTextArea, String fileName) {
        switch (fileName.split("\\.")[1]) {
            case "java" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            case "kt" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_KOTLIN);
            case "js" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            case "csv" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSV);
            case "css" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
            case "cs" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
            case "cpp" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
            case "c" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
            case "go" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
            case "html" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            case "json" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            case "lua" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
            case "php" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
            case "py" -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
            default -> syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }

}
