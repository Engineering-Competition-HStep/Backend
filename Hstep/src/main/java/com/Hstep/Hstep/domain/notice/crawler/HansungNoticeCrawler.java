package com.Hstep.Hstep.domain.notice.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HansungNoticeCrawler {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("(20\\d{2})[.-](\\d{1,2})[.-](\\d{1,2})");

    private static final Pattern NEW_LABEL_PATTERN =
            Pattern.compile("\\s*새글\\s*$");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.M.d");

    private final int timeoutMs;
    private final String userAgent;

    public HansungNoticeCrawler(
            @Value("${app.notice-crawler.timeout-ms:10000}")
            int timeoutMs,
            @Value("${app.notice-crawler.user-agent:HStepNoticeCrawler/1.0}")
            String userAgent
    ) {
        this.timeoutMs = timeoutMs;
        this.userAgent = userAgent;
    }

    public List<CrawledNotice> crawl(
            NoticeBoard board,
            int page
    ) throws IOException {

        int maxAttempts = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Document document = Jsoup.connect(board.pageUrl(page))
                        .userAgent(userAgent)
                        .header("Accept-Language", "ko-KR,ko;q=0.9")
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                        )
                        .referrer(board.getListUrl())
                        .timeout(timeoutMs)
                        .followRedirects(true)
                        .get();

                return parseDocument(document, board);

            } catch (IOException exception) {
                lastException = exception;

                if (attempt == maxAttempts) {
                    break;
                }

                long waitTime = 1500L * attempt;

                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();

                    throw new IOException(
                            "크롤링 재시도 대기 중 인터럽트가 발생했습니다.",
                            interruptedException
                    );
                }
            }
        }

        throw new IOException(
                board.name()
                        + " "
                        + page
                        + "페이지 요청이 "
                        + maxAttempts
                        + "회 모두 실패했습니다.",
                lastException
        );
    }

    /**
     * K2Web 게시판의 CSS 클래스명이 일부 변경되어도 동작하도록,
     * /artclView.do 링크와 가장 가까운 tr을 기준으로 파싱합니다.
     */
    List<CrawledNotice> parseDocument(
            Document document,
            NoticeBoard board
    ) {
        Elements links = document.select(
                "a[href*='/artclView.do'], a[href*='artclView.do']"
        );

        Map<String, CrawledNotice> noticesByUrl = new LinkedHashMap<>();

        for (Element link : links) {
            Element row = link.closest("tr");
            if (row == null) {
                continue;
            }

            String sourceUrl = resolveSourceUrl(
                    document,
                    board,
                    link
            );
            if (!StringUtils.hasText(sourceUrl)) {
                continue;
            }

            LocalDateTime publishedAt = extractPublishedAt(row.text());
            if (publishedAt == null) {
                continue;
            }

            String rawTitle = normalizeWhitespace(link.text());
            String category = extractCategory(
                    board,
                    rawTitle,
                    normalizeWhitespace(row.text())
            );
            String title = cleanTitle(rawTitle, category);

            if (!StringUtils.hasText(title)) {
                continue;
            }

            noticesByUrl.put(
                    sourceUrl,
                    new CrawledNotice(
                            title,
                            category,
                            board.getSource(),
                            sourceUrl,
                            publishedAt
                    )
            );
        }

        return new ArrayList<>(noticesByUrl.values());
    }

    private String resolveSourceUrl(
            Document document,
            NoticeBoard board,
            Element link
    ) {
        String sourceUrl = link.absUrl("href");

        if (!StringUtils.hasText(sourceUrl)) {
            try {
                String base = StringUtils.hasText(document.baseUri())
                        ? document.baseUri()
                        : board.getListUrl();
                sourceUrl = URI.create(base)
                        .resolve(link.attr("href"))
                        .toString();
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        int queryIndex = sourceUrl.indexOf('?');
        if (queryIndex >= 0) {
            sourceUrl = sourceUrl.substring(0, queryIndex);
        }

        return sourceUrl;
    }

    private LocalDateTime extractPublishedAt(String rowText) {
        Matcher matcher = DATE_PATTERN.matcher(rowText);
        if (!matcher.find()) {
            return null;
        }

        String dateText = matcher.group(1)
                + "."
                + matcher.group(2)
                + "."
                + matcher.group(3);

        try {
            LocalDate date = LocalDate.parse(
                    dateText,
                    DATE_FORMATTER
            );
            return date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String extractCategory(
            NoticeBoard board,
            String rawTitle,
            String rowText
    ) {
        return board.getCategories()
                .stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(category ->
                        startsWithCategory(rawTitle, category)
                                || rowContainsLeadingCategory(rowText, category)
                )
                .findFirst()
                .orElse("미분류");
    }

    private boolean startsWithCategory(
            String text,
            String category
    ) {
        return text.equals(category)
                || text.startsWith(category + " ")
                || text.startsWith("[" + category + "]");
    }

    private boolean rowContainsLeadingCategory(
            String rowText,
            String category
    ) {
        String escaped = Pattern.quote(category);

        return rowText.matches(
                "^(일반공지|전체게시판공지|\\d+)\\s+"
                        + escaped
                        + "(\\s+.*)?$"
        );
    }

    private String cleanTitle(
            String rawTitle,
            String category
    ) {
        String title = rawTitle;

        if (!"미분류".equals(category)) {
            if (title.startsWith(category + " ")) {
                title = title.substring(category.length()).trim();
            } else if (title.startsWith("[" + category + "]")) {
                title = title.substring(category.length() + 2).trim();
            }
        }

        title = NEW_LABEL_PATTERN.matcher(title).replaceFirst("");
        return normalizeWhitespace(title);
    }

    private String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }
}
