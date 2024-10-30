package hr.fer.oprpp1.jnotepadpp;

import hr.fer.oprpp1.jnotepadpp.documentmodel.DefaultMultipleDocumentModel;
import hr.fer.oprpp1.jnotepadpp.documentmodel.MultipleDocumentListener;
import hr.fer.oprpp1.jnotepadpp.documentmodel.SingleDocumentModel;
import hr.fer.oprpp1.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.oprpp1.jnotepadpp.local.LocalizableAction;
import hr.fer.oprpp1.jnotepadpp.local.LocalizationProvider;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class JNotepadPP extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private DefaultMultipleDocumentModel tabbedPane;
    private Actions actions;
    private JLabel lengthLabel;
    private JLabel lnColSelLabel;
    private JLabel clockLabel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final FormLocalizationProvider flp;

    public JNotepadPP() {
        flp = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(600, 600);
        initGUI();
        setLocationRelativeTo(null);
        setTitle("(unnamed) - JNotepad++");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actions.EXIT.actionPerformed(null);
            }
        });
    }

    private void initGUI() {
        tabbedPane = new DefaultMultipleDocumentModel(this);

        tabbedPane.createNewDocument();
        actions = new Actions(this, tabbedPane);
        createMenus();
        JToolBar toolBar = createToolbars();
        JPanel statusBar = createStatusBar();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(toolBar, BorderLayout.PAGE_START);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.PAGE_END);

        disableChangeCase();
        disableSort();

        tabbedPane.addMultipleDocumentListener(new MultipleDocumentListener() {
            @Override
            public void currentDocumentChanged(SingleDocumentModel previousModel, SingleDocumentModel currentModel) {
                changeTitle(currentModel);
                updateLength();
            }

            @Override
            public void documentAdded(SingleDocumentModel model) {
                currentDocumentChanged(null, model);
                updateLength();
            }

            @Override
            public void documentRemoved(SingleDocumentModel model) {
                changeTitle(tabbedPane.getCurrentDocument());
            }

            private void changeTitle(SingleDocumentModel model) {
                if (model == null) {
                    setTitle("JNotepad++");
                } else {
                    setTitle((model.getFilePath() == null ? "(unnamed)" : model.getFilePath().toFile().getAbsolutePath()) + " - JNotepad++");
                }            }
        });
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new GridLayout(1, 3));
        statusBar.setBorder(new EmptyBorder(5, 20, 5, 20));
        lengthLabel = new JLabel(flp.getString("length") + ": 0");
        String ln = flp.getString("ln");
        String col = flp.getString("col");
        String sel = flp.getString("sel");
        lnColSelLabel = new JLabel(String.format("%s: 1  %s: 1  %s: 0", ln, col, sel));
        flp.addLocalizationListener(this::updateStatusBar);

        clockLabel = new JLabel("", SwingConstants.RIGHT);
        updateTime();
        statusBar.add(lengthLabel);
        statusBar.add(lnColSelLabel);
        statusBar.add(clockLabel);
        getContentPane().add(statusBar, BorderLayout.PAGE_END);

        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
        return statusBar;
    }

    private void updateStatusBar() {
        String num = lengthLabel.getText().split(" ")[1];
        lengthLabel.setText(flp.getString("length") + ": " + num);

        String[] parts = lnColSelLabel.getText().split("  ");
        String ln = flp.getString("ln");
        String col = flp.getString("col");
        String sel = flp.getString("sel");
        String ln_num = parts[0].split(" ")[1];
        String col_num = parts[1].split(" ")[1];
        String sel_num = parts[2].split(" ")[1];
        lnColSelLabel.setText(String.format("%s: %s  %s: %s  %s: %s", ln, ln_num, col, col_num, sel, sel_num));
    }

    private void updateTime() {
        clockLabel.setText(java.time.LocalDateTime.now().format(formatter));
    }

    public void updateLength() {
        String length = flp.getString("length");
        String ln = flp.getString("ln");
        String col = flp.getString("col");
        String sel = flp.getString("sel");
        JTextArea textArea = tabbedPane.getCurrentDocument().getTextComponent();
        lengthLabel.setText(length + ": " + textArea.getText().length());
        int caretPosition = textArea.getCaretPosition();
        int line = textArea.getDocument().getDefaultRootElement().getElementIndex(caretPosition) + 1;
        int column = caretPosition - textArea.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset() + 1;
        lnColSelLabel.setText(ln + ": " + line + "  " + col + ": " + column
                + "  " + sel + ": " + Math.abs(textArea.getCaret().getDot()
                - textArea.getCaret().getMark()));
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(new LocalizableAction("file", flp) {});
        menuBar.add(fileMenu);

        fileMenu.add(new JMenuItem(actions.NEW));
        fileMenu.add(new JMenuItem(actions.OPEN));
        fileMenu.add(new JMenuItem(actions.SAVE));
        fileMenu.add(new JMenuItem(actions.SAVE_AS));
        fileMenu.add(new JMenuItem(actions.CLOSE));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(actions.EXIT));

        JMenu editMenu = new JMenu(new LocalizableAction("edit", flp) {});
        menuBar.add(editMenu);

        editMenu.add(new JMenuItem(actions.CUT));
        editMenu.add(new JMenuItem(actions.COPY));
        editMenu.add(new JMenuItem(actions.PASTE));

        JMenu infoMenu = new JMenu(new LocalizableAction("info", flp) {});
        menuBar.add(infoMenu);

        infoMenu.add(new JMenuItem(actions.STATISTICS));

        JMenu languagesMenu = new JMenu(new LocalizableAction("languages", flp) {});
        menuBar.add(languagesMenu);

        languagesMenu.add(new JMenuItem(actions.ENGLISH));
        languagesMenu.add(new JMenuItem(actions.CROATIAN));
        languagesMenu.add(new JMenuItem(actions.DANISH));

        JMenu toolsMenu = new JMenu(new LocalizableAction("tools", flp) {});
        menuBar.add(toolsMenu);

        JMenu changeCaseMenu = new JMenu(new LocalizableAction("change_case", flp) {});
        toolsMenu.add(changeCaseMenu);

        changeCaseMenu.add(new JMenuItem(actions.TO_UPPERCASE));
        changeCaseMenu.add(new JMenuItem(actions.TO_LOWERCASE));
        changeCaseMenu.add(new JMenuItem(actions.INVERT_CASE));

        JMenu sortMenu = new JMenu(new LocalizableAction("sort", flp) {});
        toolsMenu.add(sortMenu);

        sortMenu.add(new JMenuItem(actions.SORT_ASCENDING));
        sortMenu.add(new JMenuItem(actions.SORT_DESCENDING));

        JMenuItem uniqueMenu = new JMenuItem(actions.UNIQUE);
        toolsMenu.add(uniqueMenu);

        setJMenuBar(menuBar);
    }

    private JToolBar createToolbars() {
        JToolBar toolBar = new JToolBar("Tools");
        toolBar.setFloatable(true);

        toolBar.add(new JButton(actions.NEW));
        toolBar.add(new JButton(actions.OPEN));
        toolBar.add(new JButton(actions.SAVE));
        toolBar.add(new JButton(actions.SAVE_AS));
        toolBar.add(new JButton(actions.CLOSE));
        toolBar.addSeparator();

        toolBar.add(new JButton(actions.CUT));
        toolBar.add(new JButton(actions.COPY));
        toolBar.add(new JButton(actions.PASTE));
        toolBar.addSeparator();

        toolBar.add(new JButton(actions.STATISTICS));
        toolBar.addSeparator();

        toolBar.add(new JButton(actions.EXIT));
        return toolBar;
    }

    public FormLocalizationProvider getFlp() {
        return flp;
    }

    public void enableChangeCase() {
        actions.TO_UPPERCASE.setEnabled(true);
        actions.TO_LOWERCASE.setEnabled(true);
        actions.INVERT_CASE.setEnabled(true);
    }

    public void disableChangeCase() {
        actions.TO_UPPERCASE.setEnabled(false);
        actions.TO_LOWERCASE.setEnabled(false);
        actions.INVERT_CASE.setEnabled(false);
    }

    public void enableSort() {
        actions.SORT_ASCENDING.setEnabled(true);
        actions.SORT_DESCENDING.setEnabled(true);
    }

    public void disableSort() {
        actions.SORT_ASCENDING.setEnabled(false);
        actions.SORT_DESCENDING.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JNotepadPP().setVisible(true));
    }

}