package nablarch.core.validation;

import static nablarch.core.validation.ValidationContextMatcher.containsMessage;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.validation.ValidationContextMatcher.ValidationContextWrapper;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.Required;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * {@link ValidationUtil}のテストクラス。
 * <p/>
 * 本テストクラスでは、{@link nablarch.core.db.statement.SqlRow}をインプットとした
 * バリデーションのケースを実施する。
 */
public class ValidationUtilFromSqlRowTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "nablarch/core/validation/validation-util-from-sqlrow.xml");

    private static final String[][] MESSAGES = {
            {"PROP0001", "ja", "ID", "en", "ID"},
            {"PROP0002", "ja", "名前", "en", "Name"},
            {"PROP0003", "ja", "年齢"},
            {"PROP0003", "en", "Age"},
            {"MSG00001", "ja", "{0}の値が不正です。", "en", "{0} value is invalid."},
            {"MSG00011", "ja", "{0}は必ず入力してください。", "en", "{0} is required."},
            {"MSG00021", "ja", "{0}は{2}文字以下で入力してください。"},
            {"MSG00021", "en", "{0} cannot be greater than {2} characters."},
            {"MSG00022", "ja", "{0}は{1}文字以上{2}文字以下で入力してください。"},
            {"MSG00022", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00023", "ja", "{0}は{1}文字で入力してください。"},
            {"MSG00023", "en", "{0} length must be {1}."},
            {"MSG00031", "ja", "{0}は整数{1}桁で入力してください。"},
            {"MSG00031", "en", "{0} length must be under {1}."},
            {"MSG00041", "ja", "{0}は整数{1}桁で入力してください。"},
            {"MSG00041", "en", "{0} length must be under {1}."},
            {"MSG00042", "ja", "{0}は整数部{1}桁、少数部{2}桁で入力してください。"},
            {"MSG00042", "en", "{0} must be {1}-digits and {1}-digits decimal integer part."},
            {"MSG00051", "ja", "{0}は{2}以下で入力してください。"},
            {"MSG00051", "en", "{0} cannot be greater than {2}."},
            {"MSG00052", "ja", "{0}は{1}以上{2}以下で入力してください。"},
            {"MSG00052", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00053", "ja", "{0}は{1}以上{2}以下で入力してください。"},
            {"MSG00053", "en", "{0} is not in the range {1} through {2}."},
            {"MSG00061", "ja", "項目間バリデーションエラーメッセージ。"},
            {"MSG00061", "en", "inter property check error message."},
            {"MSG00071", "ja", "入力値が不正です。"},
            {"MSG00071", "en", "input value is invalid."},
            {"MSG00081", "ja", "サイズキーが不正です。"},
            {"MSG00081", "en", "size key is invalid."},
            {"MSG00091", "ja", "エラーメッセージサンプル１。"},
            {"MSG00091", "en", "sample error message1."},
            {"MSG00092", "ja", "エラーメッセージサンプル２ [{0}][{1}]。"},
            {"MSG00092", "en", "sample error message2  [{0}][{1}]."},
            {"MSG00093", "ja", "エラーメッセージサンプル３ [{0,number,#.00}]。"},
            {"MSG00093", "en", "sample error message3  [{0,number,#.00}]."},
    };

    @Before
    public void classSetup() throws Exception {

        repositoryResource.getComponentByType(MockStringResourceHolder.class)
                          .setMessages(MESSAGES);
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[] {"10"});
    }

    /** {@link nablarch.core.db.statement.SqlRow}をインプットとするバリデーションのテスト。 */
    @Test
    public void testValidationFromSqlRow() throws SQLException {

        ThreadContext.setLanguage(Locale.JAPANESE);

        Map<String, Integer> colType = new HashMap<String, Integer>();
        colType.put("charCol", java.sql.Types.VARCHAR);
        colType.put("varcharCol", java.sql.Types.LONGNVARCHAR);
        colType.put("number1Col", java.sql.Types.INTEGER);
        colType.put("number9Col", java.sql.Types.INTEGER);
        colType.put("number10Col", java.sql.Types.BIGINT);
        colType.put("number18Col", java.sql.Types.BIGINT);
        colType.put("number30Col", java.sql.Types.NUMERIC);
        colType.put("number1010Col", java.sql.Types.NUMERIC);

        //**********************************************************************
        // 1行目のアサート
        //**********************************************************************
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("charCol", "00001");
        map.put("varcharCol", "あいうえお");
        map.put("number1Col", "1");
        map.put("number9Col", "123456789");
        map.put("number10Col", "9999999999");
        map.put("number18Col", "123456789012345678");
        map.put("number30Col", "999999999999999999999999999999");
        map.put("number1010Col", "1234567890.0123456789");
        SqlRow row1 = new SqlRow(map, colType);
        ValidationContext<TestEntity> context1 = ValidationUtil
                .validateAndConvertRequest(null, TestEntity.class, row1, "test");
        Assert.assertTrue(context1.isValid());
        TestEntity testEntity1 = context1.createObject();
        assertThat(testEntity1.charCol, is("00001"));
        assertThat(testEntity1.varcharCol, is("あいうえお"));
        assertThat(testEntity1.number1Col, is(1));
        assertThat(testEntity1.number9Col, is(123456789));
        assertThat(testEntity1.number10Col, is(9999999999L));
        assertThat(testEntity1.number18Col, is(123456789012345678L));
        assertThat(testEntity1.number30Col, is(new BigDecimal("999999999999999999999999999999")));
        assertThat(testEntity1.number1010Col, is(new BigDecimal("1234567890.0123456789")));

        //**********************************************************************
        // 3行目（全項目nullのケース)
        // 全て必須項目の場合、エラーになる。
        //**********************************************************************
        map = new HashMap<String, Object>();
        map.put("charCol", null);
        map.put("varcharCol", null);
        map.put("number1Col", null);
        map.put("number9Col", null);
        map.put("number10Col", null);
        map.put("number18Col", null);
        map.put("number30Col", null);
        map.put("number1010Col", null);
        SqlRow row3 = new SqlRow(map, colType);
        ValidationContext<TestEntity> context3 = ValidationUtil
                .validateAndConvertRequest(null, TestEntity.class, row3, "test");
        Assert.assertFalse(context3.isValid());
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "charColは必ず入力してください。", "charCol"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "varcharColは必ず入力してください。", "varcharCol"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number1Colは必ず入力してください。", "number1Col"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number9Colは必ず入力してください。", "number9Col"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number10Colは必ず入力してください。", "number10Col"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number18Colは必ず入力してください。", "number18Col"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number30Colは必ず入力してください。", "number30Col"));
        assertThat(new ValidationContextWrapper(context3),
                containsMessage("MSG00011", "number1010Colは必ず入力してください。", "number1010Col"));
        //**********************************************************************
        // 3行目（全項目nullのケース)
        // 必須項目でない場合、全ての項目がnullとなる。
        //**********************************************************************
        ValidationContext<TestEntityPermitNullValue> nullValueValidationContext = ValidationUtil
                .validateAndConvertRequest(null, TestEntityPermitNullValue.class, row3, "test");
        Assert.assertTrue(nullValueValidationContext.isValid());
        TestEntityPermitNullValue entityPermitNullValue = nullValueValidationContext
                .createObject();
        assertNull(entityPermitNullValue.charCol);
        assertNull(entityPermitNullValue.varcharCol);
        assertNull(entityPermitNullValue.number1Col);
        assertNull(entityPermitNullValue.number9Col);
        assertNull(entityPermitNullValue.number9Col);
        assertNull(entityPermitNullValue.number18Col);
        assertNull(entityPermitNullValue.number30Col);
        assertNull(entityPermitNullValue.number1010Col);

    }

    public static class TestEntity {

        private String charCol;

        private String varcharCol;

        private Integer number1Col;

        private Integer number9Col;

        private Long number10Col;

        private Long number18Col;

        private BigDecimal number30Col;

        private BigDecimal number1010Col;

        public TestEntity(Map<String, ?> data) {
            charCol = (String) data.get("charCol");
            varcharCol = (String) data.get("varcharCol");
            number1Col = (Integer) data.get("number1Col");
            number9Col = (Integer) data.get("number9Col");
            number10Col = (Long) data.get("number10Col");
            number18Col = (Long) data.get("number18Col");
            number30Col = (BigDecimal) data.get("number30Col");
            number1010Col = (BigDecimal) data.get("number1010Col");
        }

        @Required
        @Length(max = 5, min = 5)
        public void setCharCol(String charCol) {
            this.charCol = charCol;
        }

        @Required
        @Length(max = 15)
        public void setVarcharCol(String varcharCol) {
            this.varcharCol = varcharCol;
        }

        @Required
        @Digits(integer = 1)
        public void setNumber1Col(Integer number1Col) {
            this.number1Col = number1Col;
        }

        @Required
        @Digits(integer = 9)
        public void setNumber9Col(Integer number9Col) {
            this.number9Col = number9Col;
        }

        @Required
        @Digits(integer = 10)
        public void setNumber10Col(Long number10Col) {
            this.number10Col = number10Col;
        }

        @Required
        @Digits(integer = 18)
        public void setNumber18Col(Long number18Col) {
            this.number18Col = number18Col;
        }

        @Required
        @Digits(integer = 30, fraction = 0)
        public void setNumber30Col(BigDecimal number30Col) {
            this.number30Col = number30Col;
        }

        @Required
        @Digits(integer = 10, fraction = 10)
        public void setNumber1010Col(BigDecimal number1010Col) {
            this.number1010Col = number1010Col;
        }

        @ValidateFor("test")
        public static void validation(ValidationContext<TestEntity> context) {
            ValidationUtil.validate(context,
                    new String[] {"charCol", "varcharCol", "number1Col",
                            "number9Col", "number10Col", "number18Col", "number30Col", "number1010Col"});
        }
    }

    public static class TestEntityPermitNullValue {

        private String charCol;

        private String varcharCol;

        private Integer number1Col;

        private Integer number9Col;

        private Long number10Col;

        private Long number18Col;

        private BigDecimal number30Col;

        private BigDecimal number1010Col;

        public TestEntityPermitNullValue(Map<String, ?> data) {
            charCol = (String) data.get("charCol");
            varcharCol = (String) data.get("varcharCol");
            number1Col = (Integer) data.get("number1Col");
            number9Col = (Integer) data.get("number9Col");
            number10Col = (Long) data.get("number10Col");
            number18Col = (Long) data.get("number18Col");
            number30Col = (BigDecimal) data.get("number30Col");
            number1010Col = (BigDecimal) data.get("number1010Col");
        }

        @Length(max = 5, min = 5)
        public void setCharCol(String charCol) {
            this.charCol = charCol;
        }

        @Length(max = 15)
        public void setVarcharCol(String varcharCol) {
            this.varcharCol = varcharCol;
        }

        @Digits(integer = 1)
        public void setNumber1Col(Integer number1Col) {
            this.number1Col = number1Col;
        }

        @Digits(integer = 9)
        public void setNumber9Col(Integer number9Col) {
            this.number9Col = number9Col;
        }

        @Digits(integer = 10)
        public void setNumber10Col(Long number10Col) {
            this.number10Col = number10Col;
        }

        @Digits(integer = 18)
        public void setNumber18Col(Long number18Col) {
            this.number18Col = number18Col;
        }

        @Digits(integer = 30, fraction = 0)
        public void setNumber30Col(BigDecimal number30Col) {
            this.number30Col = number30Col;
        }

        @Digits(integer = 10, fraction = 10)
        public void setNumber1010Col(BigDecimal number1010Col) {
            this.number1010Col = number1010Col;
        }

        @ValidateFor("test")
        public static void validation(ValidationContext<TestEntity> context) {
            ValidationUtil.validate(context,
                    new String[] {"charCol", "varcharCol", "number1Col",
                            "number9Col", "number10Col", "number18Col", "number30Col", "number1010Col"});
        }
    }
}

