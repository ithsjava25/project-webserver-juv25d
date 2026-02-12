package org.juv25d.handler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MimeTypeResolverTest {

    @Test
    void shouldReturnTextHtmlForHtmlFiles() {
        assertThat(MimeTypeResolver.getMimeType("index.html")).isEqualTo("text/html");
        assertThat(MimeTypeResolver.getMimeType("page.htm")).isEqualTo("text/html");
    }

    @Test
    void shouldReturnTextCssForCssFiles() {
        assertThat(MimeTypeResolver.getMimeType("styles.css")).isEqualTo("text/css");
    }

    @Test
    void shouldReturnApplicationJavascriptForJsFiles() {
        assertThat(MimeTypeResolver.getMimeType("app.js")).isEqualTo("application/javascript");
    }

    @Test
    void shouldReturnImagePngForPngFiles() {
        assertThat(MimeTypeResolver.getMimeType("logo.png")).isEqualTo("image/png");
    }

    @Test
    void shouldReturnImageJpegForJpgFiles() {
        assertThat(MimeTypeResolver.getMimeType("photo.jpg")).isEqualTo("image/jpeg");
        assertThat(MimeTypeResolver.getMimeType("image.jpeg")).isEqualTo("image/jpeg");
    }

    @Test
    void shouldReturnApplicationJsonForJsonFiles() {
        assertThat(MimeTypeResolver.getMimeType("data.json")).isEqualTo("application/json");
    }

    @Test
    void shouldReturnDefaultMimeTypeForUnknownExtension() {
        assertThat(MimeTypeResolver.getMimeType("file.unknown")).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldReturnDefaultMimeTypeForFileWithoutExtension() {
        assertThat(MimeTypeResolver.getMimeType("README")).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldReturnDefaultMimeTypeForNullFilename() {
        assertThat(MimeTypeResolver.getMimeType(null)).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldReturnDefaultMimeTypeForEmptyFilename() {
        assertThat(MimeTypeResolver.getMimeType("")).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldHandleUppercaseExtensions() {
        assertThat(MimeTypeResolver.getMimeType("file.HTML")).isEqualTo("text/html");
        assertThat(MimeTypeResolver.getMimeType("file.CSS")).isEqualTo("text/css");
        assertThat(MimeTypeResolver.getMimeType("file.JS")).isEqualTo("application/javascript");
    }

    @Test
    void shouldHandleMixedCaseExtensions() {
        assertThat(MimeTypeResolver.getMimeType("file.HtMl")).isEqualTo("text/html");
        assertThat(MimeTypeResolver.getMimeType("photo.JpG")).isEqualTo("image/jpeg");
    }

    @Test
    void shouldHandleFilesWithMultipleDots() {
        assertThat(MimeTypeResolver.getMimeType("my.file.name.html")).isEqualTo("text/html");
        assertThat(MimeTypeResolver.getMimeType("bundle.min.js")).isEqualTo("application/javascript");
    }

    @Test
    void shouldHandlePathsWithDirectories() {
        assertThat(MimeTypeResolver.getMimeType("/css/styles.css")).isEqualTo("text/css");
        assertThat(MimeTypeResolver.getMimeType("/js/app.js")).isEqualTo("application/javascript");
        assertThat(MimeTypeResolver.getMimeType("/images/logo.png")).isEqualTo("image/png");
    }
}
