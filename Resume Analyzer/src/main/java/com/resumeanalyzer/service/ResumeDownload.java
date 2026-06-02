package com.resumeanalyzer.service;

import org.springframework.core.io.Resource;

public record ResumeDownload(Resource resource, String originalFileName, String contentType) {
}
