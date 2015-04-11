// (c) 2000 Sun Microsystems, Inc.
// ALL RIGHTS RESERVED
// 
// License Grant-
// 
// Permission to use, copy, modify, and distribute this Software and its 
// documentation for NON-COMMERCIAL or COMMERCIAL purposes and without fee is 
// hereby granted.  
// 
// This Software is provided "AS IS".  All express warranties, including any 
// implied warranty of merchantability, satisfactory quality, fitness for a 
// particular purpose, or non-infringement, are disclaimed, except to the extent 
// that such disclaimers are held to be legally invalid.
// 
// You acknowledge that Software is not designed, licensed or intended for use in 
// the design, construction, operation or maintenance of any nuclear facility 
// ("High Risk Activities").  Sun disclaims any express or implied warranty of 
// fitness for such uses.  
//
// Please refer to the file http://www.sun.com/policies/trademarks/ for further 
// important trademark information and to 
// http://java.sun.com/nav/business/index.html for further important licensing 
// information for the Java Technology.

package lwjake2.util

import java.util.Enumeration
import java.util.Vector
import java.util.Locale
import java.text.DecimalFormatSymbols

/**
 * PrintfFormat allows the formatting of an array of
 * objects embedded within a string.  Primitive types
 * must be passed using wrapper types.  The formatting
 * is controlled by a control string.
 *
 *
 * A control string is a Java string that contains a
 * control specification.  The control specification
 * starts at the first percent sign (%) in the string,
 * provided that this percent sign
 *
 *  1. is not escaped protected by a matching % or is
 * not an escape % character,
 *  1. is not at the end of the format string, and
 *  1. precedes a sequence of characters that parses as
 * a valid control specification.
 *
 *
 *
 * A control specification usually takes the form:
 *  % ['-+ #0]* [0..9]* { . [0..9]* }+
 * { [hlL] }+ [idfgGoxXeEcs]
 *
 * There are variants of this basic form that are
 * discussed below.
 *
 *
 * The format is composed of zero or more directives
 * defined as follows:
 *
 *  * ordinary characters, which are simply copied to
 * the output stream;
 *  * escape sequences, which represent non-graphic
 * characters; and
 *  * conversion specifications,  each of which
 * results in the fetching of zero or more arguments.
 *
 *
 *
 * The results are undefined if there are insufficient
 * arguments for the format.  Usually an unchecked
 * exception will be thrown.  If the format is
 * exhausted while arguments remain, the excess
 * arguments are evaluated but are otherwise ignored.
 * In format strings containing the % form of
 * conversion specifications, each argument in the
 * argument list is used exactly once.
 *
 *
 * Conversions can be applied to the `n`th
 * argument after the format in the argument list,
 * rather than to the next unused argument.  In this
 * case, the conversion characer % is replaced by the
 * sequence %`n`$, where `n` is
 * a decimal integer giving the position of the
 * argument in the argument list.
 *
 *
 * In format strings containing the %`n`$
 * form of conversion specifications, each argument
 * in the argument list is used exactly once.

 * Escape Sequences
 *
 *
 * The following table lists escape sequences and
 * associated actions on display devices capable of
 * the action.
 *
 * Sequence
 * Name
 * Description
 * \\backlashNone.
 *
 * \aalertAttempts to alert
 * the user through audible or visible
 * notification.
 *
 * \bbackspaceMoves the
 * printing position to one column before
 * the current position, unless the
 * current position is the start of a line.
 *
 * \fform-feedMoves the
 * printing position to the initial
 * printing position of the next logical
 * page.
 *
 * \nnewlineMoves the
 * printing position to the start of the
 * next line.
 *
 * \rcarriage-returnMoves
 * the printing position to the start of
 * the current line.
 *
 * \ttabMoves the printing
 * position to the next implementation-
 * defined horizontal tab position.
 *
 * \vvertical-tabMoves the
 * printing position to the start of the
 * next implementation-defined vertical
 * tab position.
 *
 *
 * Conversion Specifications
 *
 *
 * Each conversion specification is introduced by
 * the percent sign character (%).  After the character
 * %, the following appear in sequence:
 *
 *
 * Zero or more flags (in any order), which modify the
 * meaning of the conversion specification.
 *
 *
 * An optional minimum field width.  If the converted
 * value has fewer characters than the field width, it
 * will be padded with spaces by default on the left;
 * t will be padded on the right, if the left-
 * adjustment flag (-), described below, is given to
 * the field width.  The field width takes the form
 * of a decimal integer.  If the conversion character
 * is s, the field width is the the minimum number of
 * characters to be printed.
 *
 *
 * An optional precision that gives the minumum number
 * of digits to appear for the d, i, o, x or X
 * conversions (the field is padded with leading
 * zeros); the number of digits to appear after the
 * radix character for the e, E, and f conversions,
 * the maximum number of significant digits for the g
 * and G conversions; or the maximum number of
 * characters to be written from a string is s and S
 * conversions.  The precision takes the form of an
 * optional decimal digit string, where a null digit
 * string is treated as 0.  If a precision appears
 * with a c conversion character the precision is
 * ignored.
 *
 *
 *
 * An optional h specifies that a following d, i, o,
 * x, or X conversion character applies to a type
 * short argument (the argument will be promoted
 * according to the integral promotions and its value
 * converted to type short before printing).
 *
 *
 * An optional l (ell) specifies that a following
 * d, i, o, x, or X conversion character applies to a
 * type long argument.
 *
 *
 * A field width or precision may be indicated by an
 * asterisk (*) instead of a digit string.  In this
 * case, an integer argument supplised the field width
 * precision.  The argument that is actually converted
 * is not fetched until the conversion letter is seen,
 * so the the arguments specifying field width or
 * precision must appear before the argument (if any)
 * to be converted.  If the precision argument is
 * negative, it will be changed to zero.  A negative
 * field width argument is taken as a - flag, followed
 * by a positive field width.
 *
 *
 * In format strings containing the %`n`$
 * form of a conversion specification, a field width
 * or precision may be indicated by the sequence
 * *`m`$, where m is a decimal integer
 * giving the position in the argument list (after the
 * format argument) of an integer argument containing
 * the field width or precision.
 *
 *
 * The format can contain either numbered argument
 * specifications (that is, %`n`$ and
 * *`m`$), or unnumbered argument
 * specifications (that is % and *), but normally not
 * both.  The only exception to this is that %% can
 * be mixed with the %`n`$ form.  The
 * results of mixing numbered and unnumbered argument
 * specifications in a format string are undefined.

 * Flag Characters
 *
 *
 * The flags and their meanings are:
 *
 * ' integer portion of the result of a
 * decimal conversion (%i, %d, %f, %g, or %G) will
 * be formatted with thousands' grouping
 * characters.  For other conversions the flag
 * is ignored.  The non-monetary grouping
 * character is used.
 * - result of the conversion is left-justified
 * within the field.  (It will be right-justified
 * if this flag is not specified).
 * + result of a signed conversion always
 * begins with a sign (+ or -).  (It will begin
 * with a sign only when a negative value is
 * converted if this flag is not specified.)
 * &lt;space&gt; If the first character of a
 * signed conversion is not a sign, a space
 * character will be placed before the result.
 * This means that if the space character and +
 * flags both appear, the space flag will be
 * ignored.
 * # value is to be converted to an alternative
 * form.  For c, d, i, and s conversions, the flag
 * has no effect.  For o conversion, it increases
 * the precision to force the first digit of the
 * result to be a zero.  For x or X conversion, a
 * non-zero result has 0x or 0X prefixed to it,
 * respectively.  For e, E, f, g, and G
 * conversions, the result always contains a radix
 * character, even if no digits follow the radix
 * character (normally, a decimal point appears in
 * the result of these conversions only if a digit
 * follows it).  For g and G conversions, trailing
 * zeros will not be removed from the result as
 * they normally are.
 * 0 d, i, o, x, X, e, E, f, g, and G
 * conversions, leading zeros (following any
 * indication of sign or base) are used to pad to
 * the field width;  no space padding is
 * performed.  If the 0 and - flags both appear,
 * the 0 flag is ignored.  For d, i, o, x, and X
 * conversions, if a precision is specified, the
 * 0 flag will be ignored. For c conversions,
 * the flag is ignored.
 *

 * Conversion Characters
 *
 *
 * Each conversion character results in fetching zero
 * or more arguments.  The results are undefined if
 * there are insufficient arguments for the format.
 * Usually, an unchecked exception will be thrown.
 * If the format is exhausted while arguments remain,
 * the excess arguments are ignored.

 *
 *
 * The conversion characters and their meanings are:
 *
 *
 * d,iThe int argument is converted to a
 * signed decimal in the style [-]dddd.  The
 * precision specifies the minimum number of
 * digits to appear;  if the value being
 * converted can be represented in fewer
 * digits, it will be expanded with leading
 * zeros.  The default precision is 1.  The
 * result of converting 0 with an explicit
 * precision of 0 is no characters.
 * o The int argument is converted to unsigned
 * octal format in the style ddddd.  The
 * precision specifies the minimum number of
 * digits to appear;  if the value being
 * converted can be represented in fewer
 * digits, it will be expanded with leading
 * zeros.  The default precision is 1.  The
 * result of converting 0 with an explicit
 * precision of 0 is no characters.
 * x The int argument is converted to unsigned
 * hexadecimal format in the style dddd;  the
 * letters abcdef are used.  The precision
 * specifies the minimum numberof digits to
 * appear; if the value being converted can be
 * represented in fewer digits, it will be
 * expanded with leading zeros.  The default
 * precision is 1.  The result of converting 0
 * with an explicit precision of 0 is no
 * characters.
 * X Behaves the same as the x conversion
 * character except that letters ABCDEF are
 * used instead of abcdef.
 * f The floating point number argument is
 * written in decimal notation in the style
 * [-]ddd.ddd, where the number of digits after
 * the radix character (shown here as a decimal
 * point) is equal to the precision
 * specification.  A Locale is used to determine
 * the radix character to use in this format.
 * If the precision is omitted from the
 * argument, six digits are written after the
 * radix character;  if the precision is
 * explicitly 0 and the # flag is not specified,
 * no radix character appears.  If a radix
 * character appears, at least 1 digit appears
 * before it.  The value is rounded to the
 * appropriate number of digits.
 * e,EThe floating point number argument is
 * written in the style [-]d.ddde{+-}dd
 * (the symbols {+-} indicate either a plus or
 * minus sign), where there is one digit before
 * the radix character (shown here as a decimal
 * point) and the number of digits after it is
 * equal to the precision.  A Locale is used to
 * determine the radix character to use in this
 * format.  When the precision is missing, six
 * digits are written after the radix character;
 * if the precision is 0 and the # flag is not
 * specified, no radix character appears.  The
 * E conversion will produce a number with E
 * instead of e introducing the exponent.  The
 * exponent always contains at least two digits.
 * However, if the value to be written requires
 * an exponent greater than two digits,
 * additional exponent digits are written as
 * necessary.  The value is rounded to the
 * appropriate number of digits.
 * g,GThe floating point number argument is
 * written in style f or e (or in sytle E in the
 * case of a G conversion character), with the
 * precision specifying the number of
 * significant digits.  If the precision is
 * zero, it is taken as one.  The style used
 * depends on the value converted:  style e
 * (or E) will be used only if the exponent
 * resulting from the conversion is less than
 * -4 or greater than or equal to the precision.
 * Trailing zeros are removed from the result.
 * A radix character appears only if it is
 * followed by a digit.
 * c,CThe integer argument is converted to a
 * char and the result is written.

 * s,SThe argument is taken to be a string and
 * bytes from the string are written until the
 * end of the string or the number of bytes
 * indicated by the precision specification of
 * the argument is reached.  If the precision
 * is omitted from the argument, it is taken to
 * be infinite, so all characters up to the end
 * of the string are written.
 * %Write a % character;  no argument is
 * converted.
 *
 *
 *
 * If a conversion specification does not match one of
 * the above forms, an IllegalArgumentException is
 * thrown and the instance of PrintfFormat is not
 * created.
 *
 *
 * If a floating point value is the internal
 * representation for infinity, the output is
 * [+]Infinity, where Infinity is either Infinity or
 * Inf, depending on the desired output string length.
 * Printing of the sign follows the rules described
 * above.
 *
 *
 * If a floating point value is the internal
 * representation for "not-a-number," the output is
 * [+]NaN.  Printing of the sign follows the rules
 * described above.
 *
 *
 * In no case does a non-existent or small field width
 * cause truncation of a field;  if the result of a
 * conversion is wider than the field width, the field
 * is simply expanded to contain the conversion result.
 *
 *
 *
 * The behavior is like printf.  One exception is that
 * the minimum number of exponent digits is 3 instead
 * of 2 for e and E formats when the optional L is used
 * before the e, E, g, or G conversion character.  The
 * optional L does not imply conversion to a long long
 * double.
 *
 *
 * The biggest divergence from the C printf
 * specification is in the use of 16 bit characters.
 * This allows the handling of characters beyond the
 * small ASCII character set and allows the utility to
 * interoperate correctly with the rest of the Java
 * runtime environment.
 *
 *
 * Omissions from the C printf specification are
 * numerous.  All the known omissions are present
 * because Java never uses bytes to represent
 * characters and does not have pointers:
 *
 *  * %c is the same as %C.
 *  * %s is the same as %S.
 *  * u, p, and n conversion characters.
 *  * %ws format.
 *  * h modifier applied to an n conversion character.
 *  * l (ell) modifier applied to the c, n, or s
 * conversion characters.
 *  * ll (ell ell) modifier to d, i, o, u, x, or X
 * conversion characters.
 *  * ll (ell ell) modifier to an n conversion
 * character.
 *  * c, C, d,i,o,u,x, and X conversion characters
 * apply to Byte, Character, Short, Integer, Long
 * types.
 *  * f, e, E, g, and G conversion characters apply
 * to Float and Double types.
 *  * s and S conversion characters apply to String
 * types.
 *  * All other reference types can be formatted
 * using the s or S conversion characters only.
 *
 *
 *
 * Most of this specification is quoted from the Unix
 * man page for the sprintf utility.

 * @author Allan Jacobs
* *
 * @version 1
* * Release 1: Initial release.
* * Release 2: Asterisk field widths and precisions
* *            %n$ and *m$
* *            Bug fixes
* *              g format fix (2 digits in e form corrupt)
* *              rounding in f format implemented
* *              round up when digit not printed is 5
* *              formatting of -0.0f
* *              round up/down when last digits are 50000...
 */
