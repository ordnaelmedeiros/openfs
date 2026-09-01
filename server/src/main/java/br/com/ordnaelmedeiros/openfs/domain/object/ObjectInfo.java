package br.com.ordnaelmedeiros.openfs.domain.object;

import java.time.Instant;

public record ObjectInfo(String key, long size, Instant lastModified) {}
