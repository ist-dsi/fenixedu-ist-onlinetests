/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.util.tests;

import org.apache.commons.lang3.text.translate.UnicodeEscaper;

public class ResponseSTR extends Response {

    private String response;

    private Boolean isCorrect;

    public ResponseSTR() {
        super();
        super.setResponsed();
    }

    public ResponseSTR(String op) {
        super();
        setResponse(op);
        super.setResponsed();
    }

    public String getResponse() {
        return response != null ? response : "";
    }

    public void setResponse(String op) {
        response = toValid3ByteUTF8String(op);
        if (op != null) {
            super.setResponsed();
        }
    }
    
    public String toValid3ByteUTF8String(String s) {
        final String LAST_3_BYTE_UTF_CHAR = "\uFFFF";
        UnicodeEscaper escaper = new UnicodeEscaper();
        final int length = s.length();
        StringBuilder b = new StringBuilder(length);
        for (int offset = 0; offset < length;) {
            final int codepoint = s.codePointAt(offset);
            if (codepoint > LAST_3_BYTE_UTF_CHAR.codePointAt(0)) {
                b.append(escaper.translate(s.substring(offset, offset+Character.charCount(codepoint))));
            } else {
                if (Character.isValidCodePoint(codepoint)) {
                    b.appendCodePoint(codepoint);
                } else {
                    b.append(escaper.translate(s.substring(offset, offset+Character.charCount(codepoint))));
                }
            }
            offset += Character.charCount(codepoint);
        }
        return b.toString();
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    @Override
    public boolean hasResponse(String responseOption) {
        if (isResponsed()) {
            if (response.equalsIgnoreCase(responseOption)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return response;
    }
}