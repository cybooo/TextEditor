package dev.cybo.texteditor.editor;

import dev.cybo.texteditor.Main;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

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
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Editor {

    private static final JFrame FRAME = new JFrame();
    private static final AtomicReference<JTextArea> SELECTED_TEXT_AREA = new AtomicReference<>();

    public Editor() {

        FRAME.setTitle("Text Editor");
        FRAME.setSize(1200, 800);
        FRAME.setLocationRelativeTo(null);
        FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.setFocusable(false);
        jTabbedPane.setSize(1000, 20);

        jTabbedPane.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isMiddleMouseButton(e)) {
                    if (jTabbedPane.getSelectedIndex() != 0) {
                        jTabbedPane.remove(jTabbedPane.getSelectedIndex());
                    }
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    if (jTabbedPane.getSelectedIndex() != 0) {

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
                        jPopupMenu.show(FRAME, e.getX(), e.getY() + 50);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        addDefaultButton(jTabbedPane);

        FRAME.add(jTabbedPane);
        FRAME.setVisible(true);
    }

    private static void addDefaultButton(JTabbedPane jTabbedPane) {

        JMenuBar jMenuBar = new JMenuBar();
        JMenu fileButton = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem exit = new JMenuItem("Exit");

        newFile.addActionListener(e -> createNewFile(jTabbedPane));
        open.addActionListener(e -> openFile(jTabbedPane));
        exit.addActionListener(e -> System.exit(0));
        save.addActionListener(e -> saveFile(jTabbedPane));

        fileButton.add(newFile);
        fileButton.add(open);
        fileButton.add(save);
        fileButton.add(exit);

        jMenuBar.add(fileButton);

        Editor.FRAME.setJMenuBar(jMenuBar);

        JLabel textEditor = new JLabel("TEXT EDITOR");
        textEditor.setFont(new Font("Arial", Font.BOLD, 25));


        JButton newFile1 = new JButton("New File");
        newFile1.setBorder(null);
        newFile1.setFont(new Font("Arial", Font.PLAIN, 14));
        newFile1.setFocusPainted(false);
        newFile1.setContentAreaFilled(false);
        newFile1.setPreferredSize(new Dimension(64, 32));
        newFile1.addActionListener(e -> createNewFile(jTabbedPane));

        JButton openFile = new JButton("Open File");
        openFile.setBorder(null);
        openFile.setFont(new Font("Arial", Font.PLAIN, 14));
        openFile.setFocusPainted(false);
        openFile.setContentAreaFilled(false);
        openFile.setPreferredSize(new Dimension(64, 32));
        openFile.addActionListener(e -> openFile(jTabbedPane));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        jPanel.add(textEditor);
        jPanel.add(new JLabel(""), gridBagConstraints);
        jPanel.add(newFile1);
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
                return jTabbedPane.getTabCount() > 1 ? super.calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) : -1;
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
            }
        }
    }

    private static void openFile(JTabbedPane jTabbedPane) {
        JFileChooser jFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files",
                "txt", "java", "kt", "js", "csv", "css", "cs", "cpp", "c", "go", "html", "json", "lua", "php");
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
            }

        }
    }

    public void closeSelectedFile(JTabbedPane jTabbedPane) {
        jTabbedPane.remove(jTabbedPane.getSelectedIndex());
    }

    private static RSyntaxTextArea constructNewArea(JTabbedPane jTabbedPane, String name) {

        for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
            Component component = jTabbedPane.getComponentAt(i);
            if (component.getName() != null) {
                if (component.getName().equals(name)) {
                    JOptionPane.showMessageDialog(FRAME, "File with this name is already opened!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
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
        } catch (IOException e) {
            e.printStackTrace();
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

        Color color = new Color(60,63,65,255);
        syntaxTextArea.setBackground(color);

        SELECTED_TEXT_AREA.set(syntaxTextArea);
        return syntaxTextArea;
    }

    private static void createNewFile(JTabbedPane jTabbedPane) {
        String name = JOptionPane.showInputDialog(null, "File name:",
                "Create New File", JOptionPane.INFORMATION_MESSAGE);
        if (name != null) {
            if (name.contains(".")) { // Asi není nejlepší způsob kontroly, zda má file nějaký extension - nenapadá mě zatím nic jiného.
                constructNewArea(jTabbedPane, name);
            } else {
                JOptionPane.showMessageDialog(FRAME, "You need to specify a file extension!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static void showNoFileSelectedWarning() {
        JOptionPane.showMessageDialog(FRAME, "You need to have a text file selected!", "Warning", JOptionPane.WARNING_MESSAGE);
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
