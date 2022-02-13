/*
 * Copyright Spectra, Inc. All Rights Reserved.
 */
package jobis.restapi.util;

/**
 * <p>
 * Encodes and decodes to and from Base64 notation.
 * </p>
 * <p>
 * Homepage: <a href="http://iharder.net/base64">http://iharder.net/base64</a>.
 * </p>
 * <p>
 * The constants defined in Base64 can be OR-ed together to combine options, so you might make a call like this:
 * </p>
 * <code>String encoded = Base64.encodeBytes( mybytes, Base64.GZIP | Base64.DONT_BREAK_LINES );</code>
 * <p>
 * to compress the data before encoding it and then making the output have no newline characters.
 * </p>
 * *
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.2.2
 */

public class Base64
{

    /** No options specified. Value is zero. */
    public static final int NO_OPTIONS = 0;

    /** Specify encoding. */
    public static final int ENCODE = 1;

    /** Specify decoding. */
    public static final int DECODE = 0;

    /** Specify that data should be gzip-compressed. */
    public static final int GZIP = 2;

    /** Don't break lines when encoding (violates strict Base64 specification). */
    public static final int DONT_BREAK_LINES = 8;

    /**
     * Encode using Base64-like encoding that is URL- and Filename-safe as described in Section 4 of RFC3548: <a
     * href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>. It is important to note
     * that data encoded this way is <em>not</em> officially valid Base64, or at the very least should not be called
     * Base64 without also specifying that is was encoded using the URL- and Filename-safe dialect.
     */
    public static final int URL_SAFE = 16;

    /**
     * Encode using the special "ordered" dialect of Base64 described here: <a
     * href="http://www.faqs.org/qa/rfcc-1940.html">http://www.faqs.org/qa/rfcc-1940.html</a>.
     */
    public static final int ORDERED = 32;

    /*  ******** P R I V A T E F I E L D S ******** */

    /** Maximum line length (76) of Base64 output. */
    private static final int MAX_LINE_LENGTH = 76;

    /** The equals sign (=) as a byte. */
    private static final byte EQUALS_SIGN = (byte) '=';

    /** The new line character (\n) as a byte. */
    private static final byte NEW_LINE = (byte) '\n';

    /** Preferred encoding. */
    private static final String PREFERRED_ENCODING = "UTF-8";

    // I think I end up not using the BAD_ENCODING indicator.
    // private static final byte BAD_ENCODING = -9; // Indicates error in encoding
    /** The Constant WHITE_SPACE_ENC. */
    private static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding

    /** The Constant EQUALS_SIGN_ENC. */
    private static final byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding

    /*  ******** S T A N D A R D B A S E 6 4 A L P H A B E T ******** */

    /** The 64 valid Base64 values. */
    // private static final byte[] ALPHABET;
    /** Host platform me be something funny like EBCDIC, so we hardcode these values. */
    private static final byte[] STANDARD_ALPHABET = {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/'};

    /**
     * Translates a Base64 value to either its 6-bit reconstruction value or a negative number indicating some other
     * meaning.
     */
    private static final byte[] STANDARD_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
        -5, -5, // Whitespace: Tab and Linefeed
        -9, -9, // Decimal 11 - 12
        -5, // Whitespace: Carriage Return
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
        -9, -9, -9, -9, -9, // Decimal 27 - 31
        -5, // Whitespace: Space
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
        62, // Plus sign at decimal 43
        -9, -9, -9, // Decimal 44 - 46
        63, // Slash at decimal 47
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
        -9, -9, -9, // Decimal 58 - 60
        -1, // Equals sign at decimal 61
        -9, -9, -9, // Decimal 62 - 64
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
        14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
        -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
        -9, -9, -9, -9 // Decimal 123 - 126
    };

    /*  ******** U R L S A F E B A S E 6 4 A L P H A B E T ******** */

    /**
     * Used in the URL- and Filename-safe dialect described in Section 4 of RFC3548: <a
     * href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>. Notice that the last two
     * bytes become "hyphen" and "underscore" instead of "plus" and "slash."
     */
    private static final byte[] URL_SAFE_ALPHABET = {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '-', (byte) '_'};

