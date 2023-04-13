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
 * <p>
 * The URLPattern API provides a web platform primitive for matching URLs based on a convenient pattern syntax.
 *
 * @author Yanbing Zhao
 * @see <a href="https://wicg.github.io/urlpattern">URLPattern API standard</a>
 * @see <a href="https://github.com/pillarjs/path-to-regexp">GitHub Repository of <code>path-to-regexp</code></a>
 */
public final class URLPattern {
    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string and default options. The current
     * constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL)</code> of the
     * specification when <code>baseURL</code> is not given.
     *
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @throws IllegalArgumentException if the pattern string has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern-input-options">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString) {
        this(processInit(patternString), new Options());
    }

    /**
     * Construct a {@link URLPattern} based on a component {@link Map} and default options. The current
     * constructor corresponds to <code>urlPattern = new URLPattern(input)</code> of the specification.
     *
     * @param input a component {@link Map} containing pattern syntax for one or more components
     * @throws IllegalArgumentException if the component {@link Map} contains a pattern of invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern-input-options">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull Map<? super ComponentType, String> input) {
        this(processInit(input, false), new Options());
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and default options.
     * The current constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL)</code>
     * of the specification when <code>baseURL</code> is given.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, @Nonnull String baseUrl) {
        this(processInit(patternString, baseUrl, new Options()), new Options());
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and default options.
     * The current constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL)</code>
     * of the specification when <code>baseURL</code> is given.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URI baseUrl) {
        this(processInit(patternString, baseUrl.toString(), new Options()), new Options());
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and default options.
     * The current constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL)</code>
     * of the specification when <code>baseURL</code> is given.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URL baseUrl) {
        this(processInit(patternString, baseUrl.toString(), new Options()), new Options());
    }

    /**
     * Construct a {@link URLPattern} based on a component {@link Map} and given options. This constructor
     * corresponds to <code>urlPattern = new URLPattern(input, options)</code> of the specification.
     *
     * @param input   a component {@link Map} containing pattern syntax for one or more components
     * @param options an object containing the additional configuration options that can affect match results
     * @throws IllegalArgumentException if the component {@link Map} contains a pattern of invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern-input-options">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull Map<? super ComponentType, String> input, @Nonnull Options options) {
        this(processInit(input, false), options);
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and given options. This
     * constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL, options)</code>
     * of the specification.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @param options       an object containing the additional configuration options that can affect match results
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, String baseUrl, @Nonnull Options options) {
        this(processInit(patternString, baseUrl, options), options);
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and given options. This
     * constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL, options)</code>
     * of the specification.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @param options       an object containing the additional configuration options that can affect match results
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URI baseUrl, @Nonnull Options options) {
        this(processInit(patternString, baseUrl.toString(), options), options);
    }

    /**
     * Construct a {@link URLPattern} based on a URL-like pattern string, a base URL, and given options. This
     * constructor corresponds to <code>urlPattern = new URLPattern(patternString, baseURL, options)</code>
     * of the specification.
     *
     * @param baseUrl       the base URL of the pattern string
     * @param patternString a URL-like string containing pattern syntax for one or more components
     * @param options       an object containing the additional configuration options that can affect match results
     * @throws IllegalArgumentException if either the pattern string or the base URL has an invalid format
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-urlpattern">
     * URLPattern API standard (chapter 1)</a>
     */
    public URLPattern(@Nonnull String patternString, @Nonnull java.net.URL baseUrl, @Nonnull Options options) {
        this(processInit(patternString, baseUrl.toString(), options), options);
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#PROTOCOL}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-protocol">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getProtocol() {
        return this.protocol.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#USERNAME}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-username">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getUsername() {
        return this.username.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#PASSWORD}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-password">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getPassword() {
        return this.password.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#HOSTNAME}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-hostname">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getHostname() {
        return this.hostname.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#PORT}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-port">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getPort() {
        return this.port.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#PATHNAME}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-pathname">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getPathname() {
        return this.pathname.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#SEARCH}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-search">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getSearch() {
        return this.search.regexp;
    }

    /**
     * Return the pattern string for matching the {@link ComponentType#HASH}.
     *
     * @return a pattern string
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-hash">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Pattern getHash() {
        return this.hash.regexp;
    }

    /**
     * Test if the {@link URLPattern} matches the given input string.
     *
     * @param input an input URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull String input) {
        return this.exec(input).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string.
     *
     * @param input an input URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URI input) {
        return this.exec(input.toString()).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string.
     *
     * @param input an input URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URL input) {
        return this.exec(input.toString()).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given component map.
     *
     * @param input a component map
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull Map<? super ComponentType, String> input) {
        return this.exec(input).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull String input, @Nonnull String baseUrl) {
        return this.exec(input, baseUrl).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull String input, @Nonnull java.net.URI baseUrl) {
        return this.exec(input, baseUrl.toString()).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull String input, @Nonnull java.net.URL baseUrl) {
        return this.exec(input, baseUrl.toString()).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URI input, @Nonnull String baseUrl) {
        return this.exec(input.toString(), baseUrl).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URL input, @Nonnull String baseUrl) {
        return this.exec(input.toString(), baseUrl).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URI input, @Nonnull java.net.URI baseUrl) {
        return this.exec(input.toString(), baseUrl.toString()).isPresent();
    }

    /**
     * Test if the {@link URLPattern} matches the given input string and base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-test">
     * URLPattern API standard (chapter 1)</a>
     */
    public boolean test(@Nonnull java.net.URL input, @Nonnull java.net.URL baseUrl) {
        return this.exec(input.toString(), baseUrl.toString()).isPresent();
    }

    /**
     * Match the given string based on the {@link URLPattern}.
     *
     * @param input an input URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull String input) {
        try {
            return Optional.of(match(this, parseUrlInput(input, ""), List.of(input)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Match the given string based on the {@link URLPattern}.
     *
     * @param input an input URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URI input) {
        return this.exec(input.toString());
    }

    /**
     * Match the given string based on the {@link URLPattern}.
     *
     * @param input an input URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URL input) {
        return this.exec(input.toString());
    }

    /**
     * Match the given component map based on the {@link URLPattern}.
     *
     * @param input a component map
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public <T extends Map<? super ComponentType, String>> @Nonnull Optional<Result<T>> exec(@Nonnull T input) {
        try {
            return Optional.of(match(this, processInit(input, true), List.of(input)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull String input, @Nonnull String baseUrl) {
        try {
            return Optional.of(match(this, parseUrlInput(input, baseUrl), List.of(input, baseUrl)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull String input, @Nonnull java.net.URI baseUrl) {
        return this.exec(input, baseUrl.toString());
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull String input, @Nonnull java.net.URL baseUrl) {
        return this.exec(input, baseUrl.toString());
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URI input, @Nonnull String baseUrl) {
        return this.exec(input.toString(), baseUrl);
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URL input, @Nonnull String baseUrl) {
        return this.exec(input.toString(), baseUrl);
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URI input, @Nonnull java.net.URI baseUrl) {
        return this.exec(input.toString(), baseUrl.toString());
    }

    /**
     * Match the given string based on the {@link URLPattern} and a base URL.
     *
     * @param input   an input URL string
     * @param baseUrl the base URL string
     * @return the match result, <code>Optional.empty()</code> if the match process failed
     * @see <a href="https://wicg.github.io/urlpattern/#dom-urlpattern-exec">
     * URLPattern API standard (chapter 1)</a>
     */
    public @Nonnull Optional<Result<String>> exec(@Nonnull java.net.URL input, @Nonnull java.net.URL baseUrl) {
        return this.exec(input.toString(), baseUrl.toString());
    }

    /**
     * Representation of component types of a URL.
     *
     * @see <a href="https://url.spec.whatwg.org/#concept-url">
     * URL standard (chapter 4, section 4.1)</a>
     */
    public enum ComponentType {
        PROTOCOL, USERNAME, PASSWORD, HOSTNAME, PORT, PATHNAME, SEARCH, HASH, BASE_URL;

        @Override
        public String toString() {
            return "URLPattern.ComponentType." + this.name();
        }
    }

    /**
     * An object representing configuration options. The specification now only introduces <code>ignoreCase</code>
     * as an available configuration option.
     *
     * @see <a href="https://wicg.github.io/urlpattern/#dictdef-urlpatternoptions">
     * URLPattern API standard (chapter 1)</a>
     */
    public static final class Options {
        private final boolean ignoreCase;

        public Options() {
            this.ignoreCase = false;
        }

        private Options(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        public boolean getIgnoreCase() {
            return this.ignoreCase;
        }

        public Options withIgnoreCase(boolean ignoreCase) {
            return new Options(ignoreCase);
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                if (o instanceof Options) {
                    var that = (Options) o;
                    return this.ignoreCase == that.ignoreCase;
                }
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.ignoreCase);
        }

        @Override
        public String toString() {
            return "URLPattern.Options{ignoreCase=" + this.ignoreCase + "}";
        }
    }

    /**
     * An object representing the match result, which is the return value of {@link #exec} methods.
     *
     * @see <a href="https://wicg.github.io/urlpattern/#dictdef-urlpatternresult">
     * URLPattern API standard (chapter 1)</a>
     */
    public static final class Result<T> {
        private final List<T> inputs;
        private final ComponentResult protocol;
        private final ComponentResult username;
        private final ComponentResult password;
        private final ComponentResult hostname;
        private final ComponentResult port;
        private final ComponentResult pathname;
        private final ComponentResult search;
        private final ComponentResult hash;

        private Result(List<T> inputs, ComponentResult protocol, ComponentResult username,
                       ComponentResult password, ComponentResult hostname, ComponentResult port,
                       ComponentResult pathname, ComponentResult search, ComponentResult hash) {
            this.inputs = List.copyOf(inputs);
            this.protocol = protocol;
            this.username = username;
            this.password = password;
            this.hostname = hostname;
            this.port = port;
            this.pathname = pathname;
            this.search = search;
            this.hash = hash;
        }

        public @Nonnull List<T> getInputs() {
            return this.inputs;
        }

        public @Nonnull ComponentResult getProtocol() {
            return this.protocol;
        }

        public @Nonnull ComponentResult getUsername() {
            return this.username;
        }

        public @Nonnull ComponentResult getPassword() {
            return this.password;
        }

        public @Nonnull ComponentResult getHostname() {
            return this.hostname;
        }

        public @Nonnull ComponentResult getPort() {
            return this.port;
        }

        public @Nonnull ComponentResult getPathname() {
            return this.pathname;
        }

        public @Nonnull ComponentResult getSearch() {
            return this.search;
        }

        public @Nonnull ComponentResult getHash() {
            return this.hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                if (o instanceof Result<?>) {
                    var that = (Result<?>) o;
                    return this.inputs.equals(that.inputs)
                            && this.hash.equals(that.hash) && this.search.equals(that.search)
                            && this.pathname.equals(that.pathname) && this.port.equals(that.port)
                            && this.hostname.equals(that.hostname) && this.password.equals(that.password)
                            && this.username.equals(that.username) && this.protocol.equals(that.protocol);
                }
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.inputs,
                    this.hash, this.search, this.pathname, this.port,
                    this.hostname, this.password, this.username, this.protocol);
        }

        @Override
        public String toString() {
            return "URLPattern.Result{inputs=" + this.inputs +
                    ", protocol=" + this.protocol + ", username=" + this.username +
                    ", password=" + this.password + ", hostname=" + this.hostname + ", port=" + this.port +
                    ", pathname=" + this.pathname + ", search=" + this.search + ", hash=" + this.hash + "}";
        }
    }

    /**
     * An object representing the match result of a URL component, which is part of
     *
     * @see <a href="https://wicg.github.io/urlpattern/#dictdef-urlpatternresult">
     * URLPattern API standard (chapter 1)</a>
     */
    public static final class ComponentResult {
        private final String input;
        private final Map<String, Optional<String>> groups;

        private ComponentResult(CharSequence input, Map<String, Optional<String>> groups) {
            this.input = input.toString();
            this.groups = Map.copyOf(groups);
        }

        public @Nonnull String getInput() {
            return this.input;
        }

        public @Nonnull Map<String, Optional<String>> getGroups() {
            return this.groups;
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                if (o instanceof ComponentResult) {
                    var that = (ComponentResult) o;
                    return this.groups.equals(that.groups) && this.input.equals(that.input);
                }
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return this.input.hashCode() ^ this.groups.hashCode();
        }

        @Override
        public String toString() {
            return "URLPattern.ComponentResult{input=" + this.input + ", groups=" + this.groups + "}";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this != o) {
            if (o instanceof URLPattern) {
                var that = (URLPattern) o;
                return this.options.equals(that.options)
                        && this.hash.input.equals(that.hash.input) && this.search.input.equals(that.search.input)
                        && this.pathname.input.equals(that.pathname.input) && this.port.input.equals(that.port.input)
                        && this.hostname.input.equals(that.hostname.input) && this.password.input.equals(that.password.input)
                        && this.username.input.equals(that.username.input) && this.protocol.input.equals(that.protocol.input);
            }
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.options,
                this.hash.input, this.search.input, this.pathname.input, this.port.input,
                this.hostname.input, this.password.input, this.username.input, this.protocol.input);
    }

    @Override
    public String toString() {
        return "URLPattern{protocol=" + this.protocol.input +
                ", username=" + this.username.input + ", password=" + this.password.input +
                ", hostname=" + this.hostname.input + ", port=" + this.port.input +
                ", pathname=" + this.pathname.input + ", search=" + this.search.input +
                ", hash=" + this.hash.input + ", options=" + this.options + "}";
    }

    private static final List<String> ESCAPES;
    private static final Map<String, String> SPECIAL_SCHEMES;

    static {
        SPECIAL_SCHEMES = Map.of("file", "", "ftp", "21", "http", "80", "ws", "80", "https", "443", "wss", "443");
        ESCAPES = List.of(IntStream.range(0, 256).mapToObj(i -> String.format("%%%X%X", i / 16, i % 16)).toArray(String[]::new));
    }

    private final ComponentValue protocol;
    private final ComponentValue username;
    private final ComponentValue password;
    private final ComponentValue hostname;
    private final ComponentValue port;
    private final ComponentValue pathname;
    private final ComponentValue search;
    private final ComponentValue hash;
    private final Options options;

    private URLPattern(EnumMap<ComponentType, String> processedInit, Options options) {
        this.options = options;
        var ignoreCase = options.getIgnoreCase();

        var protocol = processedInit.getOrDefault(ComponentType.PROTOCOL, "*");
        if (SPECIAL_SCHEMES.containsKey(protocol)) {
            processedInit.replace(ComponentType.PORT, SPECIAL_SCHEMES.get(protocol), "");
            processedInit.replace(ComponentType.PORT, null, "");
        }
        this.protocol = collectComponent(protocol, "", "", Part.ENCODING_PROTOCOL, false);

        var username = processedInit.getOrDefault(ComponentType.USERNAME, "*");
        this.username = collectComponent(username, "", "", Part.ENCODING_USERNAME, false);

        var password = processedInit.getOrDefault(ComponentType.PASSWORD, "*");
        this.password = collectComponent(password, "", "", Part.ENCODING_PASSWORD, false);

        var hostname = processedInit.getOrDefault(ComponentType.HOSTNAME, "*");
        if (!hostname.startsWith("[") && !hostname.startsWith("\\[") && !hostname.startsWith("{[")) {
            this.hostname = collectComponent(hostname, "", ".", Part.ENCODING_HOSTNAME, false);
        } else {
            this.hostname = collectComponent(hostname, "", ".", Part.ENCODING_IPV6_HOSTNAME, false);
        }

        var port = processedInit.getOrDefault(ComponentType.PORT, "*");
        this.port = collectComponent(port, "", "", Part.ENCODING_PORT, false);

        var pathname = processedInit.getOrDefault(ComponentType.PATHNAME, "*");
        if (SPECIAL_SCHEMES.keySet().stream().anyMatch(this.protocol.regexp.asMatchPredicate())) {
            this.pathname = collectComponent(pathname, "/", "/", Part.ENCODING_PATHNAME, ignoreCase);
        } else {
            this.pathname = collectComponent(pathname, "", "", Part.ENCODING_OPAQUE_PATHNAME, ignoreCase);
        }

        var search = processedInit.getOrDefault(ComponentType.SEARCH, "*");
        this.search = collectComponent(search, "", "", Part.ENCODING_SEARCH, false);

        var hash = processedInit.getOrDefault(ComponentType.HASH, "*");
        this.hash = collectComponent(hash, "", "", Part.ENCODING_HASH, false);
    }

    private static <T> Result<T> match(URLPattern pattern, Map<? super ComponentType, String> input, List<T> inputs) {
        var protocol = collectResult(input.getOrDefault(ComponentType.PROTOCOL, ""), pattern.protocol);
        var username = collectResult(input.getOrDefault(ComponentType.USERNAME, ""), pattern.username);
        var password = collectResult(input.getOrDefault(ComponentType.PASSWORD, ""), pattern.password);
        var hostname = collectResult(input.getOrDefault(ComponentType.HOSTNAME, ""), pattern.hostname);
        var port = collectResult(input.getOrDefault(ComponentType.PORT, ""), pattern.port);
        var pathname = collectResult(input.getOrDefault(ComponentType.PATHNAME, ""), pattern.pathname);
        var search = collectResult(input.getOrDefault(ComponentType.SEARCH, ""), pattern.search);
        var hash = collectResult(input.getOrDefault(ComponentType.HASH, ""), pattern.hash);
        return new Result<T>(inputs, protocol, username, password, hostname, port, pathname, search, hash);
    }

    private static EnumMap<ComponentType, String> processInit(String patternInput, String baseUrl, Options options) {
        var patterns = parsePatternInput(patternInput, options.getIgnoreCase());
        patterns.put(ComponentType.BASE_URL, baseUrl);
        return processInit(patterns, false);
    }

    private static EnumMap<ComponentType, String> processInit(String patternInput) {
        var patterns = parsePatternInput(patternInput, new Options().getIgnoreCase());
        return processInit(patterns, false);
    }

    private static EnumMap<ComponentType, String> processInit(Map<? super ComponentType, String> input, boolean isUrl) {
        var result = new EnumMap<ComponentType, String>(ComponentType.class);
        if (isUrl) {
            result.put(ComponentType.PROTOCOL, "");
            result.put(ComponentType.USERNAME, "");
            result.put(ComponentType.PASSWORD, "");
            result.put(ComponentType.HOSTNAME, "");
            result.put(ComponentType.PORT, "");
            result.put(ComponentType.PATHNAME, "");
            result.put(ComponentType.SEARCH, "");
            result.put(ComponentType.HASH, "");
        }
        var baseUrlOpaquePath = "";
        if (input.containsKey(ComponentType.BASE_URL)) {
            var baseUrl = parseUrlInput(input.get(ComponentType.BASE_URL), "");
            var baseUrlPathname = baseUrl.getOrDefault(ComponentType.PATHNAME, "");
            var baseUrlSpecialPort = Optional.ofNullable(baseUrl.get(ComponentType.PROTOCOL)).map(SPECIAL_SCHEMES::get);
            if (baseUrlSpecialPort.isEmpty() && !baseUrlPathname.startsWith("/")) {
                var baseUrlLastSlash = baseUrlPathname.lastIndexOf('/');
                if (baseUrlLastSlash >= 0) {
                    baseUrlOpaquePath = baseUrlPathname.substring(0, baseUrlLastSlash + 1);
                }
            }
            result.putAll(baseUrl);
        }
        if (input.containsKey(ComponentType.PROTOCOL)) {
            var protocol = input.get(ComponentType.PROTOCOL);
            protocol = protocol.endsWith(":") ? protocol.substring(0, protocol.length() - 1) : protocol;
            result.put(ComponentType.PROTOCOL, isUrl ? encode(protocol, Part.ENCODING_PROTOCOL) : protocol);
        }
        if (input.containsKey(ComponentType.USERNAME)) {
            var username = input.get(ComponentType.USERNAME);
            result.put(ComponentType.USERNAME, isUrl ? encode(username, Part.ENCODING_USERNAME) : username);
        }
        if (input.containsKey(ComponentType.PASSWORD)) {
            var password = input.get(ComponentType.PASSWORD);
            result.put(ComponentType.PASSWORD, isUrl ? encode(password, Part.ENCODING_PASSWORD) : password);
        }
        if (input.containsKey(ComponentType.HOSTNAME)) {
            var hostname = input.get(ComponentType.HOSTNAME);
            result.put(ComponentType.HOSTNAME, isUrl ? encode(hostname, Part.ENCODING_HOSTNAME) : hostname);
        }
        var protocolPort = Optional.ofNullable(result.get(ComponentType.PROTOCOL)).map(SPECIAL_SCHEMES::get);
        if (input.containsKey(ComponentType.PORT) || protocolPort.isPresent()) {
            var port = input.getOrDefault(ComponentType.PORT, protocolPort.orElse(""));
            result.put(ComponentType.PORT, isUrl ? encode(port, Part.ENCODING_PORT) : port);
        }
        if (input.containsKey(ComponentType.PATHNAME)) {
            var pathname = input.get(ComponentType.PATHNAME);
            var isPathnameAbsolute = pathname.startsWith("/");
            if (!isUrl) {
                isPathnameAbsolute = isPathnameAbsolute || pathname.startsWith("\\/") || pathname.startsWith("{/");
            }
            if (!isPathnameAbsolute) {
                pathname = baseUrlOpaquePath + pathname;
            }
            if (isUrl) {
                if (result.get(ComponentType.PROTOCOL).isEmpty() || protocolPort.isPresent()) {
                    pathname = encode(pathname, Part.ENCODING_PATHNAME);
                } else {
                    pathname = encode(pathname, Part.ENCODING_OPAQUE_PATHNAME);
                }
            }
            result.put(ComponentType.PATHNAME, pathname);
        }
        if (input.containsKey(ComponentType.SEARCH)) {
            var search = input.get(ComponentType.SEARCH);
            result.put(ComponentType.SEARCH, isUrl ? encode(search, Part.ENCODING_SEARCH) : search);
        }
        if (input.containsKey(ComponentType.HASH)) {
            var hash = input.get(ComponentType.HASH);
            result.put(ComponentType.HASH, isUrl ? encode(hash, Part.ENCODING_HASH) : hash);
        }
        return result;
    }

    private static EnumMap<ComponentType, String> parseUrlInput(String urlInput, String baseUrl) {
        var result = new EnumMap<ComponentType, String>(ComponentType.class);
        var baseUri = (java.net.URI) null;
        try {
            baseUri = new java.net.URI(baseUrl);
        } catch (java.net.URISyntaxException e) {
            return failAlways(baseUrl, e.getIndex());
        }
        var uri = (java.net.URI) null;
        try {
            uri = baseUri.resolve(new java.net.URI(urlInput));
        } catch (java.net.URISyntaxException e) {
            return failAlways(urlInput, e.getIndex());
        }
        var scheme = uri.getScheme();
        if (scheme != null) {
            result.put(ComponentType.PROTOCOL, scheme);
        }
        var username = uri.getRawAuthority() == null ? null : "";
        var password = uri.getRawAuthority() == null ? null : "";
        var userinfo = uri.getRawUserInfo();
        if (userinfo != null) {
            var c = userinfo.indexOf(':');
            if (c >= 0) {
                username = userinfo.substring(0, c);
                password = userinfo.substring(c + 1);
            } else {
                username = userinfo;
            }
        }
        if (username != null) {
            result.put(ComponentType.USERNAME, username);
        }
        if (password != null) {
            result.put(ComponentType.PASSWORD, password);
        }
        var host = uri.getHost();
        if (host != null) {
            result.put(ComponentType.HOSTNAME, host);
        }
        var port = uri.getPort();
        if (port >= 0) {
            result.put(ComponentType.PORT, Integer.toString(port, 10));
        }
        if (uri.isOpaque()) {
            var path = uri.getRawSchemeSpecificPart();
            var h = path.indexOf('#');
            var s = path.indexOf('?');
            if (s >= 0 && s < h) {
                result.put(ComponentType.PATHNAME, path.substring(0, s));
                result.put(ComponentType.SEARCH, path.substring(s + 1, h));
                result.put(ComponentType.HASH, path.substring(h + 1));
            } else if (h >= 0) {
                result.put(ComponentType.PATHNAME, path.substring(0, h));
                result.put(ComponentType.HASH, path.substring(h + 1));
            } else if (s >= 0) {
                result.put(ComponentType.PATHNAME, path.substring(0, s));
                result.put(ComponentType.SEARCH, path.substring(s + 1));
            } else {
                result.put(ComponentType.PATHNAME, path);
            }
        } else {
            var pathname = uri.getRawPath();
            var hash = uri.getRawFragment();
            var search = uri.getRawQuery();
            if (pathname != null) {
                result.put(ComponentType.PATHNAME, pathname);
            }
            if (hash != null) {
                result.put(ComponentType.HASH, hash);
            }
            if (search != null) {
                result.put(ComponentType.SEARCH, search);
            }
        }
        return result;
    }

    private static EnumMap<ComponentType, String> parsePatternInput(String patternInput, boolean ignoreCase) {
        // states
        var reachTheEnd = false;
        var groupDepthState = 0;
        var ipv6HostnameDepthState = 0;
        var states = new int[Part.STATE_URL_PARSER_SIZE];
        var componentState = -1; // -3: done, -2: authority, -1: init, 0 ~ 7: Component.ordinal()
        // tokens
        var tokens = tokenizePattern(patternInput, false);
        // result
        var result = new EnumMap<ComponentType, String>(ComponentType.class);
        while (true) {
            var tokenType = tokens[states[Part.STATE_TOKEN_INDEX]] & Part.TOKEN_MASK;
            reachTheEnd = tokenType == Part.TOKEN_END;
            states[Part.STATE_TOKEN_INCREMENT] = 1;
            if (reachTheEnd && componentState == -1) { // init
                rewindTokens(states);
                if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                    collectTokens(patternInput, tokens, states, 1);
                    componentState = ComponentType.SEARCH.ordinal();
                    result.put(ComponentType.HASH, "");
                } else if (isSingleChar(patternInput, tokens, states, "#")) {
                    collectTokens(patternInput, tokens, states, 1);
                    componentState = ComponentType.HASH.ordinal();
                } else {
                    collectTokens(patternInput, tokens, states, 0);
                    componentState = ComponentType.PATHNAME.ordinal();
                    result.put(ComponentType.SEARCH, "");
                    result.put(ComponentType.HASH, "");
                }
                stepTokens(tokens, states);
                continue;
            }
            if (reachTheEnd && componentState == -2) { // authority
                rewindTokens(states);
                componentState = ComponentType.HOSTNAME.ordinal();
                stepTokens(tokens, states);
                continue;
            }
            if (reachTheEnd) {
                result.put(ComponentType.values()[componentState], collectTokens(patternInput, tokens, states, 0));
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
                    result.put(ComponentType.HASH, "");
                    result.put(ComponentType.SEARCH, "");
                    result.put(ComponentType.PATHNAME, "");
                    result.put(ComponentType.PORT, "");
                    result.put(ComponentType.HOSTNAME, "");
                    result.put(ComponentType.PASSWORD, "");
                    result.put(ComponentType.USERNAME, "");
                    rewindTokens(states);
                    componentState = ComponentType.PROTOCOL.ordinal();
                }
                stepTokens(tokens, states);
                continue;
            }
            if (componentState == -2) { // authority
                if (isSingleChar(patternInput, tokens, states, "@")) {
                    rewindTokens(states);
                    componentState = ComponentType.USERNAME.ordinal();
                } else if (isSingleChar(patternInput, tokens, states, "/?#") || isAnotherSearch(tokens, states)) {
                    rewindTokens(states);
                    componentState = ComponentType.HOSTNAME.ordinal();
                }
                stepTokens(tokens, states);
                continue;
            }
            switch (ComponentType.values()[componentState]) {
                case PROTOCOL:
                    if (isSingleChar(patternInput, tokens, states, ":")) {
                        var protocolString = collectTokens(patternInput, tokens, states, 0);
                        var protocol = collectComponent(protocolString.isEmpty() ? "*" : protocolString,
                                "", "", Part.ENCODING_PROTOCOL, ignoreCase);
                        var mayBeSpecial = SPECIAL_SCHEMES.keySet().stream().anyMatch(protocol.regexp.asMatchPredicate());
                        var followedByDoubleSlashes = isFollowedByDoubleSlashes(patternInput, tokens, states);
                        var followedByPathname = !followedByDoubleSlashes && !mayBeSpecial;
                        result.put(ComponentType.PROTOCOL, protocolString);
                        if (mayBeSpecial) {
                            result.put(ComponentType.PATHNAME, "/");
                        }
                        collectTokens(patternInput, tokens, states, followedByDoubleSlashes ? 3 : 1);
                        componentState = followedByPathname ? ComponentType.PATHNAME.ordinal() : -2; // authority
                    }
                    break;
                case USERNAME:
                    if (isSingleChar(patternInput, tokens, states, ":")) {
                        result.put(ComponentType.USERNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.USERNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "@")) {
                        result.put(ComponentType.USERNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HOSTNAME.ordinal();
                    }
                    break;
                case PASSWORD:
                    if (isSingleChar(patternInput, tokens, states, "@")) {
                        result.put(ComponentType.PASSWORD, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HOSTNAME.ordinal();
                    }
                    break;
                case HOSTNAME:
                    if (isSingleChar(patternInput, tokens, states, "[")) {
                        ipv6HostnameDepthState += 1;
                    } else if (isSingleChar(patternInput, tokens, states, "]")) {
                        ipv6HostnameDepthState -= 1;
                    } else if (isSingleChar(patternInput, tokens, states, ":") && ipv6HostnameDepthState == 0) {
                        result.put(ComponentType.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.PORT.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "/")) {
                        result.put(ComponentType.HOSTNAME, collectTokens(patternInput, tokens, states, 0));
                        componentState = ComponentType.PATHNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(ComponentType.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(ComponentType.HOSTNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HASH.ordinal();
                    }
                    break;
                case PORT:
                    if (isSingleChar(patternInput, tokens, states, "/")) {
                        result.put(ComponentType.PORT, collectTokens(patternInput, tokens, states, 0));
                        componentState = ComponentType.PATHNAME.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(ComponentType.PORT, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(ComponentType.PORT, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HASH.ordinal();
                    }
                    break;
                case PATHNAME:
                    if (isSingleChar(patternInput, tokens, states, "?") || isAnotherSearch(tokens, states)) {
                        result.put(ComponentType.PATHNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.SEARCH.ordinal();
                    } else if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(ComponentType.PATHNAME, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HASH.ordinal();
                    }
                    break;
                case SEARCH:
                    if (isSingleChar(patternInput, tokens, states, "#")) {
                        result.put(ComponentType.SEARCH, collectTokens(patternInput, tokens, states, 1));
                        componentState = ComponentType.HASH.ordinal();
                    }
                    break;
                // case HASH: /* do nothing */
            }
            stepTokens(tokens, states);
        }
    }

    private static ComponentResult collectResult(String input, ComponentValue componentValue) {
        var matcher = componentValue.regexp.matcher(input);
        failUnless(input, 0, matcher.matches());
        var groups = new HashMap<String, Optional<String>>(matcher.groupCount());
        for (var i = 0; i < matcher.groupCount(); ++i) {
            groups.put(componentValue.groupNameList.get(i), Optional.ofNullable(matcher.group(i + 1)));
        }
        return new ComponentResult(input, groups);
    }

    private static ComponentValue collectComponent(String input, String prefixString, String separateString,
                                                   int encoding, boolean ignoreCase) {
        var result = new StringBuilder("^");
        var namesToCollect = new ArrayList<String>();
        var segPattern = appendEscape(separateString, new StringBuilder("[^")).append("]+?").toString();
        var parts = parsePattern(input, prefixString, segPattern, encoding);
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
            failUnless(input, 0, !part.name.isEmpty());
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
            failAlways(input, 0);
        }
        try {
            var flag = ignoreCase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : Pattern.UNICODE_CASE;
            var pattern = Pattern.compile(result.append("$").toString(), flag);
            return new ComponentValue(input, pattern, namesToCollect);
        } catch (PatternSyntaxException e) {
            return failAlways(result.toString(), e.getIndex());
        }
    }

    private static List<Part> parsePattern(String input, String prefixString, String segToken, int encoding) {
        // states
        var pending = new StringBuilder();
        var states = new int[Part.STATE_PATTERN_PARSER_SIZE];
        // tokens
        var tokens = tokenizePattern(input, true);
        // result
        var parts = new ArrayList<Part>();
        while (true) {
            var charToken = expectToken(input, tokens, states, Part.TOKEN_CHAR);
            var nameToken = expectToken(input, tokens, states, Part.TOKEN_NAME);
            var patternToken = expectToken(input, tokens, states, Part.TOKEN_PATTERN, Part.TOKEN_ASTERISK);
            if (!nameToken.isEmpty() || !patternToken.isEmpty()) {
                var prefix = charToken;
                if (!prefix.equals(prefixString)) {
                    pending.append(prefix);
                    prefix = "";
                }
                failUnless(input, states[Part.STATE_CURSOR], appendTextToPart(parts, pending, encoding));
                var modifier = expectModifier(input, tokens, states);
                failUnless(input, states[Part.STATE_CURSOR], appendTokenToPart(parts,
                        pending, states, prefix, nameToken, patternToken, "", segToken, modifier, encoding));
                continue;
            }
            if (charToken.isEmpty()) {
                charToken = expectToken(input, tokens, states, Part.TOKEN_ESCAPED_CHAR);
            }
            if (!charToken.isEmpty()) {
                pending.append(charToken.charAt(0) == '\\' ? charToken.substring(1) : charToken);
                continue;
            }
            var openToken = expectToken(input, tokens, states, Part.TOKEN_OPEN);
            if (!openToken.isEmpty()) {
                var prefix = expectText(input, tokens, states);
                nameToken = expectToken(input, tokens, states, Part.TOKEN_NAME);
                patternToken = expectToken(input, tokens, states, Part.TOKEN_PATTERN, Part.TOKEN_ASTERISK);
                var suffix = expectText(input, tokens, states);
                var closeToken = expectToken(input, tokens, states, Part.TOKEN_CLOSE);
                failUnless(input, states[Part.STATE_CURSOR], !closeToken.isEmpty());
                var modifier = expectModifier(input, tokens, states);
                failUnless(input, states[Part.STATE_CURSOR], appendTokenToPart(parts,
                        pending, states, prefix, nameToken, patternToken, suffix, segToken, modifier, encoding));
                continue;
            }
            failUnless(input, states[Part.STATE_CURSOR], appendTextToPart(parts, pending, encoding));
            failUnless(input, states[Part.STATE_CURSOR], states[Part.STATE_CURSOR] == input.length());
            // check duplicate names
            var names = new HashSet<String>();
            for (var part : parts) {
                failUnless(input, input.length(), part.name.isEmpty() || names.add(part.name));
            }
            return parts;
        }
    }

    private static int[] tokenizePattern(String input, boolean strict) {
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
                    if (i < size) {
                        var n = input.codePointAt(i);
                        tokens[++tokenIndex] = Part.TOKEN_ESCAPED_CHAR | (cursorStep = Character.charCount(n) + 1);
                    } else {
                        failUnless(input, cursor, !strict);
                        var n = input.codePointAt(cursor);
                        tokens[++tokenIndex] = Part.TOKEN_INVALID_CHAR | (cursorStep = Character.charCount(n));
                    }
                    continue;
                }
                case ':': {
                    var i = cursor + 1;
                    var groupNameStart = i;
                    while (i < size) {
                        var n = input.codePointAt(i);
                        if (i == groupNameStart && Character.isUnicodeIdentifierStart(n)) {
                            i += Character.charCount(n);
                            continue;
                        }
                        if (i != groupNameStart && Character.isUnicodeIdentifierPart(n)) {
                            i += Character.charCount(n);
                            continue;
                        }
                        break;
                    }
                    if (i > cursor + 1) {
                        tokens[++tokenIndex] = Part.TOKEN_NAME | (cursorStep = i - cursor);
                        failUnless(input, cursor, (cursorStep & Part.TOKEN_MASK) == 0);
                    } else {
                        failUnless(input, cursor, !strict);
                        var n = input.codePointAt(cursor);
                        tokens[++tokenIndex] = Part.TOKEN_INVALID_CHAR | (cursorStep = Character.charCount(n));
                    }
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
                        failUnless(input, cursor, (cursorStep & Part.TOKEN_MASK) == 0);
                    } else {
                        failUnless(input, cursor, !strict);
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
        // end tokens are always 0x00000000
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

    private static String collectTokens(String input, int[] tokens, int[] states, int skip) {
        var stateCursor = states[Part.STATE_CURSOR];
        var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
        var result = input.substring(states[Part.STATE_COMPONENT_START_CURSOR], stateCursor);
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

    private static boolean isFollowedByDoubleSlashes(String input, int[] tokens, int[] states) {
        var stateCursor = states[Part.STATE_CURSOR];
        var stateTokenIndex = states[Part.STATE_TOKEN_INDEX];
        var slashFirstCursor = stateCursor + (tokens[stateTokenIndex] & Part.STEP_MASK);
        switch (tokens[stateTokenIndex + 1]) {
            case (Part.TOKEN_CHAR | 1):
            case (Part.TOKEN_INVALID_CHAR | 1):
                if (input.charAt(slashFirstCursor) != '/') {
                    return false;
                }
                break;
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                if (input.charAt(slashFirstCursor + 1) != '/') {
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
                return input.charAt(slashSecondCursor) == '/';
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                return input.charAt(slashSecondCursor + 1) == '/';
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

    private static boolean isSingleChar(String input, int[] tokens, int[] states, String choices) {
        var token = tokens[states[Part.STATE_TOKEN_INDEX]];
        switch (token) {
            case (Part.TOKEN_CHAR | 1):
            case (Part.TOKEN_INVALID_CHAR | 1):
                return choices.indexOf(input.charAt(states[Part.STATE_CURSOR])) >= 0;
            case (Part.TOKEN_ESCAPED_CHAR | 2):
                return choices.indexOf(input.charAt(states[Part.STATE_CURSOR] + 1)) >= 0;
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
                    return encodeScheme(input);
                case Part.ENCODING_USERNAME:
                case Part.ENCODING_PASSWORD:
                    return encodePercent(input, " \"#<>?`{}/:;=@[^");
                case Part.ENCODING_HOSTNAME:
                    return encodeHost(input, false);
                case Part.ENCODING_IPV6_HOSTNAME:
                    return encodeHost(input, true);
                case Part.ENCODING_PORT:
                    return encodePort(input);
                case Part.ENCODING_PATHNAME:
                    return encodePath(input);
                case Part.ENCODING_OPAQUE_PATHNAME:
                    return encodePercent(input, " \"#<>?`{}");
                case Part.ENCODING_SEARCH:
                    return encodePercent(input, " \"#<>?'");
                case Part.ENCODING_HASH:
                    return encodePercent(input, " \"<>`");
            }
        }
        return input;
    }

    private static String encodeScheme(String input) {
        try {
            return new java.net.URI(input + "://dummy.test").getScheme();
        } catch (java.net.URISyntaxException e) {
            return failAlways(input + "://dummy.test", e.getIndex());
        }
    }

    private static String encodeHost(String input, boolean ipv6) {
        if (ipv6) {
            var inputLength = input.length();
            var builder = new StringBuilder();
            for (var i = 0; i < inputLength; ++i) {
                var c = input.charAt(i);
                if ("0123456789abcdef[]:".indexOf(c) >= 0) {
                    builder.append(c);
                } else if ("ABCDEF".indexOf(c) >= 0) {
                    builder.append(Character.toLowerCase(c));
                } else {
                    return failAlways(input, i);
                }
            }
            return builder.toString();
        }
        var host = encodePercent(input, "");
        failUnless(input, 0, host.equals(encodePercent(host, " #/:<>?@[\\]^|")));
        return host;
    }

    private static String encodePort(String input) {
        try {
            var port = Integer.parseInt(input, 10);
            failUnless(input, 0, (port | 0xFFFF) == port);
            return Integer.toString(port, 10);
        } catch (NumberFormatException e) {
            return failAlways(input, 0);
        }
    }

    private static String encodePath(String input) {
        if (input.startsWith("/")) {
            return encodePercent(input, " \"#<>?`{}");
        } else {
            return encodePercent("/-" + input, " \"#<>?`{}").substring(2);
        }
    }

    private static String encodePercent(String input, String percentEncodedChars) {
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

    private static void failUnless(String input, int cursor, boolean cond) {
        if (!cond) {
            throw new IllegalArgumentException("Illegal pattern near index " + cursor + ": " + input);
        }
    }

    private static <T> T failAlways(String input, int cursor) {
        throw new IllegalArgumentException("Illegal pattern near index " + cursor + ": " + input);
    }

    private static final class ComponentValue {
        private final String input;
        private final Pattern regexp;
        private final List<String> groupNameList;

        private ComponentValue(String input, Pattern regexp, List<String> groupNameList) {
            this.input = input;
            this.regexp = regexp;
            this.groupNameList = List.copyOf(groupNameList);
        }
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
