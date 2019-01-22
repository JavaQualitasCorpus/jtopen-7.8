///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable1252.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class ConvTable1252 extends ConvTableAsciiMap
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    private static final String toUnicode_ = 
      "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000B\f\r\u000E\u000F" +
      "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" +
      "\u0020\u0021\"\u0023\u0024\u0025\u0026\'\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" +
      "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" +
      "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" +
      "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" +
      "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" +
      "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F" +
      "\u20AC\u0081\u201A\u0192\u201E\u2026\u2020\u2021\u02C6\u2030\u0160\u2039\u0152\u008D\u017D\u008F" +
      "\u0090\u2018\u2019\u201C\u201D\u2022\u2013\u2014\u02DC\u2122\u0161\u203A\u0153\u009D\u017E\u0178" +
      "\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF" +
      "\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF" +
      "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF" +
      "\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF" +
      "\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF" +
      "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";


    private static final String fromUnicode_ = 
      "\u0001\u0203\u0405\u0607\u0809\u0A0B\u0C0D\u0E0F\u1011\u1213\u1415\u1617\u1819\u1A1B\u1C1D\u1E1F" +
      "\u2021\u2223\u2425\u2627\u2829\u2A2B\u2C2D\u2E2F\u3031\u3233\u3435\u3637\u3839\u3A3B\u3C3D\u3E3F" +
      "\u4041\u4243\u4445\u4647\u4849\u4A4B\u4C4D\u4E4F\u5051\u5253\u5455\u5657\u5859\u5A5B\u5C5D\u5E5F" +
      "\u6061\u6263\u6465\u6667\u6869\u6A6B\u6C6D\u6E6F\u7071\u7273\u7475\u7677\u7879\u7A7B\u7C7D\u7E7F" +
      "\u0000\u0003\u1A81\u1A1A\u1A1A\u1A8D\u1A8F\u901A\uFFFF\u0005\u1A1A\u1A9D\u1A1A\uA0A1\uA2A3\uA4A5" +
      "\uA6A7\uA8A9\uAAAB\uACAD\uAEAF\uB0B1\uB2B3\uB4B5\uB6B7\uB8B9\uBABB\uBCBD\uBEBF\uC0C1\uC2C3\uC4C5" +
      "\uC6C7\uC8C9\uCACB\uCCCD\uCECF\uD0D1\uD2D3\uD4D5\uD6D7\uD8D9\uDADB\uDCDD\uDEDF\uE0E1\uE2E3\uE4E5" +
      "\uE6E7\uE8E9\uEAEB\uECED\uEEEF\uF0F1\uF2F3\uF4F5\uF6F7\uF8F9\uFAFB\uFCFD\uFEFF\uFFFF\u0029\u1A1A" +
      "\u8C9C\uFFFF\u0006\u1A1A\u8A9A\uFFFF\u000B\u1A1A\u9F1A\u1A1A\u1A8E\u9E1A\uFFFF\t\u1A1A\u831A" +
      "\uFFFF\u0099\u1A1A\u881A\uFFFF\n\u1A1A\u981A\uFFFF\u0E9A\u1A1A\u1A96\u971A\u1A1A\u9192\u821A" +
      "\u9394\u841A\u8687\u951A\u1A1A\u851A\uFFFF\u0004\u1A1A\u891A\u1A1A\u1A1A\u1A1A\u1A8B\u9B1A\uFFFF" +
      "\u0038\u1A1A\u801A\uFFFF\u003A\u1A1A\u991A\uFFFF\u6EEE\u1A1A\u1A21\u2223\u2425\u2627\u2829\u2A2B" +
      "\u2C2D\u2E2F\u3031\u3233\u3435\u3637\u3839\u3A3B\u3C3D\u3E3F\u4041\u4243\u4445\u4647\u4849\u4A4B" +
      "\u4C4D\u4E4F\u5051\u5253\u5455\u5657\u5859\u5A5B\u5C5D\u5E5F\u6061\u6263\u6465\u6667\u6869\u6A6B" +
      "\u6C6D\u6E6F\u7071\u7273\u7475\u7677\u7879\u7A7B\u7C7D\u7E1A\uFFFF\u0050\u1A1A";


    ConvTable1252()
    {
        super(1252, toUnicode_.toCharArray(), fromUnicode_.toCharArray());
    }
}
