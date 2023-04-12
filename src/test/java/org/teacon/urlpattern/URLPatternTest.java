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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLPatternTest {
    @Test
    void someLibraryMethodReturnsTrue() {
        assertTrue(new URLPattern("/:foo/:bar").test("/test/route"));
        assertTrue(new URLPattern("/icon-:foo(\\d+).png").test("/icon-123.png"));
        assertFalse(new URLPattern("/icon-:foo(\\d+).png").test("/icon-abc.png"));
        assertTrue(new URLPattern("/(user|u)").test("/u"));
        assertFalse(new URLPattern("/(user|u)").test("/users"));
        assertTrue(new URLPattern("/:attr1?{-:attr2}?{-:attr3}?").test("/test"));
        assertTrue(new URLPattern("/:attr1?{-:attr2}?{-:attr3}?").test("/test-test"));
        assertTrue(new URLPattern("/:foo/(.*)").test("/test/route"));
        assertTrue(new URLPattern("/:foo/:bar?").test("/test"));
        assertTrue(new URLPattern("/:foo/:bar?").test("/test/route"));
        assertTrue(new URLPattern("/search/:tableName\\?useIndex=true&term=amazing").test("/search/people?useIndex=true&term=amazing"));
        assertFalse(new URLPattern("/search/:tableName\\?useIndex=true&term=amazing").test("/search/people?term=amazing&useIndex=true"));
        assertTrue(new URLPattern("/:foo*").test("/"));
        assertTrue(new URLPattern("/:foo*").test("/bar/baz"));
        assertFalse(new URLPattern("/:foo+").test("/"));
        assertTrue(new URLPattern("/:foo+").test("/bar/baz"));
    }
}