public class PrintfFormat
/**
 * Constructs an array of control specifications
 * possibly preceded, separated, or followed by
 * ordinary strings.  Control strings begin with
 * unpaired percent signs.  A pair of successive
 * percent signs designates a single percent sign in
 * the format.
 * @param fmtArg  Control string.
* *
 * @exception IllegalArgumentException if the control
* * string is null, zero length, or otherwise
* * malformed.
 */
[throws(javaClass<IllegalArgumentException>())]
(locale: Locale, fmtArg: String) {
    /**
     * Constructs an array of control specifications
     * possibly preceded, separated, or followed by
     * ordinary strings.  Control strings begin with
     * unpaired percent signs.  A pair of successive
     * percent signs designates a single percent sign in
     * the format.
     * @param fmtArg  Control string.
    * *
     * @exception IllegalArgumentException if the control
    * * string is null, zero length, or otherwise
    * * malformed.
     */
    throws(javaClass<IllegalArgumentException>())
    public constructor(fmtArg: String) : this(Locale.getDefault(), fmtArg) {
    }

    {
        dfs = DecimalFormatSymbols(locale)
        var ePos = 0
        var sFmt: ConversionSpecification? = null
        var unCS: String? = this.nonControl(fmtArg, 0)
        if (unCS != null) {
            sFmt = ConversionSpecification()
            sFmt!!.setLiteral(unCS)
            vFmt.addElement(sFmt)
        }
        while (cPos != -1 && cPos < fmtArg.length()) {
            run {
                ePos = cPos + 1
                while (ePos < fmtArg.length()) {
                    var c: Char = 0
                    c = fmtArg.charAt(ePos)
                    if (c == 'i')
                        break
                    if (c == 'd')
                        break
                    if (c == 'f')
                        break
                    if (c == 'g')
                        break
                    if (c == 'G')
                        break
                    if (c == 'o')
                        break
                    if (c == 'x')
                        break
                    if (c == 'X')
                        break
                    if (c == 'e')
                        break
                    if (c == 'E')
                        break
                    if (c == 'c')
                        break
                    if (c == 's')
                        break
                    if (c == '%')
                        break
                    ePos++
                }
            }
            ePos = Math.min(ePos + 1, fmtArg.length())
            sFmt = ConversionSpecification(fmtArg.substring(cPos, ePos))
            vFmt.addElement(sFmt)
            unCS = this.nonControl(fmtArg, ePos)
            if (unCS != null) {
                sFmt = ConversionSpecification()
                sFmt!!.setLiteral(unCS)
                vFmt.addElement(sFmt)
            }
        }
    }

    /**
     * Return a substring starting at
     * `start` and ending at either the end
     * of the String `s`, the next unpaired
     * percent sign, or at the end of the String if the
     * last character is a percent sign.
     * @param s  Control string.
    * *
     * @param start Position in the string
    * *     `s` to begin looking for the start
    * *     of a control string.
    * *
     * @return the substring from the start position
    * *     to the beginning of the control string.
     */
    private fun nonControl(s: String, start: Int): String {
        // String ret = "";
        cPos = s.indexOf("%", start)
        if (cPos == -1)
            cPos = s.length()
        return s.substring(start, cPos)
    }

    /**
     * Format an array of objects.  Byte, Short,
     * Integer, Long, Float, Double, and Character
     * arguments are treated as wrappers for primitive
     * types.
     * @param o The array of objects to format.
    * *
     * @return  The formatted String.
     */
    public fun sprintf(o: Array<Object>): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        var i = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else {
                if (cs!!.isPositionalSpecification()) {
                    i = cs!!.argumentPosition - 1
                    if (cs!!.isPositionalFieldWidth()) {
                        val ifw = cs!!.argumentPositionForFieldWidth - 1
                        cs!!.setFieldWidthWithArg((o[ifw] as Integer).intValue())
                    }
                    if (cs!!.isPositionalPrecision()) {
                        val ipr = cs!!.argumentPositionForPrecision - 1
                        cs!!.setPrecisionWithArg((o[ipr] as Integer).intValue())
                    }
                } else {
                    if (cs!!.isVariableFieldWidth()) {
                        cs!!.setFieldWidthWithArg((o[i] as Integer).intValue())
                        i++
                    }
                    if (cs!!.isVariablePrecision()) {
                        cs!!.setPrecisionWithArg((o[i] as Integer).intValue())
                        i++
                    }
                }
                if (o[i] is Byte)
                    sb.append(cs!!.internalsprintf((o[i] as Byte).byteValue()))
                else if (o[i] is Short)
                    sb.append(cs!!.internalsprintf((o[i] as Short).shortValue()))
                else if (o[i] is Integer)
                    sb.append(cs!!.internalsprintf((o[i] as Integer).intValue()))
                else if (o[i] is Long)
                    sb.append(cs!!.internalsprintf((o[i] as Long).longValue()))
                else if (o[i] is Float)
                    sb.append(cs!!.internalsprintf((o[i] as Float).floatValue()))
                else if (o[i] is Double)
                    sb.append(cs!!.internalsprintf((o[i] as Double).doubleValue()))
                else if (o[i] is Character)
                    sb.append(cs!!.internalsprintf((o[i] as Character).charValue()))
                else if (o[i] is String)
                    sb.append(cs!!.internalsprintf(o[i] as String))
                else
                    sb.append(cs!!.internalsprintf(o[i]))
                if (!cs!!.isPositionalSpecification())
                    i++
            }
        }
        return sb.toString()
    }

    /**
     * Format nothing.  Just use the control string.
     * @return  the formatted String.
     */
    public fun sprintf(): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
        }
        return sb.toString()
    }

    /**
     * Format an int.
     * @param x The int to format.
    * *
     * @return  The formatted String.
    * *
     * @exception IllegalArgumentException if the
    * *     conversion character is f, e, E, g, G, s,
    * *     or S.
     */
    throws(javaClass<IllegalArgumentException>())
    public fun sprintf(x: Int): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else
                sb.append(cs!!.internalsprintf(x))
        }
        return sb.toString()
    }

    /**
     * Format an long.
     * @param x The long to format.
    * *
     * @return  The formatted String.
    * *
     * @exception IllegalArgumentException if the
    * *     conversion character is f, e, E, g, G, s,
    * *     or S.
     */
    throws(javaClass<IllegalArgumentException>())
    public fun sprintf(x: Long): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else
                sb.append(cs!!.internalsprintf(x))
        }
        return sb.toString()
    }

    /**
     * Format a double.
     * @param x The double to format.
    * *
     * @return  The formatted String.
    * *
     * @exception IllegalArgumentException if the
    * *     conversion character is c, C, s, S,
    * *     d, d, x, X, or o.
     */
    throws(javaClass<IllegalArgumentException>())
    public fun sprintf(x: Double): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else
                sb.append(cs!!.internalsprintf(x))
        }
        return sb.toString()
    }

    /**
     * Format a String.
     * @param x The String to format.
    * *
     * @return  The formatted String.
    * *
     * @exception IllegalArgumentException if the
    * *   conversion character is neither s nor S.
     */
    throws(javaClass<IllegalArgumentException>())
    public fun sprintf(x: String): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else
                sb.append(cs!!.internalsprintf(x))
        }
        return sb.toString()
    }

    /**
     * Format an Object.  Convert wrapper types to
     * their primitive equivalents and call the
     * appropriate internal formatting method. Convert
     * Strings using an internal formatting method for
     * Strings. Otherwise use the default formatter
     * (use toString).
     * @param x the Object to format.
    * *
     * @return  the formatted String.
    * *
     * @exception IllegalArgumentException if the
    * *    conversion character is inappropriate for
    * *    formatting an unwrapped value.
     */
    throws(javaClass<IllegalArgumentException>())
    public fun sprintf(x: Object): String {
        val e = vFmt.elements()
        var cs: ConversionSpecification? = null
        var c: Char = 0
        val sb = StringBuffer()
        while (e.hasMoreElements()) {
            cs = e.nextElement() as ConversionSpecification
            c = cs!!.conversionCharacter
            if (c == '\0')
                sb.append(cs!!.getLiteral())
            else if (c == '%')
                sb.append("%")
            else {
                if (x is Byte)
                    sb.append(cs!!.internalsprintf((x as Byte).byteValue()))
                else if (x is Short)
                    sb.append(cs!!.internalsprintf((x as Short).shortValue()))
                else if (x is Integer)
                    sb.append(cs!!.internalsprintf((x as Integer).intValue()))
                else if (x is Long)
                    sb.append(cs!!.internalsprintf((x as Long).longValue()))
                else if (x is Float)
                    sb.append(cs!!.internalsprintf((x as Float).floatValue()))
                else if (x is Double)
                    sb.append(cs!!.internalsprintf((x as Double).doubleValue()))
                else if (x is Character)
                    sb.append(cs!!.internalsprintf((x as Character).charValue()))
                else if (x is String)
                    sb.append(cs!!.internalsprintf(x as String))
                else
                    sb.append(cs!!.internalsprintf(x))
            }
        }
        return sb.toString()
    }

    /**
     *
     *
     * ConversionSpecification allows the formatting of
     * a single primitive or object embedded within a
     * string.  The formatting is controlled by a
     * format string.  Only one Java primitive or
     * object can be formatted at a time.
     *
     *
     * A format string is a Java string that contains
     * a control string.  The control string starts at
     * the first percent sign (%) in the string,
     * provided that this percent sign
     *
     *  1. is not escaped protected by a matching % or
     * is not an escape % character,
     *  1. is not at the end of the format string, and
     *  1. precedes a sequence of characters that parses
     * as a valid control string.
     *
     *
     *
     * A control string takes the form:
     *  % ['-+ #0]* [0..9]* { . [0..9]* }+
     * { [hlL] }+ [idfgGoxXeEcs]
     *
     *
     *
     * The behavior is like printf.  One (hopefully the
     * only) exception is that the minimum number of
     * exponent digits is 3 instead of 2 for e and E
     * formats when the optional L is used before the
     * e, E, g, or G conversion character.  The
     * optional L does not imply conversion to a long
     * long double.
     */
    private inner class ConversionSpecification {
        /**
         * Constructor.  Used to prepare an instance
         * to hold a literal, not a control string.
         */
        constructor() {
        }

        /**
         * Constructor for a conversion specification.
         * The argument must begin with a % and end
         * with the conversion character for the
         * conversion specification.
         * @param fmtArg  String specifying the
        * *     conversion specification.
        * *
         * @exception IllegalArgumentException if the
        * *     input string is null, zero length, or
        * *     otherwise malformed.
         */
        throws(javaClass<IllegalArgumentException>())
        constructor(fmtArg: String?) {
            if (fmtArg == null)
                throw NullPointerException()
            if (fmtArg.length() == 0)
                throw IllegalArgumentException("Control strings must have positive" + " lengths.")
            if (fmtArg.charAt(0) == '%') {
                fmt = fmtArg
                pos = 1
                setArgPosition()
                setFlagCharacters()
                setFieldWidth()
                setPrecision()
                setOptionalHL()
                if (setConversionCharacter()) {
                    if (pos == fmtArg.length()) {
                        if (leadingZeros && leftJustify)
                            leadingZeros = false
                        if (precisionSet && leadingZeros) {
                            if (conversionCharacter == 'd' || conversionCharacter == 'i' || conversionCharacter == 'o' || conversionCharacter == 'x') {
                                leadingZeros = false
                            }
                        }
                    } else
                        throw IllegalArgumentException("Malformed conversion specification=" + fmtArg)
                } else
                    throw IllegalArgumentException("Malformed conversion specification=" + fmtArg)
            } else
                throw IllegalArgumentException("Control strings must begin with %.")
        }

        /**
         * Set the String for this instance.
         * @param s the String to store.
         */
        fun setLiteral(s: String) {
            fmt = s
        }

        /**
         * Get the String for this instance.  Translate
         * any escape sequences.

         * @return s the stored String.
         */
        fun getLiteral(): String {
            val sb = StringBuffer()
            var i = 0
            while (i < fmt!!.length()) {
                if (fmt!!.charAt(i) == '\\') {
                    i++
                    if (i < fmt!!.length()) {
                        val c = fmt!!.charAt(i)
                        when (c) {
                            'a' -> sb.append(7.toChar())
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\f')
                            'n' -> sb.append(System.getProperty("line.separator"))
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'v' -> sb.append(11.toChar())
                            '\\' -> sb.append('\\')
                        }
                        i++
                    } else
                        sb.append('\\')
                } else
                    i++
            }
            return fmt
        }

        /**
         * Check whether the specifier has a variable
         * field width that is going to be set by an
         * argument.
         * @return `true` if the conversion
        * *   uses an * field width; otherwise
        * *   `false`.
         */
        fun isVariableFieldWidth(): Boolean {
            return variableFieldWidth
        }

        /**
         * Set the field width with an argument.  A
         * negative field width is taken as a - flag
         * followed by a positive field width.
         * @param fw the field width.
         */
        fun setFieldWidthWithArg(fw: Int) {
            if (fw < 0)
                leftJustify = true
            fieldWidthSet = true
            fieldWidth = Math.abs(fw)
        }

        /**
         * Check whether the specifier has a variable
         * precision that is going to be set by an
         * argument.
         * @return `true` if the conversion
        * *   uses an * precision; otherwise
        * *   `false`.
         */
        fun isVariablePrecision(): Boolean {
            return variablePrecision
        }

        /**
         * Set the precision with an argument.  A
         * negative precision will be changed to zero.
         * @param pr the precision.
         */
        fun setPrecisionWithArg(pr: Int) {
            precisionSet = true
            precision = Math.max(pr, 0)
        }

        /**
         * Format an int argument using this conversion
         * specification.
         * @param s the int to format.
        * *
         * @return the formatted String.
        * *
         * @exception IllegalArgumentException if the
        * *     conversion character is f, e, E, g, or G.
         */
        throws(javaClass<IllegalArgumentException>())
        fun internalsprintf(s: Int): String {
            var s2 = ""
            when (conversionCharacter) {
                'd', 'i' -> if (optionalh)
                    s2 = printDFormat(s.toShort())
                else if (optionall)
                    s2 = printDFormat(s.toLong())
                else
                    s2 = printDFormat(s)
                'x', 'X' -> if (optionalh)
                    s2 = printXFormat(s.toShort())
                else if (optionall)
                    s2 = printXFormat(s.toLong())
                else
                    s2 = printXFormat(s)
                'o' -> if (optionalh)
                    s2 = printOFormat(s.toShort())
                else if (optionall)
                    s2 = printOFormat(s.toLong())
                else
                    s2 = printOFormat(s)
                'c', 'C' -> s2 = printCFormat(s.toChar())
                else -> throw IllegalArgumentException("Cannot format a int with a format using a " + conversionCharacter + " conversion character.")
            }
            return s2
        }

        /**
         * Format a long argument using this conversion
         * specification.
         * @param s the long to format.
        * *
         * @return the formatted String.
        * *
         * @exception IllegalArgumentException if the
        * *     conversion character is f, e, E, g, or G.
         */
        throws(javaClass<IllegalArgumentException>())
        fun internalsprintf(s: Long): String {
            var s2 = ""
            when (conversionCharacter) {
                'd', 'i' -> if (optionalh)
                    s2 = printDFormat(s.toShort())
                else if (optionall)
                    s2 = printDFormat(s)
                else
                    s2 = printDFormat(s.toInt())
                'x', 'X' -> if (optionalh)
                    s2 = printXFormat(s.toShort())
                else if (optionall)
                    s2 = printXFormat(s)
                else
                    s2 = printXFormat(s.toInt())
                'o' -> if (optionalh)
                    s2 = printOFormat(s.toShort())
                else if (optionall)
                    s2 = printOFormat(s)
                else
                    s2 = printOFormat(s.toInt())
                'c', 'C' -> s2 = printCFormat(s.toChar())
                else -> throw IllegalArgumentException("Cannot format a long with a format using a " + conversionCharacter + " conversion character.")
            }
            return s2
        }

        /**
         * Format a double argument using this conversion
         * specification.
         * @param s the double to format.
        * *
         * @return the formatted String.
        * *
         * @exception IllegalArgumentException if the
        * *     conversion character is c, C, s, S, i, d,
        * *     x, X, or o.
         */
        throws(javaClass<IllegalArgumentException>())
        fun internalsprintf(s: Double): String {
            var s2 = ""
            when (conversionCharacter) {
                'f' -> s2 = printFFormat(s)
                'E', 'e' -> s2 = printEFormat(s)
                'G', 'g' -> s2 = printGFormat(s)
                else -> throw IllegalArgumentException("Cannot " + "format a double with a format using a " + conversionCharacter + " conversion character.")
            }
            return s2
        }

        /**
         * Format a String argument using this conversion
         * specification.
         * @param s the String to format.
        * *
         * @return the formatted String.
        * *
         * @exception IllegalArgumentException if the
        * *   conversion character is neither s nor S.
         */
        throws(javaClass<IllegalArgumentException>())
        fun internalsprintf(s: String): String {
            var s2 = ""
            if (conversionCharacter == 's' || conversionCharacter == 'S')
                s2 = printSFormat(s)
            else
                throw IllegalArgumentException("Cannot " + "format a String with a format using a " + conversionCharacter + " conversion character.")
            return s2
        }

        /**
         * Format an Object argument using this conversion
         * specification.
         * @param s the Object to format.
        * *
         * @return the formatted String.
        * *
         * @exception IllegalArgumentException if the
        * *     conversion character is neither s nor S.
         */
        fun internalsprintf(s: Object): String {
            var s2 = ""
            if (conversionCharacter == 's' || conversionCharacter == 'S')
                s2 = printSFormat(s.toString())
            else
                throw IllegalArgumentException("Cannot format a String with a format using" + " a " + conversionCharacter + " conversion character.")
            return s2
        }

        /**
         * For f format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both
         * a '+' and a ' ' are specified, the blank flag
         * is ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the number of digits
         * to appear after the radix character.  Padding is
         * with trailing 0s.
         */
        private fun fFormatDigits(x: Double): CharArray {
            // int defaultDigits=6;
            var sx: String // sxOut;
            var i: Int
            var j: Int
            var k: Int
            val n1In: Int
            val n2In: Int
            var expon = 0
            var minusSign = false
            if (x > 0.0)
                sx = Double.toString(x)
            else if (x < 0.0) {
                sx = Double.toString(-x)
                minusSign = true
            } else {
                sx = Double.toString(x)
                if (sx.charAt(0) == '-') {
                    minusSign = true
                    sx = sx.substring(1)
                }
            }
            val ePos = sx.indexOf('E')
            val rPos = sx.indexOf('.')
            if (rPos != -1)
                n1In = rPos
            else if (ePos != -1)
                n1In = ePos
            else
                n1In = sx.length()
            if (rPos != -1) {
                if (ePos != -1)
                    n2In = ePos - rPos - 1
                else
                    n2In = sx.length() - rPos - 1
            } else
                n2In = 0
            if (ePos != -1) {
                var ie = ePos + 1
                expon = 0
                if (sx.charAt(ie) == '-') {
                    run {
                        ++ie
                        while (ie < sx.length()) {
                            if (sx.charAt(ie) != '0')
                                break
                            ie++
                        }
                    }
                    if (ie < sx.length())
                        expon = -Integer.parseInt(sx.substring(ie))
                } else {
                    if (sx.charAt(ie) == '+')
                        ++ie
                    while (ie < sx.length()) {
                        if (sx.charAt(ie) != '0')
                            break
                        ie++
                    }
                    if (ie < sx.length())
                        expon = Integer.parseInt(sx.substring(ie))
                }
            }
            val p: Int
            if (precisionSet)
                p = precision
            else
                p = defaultDigits - 1
            val ca1 = sx.toCharArray()
            val ca2 = CharArray(n1In + n2In)
            val ca3: CharArray
            val ca4: CharArray
            val ca5: CharArray
            run {
                j = 0
                while (j < n1In) {
                    ca2[j] = ca1[j]
                    j++
                }
            }
            i = j + 1
            run {
                k = 0
                while (k < n2In) {
                    ca2[j] = ca1[i]
                    j++
                    i++
                    k++
                }
            }
            if (n1In + expon <= 0) {
                ca3 = CharArray(-expon + n2In)
                run {
                    j = 0
                    k = 0
                    while (k < (-n1In - expon)) {
                        ca3[j] = '0'
                        k++
                        j++
                    }
                }
                run {
                    i = 0
                    while (i < (n1In + n2In)) {
                        ca3[j] = ca2[i]
                        i++
                        j++
                    }
                }
            } else
                ca3 = ca2
            var carry = false
            if (p < -expon + n2In) {
                if (expon < 0)
                    i = p
                else
                    i = p + n1In
                carry = checkForCarry(ca3, i)
                if (carry)
                    carry = startSymbolicCarry(ca3, i - 1, 0)
            }
            if (n1In + expon <= 0) {
                ca4 = CharArray(2 + p)
                if (!carry)
                    ca4[0] = '0'
                else
                    ca4[0] = '1'
                if (alternateForm || !precisionSet || precision != 0) {
                    ca4[1] = '.'
                    run {
                        i = 0
                        j = 2
                        while (i < Math.min(p, ca3.size())) {
                            ca4[j] = ca3[i]
                            i++
                            j++
                        }
                    }
                    while (j < ca4.size()) {
                        ca4[j] = '0'
                        j++
                    }
                }
            } else {
                if (!carry) {
                    if (alternateForm || !precisionSet || precision != 0)
                        ca4 = CharArray(n1In + expon + p + 1)
                    else
                        ca4 = CharArray(n1In + expon)
                    j = 0
                } else {
                    if (alternateForm || !precisionSet || precision != 0)
                        ca4 = CharArray(n1In + expon + p + 2)
                    else
                        ca4 = CharArray(n1In + expon + 1)
                    ca4[0] = '1'
                    j = 1
                }
                run {
                    i = 0
                    while (i < Math.min(n1In + expon, ca3.size())) {
                        ca4[j] = ca3[i]
                        i++
                        j++
                    }
                }
                while (i < n1In + expon) {
                    ca4[j] = '0'
                    i++
                    j++
                }
                if (alternateForm || !precisionSet || precision != 0) {
                    ca4[j] = '.'
                    j++
                    run {
                        k = 0
                        while (i < ca3.size() && k < p) {
                            ca4[j] = ca3[i]
                            i++
                            j++
                            k++
                        }
                    }
                    while (j < ca4.size()) {
                        ca4[j] = '0'
                        j++
                    }
                }
            }
            var nZeros = 0
            if (!leftJustify && leadingZeros) {
                var xThousands = 0
                if (thousands) {
                    var xlead = 0
                    if (ca4[0] == '+' || ca4[0] == '-' || ca4[0] == ' ')
                        xlead = 1
                    var xdp = xlead
                    while (xdp < ca4.size()) {
                        if (ca4[xdp] == '.')
                            break
                        xdp++
                    }
                    xThousands = (xdp - xlead) / 3
                }
                if (fieldWidthSet)
                    nZeros = fieldWidth - ca4.size()
                if ((!minusSign && (leadingSign || leadingSpace)) || minusSign)
                    nZeros--
                nZeros -= xThousands
                if (nZeros < 0)
                    nZeros = 0
            }
            j = 0
            if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
                ca5 = CharArray(ca4.size() + nZeros + 1)
                j++
            } else
                ca5 = CharArray(ca4.size() + nZeros)
            if (!minusSign) {
                if (leadingSign)
                    ca5[0] = '+'
                if (leadingSpace)
                    ca5[0] = ' '
            } else
                ca5[0] = '-'
            run {
                i = 0
                while (i < nZeros) {
                    ca5[j] = '0'
                    i++
                    j++
                }
            }
            run {
                i = 0
                while (i < ca4.size()) {
                    ca5[j] = ca4[i]
                    i++
                    j++
                }
            }

            var lead = 0
            if (ca5[0] == '+' || ca5[0] == '-' || ca5[0] == ' ')
                lead = 1
            var dp = lead
            while (dp < ca5.size()) {
                if (ca5[dp] == '.')
                    break
                dp++
            }
            val nThousands = (dp - lead) / 3
            // Localize the decimal point.
            if (dp < ca5.size())
                ca5[dp] = dfs!!.getDecimalSeparator()
            var ca6 = ca5
            if (thousands && nThousands > 0) {
                ca6 = CharArray(ca5.size() + nThousands + lead)
                ca6[0] = ca5[0]
                run {
                    i = lead
                    k = lead
                    while (i < dp) {
                        if (i > 0 && (dp - i) % 3 == 0) {
                            // ca6[k]=',';
                            ca6[k] = dfs!!.getGroupingSeparator()
                            ca6[k + 1] = ca5[i]
                            k += 2
                        } else {
                            ca6[k] = ca5[i]
                            k++
                        }
                        i++
                    }
                }
                while (i < ca5.size()) {
                    ca6[k] = ca5[i]
                    i++
                    k++
                }
            }
            return ca6
        }

        /**
         * An intermediate routine on the way to creating
         * an f format String.  The method decides whether
         * the input double value is an infinity,
         * not-a-number, or a finite double and formats
         * each type of input appropriately.
         * @param x the double value to be formatted.
        * *
         * @return the converted double value.
         */
        private fun fFormatString(x: Double): String {
            // boolean noDigits = false;
            val ca6: CharArray
            val ca7: CharArray
            if (Double.isInfinite(x)) {
                if (x == Double.POSITIVE_INFINITY) {
                    if (leadingSign)
                        ca6 = "+Inf".toCharArray()
                    else if (leadingSpace)
                        ca6 = " Inf".toCharArray()
                    else
                        ca6 = "Inf".toCharArray()
                } else
                    ca6 = "-Inf".toCharArray()
                // noDigits = true;
            } else if (Double.isNaN(x)) {
                if (leadingSign)
                    ca6 = "+NaN".toCharArray()
                else if (leadingSpace)
                    ca6 = " NaN".toCharArray()
                else
                    ca6 = "NaN".toCharArray()
                // noDigits = true;
            } else
                ca6 = fFormatDigits(x)
            ca7 = applyFloatPadding(ca6, false)
            return String(ca7)
        }

        /**
         * For e format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both a
         * '+' and a ' ' are specified, the blank flag is
         * ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear after the radix character.
         * Padding is with trailing 0s.

         * The behavior is like printf.  One (hopefully the
         * only) exception is that the minimum number of
         * exponent digits is 3 instead of 2 for e and E
         * formats when the optional L is used before the
         * e, E, g, or G conversion character. The optional
         * L does not imply conversion to a long long
         * double.
         */
        private fun eFormatDigits(x: Double, eChar: Char): CharArray {
            var ca1: CharArray
            val ca2: CharArray
            val ca3: CharArray
            // int defaultDigits=6;
            var sx: String //, sxOut;
            var i: Int
            var j: Int
            var k: Int
            val p: Int
            // int n1In, n2In;
            var expon = 0
            var ePos: Int
            val rPos: Int
            val eSize: Int
            var minusSign = false
            if (x > 0.0)
                sx = Double.toString(x)
            else if (x < 0.0) {
                sx = Double.toString(-x)
                minusSign = true
            } else {
                sx = Double.toString(x)
                if (sx.charAt(0) == '-') {
                    minusSign = true
                    sx = sx.substring(1)
                }
            }
            ePos = sx.indexOf('E')
            if (ePos == -1)
                ePos = sx.indexOf('e')
            rPos = sx.indexOf('.')
            /* Not exactly sure what the point is here - flibit
			if (rPos != -1)
				n1In = rPos;
			else if (ePos != -1)
				n1In = ePos;
			else
				n1In = sx.length();
			if (rPos != -1) {
				if (ePos != -1)
					n2In = ePos - rPos - 1;
				else
					n2In = sx.length() - rPos - 1;
			} else
				n2In = 0;
			*/
            if (ePos != -1) {
                var ie = ePos + 1
                expon = 0
                if (sx.charAt(ie) == '-') {
                    run {
                        ++ie
                        while (ie < sx.length()) {
                            if (sx.charAt(ie) != '0')
                                break
                            ie++
                        }
                    }
                    if (ie < sx.length())
                        expon = -Integer.parseInt(sx.substring(ie))
                } else {
                    if (sx.charAt(ie) == '+')
                        ++ie
                    while (ie < sx.length()) {
                        if (sx.charAt(ie) != '0')
                            break
                        ie++
                    }
                    if (ie < sx.length())
                        expon = Integer.parseInt(sx.substring(ie))
                }
            }
            if (rPos != -1)
                expon += rPos - 1
            if (precisionSet)
                p = precision
            else
                p = defaultDigits - 1
            if (rPos != -1 && ePos != -1)
                ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1, ePos)).toCharArray()
            else if (rPos != -1)
                ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1)).toCharArray()
            else if (ePos != -1)
                ca1 = sx.substring(0, ePos).toCharArray()
            else
                ca1 = sx.toCharArray()
            var carry = false
            var i0 = 0
            if (ca1[0] != '0')
                i0 = 0
            else
                run {
                    i0 = 0
                    while (i0 < ca1.size()) {
                        if (ca1[i0] != '0')
                            break
                        i0++
                    }
                }
            if (i0 + p < ca1.size() - 1) {
                carry = checkForCarry(ca1, i0 + p + 1)
                if (carry)
                    carry = startSymbolicCarry(ca1, i0 + p, i0)
                if (carry) {
                    ca2 = CharArray(i0 + p + 1)
                    ca2[i0] = '1'
                    run {
                        j = 0
                        while (j < i0) {
                            ca2[j] = '0'
                            j++
                        }
                    }
                    run {
                        i = i0
                        j = i0 + 1
                        while (j < p + 1) {
                            ca2[j] = ca1[i]
                            i++
                            j++
                        }
                    }
                    expon++
                    ca1 = ca2
                }
            }
            if (Math.abs(expon) < 100 && !optionalL)
                eSize = 4
            else
                eSize = 5
            if (alternateForm || !precisionSet || precision != 0)
                ca2 = CharArray(2 + p + eSize)
            else
                ca2 = CharArray(1 + eSize)
            if (ca1[0] != '0') {
                ca2[0] = ca1[0]
                j = 1
            } else {
                run {
                    j = 1
                    while (j < (if (ePos == -1) ca1.size() else ePos)) {
                        if (ca1[j] != '0')
                            break
                        j++
                    }
                }
                if ((ePos != -1 && j < ePos) || (ePos == -1 && j < ca1.size())) {
                    ca2[0] = ca1[j]
                    expon -= j
                    j++
                } else {
                    ca2[0] = '0'
                    j = 2
                }
            }
            if (alternateForm || !precisionSet || precision != 0) {
                ca2[1] = '.'
                i = 2
            } else
                i = 1
            run {
                k = 0
                while (k < p && j < ca1.size()) {
                    ca2[i] = ca1[j]
                    j++
                    i++
                    k++
                }
            }
            while (i < ca2.size() - eSize) {
                ca2[i] = '0'
                i++
            }
            ca2[i++] = eChar
            if (expon < 0)
                ca2[i++] = '-'
            else
                ca2[i++] = '+'
            expon = Math.abs(expon)
            if (expon >= 100) {
                when (expon / 100) {
                    1 -> ca2[i] = '1'
                    2 -> ca2[i] = '2'
                    3 -> ca2[i] = '3'
                    4 -> ca2[i] = '4'
                    5 -> ca2[i] = '5'
                    6 -> ca2[i] = '6'
                    7 -> ca2[i] = '7'
                    8 -> ca2[i] = '8'
                    9 -> ca2[i] = '9'
                }
                i++
            }
            when ((expon % 100) / 10) {
                0 -> ca2[i] = '0'
                1 -> ca2[i] = '1'
                2 -> ca2[i] = '2'
                3 -> ca2[i] = '3'
                4 -> ca2[i] = '4'
                5 -> ca2[i] = '5'
                6 -> ca2[i] = '6'
                7 -> ca2[i] = '7'
                8 -> ca2[i] = '8'
                9 -> ca2[i] = '9'
            }
            i++
            when (expon % 10) {
                0 -> ca2[i] = '0'
                1 -> ca2[i] = '1'
                2 -> ca2[i] = '2'
                3 -> ca2[i] = '3'
                4 -> ca2[i] = '4'
                5 -> ca2[i] = '5'
                6 -> ca2[i] = '6'
                7 -> ca2[i] = '7'
                8 -> ca2[i] = '8'
                9 -> ca2[i] = '9'
            }
            var nZeros = 0
            if (!leftJustify && leadingZeros) {
                var xThousands = 0
                if (thousands) {
                    var xlead = 0
                    if (ca2[0] == '+' || ca2[0] == '-' || ca2[0] == ' ')
                        xlead = 1
                    var xdp = xlead
                    while (xdp < ca2.size()) {
                        if (ca2[xdp] == '.')
                            break
                        xdp++
                    }
                    xThousands = (xdp - xlead) / 3
                }
                if (fieldWidthSet)
                    nZeros = fieldWidth - ca2.size()
                if ((!minusSign && (leadingSign || leadingSpace)) || minusSign)
                    nZeros--
                nZeros -= xThousands
                if (nZeros < 0)
                    nZeros = 0
            }
            j = 0
            if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
                ca3 = CharArray(ca2.size() + nZeros + 1)
                j++
            } else
                ca3 = CharArray(ca2.size() + nZeros)
            if (!minusSign) {
                if (leadingSign)
                    ca3[0] = '+'
                if (leadingSpace)
                    ca3[0] = ' '
            } else
                ca3[0] = '-'
            run {
                k = 0
                while (k < nZeros) {
                    ca3[j] = '0'
                    j++
                    k++
                }
            }
            run {
                i = 0
                while (i < ca2.size() && j < ca3.size()) {
                    ca3[j] = ca2[i]
                    i++
                    j++
                }
            }

            var lead = 0
            if (ca3[0] == '+' || ca3[0] == '-' || ca3[0] == ' ')
                lead = 1
            var dp = lead
            while (dp < ca3.size()) {
                if (ca3[dp] == '.')
                    break
                dp++
            }
            val nThousands = dp / 3
            // Localize the decimal point.
            if (dp < ca3.size())
                ca3[dp] = dfs!!.getDecimalSeparator()
            var ca4 = ca3
            if (thousands && nThousands > 0) {
                ca4 = CharArray(ca3.size() + nThousands + lead)
                ca4[0] = ca3[0]
                run {
                    i = lead
                    k = lead
                    while (i < dp) {
                        if (i > 0 && (dp - i) % 3 == 0) {
                            // ca4[k]=',';
                            ca4[k] = dfs!!.getGroupingSeparator()
                            ca4[k + 1] = ca3[i]
                            k += 2
                        } else {
                            ca4[k] = ca3[i]
                            k++
                        }
                        i++
                    }
                }
                while (i < ca3.size()) {
                    ca4[k] = ca3[i]
                    i++
                    k++
                }
            }
            return ca4
        }

        /**
         * Check to see if the digits that are going to
         * be truncated because of the precision should
         * force a round in the preceding digits.
         * @param ca1 the array of digits
        * *
         * @param icarry the index of the first digit that
        * *     is to be truncated from the print
        * *
         * @return `true` if the truncation forces
        * *     a round that will change the print
         */
        private fun checkForCarry(ca1: CharArray, icarry: Int): Boolean {
            var carry = false
            if (icarry < ca1.size()) {
                if (ca1[icarry] == '6' || ca1[icarry] == '7' || ca1[icarry] == '8' || ca1[icarry] == '9')
                    carry = true
                else if (ca1[icarry] == '5') {
                    var ii = icarry + 1
                    while (ii < ca1.size()) {
                        if (ca1[ii] != '0')
                            break
                        ii++
                    }
                    carry = ii < ca1.size()
                    if (!carry && icarry > 0) {
                        carry = (ca1[icarry - 1] == '1' || ca1[icarry - 1] == '3' || ca1[icarry - 1] == '5' || ca1[icarry - 1] == '7' || ca1[icarry - 1] == '9')
                    }
                }
            }
            return carry
        }

        /**
         * Start the symbolic carry process.  The process
         * is not quite finished because the symbolic
         * carry may change the length of the string and
         * change the exponent (in e format).
         * @param cLast index of the last digit changed
        * *     by the round
        * *
         * @param cFirst index of the first digit allowed
        * *     to be changed by this phase of the round
        * *
         * @return `true` if the carry forces
        * *     a round that will change the print still
        * *     more
         */
        private fun startSymbolicCarry(ca: CharArray, cLast: Int, cFirst: Int): Boolean {
            var carry = true
            run {
                var i = cLast
                while (carry && i >= cFirst) {
                    carry = false
                    when (ca[i]) {
                        '0' -> ca[i] = '1'
                        '1' -> ca[i] = '2'
                        '2' -> ca[i] = '3'
                        '3' -> ca[i] = '4'
                        '4' -> ca[i] = '5'
                        '5' -> ca[i] = '6'
                        '6' -> ca[i] = '7'
                        '7' -> ca[i] = '8'
                        '8' -> ca[i] = '9'
                        '9' -> {
                            ca[i] = '0'
                            carry = true
                        }
                    }
                    i--
                }
            }
            return carry
        }

        /**
         * An intermediate routine on the way to creating
         * an e format String.  The method decides whether
         * the input double value is an infinity,
         * not-a-number, or a finite double and formats
         * each type of input appropriately.
         * @param x the double value to be formatted.
        * *
         * @param eChar an 'e' or 'E' to use in the
        * *     converted double value.
        * *
         * @return the converted double value.
         */
        private fun eFormatString(x: Double, eChar: Char): String {
            // boolean noDigits = false;
            val ca4: CharArray
            val ca5: CharArray
            if (Double.isInfinite(x)) {
                if (x == Double.POSITIVE_INFINITY) {
                    if (leadingSign)
                        ca4 = "+Inf".toCharArray()
                    else if (leadingSpace)
                        ca4 = " Inf".toCharArray()
                    else
                        ca4 = "Inf".toCharArray()
                } else
                    ca4 = "-Inf".toCharArray()
                // noDigits = true;
            } else if (Double.isNaN(x)) {
                if (leadingSign)
                    ca4 = "+NaN".toCharArray()
                else if (leadingSpace)
                    ca4 = " NaN".toCharArray()
                else
                    ca4 = "NaN".toCharArray()
                // noDigits = true;
            } else
                ca4 = eFormatDigits(x, eChar)
            ca5 = applyFloatPadding(ca4, false)
            return String(ca5)
        }

        /**
         * Apply zero or blank, left or right padding.
         * @param ca4 array of characters before padding is
        * *     finished
        * *
         * @param noDigits NaN or signed Inf
        * *
         * @return a padded array of characters
         */
        private fun applyFloatPadding(ca4: CharArray, noDigits: Boolean): CharArray {
            var ca5 = ca4
            if (fieldWidthSet) {
                var i: Int
                var j: Int
                val nBlanks: Int
                if (leftJustify) {
                    nBlanks = fieldWidth - ca4.size()
                    if (nBlanks > 0) {
                        ca5 = CharArray(ca4.size() + nBlanks)
                        run {
                            i = 0
                            while (i < ca4.size()) {
                                ca5[i] = ca4[i]
                                i++
                            }
                        }
                        run {
                            j = 0
                            while (j < nBlanks) {
                                ca5[i] = ' '
                                j++
                                i++
                            }
                        }
                    }
                } else if (!leadingZeros || noDigits) {
                    nBlanks = fieldWidth - ca4.size()
                    if (nBlanks > 0) {
                        ca5 = CharArray(ca4.size() + nBlanks)
                        run {
                            i = 0
                            while (i < nBlanks) {
                                ca5[i] = ' '
                                i++
                            }
                        }
                        run {
                            j = 0
                            while (j < ca4.size()) {
                                ca5[i] = ca4[j]
                                i++
                                j++
                            }
                        }
                    }
                } else if (leadingZeros) {
                    nBlanks = fieldWidth - ca4.size()
                    if (nBlanks > 0) {
                        ca5 = CharArray(ca4.size() + nBlanks)
                        i = 0
                        j = 0
                        if (ca4[0] == '-') {
                            ca5[0] = '-'
                            i++
                            j++
                        }
                        run {
                            var k = 0
                            while (k < nBlanks) {
                                ca5[i] = '0'
                                i++
                                k++
                            }
                        }
                        while (j < ca4.size()) {
                            ca5[i] = ca4[j]
                            i++
                            j++
                        }
                    }
                }
            }
            return ca5
        }

        /**
         * Format method for the f conversion character.
         * @param x the double to format.
        * *
         * @return the formatted String.
         */
        private fun printFFormat(x: Double): String {
            return fFormatString(x)
        }

        /**
         * Format method for the e or E conversion
         * character.
         * @param x the double to format.
        * *
         * @return the formatted String.
         */
        private fun printEFormat(x: Double): String {
            if (conversionCharacter == 'e')
                return eFormatString(x, 'e')
            else
                return eFormatString(x, 'E')
        }

        /**
         * Format method for the g conversion character.

         * For g format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both a
         * '+' and a ' ' are specified, the blank flag is
         * ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear after the radix character.
         * Padding is with trailing 0s.
         * @param x the double to format.
        * *
         * @return the formatted String.
         */
        private fun printGFormat(x: Double): String {
            val sx: String
            val sy: String
            val sz: String
            var ret: String
            val savePrecision = precision
            var i: Int
            val ca4: CharArray
            val ca5: CharArray
            // boolean noDigits = false;
            if (Double.isInfinite(x)) {
                if (x == Double.POSITIVE_INFINITY) {
                    if (leadingSign)
                        ca4 = "+Inf".toCharArray()
                    else if (leadingSpace)
                        ca4 = " Inf".toCharArray()
                    else
                        ca4 = "Inf".toCharArray()
                } else
                    ca4 = "-Inf".toCharArray()
                // noDigits = true;
            } else if (Double.isNaN(x)) {
                if (leadingSign)
                    ca4 = "+NaN".toCharArray()
                else if (leadingSpace)
                    ca4 = " NaN".toCharArray()
                else
                    ca4 = "NaN".toCharArray()
                // noDigits = true;
            } else {
                if (!precisionSet)
                    precision = defaultDigits
                if (precision == 0)
                    precision = 1
                var ePos = -1
                if (conversionCharacter == 'g') {
                    sx = eFormatString(x, 'e').trim()
                    ePos = sx.indexOf('e')
                } else {
                    sx = eFormatString(x, 'E').trim()
                    ePos = sx.indexOf('E')
                }
                i = ePos + 1
                var expon = 0
                if (sx.charAt(i) == '-') {
                    run {
                        ++i
                        while (i < sx.length()) {
                            if (sx.charAt(i) != '0')
                                break
                            i++
                        }
                    }
                    if (i < sx.length())
                        expon = -Integer.parseInt(sx.substring(i))
                } else {
                    if (sx.charAt(i) == '+')
                        ++i
                    while (i < sx.length()) {
                        if (sx.charAt(i) != '0')
                            break
                        i++
                    }
                    if (i < sx.length())
                        expon = Integer.parseInt(sx.substring(i))
                }
                // Trim trailing zeros.
                // If the radix character is not followed by
                // a digit, trim it, too.
                if (!alternateForm) {
                    if (expon >= -4 && expon < precision)
                        sy = fFormatString(x).trim()
                    else
                        sy = sx.substring(0, ePos)
                    i = sy.length() - 1
                    while (i >= 0) {
                        if (sy.charAt(i) != '0')
                            break
                        i--
                    }
                    if (i >= 0 && sy.charAt(i) == '.')
                        i--
                    if (i == -1)
                        sz = "0"
                    else if (!Character.isDigit(sy.charAt(i)))
                        sz = sy.substring(0, i + 1) + "0"
                    else
                        sz = sy.substring(0, i + 1)
                    if (expon >= -4 && expon < precision)
                        ret = sz
                    else
                        ret = sz + sx.substring(ePos)
                } else {
                    if (expon >= -4 && expon < precision)
                        ret = fFormatString(x).trim()
                    else
                        ret = sx
                }
                // leading space was trimmed off during
                // construction
                if (leadingSpace)
                    if (x >= 0)
                        ret = " " + ret
                ca4 = ret.toCharArray()
            }
            // Pad with blanks or zeros.
            ca5 = applyFloatPadding(ca4, false)
            precision = savePrecision
            return String(ca5)
        }

        /**
         * Format method for the d conversion specifer and
         * short argument.

         * For d format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  A '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both a
         * '+' and a ' ' are specified, the blank flag is
         * ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the short to format.
        * *
         * @return the formatted String.
         */
        private fun printDFormat(x: Short): String {
            return printDFormat(Short.toString(x))
        }

        /**
         * Format method for the d conversion character and
         * long argument.

         * For d format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  A '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both a
         * '+' and a ' ' are specified, the blank flag is
         * ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the long to format.
        * *
         * @return the formatted String.
         */
        private fun printDFormat(x: Long): String {
            return printDFormat(Long.toString(x))
        }

        /**
         * Format method for the d conversion character and
         * int argument.

         * For d format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  A '+' character means that the conversion
         * will always begin with a sign (+ or -).  The
         * blank flag character means that a non-negative
         * input will be preceded with a blank.  If both a
         * '+' and a ' ' are specified, the blank flag is
         * ignored.  The '0' flag character implies that
         * padding to the field width will be done with
         * zeros instead of blanks.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the int to format.
        * *
         * @return the formatted String.
         */
        private fun printDFormat(x: Int): String {
            return printDFormat(Integer.toString(x))
        }

        /**
         * Utility method for formatting using the d
         * conversion character.
         * @param sx the String to format, the result of
        * *     converting a short, int, or long to a
        * *     String.
        * *
         * @return the formatted String.
         */
        private fun printDFormat(sx: String): String {
            var sx = sx
            var nLeadingZeros = 0
            var nBlanks = 0
            var n = 0
            var i = 0
            var jFirst = 0
            val neg = sx.charAt(0) == '-'
            if (sx.equals("0") && precisionSet && precision == 0)
                sx = ""
            if (!neg) {
                if (precisionSet && sx.length() < precision)
                    nLeadingZeros = precision - sx.length()
            } else {
                if (precisionSet && (sx.length() - 1) < precision)
                    nLeadingZeros = precision - sx.length() + 1
            }
            if (nLeadingZeros < 0)
                nLeadingZeros = 0
            if (fieldWidthSet) {
                nBlanks = fieldWidth - nLeadingZeros - sx.length()
                if (!neg && (leadingSign || leadingSpace))
                    nBlanks--
            }
            if (nBlanks < 0)
                nBlanks = 0
            if (leadingSign)
                n++
            else if (leadingSpace)
                n++
            n += nBlanks
            n += nLeadingZeros
            n += sx.length()
            val ca = CharArray(n)
            if (leftJustify) {
                if (neg)
                    ca[i++] = '-'
                else if (leadingSign)
                    ca[i++] = '+'
                else if (leadingSpace)
                    ca[i++] = ' '
                val csx = sx.toCharArray()
                jFirst = if (neg) 1 else 0
                run {
                    var j = 0
                    while (j < nLeadingZeros) {
                        ca[i] = '0'
                        i++
                        j++
                    }
                }
                run {
                    var j = jFirst
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < nBlanks) {
                        ca[i] = ' '
                        i++
                        j++
                    }
                }
            } else {
                if (!leadingZeros) {
                    run {
                        i = 0
                        while (i < nBlanks) {
                            ca[i] = ' '
                            i++
                        }
                    }
                    if (neg)
                        ca[i++] = '-'
                    else if (leadingSign)
                        ca[i++] = '+'
                    else if (leadingSpace)
                        ca[i++] = ' '
                } else {
                    if (neg)
                        ca[i++] = '-'
                    else if (leadingSign)
                        ca[i++] = '+'
                    else if (leadingSpace)
                        ca[i++] = ' '
                    run {
                        var j = 0
                        while (j < nBlanks) {
                            ca[i] = '0'
                            j++
                            i++
                        }
                    }
                }
                run {
                    var j = 0
                    while (j < nLeadingZeros) {
                        ca[i] = '0'
                        j++
                        i++
                    }
                }
                val csx = sx.toCharArray()
                jFirst = if (neg) 1 else 0
                run {
                    var j = jFirst
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
            }
            return String(ca)
        }

        /**
         * Format method for the x conversion character and
         * short argument.

         * For x format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means to lead with
         * '0x'.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the short to format.
        * *
         * @return the formatted String.
         */
        private fun printXFormat(x: Short): String {
            var sx: String? = null
            if (x == Short.MIN_VALUE)
                sx = "8000"
            else if (x < 0) {
                var t: String
                if (x == Short.MIN_VALUE)
                    t = "0"
                else {
                    t = Integer.toString(((-x - 1).inv()) xor Short.MIN_VALUE, 16)
                    if (t.charAt(0) == 'F' || t.charAt(0) == 'f')
                        t = t.substring(16, 32)
                }
                when (t.length()) {
                    1 -> sx = "800" + t
                    2 -> sx = "80" + t
                    3 -> sx = "8" + t
                    4 -> when (t.charAt(0)) {
                        '1' -> sx = "9" + t.substring(1, 4)
                        '2' -> sx = "a" + t.substring(1, 4)
                        '3' -> sx = "b" + t.substring(1, 4)
                        '4' -> sx = "c" + t.substring(1, 4)
                        '5' -> sx = "d" + t.substring(1, 4)
                        '6' -> sx = "e" + t.substring(1, 4)
                        '7' -> sx = "f" + t.substring(1, 4)
                    }
                }
            } else
                sx = Integer.toString(x.toInt(), 16)
            return printXFormat(sx)
        }

        /**
         * Format method for the x conversion character and
         * long argument.

         * For x format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means to lead with
         * '0x'.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the long to format.
        * *
         * @return the formatted String.
         */
        private fun printXFormat(x: Long): String {
            var sx: String? = null
            if (x == Long.MIN_VALUE)
                sx = "8000000000000000"
            else if (x < 0) {
                val t = Long.toString(((-x - 1).inv()) xor Long.MIN_VALUE, 16)
                when (t.length()) {
                    1 -> sx = "800000000000000" + t
                    2 -> sx = "80000000000000" + t
                    3 -> sx = "8000000000000" + t
                    4 -> sx = "800000000000" + t
                    5 -> sx = "80000000000" + t
                    6 -> sx = "8000000000" + t
                    7 -> sx = "800000000" + t
                    8 -> sx = "80000000" + t
                    9 -> sx = "8000000" + t
                    10 -> sx = "800000" + t
                    11 -> sx = "80000" + t
                    12 -> sx = "8000" + t
                    13 -> sx = "800" + t
                    14 -> sx = "80" + t
                    15 -> sx = "8" + t
                    16 -> when (t.charAt(0)) {
                        '1' -> sx = "9" + t.substring(1, 16)
                        '2' -> sx = "a" + t.substring(1, 16)
                        '3' -> sx = "b" + t.substring(1, 16)
                        '4' -> sx = "c" + t.substring(1, 16)
                        '5' -> sx = "d" + t.substring(1, 16)
                        '6' -> sx = "e" + t.substring(1, 16)
                        '7' -> sx = "f" + t.substring(1, 16)
                    }
                }
            } else
                sx = Long.toString(x, 16)
            return printXFormat(sx)
        }

        /**
         * Format method for the x conversion character and
         * int argument.

         * For x format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means to lead with
         * '0x'.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the int to format.
        * *
         * @return the formatted String.
         */
        private fun printXFormat(x: Int): String {
            var sx: String? = null
            if (x == Integer.MIN_VALUE)
                sx = "80000000"
            else if (x < 0) {
                val t = Integer.toString(((-x - 1).inv()) xor Integer.MIN_VALUE, 16)
                when (t.length()) {
                    1 -> sx = "8000000" + t
                    2 -> sx = "800000" + t
                    3 -> sx = "80000" + t
                    4 -> sx = "8000" + t
                    5 -> sx = "800" + t
                    6 -> sx = "80" + t
                    7 -> sx = "8" + t
                    8 -> when (t.charAt(0)) {
                        '1' -> sx = "9" + t.substring(1, 8)
                        '2' -> sx = "a" + t.substring(1, 8)
                        '3' -> sx = "b" + t.substring(1, 8)
                        '4' -> sx = "c" + t.substring(1, 8)
                        '5' -> sx = "d" + t.substring(1, 8)
                        '6' -> sx = "e" + t.substring(1, 8)
                        '7' -> sx = "f" + t.substring(1, 8)
                    }
                }
            } else
                sx = Integer.toString(x, 16)
            return printXFormat(sx)
        }

        /**
         * Utility method for formatting using the x
         * conversion character.
         * @param sx the String to format, the result of
        * *     converting a short, int, or long to a
        * *     String.
        * *
         * @return the formatted String.
         */
        private fun printXFormat(sx: String): String {
            var sx = sx
            var nLeadingZeros = 0
            var nBlanks = 0
            if (sx.equals("0") && precisionSet && precision == 0)
                sx = ""
            if (precisionSet)
                nLeadingZeros = precision - sx.length()
            if (nLeadingZeros < 0)
                nLeadingZeros = 0
            if (fieldWidthSet) {
                nBlanks = fieldWidth - nLeadingZeros - sx.length()
                if (alternateForm)
                    nBlanks = nBlanks - 2
            }
            if (nBlanks < 0)
                nBlanks = 0
            var n = 0
            if (alternateForm)
                n += 2
            n += nLeadingZeros
            n += sx.length()
            n += nBlanks
            val ca = CharArray(n)
            var i = 0
            if (leftJustify) {
                if (alternateForm) {
                    ca[i++] = '0'
                    ca[i++] = 'x'
                }
                run {
                    var j = 0
                    while (j < nLeadingZeros) {
                        ca[i] = '0'
                        j++
                        i++
                    }
                }
                val csx = sx.toCharArray()
                run {
                    var j = 0
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < nBlanks) {
                        ca[i] = ' '
                        j++
                        i++
                    }
                }
            } else {
                if (!leadingZeros)
                    run {
                        var j = 0
                        while (j < nBlanks) {
                            ca[i] = ' '
                            j++
                            i++
                        }
                    }
                if (alternateForm) {
                    ca[i++] = '0'
                    ca[i++] = 'x'
                }
                if (leadingZeros)
                    run {
                        var j = 0
                        while (j < nBlanks) {
                            ca[i] = '0'
                            j++
                            i++
                        }
                    }
                run {
                    var j = 0
                    while (j < nLeadingZeros) {
                        ca[i] = '0'
                        j++
                        i++
                    }
                }
                val csx = sx.toCharArray()
                run {
                    var j = 0
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
            }
            var caReturn = String(ca)
            if (conversionCharacter == 'X')
                caReturn = caReturn.toUpperCase()
            return caReturn
        }

        /**
         * Format method for the o conversion character and
         * short argument.

         * For o format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means that the
         * output begins with a leading 0 and the precision
         * is increased by 1.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the short to format.
        * *
         * @return the formatted String.
         */
        private fun printOFormat(x: Short): String {
            var sx: String? = null
            if (x == Short.MIN_VALUE)
                sx = "100000"
            else if (x < 0) {
                val t = Integer.toString(((-x - 1).inv()) xor Short.MIN_VALUE, 8)
                when (t.length()) {
                    1 -> sx = "10000" + t
                    2 -> sx = "1000" + t
                    3 -> sx = "100" + t
                    4 -> sx = "10" + t
                    5 -> sx = "1" + t
                }
            } else
                sx = Integer.toString(x.toInt(), 8)
            return printOFormat(sx)
        }

        /**
         * Format method for the o conversion character and
         * long argument.

         * For o format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means that the
         * output begins with a leading 0 and the precision
         * is increased by 1.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the long to format.
        * *
         * @return the formatted String.
         */
        private fun printOFormat(x: Long): String {
            var sx: String? = null
            if (x == Long.MIN_VALUE)
                sx = "1000000000000000000000"
            else if (x < 0) {
                val t = Long.toString(((-x - 1).inv()) xor Long.MIN_VALUE, 8)
                when (t.length()) {
                    1 -> sx = "100000000000000000000" + t
                    2 -> sx = "10000000000000000000" + t
                    3 -> sx = "1000000000000000000" + t
                    4 -> sx = "100000000000000000" + t
                    5 -> sx = "10000000000000000" + t
                    6 -> sx = "1000000000000000" + t
                    7 -> sx = "100000000000000" + t
                    8 -> sx = "10000000000000" + t
                    9 -> sx = "1000000000000" + t
                    10 -> sx = "100000000000" + t
                    11 -> sx = "10000000000" + t
                    12 -> sx = "1000000000" + t
                    13 -> sx = "100000000" + t
                    14 -> sx = "10000000" + t
                    15 -> sx = "1000000" + t
                    16 -> sx = "100000" + t
                    17 -> sx = "10000" + t
                    18 -> sx = "1000" + t
                    19 -> sx = "100" + t
                    20 -> sx = "10" + t
                    21 -> sx = "1" + t
                }
            } else
                sx = Long.toString(x, 8)
            return printOFormat(sx)
        }

        /**
         * Format method for the o conversion character and
         * int argument.

         * For o format, the flag character '-', means that
         * the output should be left justified within the
         * field.  The default is to pad with blanks on the
         * left.  The '#' flag character means that the
         * output begins with a leading 0 and the precision
         * is increased by 1.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is to
         * add no padding.  Padding is with blanks by
         * default.

         * The precision, if set, is the minimum number of
         * digits to appear.  Padding is with leading 0s.
         * @param x the int to format.
        * *
         * @return the formatted String.
         */
        private fun printOFormat(x: Int): String {
            var sx: String? = null
            if (x == Integer.MIN_VALUE)
                sx = "20000000000"
            else if (x < 0) {
                val t = Integer.toString(((-x - 1).inv()) xor Integer.MIN_VALUE, 8)
                when (t.length()) {
                    1 -> sx = "2000000000" + t
                    2 -> sx = "200000000" + t
                    3 -> sx = "20000000" + t
                    4 -> sx = "2000000" + t
                    5 -> sx = "200000" + t
                    6 -> sx = "20000" + t
                    7 -> sx = "2000" + t
                    8 -> sx = "200" + t
                    9 -> sx = "20" + t
                    10 -> sx = "2" + t
                    11 -> sx = "3" + t.substring(1)
                }
            } else
                sx = Integer.toString(x, 8)
            return printOFormat(sx)
        }

        /**
         * Utility method for formatting using the o
         * conversion character.
         * @param sx the String to format, the result of
        * *     converting a short, int, or long to a
        * *     String.
        * *
         * @return the formatted String.
         */
        private fun printOFormat(sx: String): String {
            var sx = sx
            var nLeadingZeros = 0
            var nBlanks = 0
            if (sx.equals("0") && precisionSet && precision == 0)
                sx = ""
            if (precisionSet)
                nLeadingZeros = precision - sx.length()
            if (alternateForm)
                nLeadingZeros++
            if (nLeadingZeros < 0)
                nLeadingZeros = 0
            if (fieldWidthSet)
                nBlanks = fieldWidth - nLeadingZeros - sx.length()
            if (nBlanks < 0)
                nBlanks = 0
            val n = nLeadingZeros + sx.length() + nBlanks
            val ca = CharArray(n)
            var i: Int
            if (leftJustify) {
                run {
                    i = 0
                    while (i < nLeadingZeros) {
                        ca[i] = '0'
                        i++
                    }
                }
                val csx = sx.toCharArray()
                run {
                    var j = 0
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < nBlanks) {
                        ca[i] = ' '
                        j++
                        i++
                    }
                }
            } else {
                if (leadingZeros)
                    run {
                        i = 0
                        while (i < nBlanks) {
                            ca[i] = '0'
                            i++
                        }
                    }
                else
                    run {
                        i = 0
                        while (i < nBlanks) {
                            ca[i] = ' '
                            i++
                        }
                    }
                run {
                    var j = 0
                    while (j < nLeadingZeros) {
                        ca[i] = '0'
                        j++
                        i++
                    }
                }
                val csx = sx.toCharArray()
                run {
                    var j = 0
                    while (j < csx.size()) {
                        ca[i] = csx[j]
                        j++
                        i++
                    }
                }
            }
            return String(ca)
        }

        /**
         * Format method for the c conversion character and
         * char argument.

         * The only flag character that affects c format is
         * the '-', meaning that the output should be left
         * justified within the field.  The default is to
         * pad with blanks on the left.

         * The field width is treated as the minimum number
         * of characters to be printed.  Padding is with
         * blanks by default.  The default width is 1.

         * The precision, if set, is ignored.
         * @param x the char to format.
        * *
         * @return the formatted String.
         */
        private fun printCFormat(x: Char): String {
            val nPrint = 1
            var width = fieldWidth
            if (!fieldWidthSet)
                width = nPrint
            val ca = CharArray(width)
            var i = 0
            if (leftJustify) {
                ca[0] = x
                run {
                    i = 1
                    while (i <= width - nPrint) {
                        ca[i] = ' '
                        i++
                    }
                }
            } else {
                run {
                    i = 0
                    while (i < width - nPrint) {
                        ca[i] = ' '
                        i++
                    }
                }
                ca[i] = x
            }
            return String(ca)
        }

        /**
         * Format method for the s conversion character and
         * String argument.

         * The only flag character that affects s format is
         * the '-', meaning that the output should be left
         * justified within the field.  The default is to
         * pad with blanks on the left.

         * The field width is treated as the minimum number
         * of characters to be printed.  The default is the
         * smaller of the number of characters in the the
         * input and the precision.  Padding is with blanks
         * by default.

         * The precision, if set, specifies the maximum
         * number of characters to be printed from the
         * string.  A null digit string is treated
         * as a 0.  The default is not to set a maximum
         * number of characters to be printed.
         * @param x the String to format.
        * *
         * @return the formatted String.
         */
        private fun printSFormat(x: String): String {
            var nPrint = x.length()
            var width = fieldWidth
            if (precisionSet && nPrint > precision)
                nPrint = precision
            if (!fieldWidthSet)
                width = nPrint
            var n = 0
            if (width > nPrint)
                n += width - nPrint
            if (nPrint >= x.length())
                n += x.length()
            else
                n += nPrint
            val ca = CharArray(n)
            var i = 0
            if (leftJustify) {
                if (nPrint >= x.length()) {
                    val csx = x.toCharArray()
                    run {
                        i = 0
                        while (i < x.length()) {
                            ca[i] = csx[i]
                            i++
                        }
                    }
                } else {
                    val csx = x.substring(0, nPrint).toCharArray()
                    run {
                        i = 0
                        while (i < nPrint) {
                            ca[i] = csx[i]
                            i++
                        }
                    }
                }
                run {
                    var j = 0
                    while (j < width - nPrint) {
                        ca[i] = ' '
                        j++
                        i++
                    }
                }
            } else {
                run {
                    i = 0
                    while (i < width - nPrint) {
                        ca[i] = ' '
                        i++
                    }
                }
                if (nPrint >= x.length()) {
                    val csx = x.toCharArray()
                    run {
                        var j = 0
                        while (j < x.length()) {
                            ca[i] = csx[j]
                            i++
                            j++
                        }
                    }
                } else {
                    val csx = x.substring(0, nPrint).toCharArray()
                    run {
                        var j = 0
                        while (j < nPrint) {
                            ca[i] = csx[j]
                            i++
                            j++
                        }
                    }
                }
            }
            return String(ca)
        }

        /**
         * Check for a conversion character.  If it is
         * there, store it.
         * @param x the String to format.
        * *
         * @return `true` if the conversion
        * *     character is there, and
        * *     `false` otherwise.
         */
        private fun setConversionCharacter(): Boolean {
            /* idfgGoxXeEcs */
            var ret = false
            conversionCharacter = '\0'
            if (pos < fmt!!.length()) {
                val c = fmt!!.charAt(pos)
                if (c == 'i' || c == 'd' || c == 'f' || c == 'g' || c == 'G' || c == 'o' || c == 'x' || c == 'X' || c == 'e' || c == 'E' || c == 'c' || c == 's' || c == '%') {
                    conversionCharacter = c
                    pos++
                    ret = true
                }
            }
            return ret
        }

        /**
         * Check for an h, l, or L in a format.  An L is
         * used to control the minimum number of digits
         * in an exponent when using floating point
         * formats.  An l or h is used to control
         * conversion of the input to a long or short,
         * respectively, before formatting.  If any of
         * these is present, store them.
         */
        private fun setOptionalHL() {
            optionalh = false
            optionall = false
            optionalL = false
            if (pos < fmt!!.length()) {
                val c = fmt!!.charAt(pos)
                if (c == 'h') {
                    optionalh = true
                    pos++
                } else if (c == 'l') {
                    optionall = true
                    pos++
                } else if (c == 'L') {
                    optionalL = true
                    pos++
                }
            }
        }

        /**
         * Set the precision.
         */
        private fun setPrecision() {
            val firstPos = pos
            precisionSet = false
            if (pos < fmt!!.length() && fmt!!.charAt(pos) == '.') {
                pos++
                if ((pos < fmt!!.length()) && (fmt!!.charAt(pos) == '*')) {
                    pos++
                    if (!setPrecisionArgPosition()) {
                        variablePrecision = true
                        precisionSet = true
                    }
                    return
                } else {
                    while (pos < fmt!!.length()) {
                        val c = fmt!!.charAt(pos)
                        if (Character.isDigit(c))
                            pos++
                        else
                            break
                    }
                    if (pos > firstPos + 1) {
                        val sz = fmt!!.substring(firstPos + 1, pos)
                        precision = Integer.parseInt(sz)
                        precisionSet = true
                    }
                }
            }
        }

        /**
         * Set the field width.
         */
        private fun setFieldWidth() {
            val firstPos = pos
            fieldWidth = 0
            fieldWidthSet = false
            if ((pos < fmt!!.length()) && (fmt!!.charAt(pos) == '*')) {
                pos++
                if (!setFieldWidthArgPosition()) {
                    variableFieldWidth = true
                    fieldWidthSet = true
                }
            } else {
                while (pos < fmt!!.length()) {
                    val c = fmt!!.charAt(pos)
                    if (Character.isDigit(c))
                        pos++
                    else
                        break
                }
                if (firstPos < pos && firstPos < fmt!!.length()) {
                    val sz = fmt!!.substring(firstPos, pos)
                    fieldWidth = Integer.parseInt(sz)
                    fieldWidthSet = true
                }
            }
        }

        /**
         * Store the digits `n` in %n$ forms.
         */
        private fun setArgPosition() {
            var xPos: Int
            run {
                xPos = pos
                while (xPos < fmt!!.length()) {
                    if (!Character.isDigit(fmt!!.charAt(xPos)))
                        break
                    xPos++
                }
            }
            if (xPos > pos && xPos < fmt!!.length()) {
                if (fmt!!.charAt(xPos) == '$') {
                    positionalSpecification = true
                    argumentPosition = Integer.parseInt(fmt!!.substring(pos, xPos))
                    pos = xPos + 1
                }
            }
        }

        /**
         * Store the digits `n` in *n$ forms.
         */
        private fun setFieldWidthArgPosition(): Boolean {
            var ret = false
            var xPos: Int
            run {
                xPos = pos
                while (xPos < fmt!!.length()) {
                    if (!Character.isDigit(fmt!!.charAt(xPos)))
                        break
                    xPos++
                }
            }
            if (xPos > pos && xPos < fmt!!.length()) {
                if (fmt!!.charAt(xPos) == '$') {
                    positionalFieldWidth = true
                    argumentPositionForFieldWidth = Integer.parseInt(fmt!!.substring(pos, xPos))
                    pos = xPos + 1
                    ret = true
                }
            }
            return ret
        }

        /**
         * Store the digits `n` in *n$ forms.
         */
        private fun setPrecisionArgPosition(): Boolean {
            var ret = false
            var xPos: Int
            run {
                xPos = pos
                while (xPos < fmt!!.length()) {
                    if (!Character.isDigit(fmt!!.charAt(xPos)))
                        break
                    xPos++
                }
            }
            if (xPos > pos && xPos < fmt!!.length()) {
                if (fmt!!.charAt(xPos) == '$') {
                    positionalPrecision = true
                    argumentPositionForPrecision = Integer.parseInt(fmt!!.substring(pos, xPos))
                    pos = xPos + 1
                    ret = true
                }
            }
            return ret
        }

        fun isPositionalSpecification(): Boolean {
            return positionalSpecification
        }

        fun isPositionalFieldWidth(): Boolean {
            return positionalFieldWidth
        }

        fun isPositionalPrecision(): Boolean {
            return positionalPrecision
        }

        /**
         * Set flag characters, one of '-+#0 or a space.
         */
        private fun setFlagCharacters() {
            /* '-+ #0 */
            thousands = false
            leftJustify = false
            leadingSign = false
            leadingSpace = false
            alternateForm = false
            leadingZeros = false
            while (pos < fmt!!.length()) {
                val c = fmt!!.charAt(pos)
                if (c == '\'')
                    thousands = true
                else if (c == '-') {
                    leftJustify = true
                    leadingZeros = false
                } else if (c == '+') {
                    leadingSign = true
                    leadingSpace = false
                } else if (c == ' ') {
                    if (!leadingSign)
                        leadingSpace = true
                } else if (c == '#')
                    alternateForm = true
                else if (c == '0') {
                    if (!leftJustify)
                        leadingZeros = true
                } else
                    break
                pos++
            }
        }

        /**
         * The integer portion of the result of a decimal
         * conversion (i, d, u, f, g, or G) will be
         * formatted with thousands' grouping characters.
         * For other conversions the flag is ignored.
         */
        private var thousands = false
        /**
         * The result of the conversion will be
         * left-justified within the field.
         */
        private var leftJustify = false
        /**
         * The result of a signed conversion will always
         * begin with a sign (+ or -).
         */
        private var leadingSign = false
        /**
         * Flag indicating that left padding with spaces is
         * specified.
         */
        private var leadingSpace = false
        /**
         * For an o conversion, increase the precision to
         * force the first digit of the result to be a
         * zero.  For x (or X) conversions, a non-zero
         * result will have 0x (or 0X) prepended to it.
         * For e, E, f, g, or G conversions, the result
         * will always contain a radix character, even if
         * no digits follow the point.  For g and G
         * conversions, trailing zeros will not be removed
         * from the result.
         */
        private var alternateForm = false
        /**
         * Flag indicating that left padding with zeroes is
         * specified.
         */
        private var leadingZeros = false
        /**
         * Flag indicating that the field width is *.
         */
        private var variableFieldWidth = false
        /**
         * If the converted value has fewer bytes than the
         * field width, it will be padded with spaces or
         * zeroes.
         */
        private var fieldWidth = 0
        /**
         * Flag indicating whether or not the field width
         * has been set.
         */
        private var fieldWidthSet = false
        /**
         * The minimum number of digits to appear for the
         * d, i, o, u, x, or X conversions.  The number of
         * digits to appear after the radix character for
         * the e, E, and f conversions.  The maximum number
         * of significant digits for the g and G
         * conversions.  The maximum number of bytes to be
         * printed from a string in s and S conversions.
         */
        private var precision = 0
        /**
         * Flag indicating that the precision is *.
         */
        private var variablePrecision = false
        /**
         * Flag indicating whether or not the precision has
         * been set.
         */
        private var precisionSet = false
        /*
		 */
        private var positionalSpecification = false
        var argumentPosition = 0
            private set
        private var positionalFieldWidth = false
        var argumentPositionForFieldWidth = 0
            private set
        private var positionalPrecision = false
        var argumentPositionForPrecision = 0
            private set
        /**
         * Flag specifying that a following d, i, o, u, x,
         * or X conversion character applies to a type
         * short int.
         */
        private var optionalh = false
        /**
         * Flag specifying that a following d, i, o, u, x,
         * or X conversion character applies to a type lont
         * int argument.
         */
        private var optionall = false
        /**
         * Flag specifying that a following e, E, f, g, or
         * G conversion character applies to a type double
         * argument.  This is a noop in Java.
         */
        private var optionalL = false
        /** Control string type.  */
        var conversionCharacter = '\0'
            private set
        /**
         * Position within the control string.  Used by
         * the constructor.
         */
        private var pos = 0
        /** Literal or control format string.  */
        private var fmt: String? = null

        companion object {
            /** Default precision.  */
            private val defaultDigits = 6
        }
    }
    /**
     * Get the conversion character that tells what
     * type of control character this instance has.

     * @return the conversion character.
     */
    /** Vector of control strings and format literals.  */
    private val vFmt = Vector<Object>()
    /** Character position.  Used by the constructor.  */
    private var cPos = 0
    /** Character position.  Used by the constructor.  */
    private val dfs: DecimalFormatSymbols? = null
}
