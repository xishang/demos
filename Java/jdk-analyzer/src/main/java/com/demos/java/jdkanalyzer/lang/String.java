package com.demos.java.jdkanalyzer.lang;

import java.nio.charset.Charset;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/19
 */
public final class String {

    // `char`类型占`2`个字节, 原始的表示方式为`Unicode`编码, 一个汉字也是占用一个`char`
    // getBytes(): 使用操作系统默认字符集, 如: Mac上为`UTF-8`; 用于网络传输或者文件存储时通常不直接使用`Unicode`编码, 而是使用`UTF-8`
    private final char value[];

    private int hash; // Default to 0

    /**
     * 默认构造器: 空字符
     */
    public String() {
        this.value = "".value;
    }

    public char charAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    public int compareTo(java.lang.String anotherString) {
        int len1 = value.length;
        int len2 = anotherString.value.length;
        int lim = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }


    /**
     * Tests if the substring of this string beginning at the
     * specified index starts with the specified prefix.
     *
     * @param   prefix    the prefix.
     * @param   toffset   where to begin looking in this string.
     * @return  {@code true} if the character sequence represented by the
     *          argument is a prefix of the substring of this object starting
     *          at index {@code toffset}; {@code false} otherwise.
     *          The result is {@code false} if {@code toffset} is
     *          negative or greater than the length of this
     *          {@code String} object; otherwise the result is the same
     *          as the result of the expression
     *          <pre>
     *          this.substring(toffset).startsWith(prefix)
     *          </pre>
     */
    public boolean startsWith(String prefix, int toffset) {
        char ta[] = value;
        int to = toffset;
        char pa[] = prefix.value;
        int po = 0;
        int pc = prefix.value.length;
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > value.length - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param   prefix   the prefix.
     * @return  {@code true} if the character sequence represented by the
     *          argument is a prefix of the character sequence represented by
     *          this string; {@code false} otherwise.
     *          Note also that {@code true} will be returned if the
     *          argument is an empty string or is equal to this
     *          {@code String} object as determined by the
     *          {@link #equals(Object)} method.
     * @since   1. 0
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param   suffix   the suffix.
     * @return  {@code true} if the character sequence represented by the
     *          argument is a suffix of the character sequence represented by
     *          this object; {@code false} otherwise. Note that the
     *          result will be {@code true} if the argument is the
     *          empty string or is equal to this {@code String} object
     *          as determined by the {@link #equals(Object)} method.
     */
    public boolean endsWith(String suffix) {
        return startsWith(suffix, value.length - suffix.value.length);
    }

    /**
     * Returns a hash code for this string. The hash code for a
     * {@code String} object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using {@code int} arithmetic, where {@code s[i]} is the
     * <i>i</i>th character of the string, {@code n} is the length of
     * the string, and {@code ^} indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    /**
     * Compares this string to the specified object.  The result is {@code
     * true} if and only if the argument is not {@code null} and is a {@code
     * String} object that represents the same sequence of characters as this
     * object.
     *
     * @param  anObject
     *         The object to compare this {@code String} against
     *
     * @return  {@code true} if the given object represents a {@code String}
     *          equivalent to this string, {@code false} otherwise
     *
     * @see  #compareTo(String)
     * @see  #equalsIgnoreCase(String)
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String)anObject;
            int n = value.length;
            if (n == anotherString.value.length) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index within this string of the first occurrence of
     * the specified character. If a character with value
     * {@code ch} occurs in the character sequence represented by
     * this {@code String} object, then the index (in Unicode
     * code units) of the first such occurrence is returned. For
     * values of {@code ch} in the range from 0 to 0xFFFF
     * (inclusive), this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string, then {@code -1} is returned.
     *
     * @param   ch   a character (Unicode code point).
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object, or
     *          {@code -1} if the character does not occur.
     */
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, starting the search at the specified index.
     * <p>
     * If a character with value {@code ch} occurs in the
     * character sequence represented by this {@code String}
     * object at an index no smaller than {@code fromIndex}, then
     * the index of the first such occurrence is returned. For values
     * of {@code ch} in the range from 0 to 0xFFFF (inclusive),
     * this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string at or after position {@code fromIndex}, then
     * {@code -1} is returned.
     *
     * <p>
     * There is no restriction on the value of {@code fromIndex}. If it
     * is negative, it has the same effect as if it were zero: this entire
     * string may be searched. If it is greater than the length of this
     * string, it has the same effect as if it were equal to the length of
     * this string: {@code -1} is returned.
     *
     * <p>All indices are specified in {@code char} values
     * (Unicode code units).
     *
     * @param   ch          a character (Unicode code point).
     * @param   fromIndex   the index to start the search from.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object that is greater
     *          than or equal to {@code fromIndex}, or {@code -1}
     *          if the character does not occur.
     */
    public int indexOf(int ch, int fromIndex) {
        final int max = value.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // handle most cases here (ch is a BMP code point or a
            // negative value (invalid code point))
            final char[] value = this.value;
            for (int i = fromIndex; i < max; i++) {
                if (value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return indexOfSupplementary(ch, fromIndex);
        }
    }
    public byte[] getBytes(Charset charset) {
        if (charset == null) throw new NullPointerException();
        return StringCoding.encode(charset, value, 0, value.length);
    }

    /**
     * Encodes this {@code String} into a sequence of bytes using the
     * platform's default charset, storing the result into a new byte array.
     *
     * <p> The behavior of this method when this string cannot be encoded in
     * the default charset is unspecified.  The {@link
     * java.nio.charset.CharsetEncoder} class should be used when more control
     * over the encoding process is required.
     *
     * @return  The resultant byte array
     *
     * @since      JDK1.1
     */
    public byte[] getBytes() {
        return StringCoding.encode(value, 0, value.length);
    }

    /**
     * Returns the index within this string of the first occurrence of
     * the specified character. If a character with value
     * {@code ch} occurs in the character sequence represented by
     * this {@code String} object, then the index (in Unicode
     * code units) of the first such occurrence is returned. For
     * values of {@code ch} in the range from 0 to 0xFFFF
     * (inclusive), this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string, then {@code -1} is returned.
     *
     * @param   ch   a character (Unicode code point).
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object, or
     *          {@code -1} if the character does not occur.
     */
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, starting the search at the specified index.
     * <p>
     * If a character with value {@code ch} occurs in the
     * character sequence represented by this {@code String}
     * object at an index no smaller than {@code fromIndex}, then
     * the index of the first such occurrence is returned. For values
     * of {@code ch} in the range from 0 to 0xFFFF (inclusive),
     * this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string at or after position {@code fromIndex}, then
     * {@code -1} is returned.
     *
     * <p>
     * There is no restriction on the value of {@code fromIndex}. If it
     * is negative, it has the same effect as if it were zero: this entire
     * string may be searched. If it is greater than the length of this
     * string, it has the same effect as if it were equal to the length of
     * this string: {@code -1} is returned.
     *
     * <p>All indices are specified in {@code char} values
     * (Unicode code units).
     *
     * @param   ch          a character (Unicode code point).
     * @param   fromIndex   the index to start the search from.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object that is greater
     *          than or equal to {@code fromIndex}, or {@code -1}
     *          if the character does not occur.
     */
    public int indexOf(int ch, int fromIndex) {
        final int max = value.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // handle most cases here (ch is a BMP code point or a
            // negative value (invalid code point))
            final char[] value = this.value;
            for (int i = fromIndex; i < max; i++) {
                if (value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return indexOfSupplementary(ch, fromIndex);
        }
    }

    /**
     * Converts this string to a new character array.
     *
     * @return  a newly allocated character array whose length is the length
     *          of this string and whose contents are initialized to contain
     *          the character sequence represented by this string.
     */
    public char[] toCharArray() {
        // Cannot use Arrays.copyOf because of class initialization order issues
        char result[] = new char[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }

    /**
     * Returns the string representation of the {@code Object} argument.
     *
     * @param   obj   an {@code Object}.
     * @return  if the argument is {@code null}, then a string equal to
     *          {@code "null"}; otherwise, the value of
     *          {@code obj.toString()} is returned.
     * @see     java.lang.Object#toString()
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    /**
     * Returns a canonical representation for the string object.
     * <p>
     * A pool of strings, initially empty, is maintained privately by the
     * class {@code String}.
     * <p>
     * When the intern method is invoked, if the pool already contains a
     * string equal to this {@code String} object as determined by
     * the {@link #equals(Object)} method, then the string from the pool is
     * returned. Otherwise, this {@code String} object is added to the
     * pool and a reference to this {@code String} object is returned.
     * <p>
     * It follows that for any two strings {@code s} and {@code t},
     * {@code s.intern() == t.intern()} is {@code true}
     * if and only if {@code s.equals(t)} is {@code true}.
     * <p>
     * All literal strings and string-valued constant expressions are
     * interned. String literals are defined in section 3.10.5 of the
     * <cite>The Java&trade; Language Specification</cite>.
     *
     * @return  a string that has the same contents as this string, but is
     *          guaranteed to be from a pool of unique strings.
     */
    public native String intern();

}
