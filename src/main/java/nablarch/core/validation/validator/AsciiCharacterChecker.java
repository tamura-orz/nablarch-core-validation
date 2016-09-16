package nablarch.core.validation.validator;

import java.util.BitSet;

import nablarch.core.util.annotation.Published;

/**
 * ASCII文字の文字種チェックに使用するユーティリティクラス。
 * 
 * @author Koichi Asano
 *
 */
@Published(tag = "architect")
public final class AsciiCharacterChecker {

    /**
     * 隠蔽コンストラクタ。
     */
    private AsciiCharacterChecker() {
        
    }
    /**
     * Ascii文字の集合。
     */
    private static final BitSet ASCII_CHAR_SET;
    /**
     * 半角英字の集合。
     */
    private static final BitSet ASCII_ALPHA_CHAR_SET;
    /**
     * 半角数値の集合。
     */
    private static final BitSet ASCII_NUM_CHAR_SET;
    /**
     * 半角英数値の集合。
     */
    private static final BitSet ASCII_ALNUM_CHAR_SET;
    static {

        ASCII_ALPHA_CHAR_SET = nablarch.core.util.CharacterCheckerUtil
                .createCharSet("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ASCII_NUM_CHAR_SET = nablarch.core.util.CharacterCheckerUtil.createCharSet("0123456789");
        ASCII_ALNUM_CHAR_SET = nablarch.core.util.CharacterCheckerUtil
                .createCharSet("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        ASCII_CHAR_SET = nablarch.core.util.CharacterCheckerUtil
                .createCharSet(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~");
    }

    /**
     * 文字列がAscii文字のみからなるかチェックする。<br/>
     * 
     * @param value チェック対象の文字列
     * @return チェック対象の文字列が全てAscii文字からなる場合true
     */
    public static boolean checkAsciiCharOnly(String value) {
        return nablarch.core.util.CharacterCheckerUtil.checkValidCharOnly(ASCII_CHAR_SET, value);
    }

    /**
     * 文字列が半角英字のみからなるかチェックする。<br/>
     * 
     * @param value チェック対象の文字列
     * @return チェック対象の文字列が全て半角英字からなる場合true
     */
    public static boolean checkAlphaCharOnly(String value) {
        return nablarch.core.util.CharacterCheckerUtil.checkValidCharOnly(ASCII_ALPHA_CHAR_SET, value);
    }

    /**
     * 文字列が半角数字のみからなるかチェックする。<br/>
     * 
     * @param value チェック対象の文字列
     * @return チェック対象の文字列が全て半角数字からなる場合true
     */
    public static boolean checkNumberCharOnly(String value) {
        return nablarch.core.util.CharacterCheckerUtil.checkValidCharOnly(ASCII_NUM_CHAR_SET, value);
    }

    /**
     * 文字列が半角英数字のみからなるかチェックする。<br/>
     * 
     * @param value チェック対象の文字列
     * @return チェック対象の文字列が全て半角英数字からなる場合true
     */
    public static boolean checkAlnumCharOnly(String value) {
        return nablarch.core.util.CharacterCheckerUtil.checkValidCharOnly(ASCII_ALNUM_CHAR_SET, value);
    }
}
