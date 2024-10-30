package hr.fer.oprpp1.jnotepadpp.documentmodel;

import hr.fer.oprpp1.jnotepadpp.JNotepadPP;
import hr.fer.oprpp1.jnotepadpp.local.FormLocalizationProvider;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class DefaultMultipleDocumentModel extends JTabbedPane implements MultipleDocumentModel {
    private final List<SingleDocumentModel> models = new ArrayList<>();
    private SingleDocumentModel currentModel;
    private final Map<Path, SingleDocumentModel> pathToModel = new HashMap<>();
    private final List<MultipleDocumentListener> listeners = new ArrayList<>();
    private final JNotepadPP frame;
    private FormLocalizationProvider flp;

    public DefaultMultipleDocumentModel(JNotepadPP frame) {
        this.frame = frame;
        this.flp = frame.getFlp();
        addChangeListener(e -> {
            if (getSelectedIndex() == -1) {
                currentModel = null;
                return;
            }
            currentModel = models.get(getSelectedIndex());
            for (MultipleDocumentListener l : listeners) {
                l.currentDocumentChanged(null, currentModel);
            }
        });
    }

    @Override
    public JComponent getVisualComponent() {
        return this;
    }

    @Override
    public SingleDocumentModel createNewDocument() {
        DefaultSingleDocumentModel model = new DefaultSingleDocumentModel(null, "");
        models.add(model);
        addTab("(unnamed)", new JScrollPane(model.getTextComponent()));
        setSelectedIndex(getIndexOfDocument(model));
        currentModel = model;
        model.setModified(false);
        for (MultipleDocumentListener l : listeners) {
            l.documentAdded(model);
        }

        model.addSingleDocumentListener(new SingleDocumentListener() {
            @Override
            public void documentModifyStatusUpdated(SingleDocumentModel model) {
                frame.updateLength();
                setIconAt(getIndexOfDocument(model), model.isModified() ?
                        loadImage("red_icon.jpg") :
                        loadImage("green_icon.jpg"));
            }

            @Override
            public void documentFilePathUpdated(SingleDocumentModel model) {
                setTitleAt(getIndexOfDocument(model), model.getFilePath().getFileName().toString());
                setToolTipTextAt(getIndexOfDocument(model), model.getFilePath().toFile().getAbsolutePath());
            }
        });

        model.getTextComponent().addCaretListener(e -> {
            frame.updateLength();

            Caret caret = model.getTextComponent().getCaret();
            if (caret.getDot() == caret.getMark()) {
                frame.disableChangeCase();
                frame.disableSort();
            } else {
                frame.enableChangeCase();
                frame.enableSort();
            }
        });

        setIconAt(getIndexOfDocument(model), loadImage("green_icon.jpg"));
        setToolTipTextAt(getIndexOfDocument(model), "(unnamed)");
        for (MultipleDocumentListener l : listeners) {
            l.documentAdded(model);
        }
        return model;
    }

    public void initializeDocument(SingleDocumentModel model, Path filePath, String text) {
        model.setFilePath(filePath);
        model.getTextComponent().setText(text);
        setTitleAt(getSelectedIndex(), filePath.getFileName().toString());
        pathToModel.put(filePath, model);
        setIconAt(getIndexOfDocument(model), loadImage("green_icon.jpg"));
        for (MultipleDocumentListener l : listeners) {
            l.documentAdded(model);
        }
        model.setModified(false);
    }

    @Override
    public SingleDocumentModel getCurrentDocument() {
        return currentModel;
    }

    @Override
    public SingleDocumentModel loadDocument(Path filePath) {
        if (filePath == null) throw new NullPointerException("Path cannot be null!");

        if (pathToModel.containsKey(filePath)) {
            setSelectedIndex(getIndexOfDocument(pathToModel.get(filePath)));
            return pathToModel.get(filePath);
        }

        if(!Files.isReadable(filePath)) {
            JOptionPane.showMessageDialog(
                    DefaultMultipleDocumentModel.this,
                    flp.getString("file") + " " + filePath + flp.getString("doesnt_exist") + "!",
                    flp.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        byte[] okteti;
        try {
            okteti = Files.readAllBytes(filePath);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(
                    DefaultMultipleDocumentModel.this,
                    flp.getString("error_while") + " " + filePath + ".",
                    flp.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String tekst = new String(okteti, StandardCharsets.UTF_8);

        SingleDocumentModel model = createNewDocument();
        initializeDocument(model, filePath, tekst);
        return model;
    }

    @Override
    public void saveDocument(SingleDocumentModel model, Path newPath) {
        if(newPath == null) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Save document");
            if(jfc.showSaveDialog(DefaultMultipleDocumentModel.this) != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(
                        DefaultMultipleDocumentModel.this,
                        flp.getString("nothing_saved"),
                        flp.getString("warning"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            newPath = jfc.getSelectedFile().toPath();
        }
        try {
            Files.writeString(newPath, model.getTextComponent().getText());
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(
                    DefaultMultipleDocumentModel.this,
                    flp.getString("error_while_writing") + " " + newPath.toFile().getAbsolutePath()+".\n" + flp.getString("unstable_state") + "!",
                    flp.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        setTitleAt(getSelectedIndex(), newPath.getFileName().toString());
        setToolTipTextAt(getSelectedIndex(), newPath.toFile().getAbsolutePath());
        model.setFilePath(newPath);
        pathToModel.put(newPath, model);
        model.setModified(false);
        for (MultipleDocumentListener l : listeners) {
            l.currentDocumentChanged(null, model);
        }
        JOptionPane.showMessageDialog(
                DefaultMultipleDocumentModel.this,
                flp.getString("file_saved"),
                flp.getString("info"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void closeDocument(SingleDocumentModel model) {
        removeTabAt(getIndexOfDocument(model));
        models.remove(model);
        pathToModel.remove(model.getFilePath());
        for (MultipleDocumentListener l : listeners) {
            l.documentRemoved(model);
        }
        currentModel = getDocument(getSelectedIndex());
    }

    @Override
    public void addMultipleDocumentListener(MultipleDocumentListener l) {
        listeners.add(l);
    }

    @Override
    public void removeMultipleDocumentListener(MultipleDocumentListener l) {
        listeners.remove(l);
    }

    @Override
    public int getNumberOfDocuments() {
        return models.size();
    }

    @Override
    public SingleDocumentModel getDocument(int index) {
        if(index < 0) return null;
        return models.get(index);
    }

    @Override
    public SingleDocumentModel findForPath(Path path) {
        if (path == null) throw new NullPointerException("Path cannot be null!");
        return pathToModel.getOrDefault(path, null);
    }

    @Override
    public int getIndexOfDocument(SingleDocumentModel doc) {
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).equals(doc)) return i;
        }
        return -1;
    }

    @Override
    public Iterator<SingleDocumentModel> iterator() {
        return models.iterator();
    }

    public ImageIcon loadImage(String name) {
        byte[] bytes;
        try (InputStream is = this.getClass().getResourceAsStream("/hr/fer/oprpp1/hw08/jnotepadpp/icons/" + name)) {
            if (is == null) {
                throw new IllegalArgumentException("Cannot find icon with name " + name + "!");
            }
            bytes = is.readAllBytes();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read icon with name " + name + "!");
        }
        ImageIcon icon = new ImageIcon(bytes);
        Image resized = icon.getImage().getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }
}
