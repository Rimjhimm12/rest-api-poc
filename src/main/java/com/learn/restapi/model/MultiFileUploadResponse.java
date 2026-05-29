package com.learn.restapi.model;

import java.util.List;

/*
 * Response body returned after a successful multi-file upload.
 *
 * Returned by POST /api/files/upload-multiple.
 * Each item in the "files" list is a FileUploadResponse describing one uploaded file.
 */
public class MultiFileUploadResponse {

    private int                    uploadedCount; // how many files were received
    private String                 category;      // optional batch label sent by the caller
    private List<FileUploadResponse> files;       // one entry per uploaded file
    private String                 message;       // human-readable outcome

    public MultiFileUploadResponse() {}

    public MultiFileUploadResponse(int uploadedCount, String category,
                                   List<FileUploadResponse> files, String message) {
        this.uploadedCount = uploadedCount;
        this.category      = category;
        this.files         = files;
        this.message       = message;
    }

    public int                      getUploadedCount() { return uploadedCount; }
    public void                     setUploadedCount(int uploadedCount) { this.uploadedCount = uploadedCount; }

    public String                   getCategory()      { return category; }
    public void                     setCategory(String category) { this.category = category; }

    public List<FileUploadResponse> getFiles()         { return files; }
    public void                     setFiles(List<FileUploadResponse> files) { this.files = files; }

    public String                   getMessage()       { return message; }
    public void                     setMessage(String message) { this.message = message; }
}
