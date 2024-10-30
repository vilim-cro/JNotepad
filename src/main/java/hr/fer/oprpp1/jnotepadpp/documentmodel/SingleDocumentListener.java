package hr.fer.oprpp1.jnotepadpp.documentmodel;

public interface SingleDocumentListener {
    void documentModifyStatusUpdated(SingleDocumentModel model);
    void documentFilePathUpdated(SingleDocumentModel model);
}
