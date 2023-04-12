/**
 * Copyright (C) 2023 TeaConMC &lt;contact@teacon.org&gt;
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.teacon.urlpattern;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

/**
 * Java Implementation of <a href="https://wicg.github.io/urlpattern">URLPattern API standard</a>.
 *
 * @author Yanbing Zhao
 */
public final class URLPattern {
    public URLPattern(@Nonnull String patternString) {
        this(process(patternString), DEFAULT_OPTIONS);
    }

    public URLPattern(@Nonnull Map<? super Component, String> input) {
        this(process(input, false), DEFAULT_OPTIONS);
    }

    public URLPattern(@Nonnull String patternString, @Nonnull String baseUrl) {
        this(process(patternString, baseUrl, DEFAULT_OPTIONS), DEFAULT_OPTIONS);
    }

    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URI baseUrl) {
        this(process(patternString, baseUrl.toString(), DEFAULT_OPTIONS), DEFAULT_OPTIONS);
    }

    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URL baseUrl) {
        this(process(patternString, baseUrl.toString(), DEFAULT_OPTIONS), DEFAULT_OPTIONS);
    }

    public URLPattern(@Nonnull Map<? super Component, String> input, @Nonnull Options options) {
        this(process(input, false), options);
    }

    public URLPattern(@Nonnull String patternString, String baseUrl, @Nonnull Options options) {
        this(process(patternString, baseUrl, options), options);
    }

    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URI baseUrl, @Nonnull Options options) {
        this(process(patternString, baseUrl.toString(), options), options);
    }

    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URL baseUrl, @Nonnull Options options) {
        this(process(patternString, baseUrl.toString(), options), options);
    }

    public @Nonnull Pattern getProtocol() {
        return this.protocol;
    }

    public @Nonnull Pattern getUsername() {
        return this.username;
    }

    public @Nonnull Pattern getPassword() {
        return this.password;
    }

    public @Nonnull Pattern getHostname() {
        return this.hostname;
    }

    public @Nonnull Pattern getPort() {
        return this.port;
    }

    public @Nonnull Pattern getPathname() {
        return this.pathname;
    }

    public @Nonnull Pattern getSearch() {
        return this.search;
    }

    public @Nonnull Pattern getHash() {
        return this.hash;
    }

    public boolean test(@Nonnull String input) {
        try {
            return match(this, parseUrlInput(input, "", true)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean test(@Nonnull String input, @Nonnull String baseUrl) {
        try {
            return match(this, parseUrlInput(input, baseUrl, true)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean test(@Nonnull String input, @Nonnull java.net.URI baseUrl) {
        try {
            return match(this, parseUrlInput(input, baseUrl.toString(), true)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean test(@Nonnull String input, @Nonnull java.net.URL baseUrl) {
        try {
            return match(this, parseUrlInput(input, baseUrl.toString(), true)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean test(@Nonnull Map<? super Component, String> input) {
        try {
            return match(this, process(input, true)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static @Nonnull Options options() {
        return DEFAULT_OPTIONS;
    }

    public enum Component {
        PROTOCOL, USERNAME, PASSWORD, HOSTNAME, PORT, PATHNAME, SEARCH, HASH, BASE_URL
    }

    public static final class Options {
        private final boolean ignoreCase;

        private Options(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        public boolean getIgnoreCase() {
            return this.ignoreCase;
        }

        public Options withIgnoreCase(boolean ignoreCase) {
            return new Options(ignoreCase);
        }
    }

    private static final List<String> ESCAPES;
    private static final Options DEFAULT_OPTIONS;
    private static final Map<String, String> SPECIAL_SCHEMES;

    static {
        DEFAULT_OPTIONS = new Options(false);
        SPECIAL_SCHEMES = Map.of("file", "", "ftp", "21", "http", "80", "ws", "80", "https", "443", "wss", "443");
        ESCAPES = List.of(IntStream.range(0, 256).mapToObj(i -> String.format("%%%X%X", i / 16, i % 16)).toArray(String[]::new));
    }

    private final Pattern protocol;
    private final Pattern username;
    private final Pattern password;
    private final Pattern hostname;
    private final Pattern port;
    private final Pattern pathname;
    private final Pattern search;
    private final Pattern hash;

    private URLPattern(EnumMap<Component, String> processedInit, Options options) {
        var ignoreCase = options.getIgnoreCase();

        var protocol = processedInit.getOrDefault(Component.PROTOCOL, "*");
        if (SPECIAL_SCHEMES.containsKey(protocol)) {
            processedInit.replace(Component.PORT, SPECIAL_SCHEMES.get(protocol), "");
            processedInit.replace(Component.PORT, null, "");
        }
        this.protocol = regexp(protocol, "", "", Part.ENCODING_PROTOCOL, false, new ArrayList<>());

        var username = processedInit.getOrDefault(Component.USERNAME, "*");
        this.username = regexp(username, "", "", Part.ENCODING_USERNAME, false, new ArrayList<>());

        var password = processedInit.getOrDefault(Component.PASSWORD, "*");
        this.password = regexp(password, "", "", Part.ENCODING_PASSWORD, false, new ArrayList<>());

        var hostname = processedInit.getOrDefault(Component.HOSTNAME, "*");
        if (!hostname.startsWith("[") && !hostname.startsWith("\\[") && !hostname.startsWith("{[")) {
            this.hostname = regexp(hostname, "", ".", Part.ENCODING_HOSTNAME, false, new ArrayList<>());
        } else {
            this.hostname = regexp(hostname, "", ".", Part.ENCODING_IPV6_HOSTNAME, false, new ArrayList<>());
        }

        var port = processedInit.getOrDefault(Component.PORT, "*");
        this.port = regexp(port, "", "", Part.ENCODING_PORT, false, new ArrayList<>());

        var pathname = processedInit.getOrDefault(Component.PATHNAME, "*");
        if (SPECIAL_SCHEMES.keySet().stream().anyMatch(this.protocol.asMatchPredicate())) {
            this.pathname = regexp(pathname, "/", "/", Part.ENCODING_PATHNAME, ignoreCase, new ArrayList<>());
        } else {
            this.pathname = regexp(pathname, "", "", Part.ENCODING_OPAQUE_PATHNAME, ignoreCase, new ArrayList<>());
        }

        var search = processedInit.getOrDefault(Component.SEARCH, "*");
        this.search = regexp(search, "", "", Part.ENCODING_SEARCH, false, new ArrayList<>());

        var hash = processedInit.getOrDefault(Component.HASH, "*");
        this.hash = regexp(hash, "", "", Part.ENCODING_HASH, false, new ArrayList<>());
    }

    private static Optional<?> match(URLPattern pattern, Map<? super Component, String> input) {
        var protocolMatcher = pattern.protocol.matcher(input.getOrDefault(Component.PROTOCOL, ""));
        var usernameMatcher = pattern.username.matcher(input.getOrDefault(Component.USERNAME, ""));
        if (!protocolMatcher.matches() || !usernameMatcher.matches()) {
            return Optional.empty();
        }
        var passwordMatcher = pattern.password.matcher(input.getOrDefault(Component.PASSWORD, ""));
        var hostnameMatcher = pattern.hostname.matcher(input.getOrDefault(Component.HOSTNAME, ""));
        if (!passwordMatcher.matches() || !hostnameMatcher.matches()) {
            return Optional.empty();
        }
        var portMatcher = pattern.port.matcher(input.getOrDefault(Component.PORT, ""));
        var pathnameMatcher = pattern.pathname.matcher(input.getOrDefault(Component.PATHNAME, ""));
        if (!portMatcher.matches() || !pathnameMatcher.matches()) {
            return Optional.empty();
        }
        var searchMatcher = pattern.search.matcher(input.getOrDefault(Component.SEARCH, ""));
        var hashMatcher = pattern.hash.matcher(input.getOrDefault(Component.HASH, ""));
        if (!searchMatcher.matches() || !hashMatcher.matches()) {
            return Optional.empty();
        }
        // TODO: generate results
        return Optional.of("");
    }

    private static EnumMap<Component, String> process(String patternInput, String baseUrl, Options options) {
        var patterns = parsePatternInput(patternInput, options.getIgnoreCase());
        patterns.put(Component.BASE_URL, baseUrl);
        return process(patterns, false);
    }

    private static EnumMap<Component, String> process(String patternInput) {
        var patterns = parsePatternInput(patternInput, DEFAULT_OPTIONS.getIgnoreCase());
        return process(patterns, false);
    }

    private static EnumMap<Component, String> process(Map<? super Component, String> dict, boolean isUrl) {
        var baseUrlOpaquePath = "";
        var result = new EnumMap<Component, String>(Component.class);
        if (isUrl) {
            result.put(Component.PROTOCOL, "");
            result.put(Component.USERNAME, "");
            result.put(Component.PASSWORD, "");
            result.put(Component.HOSTNAME, "");
            result.put(Component.PORT, "");
            result.put(Component.PATHNAME, "");
            result.put(Component.SEARCH, "");
            result.put(Component.HASH, "");
        }
        if (dict.containsKey(Component.BASE_URL)) {
            var baseUrl = parseUrlInput(dict.get(Component.BASE_URL), "", isUrl);
            var baseUrlPathname = baseUrl.getOrDefault(Component.PATHNAME, "");
            var baseUrlSpecialPort = Optional.ofNullable(baseUrl.get(Component.PROTOCOL)).map(SPECIAL_SCHEMES::get);
            if (baseUrlSpecialPort.isEmpty() && !baseUrlPathname.startsWith("/")) {
                var baseUrlLastSlash = baseUrlPathname.lastIndexOf('/');
                if (baseUrlLastSlash >= 0) {
                    baseUrlOpaquePath = baseUrlPathname.substring(0, baseUrlLastSlash + 1);
                }
            }
            result.putAll(baseUrl);
        }
        if (dict.containsKey(Component.PROTOCOL)) {
            var protocol = dict.get(Component.PROTOCOL);
            protocol = protocol.endsWith(":") ? protocol.substring(0, protocol.length() - 1) : protocol;
            result.put(Component.PROTOCOL, isUrl ? encode(protocol, Part.ENCODING_PROTOCOL) : protocol);
        }
        if (dict.containsKey(Component.USERNAME)) {
            var username = dict.get(Component.USERNAME);
            result.put(Component.USERNAME, isUrl ? encode(username, Part.ENCODING_USERNAME) : username);
        }
        if (dict.containsKey(Component.PASSWORD)) {
            var password = dict.get(Component.PASSWORD);
            result.put(Component.PASSWORD, isUrl ? encode(password, Part.ENCODING_PASSWORD) : password);
        }
        if (dict.containsKey(Component.HOSTNAME)) {
            var hostname = dict.get(Component.HOSTNAME);
            result.put(Component.HOSTNAME, isUrl ? encode(hostname, Part.ENCODING_HOSTNAME) : hostname);
        }
        var protocolPort = Optional.ofNullable(result.get(Component.PROTOCOL)).map(SPECIAL_SCHEMES::get);
        if (dict.containsKey(Component.PORT) || protocolPort.isPresent()) {
            var port = Optional.ofNullable(dict.get(Component.PORT)).orElseGet(protocolPort::orElseThrow);
            result.put(Component.PORT, isUrl ? encode(port, Part.ENCODING_PORT) : port);
        }
        if (dict.containsKey(Component.PATHNAME)) {
            var pathname = dict.get(Component.PATHNAME);
            var isPathnameAbsolute = pathname.startsWith("/");
            if (!isUrl) {
                isPathnameAbsolute = isPathnameAbsolute || pathname.startsWith("\\/") || pathname.startsWith("{/");
            }
            if (!isPathnameAbsolute) {
                pathname = baseUrlOpaquePath + pathname;
            }
            if (isUrl) {
                if (result.get(Component.PROTOCOL).isEmpty() || protocolPort.isPresent()) {
                    pathname = encode(pathname, Part.ENCODING_PATHNAME);
                } else {
                    pathname = encode(pathname, Part.ENCODING_OPAQUE_PATHNAME);
                }
            }
            result.put(Component.PATHNAME, pathname);
        }
        if (dict.containsKey(Component.SEARCH)) {
            var search = dict.get(Component.SEARCH);
            result.put(Component.SEARCH, isUrl ? encode(search, Part.ENCODING_SEARCH) : search);
        }
        if (dict.containsKey(Component.HASH)) {
            var hash = dict.get(Component.HASH);
            result.put(Component.HASH, isUrl ? encode(hash, Part.ENCODING_HASH) : hash);
        }
        return result;
    }

    private static EnumMap<Component, String> parseUrlInput(String urlInput, String baseUrl, boolean isUrl) {
        var result = new EnumMap<Component, String>(Component.class);
        var uri = (java.net.URI) null;
        try {
            uri = new java.net.URI(baseUrl).resolve(new java.net.URI(urlInput));
        } catch (java.net.URISyntaxException e) {
            return fail(urlInput, e.getIndex());
        }
        var scheme = uri.getScheme();
        if (scheme != null) {
            result.put(Component.PROTOCOL, appendEscape(scheme, new StringBuilder(), isUrl).toString());
        }
        var userinfo = uri.getRawUserInfo();
        if (userinfo != null) {
            var c = userinfo.indexOf(':');
            if (c >= 0) {
                var username = appendEscape(userinfo.substring(0, c), new StringBuilder(), isUrl).toString();
                var password = appendEscape(userinfo.substring(c + 1), new StringBuilder(), isUrl).toString();
                result.put(Component.USERNAME, username);
                result.put(Component.PASSWORD, password);
            } else {
                var username = appendEscape(userinfo, new StringBuilder(), isUrl).toString();
                result.put(Component.USERNAME, username);
            }
        }
        var host = uri.getHost();
        if (host != null) {
            result.put(Component.HOSTNAME, appendEscape(host, new StringBuilder(), isUrl).toString());
        }
        var port = uri.getPort();
        if (port >= 0) {
            result.put(Component.PORT, Integer.toString(port, 10));
        }
        if (uri.isOpaque()) {
            var path = uri.getRawSchemeSpecificPart();
            var h = path.indexOf('#');
            var s = path.indexOf('?');
            if (s >= 0 && s < h) {
                var pathname = appendEscape(path.substring(0, s), new StringBuilder(), isUrl).toString();
                var search = appendEscape(path.substring(s + 1, h), new StringBuilder(), isUrl).toString();
                var hash = appendEscape(path.substring(h + 1), new StringBuilder(), isUrl).toString();
                result.put(Component.PATHNAME, pathname);
                result.put(Component.SEARCH, search);
                result.put(Component.HASH, hash);
            } else if (h >= 0) {
                var pathname = appendEscape(path.substring(0, h), new StringBuilder(), isUrl).toString();
                var hash = appendEscape(path.substring(h + 1), new StringBuilder(), isUrl).toString();
                result.put(Component.PATHNAME, pathname);
                result.put(Component.HASH, hash);
            } else if (s >= 0) {
                var pathname = appendEscape(path.substring(0, s), new StringBuilder(), isUrl).toString();
                var search = appendEscape(path.substring(s + 1), new StringBuilder(), isUrl).toString();
                result.put(Component.PATHNAME, pathname);
                result.put(Component.SEARCH, search);
            } else {
                var pathname = appendEscape(path, new StringBuilder(), isUrl).toString();
                result.put(Component.PATHNAME, pathname);
            }
        } else {
            var pathname = uri.getRawPath();
            var hash = uri.getRawFragment();
            var search = uri.getRawQuery();
            if (pathname != null) {
                result.put(Component.PATHNAME, appendEscape(pathname, new StringBuilder(), isUrl).toString());
            }
            if (hash != null) {
                result.put(Component.HASH, appendEscape(hash, new StringBuilder(), isUrl).toString());
            }
            if (search != null) {
                result.put(Component.SEARCH, appendEscape(search, new StringBuilder(), isUrl).toString());
            }
        }
        return result;
    }

    private static EnumMap<Component, String> parsePatternInput(String patternInput, boolean ignoreCase) {
        // states
        var reachTheEnd = false;
        var groupDepthState = 0;
        var ipv6HostnameDepthState = 0;
        var states = new int[Part.STATE_URL_PARSER_SIZE];
        var componentState = -1; // -3: done, -2: authority, -1: init, 0 ~ 7: Component.ordinal()
        // tokens
        var tokens = tokenize(patternInput, false);
        // result
        var result = new EnumMap<Component, String>(Component.class);
        while (true) {
            var tokenType = tokens[states[Part.STATE_TOKEN_INDEX]] & Part.TOKEN_MASK;
            reachTheEnd = tokenType == Part.TOKEN_END;
            states[Part.STATE_TOKEN_INCREMENT] = 1;
            if (reachTheEnd && componentState == -1) { // init
                rewindTokens(states);
                if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                    collectTokens(patternInput, tokens, states, 1);
                    componentState = Component.SEARCH.ordinal();
                    result.put(Component.HASH, "");
                } else if (isSingleChar(patternInput, tokens, states, "#")) {
                    collectTokens(patternInput, tokens, states, 1);
                    componentState = Component.HASH.ordinal();
                } else {
                    collectTokens(patternInput, tokens, states, 0);
                    componentState = Component.PATHNAME.ordinal();
                    result.put(Component.SEARCH, "");
                    result.put(Component.HASH, "");
                }
                stepTokens(tokens, states);
                continue;
            }
            if (reachTheEnd && componentState == -2) { // authority
                rewindTokens(states);
                componentState = Component.HOSTNAME.ordinal();
                stepTokens(tokens, states);
                continue;
            }
            if (reachTheEnd) {
                result.put(Component.values()[componentState], collectTokens(patternInput, tokens, states, 0));
                /* componentState = -3; // done but not needed to assign */
                return result;
            }
            if (tokenType == Part.TOKEN_OPEN) {
                groupDepthState += 1;
                stepTokens(tokens, states);
                continue;
            }
            if (groupDepthState > 0 && tokenType != Part.TOKEN_CLOSE) {
                stepTokens(tokens, states);
                continue;
            }
            if (groupDepthState > 0) {
                groupDepthState -= 1;
            }
            if (componentState == -1) { // init
                if (isSingleChar(patternInput, tokens, states, ":")) {
                    result.put(Component.HASH, "");
                    result.put(Component.SEARCH, "");
                    result.put(Component.PATHNAME, "");
                    result.put(Component.PORT, "");
                    result.put(Component.HOSTNAME, "");
                    result.put(Component.PASSWORD, "");
                    result.put(Component.USERNAME, "");
                    rewindTokens(states);
                    componentState = Component.PROTOCOL.ordinal();
                }
                stepTokens(tokens, states);
                continue;
            }
            if (componentState == -2) { // authority
                if (isSingleChar(patternInput, tokens, states, "@")) {
                    rewindTokens(states);
                    componentState = Component.USERNAME.ordinal();
                } else if (isSingleChar(patternInput, tokens, states, "/?#") || isAnotherSearch(tokens, states)) {
                    rewindTokens(states);
                    componentState = Component.HOSTNAME.ordinal();
                }
                stepTokens(tokens, states);
                continue;
            }
            switch (Component.values()[componentState]) {
                case PROTOCOL:
                    if (isSingleChar(patternInput, tokens, states, ":")) {
                        var protocolString = collectTokens(patternInput, tokens, states, 0);
                        var protocol = regexp(protocolString.isEmpty() ? "*" : protocolString,
                                "", "", Part.ENCODING_PROTOCOL, ignoreCase, new ArrayList<>());
                        var mayBeSpecial = SPECIAL_SCHEMES.keySet().stream().anyMatch(protocol.asMatchPredicate());
                        var followedByDoubleSlashes = isFollowedByDoubleSlashes(patternInput, tokens, states);
                        var followedByPathname = !followedByDoubleSlashes && !mayBeSpecial;
                        result.put(Component.PROTOCOL, protocolString);
                        if (mayBeSpecial) {
                            result.put(Component.PATHNAME, "/");
                        }
                        collectTokens(patternInput, tokens, states, followedByDoubleSlashes ? 3 : 1);
                        componentState = followedByPathname ? Component.PATHNAME.ordinal() : -2; // authority
                    }
                    break;
                case USERNAME:
                    if (isSingleChar(patternInput, tokens, states, ":")) {
                        result.put(Component.USERNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.USERNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "@")) {
                        result.put(Component.USERNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HOSTNAME.ordinal();
                    }
                    break;
                case PASSWORD:
                    if (isSingleChar(patternInput, tokens, states, "@")) {
                        result.put(Component.PASSWORD, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HOSTNAME.ordinal();
                    }
                    break;
                case HOSTNAME:
                    if (isSingleChar(patternInput, tokens, states, "[")) {
                        ipv6HostnameDepthState += 1;
                    } else if (isSingleChar(patternInput, tokens, states, "]")) {
                        ipv6HostnameDepthState -= 1;
                    } else if (isSingleChar(patternInput, tokens, states, ":") && ipv6HostnameDepthState == 0) {
                        result.put(Component.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.PORT.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "/")) {
                        result.put(Component.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.PATHNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(Component.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(Component.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HASH.ordinal();
                    }
                    break;
                case PORT:
                    if (isSingleChar(patternInput, tokens, states, "/")) {
                        result.put(Component.PORT, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.PATHNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(Component.PORT, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(Component.PORT, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HASH.ordinal();
                    }
                    break;
                case PATHNAME:
                    if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(Component.PATHNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(Component.PATHNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HASH.ordinal();
                    }
                    break;
                case SEARCH:
                    if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(Component.SEARCH, collectTokens(patternInput, tokens, states, 1));
                        componentState = Component.HASH.ordinal();
                    }
                    break;
                // case HASH: /* do nothing */
            }
            stepTokens(tokens, states);
        }
    }

    private static Pattern regexp(String patternInput, String prefixString, String separateString,
                                  int encoding, boolean ignoreCase, List<String> namesToCollect) {
        var result = new StringBuilder("^");
        var parts = parsePattern(patternInput, prefixString, separateString, encoding);
        var segPattern = appendEscape(separateString, new StringBuilder("[^")).append("]+?").toString();
        for (var part : parts) {
            var type = part.typeAndModifier & Part.TYPE_MASK;
            var modifier = part.typeAndModifier & Part.MODIFIER_MASK;
            if (type == Part.TYPE_TEXT) {
                if (modifier == Part.MODIFIER_NONE) {
                    appendEscape(part.value, result);
                    continue;
                }
                appendEscape(part.value, result.append("(?:")).append(")").append((char) modifier);
                continue;
            }
            check(patternInput, 0, !part.name.isEmpty());
            namesToCollect.add(part.name);
            var pattern = type == Part.TYPE_SEGMENT ? segPattern : type == Part.TYPE_ASTERISK ? ".*" : part.value;
            if (part.prefix.isEmpty() && part.suffix.isEmpty()) {
                switch (modifier) {
                    case Part.MODIFIER_NONE:
                        result.append("(").append(pattern).append(")");
                        continue;
                    case Part.MODIFIER_OPTIONAL:
                        result.append("(").append(pattern).append(")?");
                        continue;
                    case Part.MODIFIER_PLUS:
                        result.append("((?:").append(pattern).append(")").append("+)");
                        continue;
                    case Part.MODIFIER_ASTERISK:
                        result.append("((?:").append(pattern).append(")").append("*)");
                        continue;
                }
            } else {
                switch (modifier) {
                    case Part.MODIFIER_NONE:
                        appendEscape(part.prefix, result.append("(?:")).append("(").append(pattern);
                        appendEscape(part.suffix, result.append(")")).append(")");
                        continue;
                    case Part.MODIFIER_OPTIONAL:
                        appendEscape(part.prefix, result.append("(?:")).append("(").append(pattern);
                        appendEscape(part.suffix, result.append(")")).append(")?");
                        continue;
                    case Part.MODIFIER_PLUS:
                        appendEscape(part.prefix, result.append("(?:")).append("((?:").append(pattern).append(")(?:");
                        appendEscape(part.prefix, appendEscape(part.suffix, result)).append("(?:").append(pattern);
                        appendEscape(part.suffix, result.append("))*)")).append(")");
                        continue;
                    case Part.MODIFIER_ASTERISK:
                        appendEscape(part.prefix, result.append("(?:")).append("((?:").append(pattern).append(")(?:");
                        appendEscape(part.prefix, appendEscape(part.suffix, result)).append("(?:").append(pattern);
                        appendEscape(part.suffix, result.append("))*)")).append(")?");
                        continue;
                }
            }
            fail(patternInput, 0);
        }
        try {
            var flag = ignoreCase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : Pattern.UNICODE_CASE;
            return Pattern.compile(result.append("$").toString(), flag);
        } catch (PatternSyntaxException e) {
            return fail(patternInput, e.getIndex());
        }
    }

    private static List<Part> parsePattern(String patternInput, String prefixString,
                                           String separateString, int encoding) {
        // states
        var pending = new StringBuilder();
        var states = new int[Part.STATE_PATTERN_PARSER_SIZE];
        // tokens
        var tokens = tokenize(patternInput, true);
        var segToken = appendEscape(separateString, new StringBuilder("([^")).append("]+?)").toString();
        // result
        var parts = new ArrayList<Part>();
        while (true) {
            var charToken = expectToken(patternInput, tokens, states, Part.TOKEN_CHAR);
            var nameToken = expectToken(patternInput, tokens, states, Part.TOKEN_NAME);
            var patternToken = expectToken(patternInput, tokens, states, Part.TOKEN_PATTERN, Part.TOKEN_ASTERISK);
            if (!nameToken.isEmpty() || !patternToken.isEmpty()) {
                var prefix = charToken;
                if (!prefix.equals(prefixString)) {
                    pending.append(prefix);
                    prefix = "";
                }
                check(patternInput, states[Part.STATE_CURSOR], appendTextToPart(parts, pending, encoding));
                var modifier = expectModifier(patternInput, tokens, states);
                check(patternInput, states[Part.STATE_CURSOR], appendTokenToPart(parts,
                        pending, states, prefix, nameToken, patternToken, "", segToken, modifier, encoding));
                continue;
            }
            if (charToken.isEmpty()) {
                charToken = expectToken(patternInput, tokens, states, Part.TOKEN_ESCAPED_CHAR);
            }
            if (!charToken.isEmpty()) {
                pending.append(charToken.charAt(0) == '\\' ? charToken.substring(1) : charToken);
                continue;
            }
            var openToken = expectToken(patternInput, tokens, states, Part.TOKEN_OPEN);
            if (!openToken.isEmpty()) {
                var prefix = expectText(patternInput, tokens, states);
                nameToken = expectToken(patternInput, tokens, states, Part.TOKEN_NAME);
                patternToken = expectToken(patternInput, tokens, states, Part.TOKEN_PATTERN, Part.TOKEN_ASTERISK);
                var suffix = expectText(patternInput, tokens, states);
                var closeToken = expectToken(patternInput, tokens, states, Part.TOKEN_CLOSE);
                check(patternInput, states[Part.STATE_CURSOR], !closeToken.isEmpty());
                var modifier = expectModifier(patternInput, tokens, states);
                check(patternInput, states[Part.STATE_CURSOR], appendTokenToPart(parts,
                        pending, states, prefix, nameToken, patternToken, suffix, segToken, modifier, encoding));
                continue;
            }
            check(patternInput, states[Part.STATE_CURSOR], appendTextToPart(parts, pending, encoding));
            check(patternInput, states[Part.STATE_CURSOR], states[Part.STATE_CURSOR] == patternInput.length());
            // check duplicate names
            var names = new HashSet<String>();
            for (var part : parts) {
                check(patternInput, patternInput.length(), part.name.isEmpty() || names.add(part.name));
            }
            return parts;
        }
    }

    private static int[] tokenize(String input, boolean strict) {
        var cursorStep = 1;
        var tokenIndex = -1;
        var size = input.length();
        var tokens = new int[input.length() + 3]; // leave at least 3 end tokens since the parser may skip 3 tokens
        for (var cursor = 0; cursor < size; cursor += cursorStep) {
            var c = input.charAt(cursor);
            switch (c) {
                case '*':
                    tokens[++tokenIndex] = Part.TOKEN_ASTERISK | (cursorStep = 1);
                    continue;
                case '+':
                case '?':
                    tokens[++tokenIndex] = Part.TOKEN_OTHER_MODIFIER | (cursorStep = 1);
                    continue;
                case '{':
                    tokens[++tokenIndex] = Part.TOKEN_OPEN | (cursorStep = 1);
                    continue;
                case '}':
                    tokens[++tokenIndex] = Part.TOKEN_CLOSE | (cursorStep = 1);
                    continue;
                case '\\': {
                    var i = cursor + 1;
                    check(input, cursor, size > i);
                    var n = input.codePointAt(i);
                    tokens[++tokenIndex] = Part.TOKEN_ESCAPED_CHAR | (cursorStep = Character.charCount(n) + 1);
                    continue;
                }
                case ':': {
                    var i = cursor + 1;
                    check(input, cursor, size > i);
                    var n = input.codePointAt(i);
                    check(input, cursor, Character.isUnicodeIdentifierStart(n));
                    for (i += Character.charCount(n); i < size; i += Character.charCount(n)) {
                        n = input.codePointAt(i);
                        if (!Character.isUnicodeIdentifierPart(n)) {
                            break;
                        }
                    }
                    tokens[++tokenIndex] = Part.TOKEN_NAME | (cursorStep = i - cursor);
                    check(input, cursor, cursorStep > 1 && (cursorStep & Part.TOKEN_MASK) == 0);
                    continue;
                }
                case '(':
                    var depth = 1;
                    var error = false;
                    var i = cursor + 1;
                    var patternStart = i;
                    for (var j = i; depth > 0 && j < size; i = ++j) {
                        var n = input.charAt(j);
                        if (n > '\u007F') {
                            error = true;
                            break;
                        }
                        if (j == patternStart && n == '?') {
                            error = true;
                            break;
                        }
                        if (n == '\\' && (++j == size || input.charAt(j) > '\u007F')) {
                            error = true;
                            break;
                        }
                        if (n == '(' && (++j == size || input.charAt(j) != '?')) {
                            error = true;
                            break;
                        }
                        depth += n == '(' ? 1 : n == ')' ? -1 : 0;
                    }
                    if (!error && depth == 0 && i > patternStart + 1) {
                        // regexp tokens start with '(' and end with ')'
                        tokens[++tokenIndex] = Part.TOKEN_PATTERN | (cursorStep = i - cursor);
                        check(input, cursor, (cursorStep & Part.TOKEN_MASK) == 0);
                    } else {
                        check(input, cursor, !strict);
                        // The specification said that all those invalid characters should be considered as one token
                        // which doesn't seem to make sense, while the implementation by pillarjs/path-to-regexp does
                        // not handle invalid characters under the lenient context. The implementation here therefore
                        // just read one character (unicode codepoint) as the token and continue the entire loop.
                        var n = input.codePointAt(cursor);
                        tokens[++tokenIndex] = Part.TOKEN_INVALID_CHAR | (cursorStep = Character.charCount(n));
                    }
                    continue;
                default:
                    var n = input.codePointAt(cursor);
                    tokens[++tokenIndex] = Part.TOKEN_CHAR | (cursorStep = Character.charCount(n));
            }
        }
        // end tokens is always 0x00000000
        return tokens;
    }

    private static void rewindTokens(int[] states) {
        states[Part.STATE_TOKEN_INCREMENT] = 0;
        states[Part.STATE_CURSOR] = states[Part.STATE_COMPONENT_START_CURSOR];
        states[Part.STATE_TOKEN_INDEX] = states[Part.STATE_COMPONENT_START_TOKEN_INDEX];
    }

    private static void stepTokens(int[] tokens, int[] states) {
        if (states[Part.STATE_TOKEN_INCREMENT] > 0) {
            var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
            states[Part.STATE_TOKEN_INDEX] = stateTokenIndex + 1;
            states[Part.STATE_CURSOR] += tokens[stateTokenIndex] & Part.STEP_MASK;
        }
    }

    private static String collectTokens(String urlInput, int[] tokens, int[] states, int skip) {
        var stateCursor = states[Part.STATE_CURSOR];
        var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
        var result = urlInput.substring(states[Part.STATE_COMPONENT_START_CURSOR], stateCursor);
        for (var i = 0; i < skip; ++i) {
            stateCursor += tokens[stateTokenIndex] & Part.STEP_MASK;
            stateTokenIndex += 1;
        }
        states[Part.STATE_TOKEN_INCREMENT] = 0;
        states[Part.STATE_CURSOR] = stateCursor;
        states[Part.STATE_TOKEN_INDEX] = stateTokenIndex;
        states[Part.STATE_COMPONENT_START_CURSOR] = stateCursor;
        states[Part.STATE_COMPONENT_START_TOKEN_INDEX] = stateTokenIndex;
        return result;
    }

    private static boolean isFollowedByDoubleSlashes(String urlInput, int[] tokens, int[] states) {
        var stateCursor = states[Part.STATE_CURSOR];
        var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
        var slashFirstCursor = stateCursor + (tokens[stateTokenIndex] & Part.STEP_MASK);
        switch (tokens[stateTokenIndex + 1]) {
            case (Part.TOKEN_CHAR | 1):
            case (Part.TOKEN_INVALID_CHAR | 1):
                if (urlInput.charAt(slashFirstCursor) != '/') {
                    return false;
                }
                break;
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                if (urlInput.charAt(slashFirstCursor + 1) != '/') {
                    return false;
                }
                break;
            default:
                return false;
        }
        var slashSecondCursor = slashFirstCursor + (tokens[stateTokenIndex + 1] & Part.STEP_MASK);
        switch (tokens[stateTokenIndex + 2]) {
            case (Part.TOKEN_CHAR | 1):
            case (Part.TOKEN_INVALID_CHAR | 1):
                return urlInput.charAt(slashSecondCursor) == '/';
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                return urlInput.charAt(slashSecondCursor + 1) == '/';
            default:
                return false;
        }
    }

    private static boolean isAnotherSearch(int[] tokens, int[] states) {
        var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
        if (tokens[stateTokenIndex] == (Part.TOKEN_OTHER_MODIFIER | 1)) {
            var prevToken = stateTokenIndex > 0 ? tokens[stateTokenIndex - 1] & Part.TOKEN_MASK : Part.TOKEN_END;
            switch (prevToken) {
                case Part.TOKEN_NAME:
                case Part.TOKEN_PATTERN:
                case Part.TOKEN_CLOSE:
                case Part.TOKEN_ASTERISK:
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    private static boolean isSingleChar(String urlInput, int[] tokens, int[] states, String choices) {
        var token = tokens[states[Part.STATE_TOKEN_INDEX]];
        switch (token) {
            case (Part.TOKEN_CHAR | 1):
            case (Part.TOKEN_INVALID_CHAR | 1):
                return choices.indexOf(urlInput.charAt(states[Part.STATE_CURSOR])) >= 0;
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                return choices.indexOf(urlInput.charAt(states[Part.STATE_CURSOR] + 1)) >= 0;
            default:
                return false;
        }
    }

    private static boolean appendTextToPart(List<Part> parts, StringBuilder pending, int encoding) {
        if (pending.length() > 0) {
            parts.add(new Part(Part.TYPE_TEXT, "", encode(pending.toString(), encoding), "", ""));
            pending.setLength(0);
        }
        return true;
    }

    private static boolean appendTokenToPart(List<Part> parts, StringBuilder pending, int[] states,
                                             String prefix, String nameToken, String patternToken,
                                             String suffix, String segToken, int modifier, int encoding) {
        var fixedGrouping = nameToken.isEmpty() && patternToken.isEmpty();
        if (fixedGrouping && modifier == Part.MODIFIER_NONE) {
            pending.append(prefix);
            return true;
        }
        if (!appendTextToPart(parts, pending, encoding)) {
            return false;
        }
        if (fixedGrouping) {
            if (!suffix.isEmpty()) {
                return false;
            }
            if (!prefix.isEmpty()) {
                parts.add(new Part(Part.TYPE_TEXT | modifier, "", encode(prefix, encoding), "", ""));
            }
            return true;
        }
        var type = Part.TYPE_PATTERN;
        if (patternToken.isEmpty() || segToken.equals(patternToken)) {
            type = Part.TYPE_SEGMENT;
            patternToken = "()";
        }
        // asterisk tokens start with '*'
        if (patternToken.charAt(0) == '*' || "(.*)".equals(patternToken)) {
            type = Part.TYPE_ASTERISK;
            patternToken = "()";
        }
        var value = patternToken.substring(1, patternToken.length() - 1);
        var name = nameToken.isEmpty() ? String.valueOf(states[Part.STATE_NAME_INDEX]++) : nameToken.substring(1);
        parts.add(new Part(type | modifier, name, value, encode(prefix, encoding), encode(suffix, encoding)));
        return true;
    }

    private static StringBuilder appendEscape(String input, StringBuilder builder, boolean isUrl) {
        return isUrl ? builder.append(input) : appendEscape(input, builder);
    }

    private static StringBuilder appendEscape(String input, StringBuilder patternBuilder) {
        for (var i = 0; i < input.length(); ++i) {
            var c = input.charAt(i);
            if (".+*?^${}()[]|/\\".indexOf(c) >= 0) {
                patternBuilder.append('\\');
            }
            patternBuilder.append(c);
        }
        return patternBuilder;
    }

    private static int expectModifier(String input, int[] tokens, int[] states) {
        var modifierToken = expectToken(input, tokens, states, Part.TOKEN_ASTERISK, Part.TOKEN_OTHER_MODIFIER);
        return !modifierToken.isEmpty() ? modifierToken.charAt(0) : Part.MODIFIER_NONE;
    }

    private static String expectText(String input, int[] tokens, int[] states) {
        var builder = new StringBuilder();
        while (true) {
            var charToken = expectToken(input, tokens, states, Part.TOKEN_CHAR, Part.TOKEN_ESCAPED_CHAR);
            if (charToken.isEmpty()) {
                return builder.toString();
            }
            // escape-char tokens start with '\\'
            builder.append(charToken, charToken.charAt(0) == '\\' ? 1 : 0, charToken.length());
        }
    }

    private static String expectToken(String input, int[] tokens, int[] states, int... tokenTypes) {
        var tokenIndex = states[Part.STATE_TOKEN_INDEX];
        var token = tokens[tokenIndex];
        for (var tokenType : tokenTypes) {
            if ((token & Part.TOKEN_MASK) == tokenType) {
                var oldCursor = states[Part.STATE_CURSOR];
                var cursor = oldCursor + token & Part.STEP_MASK;
                states[Part.STATE_CURSOR] = cursor;
                states[Part.STATE_TOKEN_INDEX] = tokenIndex + 1;
                return input.substring(oldCursor, cursor);
            }
        }
        return "";
    }

    private static String encode(String input, int encoding) {
        if (!input.isEmpty()) {
            switch (encoding) {
                case Part.ENCODING_PROTOCOL:
                    try {
                        return new java.net.URI(input + "://dummy.test").getScheme();
                    } catch (java.net.URISyntaxException e) {
                        return fail(input, e.getIndex());
                    }
                case Part.ENCODING_USERNAME:
                case Part.ENCODING_PASSWORD:
                    return encode(input, " \"#<>?`{}/:;=@[^");
                case Part.ENCODING_HOSTNAME:
                    try {
                        return new java.net.URI(null, null, input, -1, null, null, null).getHost();
                    } catch (java.net.URISyntaxException e) {
                        return fail(input, e.getIndex());
                    }
                case Part.ENCODING_IPV6_HOSTNAME:
                    var inputLength = input.length();
                    var builder = new StringBuilder();
                    for (var i = 0; i < inputLength; ++i) {
                        var c = input.charAt(i);
                        if ("0123456789abcdef[]:".indexOf(c) >= 0) {
                            builder.append(c);
                        } else if ("ABCDEF".indexOf(c) >= 0) {
                            builder.append(Character.toLowerCase(c));
                        } else {
                            return fail(input, i);
                        }
                    }
                    return builder.toString();
                case Part.ENCODING_PORT:
                    try {
                        var port = Integer.parseInt(input, 10);
                        check(input, 0, (port | 0xFFFF) == port);
                        return Integer.toString(port, 10);
                    } catch (NumberFormatException e) {
                        return fail(input, 0);
                    }
                case Part.ENCODING_PATHNAME:
                    if (input.startsWith("/")) {
                        return encode(input, " \"#<>?`{}");
                    } else {
                        return encode("/-" + input, " \"#<>?`{}").substring(2);
                    }
                case Part.ENCODING_OPAQUE_PATHNAME:
                    return encode(input, " \"#<>?`{}");
                case Part.ENCODING_SEARCH:
                    return encode(input, " \"#<>?'");
                case Part.ENCODING_HASH:
                    return encode(input, " \"<>`");
            }
        }
        return input;
    }

    private static String encode(String input, String percentEncodedChars) {
        var builder = new StringBuilder();
        for (var b : input.getBytes(StandardCharsets.UTF_8)) {
            if ((b - 0x20 | ~percentEncodedChars.indexOf(b)) < 0) {
                builder.append(ESCAPES.get(b & 0xFF));
            } else {
                builder.append((char) b);
            }
        }
        return builder.toString();
    }

    private static void check(String input, int cursor, boolean cond) {
        if (!cond) {
            throw new IllegalArgumentException("Illegal pattern near index " + cursor + ": " + input);
        }
    }

    private static <T> T fail(String input, int cursor) {
        throw new IllegalArgumentException("Illegal pattern near index " + cursor + ": " + input);
    }

    private static final class Part {
        private static final int TYPE_MASK = 0xFF00;
        private static final int TYPE_TEXT = 0x0000;
        private static final int TYPE_PATTERN = 0x0100;
        private static final int TYPE_SEGMENT = 0x0200;
        private static final int TYPE_ASTERISK = 0x0300;

        private static final int MODIFIER_MASK = 0x00FF;
        private static final int MODIFIER_NONE = '\0'; // 0x0000
        private static final int MODIFIER_OPTIONAL = '?'; // 0x003F
        private static final int MODIFIER_PLUS = '+'; // 0x002B
        private static final int MODIFIER_ASTERISK = '*'; // 0x002A

        private static final int TOKEN_MASK = 0xF0000000;
        private static final int TOKEN_OPEN = 0x80000000;
        private static final int TOKEN_CLOSE = 0x70000000;
        private static final int TOKEN_PATTERN = 0x60000000;
        private static final int TOKEN_NAME = 0x50000000;
        private static final int TOKEN_CHAR = 0x40000000;
        private static final int TOKEN_ESCAPED_CHAR = 0x30000000;
        private static final int TOKEN_OTHER_MODIFIER = 0x20000000;
        private static final int TOKEN_ASTERISK = 0x10000000;
        private static final int TOKEN_END = 0x00000000;
        private static final int TOKEN_INVALID_CHAR = 0xF0000000;

        private static final int STEP_MASK = 0x0FFFFFFF;

        private static final int STATE_NAME_INDEX = 0x00;
        private static final int STATE_TOKEN_INCREMENT = 0x00;
        private static final int STATE_CURSOR = 0x01;
        private static final int STATE_TOKEN_INDEX = 0x02;
        private static final int STATE_COMPONENT_START_CURSOR = 0x03;
        private static final int STATE_COMPONENT_START_TOKEN_INDEX = 0x04;
        private static final int STATE_PATTERN_PARSER_SIZE = 3;
        private static final int STATE_URL_PARSER_SIZE = 5;

        private static final int ENCODING_PROTOCOL = 0x00;
        private static final int ENCODING_USERNAME = 0x01;
        private static final int ENCODING_PASSWORD = 0x02;
        private static final int ENCODING_HOSTNAME = 0x03;
        private static final int ENCODING_IPV6_HOSTNAME = 0x13;
        private static final int ENCODING_PORT = 0x04;
        private static final int ENCODING_PATHNAME = 0x05;
        private static final int ENCODING_OPAQUE_PATHNAME = 0x15;
        private static final int ENCODING_SEARCH = 0x06;
        private static final int ENCODING_HASH = 0x07;

        private final String name;
        private final String value;
        private final String prefix;
        private final String suffix;
        private final int typeAndModifier;

        private Part(int typeAndModifier, String name, String value, String prefix, String suffix) {
            this.name = name;
            this.value = value;
            this.prefix = prefix;
            this.suffix = suffix;
            this.typeAndModifier = typeAndModifier;
        }
    }
}
