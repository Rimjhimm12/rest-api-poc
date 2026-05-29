package com.learn.restapi.controller;

import com.learn.restapi.model.FileUploadResponse;
import com.learn.restapi.model.MultiFileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * SECTION 15 — File Upload Operations
 *
 * Key difference from all other controllers:
 *   - Previous controllers use @RequestBody and Content-Type: application/json
 *   - This controller uses @RequestParam MultipartFile and Content-Type: multipart/form-data
 *
 * In Postman:
 *   - Do NOT use "raw → JSON" in the Body tab
 *   - Use "form-data" and change the Type dropdown to "File" for the file fields
 *
 * Both endpoints are ADMIN-only (enforced in SecurityConfig).
 * No files are stored to disk — this is a learning project.
 * The server inspects the file metadata and returns it in the response.
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload", description = "Upload one or many files to the server (ADMIN only)")
public class FileUploadController {

    // -------------------------------------------------------------------------
    // API 1 — Single File Upload
    //
    // POST /api/files/upload
    // Auth:      Basic auth — admin / password123
    // Body type: multipart/form-data
    // Fields:    file        (required) — the binary file
    //            description (optional) — a text label
    //
    // Returns 201 Created with file name, type, size, and description.
    // Returns 400 if no file is attached or the file is empty.
    // -------------------------------------------------------------------------
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary     = "Upload a single file (ADMIN only)",
        description = """
                Accepts one file via multipart/form-data.
                In Postman: Body → form-data → add key 'file' with Type set to File.
                Optionally add key 'description' with Type Text.
                """,
        security = @SecurityRequirement(name = "basicAuth")
    )
    public ResponseEntity<FileUploadResponse> uploadSingleFile(
            @RequestParam("file")                         MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        // Guard: reject empty file payloads early with a clear message
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                "No file received. Make sure you selected a file and set the Type to 'File' in Postman.");
        }

        FileUploadResponse response = new FileUploadResponse(
                file.getOriginalFilename(),   // e.g. "photo.jpg"
                file.getContentType(),        // e.g. "image/jpeg"
                file.getSize(),               // size in bytes
                description,                  // null if not provided — Jackson omits null by default
                "File uploaded successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // API 2 — Multiple File Upload
    //
    // POST /api/files/upload-multiple
    // Auth:      Basic auth — admin / password123
    // Body type: multipart/form-data
    // Fields:    files    (required, repeat once per file) — the binary files
    //            category (optional)                       — a text label for the batch
    //
    // Returns 201 Created with a count and per-file details.
    // Returns 400 if no files are attached.
    //
    // How multiple files work in Postman:
    //   Add multiple rows all with the key name "files" and Type set to "File".
    //   HTTP sends them as separate parts under the same key name.
    //   Spring collects all matching parts into the List<MultipartFile> below.
    // -------------------------------------------------------------------------
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary     = "Upload multiple files in one request (ADMIN only)",
        description = """
                Accepts two or more files via multipart/form-data.
                In Postman: Body → form-data → add several rows all named 'files' with Type File.
                Optionally add key 'category' with Type Text to label the batch.
                """,
        security = @SecurityRequirement(name = "basicAuth")
    )
    public ResponseEntity<MultiFileUploadResponse> uploadMultipleFiles(
            @RequestParam("files")                        List<MultipartFile> files,
            @RequestParam(value = "category", required = false) String category) {

        // Guard: reject if the list is empty or every file in it is empty
        List<MultipartFile> nonEmpty = files.stream()
                .filter(f -> !f.isEmpty())
                .toList();

        if (nonEmpty.isEmpty()) {
            throw new IllegalArgumentException(
                "No files received. Add at least one file row with Type set to 'File' in Postman.");
        }

        // Build one FileUploadResponse per file — description is not applicable here
        List<FileUploadResponse> fileDetails = nonEmpty.stream()
                .map(f -> new FileUploadResponse(
                        f.getOriginalFilename(),
                        f.getContentType(),
                        f.getSize(),
                        null,   // description not applicable for multi-upload items
                        null    // message not applicable for individual items
                ))
                .toList();

        MultiFileUploadResponse response = new MultiFileUploadResponse(
                fileDetails.size(),
                category,
                fileDetails,
                fileDetails.size() + " file(s) uploaded successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
