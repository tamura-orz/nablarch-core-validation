package nablarch.core.validation.domain.sample;

import java.math.BigDecimal;
import java.util.Map;

import nablarch.core.validation.PropertyName;
import nablarch.core.validation.ValidateFor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationUtil;
import nablarch.core.validation.validator.Required;

public class SampleDto implements TestDto {

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

    public SampleDto() {
    }

    public SampleDto(Map<String, Object> params) {
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
    }

    public String getFreeText() {
        return freeText;
    }

    @PropertyName("フリーテキスト")
    @Domain(DomainType.FREE_TEXT)
    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public String getUserId() {
        return userId;
    }

    @PropertyName("ユーザID")
    @Domain(DomainType.USER_ID)
    @Required
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String[] getUserIds() {
        return userIds;
    }

    @PropertyName("ユーザIDリスト")
    @Domain(DomainType.USER_ID)
    public void setUserIds(String[] userIds) {
        this.userIds = userIds;
    }

    public String getName() {
        return name;
    }

    @PropertyName("名前")
    @Domain(DomainType.NAME)
    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    @PropertyName("日付")
    @Domain(DomainType.DATE)
    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    @PropertyName("ステータス")
    @Domain(DomainType.STATUS)
    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus2() {
        return status2;
    }

    @PropertyName("ステータス")
    @Domain(DomainType.STATUS)
    public void setStatus2(String status2) {
        this.status2 = status2;
    }

    public Boolean getRegistered() {
        return registered;
    }

    @PropertyName("登録済み")
    @Domain(DomainType.REGISTERED)
    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }

    @PropertyName("見積工数")
    @Domain(DomainType.ESTIMATE)
    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
    }

    public Integer getScore() {
        return score;
    }

    @PropertyName("点数")
    @Domain(DomainType.SCORE)
    public void setScore(Integer score) {
        this.score = score;
    }

    @ValidateFor("test")
    public static void validateForTest(ValidationContext<SampleForm> context) {
        ValidationUtil.validateAll(context);
    }

    @ValidateFor("testScore")
    public static void validateForTestScore(ValidationContext<SampleForm> context) {
        ValidationUtil.validate(context, new String[] {"score"});
    }
}
