/**
 *
 * (C) Copyright 2008-2011 syslog4j.org
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package com.nesscomputing.syslog4j.test.message.modifier;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.nesscomputing.syslog4j.Syslog;
import com.nesscomputing.syslog4j.SyslogFacility;
import com.nesscomputing.syslog4j.SyslogIF;
import com.nesscomputing.syslog4j.SyslogLevel;
import com.nesscomputing.syslog4j.SyslogMessageModifierIF;
import com.nesscomputing.syslog4j.SyslogRuntimeException;
import com.nesscomputing.syslog4j.impl.message.modifier.AbstractSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.checksum.ChecksumSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.escape.HTMLEntityEscapeSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.hash.HashSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.mac.MacSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.sequential.SequentialSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.text.PrefixSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.text.StringCaseSyslogMessageModifier;
import com.nesscomputing.syslog4j.impl.message.modifier.text.SuffixSyslogMessageModifier;
import com.nesscomputing.syslog4j.util.Base64;

public class SyslogMessageModifierVerifyTest extends TestCase {
    protected static final Logger LOG = Logger.getLogger("test");

    public void testCRC32Verify() {
        SyslogIF syslog = Syslog.getInstance("udp");

        ChecksumSyslogMessageModifier modifier = ChecksumSyslogMessageModifier.createCRC32();

        String message = "crc32 checksum Test 1234 ABCD";

        String modifiedMessage = modifier.modify(syslog, SyslogFacility.kern, SyslogLevel.EMERGENCY, message);
        LOG.info(modifiedMessage);

        if (!modifier.verify(message,"2399A0FD")) {
            fail();
        }

        if (!modifier.verify(message,597270781)) {
            fail();
        }

        if (!modifier.verify(modifiedMessage)) {
            fail();
        }

        modifier.getConfig().setContinuous(true);

        try {
            modifier.verify(message,"2399A0FD");
            fail("Should throw an Exception");

        } catch (SyslogRuntimeException sre) {
            //
        }

        try {
            modifier.verify(message,597270781);
            fail("Should throw an Exception");

        } catch (SyslogRuntimeException sre) {
            //
        }
    }

    public void testAdler32Verify() {
        SyslogIF syslog = Syslog.getInstance("udp");

        ChecksumSyslogMessageModifier modifier = ChecksumSyslogMessageModifier.createADLER32();
        modifier.getConfig().setPrefix(" ");

        String message = "adler32 checksum Test 4321 DCBA";

        String modifiedMessage = modifier.modify(syslog,SyslogFacility.kern, SyslogLevel.EMERGENCY,message);
        LOG.info(modifiedMessage);

        if (!modifier.verify(message,"A8D109B5")) {
            fail();
        }

        if (!modifier.verify(message,2832271797l)) {
            fail();
        }

        if (!modifier.verify(modifiedMessage)) {
            fail();
        }

        modifier.getConfig().setContinuous(true);

        try {
            modifier.verify(message,"A8D109B5");
            fail("Should throw an Exception");

        } catch (SyslogRuntimeException sre) {
            //
        }

        try {
            modifier.verify(message,2832271797l);
            fail("Should throw an Exception");

        } catch (SyslogRuntimeException sre) {
            //
        }
    }

    public void testHashVerify() {
        SyslogIF syslog = Syslog.getInstance("tcp");

        HashSyslogMessageModifier modifier = HashSyslogMessageModifier.createMD5();
        modifier.getConfig().setSuffix(null);

        String message = "md5 hash Test 1212 ABAB";

        String modifiedMessage = modifier.modify(syslog,SyslogFacility.kern, SyslogLevel.EMERGENCY,message);
        LOG.info(modifiedMessage);

        if (!modifier.verify(message,"fqfK2PYV76Wv9yNQjLoVeg==")) {
            fail();
        }

        if (!modifier.verify(message,Base64.decode("fqfK2PYV76Wv9yNQjLoVeg=="))) {
            fail();
        }

        if (!modifier.verify(modifiedMessage)) {
            fail();
        }

        assertFalse(modifier.verify(null));
    }

    public void testMacVerify() {
        SyslogIF syslog = Syslog.getInstance("udp");

        MacSyslogMessageModifier modifier = MacSyslogMessageModifier.createHmacMD5("fb7Jl0VGnzY5ehJCdeff7bSZ5Vk=");
        modifier.getConfig().setPrefix(" ");
        modifier.getConfig().setSuffix(null);

        String message = "hmacmd5 Test 3434 DCDC";

        String modifiedMessage = modifier.modify(syslog,SyslogFacility.kern, SyslogLevel.EMERGENCY,message);
        LOG.info(modifiedMessage);

        if (!modifier.verify(message,"MfWJ4XhFiMlPwnFEJ401zA==")) {
            fail();
        }

        if (!modifier.verify(message,Base64.decode("MfWJ4XhFiMlPwnFEJ401zA=="))) {
            fail();
        }

        if (!modifier.verify(modifiedMessage)) {
            fail();
        }
    }

    public void testNoopVerify() {
        SyslogMessageModifierIF modifier = SequentialSyslogMessageModifier.createDefault();
        assertTrue(modifier.verify(null));

        modifier = HTMLEntityEscapeSyslogMessageModifier.createDefault();
        assertTrue(modifier.verify(null));

        modifier = new PrefixSyslogMessageModifier("x");
        assertTrue(modifier.verify(null));

        modifier = StringCaseSyslogMessageModifier.LOWER;
        assertTrue(modifier.verify(null));

        modifier = new SuffixSyslogMessageModifier("y");
        assertTrue(modifier.verify(null));
    }

    public void testParseInline() {
        assertNull(AbstractSyslogMessageModifier.parseInlineModifier(null,null,null));
        assertNull(AbstractSyslogMessageModifier.parseInlineModifier("",null,null));
        assertNull(AbstractSyslogMessageModifier.parseInlineModifier(" ",null,null));

        String[] data = AbstractSyslogMessageModifier.parseInlineModifier("xyz abc",null,null);
        assertTrue(Arrays.equals(new String[] { "xyz", "abc" },data));

        data = AbstractSyslogMessageModifier.parseInlineModifier("abc def","",null);
        assertTrue(Arrays.equals(new String[] { "abc", "def" },data));

        data = AbstractSyslogMessageModifier.parseInlineModifier("ghi jkl",null,"");
        assertTrue(Arrays.equals(new String[] { "ghi", "jkl" },data));

        data = AbstractSyslogMessageModifier.parseInlineModifier("mno pqr","","");
        assertTrue(Arrays.equals(new String[] { "mno", "pqr" },data));
    }
}
