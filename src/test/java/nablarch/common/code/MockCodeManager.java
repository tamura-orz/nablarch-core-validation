package nablarch.common.code;

import nablarch.common.code.BasicCodeManager;
import nablarch.core.ThreadContext;

import java.util.*;
import java.util.Map.Entry;

/**
 * CodeManagerのMockクラス。
 *
 * @author Naoki Yamamoto
 */
public class MockCodeManager extends BasicCodeManager {

	private Map<String, Map<String, String>> codeNames;

	private Map<String, Map<String, String>> codePatterns;

	public void setCodeNames(String[][] codeNameArray) {
		codeNames = new LinkedHashMap<String, Map<String, String>>();
		for (final String[] codeName : codeNameArray) {
			String key = codeName[0] + codeName[1] + codeName[3];
			Map<String, String> map = new HashMap<String, String>() {{
				put("VALUE", codeName[1]);
				put("SORT_ORDER", codeName[2]);
				put("LANG", codeName[3]);
				put("NAME", codeName[4]);
				put("SHORT_NAME", codeName[5]);
				put("NAME_WITH_VALUE", codeName[6]);
				put("OPTION01", codeName[7]);
			}};
			codeNames.put(key, map);
		}
	}

	public void setCodePatterns(String[][] codePatternArray) {
    	codePatterns = new LinkedHashMap<String, Map<String, String>>();
		for (final String[] codePattern : codePatternArray) {
			String key = codePattern[0] + codePattern[1];
			Map<String, String> map = new HashMap<String, String>() {{
				put("PATTERN1", codePattern[2]);
				put("PATTERN2", codePattern[3]);
				put("PATTERN3", codePattern[4]);
			}};
			codePatterns.put(key, map);
		}
	}


    /**
     * {@inheritDoc}
     */
	@Override
    public boolean contains(String codeId, String value) {
		return codePatterns.containsKey(codeId + value);
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public boolean contains(String codeId, String pattern, String value) {
		Map<String, String> codePattern = codePatterns.get(codeId + value);
		if (codePattern != null) {
			return codePattern.get(pattern).equals("1");
		}
		return false;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getName(String codeId, String value) {
		Map<String, String> codeName = codeNames.get(codeId + value + ThreadContext.getLanguage().getLanguage());
		if (codeName != null) {
			return codeName.get("NAME");
		} else {
			throw new IllegalArgumentException("name was not found."
                    + " code id = " + codeId);
		}
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getName(String codeId, String value, Locale locale) {
		Map<String, String> codeName = codeNames.get(codeId + value + locale.getLanguage());
		if (codeName != null) {
			return codeName.get("NAME");
		} else {
			throw new IllegalArgumentException("name was not found."
                    + " code id = " + codeId);
		}
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getShortName(String codeId, String value) {
		Map<String, String> codeName = codeNames.get(codeId + value + ThreadContext.getLanguage().getLanguage());
		if (codeName != null) {
			return codeName.get("SHORT_NAME");
		}
		return null;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getShortName(String codeId, String value, Locale locale) {
		Map<String, String> codeName = codeNames.get(codeId + value + locale.getLanguage());
		if (codeName != null) {
			return codeName.get("NAME");
		}
		return null;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getOptionalName(String codeId, String value,
            String optionColumnName) {
		Map<String, String> codeName = codeNames.get(codeId + value + ThreadContext.getLanguage().getLanguage());
		if (codeName != null) {
			return codeName.get(optionColumnName);
		}
		return null;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String getOptionalName(String codeId, String value,
            String optionColumnName, Locale locale) {
		Map<String, String> codeName = codeNames.get(codeId + value + locale.getLanguage());
		if (codeName != null) {
			return codeName.get(optionColumnName);
		}
		return null;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public List<String> getValues(String codeId) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, Map<String, String>> entry : codeNames.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(codeId) && key.endsWith(ThreadContext.getLanguage().getLanguage())) {
				result.add(entry.getValue().get("VALUE"));
			}
		}
        return result;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public List<String>  getValues(String codeId, String pattern) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, Map<String, String>> entry : codeNames.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(codeId) && key.endsWith(ThreadContext.getLanguage().getLanguage())) {
				String value = entry.getValue().get("VALUE");
				if (codePatterns.get(codeId + value).get(pattern) == "1") {
					result.add(value);
				}
			}
		}



        return result;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public List<String> getValues(String codeId, Locale locale) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, Map<String, String>> entry : codeNames.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(codeId) && key.endsWith(locale.getDisplayLanguage())) {
				result.add(entry.getValue().get("VALUE"));
			}
		}
        return result;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public List<String> getValues(String codeId, String pattern, Locale locale) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, Map<String, String>> entry : codeNames.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(codeId) && key.endsWith(locale.getLanguage())) {
				String value = entry.getValue().get("VALUE");
				if (codePatterns.get(codeId + value).get(pattern) == "1") {
					result.add(value);
				}
			}
		}
        return result;
    }
}
