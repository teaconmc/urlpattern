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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class URLPatternTest {
    /**
     * FIXME: support unicode identifier on higher unicode version which is not bounded by java 11
     */
    private final int skipUnsupportedUnicodeIdentifier = Integer.parseInt("1");
    /**
     * FIXME: replace the algorithm by what the whatwg url standard provides
     */
    private final int skipIncorrectUriParsingBehavior = Integer.parseInt("1");
    /**
     * FIXME: wait for the specification decided the correct behavior
     */
    private final int skipSpecificationDeciding = Integer.parseInt("1");

    @Test
    void testFromRustTestCases() {
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/ba")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test("https://example.com/foo/bar"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test("https://example.com/foo/bar/baz"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.SEARCH, "otherquery", URLPattern.ComponentType.HASH, "otherhash")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.SEARCH, "otherquery", URLPattern.ComponentType.HASH, "otherhash")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?otherquery#otherhash")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "https", URLPattern.ComponentType.HOSTNAME, "example.com", URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.SEARCH, "otherquery", URLPattern.ComponentType.HASH, "otherhash")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("https://example.com/foo/bar"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("https://example.com/foo/bar?otherquery#otherhash"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("https://example.com/foo/bar?query#hash"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("https://example.com/foo/bar/baz"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("https://other.com/foo/bar"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test("http://other.com/foo/bar"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://other.com")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com?query#hash")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar", URLPattern.ComponentType.BASE_URL, "http://example.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/([^\\/]+?)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/index.html")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/:bar*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/*+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foobar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/(.*)*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/**")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/fo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{/bar}*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, "(café)")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HASH, "(café)")).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":café")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, ":café")).test(Map.of(URLPattern.ComponentType.USERNAME, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, ":café")).test(Map.of(URLPattern.ComponentType.PASSWORD, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, ":café")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:café")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, ":café")).test(Map.of(URLPattern.ComponentType.SEARCH, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HASH, ":café")).test(Map.of(URLPattern.ComponentType.HASH, "foo")));
        // the unicode version bundled by Java 11 does not recognize U+2118 as an unicode identifier start, so let us
        // ignore these tests until Java versions recognizing U+2118 as an unicode identifier start become popular
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":℘")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, ":℘")).test(Map.of(URLPattern.ComponentType.USERNAME, "foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, ":℘")).test(Map.of(URLPattern.ComponentType.PASSWORD, "foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, ":℘")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:℘")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, ":℘")).test(Map.of(URLPattern.ComponentType.SEARCH, "foo")));
        assertDoesNotThrow(() -> this.skipUnsupportedUnicodeIdentifier != 0 || new URLPattern(Map.of(URLPattern.ComponentType.HASH, ":℘")).test(Map.of(URLPattern.ComponentType.HASH, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":㐀")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, ":㐀")).test(Map.of(URLPattern.ComponentType.USERNAME, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, ":㐀")).test(Map.of(URLPattern.ComponentType.PASSWORD, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, ":㐀")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:㐀")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, ":㐀")).test(Map.of(URLPattern.ComponentType.SEARCH, "foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HASH, ":㐀")).test(Map.of(URLPattern.ComponentType.HASH, "foo")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "(.*)")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "(.*)")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "cafe")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "foo-bar")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foo-bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, "caf%C3%A9")).test(Map.of(URLPattern.ComponentType.USERNAME, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, "café")).test(Map.of(URLPattern.ComponentType.USERNAME, "café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.USERNAME, "caf%c3%a9")).test(Map.of(URLPattern.ComponentType.USERNAME, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, "caf%C3%A9")).test(Map.of(URLPattern.ComponentType.PASSWORD, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, "café")).test(Map.of(URLPattern.ComponentType.PASSWORD, "café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PASSWORD, "caf%c3%a9")).test(Map.of(URLPattern.ComponentType.PASSWORD, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "xn--caf-dma.com")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "café.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "café.com")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "café.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PORT, "")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80{20}?")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80 ")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PORT, "80")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "http{s}?", URLPattern.ComponentType.PORT, "80")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "http", URLPattern.ComponentType.PORT, "80")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PORT, "80")).test(Map.of(URLPattern.ComponentType.PORT, "80")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PORT, "(.*)")).test(Map.of(URLPattern.ComponentType.PORT, "invalid80")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/./bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/baz")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar/../baz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/caf%C3%A9")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/café")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/caf%c3%a9")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/../bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "./foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "", URLPattern.ComponentType.BASE_URL, "https://example.com")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/", URLPattern.ComponentType.BASE_URL, "https://example.com")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{/bar}", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")).test(Map.of(URLPattern.ComponentType.PATHNAME, "./bar", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "\\/bar", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")).test(Map.of(URLPattern.ComponentType.PATHNAME, "./bar", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "b", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")).test(Map.of(URLPattern.ComponentType.PATHNAME, "./b", URLPattern.ComponentType.BASE_URL, "https://example.com/foo/")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")).test("https://example.com/foo/bar"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar", URLPattern.ComponentType.BASE_URL, "https://example.com")).test("https://example.com/foo/bar"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":name.html", URLPattern.ComponentType.BASE_URL, "https://example.com")).test("https://example.com/foo.html"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, "q=caf%C3%A9")).test(Map.of(URLPattern.ComponentType.SEARCH, "q=café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, "q=café")).test(Map.of(URLPattern.ComponentType.SEARCH, "q=café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.SEARCH, "q=caf%c3%a9")).test(Map.of(URLPattern.ComponentType.SEARCH, "q=café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HASH, "caf%C3%A9")).test(Map.of(URLPattern.ComponentType.HASH, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HASH, "café")).test(Map.of(URLPattern.ComponentType.HASH, "café")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.HASH, "caf%c3%a9")).test(Map.of(URLPattern.ComponentType.HASH, "café")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "about", URLPattern.ComponentType.PATHNAME, "(blank|sourcedoc)")).test("about:blank"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "data", URLPattern.ComponentType.PATHNAME, ":number([0-9]+)")).test("data:8675309"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/(\\m)")).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo!")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo!")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo\\:")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo:")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo\\{")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo{")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo\\(")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo(")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")));
        // the url parsing algorithm based on java.net.URI cannot parse those urls which contain spaces correctly (the
        // whatwg url standard continues to process when the url is opaque while it throws an error)
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.BASE_URL, "javascript:var x = 1;")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "(data|javascript)", URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "(https|javascript)", URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "javascript", URLPattern.ComponentType.PATHNAME, "var x = 1;")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "var x = 1;")).test(Map.of(URLPattern.ComponentType.PATHNAME, "var x = 1;")));
        // the specification itself has not decided the correct behavior yet
        assertTrue(this.skipSpecificationDeciding != 0 || new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo/bar")).test("./foo/bar", "https://example.com"));
        assertTrue(new URLPattern("https://example.com:8080/foo?bar#baz").test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo", URLPattern.ComponentType.SEARCH, "bar", URLPattern.ComponentType.HASH, "baz", URLPattern.ComponentType.BASE_URL, "https://example.com:8080")));
        assertTrue(new URLPattern("/foo?bar#baz", "https://example.com:8080").test(Map.of(URLPattern.ComponentType.PATHNAME, "/foo", URLPattern.ComponentType.SEARCH, "bar", URLPattern.ComponentType.HASH, "baz", URLPattern.ComponentType.BASE_URL, "https://example.com:8080")));
        assertTrue(new URLPattern("http{s}?://{*.}?example.com/:product/:endpoint").test("https://sub.example.com/foo/bar"));
        assertTrue(new URLPattern("https://example.com?foo").test("https://example.com/?foo"));
        assertTrue(new URLPattern("https://example.com#foo").test("https://example.com/#foo"));
        assertTrue(new URLPattern("https://example.com:8080?foo").test("https://example.com:8080/?foo"));
        assertTrue(new URLPattern("https://example.com:8080#foo").test("https://example.com:8080/#foo"));
        assertTrue(new URLPattern("https://example.com/?foo").test("https://example.com/?foo"));
        assertTrue(new URLPattern("https://example.com/#foo").test("https://example.com/#foo"));
        assertFalse(new URLPattern("https://example.com/*?foo").test("https://example.com/?foo"));
        assertTrue(new URLPattern("https://example.com/*\\?foo").test("https://example.com/?foo"));
        assertFalse(new URLPattern("https://example.com/:name?foo").test("https://example.com/bar?foo"));
        assertTrue(new URLPattern("https://example.com/:name\\?foo").test("https://example.com/bar?foo"));
        assertFalse(new URLPattern("https://example.com/(bar)?foo").test("https://example.com/bar?foo"));
        assertTrue(new URLPattern("https://example.com/(bar)\\?foo").test("https://example.com/bar?foo"));
        assertFalse(new URLPattern("https://example.com/{bar}?foo").test("https://example.com/bar?foo"));
        assertTrue(new URLPattern("https://example.com/{bar}\\?foo").test("https://example.com/bar?foo"));
        assertFalse(new URLPattern("https://example.com/").test("https://example.com:8080/"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("data:foobar").test("data:foobar"));
        assertTrue(new URLPattern("data\\:foobar").test("data:foobar"));
        assertTrue(new URLPattern("https://{sub.}?example.com/foo").test("https://example.com/foo"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("https://{sub.}?example{.com/}foo").test("https://example.com/foo"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("{https://}example.com/foo").test("https://example.com/foo"));
        assertTrue(new URLPattern("https://(sub.)?example.com/foo").test("https://example.com/foo"));
        assertFalse(new URLPattern("https://(sub.)?example(.com/)foo").test("https://example.com/foo"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("(https://)example.com/foo").test("https://example.com/foo"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("https://{sub{.}}example.com/foo").test("https://example.com/foo"));
        assertTrue(new URLPattern("https://(sub(?:.))?example.com/foo").test("https://example.com/foo"));
        assertTrue(new URLPattern("file:///foo/bar").test("file:///foo/bar"));
        // the url parsing algorithm based on java.net.URI cannot parse 'data:' correctly
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern("data:").test("data:"));
        assertFalse(new URLPattern("foo://bar").test("foo://bad_url_browser_interop"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("(café)://foo").test(Map.of()));
        assertTrue(new URLPattern("https://example.com/foo?bar#baz").test(Map.of(URLPattern.ComponentType.PROTOCOL, "https:", URLPattern.ComponentType.SEARCH, "?bar", URLPattern.ComponentType.HASH, "#baz", URLPattern.ComponentType.BASE_URL, "http://example.com/foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, "http{s}?:", URLPattern.ComponentType.SEARCH, "?bar", URLPattern.ComponentType.HASH, "#baz")).test("http://example.com/foo?bar#baz"));
        // the url parsing algorithm based on java.net.URI handles a different strategy on resolving '?bar#baz' based
        // on '/foo' (the whatwg url standard returns '/foo?bar#baz' while it returns '/?bar#baz')
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern("?bar#baz", "https://example.com/foo").test("?bar#baz", "https://example.com/foo"));
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern("?bar", "https://example.com/foo#baz").test("?bar", "https://example.com/foo#snafu"));
        assertTrue(new URLPattern("#baz", "https://example.com/foo?bar").test("#baz", "https://example.com/foo?bar"));
        assertTrue(new URLPattern("#baz", "https://example.com/foo").test("#baz", "https://example.com/foo"));
        // the url parsing algorithm based on java.net.URI handles a different strategy on resolving those urls based
        // on data urls (the whatwg url standard throws an error while it continues to process)
        assertFalse(this.skipIncorrectUriParsingBehavior == 0 && new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*")).test("foo", "data:data-urls-cannot-be-base-urls"));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*")).test("foo", "not|a|valid|url"));
        assertTrue(new URLPattern("https://foo\\:bar@example.com").test("https://foo:bar@example.com"));
        assertTrue(new URLPattern("https://foo@example.com").test("https://foo@example.com"));
        assertTrue(new URLPattern("https://\\:bar@example.com").test("https://:bar@example.com"));
        assertTrue(new URLPattern("https://:user::pass@example.com").test("https://foo:bar@example.com"));
        // the url parsing algorithm based on java.net.URI handles a different strategy on resolving those urls whose
        // protocols are special but are not followed by '://' (the whatwg url standard continues to handle authority
        // components while it will parse as an opaque url)
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern("https\\:foo\\:bar@example.com").test("https:foo:bar@example.com"));
        assertTrue(new URLPattern("data\\:foo\\:bar@example.com").test("data:foo:bar@example.com"));
        assertFalse(new URLPattern("https://foo{\\:}bar@example.com").test("https://foo:bar@example.com"));
        assertTrue(new URLPattern("data{\\:}channel.html", "https://example.com").test("https://example.com/data:channel.html"));
        assertTrue(new URLPattern("http://[\\:\\:1]/").test("http://[::1]/"));
        assertTrue(new URLPattern("http://[\\:\\:1]:8080/").test("http://[::1]:8080/"));
        assertTrue(new URLPattern("http://[\\:\\:a]/").test("http://[::a]/"));
        assertTrue(new URLPattern("http://[:address]/").test("http://[::1]/"));
        assertTrue(new URLPattern("http://[\\:\\:AB\\::num]/").test("http://[::ab:1]/"));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "[\\:\\:AB\\::num]")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "[::ab:1]")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "[\\:\\:xY\\::num]")).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "{[\\:\\:ab\\::num]}")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "[::ab:1]")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "{[\\:\\:fé\\::num]}")).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "{[\\:\\::num\\:1]}")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "[::ab:1]")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "{[\\:\\::num\\:fé]}")).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "[*\\:1]")).test(Map.of(URLPattern.ComponentType.HOSTNAME, "[::ab:1]")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "*\\:1]")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("https://foo{{@}}example.com").test("https://foo@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("https://foo{@example.com").test("https://foo@example.com"));
        // the url parsing algorithm based on java.net.URI cannot parse those urls which contain spaces correctly (the
        // whatwg url standard continues to process when the url is opaque while it throws an error)
        assertTrue(this.skipIncorrectUriParsingBehavior != 0 || new URLPattern("data\\:text/javascript,let x = 100/:tens?5;").test("data:text/javascript,let x = 100/5;"));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:id/:id")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/foo", URLPattern.ComponentType.BASE_URL, "")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern("/foo", "").test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":name*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":name+")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":name")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":name*")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":name+")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PROTOCOL, ":name")).test(Map.of(URLPattern.ComponentType.PROTOCOL, "foobar")));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad#hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad%hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad/hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad\\:hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad<hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad>hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad?hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad@hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad[hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad]hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad\\\\hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad^hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad|hostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad\nhostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad\rhostname")).test(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new URLPattern(Map.of(URLPattern.ComponentType.HOSTNAME, "bad\thostname")).test(Map.of()));
        assertTrue(new URLPattern(Map.of()).test("https://example.com/"));
        assertTrue(new URLPattern(Map.of()).test("https://example.com/"));
        assertTrue(new URLPattern(Map.of()).test(Map.of()));
        assertTrue(new URLPattern(Map.of()).test(Map.of()));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "(foo)(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{(foo)bar}(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "(foo)?(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}(barbaz)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}{(.*)}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}{(.*)bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}{bar(.*)}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}:bar(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}?(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobarbaz")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo\\bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo\\.bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo.bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo(foo)bar}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "{:foo}bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo\\bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo{}(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo{}bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo{}?bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*{}**?")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foobar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo(baz)(.*)")).test(Map.of(URLPattern.ComponentType.PATHNAME, "bazbar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo(baz)bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "bazbar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*\\/*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*/{*}")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")));
        assertFalse(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "*//*")).test(Map.of(URLPattern.ComponentType.PATHNAME, "foo/bar")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:foo.")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/bar.")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:foo..")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/bar..")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "./foo")).test(Map.of(URLPattern.ComponentType.PATHNAME, "./foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "../foo")).test(Map.of(URLPattern.ComponentType.PATHNAME, "../foo")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo./")).test(Map.of(URLPattern.ComponentType.PATHNAME, "bar./")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, ":foo../")).test(Map.of(URLPattern.ComponentType.PATHNAME, "bar../")));
        assertTrue(new URLPattern(Map.of(URLPattern.ComponentType.PATHNAME, "/:foo\\bar")).test(Map.of(URLPattern.ComponentType.PATHNAME, "/bazbar")));
    }
}
