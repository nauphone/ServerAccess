/*
 * Copyright (C) 2015 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */

package ru.naumen.servacc.test.util;

import org.junit.Test;
import ru.naumen.servacc.util.Util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Andrey Hitrin
 * @since 15.10.15
 */
public class UtilTest {
    @Test
    public void encodeAndDecodeString() {
        assertThat(Util.base64encode("secret"), is("c2VjcmV0"));
        assertThat(new String(Util.base64decode("c2VjcmV0")), is("secret"));
    }

    @Test
    public void encodeAndDecodeBytes() {
        assertThat(Util.base64encode("metal".getBytes()), is("bWV0YWw="));
        assertThat(new String(Util.base64decode("bWV0YWw=".getBytes())), is("metal"));
    }
}
