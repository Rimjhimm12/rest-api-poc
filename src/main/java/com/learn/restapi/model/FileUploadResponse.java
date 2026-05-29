package com.learn.restapi.model;

/*
 * Response body returned after a successful file upload.
 *
 * Used as:
 *   - the top-level response for  POST /api/files/upload  (single file)
 *   - an item inside the list for POST /api/files/upload-multiple
 *
 * No validation annotations are needed here because this class is
 * never used as a @RequestBody — it is only ever written as a response.
 */
public class FileUploadResponse {

    private String fileName;    // original file name as it existed on the client machine
    private String fileType;    // MIME type e.g. "image/jpeg", "application/pdf"
    private long   size;        // file size in bytes
    private String description; // optional text label sent by the caller (single-upload only)
    private String message;     // human-readable outcome

    public FileUploadResponse() {}

    public FileUploadResponse(String fileName, String fileType, long size,
                              String description, String message) {
        this.fileName    = fileName;
        this.fileType    = fileType;
        this.size        = size;
        this.description = description;
        this.message     = message;
    }

    public String getFileName()    { return fileName; }
    public void   setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType()    { return fileType; }
    public void   setFileType(String fileType) { this.fileType = fileType; }

    public long   getSize()        { return size; }
    public void   setSize(long size) { this.size = size; }

    public String getDescription() { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getMessage()     { return message; }
    public void   setMessage(String message) { this.message = message; }
}
