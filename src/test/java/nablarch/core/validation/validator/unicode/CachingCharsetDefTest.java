package nablarch.core.validation.validator.unicode;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CachingCharsetDef}のテストケース
 * @author T.Kawasaki
 */
public class CachingCharsetDefTest {
    private CachingCharsetDef target = new CachingCharsetDef();

    /**
     * 一度問い合わせたコードポイントの判定結果はキャッシュされること。
     */
    @Test
    public void testCached() {
        MockCharsetDef mock = new MockCharsetDef();
        mock.allowed.set(0x0020);     // U+0020が許容文字
        assertThat(mock.count, is(0));   // 初期値
        
        target.setCharsetDef(mock);
        
        //--- 許容文字と判定されるケース
        // 1回目
        assertThat(target.contains(0x0020), is(true));
        // 初回なのでキャッシュは効かないこと（呼び出し回数が増加すること）
        assertThat(mock.count, is(1));
        // 2回目
        // キャッシュが効いているので呼び出し回数が増えていないこと
        assertThat(target.contains(0x0020), is(true));
        assertThat(mock.count, is(1));   
        
        //--- 許容文字と判定されないケース
        // 1回目
        // 初回なのでキャッシュは効かないこと（呼び出し回数が増加すること）
        assertThat(target.contains(0x0021), is(false));
        assertThat(mock.count, is(2));   
        // 2回目
        // キャッシュが効いているので呼び出し回数が増えていないこと
        assertThat(target.contains(0x0021), is(false));
        assertThat(mock.count, is(2));   
    }

    /**
     * 委譲先の許容文字集合定義が設定されていない状態で
     * 判定メソッドを起動された場合、例外が発生すること。
     */
    @Test(expected = IllegalStateException.class)
    public void test() {
        target.contains(0);
    }

    /**
     * メッセージIDの設定、取得のテスト。
     */
    @Test
    public void testMessageId() {
        target.setMessageId("messageId");
        assertThat(target.getMessageId(), is("messageId"));
    }

    private static class MockCharsetDef extends CharsetDefSupport {

        int count = 0;
        final BitSet allowed = new BitSet();

        public boolean contains(int codePoint) {
            count++;
            return allowed.get(codePoint);
        }

    }
}
