package hu.webarticum.resourcemanager.config;

import java.util.regex.Pattern;

/**
 * Property parser for boolean config items.
 *
 * Interprets 'true', '1', 'yes' and 'on' as <code>true</code>;
 * and '' (empty string), 'false', '0', 'no' and 'off'
 * as <code>false</code> (all case-insensitively).
 * Other values are invalid, and cause an {@link IllegalArgumentException}.
 */
public class BooleanValueParser implements ValueParser<Boolean> {

    private static final Pattern TRUE_PATTERN = Pattern.compile(
            "true|1|yes|on", Pattern.CASE_INSENSITIVE);

    private static final Pattern FALSE_PATTERN = Pattern.compile(
            "|false|0|no|off", Pattern.CASE_INSENSITIVE);


    @Override
    public Boolean parse(String value) {
        if (TRUE_PATTERN.matcher(value).matches()) {
            return Boolean.TRUE;
        } else if (FALSE_PATTERN.matcher(value).matches()) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid boolean value: '%s'", value));
        }
    }

}
