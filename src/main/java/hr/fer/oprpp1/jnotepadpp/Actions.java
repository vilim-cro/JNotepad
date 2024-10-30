package hr.fer.oprpp1.jnotepadpp;

import hr.fer.oprpp1.jnotepadpp.documentmodel.DefaultMultipleDocumentModel;
import hr.fer.oprpp1.jnotepadpp.documentmodel.SingleDocumentModel;
import hr.fer.oprpp1.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.oprpp1.jnotepadpp.local.LocalizationProvider;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serial;
import java.nio.file.Path;
import java.text.Collator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Actions {
    private final DefaultMultipleDocumentModel tabbedPane;
    private final JFrame window;
    private final FormLocalizationProvider flp;
    private JTextArea textArea;
    private int start;
    private int len;

    public Actions(JNotepadPP window, DefaultMultipleDocumentModel tabbedPane) {
        this.window = window;
        this.flp = window.getFlp();
        this.tabbedPane = tabbedPane;
        createActions();
    }

    public Action NEW = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tabbedPane.createNewDocument();
        }
    };

    public Action OPEN = new AbstractAction() {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Open file");
            if(fc.showOpenDialog(tabbedPane) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File fileName = fc.getSelectedFile();
            Path filePath = fileName.toPath();
            tabbedPane.loadDocument(filePath);
        }
    };

    public Action SAVE = new AbstractAction() {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            tabbedPane.saveDocument(tabbedPane.getCurrentDocument(), tabbedPane.getCurrentDocument().getFilePath());
        }
    };

    public Action SAVE_AS = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save file");
            if(fc.showSaveDialog(tabbedPane) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File fileName = fc.getSelectedFile();
            Path filePath = fileName.toPath();
            if (filePath.toFile().exists()) {
                int result = JOptionPane.showConfirmDialog(
                        tabbedPane,
                        flp.getString("file") + " " + filePath.toFile().getAbsolutePath() + " " + flp.getString("already_exists") + "?",
                        "Save",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return;
            }
            SingleDocumentModel oldModel = tabbedPane.getCurrentDocument();
            SingleDocumentModel model = tabbedPane.createNewDocument();
            tabbedPane.initializeDocument(model, filePath, oldModel.getTextComponent().getText());
            tabbedPane.saveDocument(model, filePath);
        }
    };

    public Action CUT = new DefaultEditorKit.CutAction();
    public Action COPY = new DefaultEditorKit.CopyAction();
    public Action PASTE = new DefaultEditorKit.PasteAction();

    public Action CLOSE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            closeDocument();
        }
    };

    public Action ENGLISH = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("en");
        }
    };

    public Action CROATIAN = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("hr");
        }
    };

    public Action DANISH = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("da");
        }
    };

    public Action TO_UPPERCASE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = getText().toUpperCase();
            textArea.replaceRange(text, start, start + len);
        }
    };

    public Action TO_LOWERCASE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = getText().toLowerCase();
            textArea.replaceRange(text, start, start + len);
        }
    };

    public Action INVERT_CASE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            char[] chars = getText().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLowerCase(chars[i])) {
                    chars[i] = Character.toUpperCase(chars[i]);
                } else if (Character.isUpperCase(chars[i])) {
                    chars[i] = Character.toLowerCase(chars[i]);
                }
            }
            String text = new String(chars);
            textArea.replaceRange(text, start, start + len);
        }
    };

    public Action SORT_ASCENDING = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sort(true);
        }
    };

    public Action SORT_DESCENDING = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sort(false);
        }
    };

    public Action UNIQUE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = getWholeLineText();
            String[] lines = text.split("\n");
            Set<String> usedLines = new HashSet<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (!usedLines.contains(lines[i])) {
                    usedLines.add(lines[i]);
                    sb.append(lines[i]).append("\n");
                }
            }
            textArea.replaceRange(sb.toString(), start, start + len);
        }
    };

    public Action STATISTICS = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea textArea = tabbedPane.getCurrentDocument().getTextComponent();
            int numberOfCharacters = textArea.getText().replaceAll("\n", "").length();
            int numberOfNonBlankCharacters = textArea.getText().replaceAll("\\s+", "").length();
            int numberOfLines = textArea.getLineCount();
            JOptionPane.showMessageDialog(
                    tabbedPane,
                    flp.getString("document_has") + " " + numberOfCharacters +
                            " " + flp.getString("characters") + ", " +
                            numberOfNonBlankCharacters + " " + flp.getString("non_blank") +
                            " " + numberOfLines + " " + flp.getString("lines") + ".",
                    "Statistics",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    };

    public Action EXIT = new AbstractAction() {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            while(tabbedPane.getNumberOfDocuments() != 0) {
                if(!closeDocument()) return;
            }
            window.dispose();
        }
    };

    private void createActions() {
        NEW.putValue(
                Action.NAME,
                flp.getString("create"));
        NEW.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control N"));
        NEW.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("create_description"));

        OPEN.putValue(
                Action.NAME,
                flp.getString("open"));
        OPEN.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control O"));
        OPEN.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("open_description"));

        SAVE.putValue(
                Action.NAME,
                flp.getString("save"));
        SAVE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control S"));
        SAVE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("save_description"));

        SAVE_AS.putValue(
                Action.NAME,
                flp.getString("save_as"));
        SAVE_AS.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt S"));
        SAVE_AS.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("save_as_description"));

        CUT.putValue(
                Action.NAME,
                flp.getString("cut"));
        CUT.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control X"));
        CUT.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("cut_description"));

        COPY.putValue(
                Action.NAME,
                flp.getString("copy"));
        COPY.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control C"));
        COPY.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("copy_description"));

        PASTE.putValue(
                Action.NAME,
                flp.getString("paste"));
        PASTE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control V"));
        PASTE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("paste_description"));

        CLOSE.putValue(
                Action.NAME,
                flp.getString("close"));
        CLOSE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control W"));
        CLOSE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("close_description"));

        ENGLISH.putValue(
                Action.NAME,
                flp.getString("english"));
        ENGLISH.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control E"));
        ENGLISH.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("english_description"));

        CROATIAN.putValue(
                Action.NAME,
                flp.getString("croatian"));
        CROATIAN.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt H"));
        CROATIAN.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("croatian_description"));

        DANISH.putValue(
                Action.NAME,
                flp.getString("danish"));
        DANISH.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control D"));
        DANISH.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("danish_description"));

        TO_UPPERCASE.putValue(
                Action.NAME,
                flp.getString("to_uppercase"));
        TO_UPPERCASE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control U"));
        TO_UPPERCASE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("to_uppercase_description"));

        TO_LOWERCASE.putValue(
                Action.NAME,
                flp.getString("to_lowercase"));
        TO_LOWERCASE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control L"));
        TO_LOWERCASE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("to_lowercase_description"));

        INVERT_CASE.putValue(
                Action.NAME,
                flp.getString("invert_case"));
        INVERT_CASE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt I"));
        INVERT_CASE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("invert_case_description"));

        SORT_ASCENDING.putValue(
                Action.NAME,
                flp.getString("sort_ascending"));
        SORT_ASCENDING.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt A"));
        SORT_ASCENDING.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("sort_ascending_description"));

        SORT_DESCENDING.putValue(
                Action.NAME,
                flp.getString("sort_descending"));
        SORT_DESCENDING.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt D"));
        SORT_DESCENDING.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("sort_descending_description"));

        UNIQUE.putValue(
                Action.NAME,
                flp.getString("unique"));
        UNIQUE.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control alt U"));
        UNIQUE.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("unique_description"));

        STATISTICS.putValue(
                Action.NAME,
                flp.getString("statistics"));
        STATISTICS.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control I"));
        STATISTICS.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("statistics_description"));

        EXIT.putValue(
                Action.NAME,
                flp.getString("exit"));
        EXIT.putValue(
                Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control Q"));
        EXIT.putValue(
                Action.SHORT_DESCRIPTION,
                flp.getString("exit_description"));

        flp.addLocalizationListener(() -> {
            NEW.putValue(Action.NAME, flp.getString("create"));
            NEW.putValue(Action.SHORT_DESCRIPTION, flp.getString("create_description"));
            OPEN.putValue(Action.NAME, flp.getString("open"));
            OPEN.putValue(Action.SHORT_DESCRIPTION, flp.getString("open_description"));
            SAVE.putValue(Action.NAME, flp.getString("save"));
            SAVE.putValue(Action.SHORT_DESCRIPTION, flp.getString("save_description"));
            SAVE_AS.putValue(Action.NAME, flp.getString("save_as"));
            SAVE_AS.putValue(Action.SHORT_DESCRIPTION, flp.getString("save_as_description"));
            CUT.putValue(Action.NAME, flp.getString("cut"));
            CUT.putValue(Action.SHORT_DESCRIPTION, flp.getString("cut_description"));
            COPY.putValue(Action.NAME, flp.getString("copy"));
            COPY.putValue(Action.SHORT_DESCRIPTION, flp.getString("copy_description"));
            PASTE.putValue(Action.NAME, flp.getString("paste"));
            PASTE.putValue(Action.SHORT_DESCRIPTION, flp.getString("paste_description"));
            CLOSE.putValue(Action.NAME, flp.getString("close"));
            CLOSE.putValue(Action.SHORT_DESCRIPTION, flp.getString("close_description"));
            ENGLISH.putValue(Action.NAME, flp.getString("english"));
            ENGLISH.putValue(Action.SHORT_DESCRIPTION, flp.getString("english_description"));
            CROATIAN.putValue(Action.NAME, flp.getString("croatian"));
            CROATIAN.putValue(Action.SHORT_DESCRIPTION, flp.getString("croatian_description"));
            DANISH.putValue(Action.NAME, flp.getString("danish"));
            DANISH.putValue(Action.SHORT_DESCRIPTION, flp.getString("danish_description"));
            TO_UPPERCASE.putValue(Action.NAME, flp.getString("to_uppercase"));
            TO_UPPERCASE.putValue(Action.SHORT_DESCRIPTION, flp.getString("to_uppercase_description"));
            TO_LOWERCASE.putValue(Action.NAME, flp.getString("to_lowercase"));
            TO_LOWERCASE.putValue(Action.SHORT_DESCRIPTION, flp.getString("to_lowercase_description"));
            INVERT_CASE.putValue(Action.NAME, flp.getString("invert_case"));
            INVERT_CASE.putValue(Action.SHORT_DESCRIPTION, flp.getString("invert_case_description"));
            SORT_ASCENDING.putValue(Action.NAME, flp.getString("sort_ascending"));
            SORT_ASCENDING.putValue(Action.SHORT_DESCRIPTION, flp.getString("sort_ascending_description"));
            SORT_DESCENDING.putValue(Action.NAME, flp.getString("sort_descending"));
            SORT_DESCENDING.putValue(Action.SHORT_DESCRIPTION, flp.getString("sort_descending_description"));
            UNIQUE.putValue(Action.NAME, flp.getString("unique"));
            UNIQUE.putValue(Action.SHORT_DESCRIPTION, flp.getString("unique_description"));
            STATISTICS.putValue(Action.NAME, flp.getString("statistics"));
            STATISTICS.putValue(Action.SHORT_DESCRIPTION, flp.getString("statistics_description"));
            EXIT.putValue(Action.NAME, flp.getString("exit"));
            EXIT.putValue(Action.SHORT_DESCRIPTION, flp.getString("exit_description"));
        });
    }

    private boolean closeDocument() {
        SingleDocumentModel model = tabbedPane.getCurrentDocument();
        if (model.isModified()) {
            int result = JOptionPane.showConfirmDialog(
                    tabbedPane,
                    flp.getString("save_changes") + " " + (model.getFilePath() == null ? "(unnamed)" : model.getFilePath().toFile().getAbsolutePath()) + "?",
                    "Save",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                tabbedPane.saveDocument(model, model.getFilePath());
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        tabbedPane.closeDocument(model);
        return true;
    }

    public String getText() {
        textArea = tabbedPane.getCurrentDocument().getTextComponent();
        start = Math.min(textArea.getCaret().getDot(), textArea.getCaret().getMark());
        len = Math.abs(textArea.getCaret().getDot() - textArea.getCaret().getMark());
        return textArea.getText().substring(start, start + len);
    }

    public String getWholeLineText() {
        textArea = tabbedPane.getCurrentDocument().getTextComponent();
        Document doc = textArea.getDocument();
        Element root = doc.getDefaultRootElement();
        int startLine = root.getElementIndex(textArea.getSelectionStart());
        int endLine = root.getElementIndex(textArea.getSelectionEnd());
        try {
            int lineStartOffset = root.getElement(startLine).getStartOffset();
            int lineEndOffset = root.getElement(endLine).getEndOffset();
            start = lineStartOffset;
            len = lineEndOffset - lineStartOffset - 1;
            return doc.getText(lineStartOffset, len);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sort(boolean ascending) {
        String text = getWholeLineText();
        String[] lines = text.split("\n");
        Locale locale = Locale.forLanguageTag(flp.getLanguage());
        Collator collator = Collator.getInstance(locale);
        for (int i = 0; i < lines.length; i++) {
            for (int j = i + 1; j < lines.length; j++) {
                if (collator.compare(lines[i], lines[j]) > 0 && ascending ||
                        collator.compare(lines[i], lines[j]) < 0 && !ascending) {
                    String temp = lines[i];
                    lines[i] = lines[j];
                    lines[j] = temp;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        textArea.replaceRange(sb.toString(), start, start + len);
    }
}
