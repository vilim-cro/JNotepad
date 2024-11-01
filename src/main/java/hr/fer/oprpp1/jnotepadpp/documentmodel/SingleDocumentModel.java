package hr.fer.oprpp1.jnotepadpp.documentmodel;

import javax.swing.*;
import java.nio.file.Path;

public interface SingleDocumentModel {
    JTextArea getTextComponent();
    Path getFilePath();
    void setFilePath(Path path);
    boolean isModified();
    void setModified(boolean modified);
    void addSingleDocumentListener(SingleDocumentListener l);
    void removeSingleDocumentListener(SingleDocumentListener l);
}
