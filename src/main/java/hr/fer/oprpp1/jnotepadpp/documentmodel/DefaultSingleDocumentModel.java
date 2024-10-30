package hr.fer.oprpp1.jnotepadpp.documentmodel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefaultSingleDocumentModel implements SingleDocumentModel {
    private boolean textModified = false;
    private final JTextArea textArea;
    private Path filePath;
    private final List<SingleDocumentListener> listeners;
    public DefaultSingleDocumentModel(Path filePath, String textContent) {
        textArea = new JTextArea(textContent);
        this.filePath = filePath;
        textArea.setText(textContent);
        listeners = new ArrayList<>();
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textModified = true;
                for (SingleDocumentListener l : listeners) {
                    l.documentModifyStatusUpdated(DefaultSingleDocumentModel.this);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });
    }

    @Override
    public JTextArea getTextComponent() {
        return textArea;
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(Path path) {
        filePath = path;
        for (SingleDocumentListener l : listeners) {
            l.documentFilePathUpdated(this);
        }
    }

    @Override
    public boolean isModified() {
        return textModified;
    }

    @Override
    public void setModified(boolean modified) {
        textModified = modified;
        for (SingleDocumentListener l : listeners) {
            l.documentModifyStatusUpdated(this);
        }
    }

    @Override
    public void addSingleDocumentListener(SingleDocumentListener l) {
        listeners.add(l);
    }

    @Override
    public void removeSingleDocumentListener(SingleDocumentListener l) {
        listeners.remove(l);
    }
}
