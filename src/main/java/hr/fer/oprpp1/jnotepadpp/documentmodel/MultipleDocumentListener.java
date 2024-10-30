package hr.fer.oprpp1.jnotepadpp.documentmodel;

 public interface MultipleDocumentListener {
    void currentDocumentChanged(SingleDocumentModel previousModel,
                                SingleDocumentModel currentModel);
    void documentAdded(SingleDocumentModel model);
    void documentRemoved(SingleDocumentModel model);
}
