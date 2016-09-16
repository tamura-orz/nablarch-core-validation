package nablarch.core.validation;

import nablarch.core.message.StringResource;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * {@link ValidationResultMessage}のテスト。
 *
 * @author hisaaki sioiri
 */
public class ValidationResultMessageTest {

    @Test
    public void testCreateInstance() {
        ValidationResultMessage message = new ValidationResultMessage("prop", new StringResource() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getValue(Locale locale) {
                return "message";
            }
        }, new Object[0]);

        assertThat(message.getPropertyName(), is("prop"));
        assertThat(message.toString(), is(containsString("id")));
        assertThat(message.toString(), is(containsString("prop")));
        
        ValidationResultMessage other = new ValidationResultMessage("prop", new StringResource() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getValue(Locale locale) {
                return "message";
            }
        }, new Object[0]);
        
        assertThat(other.hashCode(), is(message.hashCode()));
        assertThat("同一インスタンス", message.equals(message), is(true));
        assertThat("ことなるインスタンスだけど状態は同じ", message.equals(other), is(true));

        assertThat("nullとは一致しない", message.equals(null), is(false));
        assertThat("異なるクラスとは一致しない", message.equals(""), is(false));

        ValidationResultMessage notEqualId = new ValidationResultMessage("prop", new StringResource() {
            @Override
            public String getId() {
                return "id1";
            }

            @Override
            public String getValue(Locale locale) {
                return "message";
            }
        }, new Object[0]);
        assertThat("メッセージIDが一致しない", message.equals(notEqualId), is(false));

        ValidationResultMessage notEqualProp = new ValidationResultMessage("prop1", new StringResource() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getValue(Locale locale) {
                return "message1";
            }
        }, new Object[0]);
        assertThat("プロパティが一致しない", message.equals(notEqualProp), is(false));
    }
}
