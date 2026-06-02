package com.resumeanalyzer.service;

public record StoredResume(String originalName, String storedName, String contentType, long size) {
}
