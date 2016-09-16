package nablarch.core.validation.domain.sample;

import java.math.BigDecimal;

public interface TestDto {
    String getFreeText();
    String getUserId();
    String[] getUserIds();
    String getName();
    String getDate();
    String getStatus();
    String getStatus2();
    Boolean getRegistered();
    BigDecimal getEstimate();
    Integer getScore();
}
