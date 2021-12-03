package hu.webarticum.resourcemanager.config;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ValueParsers {

    private ValueParsers() {
        // constant class
    }


    public static final ValueParser<Boolean> BOOLEAN = new BooleanValueParser();

    public static final ValueParser<Byte> BYTE = Byte::valueOf;

    public static final ValueParser<Character> CHARACTER = value -> {
        if (value.length() != 1) {
            throw new IllegalArgumentException(String.format("Invalid character value: '%s'", value));
        }

        return value.charAt(0);
    };

    public static final ValueParser<Short> SHORT = Short::valueOf;

    public static final ValueParser<Integer> INTEGER = Integer::valueOf;

    public static final ValueParser<Long> LONG = Long::valueOf;

    public static final ValueParser<Float> FLOAT = Float::valueOf;

    public static final ValueParser<Double> DOUBLE = Double::valueOf;

    public static final ValueParser<BigInteger> BIG_INTEGER = BigInteger::new;

    public static final ValueParser<BigDecimal> BIG_DECIMAL = BigDecimal::new;

    public static final ValueParser<String> STRING = String::new;
    
    
    public static <T extends Enum<T>> ValueParser<T> enumParser(Class<T> enumClass) {
        return name -> Enum.valueOf(enumClass, name);
    }

}
