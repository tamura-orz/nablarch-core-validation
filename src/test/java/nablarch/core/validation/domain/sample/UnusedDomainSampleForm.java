package nablarch.core.validation.domain.sample;

import nablarch.common.code.validator.CodeValue;
import nablarch.common.date.YYYYMMDD;
import nablarch.core.validation.*;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.validator.Length;
import nablarch.core.validation.validator.NumberRange;
import nablarch.core.validation.validator.Required;
import nablarch.core.validation.validator.unicode.SystemChar;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ドメインアノテーションを使用しないフォーム。
 * {@link SampleForm}と同じ内容で定義する。
 */
public class UnusedDomainSampleForm implements TestForm {

    private String freeText;
    private String userId; // @Required
    private String[] userIds;
    private String name;
    private String date;
    private String status;
    private String status2; // @CodeValue for other pattern
    private Boolean registered;
    private BigDecimal estimate;
    private Integer score;

    private UnusedDomainSampleDto sampleDto;

    private UnusedDomainSampleDto[] fixedLengthSampleDtos;

    private Integer sampleDtosSize;
    private UnusedDomainSampleDto[] variableLengthSampleDtos;

    public UnusedDomainSampleForm() {
    }

    public UnusedDomainSampleForm(Map<String, Object> params) {
        freeText = (String) params.get("freeText");
        userId = (String) params.get("userId");
        userIds = (String[]) params.get("userIds");
        name = (String) params.get("name");
        date = (String) params.get("date");
        status = (String) params.get("status");
        status2 = (String) params.get("status2");
        registered = (Boolean) params.get("registered");
        estimate = (BigDecimal) params.get("estimate");
        score = (Integer) params.get("score");

        sampleDto = (UnusedDomainSampleDto) params.get("sampleDto");

        fixedLengthSampleDtos = (UnusedDomainSampleDto[]) params.get("fixedLengthSampleDtos");

        sampleDtosSize = (Integer) params.get("sampleDtosSize");
        variableLengthSampleDtos = (UnusedDomainSampleDto[]) params.get("variableLengthSampleDtos");
    }

    public String getFreeText() {
        return freeText;
    }

    @PropertyName("フリーテキスト")
    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public String getUserId() {
        return userId;
    }

    @PropertyName("ユーザID")
    @Length(min=10, max=10)
    @Required
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String[] getUserIds() {
        return userIds;
    }

    @PropertyName("ユーザIDリスト")
    @Length(min=10, max=10)
    public void setUserIds(String[] userIds) {
        this.userIds = userIds;
    }

    public String getName() {
        return name;
    }

    @PropertyName("名前")
    @Length(min=0, max=40)
    @SystemChar
    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    @PropertyName("日付")
    @YYYYMMDD(allowFormat="yyyy-MM-dd")
    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    @PropertyName("ステータス")
    @CodeValue(codeId="0002", pattern="PATTERN1")
    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus2() {
        return status2;
    }

    @PropertyName("ステータス")
    @CodeValue(codeId="0002", pattern="PATTERN2")
    public void setStatus2(String status2) {
        this.status2 = status2;
    }

    public Boolean getRegistered() {
        return registered;
    }

    @PropertyName("登録済み")
    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }

    @PropertyName("見積工数")
    @Digits(integer=3, fraction=2)
    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
    }

    public Integer getScore() {
        return score;
    }

    @PropertyName("点数")
    @NumberRange(min=0, max=100)
    @Digits(integer=3)
    public void setScore(Integer score) {
        this.score = score;
    }

    public UnusedDomainSampleDto getSampleDto() {
        return sampleDto;
    }

    @ValidationTarget
    public void setSampleDto(UnusedDomainSampleDto sampleDto) {
        this.sampleDto = sampleDto;
    }

    public UnusedDomainSampleDto[] getFixedLengthSampleDtos() {
        return fixedLengthSampleDtos;
    }

    @ValidationTarget(size = 2)
    public void setFixedLengthSampleDtos(UnusedDomainSampleDto[] fixedLengthSampleDtos) {
        this.fixedLengthSampleDtos = fixedLengthSampleDtos;
    }

    public Integer getSampleDtosSize() {
        return sampleDtosSize;
    }

    @Digits(integer=1)
    @Required
    @PropertyName("SampleDtosサイズ")
    public void setSampleDtosSize(Integer sampleDtosSize) {
        this.sampleDtosSize = sampleDtosSize;
    }

    public UnusedDomainSampleDto[] getVariableLengthSampleDtos() {
        return variableLengthSampleDtos;
    }

    @ValidationTarget(sizeKey = "sampleDtosSize")
    public void setVariableLengthSampleDtos(UnusedDomainSampleDto[] variableLengthSampleDtos) {
        this.variableLengthSampleDtos = variableLengthSampleDtos;
    }

    @ValidateFor("test")
    public static void validateForTest(ValidationContext<SampleForm> context) {
        ValidationUtil.validateAll(context);
    }

    @ValidateFor("testScore")
    public static void validateForTestScore(ValidationContext<SampleForm> context) {
        ValidationUtil.validate(context, new String[] {"score"});
    }

    @ValidateFor("testDirectCallable")
    public static void validateForDirectCallable(ValidationContext<SampleForm> context) {
        ValidationUtil.validate(context, new String[] {"freeText"});
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("codeId", "0002");
        params.put("pattern", "PATTERN1");
        ValidationUtil.validate(context, "freeText", CodeValue.class, params);
    }
}