    /** Used in decoding URL- and Filename-safe dialects of Base64. */
    private static final byte[] URL_SAFE_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
        -5, -5, // Whitespace: Tab and Linefeed
        -9, -9, // Decimal 11 - 12
        -5, // Whitespace: Carriage Return
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
        -9, -9, -9, -9, -9, // Decimal 27 - 31
        -5, // Whitespace: Space
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
        -9, // Plus sign at decimal 43
        -9, // Decimal 44
        62, // Minus sign at decimal 45
        -9, // Decimal 46
        -9, // Slash at decimal 47
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
        -9, -9, -9, // Decimal 58 - 60
        -1, // Equals sign at decimal 61
        -9, -9, -9, // Decimal 62 - 64
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
        14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
        -9, -9, -9, -9, // Decimal 91 - 94
        63, // Underscore at decimal 95
        -9, // Decimal 96
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
        -9, -9, -9, -9 // Decimal 123 - 126
    };

    /*  ******** O R D E R E D B A S E 6 4 A L P H A B E T ******** */

    /**
     * I don't get the point of this technique, but it is described here: <a
     * href="http://www.faqs.org/qa/rfcc-1940.html">http://www.faqs.org/qa/rfcc-1940.html</a>.
     */
    private static final byte[] ORDERED_ALPHABET = {(byte) '-', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) '_', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z'};

    /** Used in decoding the "ordered" dialect of Base64. */
    private static final byte[] ORDERED_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
        -5, -5, // Whitespace: Tab and Linefeed
        -9, -9, // Decimal 11 - 12
        -5, // Whitespace: Carriage Return
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
        -9, -9, -9, -9, -9, // Decimal 27 - 31
        -5, // Whitespace: Space
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
        -9, // Plus sign at decimal 43
        -9, // Decimal 44
        0, // Minus sign at decimal 45
        -9, // Decimal 46
        -9, // Slash at decimal 47
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // Numbers zero through nine
        -9, -9, -9, // Decimal 58 - 60
        -1, // Equals sign at decimal 61
        -9, -9, -9, // Decimal 62 - 64
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, // Letters 'A' through 'M'
        24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, // Letters 'N' through 'Z'
        -9, -9, -9, -9, // Decimal 91 - 94
        37, // Underscore at decimal 95
        -9, // Decimal 96
        38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, // Letters 'a' through 'm'
        51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, // Letters 'n' through 'z'
        -9, -9, -9, -9 // Decimal 123 - 126
    };

    /*  ******** D E T E R M I N E W H I C H A L H A B E T ******** */

    /**
     * Returns one of the _SOMETHING_ALPHABET byte arrays depending on the options specified. It's possible, though
     * silly, to specify ORDERED and URLSAFE in which case one of them will be picked, though there is no guarantee as
     * to which one will be picked.
     *
     * @param options the options
     * @return the alphabet
     */
    protected static final byte[] getAlphabet(int options)
    {
        if ((options & URL_SAFE) == URL_SAFE)
        {
            return URL_SAFE_ALPHABET;
        }
        else if ((options & ORDERED) == ORDERED)
        {
            return ORDERED_ALPHABET;
        }
        else
        {
            return STANDARD_ALPHABET;
        }

    }

    /**
     * Returns one of the _SOMETHING_DECODABET byte arrays depending on the options specified. It's possible, though
     * silly, to specify ORDERED and URL_SAFE in which case one of them will be picked, though there is no guarantee as
     * to which one will be picked.
     *
     * @param options the options
     * @return the decodabet
     */
    protected static final byte[] getDecodabet(int options)
    {
        if ((options & URL_SAFE) == URL_SAFE)
        {
            return URL_SAFE_DECODABET;
        }
        else if ((options & ORDERED) == ORDERED)
        {
            return ORDERED_DECODABET;
        }
        else
        {
            return STANDARD_DECODABET;
        }

    }

    /**
     * Instantiates a new base64.
     */
    private Base64()
    {
    }

    /**
     * Encode3to4.
     *
     * @param b4          the b4
     * @param threeBytes  the three bytes
     * @param numSigBytes the num sig bytes
     * @param options     the options
     * @return the byte[]
     */
    protected static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options)
    {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    }

    /**
     * Encode3to4.
     *
     * @param source      the source
     * @param srcOffset   the src offset
     * @param numSigBytes the num sig bytes
     * @param destination the destination
     * @param destOffset  the dest offset
     * @param options     the options
     * @return the byte[]
     */
    protected static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options)
    {
        byte[] alphabet = getAlphabet(options);

        int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0) | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0) | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0); // NOPMD by yshwang on 13. 11. 11 오후 3:33

        switch (numSigBytes)
        {
            case 3:
                destination[destOffset] = alphabet[(inBuff >>> 18)];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = alphabet[(inBuff) & 0x3f];
                return destination;

            case 2:
                destination[destOffset] = alphabet[(inBuff >>> 18)];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = alphabet[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            case 1:
                destination[destOffset] = alphabet[(inBuff >>> 18)];
                destination[destOffset + 1] = alphabet[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = EQUALS_SIGN;
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            default:
                return destination;
        }
    }

    /**
     * Encode object.
     *
     * @param serializableObject the serializable object
     * @return the string
     */
    public static String encodeObject(java.io.Serializable serializableObject)
    {
        return encodeObject(serializableObject, NO_OPTIONS);
    } // end encodeObject

    /**
     * Serializes an object and returns the Base64-encoded version of that serialized object. If the object cannot be
     * serialized or there is another error, the method will return <tt>null</tt>.
     * <p>
     * Valid options:
     *
     * <pre>
     * GZIP: gzip-compresses object before encoding it.
     * DONT_BREAK_LINES: don't break lines at 76 characters
     * &lt;i&gt;Note: Technically, this makes your encoding non-compliant.&lt;/i&gt;
     * </pre>
     * <p>
     * Example: <code>encodeObject( myObj, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeObject( myObj, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param serializableObject The object to encode
     * @param options            Specified options
     * @return The Base64-encoded object
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeObject(java.io.Serializable serializableObject, int options)
    {
        // Streams
        java.io.ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        java.io.ObjectOutputStream oos = null;
        java.util.zip.GZIPOutputStream gzos = null;

        // Isolate options
        int gzip = (options & GZIP); // NOPMD by yshwang on 13. 11. 11 오후 3:33
        // int dontBreakLines = (options & DONT_BREAK_LINES);

        try
        {
            // ObjectOutputStream -> (GZIP) -> Base64 -> ByteArrayOutputStream
            baos = new java.io.ByteArrayOutputStream();
            b64os = new Base64.OutputStream(baos, ENCODE | options);

            // GZip?
            if (gzip == GZIP)
            {
                gzos = new java.util.zip.GZIPOutputStream(b64os);
                oos = new java.io.ObjectOutputStream(gzos);
            } // end if: gzip
            else
            {
                oos = new java.io.ObjectOutputStream(b64os);
            }

            oos.writeObject(serializableObject);
        } // end try
        catch (java.io.IOException e)
        {
            //e.printStackTrace();
            return null;
        } // end catch
        finally
        {
            try
            {
                if (oos != null)
                {
                    oos.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                if (baos != null)
                {
                    baos.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } // end finally

        // Return value according to relevant encoding.
        try
        {
            return new String(baos.toByteArray(), PREFERRED_ENCODING);
        } // end try
        catch (java.io.UnsupportedEncodingException uue)
        {
            return new String(baos.toByteArray());
        } // end catch

    } // end encode

    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Valid options:
     *
     * <pre>
     * GZIP: gzip-compresses object before encoding it.
     * DONT_BREAK_LINES: don't break lines at 76 characters
     * &lt;i&gt;Note: Technically, this makes your encoding non-compliant.&lt;/i&gt;
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param source  The data to convert
     * @param options Specified options
     * @return the string
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeBytes(byte[] source, int options)
    {
        return encodeBytes(source, 0, source.length, options);
    } // end encodeBytes

    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Valid options:
     *
     * <pre>
     * GZIP: gzip-compresses object before encoding it.
     * DONT_BREAK_LINES: don't break lines at 76 characters
     * &lt;i&gt;Note: Technically, this makes your encoding non-compliant.&lt;/i&gt;
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param source  The data to convert
     * @param off     Offset in array where conversion should begin
     * @param len     Length of data to convert
     * @param options Specified options options alphabet type is pulled from this (standard, url-safe, ordered)
     * @return the string
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeBytes(byte[] source, int off, int len, int options)
    {
        // Isolate options
        int dontBreakLines = (options & DONT_BREAK_LINES); // NOPMD by yshwang on 13. 11. 11 오후 3:33
        int gzip = (options & GZIP); // NOPMD by yshwang on 13. 11. 11 오후 3:33

        // Compress?
        if (gzip == GZIP)
        {
            java.io.ByteArrayOutputStream baos = null;
            java.util.zip.GZIPOutputStream gzos = null;
            Base64.OutputStream b64os = null;

            try
            {
                // GZip -> Base64 -> ByteArray
                baos = new java.io.ByteArrayOutputStream();
                b64os = new Base64.OutputStream(baos, ENCODE | options);
                gzos = new java.util.zip.GZIPOutputStream(b64os);

                gzos.write(source, off, len);
                gzos.close();
            } // end try
            catch (java.io.IOException e)
            {
                //e.printStackTrace();
                return null;
            } // end catch
            finally
            {
                try
                {
                    if(gzos != null)
                    {
                        gzos.close();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                try
                {
                    if(b64os != null)
                    {
                        b64os.close();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                try
                {
                    if(baos != null)
                    {
                        baos.close();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            } // end finally

            // Return value according to relevant encoding.
            try
            {
                return new String(baos.toByteArray(), PREFERRED_ENCODING);
            } // end try
            catch (java.io.UnsupportedEncodingException uue)
            {
                return new String(baos.toByteArray());
            } // end catch
        } // end if: compress

        // Else, don't compress. Better not to use streams at all then.
        else
        {
            // Convert option to boolean in way that code likes it.
            boolean breakLines = dontBreakLines == 0;

            int len43 = len * 4 / 3;
            byte[] outBuff = new byte[(len43) // Main 4:3 // NOPMD by yshwang on 13. 11. 11 오후 3:33
                + ((len % 3) > 0 ? 4 : 0) // Account for padding / NOPMD by yshwang on 13. 11. 11 오후 3:33
                + (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)]; // New lines // NOPMD by yshwang on 13. 11. 11 오후 3:33
            int d = 0;
            int e = 0;
            int len2 = len - 2;
            int lineLength = 0;
            for (; d < len2; d += 3, e += 4)
            {
                encode3to4(source, d + off, 3, outBuff, e, options);

                lineLength += 4;
                if (breakLines && lineLength == MAX_LINE_LENGTH)
                {
                    outBuff[e + 4] = NEW_LINE;
                    e++;
                    lineLength = 0;
                } // end if: end of line
            } // en dfor: each piece of array

            if (d < len)
            {
                encode3to4(source, d + off, len - d, outBuff, e, options);
                e += 4;
            } // end if: some padding needed

            // Return value according to relevant encoding.
            try
            {
                return new String(outBuff, 0, e, PREFERRED_ENCODING);
            } // end try
            catch (java.io.UnsupportedEncodingException uue)
            {
                return new String(outBuff, 0, e);
            } // end catch

        } // end else: don't compress

    } // end encodeBytes

    /*  ******** D E C O D I N G M E T H O D S ******** */

    /**
     * Decodes four bytes from array <var>source</var> and writes the resulting bytes (up to three of them) to
     * <var>destination</var>. The source and destination arrays can be manipulated anywhere along their length by
     * specifying <var>srcOffset</var> and <var>destOffset</var>. This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 4 for the <var>source</var> array or <var>destOffset</var>
     * + 3 for the <var>destination</var> array. This method returns the actual number of bytes that were converted from
     * the Base64 encoding.
     * <p>
     * This is the lowest level of the decoding methods with all possible parameters.
     * </p>
     *
     * @param source      the array to convert
     * @param srcOffset   the index where conversion begins
     * @param destination the array to hold the conversion
     * @param destOffset  the index where output will be put
     * @param options     alphabet type is pulled from this (standard, url-safe, ordered)
     * @return the number of decoded bytes converted
     * @since 1.3
     */
    protected static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options)
    {
        byte[] decodabet = getDecodabet(options);

        // Example: Dk==
        if (source[srcOffset + 2] == EQUALS_SIGN)
        {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
            // | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            int outBuff = ((decodabet[source[srcOffset]] & 0xFF) << 18) | ((decodabet[source[srcOffset + 1]] & 0xFF) << 12);

            destination[destOffset] = (byte) (outBuff >>> 16);
            return 1;
        }

        // Example: DkL=
        else if (source[srcOffset + 3] == EQUALS_SIGN)
        {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            int outBuff = ((decodabet[source[srcOffset]] & 0xFF) << 18) | ((decodabet[source[srcOffset + 1]] & 0xFF) << 12) | ((decodabet[source[srcOffset + 2]] & 0xFF) << 6);

            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        }

        // Example: DkLE
        else
        {
            try
            {
                // Two ways to do the same thing. Don't know which way I like best.
                // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
                // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
                // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
                // | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
                int outBuff = ((decodabet[source[srcOffset]] & 0xFF) << 18) | ((decodabet[source[srcOffset + 1]] & 0xFF) << 12) | ((decodabet[source[srcOffset + 2]] & 0xFF) << 6) | ((decodabet[source[srcOffset + 3]] & 0xFF)); // NOPMD by yshwang on 13. 11. 11 오후 3:33

                destination[destOffset] = (byte) (outBuff >> 16);
                destination[destOffset + 1] = (byte) (outBuff >> 8);
                destination[destOffset + 2] = (byte) (outBuff);

                return 3;
            }
            catch (Exception e)
            {
                //System.out.println("" + source[srcOffset] + ": " + (decodabet[source[srcOffset]]));
                //System.out.println("" + source[srcOffset + 1] + ": " + (decodabet[source[srcOffset + 1]]));
                //System.out.println("" + source[srcOffset + 2] + ": " + (decodabet[source[srcOffset + 2]]));
                //System.out.println("" + source[srcOffset + 3] + ": " + (decodabet[source[srcOffset + 3]]));
                return -1;
            } // end catch
        }
    } // end decodeToBytes

    /**
     * Very low-level access to decoding ASCII characters in the form of a byte array. Does not support automatically
     * gunzipping or any other "fancy" features.
     *
     * @param source  The Base64 encoded data
     * @param off     The offset of where to begin decoding
     * @param len     The length of characters to decode
     * @param options the options
     * @return decoded data
     * @since 1.3
     */
    public static byte[] decode(byte[] source, int off, int len, int options)
    {
        byte[] decodabet = getDecodabet(options);

        int len34 = len * 3 / 4;
        byte[] outBuff = new byte[len34]; // Upper limit on size of output
        int outBuffPosn = 0;

        byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;
        for (i = off; i < off + len; i++)
        {
            sbiCrop = (byte) (source[i] & 0x7f); // Only the low seven bits
            sbiDecode = decodabet[sbiCrop];

            if (sbiDecode >= WHITE_SPACE_ENC) // White space, Equals sign or better
            {
                if (sbiDecode >= EQUALS_SIGN_ENC)
                {
                    b4[b4Posn++] = sbiCrop;
                    if (b4Posn > 3)
                    {
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                        b4Posn = 0;

                        // If that was the equals sign, break out of 'for' loop
                        if (sbiCrop == EQUALS_SIGN)
                        {
                            break;
                        }
                    } // end if: quartet built

                } // end if: equals sign or better

            } // end if: white space, equals sign or better
            else
            {
                //System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
                return null;
            } // end else:
        } // each input character

        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    } // end decode

    /**
     * Decodes data from Base64 notation, automatically detecting gzip-compressed data and decompressing it.
     *
     * @param s the string to decode
     * @return the decoded data
     * @since 1.4
     */
    public static byte[] decode(String s)
    {
        return decode(s, NO_OPTIONS);
    }

    /**
     * Decodes data from Base64 notation, automatically detecting gzip-compressed data and decompressing it.
     *
     * @param s       the string to decode
     * @param options encode options such as URL_SAFE
     * @return the decoded data
     * @since 1.4
     */
    public static byte[] decode(String s, int options)
    {
        byte[] bytes;
        try
        {
            bytes = s.getBytes(PREFERRED_ENCODING);
        } // end try
        catch (java.io.UnsupportedEncodingException uee)
        {
            bytes = s.getBytes();
        } // end catch
        // </change>

        // Decode
        bytes = decode(bytes, 0, bytes.length, options);

        // Check to see if it's gzip-compressed
        // GZIP Magic Two-Byte Number: 0x8b1f (35615)
        if (bytes != null && bytes.length >= 4)
        {

            int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
            if (java.util.zip.GZIPInputStream.GZIP_MAGIC == head)
            {
                java.io.ByteArrayInputStream bais = null;
                java.util.zip.GZIPInputStream gzis = null;
                java.io.ByteArrayOutputStream baos = null;
                byte[] buffer = new byte[2048];
                int length = 0;

                try
                {
                    baos = new java.io.ByteArrayOutputStream();
                    bais = new java.io.ByteArrayInputStream(bytes);
                    gzis = new java.util.zip.GZIPInputStream(bais);

                    while ((length = gzis.read(buffer)) >= 0) // NOPMD by yshwang on 13. 11. 11 오후 3:33
                    {
                        baos.write(buffer, 0, length);
                    } // end while: reading input

                    // No error? Get new bytes.
                    bytes = baos.toByteArray();

                } // end try
                catch (java.io.IOException e)
                {
                    e.printStackTrace();
                } // end catch
                finally
                {
                    try
                    {
                        if(baos != null)
                        {
                            baos.close();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        if(gzis != null)
                        {
                            gzis.close();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    try
                    {
                        if(bais != null)
                        {
                            bais.close();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } // end finally

            } // end if: gzipped
        } // end if: bytes.length >= 2

        return bytes;
    } // end decode

    /**
     * A {@link Base64.OutputStream} will write data to another <tt>java.io.OutputStream</tt>, given in the constructor,
     * and encode/decode to/from Base64 notation on the fly.
     *
     * @see Base64
     * @since 1.3
     */
    public static class OutputStream extends java.io.FilterOutputStream
    {

        /** The encode. */
        private final boolean encode;

        /** The position. */
        private int position;

        /** The buffer. */
        private byte[] buffer;

        /** The buffer length. */
        private final int bufferLength;

        /** The line length. */
        private int lineLength;

        /** The break lines. */
        private final boolean breakLines;

        /** The b4. */
        private final byte[] b4; // Scratch used in a few places

        /** The suspend encoding. */
        private boolean suspendEncoding;

        /** The options. */
        private final int options; // Record for later

        /** The alphabet. */
        private byte[] alphabet; // Local copies to avoid extra method calls // NOPMD by kwlee on 09. 2. 17 ���� 5:29

        /** The decodabet. */
        private final byte[] decodabet; // Local copies to avoid extra method calls

        /**
         * Constructs a {@link Base64.OutputStream} in ENCODE mode.
         *
         * @param out the <tt>java.io.OutputStream</tt> to which data will be written.
         * @since 1.3
         */
        public OutputStream(java.io.OutputStream out)
        {
            this(out, ENCODE);
        } // end constructor

        /**
         * Constructs a {@link Base64.OutputStream} in either ENCODE or DECODE mode.
         * <p>
         * Valid options:
         *
         * <pre>
         * ENCODE or DECODE: Encode or Decode as data is read.
         * DONT_BREAK_LINES: don't break lines at 76 characters
         * (only meaningful when encoding)
         * &lt;i&gt;Note: Technically, this makes your encoding non-compliant.&lt;/i&gt;
         * </pre>
         * <p>
         * Example: <code>new Base64.OutputStream( out, Base64.ENCODE )</code>
         *
         * @param out     the <tt>java.io.OutputStream</tt> to which data will be written.
         * @param options Specified options.
         * @see Base64#ENCODE
         * @see Base64#DECODE
         * @see Base64#DONT_BREAK_LINES
         * @since 1.3
         */
        public OutputStream(java.io.OutputStream out, int options)
        {
            super(out);
            this.breakLines = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
            this.encode = (options & ENCODE) == ENCODE;
            this.bufferLength = encode ? 3 : 4;
            this.buffer = new byte[bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.alphabet = getAlphabet(options);
            this.decodabet = getDecodabet(options);
        } // end constructor

        /**
         * Writes the byte to the output stream after converting to/from Base64 notation. When encoding, bytes are
         * buffered three at a time before the output stream actually gets a write() call. When decoding, bytes are
         * buffered four at a time.
         *
         * @param theByte the byte to write
         * @throws java.io.IOException Signals that an I/O exception has occurred.
         * @since 1.3
         */
        public void write(int theByte) throws java.io.IOException
        {
            // Encoding suspended?
            if (suspendEncoding)
            {
                super.out.write(theByte);
                return;
            } // end if: supsended

            // Encode?
            if (encode)
            {
                buffer[position++] = (byte) theByte;
                if (position >= bufferLength) // Enough to encode.
                {
                    out.write(encode3to4(b4, buffer, bufferLength, options));

                    lineLength += 4;
                    if (breakLines && lineLength >= MAX_LINE_LENGTH)
                    {
                        out.write(NEW_LINE);
                        lineLength = 0;
                    } // end if: end of line

                    position = 0;
                } // end if: enough to output
            } // end if: encoding

            // Else, Decoding
            else
            {
                // Meaningful Base64 character?
                if (decodabet[theByte & 0x7f] > WHITE_SPACE_ENC)
                {
                    buffer[position++] = (byte) theByte;
                    if (position >= bufferLength) // Enough to output.
                    {
                        int len = Base64.decode4to3(buffer, 0, b4, 0, options);
                        out.write(b4, 0, len);
                        // out.write( Base64.decode4to3( buffer ) );
                        position = 0;
                    } // end if: enough to output
                } // end if: meaningful base64 character
                else if (decodabet[theByte & 0x7f] != WHITE_SPACE_ENC)
                {
                    throw new java.io.IOException("Invalid character in Base64 data.");
                } // end else: not white space either
            } // end else: decoding
        } // end write

        /**
         * Calls {@link #write(int)} repeatedly until <var>len</var> bytes are written.
         *
         * @param theBytes array from which to read bytes
         * @param off      offset for array
         * @param len      max number of bytes to read into array
         * @throws java.io.IOException Signals that an I/O exception has occurred.
         * @since 1.3
         */
        public void write(byte[] theBytes, int off, int len) throws java.io.IOException
        {
            // Encoding suspended?
            if (suspendEncoding)
            {
                super.out.write(theBytes, off, len);
                return;
            } // end if: supsended

            for (int i = 0; i < len; i++)
            {
                write(theBytes[off + i]);
            } // end for: each byte written

        } // end write

        /**
         * Method added by PHIL. [Thanks, PHIL. -Rob] This pads the buffer without closing the stream.
         *
         * @throws java.io.IOException Signals that an I/O exception has occurred.
         */
        public void flushBase64() throws java.io.IOException
        {
            if (position > 0)
            {
                if (encode)
                {
                    out.write(encode3to4(b4, buffer, position, options));
                    position = 0;
                } // end if: encoding
                else
                {
                    throw new java.io.IOException("Base64 input not properly padded.");
                } // end else: decoding
            } // end if: buffer partially full

        } // end flush

        /**
         * Flushes and closes (I think, in the superclass) the stream.
         *
         * @throws java.io.IOException Signals that an I/O exception has occurred.
         * @since 1.3
         */
        public void close() throws java.io.IOException
        {
            // 1. Ensure that pending characters are written
            flushBase64();

            // 2. Actually close the stream
            // Base class both flushes and closes.
            super.close();

            buffer = null;
            out = null;
        } // end close
    } // end inner class OutputStream

} // end class Base64
