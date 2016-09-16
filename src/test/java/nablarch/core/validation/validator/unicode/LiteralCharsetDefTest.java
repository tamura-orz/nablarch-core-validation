package nablarch.core.validation.validator.unicode;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link LiteralCharsetDef}のテストクラス。
 *
 * @author T.Kawasaki
 */
public class LiteralCharsetDefTest {

    /** テスト対象 */
    private LiteralCharsetDef target = new LiteralCharsetDef();

    /**
     * 設定された文字が、許容文字と判定されること。
     * その他の文字は、非許容と判定されること。
     */
    @Test
    public void test() {
        // 1と3を許可
        target.setAllowedCharacters("13");

        assertThat(target.contains(0x30), is(false));  // 0
        assertThat(target.contains(0x31), is(true));   // 1
        assertThat(target.contains(0x32), is(false));  // 2
        assertThat(target.contains(0x33), is(true));   // 3
        assertThat(target.contains(0x34), is(false));  // 4
    }

    /** サロゲートペアの文字が混在しても、正しく判定できること。 */
    @Test
    public void testSurrogatePair() {
        // 1と魚花（ほっけ）と8の三文字を許可(ホッケはサロゲート領域の文字）
        target.setAllowedCharacters("1\uD867\uDE3D8");

        assertThat(target.contains(0x30), is(false));   // 0
        assertThat(target.contains(0x31), is(true));    // 1
        assertThat(target.contains(0x29E3D), is(true)); // ホッケ（魚花）
        assertThat(target.contains(0x38), is(true));    // 8
        assertThat(target.contains(0x39), is(false));   // 9
    }

    @Test
    public void testMessageId() {
        target.setMessageId("メッセージID");
        assertThat(target.getMessageId(), is("メッセージID"));
    }

}
