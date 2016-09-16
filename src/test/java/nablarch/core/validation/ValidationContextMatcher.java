package nablarch.core.validation;

import nablarch.core.message.Message;
import nablarch.core.util.annotation.Published;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.List;

/**
 * VAlidationContext用のMatcherクラス。
 *
 * @author sioiri
 */
public class ValidationContextMatcher extends TypeSafeMatcher<ValidationContextMatcher.ValidationContextWrapper> {

    private final String messageId;

    private final String message;

    private final String propertyName;

    public ValidationContextMatcher(String messageId,
            String message, String propertyName) {
        this.messageId = messageId;
        this.message = message;
        this.propertyName = propertyName;
    }

    public void describeTo(Description description) {
        description.appendText("message id = " + messageId + ", ");
        description.appendText("message = " + message + ", ");
        description.appendText("property name = " + propertyName);
    }

    @Override
    public boolean matchesSafely(ValidationContextWrapper contextWrapper) {
        for (Message msg : contextWrapper.getMessages()) {
            // 一致するメッセージに対して比較を実施
            if (msg.getMessageId().equals(messageId) && message.equals(msg.formatMessage())) {
                // プロパティ名の比較
                if (propertyName != null) {
                    if (!propertyName.equals(((ValidationResultMessage) msg)
                            .getPropertyName())) {
                        return false;
                    }
                } else {
                    if (msg instanceof ValidationResultMessage) {
                        if ((((ValidationResultMessage) msg).getPropertyName())
                                != null) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        // ここまで処理が来る場合は、一致するメッセージIDが存在しなかった場合
        return false;
    }

    /**
     * メッセージの存在チェック。
     *
     * @param messageId 期待するメッセージID
     * @param message 期待するメッセージ
     * @return 期待するメッセージの存在チェック用 {@link Matcher}
     */
    public static Matcher<ValidationContextWrapper> containsMessage(
            final String messageId, final String message) {
        return containsMessage(messageId, message, null);
    }

    /**
     * メッセージの存在チェック。
     *
     * @param messageId 期待するメッセージID
     * @param message 期待するメッセージ
     * @param propertyName 期待するプロパティ名
     * @return 期待するメッセージの存在チェック用 {@link Matcher}
     */
    public static Matcher<ValidationContextWrapper> containsMessage(
            final String messageId, final String message,
            final String propertyName) {
        return new ValidationContextMatcher(messageId, message,
                propertyName);
    }

    /**
     * 実行結果のValidationContextをラップするクラス。
     * <br/>
     * 本クラスでラップすることにより、Junit実行結果のコンソールに実行結果を文字列で出力することが可能となる。
     */
    public static class ValidationContextWrapper {

        private ValidationContext<?> context;

        public ValidationContextWrapper(ValidationContext<?> context) {
            this.context = context;
        }

        @Published
        public List<Message> getMessages() {
            return context.getMessages();
        }

        @Override
        public String toString() {
            List<Message> messages = context.getMessages();
            StringBuilder sb = new StringBuilder(100);
            for (Message message : messages) {
                sb.append("message id = ");
                sb.append(message.getMessageId());
                sb.append(", message = ");
                sb.append(message.formatMessage());
                if (message instanceof ValidationResultMessage) {
                    sb.append(", property name = ");
                    sb.append(
                            ((ValidationResultMessage) message)
                                    .getPropertyName());
                }
                sb.append("\n");
            }
            return sb.substring(0, sb.length() - 1);
        }
    }
}
