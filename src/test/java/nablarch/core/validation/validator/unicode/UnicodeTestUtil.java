package nablarch.core.validation.validator.unicode;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * nablarch.core.validation.validator.unicodeパッケージのテスト用ユーティリティクラス。
 *
 * @author T.Kawasaki
 */
public final class UnicodeTestUtil {

    /**
     * 文字からコードポイントを取得する。
     *
     * @param c 文字
     * @return コードポイント
     */
    static int codePointOf(char c) {
        return String.valueOf(c).codePointAt(0);
    }

    /**
     * 文字からコードポイントを取得する。
     * リテラル文字が期待とは異なるコードポイントにマッピングされて、
     * テストが正常に実行されないことを防止するため、
     * 期待するコードポイントをチェック用に設定する。
     * （リテラル文字をコメントで書いてもよいが、コメントも間違える可能性があるので）
     *
     * @param c     文字
     * @param check その文字が変換されるはずのコードポイント
     * @return コードポイント
     */
    static int codePointOf(char c, int check) {
        int codePoint = codePointOf(c);
        assertThat("テストのバグ", codePoint, is(check));
        return codePoint;
    }

    /**
     * 文字列の先頭文字のコードポイントを取得する。
     *
     * @param s 文字列
     * @return 先頭文字のコードポイント
     */
    static int codePointOf(String s) {
        if (countCodePoint(s) > 1) {
            throw new IllegalArgumentException(s);
        }
        return s.codePointAt(0);
    }

    /**
     * 文字列中のコードポイント数をカウントする。
     *
     * @param s 文字列
     * @return コードポイント数
     */
    static int countCodePoint(String s) {
        return s.codePointCount(0, s.length() - 1);
    }

    // テスト用クラスだけど、テストしておく

    @Test
    public void testCodePointOf() {
        assertThat(codePointOf('A'), is(0x41));
        assertThat(codePointOf("0"), is(0x30));
        assertThat(codePointOf(HOKKE), is(0x29E3D));
    }

    @Test
    public void testCountCodePoint() {
        assertThat(HOKKE.length(), is(2));
        assertThat(countCodePoint(HOKKE), is(1));
    }

    private static final String HOKKE = "\uD867\uDE3D";
}
